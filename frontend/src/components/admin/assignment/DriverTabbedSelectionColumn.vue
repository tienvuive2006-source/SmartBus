<template>
  <section class="driver-column">
    <header class="driver-header">
      <div class="column-title">
        <span class="step-badge">1</span>
        <h4>Chọn tài xế</h4>
      </div>

      <!-- Role Tabs -->
      <nav class="role-tabs" aria-label="Vai trò tài xế">
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeRole === 'PRIMARY' }"
          @click="$emit('update:activeRole', 'PRIMARY')"
        >
          <span class="tab-title">Tài xế chính</span>
        </button>

        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeRole === 'SECONDARY' }"
          @click="$emit('update:activeRole', 'SECONDARY')"
        >
          <span class="tab-title">
            Tài xế phụ
            <small :class="secondaryRequired ? 'required-label' : 'text-slate-400 font-normal'">
              {{ secondaryRequired ? '(bắt buộc)' : '(tùy chọn)' }}
            </small>
          </span>
        </button>
      </nav>

      <!-- Search & Filter Button Bar -->
      <div class="search-filter-row">
        <div class="search-input-wrap">
          <span class="material-symbols-outlined search-icon">search</span>
          <input
            :value="currentSearch"
            placeholder="Tìm tên, SĐT tài xế..."
            @input="updateSearch($event.target.value)"
          />
        </div>
        <button type="button" class="filter-btn" title="Lọc nâng cao">
          <span class="material-symbols-outlined">tune</span>
        </button>
      </div>

      <!-- Filter Chips -->
      <div class="filter-chips-row">
        <button
          type="button"
          class="chip-btn"
          :class="{ active: activeChip === 'AVAILABLE' }"
          @click="toggleChip('AVAILABLE')"
        >
          <span v-if="activeChip === 'AVAILABLE'" class="material-symbols-outlined chip-check">check</span>
          Rảnh
        </button>
        <button
          type="button"
          class="chip-btn"
          :class="{ active: activeChip === 'ROUTE' }"
          @click="toggleChip('ROUTE')"
        >
          Đúng tuyến
        </button>
        <button
          type="button"
          class="chip-btn"
          :class="{ active: activeChip === 'REST' }"
          @click="toggleChip('REST')"
        >
          Đủ giờ nghỉ
        </button>
        <button
          type="button"
          class="chip-btn"
          :class="{ active: activeChip === 'OUTSIDE' }"
          @click="toggleChip('OUTSIDE')"
        >
          Ngoài tuyến
        </button>
      </div>
    </header>

    <!-- Driver Cards List -->
    <div class="driver-list">
      <div v-if="activeRole === 'SECONDARY' && secondaryRequired" class="required-notice">
        <span class="material-symbols-outlined">warning</span>
        <span>Chuyến từ 6 giờ trở lên phải bố trí tài xế phụ.</span>
      </div>

      <!-- Clear Option for Secondary Driver -->
      <button
        v-if="activeRole === 'SECONDARY' && !secondaryRequired"
        type="button"
        class="empty-choice"
        :class="{ selected: !secondarySelected }"
        @click="$emit('clear-secondary')"
      >
        <span class="material-symbols-outlined icon-off">person_off</span>
        <div class="choice-text">
          <strong>Không chọn tài xế phụ</strong>
          <small>Chuyến xe này không cần thêm tài xế phụ</small>
        </div>
        <span v-if="!secondarySelected" class="material-symbols-outlined check-icon">check_circle</span>
      </button>

      <p v-if="!filteredDriverList.length" class="empty-message">Không tìm thấy tài xế phù hợp.</p>

      <!-- Driver Card -->
      <div
        v-for="driver in filteredDriverList"
        :key="driver.id"
        class="driver-card"
        :class="{
          selected: currentSelected?.id === driver.id,
          disabled: driver.conflict
        }"
        @click="!driver.conflict && selectDriver(driver)"
      >
        <!-- Top Row: Checkbox, Avatar, Name, Phone & Status Rating -->
        <div class="card-header-row">
          <div class="card-checkbox" :class="{ checked: currentSelected?.id === driver.id }">
            <span v-if="currentSelected?.id === driver.id" class="material-symbols-outlined">check</span>
          </div>

          <div class="avatar-box">
            <img v-if="driver.avatarUrl" :src="driver.avatarUrl" :alt="driver.fullName" />
            <span v-else>{{ driver.fullName?.charAt(0) }}</span>
          </div>

          <div class="driver-info">
            <div class="name-row">
              <strong class="driver-name">{{ driver.fullName }}</strong>
            </div>
            <div class="sub-info">
              <span class="driver-phone">{{ driver.phone || 'Chưa cập nhật SĐT' }}</span>
              <span class="role-tag" :class="`route-role-${String(driver.routeRole || 'UNRESTRICTED').toLowerCase()}`">
                • {{ routeRoleLabel(driver) }}
              </span>
            </div>
            <div class="license-info" :class="licenseTone(driver)">
              <span class="material-symbols-outlined">id_card</span>
              <strong>{{ driver.driverLicenseClass ? `GPLX hạng ${driver.driverLicenseClass}` : 'Chưa cập nhật GPLX' }}</strong>
              <template v-if="driver.driverLicenseExpiryDate">
                <span>• HSD {{ formatLicenseDate(driver.driverLicenseExpiryDate) }}</span>
              </template>
            </div>
          </div>

          <div class="status-rating-col">
            <span class="status-chip" :class="driver.conflict ? 'busy' : (driver.needsRelocation ? 'relocate' : 'free')" :title="driver.needsRelocation ? `Phải chạy rỗng từ ${driver.lastKnownLocation || 'nơi khác'} đến bến xuất phát` : ''">
              {{ driver.conflict ? (driver.conflictReason || 'ĐANG CHẠY') : (driver.needsRelocation ? 'CHẠY RỖNG' : 'RẢNH') }}
            </span>
            <div v-if="driver.rating != null" class="rating-box">
              <span class="star-icon">★</span>
              <span class="rating-val">{{ driver.rating }}</span>
            </div>
          </div>
        </div>

        <!-- Middle Grid: Metrics -->
        <div class="metrics-grid">
          <div class="metric-item">
            <span class="metric-label">Vị trí hiện tại</span>
            <strong class="metric-val">{{ driver.lastKnownLocation || 'Chưa xác định' }}</strong>
          </div>
          <div class="metric-item">
            <span class="metric-label">Thời điểm ghi nhận</span>
            <strong class="metric-val">{{ driver.lastKnownAt || 'Chưa có dữ liệu' }}</strong>
          </div>
        </div>

        <!-- Bottom Pill Tag: Eligibility -->
        <div class="eligibility-row">
          <span
            class="eligibility-pill"
            :class="driver.conflict ? 'not-eligible' : 'eligible'"
          >
            <span class="material-symbols-outlined pill-icon">
              {{ driver.conflict ? 'cancel' : 'check_circle' }}
            </span>
            <span>{{ driver.conflict ? 'ĐANG TRÙNG LỊCH' : 'KHÔNG TRÙNG LỊCH' }}</span>
          </span>

          <span v-if="driver.needsRelocation && !driver.conflict" class="relocate-pill" :title="`Phải chạy rỗng từ ${driver.lastKnownLocation || 'Nơi khác'}`">
            <span class="material-symbols-outlined">directions_car</span>
            <span class="relocate-text">Từ {{ driver.lastKnownLocation || 'Nơi khác' }}</span>
          </span>
        </div>
      </div>

      <!-- List Count Footer -->
      <div v-if="filteredDriverList.length" class="list-footer-bar">
        <span>Hiển thị {{ filteredDriverList.length }} / {{ currentDrivers.length }} tài xế</span>
        <button type="button" class="btn-see-all">Xem tất cả &gt;</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  activeRole: { type: String, default: 'PRIMARY' },
  primaryDrivers: { type: Array, default: () => [] },
  secondaryDrivers: { type: Array, default: () => [] },
  primarySelected: { type: Object, default: null },
  secondarySelected: { type: Object, default: null },
  primarySearch: { type: String, default: '' },
  secondarySearch: { type: String, default: '' },
  allowOutside: Boolean,
  showOutside: Boolean,
  secondaryRequired: Boolean
})

