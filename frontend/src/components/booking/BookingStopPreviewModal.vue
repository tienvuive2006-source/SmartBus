<template>
  <Teleport to="body">
    <div class="stop-overlay" role="presentation" @click.self="emit('close')">
      <section class="stop-modal" role="dialog" aria-modal="true" aria-labelledby="stop-modal-title">
        <header class="stop-header">
          <div class="stop-header__main">
            <span class="stop-header__icon material-symbols-outlined">route</span>
            <div class="stop-header__copy">
              <span>Hành trình chuyến xe</span>
              <h2 id="stop-modal-title">{{ showPassengerDetails ? 'Khách theo điểm đón và trả' : 'Điểm đón, trả và trạm nghỉ' }}</h2>
              <p>
                <strong>{{ trip.departurePoint }}</strong>
                <i class="material-symbols-outlined">arrow_forward</i>
                <strong>{{ trip.arrivalPoint }}</strong>
              </p>
            </div>
          </div>
          <button type="button" class="stop-close" aria-label="Đóng hành trình" @click="emit('close')">
            <span class="material-symbols-outlined">close</span>
          </button>
        </header>

        <div v-if="!loading && !error && stops.length" class="journey-summary" aria-label="Tóm tắt hành trình">
          <div>
            <span class="summary-icon material-symbols-outlined">pin_drop</span>
            <p><strong>{{ stops.length }}</strong><small>Tổng điểm dừng</small></p>
          </div>
          <div>
            <span class="summary-icon is-pickup material-symbols-outlined">person_pin_circle</span>
            <p><strong>{{ pickupCount }}</strong><small>Điểm đón</small></p>
          </div>
          <div>
            <span class="summary-icon is-dropoff material-symbols-outlined">location_on</span>
            <p><strong>{{ dropoffCount }}</strong><small>Điểm trả</small></p>
          </div>
          <div>
            <span class="summary-icon is-rest material-symbols-outlined">local_cafe</span>
            <p><strong>{{ restCount }}</strong><small>Trạm nghỉ</small></p>
          </div>
        </div>

        <main class="stop-body">
          <div v-if="loading" class="stop-loading" aria-label="Đang tải hành trình">
            <div v-for="item in 4" :key="item" class="stop-skeleton">
              <span></span><div><i></i><i></i></div>
            </div>
          </div>

          <div v-else-if="error" class="stop-state stop-state--error">
            <span class="material-symbols-outlined">cloud_off</span>
            <h3>Không tải được hành trình</h3>
            <p>{{ error }}</p>
            <button type="button" @click="loadStops">Thử lại</button>
          </div>

          <div v-else-if="!stops.length" class="stop-state">
            <span class="material-symbols-outlined">distance</span>
            <h3>Nhà xe chưa cập nhật điểm dừng</h3>
            <p>Chuyến xe hiện chỉ có thông tin điểm đi và điểm đến.</p>
          </div>

          <ol v-else class="stop-timeline">
            <li v-for="(stop, index) in stops" :key="stop.id" class="stop-item">
              <div class="timeline-rail">
                <span class="stop-order" :class="typeClass(stop.stopType)">{{ index + 1 }}</span>
              </div>

              <article class="stop-card">
                <div class="stop-card__top">
                  <div class="stop-name">
                    <span class="material-symbols-outlined" :class="typeClass(stop.stopType)">{{ typeIcon(stop.stopType) }}</span>
                    <div>
                      <h3>{{ stop.name }}</h3>
                      <p v-if="stop.address && stop.address !== stop.name">{{ stop.address }}</p>
                    </div>
                  </div>
                  <span class="stop-badge" :class="typeClass(stop.stopType)">{{ typeLabel(stop.stopType, index) }}</span>
                </div>

                <p class="stop-description">{{ typeDescription(stop.stopType) }}</p>

                <div v-if="showPassengerDetails && stop.stopType !== 'REST'" class="passenger-details">
                  <div v-if="pickupBookings(stop.id).length" class="passenger-group passenger-group--pickup">
                    <p class="passenger-group__title"><span class="material-symbols-outlined">login</span>Đón {{ passengerTotal(pickupBookings(stop.id)) }} khách</p>
                    <article v-for="booking in pickupBookings(stop.id)" :key="`pickup-${stop.id}-${booking.bookingId}`" class="passenger-row">
                      <div><strong>{{ booking.customerName }}</strong><span>Ghế {{ booking.seatNumbers.join(', ') }}</span></div>
                      <a :href="`tel:${booking.customerPhone}`" :aria-label="`Gọi ${booking.customerName}`"><span class="material-symbols-outlined">call</span>{{ booking.customerPhone }}</a>
                    </article>
                  </div>

                  <div v-if="dropoffBookings(stop.id).length" class="passenger-group passenger-group--dropoff">
                    <p class="passenger-group__title"><span class="material-symbols-outlined">logout</span>Trả {{ passengerTotal(dropoffBookings(stop.id)) }} khách</p>
                    <article v-for="booking in dropoffBookings(stop.id)" :key="`dropoff-${stop.id}-${booking.bookingId}`" class="passenger-row">
                      <div><strong>{{ booking.customerName }}</strong><span>Ghế {{ booking.seatNumbers.join(', ') }}</span></div>
                      <a :href="`tel:${booking.customerPhone}`" :aria-label="`Gọi ${booking.customerName}`"><span class="material-symbols-outlined">call</span>{{ booking.customerPhone }}</a>
                    </article>
                  </div>

                  <p v-if="!pickupBookings(stop.id).length && !dropoffBookings(stop.id).length" class="passenger-empty">Không có khách đón hoặc trả tại điểm này</p>
                </div>
              </article>
            </li>
          </ol>
        </main>

        <footer class="stop-footer">
          <p><span class="material-symbols-outlined">info</span>Giá vé được tính cho toàn bộ hành trình</p>
          <button type="button" @click="emit('close')">Đóng</button>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouteStopApi } from '@/services/routeStopApi'

