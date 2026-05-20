import {
  getAccessToken,
  getRefreshToken,
  isAccessTokenExpired,
  setAuthTokens,
} from '../lib/authSession';

type QueryValue = string | number | boolean | null | undefined;

const rawBaseUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;
export const API_BASE_URL = rawBaseUrl?.trim().replace(/\/+$/, '') ?? '';

let refreshPromise: Promise<string | null> | null = null;

function buildUrl(path: string, query?: Record<string, QueryValue>) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = new URL(`${API_BASE_URL}${normalizedPath}`, window.location.origin);

  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        url.searchParams.set(key, String(value));
      }
    });
  }

  return url.toString();
}

function attachAuthHeader(headers: Headers) {
  const token = getAccessToken();
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }
}

function extractTokensFromPayload(payload: unknown): { access: string | null; refresh: string | null } {
  if (!payload || typeof payload !== 'object') {
    return { access: typeof payload === 'string' ? payload : null, refresh: null };
  }

  const obj = payload as Record<string, unknown>;
  const nested = obj.data && typeof obj.data === 'object' ? (obj.data as Record<string, unknown>) : null;
  const source = nested ?? obj;

  const access = [source.accessToken, source.token]
    .find((v): v is string => typeof v === 'string' && v.length > 0) ?? null;
  const refresh = typeof source.refreshToken === 'string' && source.refreshToken.length > 0
    ? source.refreshToken
    : null;

  return { access, refresh };
}

async function refreshAccessToken(): Promise<string | null> {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    const storedRefresh = getRefreshToken();
    if (!storedRefresh) return null;

    const refreshResp = await fetch(buildUrl('/auth/refresh'), {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken: storedRefresh }),
    });

    if (!refreshResp.ok) return null;

    const refreshPayload = await refreshResp.json();
    const { access, refresh } = extractTokensFromPayload(refreshPayload);
    if (!access) return null;

    setAuthTokens(access, refresh ?? storedRefresh);

    const { syncAuthTokensToStore } = await import('../store/authStore');
    syncAuthTokensToStore(access, refresh ?? storedRefresh);

    return access;
  })().finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

async function fetchWithAuth(
  path: string,
  options: {
    method?: string;
    body?: unknown;
    query?: Record<string, QueryValue>;
    headers?: HeadersInit;
  },
  allowRefresh: boolean,
): Promise<Response> {
  const headers = new Headers(options.headers);

  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  headers.set('Accept', 'application/json');
  attachAuthHeader(headers);

  const bodyPayload = options.body === undefined
    ? undefined
    : options.body instanceof FormData
      ? options.body
      : JSON.stringify(options.body);

  let response = await fetch(buildUrl(path, options.query), {
    method: options.method ?? 'GET',
    headers,
    body: bodyPayload,
  });

  if (response.status !== 401 || !allowRefresh) {
    return response;
  }

  // Reintento si el token existía pero no se envió (carrera tras login)
  if (!headers.has('Authorization')) {
    const token = getAccessToken();
    if (token) {
      const retryHeaders = new Headers(headers);
      retryHeaders.set('Authorization', `Bearer ${token}`);
      response = await fetch(buildUrl(path, options.query), {
        method: options.method ?? 'GET',
        headers: retryHeaders,
        body: bodyPayload,
      });
      if (response.status !== 401) return response;
    }
  }

  const newAccess = await refreshAccessToken();
  if (!newAccess) return response;

  const retryHeaders = new Headers(headers);
  retryHeaders.set('Authorization', `Bearer ${newAccess}`);

  return fetch(buildUrl(path, options.query), {
    method: options.method ?? 'GET',
    headers: retryHeaders,
    body: bodyPayload,
  });
}

export async function requestJson<T>(
  path: string,
  options: {
    method?: string;
    body?: unknown;
    query?: Record<string, QueryValue>;
    headers?: HeadersInit;
  } = {},
): Promise<T> {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const isAuthPath = normalizedPath.startsWith('/auth/');

  let response: Response;
  try {
    response = await fetchWithAuth(path, options, !isAuthPath);
  } catch (error) {
    const err = new Error('No se pudo conectar con el backend. Verifica que el gateway esté activo y que la API permita solicitudes desde este origen.') as any;
    err.cause = error;
    throw err;
  }

  const contentType = response.headers.get('content-type') ?? '';

  let payload: any = null;
  if (response.status === 204) {
    payload = null;
  } else {
    const textBody = await response.text();
    const hasJsonBody = contentType.includes('application/json');

    if (hasJsonBody && textBody) {
      try {
        payload = JSON.parse(textBody);
      } catch {
        payload = textBody;
      }
    } else {
      payload = textBody;
    }
  }

  if (!response.ok) {
    const defaultMsg = typeof payload === 'string'
      ? payload
      : payload && typeof payload === 'object' && 'message' in payload
        ? String((payload as { message?: string }).message)
        : `Request failed with status ${response.status}`;

    const isAuth = response.status === 401;
    const message = isAuth
      ? (typeof payload === 'string' && payload.length ? payload : 'Unauthorized: token missing or invalid')
      : defaultMsg;

    try {
      if (response.status === 401 && !isAuthPath && getAccessToken()) {
        const { logoutFromApi } = await import('../store/authStore');
        logoutFromApi();
        window.dispatchEvent(new CustomEvent('donaton:force-login', {
          detail: { status: 401, message },
        }));
      }

      if (response.status === 403) {
        const { logoutFromApi } = await import('../store/authStore');
        logoutFromApi();
        window.dispatchEvent(new CustomEvent('donaton:force-login', { detail: { status: 403, message } }));
      }
    } catch {
      // ignore
    }

    const err = new Error(message) as any;
    err.status = response.status;
    err.statusText = response.statusText;
    err.payload = payload;
    throw err;
  }

  return payload as T;
}

export function validateStoredAccessToken(): boolean {
  const token = getAccessToken();
  if (!token) return false;
  return !isAccessTokenExpired(token);
}
