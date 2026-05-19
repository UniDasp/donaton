import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, LoginCredentials, RegisterData, Permission, UserRole } from '../types';
import { MOCK_USERS } from '../mock/users';
import { requestJson } from '../services/api';

const ROLE_PERMISSIONS_MAP: Record<UserRole, Permission[]> = {
  admin: [
    'dashboard:view', 'donations:view', 'donations:create', 'donations:edit', 'donations:delete',
    'needs:view', 'needs:create', 'needs:edit', 'needs:delete',
    'logistics:view', 'logistics:edit', 'users:manage', 'reports:view', 'settings:manage'
  ],
  operador: [
    'dashboard:view', 'donations:view', 'donations:create', 'donations:edit',
    'needs:view', 'needs:create', 'needs:edit', 'logistics:view', 'logistics:edit'
  ],
  coordinador: [
    'dashboard:view', 'donations:view', 'needs:view', 'needs:create', 'needs:edit',
    'logistics:view', 'logistics:edit', 'reports:view'
  ],
  donante: ['dashboard:view', 'donations:view', 'donations:create'],
  voluntario: ['dashboard:view', 'donations:view', 'logistics:view'],
};

function decodeJwtPayload(token: string): any | null {
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;

    const payloadPart = parts[1];
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');

    const json = decodeURIComponent(
      Array.from(atob(padded))
        .map(c => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

function roleFromAccessToken(token: string | null | undefined): UserRole | null {
  if (!token) return null;
  const payload = decodeJwtPayload(token);
  const apiRole = payload && typeof payload === 'object' ? (payload.role as string | undefined) : undefined;
  if (apiRole === 'ADMIN') return 'admin';
  if (apiRole === 'ONG') return 'coordinador';
  if (apiRole === 'USER') return 'donante';
  return null;
}

function buildUserProfile(email: string, role: UserRole, name?: string, phone?: string): User {
  return {
    id: email,
    name: name ?? email.split('@')[0],
    email,
    role,
    phone,
    createdAt: new Date().toISOString(),
    lastLogin: new Date().toISOString(),
    permissions: ROLE_PERMISSIONS_MAP[role],
  };
}

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (data: RegisterData) => Promise<boolean>;
  refreshAccessToken: () => Promise<void>;
  logout: () => void;
  clearError: () => void;
  hasPermission: (permission: Permission) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (credentials) => {
        set({ isLoading: true, error: null });

        try {
          const raw = await requestJson<unknown>('/auth/login', {
            method: 'POST',
            body: credentials,
          });

          const token = typeof raw === 'string'
            ? raw
            : (raw && typeof raw === 'object'
              ? ((raw as any).accessToken ?? (raw as any).token ?? null)
              : null);

          const refreshToken = raw && typeof raw === 'object'
            ? ((raw as any).refreshToken ?? null)
            : null;

          const demoUser = MOCK_USERS.find(u => u.email === credentials.email);
          const derivedRole = roleFromAccessToken(token) ?? demoUser?.role ?? 'donante';
          const user = demoUser
            ? { ...demoUser, role: derivedRole, permissions: ROLE_PERMISSIONS_MAP[derivedRole], lastLogin: new Date().toISOString() }
            : buildUserProfile(credentials.email, derivedRole, credentials.email.split('@')[0]);

          set({
            user,
            token,
            refreshToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error) {
          const status = error && typeof error === 'object' ? (error as any).status : undefined;
          const message = status === 500
            ? 'Credenciales inválidas'
            : error instanceof Error
              ? error.message
              : 'Credenciales inválidas';

          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: message,
          });
        }
      },

      register: async (data) => {
        set({ isLoading: true, error: null });

        if (data.password !== data.confirmPassword) {
          set({ isLoading: false, error: 'Las contraseñas no coinciden' });
          return false;
        }

        try {
          await requestJson<{ id: number; email: string; name?: string; phone?: string; role: 'USER' | 'ADMIN' | 'ONG' }>('/auth/register', {
            method: 'POST',
            body: {
              name: data.name,
              email: data.email,
              phone: data.phone,
              password: data.password,
            },
          });

          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
          return true;
        } catch (error) {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: error instanceof Error ? error.message : 'No se pudo registrar la cuenta',
          });
          return false;
        }
      },

      refreshAccessToken: async () => {
        const { refreshToken, user, isAuthenticated } = get();
        if (!isAuthenticated || !user) return;
        if (!refreshToken) return;

        try {
          const raw = await requestJson<unknown>('/auth/refresh', {
            method: 'POST',
            body: { refreshToken },
          });

          const newAccessToken = raw && typeof raw === 'object'
            ? ((raw as any).accessToken ?? (raw as any).token ?? null)
            : (typeof raw === 'string' ? raw : null);

          const newRefreshToken = raw && typeof raw === 'object'
            ? ((raw as any).refreshToken ?? refreshToken)
            : refreshToken;

          if (newAccessToken) {
            set({ token: newAccessToken, refreshToken: newRefreshToken });
          }
        } catch {
          
          set({ user: null, token: null, refreshToken: null, isAuthenticated: false, isLoading: false });
        }
      },

      logout: () => {
        set({ 
          user: null, 
          token: null,
          refreshToken: null,
          isAuthenticated: false, 
          isLoading: false, 
          error: null 
        });
      },

      clearError: () => {
        set({ error: null });
      },

      hasPermission: (permission: Permission) => {
        const { user } = get();
        if (!user) return false;
        return user.permissions.includes(permission);
      }
    }),
    {
      name: 'donaton-auth',
      partialize: (state) => ({ user: state.user, token: state.token, refreshToken: state.refreshToken, isAuthenticated: state.isAuthenticated })
    }
  )
);