const props = defineProps({
  trip: { type: Object, required: true },
  showPassengerDetails: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])
const api = useRouteStopApi()
const stops = ref([])
const loading = ref(true)
const error = ref('')
const passengers = ref([])

const pickupCount = computed(() => stops.value.filter(stop => ['PICKUP', 'BOTH'].includes(stop.stopType)).length)
const dropoffCount = computed(() => stops.value.filter(stop => ['DROPOFF', 'BOTH'].includes(stop.stopType)).length)
const restCount = computed(() => stops.value.filter(stop => stop.stopType === 'REST').length)

const loadStops = async () => {
  loading.value = true
  error.value = ''
  try {
    const requests = [api.getTripStops(props.trip.id)]
    if (props.showPassengerDetails) requests.push(api.getDriverStopPassengers(props.trip.id))
    const [stopsResponse, passengersResponse] = await Promise.all(requests)
    stops.value = stopsResponse.data || []
    passengers.value = passengersResponse?.data || []
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message || 'Vui lòng thử lại sau.'
  } finally {
    loading.value = false
  }
}

const typeLabel = (type, index) => {
  if (index === 0) return 'Điểm xuất phát'
  if (index === stops.value.length - 1) return 'Điểm kết thúc'
  return ({ PICKUP: 'Điểm đón', DROPOFF: 'Điểm trả', REST: 'Trạm nghỉ', BOTH: 'Đón và trả' }[type] || 'Điểm dừng')
}
const typeIcon = type => ({ PICKUP: 'person_pin_circle', DROPOFF: 'location_on', REST: 'local_cafe', BOTH: 'transfer_within_a_station' }[type] || 'pin_drop')
const typeClass = type => ({ PICKUP: 'is-pickup', DROPOFF: 'is-dropoff', REST: 'is-rest', BOTH: 'is-both' }[type] || 'is-both')
const typeDescription = type => ({
  PICKUP: 'Hành khách có thể lên xe tại điểm này',
  DROPOFF: 'Hành khách có thể xuống xe tại điểm này',
  REST: 'Xe dừng nghỉ, không đón hoặc trả khách',
  BOTH: 'Hành khách có thể lên hoặc xuống xe tại điểm này'
}[type] || '')
const pickupBookings = stopId => passengers.value.filter(booking => booking.pickupStopId === stopId)
const dropoffBookings = stopId => passengers.value.filter(booking => booking.dropoffStopId === stopId)
const passengerTotal = bookings => bookings.reduce((total, booking) => total + Number(booking.passengerCount || 0), 0)

