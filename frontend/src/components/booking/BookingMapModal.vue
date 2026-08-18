<template>
  <Teleport to="body">
    <div class="map-overlay" @click.self="emit('close')">
      <section class="map-modal" role="dialog" aria-modal="true" aria-labelledby="map-title">
        <header class="map-header">
          <div class="map-brand">
            <span class="map-brand__mark">TN</span>
            <div><strong>TRUNG NAM</strong><small>LIMOUSINE</small></div>
          </div>
          <div class="map-heading">
            <span>Chi tiết tuyến đường</span>
            <h2 id="map-title">Bản đồ hành trình</h2>
            <p>{{ trip?.departurePoint }} <i class="material-symbols-outlined">arrow_forward</i> {{ trip?.arrivalPoint }}</p>
          </div>
          <button type="button" class="map-close" aria-label="Đóng bản đồ" @click="emit('close')">
            <span class="material-symbols-outlined">close</span>
          </button>
        </header>

        <div class="route-facts">
          <div><span class="material-symbols-outlined">trip_origin</span><p><small>Điểm bắt đầu</small><strong>{{ trip?.departurePoint }}</strong></p></div>
          <div><span class="material-symbols-outlined">route</span><p><small>Tổng quãng đường</small><strong>{{ distanceLabel }}</strong></p></div>
          <div><span class="material-symbols-outlined">local_cafe</span><p><small>Trạm dừng chân</small><strong>{{ restStops.length }} trạm</strong></p></div>
          <div><span class="material-symbols-outlined">location_on</span><p><small>Điểm kết thúc</small><strong>{{ trip?.arrivalPoint }}</strong></p></div>
        </div>

        <div class="map-workspace">
          <main class="map-canvas-wrap">
            <div id="route-map" class="map-canvas"></div>
            <span class="map-mode"><i class="material-symbols-outlined">route</i> Lộ trình tuyến xe</span>
            <div v-if="mapLoading" class="map-loading">
              <span class="map-loading__route"><i></i><i></i><i></i></span>
              <strong>Đang dựng hành trình</strong>
              <small>Định vị tuyến đường và các trạm nghỉ</small>
            </div>
            <div v-else-if="mapError" class="map-error">
              <span class="material-symbols-outlined">map</span>
              <strong>Không tải được bản đồ</strong>
              <small>{{ mapError }}</small>
            </div>
          </main>

          <aside class="map-sidebar">
            <section class="sidebar-section route-overview">
              <h3><span class="material-symbols-outlined">conversion_path</span>Thông tin hành trình</h3>
              <div class="overview-grid">
                <p><small>Quãng đường</small><strong>{{ distanceLabel }}</strong></p>
                <p><small>Trạm nghỉ</small><strong>{{ restStops.length }} trạm</strong></p>
                <p><small>Từ</small><strong>{{ trip?.departurePoint }}</strong></p>
                <p><small>Đến</small><strong>{{ trip?.arrivalPoint }}</strong></p>
              </div>
            </section>

            <section class="sidebar-section rest-section">
              <div class="section-heading">
                <h3><span class="material-symbols-outlined">conversion_path</span>Các điểm trên tuyến</h3>
                <span>{{ restStops.length + 2 }}</span>
              </div>

              <div v-if="stopsLoading" class="rest-skeletons">
                <div v-for="item in 3" :key="item"><span></span><p><i></i><i></i></p></div>
              </div>
              <ol v-else class="rest-list">
                <li class="route-point route-point--start">
                  <button type="button" @click="focusEndpoint('start')">
                    <span class="rest-index"><i class="material-symbols-outlined">trip_origin</i></span>
                    <div>
                      <em>Điểm bắt đầu</em>
                      <strong>{{ trip?.departurePoint }}</strong>
                      <small>Khởi đầu hành trình</small>
                    </div>
                    <span class="material-symbols-outlined">chevron_right</span>
                  </button>
                </li>
                <li v-for="(stop, index) in restStops" :key="stop.id">
                  <button type="button" :disabled="!hasCoordinates(stop)" @click="focusStop(stop)">
                    <span class="rest-index">{{ index + 1 }}</span>
                    <div>
                      <strong>{{ stop.name }}</strong>
                      <small>{{ stop.address || 'Địa chỉ đang cập nhật' }}</small>
                      <em v-if="!hasCoordinates(stop)">Chưa có tọa độ bản đồ</em>
                    </div>
                    <span class="material-symbols-outlined">chevron_right</span>
                  </button>
                </li>
                <li class="route-point route-point--end">
                  <button type="button" @click="focusEndpoint('end')">
                    <span class="rest-index"><i class="material-symbols-outlined">location_on</i></span>
                    <div>
                      <em>Điểm kết thúc</em>
                      <strong>{{ trip?.arrivalPoint }}</strong>
                      <small>Kết thúc hành trình</small>
                    </div>
                    <span class="material-symbols-outlined">chevron_right</span>
                  </button>
                </li>
              </ol>
            </section>
          </aside>
        </div>

        <footer class="map-footer">
          <div class="map-legend">
            <span><i class="legend-dot legend-start"></i>Điểm xuất phát</span>
            <span><i class="legend-dot legend-rest"></i>Trạm nghỉ</span>
            <span><i class="legend-dot legend-end"></i>Điểm kết thúc</span>
          </div>
          <p class="route-note"><span class="material-symbols-outlined">info</span>Bản đồ dùng chung cho các chuyến chạy trên tuyến này</p>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { decodePolyline, fetchPolylineFromCloudinary } from '@/utils/polyline'