const emit = defineEmits([
  'update:activeRole', 'update:primarySearch', 'update:secondarySearch',
  'update:showOutside', 'select-primary', 'select-secondary', 'clear-secondary'
])

// Show every configured driver by default, including busy/conflicting drivers.
// Admin still needs to see why a driver cannot be selected.
const activeChip = ref(null)

const toggleChip = (chipKey) => {
  const nextChip = activeChip.value === chipKey ? null : chipKey
  activeChip.value = nextChip

  // The parent owns the route eligibility list. It must be told to include
  // outside-route drivers before this column can filter/display them.
  emit('update:showOutside', nextChip === 'OUTSIDE')
}

const currentDrivers = computed(() => props.activeRole === 'PRIMARY' ? props.primaryDrivers : props.secondaryDrivers)
const currentSelected = computed(() => props.activeRole === 'PRIMARY' ? props.primarySelected : props.secondarySelected)
const currentSearch = computed(() => props.activeRole === 'PRIMARY' ? props.primarySearch : props.secondarySearch)

const updateSearch = value => emit(props.activeRole === 'PRIMARY' ? 'update:primarySearch' : 'update:secondarySearch', value)
const selectDriver = driver => emit(props.activeRole === 'PRIMARY' ? 'select-primary' : 'select-secondary', driver)

