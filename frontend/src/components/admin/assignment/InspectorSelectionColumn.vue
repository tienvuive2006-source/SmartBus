<template>
  <section class="resource-column">
    <header class="resource-header">
      <div class="column-title">
        <span class="step-badge">3</span>
        <h4>Chọn lơ xe / Nhân viên</h4>
      </div>

      <!-- Search & Filter Bar -->
      <div class="search-filter-row">
        <div class="search-input-wrap">
          <span class="material-symbols-outlined search-icon">search</span>
          <input
            :value="search"
            placeholder="Tìm tên, SĐT lơ xe..."
            @input="$emit('update:search', $event.target.value)"
          />
        </div>
        <button type="button" class="filter-btn" title="Lọc lơ xe">
          <span class="material-symbols-outlined">tune</span>
        </button>
      </div>

      <label v-if="allowOutside" class="route-toggle">
        <input :checked="showOutside" type="checkbox" @change="$emit('update:showOutside', $event.target.checked)" />
        Hiện nhân sự ngoài tuyến
      </label>
    </header>

    <div class="resource-list">
      <!-- Clear Choice Button -->
      <button
        type="button"
        class="empty-choice"
        :class="{ selected: !selected }"
        @click="$emit('clear')"
      >
        <span class="material-symbols-outlined icon-off">person_off</span>
        <div class="choice-text">
          <strong>Không bố trí lơ xe</strong>
          <small>Chuyến xe này không cần thêm nhân viên phục vụ</small>
        </div>
        <span v-if="!selected" class="material-symbols-outlined check-icon">check_circle</span>
      </button>

      <p v-if="loading" class="empty-message">Đang tải danh sách lơ xe...</p>
      <p v-else-if="!inspectors.length" class="empty-message">Không tìm thấy lơ xe phù hợp.</p>

      <!-- Inspector Card -->
      <div
        v-for="inspector in inspectors"
        :key="inspector.id"
        class="resource-card"
        :class="{ selected: selected?.id === inspector.id, disabled: inspector.conflict }"
        @click="!inspector.conflict && $emit('select', inspector)"
      >
        <!-- Top Row: Checkbox, Avatar, Name, Phone & Status Rating -->
        <div class="card-header-row">
          <div class="card-checkbox" :class="{ checked: selected?.id === inspector.id }">
            <span v-if="selected?.id === inspector.id" class="material-symbols-outlined">check</span>
          </div>

          <div class="avatar-box">
            <img v-if="inspector.avatarUrl" :src="inspector.avatarUrl" :alt="inspector.fullName" />
            <span v-else>{{ inspector.fullName?.charAt(0) }}</span>
          </div>

          <div class="inspector-info">
            <div class="name-row">
              <strong class="inspector-name">{{ inspector.fullName }}</strong>
            </div>
            <div class="sub-info">
              <span class="inspector-phone">{{ inspector.phone || 'Chưa cập nhật SĐT' }}</span>
              <span class="role-tag" :class="`route-role-${String(inspector.routeRole || 'UNRESTRICTED').toLowerCase()}`">• {{ routeRoleLabel(inspector) }}</span>
            </div>
          </div>

          <div class="status-rating-col">
            <span class="status-chip" :class="inspector.conflict ? 'busy' : (inspector.needsRelocation ? 'relocate' : 'free')" :title="inspector.needsRelocation ? `Phải chạy rỗng từ ${inspector.lastKnownLocation || 'nơi khác'} đến bến xuất phát` : ''">
              {{ inspector.conflict ? (inspector.conflictReason || 'ĐANG CHẠY') : (inspector.needsRelocation ? 'CHẠY RỖNG' : 'RẢNH') }}
            </span>
            <div v-if="inspector.rating != null" class="rating-box">
              <span class="star-icon">★</span>
              <span class="rating-val">{{ inspector.rating }}</span>
            </div>
          </div>
        </div>

        <!-- Middle Grid: 2 Operational Metrics -->
        <div class="metrics-grid">
          <div class="metric-item">
            <span class="metric-label">Chuyến trong tháng</span>
            <strong class="metric-val">{{ countValue(inspector.tripsThisMonth) }}</strong>
          </div>
          <div class="metric-item">
            <span class="metric-label">Vị trí hiện tại</span>
            <strong class="metric-val">{{ inspector.lastKnownLocation || 'Chưa xác định' }}</strong>
          </div>
        </div>

        <!-- Bottom Pill Tag: Eligibility -->
        <div class="eligibility-row">
          <span
            class="eligibility-pill"
            :class="inspector.conflict ? 'not-eligible' : 'eligible'"
          >
            <span class="material-symbols-outlined pill-icon">
              {{ inspector.conflict ? 'cancel' : 'check_circle' }}
            </span>
            <span>{{ inspector.conflict ? 'BẬN CHUYẾN' : 'ĐỦ ĐIỀU KIỆN' }}</span>
          </span>

          <span v-if="inspector.needsRelocation && !inspector.conflict" class="relocate-pill" :title="`Phải di chuyển từ ${inspector.lastKnownLocation || 'Nơi khác'}`">
            <span class="material-symbols-outlined">directions_car</span>
            <span class="relocate-text">Từ {{ inspector.lastKnownLocation || 'Nơi khác' }}</span>
          </span>
        </div>
      </div>

      <!-- List Footer -->
      <div v-if="inspectors.length" class="list-footer-bar">
        <span>Hiển thị {{ inspectors.length }} lơ xe</span>
        <button type="button" class="btn-see-all">Xem tất cả &gt;</button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  inspectors: { type: Array, default: () => [] },
  selected: { type: Object, default: null },
  search: String,
  loading: Boolean,
  allowOutside: Boolean,
  showOutside: Boolean
})
defineEmits(['select', 'clear', 'update:search', 'update:showOutside'])

