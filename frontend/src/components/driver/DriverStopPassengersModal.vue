<template>
  <Teleport to="body">
    <div class="dispatch-overlay" role="presentation" @click.self="$emit('close')">
      <section class="dispatch-modal" role="dialog" aria-modal="true" aria-labelledby="dispatch-title">
        <header class="dispatch-header">
          <div>
            <span>Điều phối hành khách</span>
            <h2 id="dispatch-title">Danh sách khách đón và trả</h2>
            <p>{{ trip.departurePoint }} → {{ trip.arrivalPoint }}</p>
          </div>
          <button type="button" aria-label="Đóng" @click="$emit('close')"><span class="material-symbols-outlined">close</span></button>
        </header>

        <main class="dispatch-body">
          <div v-if="loading" class="space-y-3">
            <div class="grid grid-cols-3 gap-3"><div v-for="item in 3" :key="item" class="h-20 animate-pulse rounded-xl bg-slate-100" /></div>
            <div class="h-52 animate-pulse rounded-2xl bg-slate-100" />
          </div>

          <div v-else-if="error" class="dispatch-state">
            <span class="material-symbols-outlined">cloud_off</span>
            <strong>Không tải được danh sách hành khách</strong>
            <p>{{ error }}</p>
            <button type="button" @click="loadData">Thử lại</button>
          </div>

          <template v-else>
            <section class="dispatch-summary" aria-label="Tổng quan hành khách">
              <article class="summary-card summary-card--pickup"><span class="material-symbols-outlined summary-pickup">login</span><div><small>Cần đón</small><strong>{{ pickupTotal }} khách</strong></div></article>
              <article class="summary-card summary-card--dropoff"><span class="material-symbols-outlined summary-dropoff">logout</span><div><small>Cần trả</small><strong>{{ dropoffTotal }} khách</strong></div></article>
              <article class="summary-card summary-card--contact"><span class="material-symbols-outlined summary-contact">call</span><div><small>Liên hệ</small><strong>{{ contactCount }} số</strong></div></article>
            </section>

            <nav class="dispatch-tabs" aria-label="Loại danh sách">
              <button type="button" class="tab-pickup" :class="{ active: activeTab === 'pickup' }" @click="activeTab = 'pickup'"><span class="material-symbols-outlined">person_pin_circle</span>Cần đón <b>{{ pickupTotal }}</b></button>
              <button type="button" class="tab-dropoff" :class="{ active: activeTab === 'dropoff' }" @click="activeTab = 'dropoff'"><span class="material-symbols-outlined">location_on</span>Cần trả <b>{{ dropoffTotal }}</b></button>
            </nav>

            <section v-if="currentGroups.length" class="dispatch-groups">
              <article v-for="(group, index) in currentGroups" :key="`${activeTab}-${group.stop.id}`" class="dispatch-group" :class="activeTab === 'pickup' ? 'dispatch-group--pickup' : 'dispatch-group--dropoff'">
                <header>
                  <span class="dispatch-order">{{ index + 1 }}</span>
                  <div><small>{{ activeTab === 'pickup' ? 'Điểm đón' : 'Điểm trả' }}</small><h3>{{ group.stop.name }}</h3></div>
                  <strong>{{ group.total }} khách</strong>
                </header>
                <div class="dispatch-passengers">
                  <div v-for="booking in group.bookings" :key="booking.bookingId" class="dispatch-passenger">
                    <span class="passenger-avatar" :class="activeTab === 'pickup' ? 'passenger-avatar--pickup' : 'passenger-avatar--dropoff'">{{ initials(booking.customerName) }}</span>
                    <div class="passenger-copy"><strong>{{ booking.customerName }}</strong><span>{{ booking.passengerCount }} khách · Ghế {{ booking.seatNumbers.join(', ') }}</span></div>
                    <a :href="`tel:${booking.customerPhone}`" :aria-label="`Gọi ${booking.customerName}`"><span class="material-symbols-outlined">call</span><em>{{ booking.customerPhone }}</em></a>
                  </div>
                </div>
              </article>
            </section>

            <div v-else class="dispatch-state dispatch-state--empty">
              <span class="material-symbols-outlined">group_off</span>
              <strong>{{ activeTab === 'pickup' ? 'Không có khách cần đón' : 'Không có khách cần trả' }}</strong>
              <p>Hiện chưa có hành khách tại các điểm này.</p>
            </div>
          </template>
        </main>

        <footer class="dispatch-footer">
          <p><span class="material-symbols-outlined">verified_user</span>Chỉ hiển thị hành khách thuộc chuyến được phân công</p>
          <button type="button" @click="$emit('close')">Đóng</button>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouteStopApi } from '@/services/routeStopApi'

