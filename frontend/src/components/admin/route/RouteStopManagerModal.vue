<template>
  <Teleport to="body">
    <div v-if="open" class="route-stop-overlay fixed inset-0 z-[10000] flex items-center justify-center bg-slate-900/60 p-3 backdrop-blur-sm">
      <section class="route-stop-modal flex w-full max-w-5xl flex-col overflow-hidden rounded-[28px] bg-[#f7f9f8] shadow-2xl shadow-emerald-950/30">
        <header class="route-stop-header flex shrink-0 items-start justify-between bg-[#075955] px-6 py-5 text-white md:px-8">
          <div>
            <p class="text-xl font-black tracking-tight">Thiết lập điểm đón và trả khách</p>
            <p class="mt-1 text-sm font-medium text-emerald-100">{{ route?.departurePoint }} → {{ route?.arrivalPoint }}</p>
          </div>
          <button type="button" class="rounded-xl p-2 transition hover:bg-white/10 active:scale-95" aria-label="Đóng" @click="open = false"><span class="material-symbols-outlined">close</span></button>
        </header>

        <div class="route-stop-layout min-h-0 flex-1 overflow-hidden">
          <main ref="formScroller" class="route-stop-form min-h-0 overflow-y-auto p-5 md:p-7">
            <div class="mb-5 rounded-2xl bg-emerald-50 px-4 py-3 text-sm font-semibold leading-6 text-emerald-900">
              Xe đi theo thứ tự từ trên xuống. Điểm đón/trả dành cho hành khách; trạm nghỉ chỉ là nơi xe dừng để khách nghỉ chân. Giá vé không thay đổi.
            </div>

            <div class="relative space-y-4 before:absolute before:bottom-12 before:left-[23px] before:top-12 before:w-0.5 before:bg-emerald-200">
              <article v-for="(stop, index) in stops" :key="stop.localId" class="relative grid gap-4 rounded-2xl bg-white p-4 shadow-[0_8px_24px_rgba(7,89,85,0.06)] md:grid-cols-[48px_1fr]">
                <div class="relative z-10 flex h-12 w-12 items-center justify-center rounded-2xl text-sm font-black text-white shadow-md" :class="stopBadgeClass(stop, index)">
                  {{ index + 1 }}
                </div>

                <div>
                  <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <p class="font-black text-slate-900">{{ stopTitle(index) }}</p>
                      <p class="mt-0.5 text-xs font-medium text-slate-500">{{ stopDescription(index) }}</p>
                    </div>
                    <div v-if="isMiddleStop(index)" class="flex items-center gap-1">
                      <button type="button" class="icon-btn" :disabled="index === 1" title="Đưa lên trên" @click="move(index, -1)"><span class="material-symbols-outlined">arrow_upward</span></button>
                      <button type="button" class="icon-btn" :disabled="index === stops.length - 2" title="Đưa xuống dưới" @click="move(index, 1)"><span class="material-symbols-outlined">arrow_downward</span></button>
                      <button type="button" class="icon-btn text-rose-500" title="Xóa trạm" @click="removeStop(index)"><span class="material-symbols-outlined">delete</span></button>
                    </div>
                  </div>

                  <div class="grid gap-4 md:grid-cols-2">
                    <label class="field-group md:col-span-2"><span>{{ stop.stopType === 'REST' ? 'Tên trạm nghỉ' : 'Tên hoặc địa chỉ điểm đón/trả' }}</span><input v-model.trim="stop.name" class="field" :placeholder="stop.stopType === 'REST' ? 'Ví dụ: Trạm dừng chân Tam Kỳ' : 'Ví dụ: 03 Nam Cao, Đà Nẵng'" /></label>
                    <label class="field-group md:col-span-2">
                      <span>Loại điểm trên hành trình</span>
                      <div class="locked-field" :class="stopTypeClass(stop.stopType)"><span class="material-symbols-outlined">{{ stopTypeIcon(stop.stopType) }}</span>{{ stopTypeLabel(stop.stopType) }}</div>
                    </label>
                  </div>
                </div>
              </article>
            </div>

            <div class="mt-4 grid gap-3 md:grid-cols-3">
              <button type="button" class="add-stop-btn add-stop-btn--pickup" @click="addStop('PICKUP')"><span class="material-symbols-outlined">person_pin_circle</span><span>Thêm điểm đón<small>Khách lên xe</small></span></button>
              <button type="button" class="add-stop-btn add-stop-btn--dropoff" @click="addStop('DROPOFF')"><span class="material-symbols-outlined">location_on</span><span>Thêm điểm trả<small>Khách xuống xe</small></span></button>
              <button type="button" class="add-stop-btn add-stop-btn--rest" @click="addStop('REST')"><span class="material-symbols-outlined">local_cafe</span><span>Thêm trạm nghỉ<small>Chỉ nghỉ chân</small></span></button>
            </div>
            <p v-if="error" class="mt-4 rounded-xl bg-rose-50 p-3 text-sm font-bold text-rose-700">{{ error }}</p>
          </main>

          <aside class="setup-guide overflow-y-auto border-l border-slate-200 bg-white p-6">
            <p class="text-sm font-black text-slate-900">Cách thiết lập</p>
            <ol class="mt-5 space-y-5">
              <li v-for="item in guide" :key="item.step" class="flex gap-3"><span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-[#075955] text-xs font-black text-white">{{ item.step }}</span><div><p class="text-sm font-bold text-slate-800">{{ item.title }}</p><p class="mt-1 text-xs leading-5 text-slate-500">{{ item.text }}</p></div></li>
            </ol>
            <div class="mt-7 rounded-2xl bg-amber-50 p-4 text-xs leading-5 text-amber-900"><strong>Lưu ý:</strong><br>Sắp xếp các điểm đúng theo thứ tự xe chạy từ điểm đầu đến điểm cuối.</div>
          </aside>
        </div>

        <footer class="route-stop-footer flex shrink-0 items-center justify-between border-t border-slate-200 bg-white px-6 py-4 md:px-8">
          <p class="hidden text-xs font-semibold text-slate-500 sm:block">{{ stops.length }} điểm trên lộ trình</p>
          <div class="ml-auto flex gap-2"><button type="button" class="rounded-xl px-5 py-3 text-sm font-bold text-slate-500 transition hover:bg-slate-100" @click="open = false">Hủy</button><button type="button" :disabled="saving" class="rounded-xl bg-[#075955] px-6 py-3 text-sm font-black text-white shadow-lg shadow-emerald-900/15 transition hover:bg-[#064b48] active:scale-95 disabled:opacity-50" @click="save">{{ saving ? 'Đang lưu...' : 'Lưu lộ trình đón trả' }}</button></div>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { useRouteStopApi } from '@/services/routeStopApi'
const api = useRouteStopApi()
const emit = defineEmits(['notify'])
const open = ref(false), route = ref(null), stops = ref([]), saving = ref(false), error = ref('')
const formScroller = ref(null)
const guide = [
  { step: 1, title: 'Điểm đầu và cuối', text: 'Hệ thống giữ cố định nơi xe xuất phát và kết thúc.' },
  { step: 2, title: 'Điểm đón, điểm trả', text: 'Thêm nơi hành khách được lên xe hoặc xuống xe.' },
  { step: 3, title: 'Trạm nghỉ', text: 'Chỉ là nơi nghỉ chân và khách không thể chọn để lên hoặc xuống xe.' }
]
let localId = 0
const makeStop = data => ({ localId: ++localId, name: '', address: '', stopType: 'BOTH', offsetMinutes: 0, ...data })
const normalizeEndpoints = () => {
  if (!stops.value.length) return
  stops.value[0].stopType = 'PICKUP'; stops.value[0].offsetMinutes = 0
  stops.value[stops.value.length - 1].stopType = 'DROPOFF'
}
const show = async selectedRoute => {
  route.value = selectedRoute; open.value = true; error.value = ''
  await nextTick()
  if (formScroller.value) formScroller.value.scrollTop = 0
  try {
    const response = await api.getRouteStops(selectedRoute.id)
    stops.value = response.data.length ? response.data.map(makeStop) : [
      makeStop({ name: selectedRoute.departurePoint, address: selectedRoute.departurePoint, stopType: 'PICKUP', offsetMinutes: 0 }),
      makeStop({ name: selectedRoute.arrivalPoint, address: selectedRoute.arrivalPoint, stopType: 'DROPOFF', offsetMinutes: 60 })
    ]
    normalizeEndpoints()
  } catch (e) { error.value = e.response?.data?.error || 'Không tải được điểm dừng.' }
}
const isMiddleStop = index => index > 0 && index < stops.value.length - 1
const typeNumber = (index, type) => stops.value.slice(1, index + 1).filter(stop => stop.stopType === type).length
const stopTitle = index => {
  if (index === 0) return 'Điểm bắt đầu'
  if (index === stops.value.length - 1) return 'Điểm kết thúc'
  const type = stops.value[index].stopType
  if (type === 'PICKUP') return `Điểm đón ${typeNumber(index, type)}`
  if (type === 'DROPOFF') return `Điểm trả ${typeNumber(index, type)}`
  if (type === 'REST') return `Trạm nghỉ ${typeNumber(index, type)}`
  return `Điểm đón/trả ${typeNumber(index, type)}`
}
const stopDescription = index => {
  if (index === 0) return 'Xe xuất phát và đón khách tại đây'
  if (index === stops.value.length - 1) return 'Xe kết thúc hành trình và trả khách tại đây'
  const type = stops.value[index].stopType
  if (type === 'PICKUP') return 'Hành khách có thể lên xe tại đây'
  if (type === 'DROPOFF') return 'Hành khách có thể xuống xe tại đây'
  if (type === 'REST') return 'Xe dừng để khách nghỉ chân, không đón hoặc trả khách'
  return 'Hành khách có thể lên hoặc xuống xe tại đây'
}
const stopTypeLabel = type => ({ PICKUP: 'Điểm đón khách', DROPOFF: 'Điểm trả khách', REST: 'Trạm nghỉ chân', BOTH: 'Điểm đón và trả khách' }[type] || 'Điểm trên hành trình')
const stopTypeIcon = type => ({ PICKUP: 'person_pin_circle', DROPOFF: 'location_on', REST: 'local_cafe', BOTH: 'transfer_within_a_station' }[type] || 'pin_drop')
const stopTypeClass = type => ({ DROPOFF: 'locked-field--drop', REST: 'locked-field--rest', BOTH: 'locked-field--both' }[type] || '')
const stopBadgeClass = (stop, index) => index === 0 ? 'bg-emerald-600' : index === stops.value.length - 1 ? 'bg-rose-500' : stop.stopType === 'REST' ? 'bg-amber-500' : stop.stopType === 'DROPOFF' ? 'bg-rose-500' : 'bg-[#075955]'
const addStop = type => {
  const lastIndex = stops.value.length - 1
  let insertIndex = lastIndex
  if (type === 'PICKUP') {
    const nextGroup = stops.value.findIndex((stop, index) => index > 0 && !['PICKUP', 'BOTH'].includes(stop.stopType))
    insertIndex = nextGroup > 0 ? nextGroup : lastIndex
  } else if (type === 'REST') {
    const firstDropoff = stops.value.findIndex((stop, index) => index > 0 && stop.stopType === 'DROPOFF')
    insertIndex = firstDropoff > 0 ? firstDropoff : lastIndex
  }
  stops.value.splice(insertIndex, 0, makeStop({ stopType: type, offsetMinutes: insertIndex }))
  normalizeEndpoints()
}
const move = (index, direction) => {
  const target = index + direction
  if (!isMiddleStop(target)) return
  ;[stops.value[index], stops.value[target]] = [stops.value[target], stops.value[index]]
}
const removeStop = index => { if (isMiddleStop(index)) stops.value.splice(index, 1); normalizeEndpoints() }
const save = async () => {
  saving.value = true; error.value = ''; normalizeEndpoints()
  try {
    if (stops.value.some(stop => !stop.name?.trim())) throw new Error('Vui lòng nhập tên cho tất cả điểm dừng.')
    const payload = stops.value.map((stop, index) => ({ name: stop.name, address: stop.name, stopType: stop.stopType, offsetMinutes: index, stopOrder: index }))
    await api.saveRouteStops(route.value.id, payload)
    open.value = false
    emit('notify', { type: 'success', message: 'Đã lưu lộ trình đón và trả khách.' })
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message || e.message || 'Không lưu được điểm dừng.'
    emit('notify', { type: 'error', message: error.value })
  }
  finally { saving.value = false }
}
defineExpose({ show })
</script>

<style scoped>
.route-stop-overlay { overflow: hidden; }
.route-stop-modal {
  isolation: isolate;
  height: min(46rem, calc(100dvh - 1.5rem));
  max-height: calc(100dvh - 1.5rem);
  background-color: #f4f7f6 !important;
  opacity: 1;
}
.route-stop-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 17rem;
  background-color: #f4f7f6;
}
.route-stop-form {
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  background-color: #eef3f2;
}
.setup-guide {
  overscroll-behavior: contain;
  background-color: #ffffff;
}
@media (max-width: 63.99rem) {
  .route-stop-layout { display: block; overflow: hidden; }
  .route-stop-form { height: 100%; }
  .setup-guide { display: none; }
}
@media (max-height: 42rem) {
  .route-stop-header { padding-top: .8rem; padding-bottom: .8rem; }
  .route-stop-footer { padding-top: .65rem; padding-bottom: .65rem; }
}
.field-group > span { display: block; margin-bottom: .4rem; font-size: .65rem; font-weight: 800; letter-spacing: .06em; color: rgb(100 116 139); }
.field-group small { display: block; margin-top: .35rem; font-size: .68rem; font-weight: 500; color: rgb(148 163 184); }
.field { width: 100%; border: 1px solid rgb(226 232 240); border-radius: .75rem; background: rgb(248 250 252); padding: .72rem .8rem; font-size: .78rem; font-weight: 650; color: rgb(30 41 59); outline: none; transition: border-color .2s, box-shadow .2s, background .2s; }
.field:focus { border-color: #075955; background: white; box-shadow: 0 0 0 3px rgb(7 89 85 / .09); }
.field:disabled { cursor: not-allowed; color: rgb(100 116 139); }
.locked-field { display: flex; align-items: center; gap: .5rem; border-radius: .75rem; background: rgb(236 253 245); padding: .72rem .8rem; font-size: .78rem; font-weight: 800; color: rgb(4 120 87); }
.locked-field--drop { background: rgb(255 241 242); color: rgb(225 29 72); }
.locked-field--rest { background: rgb(255 251 235); color: rgb(180 83 9); }
.locked-field--both { background: rgb(239 246 255); color: rgb(29 78 216); }
.locked-field .material-symbols-outlined { font-size: 1rem; }
.add-stop-btn { display: flex; align-items: center; justify-content: center; gap: .65rem; border: 2px dashed rgb(203 213 225); border-radius: 1rem; background: white; padding: .9rem .75rem; text-align: left; color: rgb(51 65 85); transition: border-color .2s, background .2s, transform .2s; }
.add-stop-btn > .material-symbols-outlined { font-size: 1.35rem; }
.add-stop-btn > span:last-child { font-size: .75rem; font-weight: 900; }
.add-stop-btn small { display: block; margin-top: .15rem; font-size: .62rem; font-weight: 650; color: rgb(148 163 184); }
.add-stop-btn:hover { background: rgb(248 250 252); }
.add-stop-btn:active { transform: scale(.98); }
.add-stop-btn--pickup { border-color: rgb(110 231 183); color: rgb(4 120 87); }
.add-stop-btn--dropoff { border-color: rgb(253 164 175); color: rgb(190 18 60); }
.add-stop-btn--rest { border-color: rgb(252 211 77); color: rgb(180 83 9); }
.icon-btn { display: inline-flex; height: 2.25rem; width: 2.25rem; align-items: center; justify-content: center; border-radius: .65rem; background: rgb(248 250 252); color: rgb(71 85 105); transition: background .2s, transform .2s; }
.icon-btn:hover:not(:disabled) { background: rgb(226 232 240); }
.icon-btn:active:not(:disabled) { transform: scale(.94); }
.icon-btn:disabled { cursor: not-allowed; opacity: .3; }
</style>
