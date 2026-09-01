<template>
  <aside class="maintenance-vehicle-summary" aria-live="polite">
    <header><span class="material-symbols-outlined">history</span><div><strong>Hồ sơ bảo trì</strong><small>Lịch sử của xe được chọn</small></div></header>

    <div v-if="!bus" class="maintenance-vehicle-summary__empty">
      <span class="material-symbols-outlined">directions_bus</span>
      <strong>Chưa chọn xe</strong>
      <p>Chọn biển số để xem số lần đã bảo dưỡng và lịch sử gần nhất.</p>
    </div>

    <template v-else>
      <div class="maintenance-vehicle-summary__identity">
        <span class="material-symbols-outlined">airport_shuttle</span>
        <div><strong>{{ bus.licensePlate }}</strong><small>{{ bus.busType }}</small></div>
      </div>

      <div class="maintenance-vehicle-summary__stats">
        <article><strong>{{ completedRecords.length }}</strong><small>Lần đã bảo dưỡng</small></article>
        <article><strong>{{ formatNumber(bus.currentMileage) }}</strong><small>Km hiện tại</small></article>
        <article><strong>{{ lastCompleted ? formatDate(lastCompleted.completedAt) : 'Chưa có' }}</strong><small>Lần gần nhất</small></article>
        <article><strong>{{ formatMoney(totalCost) }}</strong><small>Tổng chi phí</small></article>
      </div>

      <div class="maintenance-vehicle-summary__history">
        <div class="maintenance-vehicle-summary__title"><strong>Lịch sử gần đây</strong><small>{{ completedRecords.length }} phiếu hoàn thành</small></div>
        <div v-if="!recentRecords.length" class="maintenance-vehicle-summary__none">Xe chưa có lần bảo dưỡng hoàn thành.</div>
        <article v-for="item in recentRecords" :key="item.id">
          <span class="material-symbols-outlined">build</span>
          <div><strong>{{ typeLabel(item.maintenanceType) }}</strong><small>{{ formatFullDate(item.completedAt) }}</small></div>
          <b>{{ formatMoney(item.actualCost) }}</b>
        </article>
      </div>
    </template>
  </aside>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ bus: Object, records: { type: Array, default: () => [] } })
const completedRecords = computed(() => props.records
  .filter(item => item.status === 'COMPLETED' && Number(item.bus?.id) === Number(props.bus?.id))
  .sort((a, b) => new Date(b.completedAt || 0) - new Date(a.completedAt || 0)))
const recentRecords = computed(() => completedRecords.value.slice(0, 3))
const lastCompleted = computed(() => completedRecords.value[0] || null)
const totalCost = computed(() => completedRecords.value.reduce((sum, item) => sum + Number(item.actualCost || 0), 0))
const typeLabel = type => ({ PERIODIC: 'Bảo trì định kỳ', OIL_CHANGE: 'Thay dầu', TIRE: 'Lốp và phanh', REPAIR: 'Sửa chữa', INSPECTION: 'Đăng kiểm' }[type] || type)
const formatNumber = value => Number(value || 0).toLocaleString('vi-VN', { maximumFractionDigits: 1 })
const formatMoney = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const formatDate = value => value ? new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(value)) : 'Chưa có'
const formatFullDate = value => value ? new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value)) : 'Chưa cập nhật'
</script>
