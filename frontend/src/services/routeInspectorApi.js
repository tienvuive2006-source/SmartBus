import { useApi } from '@/composables/useApi'

export const useRouteInspectorApi = () => {
  const api = useApi()
  return {
    getConfig: routeId => api.get(`/route-inspectors/routes/${routeId}`),
    getInspectorRoutes: inspectorId => api.get(`/route-inspectors/inspectors/${inspectorId}`),
    updateInspectorRoutes: (inspectorId, config) => api.put(`/route-inspectors/inspectors/${inspectorId}`, config)
  }
}