import { removeAccents } from '@/composables/useLocationSearch'
import { useRouteStopApi } from '@/services/routeStopApi'

const props = defineProps({ trip: { type: Object, required: true } })
const emit = defineEmits(['close'])
const stopApi = useRouteStopApi()

const mapLoading = ref(true)
const stopsLoading = ref(true)
const mapError = ref('')
const routeDistance = ref(null)
const restStops = ref([])
const googleMap = ref(null)
const googleMarkers = []
const leafletRestMarkers = new Map()
let directionsRenderer = null
let leafletMap = null

const cityCoordinates = {
  'Ha Noi': [21.028511, 105.804817], 'Hai Phong': [20.844912, 106.688087],
  'SaPa': [22.336404, 103.843848], 'Da Nang': [16.047079, 108.206230],
  'Nha Trang': [12.238791, 109.196747], 'Ho Chi Minh': [10.823099, 106.629664],
  'Sai Gon': [10.823099, 106.629664], 'Can Tho': [10.045162, 105.746857],
  'Da Lat': [11.940419, 108.458313], 'Hue': [16.463713, 107.590866],
  'Vung Tau': [10.345995, 107.084052]
}

const distanceFromDuration = computed(() => {
  const match = String(props.trip?.duration || '').match(/\(([\d.,]+)\s*km\)/i)
  return match ? `${match[1].replace(',', '.')} km` : ''
})

const distanceLabel = computed(() => {
  if (routeDistance.value) return `${routeDistance.value} km`
  if (props.trip?.distance) return String(props.trip.distance).toLowerCase().includes('km') ? props.trip.distance : `${props.trip.distance} km`
  if (distanceFromDuration.value) return distanceFromDuration.value
  return 'Đang cập nhật'
})

const normalize = value => removeAccents(value || '').toLowerCase().replace(/hcm|sai gon/g, 'ho chi minh').replace(/hn/g, 'ha noi').replace(/[,.-]/g, ' ').replace(/\b(ben xe|thanh pho|tp|tinh|huyen|xa|quan|phuong)\b/gi, '').replace(/\s+/g, ' ').trim()
const hasCoordinates = stop => Number.isFinite(Number(stop.latitude)) && Number.isFinite(Number(stop.longitude))
const restCoordinates = () => restStops.value.filter(hasCoordinates).map(stop => [Number(stop.latitude), Number(stop.longitude)])

const endpointCoordinates = () => {
  const findFallback = point => {
    const normalizedPoint = normalize(point)
    const match = Object.keys(cityCoordinates).find(city => normalizedPoint.includes(normalize(city)))
    return match ? cityCoordinates[match] : null
  }
  const from = props.trip.departureLat && props.trip.departureLng
    ? [Number(props.trip.departureLat), Number(props.trip.departureLng)]
    : findFallback(props.trip.departurePoint)
  const to = props.trip.arrivalLat && props.trip.arrivalLng
    ? [Number(props.trip.arrivalLat), Number(props.trip.arrivalLng)]
    : findFallback(props.trip.arrivalPoint)
  return { from, to }
}

