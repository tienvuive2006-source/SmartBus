<template>
  <section v-if="stops.length" class="rounded-2xl border border-emerald-100 bg-white p-6 shadow-sm md:p-8">
    <div class="mb-5 flex items-center gap-3"><span class="material-symbols-outlined rounded-full bg-emerald-50 p-2 text-[#075955]">pin_drop</span><div><h2 class="text-sm font-black uppercase tracking-widest text-slate-900">{{ title }}</h2><p class="mt-1 text-xs font-semibold text-slate-500">Giá vé giữ nguyên cho toàn chuyến</p></div></div>
    <div class="grid gap-4 md:grid-cols-2">
      <label><span class="mb-1.5 block text-[10px] font-black uppercase tracking-widest text-slate-500">Điểm đón</span><select v-model.number="pickupId" class="stop-select"><option v-for="stop in pickupOptions" :key="stop.id" :value="stop.id">{{ stop.name }}</option></select><small v-if="selectedPickup?.address && selectedPickup.address !== selectedPickup.name" class="mt-1 block text-slate-400">{{ selectedPickup.address }}</small></label>
      <label><span class="mb-1.5 block text-[10px] font-black uppercase tracking-widest text-slate-500">Điểm trả</span><select v-model.number="dropoffId" class="stop-select"><option v-for="stop in dropoffOptions" :key="stop.id" :value="stop.id">{{ stop.name }}</option></select><small v-if="selectedDropoff?.address && selectedDropoff.address !== selectedDropoff.name" class="mt-1 block text-slate-400">{{ selectedDropoff.address }}</small></label>
    </div>
    <p v-if="!valid" class="mt-3 text-xs font-bold text-rose-600">Điểm trả phải nằm sau điểm đón.</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouteStopApi } from '@/services/routeStopApi'
const props = defineProps({ tripId: [String, Number], title: { type: String, default: 'Điểm đón và điểm trả' }, modelValue: { type: Object, default: () => ({ valid: true }) } })
const emit = defineEmits(['update:modelValue'])
const api = useRouteStopApi(), stops = ref([]), pickupId = ref(null), dropoffId = ref(null)
const pickupOptions = computed(() => stops.value.filter(s => ['PICKUP', 'BOTH'].includes(s.stopType) && s.stopOrder < stops.value.length - 1))
const selectedPickup = computed(() => stops.value.find(s => s.id === pickupId.value))
const dropoffOptions = computed(() => stops.value.filter(s => ['DROPOFF', 'BOTH'].includes(s.stopType) && (!selectedPickup.value || s.stopOrder > selectedPickup.value.stopOrder)))
const selectedDropoff = computed(() => stops.value.find(s => s.id === dropoffId.value))
const valid = computed(() => !!selectedPickup.value && !!selectedDropoff.value && selectedPickup.value.stopOrder < selectedDropoff.value.stopOrder)
const publish = () => emit('update:modelValue', { pickupStopId: pickupId.value, dropoffStopId: dropoffId.value, pickupName: selectedPickup.value?.name, dropoffName: selectedDropoff.value?.name, valid: stops.value.length ? valid.value : true })
watch(pickupId, () => { if (!dropoffOptions.value.some(s => s.id === dropoffId.value)) dropoffId.value = dropoffOptions.value.at(-1)?.id || null; publish() })
watch(dropoffId, publish)
onMounted(async () => {
  try {
    const response = await api.getTripStops(props.tripId); stops.value = response.data || []
    if (stops.value.length) { pickupId.value = pickupOptions.value[0]?.id || null; dropoffId.value = dropoffOptions.value.at(-1)?.id || null }
    publish()
  } catch { stops.value = []; publish() }
})
</script>

<style scoped>.stop-select { width: 100%; border: 1px solid rgb(226 232 240); border-radius: .8rem; background: rgb(248 250 252); padding: .85rem; font-size: .8rem; font-weight: 700; outline: none; }.stop-select:focus { border-color: #075955; }</style>