const countValue = value => value === null || value === undefined ? 'Chưa cập nhật' : `${value} chuyến`
const yearValue = value => value === null || value === undefined ? 'Chưa cập nhật' : `${value} năm`
const routeRoleLabel = inspector => {
  if (inspector.routeRole === 'PRIMARY') return 'Tuyến chính'
  if (inspector.routeRole === 'BACKUP') return 'Dự phòng'
  if (inspector.routeRole === 'OUTSIDE') return 'Ngoài tuyến'
  return 'Dùng mọi tuyến'
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

.empty-choice {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.6rem;
  border-radius: 0.6rem;
  border: 1px dashed #cbd5e1;
  background: #ffffff;
  cursor: pointer;
  margin-bottom: 0.6rem;
}

.empty-choice.selected {
  border-color: #0f766e;
  background: #f0fdf4;
}

.choice-text {
  text-align: left;
}

.choice-text strong {
  display: block;
  font-size: 0.72rem;
  color: #0f172a;
}

.choice-text small {
  font-size: 0.6rem;
  color: #64748b;
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

.avatar-box {
  width: 2.35rem;
  height: 2.35rem;
  border-radius: 9999px;
  overflow: hidden;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  font-weight: 800;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.avatar-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.inspector-info {
  flex: 1;
  min-width: 0;
}

.inspector-name {
  color: #0f172a;
  font-size: 0.8rem;
  font-weight: 850;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.1rem;
}

.inspector-phone {
  color: #64748b;
  font-size: 0.65rem;
}

.role-tag {
  color: #64748b;
  font-size: 0.65rem;
}
.role-tag.route-role-primary { color:#15803d; font-weight:750; }
.role-tag.route-role-backup { color:#b45309; font-weight:750; }
.role-tag.route-role-outside { color:#7c3aed; font-weight:750; }

.status-rating-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.2rem;
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

.status-chip.busy {
  background: #fee2e2;
  color: #b91c1c;
}

.status-chip.relocate {
  background: #e0e7ff;
  color: #4338ca;
}

.rating-box {
  display: flex;
  align-items: center;
  gap: 0.15rem;
}

.star-icon {
  color: #eab308;
  font-size: 0.75rem;
}

.rating-val {
  color: #334155;
  font-size: 0.68rem;
  font-weight: 800;
}

/* Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.4rem;
}

.eligibility-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.2rem 0.45rem;
  border-radius: 0.35rem;
  font-size: 0.6rem;
  font-weight: 850;
  flex-shrink: 0;
}

.eligibility-pill.eligible {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.eligibility-pill.not-eligible {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.pill-icon {
  font-size: 0.75rem;
}

.relocate-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  padding: 0.2rem 0.4rem;
  border-radius: 0.35rem;
  font-size: 0.58rem;
  font-weight: 800;
  background: #e0e7ff;
  color: #4338ca;
  max-width: 50%;
}

.relocate-pill .material-symbols-outlined {
  font-size: 0.75rem;
  flex-shrink: 0;
}

.relocate-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  font-weight: 850;
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