const props = defineProps({ trip: { type: Object, required: true } })
const emit = defineEmits(['close'])
const api = useRouteStopApi()
const stops = ref([])
const passengers = ref([])
const loading = ref(true)
const error = ref('')
const activeTab = ref('pickup')

const passengerTotal = bookings => bookings.reduce((total, booking) => total + Number(booking.passengerCount || 0), 0)
const buildGroups = field => stops.value.map(stop => {
  const bookings = passengers.value.filter(booking => booking[field] === stop.id)
  return { stop, bookings, total: passengerTotal(bookings) }
}).filter(group => group.bookings.length)

const pickupGroups = computed(() => buildGroups('pickupStopId'))
const dropoffGroups = computed(() => buildGroups('dropoffStopId'))
const pickupTotal = computed(() => passengerTotal(passengers.value.filter(booking => booking.pickupStopId)))
const dropoffTotal = computed(() => passengerTotal(passengers.value.filter(booking => booking.dropoffStopId)))
const contactCount = computed(() => new Set(passengers.value.map(booking => booking.customerPhone).filter(Boolean)).size)
const currentGroups = computed(() => activeTab.value === 'pickup' ? pickupGroups.value : dropoffGroups.value)

const initials = name => (name || '?').trim().split(/\s+/).slice(-2).map(part => part[0]).join('').toUpperCase()

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [stopsResponse, passengersResponse] = await Promise.all([
      api.getTripStops(props.trip.id),
      api.getDriverStopPassengers(props.trip.id)
    ])
    stops.value = stopsResponse.data || []
    passengers.value = passengersResponse.data || []
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message || 'Vui lòng thử lại sau.'
  } finally {
    loading.value = false
  }
}

const handleKeydown = event => { if (event.key === 'Escape') emit('close') }
onMounted(() => { window.addEventListener('keydown', handleKeydown); loadData() })
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))
</script>