const routeRoleLabel = driver => {
  if (driver.routeRole === 'PRIMARY') return 'Tuyến chính'
  if (driver.routeRole === 'BACKUP') return 'Dự phòng'
  if (driver.routeRole === 'OUTSIDE') return 'Ngoài tuyến'
  return 'Không giới hạn tuyến'
}

const formatLicenseDate = value => {
  const [year, month, day] = String(value || '').split('-')
  return year && month && day ? `${day}/${month}/${year}` : value
}

const licenseTone = driver => {
  if (!driver.driverLicenseClass) return 'missing'
  if (!driver.driverLicenseExpiryDate) return 'valid'
  return driver.driverLicenseExpiryDate < new Date().toISOString().slice(0, 10) ? 'expired' : 'valid'
}

const filteredDriverList = computed(() => {
  let list = currentDrivers.value
  if (activeChip.value === 'AVAILABLE') {
    list = list.filter(d => !d.conflict)
  } else if (activeChip.value === 'ROUTE') {
    list = list.filter(d => d.routeRole && d.routeRole !== 'OUTSIDE')
  } else if (activeChip.value === 'REST') {
    list = list.filter(d => !d.leaveWarning)
  } else if (activeChip.value === 'OUTSIDE') {
    list = list.filter(d => d.routeRole === 'OUTSIDE')
  }
  return list
})

</script>

<style scoped>
.driver-column {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  border-right: 1px solid #e2e8f0;
  background: #ffffff;
}

.driver-header {
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

/* Role Tabs */
.role-tabs {
  display: flex;
  gap: 0.35rem;
  margin-top: 0.65rem;
  background: #f1f5f9;
  padding: 0.25rem;
  border-radius: 0.6rem;
}

.tab-btn {
  flex: 1;
  padding: 0.45rem 0.5rem;
  border-radius: 0.45rem;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 0.72rem;
  font-weight: 750;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}

.tab-btn.active {
  background: #ea580c;
  color: #ffffff;
  box-shadow: 0 2px 6px rgba(234, 88, 12, 0.25);
}

.required-label {
  color: #dc2626;
  font-weight: 800;
}

.tab-btn.active .required-label {
  color: #fff7ed;
}

.required-notice {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin: 0.65rem 0.65rem 0;
  padding: 0.55rem 0.65rem;
  border: 1px solid #fed7aa;
  border-radius: 0.55rem;
  background: #fff7ed;
  color: #c2410c;
  font-size: 0.67rem;
  font-weight: 750;
}

.required-notice .material-symbols-outlined {
  flex: none;
  font-size: 1rem;
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

/* Filter Chips */
.filter-chips-row {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-top: 0.55rem;
  overflow-x: auto;
}

.chip-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  padding: 0.25rem 0.55rem;
  border-radius: 9999px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #475569;
  font-size: 0.65rem;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.chip-btn.active {
  border-color: #15803d;
  background: #f0fdf4;
  color: #15803d;
}

.chip-check {
  font-size: 0.8rem;
}

/* Driver List */
.driver-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0.65rem;
  background: #f8fafc;
}

.driver-card {
  position: relative;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  padding: 0.65rem;
  margin-bottom: 0.6rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.driver-card:hover:not(.disabled) {
  border-color: #0f766e;
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.08);
}

.driver-card.selected {
  border: 2px solid #0f766e;
  background: #f0fdf4;
}

.driver-card.disabled {
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

.driver-info {
  flex: 1;
  min-width: 0;
}

.driver-name {
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

.driver-phone {
  color: #64748b;
  font-size: 0.65rem;
}

.role-tag {
  color: #64748b;
  font-size: 0.65rem;
}

.role-tag.route-role-primary {
  color: #15803d;
  font-weight: 750;
}

.role-tag.route-role-backup {
  color: #b45309;
  font-weight: 750;
}

.role-tag.route-role-outside {
  color: #7c3aed;
  font-weight: 750;
}

.license-info {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  margin-top: 0.22rem;
  font-size: 0.58rem;
  color: #64748b;
}

.license-info .material-symbols-outlined {
  font-size: 0.78rem;
}

.license-info strong {
  font-weight: 850;
}

.license-info.valid {
  color: #047857;
}

.license-info.expired {
  color: #dc2626;
}

.license-info.missing {
  color: #b45309;
}

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
  font-weight: 800;
  cursor: pointer;
}

.btn-see-all:hover {
  text-decoration: underline;
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

.empty-message {
  padding: 2rem;
  text-align: center;
  color: #94a3b8;
  font-size: 0.72rem;
}
</style>
