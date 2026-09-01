import { useApi } from '@/composables/useApi';

export const useRouteBusTypeApi = () => {
  const api = useApi();

  return {
    getConfig: routeId => api.get(`/route-bus-types/routes/${routeId}`),
    updateConfig: (routeId, config) => api.put(`/route-bus-types/routes/${routeId}`, config)
  };
};
