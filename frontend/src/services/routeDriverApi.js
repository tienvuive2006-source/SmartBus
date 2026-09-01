import { useApi } from '@/composables/useApi';

export const useRouteDriverApi = () => {
  const api = useApi();
  return {
    getConfig: routeId => api.get(`/route-drivers/routes/${routeId}`),
    updateConfig: (routeId, config) => api.put(`/route-drivers/routes/${routeId}`, config),
    getDriverRoutes: driverId => api.get(`/route-drivers/drivers/${driverId}`),
    updateDriverRoutes: (driverId, config) => api.put(`/route-drivers/drivers/${driverId}`, config)
  };
};
