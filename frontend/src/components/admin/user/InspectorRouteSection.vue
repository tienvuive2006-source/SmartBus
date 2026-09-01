<template>
  <FormSection icon="route" title="Tuyến đường phụ trách" description="Chọn tuyến chính hoặc tuyến dự phòng cho lơ xe.">
    <div v-if="loading" class="route-state">Đang tải danh sách tuyến...</div>
    <div v-else-if="!routes.length" class="route-state">Chưa có tuyến đường nào.</div>
    <div v-else class="route-list">
      <article v-for="route in routes" :key="route.id" class="route-item">
        <span class="material-symbols-outlined route-icon">route</span>
        <div class="min-w-0 flex-1">
          <strong class="block truncate text-xs text-slate-800">{{ route.name || routeLabel(route) }}</strong>
          <small class="mt-1 block truncate text-[10px] font-semibold text-slate-400">{{ routeLabel(route) }}</small>
        </div>
        <select class="route-role" :value="roleOf(route.id)" @change="setRole(route.id, $event.target.value)">
          <option value="">Không gán</option>
          <option value="PRIMARY">Tuyến chính</option>
          <option value="BACKUP">Dự phòng</option>
        </select>
      </article>
    </div>
    <p class="mt-3 flex items-start gap-2 text-[10px] font-semibold leading-4 text-slate-400">
      <span class="material-symbols-outlined mt-px text-[15px] text-teal-600">info</span>
      Tuyến chiều ngược lại được gán tự động khi lưu, giống quy tắc phân tuyến tài xế.
    </p>
  </FormSection>
</template>

<script setup>
import FormSection from './FormSection.vue'

const props = defineProps({
  form: { type: Object, required: true },
  routes: { type: Array, default: () => [] },
  loading: Boolean
})

const shortPoint = value => String(value || '').split(',')[0].trim()
const routeLabel = route => `${shortPoint(route.departurePoint)} → ${shortPoint(route.arrivalPoint)}`
const roleOf = routeId => {
  if ((props.form.primaryRouteIds || []).includes(routeId)) return 'PRIMARY'
  if ((props.form.backupRouteIds || []).includes(routeId)) return 'BACKUP'
  return ''
}
const setRole = (routeId, role) => {
  props.form.primaryRouteIds = (props.form.primaryRouteIds || []).filter(id => id !== routeId)
  props.form.backupRouteIds = (props.form.backupRouteIds || []).filter(id => id !== routeId)
  if (role === 'PRIMARY') props.form.primaryRouteIds.push(routeId)
  if (role === 'BACKUP') props.form.backupRouteIds.push(routeId)
}
</script>

<style scoped>
.route-list { display: grid; gap: .55rem; max-height: 17rem; overflow-y: auto; padding-right: .2rem; }
.route-item { display: flex; align-items: center; gap: .7rem; border: 1px solid #e1e8e9; border-radius: .8rem; padding: .65rem .7rem; background: #f8faf9; transition: .18s ease; }
.route-item:hover { border-color: #9dcfc5; background: #fff; }
.route-icon { display: grid; width: 2rem; height: 2rem; flex: none; place-items: center; border-radius: .6rem; color: #087769; background: #e8f7f3; font-size: 1rem; }
.route-role { min-width: 7.4rem; border: 1px solid #d4dfe0; border-radius: .6rem; padding: .5rem .55rem; color: #40545c; background: #fff; font-size: .68rem; font-weight: 800; outline: none; }
.route-role:focus { border-color: #168a7a; box-shadow: 0 0 0 3px rgb(13 148 136 / .08); }
.route-state { display: grid; min-height: 5rem; place-items: center; border: 1px dashed #cddadb; border-radius: .8rem; color: #829398; background: #f8faf9; font-size: .72rem; font-weight: 700; }
@media (max-width: 520px) { .route-item { align-items: flex-start; flex-wrap: wrap; }.route-role { width: 100%; margin-left: 2.7rem; } }
</style>
