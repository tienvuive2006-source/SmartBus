import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useApi } from '@/composables/useApi';

export function usePopularRoutes(trips, filterFrom, filterTo, filterCompany, filterDate) {
  const api = useApi();
  const savedRoutes = ref([]);
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

    const normalizeKey = (s) => {
      if (!s) return '';
      return s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]/g, '');
    };

    const rawImagesMap = JSON.parse(localStorage.getItem('smartbus_route_images') || '{}');
    const routeImagesMap = {};
    for (const k in rawImagesMap) {
      const parts = k.split('||');
      if (parts.length === 2) {
        routeImagesMap[`${normalizeKey(parts[0])}||${normalizeKey(parts[1])}`] = rawImagesMap[k];
      }
    }
    
    const routeNamesMap = {};
    
    savedRoutes.value.forEach(r => {
      const sd = normalizeKey(r.departurePoint.split(',')[0]);
      const sa = normalizeKey(r.arrivalPoint.split(',')[0]);
      
      if (r.imageUrl) {
        routeImagesMap[`${sd}||${sa}`] = r.imageUrl;
      }
      
      if (r.name && !r.name.includes('➔')) {
        routeNamesMap[`${sd}||${sa}`] = r.name;
      }
    });
    
    let filtered = [...(trips.value || [])];
    if (filterFrom.value) filtered = filtered.filter(t => t.departurePoint === filterFrom.value);
    if (filterTo.value) filtered = filtered.filter(t => t.arrivalPoint === filterTo.value);
    if (filterCompany.value) filtered = filtered.filter(t => t.companyName === filterCompany.value);
    if (filterDate.value) filtered = filtered.filter(t => t.departureDate?.split('T')[0] === filterDate.value);

    const routes = [];
    const seen = new Set();

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

    filtered.sort((a, b) => b.id - a.id).forEach(t => {
      const key = `${t.departurePoint}-${t.arrivalPoint}`;
      if (!seen.has(key)) {
        seen.add(key);
        const rawShortFrom = t.departurePoint.split(',')[0].trim();
        const rawShortTo = t.arrivalPoint.split(',')[0].trim();
        
        const targetKey = `${normalizeKey(rawShortFrom)}||${normalizeKey(rawShortTo)}`;
        
        let matchedImageUrl = routeImagesMap[targetKey];
        if (!matchedImageUrl) {
          for (const mapKey in routeImagesMap) {
            const [mFrom, mTo] = mapKey.split('||');
            if (mFrom && mTo && normalizeKey(rawShortFrom).includes(mFrom) && normalizeKey(rawShortTo).includes(mTo)) {
              matchedImageUrl = routeImagesMap[mapKey];
              break;
            }
          }
        }
        
        const displayFrom = getCityName(t.departurePoint);
        const displayTo = getCityName(t.arrivalPoint);
        
        const routeImageUrl = matchedImageUrl || t.imageUrl || '';
        
        let matchedName = routeNamesMap[targetKey];
        if (!matchedName) {
          for (const mapKey in routeNamesMap) {
            const [mFrom, mTo] = mapKey.split('||');
            if (mFrom && mTo && normalizeKey(rawShortFrom).includes(mFrom) && normalizeKey(rawShortTo).includes(mTo)) {
              matchedName = routeNamesMap[mapKey];
              break;
            }
          }
        }

        routes.push({ 
          id: t.id, 
          from: t.departurePoint, 
          to: t.arrivalPoint, 
          shortFrom: displayFrom,
          shortTo: displayTo,
          date: t.departureDate,
          price: t.ticketPrice,
          busType: t.busType,
          image: routeImageUrl,
          customName: matchedName || null
        });
      }
    });
    return routes;
  });

  return { popularRoutes };
}