<style scoped>
.dispatch-overlay { position: fixed; inset: 0; z-index: 10010; display: flex; align-items: center; justify-content: center; padding: 1rem; background: rgb(15 23 42 / .62); backdrop-filter: blur(5px); }
.dispatch-modal { display: flex; width: min(62rem, calc(100vw - 2rem)); height: min(50rem, calc(100dvh - 1.5rem)); max-height: calc(100dvh - 1.5rem); flex-direction: column; overflow: hidden; border-radius: 1.25rem; background: #f5f7fa; box-shadow: 0 30px 80px rgb(15 23 42 / .32); }
.dispatch-header { display: flex; flex-shrink: 0; align-items: flex-start; justify-content: space-between; gap: 1rem; background: #17324d; padding: 1.4rem 1.75rem; color: white; }
.dispatch-header > div > span { font-size: .62rem; font-weight: 850; letter-spacing: .12em; text-transform: uppercase; color: rgb(147 197 253); }
.dispatch-header h2 { margin-top: .2rem; font-size: 1.18rem; font-weight: 950; letter-spacing: -.025em; }
.dispatch-header p { margin-top: .3rem; font-size: .72rem; font-weight: 600; color: rgb(203 213 225); }
.dispatch-header button { display: inline-flex; border-radius: .6rem; padding: .4rem; transition: background .2s, transform .2s; }
.dispatch-header button:hover { background: rgb(255 255 255 / .12); }
.dispatch-header button:active { transform: scale(.92); }
.dispatch-body { min-height: 22rem; flex: 1; overflow-y: auto; padding: 1.5rem; background: #eef2f7; }
.dispatch-summary { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: .75rem; }
.dispatch-summary article { display: flex; min-width: 0; align-items: center; gap: .65rem; border: 1px solid; border-radius: .9rem; padding: .8rem; box-shadow: 0 5px 16px rgb(15 23 42 / .05); }
.summary-card--pickup { border-color: rgb(110 231 183) !important; background: rgb(236 253 245); }
.summary-card--dropoff { border-color: rgb(253 164 175) !important; background: rgb(255 241 242); }
.summary-card--contact { border-color: rgb(147 197 253) !important; background: rgb(239 246 255); }
.dispatch-summary .material-symbols-outlined { display: flex; height: 2.3rem; width: 2.3rem; flex-shrink: 0; align-items: center; justify-content: center; border-radius: .7rem; font-size: 1.1rem; }
.summary-pickup { background: rgb(5 150 105); color: white; }
.summary-dropoff { background: rgb(225 29 72); color: white; }
.summary-contact { background: rgb(37 99 235); color: white; }
.dispatch-summary small { display: block; font-size: .6rem; font-weight: 750; color: rgb(100 116 139); }
.dispatch-summary strong { display: block; margin-top: .1rem; font-size: .86rem; color: rgb(15 23 42); }
.dispatch-tabs { display: grid; grid-template-columns: 1fr 1fr; gap: .5rem; margin-top: 1rem; border-radius: .9rem; background: white; padding: .35rem; box-shadow: inset 0 0 0 1px rgb(203 213 225); }
.dispatch-tabs button { display: flex; align-items: center; justify-content: center; gap: .4rem; border-radius: .65rem; padding: .7rem; color: rgb(100 116 139); font-size: .72rem; font-weight: 850; transition: background .2s, color .2s, box-shadow .2s; }
.dispatch-tabs .tab-pickup.active { background: rgb(5 150 105); color: white; box-shadow: 0 5px 14px rgb(5 150 105 / .22); }
.dispatch-tabs .tab-dropoff.active { background: rgb(225 29 72); color: white; box-shadow: 0 5px 14px rgb(225 29 72 / .2); }
.dispatch-tabs .material-symbols-outlined { font-size: 1rem; }
.dispatch-tabs b { display: inline-flex; min-width: 1.3rem; justify-content: center; border-radius: .35rem; background: rgb(241 245 249); padding: .12rem .3rem; color: rgb(71 85 105); font-size: .62rem; }
.dispatch-tabs button.active b { background: rgb(255 255 255 / .2); color: white; }
.dispatch-groups { display: grid; gap: .85rem; margin-top: 1rem; }
.dispatch-group { overflow: hidden; border: 1px solid; border-radius: 1rem; background: white; box-shadow: 0 6px 18px rgb(15 23 42 / .06); }
.dispatch-group--pickup { border-color: rgb(167 243 208); }
.dispatch-group--dropoff { border-color: rgb(254 205 211); }
.dispatch-group > header { display: grid; grid-template-columns: 2rem minmax(0, 1fr) auto; align-items: center; gap: .7rem; border-bottom: 1px solid rgb(226 232 240); padding: .75rem .9rem; }
.dispatch-order { display: flex; height: 2rem; width: 2rem; align-items: center; justify-content: center; border-radius: .6rem; color: white; font-size: .68rem; font-weight: 900; }
.dispatch-group--pickup > header { background: rgb(236 253 245); border-bottom-color: rgb(167 243 208); }
.dispatch-group--dropoff > header { background: rgb(255 241 242); border-bottom-color: rgb(254 205 211); }
.dispatch-group--pickup .dispatch-order { background: rgb(5 150 105); }
.dispatch-group--dropoff .dispatch-order { background: rgb(225 29 72); }
.dispatch-group header small { display: block; font-size: .55rem; font-weight: 850; letter-spacing: .08em; text-transform: uppercase; color: rgb(148 163 184); }
.dispatch-group header h3 { margin-top: .1rem; font-size: .8rem; font-weight: 900; color: rgb(30 41 59); }
.dispatch-group header > strong { border-radius: .5rem; padding: .35rem .55rem; font-size: .65rem; }
.dispatch-group--pickup header > strong { background: rgb(209 250 229); color: rgb(4 120 87); }
.dispatch-group--dropoff header > strong { background: rgb(255 228 230); color: rgb(190 18 60); }
.dispatch-passengers { display: grid; }
.dispatch-passenger { display: grid; grid-template-columns: 2.25rem minmax(0, 1fr) auto; align-items: center; gap: .65rem; border-top: 1px solid rgb(241 245 249); padding: .7rem .9rem; transition: background .2s; }
.dispatch-passenger:hover { background: rgb(248 250 252); }
.dispatch-passenger:first-child { border-top: 0; }
.passenger-avatar { display: flex; height: 2.25rem; width: 2.25rem; align-items: center; justify-content: center; border-radius: .65rem; font-size: .65rem; font-weight: 900; }
.passenger-avatar--pickup { background: rgb(209 250 229); color: rgb(4 120 87); }
.passenger-avatar--dropoff { background: rgb(255 228 230); color: rgb(190 18 60); }
.passenger-copy { min-width: 0; }
.passenger-copy strong { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: .75rem; color: rgb(30 41 59); }
.passenger-copy span { display: block; margin-top: .15rem; font-size: .62rem; font-weight: 650; color: rgb(100 116 139); }
.dispatch-passenger a { display: inline-flex; align-items: center; gap: .3rem; border-radius: .65rem; background: rgb(219 234 254); padding: .5rem .6rem; color: rgb(29 78 216); font-size: .65rem; font-weight: 850; transition: background .2s, transform .2s; }
.dispatch-passenger a:hover { background: rgb(191 219 254); }
.dispatch-passenger a:active { transform: scale(.96); }
.dispatch-passenger a .material-symbols-outlined { font-size: .95rem; }
.dispatch-passenger a em { font-style: normal; }
.dispatch-state { display: flex; min-height: 20rem; flex-direction: column; align-items: center; justify-content: center; text-align: center; color: rgb(100 116 139); }
.dispatch-state > .material-symbols-outlined { margin-bottom: .7rem; font-size: 2.5rem; color: rgb(225 29 72); }
.dispatch-state strong { color: rgb(30 41 59); }
.dispatch-state p { margin-top: .35rem; font-size: .72rem; }
.dispatch-state button { margin-top: 1rem; border-radius: .65rem; background: #17324d; padding: .65rem 1rem; color: white; font-size: .72rem; font-weight: 850; }
.dispatch-state--empty > .material-symbols-outlined { color: rgb(148 163 184); }
.dispatch-footer { display: flex; flex-shrink: 0; align-items: center; justify-content: space-between; gap: 1rem; border-top: 1px solid rgb(226 232 240); background: white; padding: .9rem 1.25rem; }
.dispatch-footer p { display: flex; align-items: center; gap: .35rem; font-size: .62rem; font-weight: 650; color: rgb(100 116 139); }
.dispatch-footer p .material-symbols-outlined { font-size: .9rem; color: rgb(37 99 235); }
.dispatch-footer button { border-radius: .65rem; background: #17324d; padding: .65rem 1rem; color: white; font-size: .72rem; font-weight: 900; transition: background .2s, transform .2s; }
.dispatch-footer button:hover { background: #0f2538; }
.dispatch-footer button:active { transform: scale(.96); }
@media (max-width: 36rem) {
  .dispatch-modal { width: calc(100vw - 1rem); height: calc(100dvh - 1rem); max-height: calc(100dvh - 1rem); }
  .dispatch-header { padding: 1rem; }
  .dispatch-body { padding: .85rem; }
  .dispatch-summary { grid-template-columns: 1fr; }
  .dispatch-passenger a em { display: none; }
  .dispatch-footer p { display: none; }
  .dispatch-footer { justify-content: flex-end; }
}
</style>
