import { useApi } from '@/composables/useApi';

export const useRouteVehicleApi = () => {
  const api = useApi();

  return {
    getConfig: routeId => api.get(`/route-vehicles/routes/${routeId}`),
    updateConfig: (routeId, config) => api.put(`/route-vehicles/routes/${routeId}`, config)
  };
};
