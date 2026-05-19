type QueryValue = string | number | boolean | null | undefined;

const rawBaseUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;
export const API_BASE_URL = rawBaseUrl?.trim().replace(/\/+$/, '') ?? '';

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

  const headers = new Headers(options.headers);

  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  headers.set('Accept', 'application/json');

  function readPersistedAuth(): any | null {
    try {
      const raw = localStorage.getItem('donaton-auth');
      if (!raw) return null;
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  function extractRefreshToken(persisted: any): string | null {
    if (!persisted || typeof persisted !== 'object') return null;

    const candidates = [
      persisted.refreshToken,
      persisted.state && persisted.state.refreshToken,
      persisted.value && persisted.value.refreshToken,
    ];

    const direct = candidates.find(v => typeof v === 'string' && v.length > 0);
    if (direct) return direct;

    
    const stack: any[] = [persisted];
    while (stack.length) {
      const obj = stack.shift();
      if (!obj || typeof obj !== 'object') continue;
      if (typeof obj.refreshToken === 'string' && obj.refreshToken.length) return obj.refreshToken;
      for (const v of Object.values(obj)) {
        if (v && typeof v === 'object') stack.push(v);
      }
    }
    return null;
  }

  function tryUpdatePersistedTokens(newAccessToken: string, newRefreshToken?: string | null) {
    try {
      const persisted = readPersistedAuth();
      if (!persisted || typeof persisted !== 'object') return;

      const updated = structuredClone(persisted);

      if (updated.state && typeof updated.state === 'object') {
        updated.state.token = newAccessToken;
        if (newRefreshToken) updated.state.refreshToken = newRefreshToken;
      } else {
        (updated as any).token = newAccessToken;
        if (newRefreshToken) (updated as any).refreshToken = newRefreshToken;
      }

      localStorage.setItem('donaton-auth', JSON.stringify(updated));
    } catch {
      
    }
  }

  
  try {
    const raw = localStorage.getItem('donaton-auth');
    if (raw) {
      const parsed = JSON.parse(raw);

      
      const tokenCandidates = [
        parsed && parsed.token,
        parsed && parsed.state && parsed.state.token,
        parsed && parsed.value && parsed.value.token,
      ];

      
      if (!tokenCandidates.some(Boolean)) {
        const stack: any[] = [parsed];
        while (stack.length) {
          const obj = stack.shift();
          if (!obj || typeof obj !== 'object') continue;
          if (typeof obj.token === 'string') {
            tokenCandidates.push(obj.token);
            break;
          }
          for (const v of Object.values(obj)) {
            if (v && typeof v === 'object') stack.push(v as any);
          }
        }
      }

      const token = tokenCandidates.find(t => typeof t === 'string') as string | undefined;
      if (token && !headers.has('Authorization')) {
        
        try { console.debug('api: attaching Authorization header'); } catch (e) {}
        headers.set('Authorization', `Bearer ${token}`);
      }
    }
  } catch (e) {
    
  }

  let response: Response;

  const bodyPayload = options.body === undefined ? undefined : options.body instanceof FormData ? options.body : JSON.stringify(options.body);

  try {
    response = await fetch(buildUrl(path, options.query), {
      method: options.method ?? 'GET',
      headers,
      body: bodyPayload,
    });
  } catch (error) {
    const err = new Error('No se pudo conectar con el backend. Verifica que el gateway esté activo y que la API permita solicitudes desde este origen.') as any;
    err.cause = error;
    throw err;
  }

  
  
  if (response.status === 401) {
    try {
      const sentAuth = headers.get('Authorization') ?? '';
      if (sentAuth.startsWith('Bearer ')) {
        const rawToken = sentAuth.slice(7);
        const retryHeaders = new Headers(headers);
        retryHeaders.set('Authorization', rawToken);

        const retryResp = await fetch(buildUrl(path, options.query), {
          method: options.method ?? 'GET',
          headers: retryHeaders,
          body: bodyPayload,
        });

        
        if (retryResp.ok) {
          response = retryResp;
        } else {
          
          
          try { (response as any).retryAttempted = true; } catch (e) {}
        }
      }
    } catch (e) {
      
    }
  }

  
  if (response.status === 401 && !isAuthPath) {
    try {
      const persisted = readPersistedAuth();
      const refreshToken = extractRefreshToken(persisted);

      const canRefresh = !!refreshToken;
      const hasAuthHeader = headers.has('Authorization');

      if (canRefresh && hasAuthHeader) {
        const refreshHeaders = new Headers();
        refreshHeaders.set('Accept', 'application/json');
        refreshHeaders.set('Content-Type', 'application/json');

        const refreshResp = await fetch(buildUrl('/auth/refresh'), {
          method: 'POST',
          headers: refreshHeaders,
          body: JSON.stringify({ refreshToken }),
        });

        if (refreshResp.ok) {
          const refreshText = await refreshResp.text();
          let refreshPayload: any = null;
          try {
            refreshPayload = refreshText ? JSON.parse(refreshText) : null;
          } catch {
            refreshPayload = refreshText;
          }

          const newAccessToken = refreshPayload && typeof refreshPayload === 'object'
            ? (refreshPayload.accessToken ?? refreshPayload.token ?? null)
            : (typeof refreshPayload === 'string' ? refreshPayload : null);
          const newRefreshToken = refreshPayload && typeof refreshPayload === 'object'
            ? (refreshPayload.refreshToken ?? refreshToken)
            : refreshToken;

          if (typeof newAccessToken === 'string' && newAccessToken.length) {
            tryUpdatePersistedTokens(newAccessToken, typeof newRefreshToken === 'string' ? newRefreshToken : null);

            const retryHeaders = new Headers(headers);
            retryHeaders.set('Authorization', `Bearer ${newAccessToken}`);

            const retryResp = await fetch(buildUrl(path, options.query), {
              method: options.method ?? 'GET',
              headers: retryHeaders,
              body: bodyPayload,
            });

            response = retryResp;
          }
        }
      }
    } catch {
      
    }
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
      } catch (e) {
        
        
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
      if (response.status === 401) {
        window.dispatchEvent(new CustomEvent('donaton:http-error', { detail: { status: 401, message } }));
      }

      if (response.status === 403) {
        window.dispatchEvent(new CustomEvent('donaton:force-login', { detail: { status: 403, message } }));

        
        try { localStorage.removeItem('donaton-auth'); } catch {}

        
        if (window.location.pathname !== '/login') {
          window.location.assign('/login');
        }
      }
    } catch {
      
    }

    const err = new Error(message) as any;
    err.status = response.status;
    err.statusText = response.statusText;
    err.payload = payload;
    
    try {
      err.requestHeaders = {} as Record<string,string>;
      headers.forEach((v, k) => { if (k.toLowerCase() !== 'authorization') err.requestHeaders[k] = v; else err.requestHeaders[k] = 'REDACTED'; });
    } catch (e) {}
    throw err;
  }

  return payload as T;
}