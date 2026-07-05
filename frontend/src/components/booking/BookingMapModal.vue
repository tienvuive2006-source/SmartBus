<template>
  <div class="fixed inset-0 z-[1000] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white w-full max-w-4xl h-[80vh] rounded-3xl overflow-hidden shadow-2xl flex flex-col relative animate-scale-up">
      <div class="p-5 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
        <div>
           <h3 class="text-lg font-black text-[#075955] flex items-center gap-2">
             <span class="material-symbols-outlined">explore</span> Bản đồ hành trình chi tiết
           </h3>
           <p class="text-[11px] font-bold text-gray-500 uppercase tracking-widest">
             {{ trip?.departurePoint }} ➝ {{ trip?.arrivalPoint }}
             <span v-if="routeDistance" class="ml-3 text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
               <span class="material-symbols-outlined text-[10px] align-middle mr-1">distance</span>
               {{ routeDistance }} km
             </span>
           </p>
        </div>
        <button @click="$emit('close')" class="w-10 h-10 rounded-full hover:bg-gray-200 flex items-center justify-center transition-all">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <div class="flex-1 relative bg-gray-100">
        <div id="route-map" class="w-full h-full"></div>
        
        <div v-if="mapLoading" class="absolute inset-0 bg-white/80 flex flex-col items-center justify-center z-10">
          <div class="w-12 h-12 border-4 border-[#075955] border-t-transparent rounded-full animate-spin mb-4"></div>
          <p class="text-sm font-bold text-[#075955]">Đang tải bản đồ vệ tinh...</p>
        </div>
      </div>

      <div class="p-5 bg-white border-t border-gray-100 flex flex-col md:flex-row justify-between items-center gap-4">
         <div class="flex items-center gap-6">
            <div class="flex items-center gap-2">
               <div class="w-3 h-3 rounded-full bg-blue-500 border-2 border-white shadow-sm"></div>
               <span class="text-xs font-bold text-gray-600">Điểm đón</span>
            </div>
            <div class="flex items-center gap-2">
               <div class="w-3 h-3 rounded-full bg-red-500 border-2 border-white shadow-sm"></div>
               <span class="text-xs font-bold text-gray-600">Điểm trả</span>
            </div>
         </div>
         <button 
           @click="!isTripPassed(trip) && $router.push({ path: '/booking/seat', query: { tripId: trip?.id } })"
           :class="['px-8 py-2.5 rounded-xl font-black text-xs uppercase tracking-widest transition-all shadow-md', isTripPassed(trip) ? 'bg-gray-300 text-gray-500 cursor-not-allowed' : 'bg-[#f03a17] hover:bg-[#d63314] text-white active:scale-95']"
           :disabled="isTripPassed(trip)"
         >
           {{ isTripPassed(trip) ? 'Đã khởi hành' : 'Chọn chỗ chuyến này' }}
         </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { decodePolyline, uploadPolylineToCloudinary, fetchPolylineFromCloudinary } from '@/utils/polyline';
import { removeAccents } from '@/composables/useLocationSearch';

const props = defineProps({
  trip: { type: Object, required: true }
});

const emit = defineEmits(['close']);
const router = useRouter();

const mapLoading = ref(true);
const routeDistance = ref(null);
const useGoogleMaps = ref(false);
const googleMap = ref(null);
const directionsRenderer = ref(null);
let leafletMap = null;

const cityCoordinates = {
  'Ha Noi': [21.028511, 105.804817],
  'Hai Phong': [20.844912, 106.688087],
  'SaPa': [22.336404, 103.843848],
  'Da Nang': [16.047079, 108.206230],
  'Nha Trang': [12.238791, 109.196747],
  'Ho Chi Minh': [10.823099, 106.629664],
  'Sai Gon': [10.823099, 106.629664],
  'Can Tho': [10.045162, 105.746857],
  'Da Lat': [11.940419, 108.458313],
  'Hue': [16.463713, 107.590866],
  'Vung Tau': [10.345995, 107.084052]
};

