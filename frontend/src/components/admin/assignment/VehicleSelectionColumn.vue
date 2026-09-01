<template>
  <section class="resource-column">
    <header class="resource-header">
      <div class="column-title">
        <span class="step-badge">2</span>
        <h4>Chọn phương tiện</h4>
      </div>

      <!-- Search & Filter Bar -->
      <div class="search-filter-row">
        <div class="search-input-wrap">
          <span class="material-symbols-outlined search-icon">search</span>
          <input
            :value="search"
            placeholder="Tìm biển số, dòng xe..."
            @input="$emit('update:search', $event.target.value)"
          />
        </div>
        <button type="button" class="filter-btn" title="Lọc phương tiện">
          <span class="material-symbols-outlined">tune</span>
        </button>
      </div>

      <label v-if="allowOutside" class="route-toggle">
        <input :checked="showOutside" type="checkbox" @change="$emit('update:showOutside', $event.target.checked)" />
        Hiện xe ngoài tuyến
      </label>
    </header>

    <div class="resource-list">
      <p v-if="loading" class="empty-message">Đang tải nhóm xe của tuyến...</p>
      <p v-else-if="!buses.length" class="empty-message">Không có xe phù hợp dòng xe chuyến này.</p>

      <!-- Vehicle Card -->
      <div
        v-for="bus in buses"
        :key="bus.id"
        class="resource-card"
        :class="{ selected: selected?.id === bus.id, disabled: bus.conflict }"
        @click="!bus.conflict && $emit('select', bus)"
      >
        <!-- Top Row: Checkbox, Bus Icon, License Plate & Status Badge -->
        <div class="card-header-row">
          <div class="card-checkbox" :class="{ checked: selected?.id === bus.id }">
            <span v-if="selected?.id === bus.id" class="material-symbols-outlined">check</span>
          </div>

          <div class="bus-icon-box">
            <span class="material-symbols-outlined">directions_bus</span>
          </div>

          <div class="bus-main-info">
            <div class="plate-row">
              <strong class="plate-number">{{ bus.licensePlate }}</strong>
            </div>
            <span class="bus-specs">
              {{ bus.busType || 'Chưa cập nhật dòng xe' }}<template v-if="bus.capacity != null"> • {{ bus.capacity }} ghế</template><template v-if="bus.seatType"> • {{ bus.seatType }}</template>
            </span>
          </div>

          <span class="status-chip" :class="bus.conflict ? 'maintenance' : 'free'">
            {{ bus.conflict ? (bus.conflictReason || 'BẢO DƯỠNG') : 'RẢNH' }}
          </span>
        </div>

        <!-- Middle Grid: Current Station, Maintenance, Registration Expiry -->
        <div class="metrics-grid">
          <div class="metric-item">
            <span class="metric-label">Vị trí hiện tại</span>
            <strong class="metric-val">{{ bus.assignmentLocation || 'Chưa xác định' }}</strong>
          </div>
          <div class="metric-item">
            <span class="metric-label">Bảo dưỡng</span>
            <strong class="metric-val" :class="bus.conflict ? 'text-red-600' : 'text-emerald-600'">
              {{ maintenanceLabel(bus) }}
            </strong>
          </div>
          <div class="metric-item">
            <span class="metric-label">Đăng kiểm</span>
            <strong class="metric-val" :class="inspectionTone(bus)">{{ inspectionLabel(bus) }}</strong>
          </div>
        </div>

        <!-- Bottom Pill Tag: Route Suitability -->
        <div class="eligibility-row">
          <span
            class="eligibility-pill"
            :class="bus.conflict ? 'not-fit' : 'fit'"
          >
            <span class="material-symbols-outlined pill-icon">
              {{ bus.conflict ? 'cancel' : 'check_circle' }}
            </span>
            <span>{{ bus.conflict ? (bus.conflictReason || 'KHÔNG KHẢ DỤNG') : 'ĐÚNG DÒNG CỦA CHUYẾN' }}</span>
          </span>
        </div>
      </div>

      <!-- List Footer -->
      <div v-if="buses.length" class="list-footer-bar">
        <span>Hiển thị {{ buses.length }} / {{ routeCount || buses.length }} xe</span>
        <button type="button" class="btn-see-all">Xem tất cả &gt;</button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  buses: { type: Array, default: () => [] },
  selected: { type: Object, default: null },
  search: String,
  loading: Boolean,
  routeCount: Number,
  allowOutside: Boolean,
  showOutside: Boolean
})
defineEmits(['select', 'update:search', 'update:showOutside'])

const maintenanceLabel = bus => {
  const currentMileage = Number(bus.currentMileage)
  const lastMaintenanceMileage = Number(bus.lastMaintenanceMileage)
  if (!Number.isFinite(currentMileage) || currentMileage <= 0) return 'Chưa cập nhật km'

  const interval = Number(bus.maintenanceIntervalKm || 10000)
  const travelled = currentMileage - (Number.isFinite(lastMaintenanceMileage) ? lastMaintenanceMileage : 0)
  const remaining = interval - travelled
  return remaining >= 0
    ? `Còn ${remaining.toLocaleString('vi-VN')} km`
    : `Quá ${Math.abs(remaining).toLocaleString('vi-VN')} km`
}

