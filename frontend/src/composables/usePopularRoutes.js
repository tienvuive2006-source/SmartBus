import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useApi } from '@/composables/useApi';

// Global state cache to prevent flickering on SPA navigation
const globalSavedRoutes = ref([]);

export function usePopularRoutes(trips, filterFrom, filterTo, filterCompany, filterDate) {
  const api = useApi();
  const savedRoutes = globalSavedRoutes; // Use the global cache
  const _syncCounter = ref(0);
  let _syncInterval = null;

  const fetchSavedRoutes = async () => {
    try {
      const response = await api.get('/routes');
      savedRoutes.value = response.data;
    } catch (err) { console.error(err); }
  };

  const syncStorage = () => { _syncCounter.value++; };

  onMounted(() => {
    fetchSavedRoutes();
    _syncInterval = setInterval(syncStorage, 3000);
    window.addEventListener('storage', syncStorage);
  });

  onUnmounted(() => {
    if (_syncInterval) clearInterval(_syncInterval);
    window.removeEventListener('storage', syncStorage);
  });

  const popularRoutes = computed(() => {
    // Kích hoạt recompute khi storage thay đổi
    _syncCounter.value;

    let filteredTrips = [...(trips.value || [])];
    if (filterFrom.value) filteredTrips = filteredTrips.filter(t => t.departurePoint === filterFrom.value);
    if (filterTo.value) filteredTrips = filteredTrips.filter(t => t.arrivalPoint === filterTo.value);
    if (filterCompany.value) filteredTrips = filteredTrips.filter(t => t.companyName === filterCompany.value);
    if (filterDate.value) filteredTrips = filteredTrips.filter(t => t.departureDate?.split('T')[0] === filterDate.value);

    const routes = [];

    const getCityName = (loc) => {
      if (!loc) return '';
      if (loc.includes('Hồ Chí Minh')) return 'Hồ Chí Minh';
      if (loc.includes('Hà Nội')) return 'Hà Nội';
      if (loc.includes('Đà Nẵng')) return 'Đà Nẵng';
      if (loc.includes('Quy Nhơn')) return 'Quy Nhơn';
      if (loc.includes('Nha Trang')) return 'Nha Trang';
      if (loc.includes('Đà Lạt')) return 'Đà Lạt';
      if (loc.includes('Cần Thơ')) return 'Cần Thơ';
      if (loc.includes('Hải Phòng')) return 'Hải Phòng';
      if (loc.includes('Vũng Tàu')) return 'Vũng Tàu';
      
      let city = loc.replace(/Bến xe Trung tâm/i, '')
                    .replace(/Bến xe Phía Nam/i, '')
                    .replace(/Bến xe Phía Bắc/i, '')
                    .replace(/Bến xe Liên tỉnh/i, '')
                    .replace(/Bến xe/i, '')
                    .trim();
                    
      if (city.includes(',')) {
        const parts = city.split(',');
        city = parts[parts.length - 1].trim();
      }
      return city;
    };

    savedRoutes.value.forEach(r => {
      if (r.isVisible === false) return;
      if (filterFrom.value && r.departurePoint !== filterFrom.value) return;
      if (filterTo.value && r.arrivalPoint !== filterTo.value) return;

      const displayFrom = getCityName(r.departurePoint);
      const displayTo = getCityName(r.arrivalPoint);

      const matchingTrips = filteredTrips.filter(trip => trip.departurePoint === r.departurePoint && trip.arrivalPoint === r.arrivalPoint);
      const validPrices = matchingTrips.map(trip => trip.price).filter(price => price && price > 0);
      const lowestPrice = validPrices.length > 0 ? Math.min(...validPrices) : (r.price || 0);

      routes.push({ 
        id: r.id, 
        from: r.departurePoint, 
        to: r.arrivalPoint, 
        shortFrom: displayFrom,
        shortTo: displayTo,
        date: null,
        price: lowestPrice,
        busType: null,
        image: r.imageUrl || '',
        customName: r.name || null
      });
    });

    return routes;
  });

  return { popularRoutes };
}
