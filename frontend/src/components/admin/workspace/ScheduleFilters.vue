<template>
  <section class="schedule-filters" aria-label="Bộ lọc lịch phân công">
    <label class="filter-search">
      <span class="material-symbols-outlined">search</span>
      <input :value="search" type="search" placeholder="Tuyến, biển số, tài xế, lơ xe..." @input="$emit('update:search', $event.target.value)" />
    </label>

    <label class="filter-select">
      <span class="material-symbols-outlined">task_alt</span>
      <select :value="status" @change="$emit('update:status', $event.target.value)">
        <option value="">Mọi trạng thái</option>
        <option value="UNASSIGNED">Chưa phân công</option>
        <option value="ASSIGNED">Đã phân công</option>
        <option value="IN_PROGRESS">Đang chạy</option>
        <option value="COMPLETED">Đã hoàn thành</option>
        <option value="CANCELLED">Đã hủy</option>
      </select>
    </label>

    <label class="filter-select">
      <span class="material-symbols-outlined">route</span>
      <select :value="route" @change="$emit('update:route', $event.target.value)">
        <option value="">Mọi tuyến đường</option>
        <option v-for="item in routeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
    </label>

    <label class="filter-select">
      <span class="material-symbols-outlined">directions_bus</span>
      <select :value="vehicle" @change="$emit('update:vehicle', $event.target.value)">
        <option value="">Mọi phương tiện</option>
        <option value="UNASSIGNED">Chưa gán xe</option>
        <option v-for="plate in vehicleOptions" :key="plate" :value="plate">{{ plate }}</option>
      </select>
    </label>

    <label class="filter-select filter-select--time">
      <span class="material-symbols-outlined">schedule</span>
      <select :value="timePeriod" @change="$emit('update:timePeriod', $event.target.value)">
        <option value="">Cả ngày</option>
        <option value="MORNING">Buổi sáng · trước 12h</option>
        <option value="AFTERNOON">Buổi chiều · 12h–18h</option>
        <option value="EVENING">Buổi tối · từ 18h</option>
      </select>
    </label>

    <button v-if="hasActiveFilters" type="button" class="filter-reset" title="Xóa toàn bộ bộ lọc" @click="$emit('reset')">
      <span class="material-symbols-outlined">filter_alt_off</span>
      Đặt lại
    </button>

    <div class="filter-result">
      <strong>{{ resultCount }}</strong>
      <span>chuyến trong tuần</span>
    </div>
  </section>
</template>

<script setup>
defineProps({
  search: { type: String, default: '' },
  status: { type: String, default: '' },
  route: { type: String, default: '' },
  vehicle: { type: String, default: '' },
  timePeriod: { type: String, default: '' },
  routeOptions: { type: Array, default: () => [] },
  vehicleOptions: { type: Array, default: () => [] },
  resultCount: { type: Number, default: 0 },
  hasActiveFilters: Boolean
})

defineEmits(['update:search', 'update:status', 'update:route', 'update:vehicle', 'update:timePeriod', 'reset'])
</script>

<style scoped>
.schedule-filters { display:flex; min-height:3rem; flex:none; align-items:center; gap:.35rem; overflow-x:auto; border-bottom:1px solid #e2e8f0; background:#f8faf9; padding:.35rem .65rem; scrollbar-width:thin; }
.filter-search,.filter-select { position:relative; display:flex; min-width:0; flex:none; align-items:center; }
.filter-search { width:min(12.5rem,19vw); }
.filter-select { width:8.25rem; }
.filter-select--time { width:9rem; }
.filter-search>.material-symbols-outlined,.filter-select>.material-symbols-outlined { position:absolute; left:.55rem; z-index:1; color:#78908d; font-size:.88rem; pointer-events:none; }
.filter-search input,.filter-select select { width:100%; height:1.95rem; border:1px solid #d9e3e3; border-radius:.55rem; outline:none; color:#40535b; background:#fff; font-size:.59rem; font-weight:750; transition:.18s ease; }
.filter-search input { padding:0 .55rem 0 1.75rem; }
.filter-select select { padding:0 1.45rem 0 1.75rem; cursor:pointer; }
.filter-search input:focus,.filter-select select:focus { border-color:#348f82; box-shadow:0 0 0 3px rgb(8 124 109 / .08); }
.filter-result { position:sticky; right:0; z-index:3; display:grid; min-width:7.25rem; height:2.3rem; flex:none; align-content:center; margin-left:auto; border-left:1px solid #dbe7e5; padding:0 .65rem; text-align:right; white-space:nowrap; background:#f8faf9; box-shadow:-8px 0 12px #f8faf9; }
.filter-result strong { color:#075955; font-size:.72rem; font-weight:950; font-variant-numeric:tabular-nums; }
.filter-result span { color:#84938f; font-size:.5rem; font-weight:700; }
.filter-reset { display:flex; height:1.95rem; flex:none; align-items:center; gap:.25rem; border:1px solid #c7dcd8; border-radius:.55rem; padding:0 .6rem; color:#087467; background:#fff; font-size:.56rem; font-weight:850; transition:.18s ease; }
.filter-reset:hover { border-color:#78b9ae; background:#edf8f5; }.filter-reset:active { transform:scale(.98); }.filter-reset span { font-size:.9rem; }
@media(max-width:900px){.filter-search{width:11rem}.filter-result{margin-left:0}}
</style>
