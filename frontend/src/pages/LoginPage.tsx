import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { HeartHandshake, Eye, EyeOff, AlertCircle, Loader2 } from 'lucide-react';

export function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const { login, register, isLoading, error, clearError } = useAuthStore();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    if (isLogin) {
      await login({ email, password });
    } else {
      const registered = await register({ name, email, phone, password, confirmPassword });
      if (registered) {
        setIsLogin(true);
        setPassword('');
        setConfirmPassword('');
      }
    }
  };

  const fillDemo = (role: string) => {
    const demos: Record<string, { email: string; password: string }> = {
      admin: { email: 'admin@donaton.test', password: 'admin123' },
      ong: { email: 'ong@donaton.test', password: 'ong123' },
      user: { email: 'user@donaton.test', password: 'user123' },
    };
    const demo = demos[role];
    if (demo) {
      setEmail(demo.email);
      setPassword(demo.password);
      setIsLogin(true);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center mx-auto mb-4">
            <HeartHandshake className="w-7 h-7 text-primary-foreground" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Donaton</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Plataforma de gestión de donaciones en emergencias
          </p>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex gap-2 mb-6 p-1 bg-secondary rounded-md">
            <button
              onClick={() => { setIsLogin(true); clearError(); }}
              className={`flex-1 py-2 text-sm font-medium rounded transition-colors ${
                isLogin ? 'bg-card text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Iniciar sesión
            </button>
            <button
              onClick={() => { setIsLogin(false); clearError(); }}
              className={`flex-1 py-2 text-sm font-medium rounded transition-colors ${
                !isLogin ? 'bg-card text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Registrarse
            </button>
          </div>

          {error && (
            <div className="flex items-center gap-2 p-3 mb-4 rounded-md bg-destructive/10 text-destructive text-sm">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-1.5">Nombre completo</label>
                  <input
                    type="text"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                    placeholder="Tu nombre"
                    required={!isLogin}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1.5">Teléfono</label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                    placeholder="+56 9 0000 0000"
                    required={!isLogin}
                  />
                </div>
              </>
            )}

            <div>
              <label className="block text-sm font-medium mb-1.5">Correo electrónico</label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder="tu@email.com"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1.5">Contraseña</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full px-3 py-2 pr-10 rounded-md border border-border bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="••••••••"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {!isLogin && (
              <div>
                <label className="block text-sm font-medium mb-1.5">Confirmar contraseña</label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={e => setConfirmPassword(e.target.value)}
                  className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="••••••••"
                  required={!isLogin}
                />
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-2.5 bg-primary text-primary-foreground rounded-md text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
              {isLogin ? 'Iniciar sesión' : 'Crear cuenta'}
            </button>
          </form>

          {isLogin && (
            <div className="mt-4 text-center">
              <a href="#" className="text-sm text-primary hover:underline">¿Olvidaste tu contraseña?</a>
            </div>
          )}
        </div>

        <div className="mt-6">
          <p className="text-xs text-muted-foreground text-center mb-3">Cuentas de demo</p>
          <div className="flex flex-wrap gap-2 justify-center">
            {['admin', 'ong', 'user'].map(role => (
              <button
                key={role}
                onClick={() => fillDemo(role)}
                className="px-3 py-1.5 text-xs rounded-md border border-border bg-card hover:bg-secondary transition-colors text-muted-foreground capitalize"
              >
                {role}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