const loadRestStops = async () => {
  stopsLoading.value = true
  try {
    const response = await stopApi.getTripStops(props.trip.id)
    restStops.value = (Array.isArray(response.data) ? response.data : [])
      .filter(stop => String(stop.stopType).toUpperCase() === 'REST')
      .sort((a, b) => Number(a.stopOrder || 0) - Number(b.stopOrder || 0))
  } catch (error) {
    console.error('Không thể tải trạm nghỉ:', error)
    restStops.value = []
  } finally {
    stopsLoading.value = false
  }
}

const createGoogleMarker = (position, title, color, label = '') => {
  const marker = new window.google.maps.Marker({
    map: googleMap.value, position, title, label: label ? { text: label, color: '#ffffff', fontWeight: '800' } : undefined,
    icon: { path: window.google.maps.SymbolPath.CIRCLE, fillColor: color, fillOpacity: 1, strokeColor: '#ffffff', strokeWeight: 3, scale: label ? 11 : 9 }
  })
  googleMarkers.push(marker)
  return marker
}

const initGoogleMap = () => {
  const container = document.getElementById('route-map')
  const { from, to } = endpointCoordinates()
  if (!container || !from || !to) { mapError.value = 'Chuyến xe chưa có đủ tọa độ điểm đi và điểm đến.'; mapLoading.value = false; return }
  googleMap.value = new window.google.maps.Map(container, { center: { lat: 16, lng: 106 }, zoom: 6, mapId: 'CUSTOMER_VIEW_MAP', mapTypeControl: false, streetViewControl: false, fullscreenControl: true })
  directionsRenderer = new window.google.maps.DirectionsRenderer({ map: googleMap.value, suppressMarkers: true, polylineOptions: { strokeColor: '#08786f', strokeWeight: 6, strokeOpacity: .9 } })
  createGoogleMarker({ lat: from[0], lng: from[1] }, props.trip.departurePoint, '#1976d2')
  restStops.value.filter(hasCoordinates).forEach((stop, index) => createGoogleMarker({ lat: Number(stop.latitude), lng: Number(stop.longitude) }, stop.name, '#e3a51a', String(index + 1)))
  createGoogleMarker({ lat: to[0], lng: to[1] }, props.trip.arrivalPoint, '#e33d4f')
  new window.google.maps.DirectionsService().route({
    origin: { lat: from[0], lng: from[1] }, destination: { lat: to[0], lng: to[1] },
    waypoints: restCoordinates().map(([lat, lng]) => ({ location: { lat, lng }, stopover: true })),
    optimizeWaypoints: false, travelMode: window.google.maps.TravelMode.DRIVING
  }, (result, status) => {
    if (status === 'OK') {
      directionsRenderer.setDirections(result)
      routeDistance.value = (result.routes[0].legs.reduce((sum, leg) => sum + Number(leg.distance?.value || 0), 0) / 1000).toFixed(1)
    } else mapError.value = 'Không thể dựng đường đi theo dữ liệu hiện tại.'
    mapLoading.value = false
  })
}

const leafletIcon = (color, label, icon) => window.L.divIcon({
  html: `<div class="route-marker" style="background:${color}">${label || `<span class="material-symbols-outlined">${icon}</span>`}</div>`,
  className: '', iconSize: [30, 30], iconAnchor: [15, 15]
})

