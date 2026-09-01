<template>
  <section class="return-bar" :class="{ inactive: !modelValue }">
    <label class="return-toggle">
      <input
        :checked="modelValue"
        type="checkbox"
        @change="$emit('update:modelValue', $event.target.checked)"
      />
      <span class="route-icon"><span class="material-symbols-outlined">swap_horiz</span></span>
      <span class="decision">
        <strong>Gán luôn chuyến về</strong>
        <small>{{ modelValue ? 'Cùng tài xế và xe đã chọn' : 'Không gán chuyến về' }}</small>
      </span>
    </label>

    <div class="return-route">
      <span class="route-points">
        <strong>{{ shortPoint(returnTrip.departurePoint) }}</strong>
        <span class="material-symbols-outlined">arrow_forward</span>
        <strong>{{ shortPoint(returnTrip.arrivalPoint) }}</strong>
      </span>
      <span class="route-time">
        <span class="material-symbols-outlined">calendar_month</span>
        {{ formatDate(returnTrip.departureDate) }}
        <span class="dot">•</span>
        <span class="material-symbols-outlined">schedule</span>
        {{ returnTrip.departureTime }}–{{ returnTrip.arrivalTime || 'Chưa rõ' }}
        <template v-if="waitingLabel"><span class="dot">•</span><em>Chờ {{ waitingLabel }}</em></template>
      </span>
    </div>

    <div class="assigned-resources">
      <span><span class="material-symbols-outlined">badge</span><strong>{{ driverName }}</strong></span>
      <span><span class="material-symbols-outlined">directions_bus</span><strong>{{ licensePlate }}</strong></span>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: true },
  outboundTrip: { type: Object, required: true },
  returnTrip: { type: Object, required: true },
  driverName: { type: String, default: '' },
  licensePlate: { type: String, default: '' }
})

defineEmits(['update:modelValue'])

const shortPoint = value => String(value || '').split(',')[0]
const formatDate = value => {
  if (!value) return ''
  const [year, month, day] = value.split('-')
  return `${day}/${month}/${year}`
}
const toTimestamp = (date, time) => {
  if (!date || !time) return null
  const timestamp = new Date(`${date}T${time}:00`).getTime()
  return Number.isNaN(timestamp) ? null : timestamp
}
const outboundArrivalTimestamp = computed(() => {
  const departure = toTimestamp(props.outboundTrip.departureDate, props.outboundTrip.departureTime)
  let arrival = toTimestamp(props.outboundTrip.departureDate, props.outboundTrip.arrivalTime)
  if (departure !== null && arrival !== null && arrival < departure) arrival += 86400000
  return arrival
})
const waitingLabel = computed(() => {
  const arrival = outboundArrivalTimestamp.value
  const returnDeparture = toTimestamp(props.returnTrip.departureDate, props.returnTrip.departureTime)
  if (arrival === null || returnDeparture === null || returnDeparture < arrival) return ''
  const totalMinutes = Math.round((returnDeparture - arrival) / 60000)
  const days = Math.floor(totalMinutes / 1440)
  const hours = Math.floor((totalMinutes % 1440) / 60)
  const minutes = totalMinutes % 60
  return [days ? `${days} ngày` : '', hours ? `${hours} giờ` : '', minutes ? `${minutes} phút` : ''].filter(Boolean).join(' ')
})
</script>

<style scoped>
.return-bar{display:grid;grid-template-columns:minmax(13rem,.85fr) minmax(24rem,1.7fr) minmax(16rem,.8fr);align-items:center;gap:1rem;min-height:4.25rem;flex:none;border-top:1px solid #bce9db;background:#f2fbf7;padding:.65rem 1.25rem;color:#173c37;transition:.2s ease}.return-bar.inactive{border-color:#e2e8f0;background:#f8fafc;color:#64748b}.return-toggle{display:flex;cursor:pointer;align-items:center;gap:.7rem;min-width:0}.return-toggle input{width:1.05rem;height:1.05rem;flex:none;accent-color:#087c6d}.route-icon{display:flex;width:2.35rem;height:2.35rem;flex:none;align-items:center;justify-content:center;border-radius:.7rem;background:#087c6d;color:#fff}.route-icon span{font-size:1.15rem}.decision{min-width:0}.decision strong,.decision small{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.decision strong{font-size:.78rem;font-weight:900}.decision small{margin-top:.15rem;font-size:.64rem;font-weight:600;color:#648079}.return-route{min-width:0;border-left:1px solid #cfe8e1;padding-left:1rem}.route-points{display:flex;align-items:center;gap:.5rem;min-width:0}.route-points strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.76rem;font-weight:850;color:#1e293b}.route-points .material-symbols-outlined{flex:none;font-size:1rem;color:#0b8a79}.route-time{display:flex;align-items:center;gap:.28rem;margin-top:.3rem;font-size:.63rem;font-weight:700;color:#64748b;white-space:nowrap}.route-time .material-symbols-outlined{font-size:.82rem;color:#78908b}.route-time .dot{margin:0 .15rem;color:#b1c4c0}.route-time em{font-style:normal;color:#b56708}.assigned-resources{display:grid;grid-template-columns:1fr 1fr;gap:.45rem}.assigned-resources>span{display:flex;min-width:0;align-items:center;gap:.4rem;border-radius:.55rem;background:#fff;padding:.52rem .6rem;box-shadow:inset 0 0 0 1px #dce9e6}.assigned-resources .material-symbols-outlined{flex:none;font-size:1rem;color:#087c6d}.assigned-resources strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.68rem;font-weight:850;color:#334155}.inactive .route-icon{background:#94a3b8}.inactive .assigned-resources{opacity:.55}
@media(max-width:1100px){.return-bar{grid-template-columns:minmax(12rem,.8fr) minmax(22rem,1.5fr)}.assigned-resources{display:none}}
</style>