const inspectionLabel = bus => {
  if (!bus.inspectionExpiryDate) return 'Chưa cập nhật'
  const [year, month, day] = bus.inspectionExpiryDate.split('-')
  return `${day}/${month}/${year}`
}

const inspectionTone = bus => {
  if (!bus.inspectionExpiryDate) return ''
  const expiry = new Date(`${bus.inspectionExpiryDate}T23:59:59`).getTime()
  return expiry < Date.now() ? 'text-red-600' : 'text-emerald-600'
}
</script>

<style scoped>
.resource-column {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  border-right: 1px solid #e2e8f0;
  background: #ffffff;
}

.resource-header {
  padding: 0.85rem;
  border-bottom: 1px solid #e2e8f0;
  background: #ffffff;
  flex: none;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.step-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.35rem;
  height: 1.35rem;
  border-radius: 0.4rem;
  background: #e6f4f1;
  color: #0f766e;
  font-size: 0.75rem;
  font-weight: 900;
}

.column-title h4 {
  color: #0f172a;
  font-size: 0.88rem;
  font-weight: 850;
}

/* Search & Filter Bar */
.search-filter-row {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 0.65rem;
}

.search-input-wrap {
  position: relative;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 0.6rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 1.1rem;
}

.search-input-wrap input {
  width: 100%;
  height: 2.15rem;
  padding-left: 2rem;
  padding-right: 0.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.55rem;
  background: #f8fafc;
  font-size: 0.72rem;
  color: #0f172a;
  outline: none;
  box-sizing: border-box;
}

.search-input-wrap input:focus {
  border-color: #0f766e;
  background: #ffffff;
}

.filter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.15rem;
  height: 2.15rem;
  border-radius: 0.55rem;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #475569;
  cursor: pointer;
}

.filter-btn:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.route-toggle {
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.65rem;
  color: #64748b;
  cursor: pointer;
}

/* Resource List */
.resource-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0.65rem;
  background: #f8fafc;
}

.resource-card {
  position: relative;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  padding: 0.65rem;
  margin-bottom: 0.6rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.resource-card:hover:not(.disabled) {
  border-color: #0f766e;
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.08);
}

.resource-card.selected {
  border: 2px solid #0f766e;
  background: #f0fdf4;
}

.resource-card.disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Card Header Row */
.card-header-row {
  display: flex;
  align-items: center;
  gap: 0.55rem;
}

.card-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.15rem;
  height: 1.15rem;
  border-radius: 0.3rem;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  flex-shrink: 0;
}

.card-checkbox.checked {
  background: #0f766e;
  border-color: #0f766e;
  color: #ffffff;
}

.card-checkbox span {
  font-size: 0.85rem;
}

.bus-icon-box {
  width: 2.35rem;
  height: 2.35rem;
  border-radius: 0.55rem;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  flex-shrink: 0;
}

.bus-icon-box span {
  font-size: 1.25rem;
}

.bus-main-info {
  flex: 1;
  min-width: 0;
}

.plate-number {
  color: #0f172a;
  font-size: 0.82rem;
  font-weight: 900;
  letter-spacing: -0.01em;
}

.bus-specs {
  display: block;
  color: #64748b;
  font-size: 0.64rem;
  margin-top: 0.1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-chip {
  padding: 0.15rem 0.4rem;
  border-radius: 0.35rem;
  font-size: 0.58rem;
  font-weight: 850;
  white-space: nowrap;
}

.status-chip.free {
  background: #dcfce7;
  color: #15803d;
}

.status-chip.maintenance {
  background: #fef3c7;
  color: #b45309;
}

/* Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0.3rem;
  margin-top: 0.55rem;
  padding: 0.45rem;
  background: #f8fafc;
  border-radius: 0.45rem;
}

.metric-item {
  display: flex;
  flex-direction: column;
}

.metric-label {
  color: #94a3b8;
  font-size: 0.55rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.metric-val {
  color: #1e293b;
  font-size: 0.68rem;
  font-weight: 800;
  margin-top: 0.1rem;
}

/* Eligibility Row */
.eligibility-row {
  margin-top: 0.45rem;
}

.eligibility-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.2rem 0.45rem;
  border-radius: 0.35rem;
  font-size: 0.6rem;
  font-weight: 850;
}

.eligibility-pill.fit {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.eligibility-pill.not-fit {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.pill-icon {
  font-size: 0.75rem;
}

/* List Footer Bar */
.list-footer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.4rem 0.2rem;
  color: #64748b;
  font-size: 0.65rem;
  font-weight: 700;
}

.btn-see-all {
  border: none;
  background: transparent;
  color: #0f766e;
  font-size: 0.65rem;
  font-weight: 800;
  cursor: pointer;
}

.btn-see-all:hover {
  text-decoration: underline;
}

.empty-message {
  padding: 2rem;
  text-align: center;
  color: #94a3b8;
  font-size: 0.72rem;
}
</style>
