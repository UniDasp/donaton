import { useQuery } from '@tanstack/react-query';
import { donationService } from '../services/donationService';
import { needService } from '../services/needService';
import { logisticsService } from '../services/logisticsService';
import { dashboardService } from '../services/dashboardService';
import { usersService } from '../services/usersService';
import { MOCK_CENTERS, MOCK_VEHICLES, MOCK_ROUTES } from '../mock/logistics';

export function useDonations() {
  return useQuery({
    queryKey: ['donations'],
    queryFn: () => donationService.getAll(),
  });
}

export function useDonation(id: string) {
  return useQuery({
    queryKey: ['donations', id],
    queryFn: () => donationService.getById(id),
    enabled: !!id,
  });
}

export function useNeeds() {
  return useQuery({
    queryKey: ['needs'],
    queryFn: () => needService.getAll(),
  });
}

export function useNeed(id: string) {
  return useQuery({
    queryKey: ['needs', id],
    queryFn: () => needService.getById(id),
    enabled: !!id,
  });
}

export function useCenters() {
  return useQuery({
    queryKey: ['centers'],
    queryFn: () => Promise.resolve([...MOCK_CENTERS]),
  });
}

export function useVehicles() {
  return useQuery({
    queryKey: ['vehicles'],
    queryFn: () => Promise.resolve([...MOCK_VEHICLES]),
  });
}

export function useRoutes() {
  return useQuery({
    queryKey: ['routes'],
    queryFn: () => Promise.resolve([...MOCK_ROUTES]),
  });
}

export function useUsers() {
  return useQuery({
    queryKey: ['auth', 'users'],
    queryFn: () => usersService.getAll(),
  });
}

export function useEnvios() {
  return useQuery({
    queryKey: ['envios'],
    queryFn: () => logisticsService.getAll(),
  });
}

export function useDashboardKPIs() {
  return useQuery({
    queryKey: ['dashboard', 'kpis'],
    queryFn: () => dashboardService.getKPIs(),
  });
}

export function useActivities(limit?: number) {
  return useQuery({
    queryKey: ['dashboard', 'activities', limit],
    queryFn: () => dashboardService.getActivities(limit),
  });
}

export function useAlerts() {
  return useQuery({
    queryKey: ['dashboard', 'alerts'],
    queryFn: () => dashboardService.getAlerts(),
  });
}

export function useRegionStatus() {
  return useQuery({
    queryKey: ['dashboard', 'regions'],
    queryFn: () => dashboardService.getRegionStatus(),
  });
}