const drawLeafletRoute = async (L, from, to) => {
  if (props.trip.routeData) {
    try {
      let routeData = props.trip.routeData
      if (routeData.startsWith('http')) routeData = await fetchPolylineFromCloudinary(routeData)
      const coordinates = routeData.startsWith('[') ? JSON.parse(routeData) : decodePolyline(routeData)
      if (coordinates?.length) {
        L.polyline(coordinates, { color: '#08786f', weight: 6, opacity: .9, lineJoin: 'round' }).addTo(leafletMap)
        const distanceMeters = coordinates.slice(1).reduce((total, point, index) => {
          return total + L.latLng(coordinates[index]).distanceTo(L.latLng(point))
        }, 0)
        if (distanceMeters > 0) routeDistance.value = (distanceMeters / 1000).toFixed(1)
        leafletMap.fitBounds(L.latLngBounds(coordinates), { padding: [45, 45] })
        return
      }
    } catch (error) { console.error('Không thể đọc dữ liệu tuyến:', error) }
  }
  const points = [from, ...restCoordinates(), to].map(([lat, lng]) => `${lng},${lat}`).join(';')
  try {
    const response = await fetch(`https://router.project-osrm.org/route/v1/driving/${points}?overview=full&geometries=polyline`)
    const data = await response.json()
    if (!data.routes?.length) throw new Error('Không tìm thấy tuyến')
    const route = data.routes[0]
    const coordinates = decodePolyline(route.geometry)
    L.polyline(coordinates, { color: '#08786f', weight: 6, opacity: .9, lineJoin: 'round' }).addTo(leafletMap)
    routeDistance.value = (route.distance / 1000).toFixed(1)
    leafletMap.fitBounds(L.latLngBounds(coordinates), { padding: [45, 45] })
  } catch (error) {
    const fallback = [from, ...restCoordinates(), to]
    L.polyline(fallback, { color: '#08786f', weight: 4, dashArray: '9, 9' }).addTo(leafletMap)
    leafletMap.fitBounds(fallback, { padding: [45, 45] })
  }
}

const initLeafletMap = async () => {
  await nextTick()
  const L = window.L
  const { from, to } = endpointCoordinates()
  if (!L || !from || !to) { mapError.value = 'Chuyến xe chưa có đủ tọa độ điểm đi và điểm đến.'; mapLoading.value = false; return }
  if (leafletMap) leafletMap.remove()
  leafletMap = L.map('route-map', { zoomControl: true }).setView([16, 106], 6)
  L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', { maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps' }).addTo(leafletMap)
  L.marker(from, { icon: leafletIcon('#1976d2', '', 'trip_origin') }).addTo(leafletMap).bindPopup(`<strong>${props.trip.departurePoint}</strong><br>Điểm xuất phát`)
  restStops.value.filter(hasCoordinates).forEach((stop, index) => {
    const marker = L.marker([Number(stop.latitude), Number(stop.longitude)], { icon: leafletIcon('#e3a51a', String(index + 1), '') }).addTo(leafletMap).bindPopup(`<strong>${stop.name}</strong><br>${stop.address || 'Trạm dừng chân'}`)
    leafletRestMarkers.set(stop.id, marker)
  })
  L.marker(to, { icon: leafletIcon('#e33d4f', '', 'location_on') }).addTo(leafletMap).bindPopup(`<strong>${props.trip.arrivalPoint}</strong><br>Điểm kết thúc`)
  await drawLeafletRoute(L, from, to)
  mapLoading.value = false
}

const loadLeaflet = () => {
  if (!document.getElementById('leaflet-css')) {
    const link = document.createElement('link'); link.id = 'leaflet-css'; link.rel = 'stylesheet'; link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'; document.head.appendChild(link)
  }
  if (window.L) { initLeafletMap(); return }
  const existing = document.getElementById('leaflet-script')
  if (existing) { existing.addEventListener('load', initLeafletMap, { once: true }); return }
  const script = document.createElement('script'); script.id = 'leaflet-script'; script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'; script.onload = initLeafletMap; script.onerror = () => { mapError.value = 'Không tải được thư viện bản đồ.'; mapLoading.value = false }; document.head.appendChild(script)
}

const focusStop = stop => {
  if (!hasCoordinates(stop)) return
  const position = { lat: Number(stop.latitude), lng: Number(stop.longitude) }
  if (googleMap.value) { googleMap.value.panTo(position); googleMap.value.setZoom(11) }
  if (leafletMap) { leafletMap.setView([position.lat, position.lng], 11); leafletRestMarkers.get(stop.id)?.openPopup() }
}

const focusEndpoint = type => {
  const { from, to } = endpointCoordinates()
  const coordinates = type === 'start' ? from : to
  if (!coordinates) return
  const position = { lat: Number(coordinates[0]), lng: Number(coordinates[1]) }
  if (googleMap.value) { googleMap.value.panTo(position); googleMap.value.setZoom(11) }
  if (leafletMap) leafletMap.setView([position.lat, position.lng], 11)
}

