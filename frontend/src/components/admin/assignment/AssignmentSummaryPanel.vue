<template>
  <aside class="summary-panel">
    <header class="panel-header">
      <div class="column-title">
        <span class="step-badge">4</span>
        <h4>Tổng quan phân công</h4>
      </div>
    </header>

    <div class="panel-content">
      <!-- 1. Driver Summary Box -->
      <div class="summary-card">
        <div class="card-icon-title">
          <span class="material-symbols-outlined icon-teal">person</span>
          <span class="card-label">Tài xế chính</span>
        </div>
        <div class="card-value-block">
          <strong v-if="driver" class="val-name">{{ driver.fullName }}</strong>
          <span v-else class="val-empty">Chưa chọn tài xế chính</span>
          <small v-if="driver" class="val-sub">SĐT: {{ driver.phone || 'Chưa cập nhật' }}<template v-if="driver.driverLicenseClass"> • GPLX hạng {{ driver.driverLicenseClass }}</template></small>
        </div>
      </div>

      <!-- 2. Secondary Driver Summary Box -->
      <div class="summary-card">
        <div class="card-icon-title">
          <span class="material-symbols-outlined icon-orange">person_add</span>
          <span class="card-label">
            Tài xế phụ
            <em v-if="secondaryRequired" class="required-badge">Bắt buộc</em>
          </span>
        </div>
        <div class="card-value-block">
          <strong v-if="secondaryDriver" class="val-name">{{ secondaryDriver.fullName }}</strong>
          <span v-else class="val-empty">{{ secondaryRequired ? 'Chưa chọn — bắt buộc với chuyến dài' : 'Không bố trí' }}</span>
          <small v-if="secondaryDriver" class="val-sub">SĐT: {{ secondaryDriver.phone }}<template v-if="secondaryDriver.driverLicenseClass"> • GPLX hạng {{ secondaryDriver.driverLicenseClass }}</template></small>
        </div>
      </div>

      <!-- 3. Vehicle Summary Box -->
      <div class="summary-card">
        <div class="card-icon-title">
          <span class="material-symbols-outlined icon-blue">directions_bus</span>
          <span class="card-label">Phương tiện</span>
        </div>
        <div class="card-value-block">
          <strong v-if="bus" class="val-name">{{ bus.licensePlate }}</strong>
          <span v-else class="val-empty">Chưa chọn phương tiện</span>
          <small v-if="bus" class="val-sub">{{ bus.busType || 'Chưa cập nhật dòng xe' }}<template v-if="bus.capacity != null"> • {{ bus.capacity }} ghế</template></small>
        </div>
      </div>

      <!-- 4. Inspector Summary Box -->
      <div class="summary-card">
        <div class="card-icon-title">
          <span class="material-symbols-outlined icon-purple">badge</span>
          <span class="card-label">Lơ xe / Phục vụ</span>
        </div>
        <div class="card-value-block">
          <strong v-if="inspector" class="val-name">{{ inspector.fullName }}</strong>
          <span v-else class="val-empty">Không bố trí</span>
          <small v-if="inspector" class="val-sub">SĐT: {{ inspector.phone }}</small>
        </div>
      </div>

      <!-- Chỉ hiện khi hệ thống tìm thấy chuyến về phù hợp -->
      <slot name="return-trip"></slot>

      <!-- Checklist Section -->
      <div class="checklist-section">
        <h5 class="checklist-title">Kiểm tra điều kiện vận hành</h5>
        <ul class="checklist">
          <li :class="{ check: driver && !driver.conflict }">
            <span class="material-symbols-outlined list-icon">
              {{ driver && !driver.conflict ? 'check_circle' : 'cancel' }}
            </span>
            <span>Tài xế chính rảnh & đủ giờ nghỉ</span>
          </li>
          <li v-if="secondaryRequired" :class="{ check: secondaryDriver && !secondaryDriver.conflict }">
            <span class="material-symbols-outlined list-icon">
              {{ secondaryDriver && !secondaryDriver.conflict ? 'check_circle' : 'cancel' }}
            </span>
            <span>Đã bố trí tài xế phụ cho chuyến dài</span>
          </li>
          <li :class="{ check: bus && !bus.conflict }">
            <span class="material-symbols-outlined list-icon">
              {{ bus && !bus.conflict ? 'check_circle' : 'cancel' }}
            </span>
            <span>Xe đúng dòng & đạt chuẩn kỹ thuật</span>
          </li>
          <li :class="{ check: isReady }">
            <span class="material-symbols-outlined list-icon">
              {{ isReady ? 'check_circle' : 'error' }}
            </span>
            <span>Không trùng lịch chuyến khác</span>
          </li>
        </ul>
      </div>

      <!-- Final Status Alert Banner -->
      <div class="status-banner" :class="isReady ? 'ready-banner' : 'warning-banner'">
        <span class="material-symbols-outlined banner-icon">
          {{ isReady ? 'verified' : 'warning' }}
        </span>
        <div class="banner-text">
          <strong>{{ isReady ? 'ĐỦ ĐIỀU KIỆN KÍCH HOẠT CHUYẾN' : 'CHƯA ĐỦ ĐIỀU KIỆN PHÂN CÔNG' }}</strong>
          <p>{{ readinessMessage }}</p>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  driver: { type: Object, default: null },
  secondaryDriver: { type: Object, default: null },
  bus: { type: Object, default: null },
  inspector: { type: Object, default: null },
  conflictFree: { type: Boolean, default: true },
  secondaryRequired: { type: Boolean, default: false },
  trip: Object
})