const handleKeydown = event => { if (event.key === 'Escape') emit('close') }
onMounted(() => { window.addEventListener('keydown', handleKeydown); loadStops() })
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))
</script>

<style scoped>
.stop-overlay { position: fixed; inset: 0; z-index: 10010; display: grid; place-items: center; padding: 1rem; background: rgb(15 23 42 / .62); backdrop-filter: blur(6px); }
.stop-modal { display: flex; width: min(48rem, 100%); max-height: calc(100dvh - 2rem); flex-direction: column; overflow: hidden; border: 1px solid rgb(255 255 255 / .45); border-radius: 1.35rem; background: #f5f8f7; box-shadow: 0 2rem 5rem rgb(2 44 42 / .3); }
.stop-header { position: relative; display: flex; flex-shrink: 0; align-items: flex-start; justify-content: space-between; gap: 1rem; overflow: hidden; padding: 1rem 1.25rem; color: white; background: #075955; }
.stop-header::after { position: absolute; inset: 0; content: ''; opacity: .13; pointer-events: none; background-image: radial-gradient(circle at 1px 1px, white 1px, transparent 0); background-size: 18px 18px; mask-image: linear-gradient(90deg, transparent, black); }
.stop-header__main { position: relative; z-index: 1; display: flex; min-width: 0; align-items: center; gap: .8rem; }
.stop-header__icon { display: grid; width: 2.55rem; height: 2.55rem; flex: 0 0 auto; place-items: center; border: 1px solid rgb(255 255 255 / .18); border-radius: .72rem; color: #f3c94f; background: rgb(255 255 255 / .09); font-size: 1.25rem; }
.stop-header__copy > span { display: block; color: #a7f3d0; font-size: .58rem; font-weight: 850; letter-spacing: .1em; text-transform: uppercase; }
.stop-header h2 { margin-top: .08rem; font-size: 1.12rem; font-weight: 900; line-height: 1.2; letter-spacing: -.02em; }
.stop-header p { display: flex; min-width: 0; align-items: center; gap: .4rem; margin-top: .3rem; color: rgb(255 255 255 / .72); font-size: .66rem; font-weight: 650; }
.stop-header p strong { overflow: hidden; max-width: 15rem; text-overflow: ellipsis; white-space: nowrap; }
.stop-header p i { flex: 0 0 auto; color: #f3c94f; font-size: .8rem; font-style: normal; }
.stop-close { position: relative; z-index: 2; display: grid; width: 2rem; height: 2rem; flex: 0 0 auto; place-items: center; border-radius: .6rem; background: rgb(255 255 255 / .1); transition: background .2s, transform .2s; }
.stop-close .material-symbols-outlined { font-size: 1.15rem; }.stop-close:hover { background: rgb(255 255 255 / .2); }.stop-close:active { transform: scale(.93); }
.journey-summary { display: grid; flex-shrink: 0; grid-template-columns: repeat(4, 1fr); border-bottom: 1px solid #dbe6e3; background: white; }
.journey-summary > div { display: flex; align-items: center; justify-content: center; gap: .55rem; padding: .8rem .7rem; border-right: 1px solid #e3ebe9; }
.journey-summary > div:last-child { border-right: 0; }
.summary-icon { display: grid; width: 1.85rem; height: 1.85rem; place-items: center; border-radius: .5rem; color: #47635e; background: #edf3f1; font-size: 1rem; }
.summary-icon.is-pickup { color: #087a63; background: #e8f7f1; }.summary-icon.is-dropoff { color: #c43e5d; background: #fff0f3; }.summary-icon.is-rest { color: #b66313; background: #fff7e6; }
.journey-summary p { display: flex; flex-direction: column; }.journey-summary strong { color: #18332f; font-size: .9rem; line-height: 1; font-variant-numeric: tabular-nums; }.journey-summary small { margin-top: .2rem; color: #788b87; font-size: .58rem; font-weight: 700; }
.stop-body { min-height: 15rem; overflow-y: auto; padding: 1.2rem 1.4rem 1.5rem; scroll-behavior: smooth; }
.stop-timeline { position: relative; display: grid; gap: .65rem; }
.stop-timeline::before { position: absolute; top: 1.2rem; bottom: 1.2rem; left: 1.05rem; width: 2px; content: ''; background: #cfe6df; }
.stop-item { position: relative; display: grid; grid-template-columns: 2.15rem minmax(0, 1fr); gap: .75rem; }
.timeline-rail { position: relative; z-index: 1; padding-top: .75rem; }
.stop-order { display: grid; width: 2.15rem; height: 2.15rem; place-items: center; border: 4px solid #f5f8f7; border-radius: .65rem; color: white; background: #087363; font-size: .65rem; font-weight: 900; box-shadow: 0 4px 12px rgb(7 89 85 / .16); font-variant-numeric: tabular-nums; }
.stop-order.is-dropoff { background: #c84a66; }.stop-order.is-rest { color: #9a510e; background: #f8d99d; }.stop-order.is-both { background: #3978a8; }
.stop-card { border: 1px solid #dfe8e6; border-radius: .85rem; padding: .85rem .95rem; background: white; transition: border-color .2s, box-shadow .2s, transform .2s; }
.stop-card:hover { border-color: #bbd8d1; box-shadow: 0 .55rem 1.4rem rgb(27 82 75 / .07); transform: translateY(-1px); }
.stop-card__top { display: flex; align-items: flex-start; justify-content: space-between; gap: .75rem; }
.stop-name { display: flex; min-width: 0; align-items: flex-start; gap: .55rem; }
.stop-name > span { margin-top: .05rem; color: #087363; font-size: 1.05rem; }.stop-name > span.is-dropoff { color: #c43e5d; }.stop-name > span.is-rest { color: #b66313; }.stop-name > span.is-both { color: #3978a8; }
.stop-name h3 { color: #1e302d; font-size: .8rem; font-weight: 850; line-height: 1.35; }.stop-name p { margin-top: .2rem; color: #798a86; font-size: .66rem; font-weight: 600; line-height: 1.45; }
.stop-description { margin: .45rem 0 0 1.6rem; color: #98a6a3; font-size: .64rem; line-height: 1.45; }
.stop-badge { flex: 0 0 auto; border-radius: .4rem; padding: .28rem .45rem; color: #08725e; background: #eaf8f2; font-size: .53rem; font-weight: 900; letter-spacing: .045em; text-transform: uppercase; }
.stop-badge.is-dropoff { color: #b62f50; background: #fff0f3; }.stop-badge.is-rest { color: #aa590d; background: #fff6e4; }.stop-badge.is-both { color: #2d6693; background: #edf6fd; }
.passenger-details { display: grid; gap: .6rem; margin: .8rem 0 0 1.6rem; border-top: 1px dashed #dfe8e6; padding-top: .7rem; }
.passenger-group { overflow: hidden; border-radius: .65rem; background: #f7faf9; }
.passenger-group__title { display: flex; align-items: center; gap: .3rem; padding: .42rem .6rem; color: #08725e; background: #eaf8f2; font-size: .63rem; font-weight: 900; }.passenger-group--dropoff .passenger-group__title { color: #b62f50; background: #fff0f3; }.passenger-group__title span { font-size: .85rem; }
.passenger-row { display: flex; align-items: center; justify-content: space-between; gap: .75rem; border-top: 1px solid #e2ebe9; padding: .55rem .65rem; }.passenger-row div { min-width: 0; }.passenger-row strong { display: block; overflow: hidden; color: #253b37; font-size: .7rem; text-overflow: ellipsis; white-space: nowrap; }.passenger-row div span { display: block; margin-top: .12rem; color: #748581; font-size: .6rem; font-weight: 700; }
.passenger-row a { display: inline-flex; flex: 0 0 auto; align-items: center; gap: .25rem; border: 1px solid #d9e7e3; border-radius: .55rem; padding: .35rem .5rem; color: #075955; background: white; font-size: .62rem; font-weight: 850; transition: background .2s, transform .2s; }.passenger-row a:hover { background: #eaf8f2; }.passenger-row a:active { transform: scale(.96); }.passenger-row a span { font-size: .82rem; }
.passenger-empty { padding: .35rem; color: #91a09d; font-size: .62rem; font-style: italic; text-align: center; }
.stop-loading { display: grid; gap: .7rem; }.stop-skeleton { display: grid; grid-template-columns: 2.15rem 1fr; gap: .75rem; }.stop-skeleton > span { width: 2.15rem; height: 2.15rem; border-radius: .65rem; }.stop-skeleton > div { display: grid; gap: .55rem; border-radius: .85rem; padding: .9rem; background: white; }.stop-skeleton i { height: .65rem; border-radius: .25rem; }.stop-skeleton i:first-child { width: 45%; }.stop-skeleton i:last-child { width: 72%; }.stop-skeleton span, .stop-skeleton i { background: linear-gradient(90deg, #e2eae8 25%, #f3f6f5 45%, #e2eae8 65%); background-size: 220% 100%; animation: stop-wave 1.4s infinite linear; }
.stop-state { display: flex; min-height: 15rem; flex-direction: column; align-items: center; justify-content: center; text-align: center; }.stop-state > span { display: grid; width: 3.8rem; height: 3.8rem; place-items: center; border-radius: 1rem; color: #087363; background: #e7f6f1; font-size: 1.8rem; }.stop-state h3 { margin-top: .9rem; color: #1d342f; font-size: .95rem; font-weight: 900; }.stop-state p { max-width: 25rem; margin-top: .35rem; color: #768985; font-size: .72rem; line-height: 1.55; }.stop-state button { margin-top: .9rem; border-radius: .65rem; padding: .6rem .9rem; color: white; background: #075955; font-size: .7rem; font-weight: 850; }.stop-state--error > span { color: #c43e5d; background: #fff0f3; }
.stop-footer { display: flex; flex-shrink: 0; align-items: center; justify-content: space-between; gap: 1rem; border-top: 1px solid #dfe8e6; padding: .75rem 1.25rem; background: white; }.stop-footer p { display: flex; align-items: center; gap: .4rem; color: #70827e; font-size: .65rem; font-weight: 700; }.stop-footer p span { color: #087363; font-size: 1rem; }.stop-footer button { border-radius: .65rem; padding: .62rem 1rem; color: white; background: #075955; font-size: .7rem; font-weight: 900; transition: background .2s, transform .2s; }.stop-footer button:hover { background: #064b48; }.stop-footer button:active { transform: scale(.96); }
.stop-close:focus-visible, .stop-footer button:focus-visible, .stop-state button:focus-visible, .passenger-row a:focus-visible { outline: 3px solid #f3c94f; outline-offset: 2px; }
@keyframes stop-wave { to { background-position-x: -220%; } }
@media (max-width: 640px) {
  .stop-overlay { padding: 0; }.stop-modal { width: 100%; max-height: 100dvh; min-height: 100dvh; border: 0; border-radius: 0; }
  .stop-header { padding: .8rem .9rem; }.stop-header__icon { display: none; }.stop-header h2 { font-size: 1rem; }.stop-header p strong { max-width: 8.5rem; }
  .journey-summary > div { gap: .3rem; padding: .65rem .25rem; }.summary-icon { width: 1.55rem; height: 1.55rem; font-size: .85rem; }.journey-summary small { font-size: .5rem; }
  .stop-body { padding: .9rem .8rem 1.1rem; }.stop-item { gap: .55rem; }.stop-card { padding: .75rem; }.stop-description, .passenger-details { margin-left: 0; }
  .stop-badge { max-width: 6.5rem; text-align: center; }.stop-footer { padding: .7rem .9rem; }
}
</style>