const handleKeydown = event => { if (event.key === 'Escape') emit('close') }

onMounted(async () => {
  window.addEventListener('keydown', handleKeydown)
  await loadRestStops()
  if (window.google?.maps) initGoogleMap()
  else loadLeaflet()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  googleMarkers.forEach(marker => marker.setMap(null))
  directionsRenderer?.setMap(null)
  if (leafletMap) leafletMap.remove()
  leafletMap = null
  leafletRestMarkers.clear()
})
</script>

<style scoped>
.map-overlay { position: fixed; inset: 0; z-index: 10010; display: grid; place-items: center; padding: 1rem; background: rgb(10 31 38 / .66); backdrop-filter: blur(7px); }
.map-modal { display: flex; width: min(90rem, 100%); height: min(53rem, calc(100dvh - 2rem)); flex-direction: column; overflow: hidden; border: 1px solid rgb(255 255 255 / .55); border-radius: 1.45rem; background: #f6f9f8; box-shadow: 0 2rem 6rem rgb(1 39 37 / .36); }
.map-header { display: grid; flex: 0 0 auto; grid-template-columns: auto 1fr auto; align-items: center; gap: 1.35rem; border-bottom: 1px solid #dfe8e6; padding: 1rem 1.25rem; background: white; }
.map-brand { display: flex; align-items: center; gap: .65rem; border-right: 1px solid #dce5e3; padding-right: 1.35rem; }.map-brand__mark { display: grid; width: 2.5rem; height: 2.8rem; place-items: center; color: #c89424; border: 2px solid #d9aa43; border-radius: .35rem .35rem .7rem .7rem; font-size: .85rem; font-weight: 950; }.map-brand div { display: flex; flex-direction: column; }.map-brand strong { color: #075955; font-size: .8rem; letter-spacing: .08em; }.map-brand small { color: #c89424; font-size: .5rem; font-weight: 900; letter-spacing: .2em; }
.map-heading > span { color: #0a796f; font-size: .56rem; font-weight: 850; letter-spacing: .1em; text-transform: uppercase; }.map-heading h2 { color: #143d39; font-size: 1.3rem; font-weight: 900; letter-spacing: -.025em; line-height: 1.15; }.map-heading p { display: flex; align-items: center; gap: .35rem; margin-top: .22rem; color: #71827f; font-size: .72rem; font-weight: 650; }.map-heading p i { color: #c89424; font-size: .85rem; font-style: normal; }
.map-close { display: grid; width: 2.35rem; height: 2.35rem; place-items: center; border-radius: .7rem; color: #51635f; background: #f1f5f4; transition: background .2s, transform .2s; }.map-close:hover { color: #075955; background: #e5f2ef; }.map-close:active { transform: scale(.93); }
.route-facts { display: grid; flex: 0 0 auto; grid-template-columns: 1.35fr .8fr .75fr 1.35fr; border-bottom: 1px solid #dfe8e6; padding: .75rem 1rem; background: white; }.route-facts > div { display: flex; min-width: 0; align-items: center; justify-content: center; gap: .55rem; border-right: 1px solid #e3ebe9; }.route-facts > div:last-child { border-right: 0; }.route-facts > div > span { flex: 0 0 auto; color: #08786f; font-size: 1.25rem; }.route-facts p { display: flex; min-width: 0; flex-direction: column; }.route-facts small { color: #84938f; font-size: .55rem; font-weight: 700; }.route-facts strong { overflow: hidden; margin-top: .12rem; color: #164b46; font-size: .72rem; font-weight: 850; text-overflow: ellipsis; white-space: nowrap; font-variant-numeric: tabular-nums; }
.map-workspace { display: grid; min-height: 0; flex: 1; grid-template-columns: minmax(0, 1.95fr) minmax(19rem, .85fr); gap: .8rem; padding: .8rem; }.map-canvas-wrap { position: relative; min-height: 0; overflow: hidden; border-radius: .85rem; background: #dce9e5; }.map-canvas { width: 100%; height: 100%; }.map-mode { position: absolute; z-index: 500; top: 1rem; left: 1rem; display: flex; align-items: center; gap: .4rem; border: 1px solid rgb(255 255 255 / .5); border-radius: .65rem; padding: .55rem .7rem; color: white; background: rgb(7 89 85 / .9); box-shadow: 0 .45rem 1rem rgb(7 89 85 / .2); font-size: .68rem; font-weight: 800; backdrop-filter: blur(8px); }.map-mode i { font-size: .95rem; font-style: normal; }
.map-loading, .map-error { position: absolute; inset: 0; z-index: 600; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #54706b; background: rgb(245 249 248 / .9); backdrop-filter: blur(3px); }.map-loading strong, .map-error strong { margin-top: .8rem; color: #164b46; font-size: .82rem; }.map-loading small, .map-error small { margin-top: .25rem; font-size: .65rem; }.map-loading__route { display: flex; align-items: center; gap: .45rem; }.map-loading__route i { width: .65rem; height: .65rem; border-radius: 50%; background: #0a8276; animation: map-pulse 1.1s infinite alternate; }.map-loading__route i:nth-child(2) { animation-delay: .2s; }.map-loading__route i:nth-child(3) { animation-delay: .4s; }.map-error > span { color: #b9475b; font-size: 2.5rem; }
.map-sidebar { min-height: 0; overflow-y: auto; border: 1px solid #dfe8e6; border-radius: .85rem; background: white; }.sidebar-section { padding: 1rem; border-bottom: 1px solid #e1e9e7; }.sidebar-section:last-child { border-bottom: 0; }.sidebar-section h3 { display: flex; align-items: center; gap: .4rem; color: #18423e; font-size: .76rem; font-weight: 900; }.sidebar-section h3 span { color: #08786f; font-size: 1rem; }
.overview-grid { display: grid; grid-template-columns: 1fr 1fr; gap: .8rem 1rem; margin-top: .9rem; }.overview-grid p { display: flex; min-width: 0; flex-direction: column; }.overview-grid small { color: #84938f; font-size: .56rem; font-weight: 700; }.overview-grid strong { overflow: hidden; margin-top: .14rem; color: #17524c; font-size: .68rem; font-weight: 850; text-overflow: ellipsis; white-space: nowrap; font-variant-numeric: tabular-nums; }
.section-heading { display: flex; align-items: center; justify-content: space-between; }.section-heading > span { display: grid; min-width: 1.45rem; height: 1.45rem; place-items: center; border-radius: .4rem; color: #9b650b; background: #fff3d8; font-size: .62rem; font-weight: 900; }
.rest-list { position: relative; display: grid; margin-top: .75rem; }.rest-list::before { position: absolute; top: 1.25rem; bottom: 1.25rem; left: .82rem; width: 2px; content: ''; background: #f1dca8; }.rest-list li { position: relative; }.rest-list button { display: grid; width: 100%; grid-template-columns: 1.65rem minmax(0, 1fr) auto; align-items: center; gap: .65rem; border-radius: .65rem; padding: .65rem .45rem; text-align: left; transition: background .2s, transform .2s; }.rest-list button:not(:disabled):hover { background: #fffaf0; transform: translateX(2px); }.rest-list button:disabled { cursor: default; }.rest-index { position: relative; z-index: 1; display: grid; width: 1.65rem; height: 1.65rem; place-items: center; border: 3px solid white; border-radius: 50%; color: white; background: #dda31e; font-size: .58rem; font-weight: 900; }.rest-list button > div { display: flex; min-width: 0; flex-direction: column; }.rest-time { color: #a76e0a; font-size: .56rem; font-weight: 900; font-variant-numeric: tabular-nums; }.rest-list strong { margin-top: .1rem; color: #273b37; font-size: .68rem; font-weight: 850; }.rest-list small { overflow: hidden; margin-top: .15rem; color: #879590; font-size: .58rem; text-overflow: ellipsis; white-space: nowrap; }.rest-list em { margin-top: .15rem; color: #b9475b; font-size: .54rem; font-style: normal; }.rest-list button > .material-symbols-outlined { color: #aeb9b6; font-size: 1rem; }
.rest-index i { font-size: .78rem; font-style: normal; }.route-point--start .rest-index { background: #1976d2; }.route-point--end .rest-index { background: #e33d4f; }.route-point--start em { color: #1976d2; font-weight: 850; letter-spacing: .04em; text-transform: uppercase; }.route-point--end em { color: #d9364a; font-weight: 850; letter-spacing: .04em; text-transform: uppercase; }
.rest-empty { display: flex; min-height: 9rem; flex-direction: column; align-items: center; justify-content: center; text-align: center; }.rest-empty > span { color: #c99728; font-size: 1.7rem; }.rest-empty strong { margin-top: .45rem; color: #394c48; font-size: .7rem; }.rest-empty p { max-width: 14rem; margin-top: .25rem; color: #899793; font-size: .6rem; line-height: 1.45; }
.rest-skeletons { display: grid; gap: .7rem; margin-top: .85rem; }.rest-skeletons > div { display: grid; grid-template-columns: 1.65rem 1fr; gap: .65rem; }.rest-skeletons span, .rest-skeletons i { display: block; background: linear-gradient(90deg, #e4ebe9 25%, #f5f7f7 45%, #e4ebe9 65%); background-size: 220% 100%; animation: skeleton-wave 1.3s infinite linear; }.rest-skeletons span { width: 1.65rem; height: 1.65rem; border-radius: 50%; }.rest-skeletons p { display: grid; gap: .35rem; }.rest-skeletons i { width: 70%; height: .5rem; border-radius: .2rem; }.rest-skeletons i:last-child { width: 90%; }
.map-footer { display: flex; flex: 0 0 auto; align-items: center; justify-content: space-between; gap: 1rem; border-top: 1px solid #dfe8e6; padding: .85rem 1.25rem; background: white; }.map-legend { display: flex; flex-wrap: wrap; gap: 1.2rem; }.map-legend span { display: flex; align-items: center; gap: .35rem; color: #536763; font-size: .63rem; font-weight: 750; }.legend-dot { width: .65rem; height: .65rem; border: 2px solid white; border-radius: 50%; box-shadow: 0 0 0 1px #d6dfdd; }.legend-start { background: #1976d2; }.legend-rest { background: #e3a51a; }.legend-end { background: #e33d4f; }.route-note { display: flex; align-items: center; gap: .35rem; color: #758581; font-size: .62rem; font-weight: 700; }.route-note span { color: #08786f; font-size: 1rem; }
.map-close:focus-visible, .rest-list button:focus-visible { outline: 3px solid #e5b840; outline-offset: 2px; }
:deep(.route-marker) { display: grid; width: 30px; height: 30px; place-items: center; border: 3px solid white; border-radius: 50%; color: white; box-shadow: 0 4px 12px rgb(15 23 42 / .25); font-size: 11px; font-weight: 900; }.map-canvas-wrap :deep(.route-marker .material-symbols-outlined) { font-size: 15px; }
@keyframes map-pulse { to { transform: translateY(-5px); opacity: .45; } } @keyframes skeleton-wave { to { background-position-x: -220%; } }
@media (max-width: 900px) { .map-modal { height: calc(100dvh - 1rem); }.map-workspace { grid-template-columns: 1fr; grid-template-rows: minmax(18rem, 1.2fr) minmax(13rem, .8fr); }.map-sidebar { display: grid; grid-template-columns: .9fr 1.1fr; }.sidebar-section { border-right: 1px solid #e1e9e7; border-bottom: 0; }.route-facts { grid-template-columns: repeat(2, 1fr); row-gap: .75rem; }.route-facts > div:nth-child(even) { border-right: 0; } }
@media (max-width: 640px) { .map-overlay { padding: 0; }.map-modal { width: 100%; height: 100dvh; border: 0; border-radius: 0; }.map-header { grid-template-columns: 1fr auto; padding: .75rem .85rem; }.map-brand { display: none; }.map-heading h2 { font-size: 1.05rem; }.map-heading p { max-width: 17rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.route-facts { grid-template-columns: repeat(2, 1fr); padding: .6rem; }.route-facts > div { justify-content: flex-start; padding-left: .55rem; }.route-facts > div:nth-child(even) { border-right: 0; }.map-workspace { grid-template-rows: minmax(16rem, .9fr) minmax(16rem, 1.1fr); padding: .55rem; }.map-sidebar { display: block; }.sidebar-section { border-right: 0; border-bottom: 1px solid #e1e9e7; }.map-footer { padding: .65rem .8rem; }.map-legend { gap: .6rem; }.map-legend span { font-size: .54rem; }.route-note { display: none; } }
</style>