const isReady = computed(() => {
  return !!props.driver && !props.driver.conflict
    && !!props.bus && !props.bus.conflict
    && props.conflictFree
    && (!props.secondaryRequired || (!!props.secondaryDriver && !props.secondaryDriver.conflict))
})

const readinessMessage = computed(() => {
  if (isReady.value) return 'Tất cả tài nguyên đã sẵn sàng xuất bến đúng giờ'
  if (props.secondaryRequired && !props.secondaryDriver) return 'Chuyến dài bắt buộc phải chọn thêm tài xế phụ'
  return 'Vui lòng chọn đủ tài xế chính và phương tiện'
})
</script>

<style scoped>
.summary-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: #ffffff;
}

.panel-header {
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

.panel-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0.85rem;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.summary-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.65rem;
  padding: 0.65rem;
}

.card-icon-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.card-icon-title span {
  font-size: 1.1rem;
}

.icon-teal { color: #0d9488; }
.icon-orange { color: #ea580c; }
.icon-blue { color: #2563eb; }
.icon-purple { color: #9333ea; }

.card-label {
  color: #64748b;
  font-size: 0.65rem;
  font-weight: 750;
}

.required-badge {
  display: inline-flex;
  margin-left: 0.25rem;
  padding: 0.08rem 0.28rem;
  border-radius: 0.25rem;
  background: #ffedd5;
  color: #c2410c;
  font-size: 0.55rem;
  font-style: normal;
  font-weight: 850;
  text-transform: uppercase;
}

.card-value-block {
  margin-top: 0.3rem;
  display: flex;
  flex-direction: column;
}

.val-name {
  color: #0f172a;
  font-size: 0.82rem;
  font-weight: 850;
}

.val-empty {
  color: #94a3b8;
  font-size: 0.75rem;
  font-style: italic;
}

.val-sub {
  color: #64748b;
  font-size: 0.64rem;
  margin-top: 0.1rem;
}

/* Checklist Section */
.checklist-section {
  margin-top: 0.4rem;
  padding: 0.65rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.65rem;
}

.checklist-title {
  color: #0f172a;
  font-size: 0.75rem;
  font-weight: 850;
  margin-bottom: 0.45rem;
}

.checklist {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.checklist li {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.68rem;
  color: #64748b;
  font-weight: 650;
}

.checklist li.check {
  color: #15803d;
  font-weight: 750;
}

.list-icon {
  font-size: 0.95rem;
  color: #cbd5e1;
}

.checklist li.check .list-icon {
  color: #16a34a;
}

/* Status Banner */
.status-banner {
  margin-top: auto;
  display: flex;
  align-items: flex-start;
  gap: 0.55rem;
  padding: 0.75rem;
  border-radius: 0.65rem;
}

.ready-banner {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
}

.warning-banner {
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #92400e;
}

.banner-icon {
  font-size: 1.35rem;
  flex-shrink: 0;
  margin-top: 0.05rem;
}

.banner-text strong {
  display: block;
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: -0.01em;
}

.banner-text p {
  font-size: 0.64rem;
  margin-top: 0.15rem;
  line-height: 1.35;
  opacity: 0.9;
}
</style>
