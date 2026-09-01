<template>
  <section class="maintenance-table-card">
    <header><div><span class="material-symbols-outlined">fact_check</span><strong>Phiếu bảo trì</strong></div><span>{{ records.length }} phiếu</span></header>
    <div v-if="loading" class="maintenance-loading">Đang tải dữ liệu bảo trì…</div>
    <div v-else-if="!records.length" class="maintenance-empty"><span class="material-symbols-outlined">car_repair</span><strong>Chưa có phiếu bảo trì</strong><p>Tạo phiếu đầu tiên để theo dõi lịch sử kỹ thuật của đội xe.</p></div>
    <div v-else class="maintenance-table-scroll"><table><thead><tr><th>Xe</th><th>Nội dung</th><th>Lịch dự kiến</th><th>Km hiện tại</th><th>Chi phí</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
      <tr v-for="item in records" :key="item.id"><td><strong class="maintenance-plate">{{ item.bus?.licensePlate }}</strong><small>{{ item.bus?.busType }}</small></td><td><strong>{{ typeLabel(item.maintenanceType) }}</strong><small>{{ item.garageName || 'Chưa chọn gara' }}</small></td><td><strong>{{ formatDate(item.scheduledStart) }}</strong><small>{{ item.expectedEnd ? `Đến ${formatDate(item.expectedEnd)}` : 'Chưa có giờ kết thúc' }}</small></td><td><strong>{{ formatNumber(item.bus?.currentMileage) }} km</strong><small>Chu kỳ {{ formatNumber(item.bus?.maintenanceIntervalKm) }} km</small></td><td><strong>{{ formatMoney(item.actualCost ?? item.estimatedCost) }}</strong><small>{{ item.actualCost != null ? 'Thực tế' : 'Dự kiến' }}</small></td><td><span class="maintenance-status" :class="item.status.toLowerCase()">{{ statusLabel(item.status) }}</span></td><td><div class="maintenance-actions"><button v-if="item.status === 'SCHEDULED'" type="button" @click="$emit('start', item)">Bắt đầu</button><button v-if="item.status === 'IN_PROGRESS'" class="complete" type="button" @click="$emit('complete', item)">Hoàn thành</button><button v-if="['SCHEDULED','IN_PROGRESS'].includes(item.status)" class="cancel" type="button" @click="$emit('cancel', item)">Hủy</button></div></td></tr>
    </tbody></table></div>
  </section>
</template>
<script setup>
defineProps({ records: { type: Array, default: () => [] }, loading: Boolean })
defineEmits(['start', 'complete', 'cancel'])
const formatNumber = value => Number(value || 0).toLocaleString('vi-VN', { maximumFractionDigits: 1 })
const formatMoney = value => value == null ? '—' : `${Number(value).toLocaleString('vi-VN')}đ`
const formatDate = value => value ? new Date(value).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' }) : '—'
const typeLabel = type => ({ PERIODIC:'Bảo trì định kỳ', OIL_CHANGE:'Thay dầu', TIRE:'Lốp và phanh', REPAIR:'Sửa chữa', INSPECTION:'Đăng kiểm' }[type] || type)
const statusLabel = status => ({ SCHEDULED:'Đã lên lịch', IN_PROGRESS:'Đang bảo trì', COMPLETED:'Hoàn thành', CANCELLED:'Đã hủy' }[status] || status)
</script>