const normalize = (s) => {
  return removeAccents(s || '')
    .toLowerCase()
    .replace(/hcm|sai gon/g, 'ho chi minh')
    .replace(/hn/g, 'ha noi')
    .replace(/[,.-]/g, ' ')
    .replace(/\b(ben xe|thanh pho|tp|tinh|huyen|xa|quan|phuong)\b/gi, '')
    .replace(/\s+/g, ' ')
    .trim();
};

const isTripPassed = (trip) => {
  if (!trip || !trip.departureDate || !trip.departureTime) return false;
  try {
    const [year, month, day] = trip.departureDate.split('T')[0].split('-');
    const [hour, minute] = trip.departureTime.split(':');
    const depTime = new Date(year, month - 1, day, hour, minute);
    return new Date() > depTime;
  } catch (e) {
    return false;
  }
};

const initGoogleMap = () => {
  const container = document.getElementById('route-map');
  if (!container) return;
  
  googleMap.value = new window.google.maps.Map(container, {
    center: { lat: 16.0, lng: 106.0 },
    zoom: 6,
    disableDefaultUI: false,
    mapId: 'CUSTOMER_VIEW_MAP'
  });

  directionsRenderer.value = new window.google.maps.DirectionsRenderer({
    map: googleMap.value,
    polylineOptions: { strokeColor: '#075955', strokeWeight: 6 }
  });

  updateGoogleMap();
};

const updateGoogleMap = () => {
  if (!googleMap.value || !props.trip) return;
  
  const from = { lat: Number(props.trip.departureLat), lng: Number(props.trip.departureLng) };
  const to = { lat: Number(props.trip.arrivalLat), lng: Number(props.trip.arrivalLng) };

  if (from.lat > 1 && to.lat > 1) {
    const directionsService = new window.google.maps.DirectionsService();
    directionsService.route({
      origin: from,
      destination: to,
      travelMode: window.google.maps.TravelMode.DRIVING
    }, (result, status) => {
      if (status === 'OK') {
        directionsRenderer.value.setDirections(result);
        const route = result.routes[0];
        routeDistance.value = (route.legs[0].distance.value / 1000).toFixed(1);
        mapLoading.value = false;
      }
    });
  }
};

