import { useEffect, useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { MainLayout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { DonationsPage } from './pages/DonationsPage';
import { NeedsPage } from './pages/NeedsPage';
import { LogisticsPage } from './pages/LogisticsPage';
import { UsersPage } from './pages/UsersPage';
import type { Permission } from './types';

function HydrationGate({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(useAuthStore.persist.hasHydrated());

  useEffect(() => {
    if (!ready) {
      const unsub = useAuthStore.persist.onFinishHydration(() => setReady(true));
      return unsub;
    }
  }, []);

  if (!ready) return null;

  return <>{children}</>;
}

function ProtectedRoute({ children, requiredPermission }: { children: React.ReactNode; requiredPermission?: Permission }) {
  const { isAuthenticated, hasPermission } = useAuthStore();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (requiredPermission && !hasPermission(requiredPermission)) {
    return <Navigate to="/" replace />;
  }
  
  return <MainLayout>{children}</MainLayout>;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }
  
  return <>{children}</>;
}

export default function App() {
  return (
    <HydrationGate>
      <Routes>
        <Route path="/login" element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } />
        
        <Route path="/" element={
          <ProtectedRoute requiredPermission="dashboard:view">
            <DashboardPage />
          </ProtectedRoute>
        } />
        
        <Route path="/donations" element={
          <ProtectedRoute requiredPermission="donations:view">
            <DonationsPage />
          </ProtectedRoute>
        } />
        
        <Route path="/needs" element={
          <ProtectedRoute requiredPermission="needs:view">
            <NeedsPage />
          </ProtectedRoute>
        } />
        
        <Route path="/logistics" element={
          <ProtectedRoute requiredPermission="logistics:view">
            <LogisticsPage />
          </ProtectedRoute>
        } />

        <Route path="/users" element={
          <ProtectedRoute requiredPermission="users:manage">
            <UsersPage />
          </ProtectedRoute>
        } />
        
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </HydrationGate>
  );
}