const initMap = async () => {
  const L = window.L;
  if (!L) return;
  
  nextTick(async () => {
    if (leafletMap) {
      leafletMap.remove();
    }
    
    leafletMap = L.map('route-map').setView([16.0, 106.0], 6);
    
    L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20,
      subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
      attribution: '© Google Maps'
    }).addTo(leafletMap);

    const from = normalize(props.trip.departurePoint);
    const to = normalize(props.trip.arrivalPoint);
    
    let fromCoords = props.trip.departureLat && props.trip.departureLng 
      ? [Number(props.trip.departureLat), Number(props.trip.departureLng)] 
      : null;
      
    let toCoords = props.trip.arrivalLat && props.trip.arrivalLng 
      ? [Number(props.trip.arrivalLat), Number(props.trip.arrivalLng)] 
      : null;
    
    if (!fromCoords || !toCoords) {
      for (const city in cityCoordinates) {
        const normalizedCity = normalize(city);
        if (!fromCoords && from.includes(normalizedCity)) fromCoords = cityCoordinates[city];
        if (!toCoords && to.includes(normalizedCity)) toCoords = cityCoordinates[city];
      }
    }
    
    if (!fromCoords || !toCoords) {
       for (const city in cityCoordinates) {
         const cityKey = city.toLowerCase();
         if (!fromCoords && from.toLowerCase().includes(cityKey)) fromCoords = cityCoordinates[city];
         if (!toCoords && to.toLowerCase().includes(cityKey)) toCoords = cityCoordinates[city];
       }
    }
    
    if (fromCoords && toCoords) {
      const startIcon = L.divIcon({
        html: `<div class="w-6 h-6 bg-blue-500 border-2 border-white rounded-full shadow-lg flex items-center justify-center text-white"><span class="material-symbols-outlined text-xs font-black">trip_origin</span></div>`,
        className: '', iconSize: [24, 24]
      });
      const endIcon = L.divIcon({
        html: `<div class="w-6 h-6 bg-red-500 border-2 border-white rounded-full shadow-lg flex items-center justify-center text-white"><span class="material-symbols-outlined text-xs font-black">location_on</span></div>`,
        className: '', iconSize: [24, 24]
      });

      L.marker(fromCoords, { icon: startIcon }).addTo(leafletMap).bindPopup('Điểm khởi hành');
      L.marker(toCoords, { icon: endIcon }).addTo(leafletMap).bindPopup('Điểm đến');
      
      if (props.trip.routeData) {
         try {
           let routeStr = props.trip.routeData;
           
           if (routeStr.startsWith('http')) {
               routeStr = await fetchPolylineFromCloudinary(routeStr);
           }
           
           let coords = [];
           if (routeStr.startsWith('[')) {
               coords = JSON.parse(routeStr);
           } else if (routeStr) {
               coords = decodePolyline(routeStr);
           }
           
           if (coords && coords.length > 0) {
              L.polyline(coords, { color: '#075955', weight: 5, opacity: 0.8, lineJoin: 'round' }).addTo(leafletMap);
              const bounds = L.latLngBounds(coords);
              leafletMap.fitBounds(bounds, { padding: [50, 50] });
              mapLoading.value = false;
              return;
           }
         } catch (e) { console.error("Lỗi parse routeData", e); }
      }
      
      let points = `${fromCoords[1]},${fromCoords[0]};${toCoords[1]},${toCoords[0]}`;
      
      const latDiff = Math.abs(fromCoords[0] - toCoords[0]);
      const isSouthBound = fromCoords[0] > toCoords[0];
      
      if (latDiff > 2) {
        let waypoints = [];
        const minLat = Math.min(fromCoords[0], toCoords[0]);
        const maxLat = Math.max(fromCoords[0], toCoords[0]);
        if (minLat < 13.0 && maxLat > 13.0) waypoints.push([109.2887, 13.0645]);
        if (minLat < 15.1 && maxLat > 15.1) waypoints.push([108.8268, 15.1522]);
        if (minLat < 17.5 && maxLat > 17.5) waypoints.push([106.5960, 17.4912]);
        
        if (isSouthBound) waypoints.reverse();
        if (waypoints.length > 0) {
           const waypointsStr = waypoints.map(wp => `${wp[0]},${wp[1]}`).join(';');
           points = `${fromCoords[1]},${fromCoords[0]};${waypointsStr};${toCoords[1]},${toCoords[0]}`;
        }
      }

      const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${points}?overview=full&geometries=polyline`;
      
      fetch(osrmUrl)
        .then(res => res.json())
        .then(data => {
          if (data.routes && data.routes.length > 0) {
            const route = data.routes[0];
            const encodedPolyline = route.geometry;
            
            uploadPolylineToCloudinary(encodedPolyline).catch(e => console.error(e));
            
            const coordinates = decodePolyline(encodedPolyline);
            
            L.polyline(coordinates, { color: '#075955', weight: 5, opacity: 0.8, lineJoin: 'round' }).addTo(leafletMap);
            routeDistance.value = (route.distance / 1000).toFixed(1);
            
            const bounds = L.latLngBounds(coordinates);
            leafletMap.fitBounds(bounds, { padding: [50, 50] });
          } else {
            L.polyline([fromCoords, toCoords], { color: '#075955', weight: 4, dashArray: '10, 10' }).addTo(leafletMap);
            leafletMap.fitBounds([fromCoords, toCoords], { padding: [50, 50] });
          }
        })
        .catch(err => {
          console.error("OSRM Error:", err);
          L.polyline([fromCoords, toCoords], { color: '#075955', weight: 4, dashArray: '10, 10' }).addTo(leafletMap);
        });
    }
    
    mapLoading.value = false;
  });
};

onMounted(() => {
  if (window.google && window.google.maps) {
    useGoogleMaps.value = true;
    nextTick(() => initGoogleMap());
  } else {
    useGoogleMaps.value = false;
    if (!document.getElementById('leaflet-css')) {
      const link = document.createElement('link');
      link.id = 'leaflet-css';
      link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }

    if (!window.L) {
      const script = document.createElement('script');
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => setTimeout(initMap, 300);
      document.head.appendChild(script);
    } else {
      setTimeout(initMap, 300);
    }
  }
});

onUnmounted(() => {
  if (leafletMap) {
    leafletMap.remove();
    leafletMap = null;
  }
});
</script>
