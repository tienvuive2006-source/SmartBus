<template>
  <Teleport to="body">
    <div v-if="open" class="maintenance-modal-layer">
      <button class="maintenance-modal-backdrop" type="button" aria-label="Đóng" @click="$emit('close')"></button>
      <form class="maintenance-modal maintenance-modal--create" @submit.prevent="submit">
        <header><span class="material-symbols-outlined">build_circle</span><div><h3>Lập phiếu bảo trì</h3><p>Lên lịch và thiết lập chu kỳ kilomet</p></div><button type="button" @click="$emit('close')"><span class="material-symbols-outlined">close</span></button></header>
        <div class="maintenance-modal__body">
          <p v-if="error" class="maintenance-inline-error">{{ error }}</p>
          <div class="maintenance-create-layout">
            <section class="maintenance-create-fields">
              <label><span>Xe cần bảo trì *</span><select v-model.number="form.busId" required><option disabled value="">Chọn biển số xe</option><option v-for="bus in availableBuses" :key="bus.id" :value="bus.id">{{ bus.licensePlate }} · {{ bus.busType }}</option></select></label>
              <div class="maintenance-form-grid">
                <label><span>Loại bảo trì *</span><select v-model="form.maintenanceType" required><option value="PERIODIC">Bảo trì định kỳ</option><option value="OIL_CHANGE">Thay dầu</option><option value="TIRE">Lốp và hệ thống phanh</option><option value="REPAIR">Sửa chữa phát sinh</option><option value="INSPECTION">Đăng kiểm</option></select></label>
                <label><span>Chu kỳ nhắc *</span><div class="maintenance-input-suffix"><input v-model.number="form.maintenanceIntervalKm" min="100" step="100" required type="number" /><i>km</i></div></label>
                <label><span>Bắt đầu dự kiến *</span><input v-model="form.scheduledStart" required type="datetime-local" /></label>
                <label><span>Hoàn thành dự kiến</span><input v-model="form.expectedEnd" type="datetime-local" /></label>
                <label><span>Gara phụ trách</span><input v-model.trim="form.garageName" type="text" placeholder="Tên gara hoặc bộ phận kỹ thuật" /></label>
                <label><span>Chi phí dự kiến</span><div class="maintenance-input-suffix"><input v-model.number="form.estimatedCost" min="0" step="1000" type="number" /><i>đ</i></div></label>
              </div>
              <label><span>Mô tả công việc</span><textarea v-model.trim="form.description" rows="4" maxlength="1000" placeholder="Các hạng mục cần kiểm tra hoặc thay thế"></textarea></label>
            </section>
            <MaintenanceVehicleSummary :bus="selectedBus" :records="records" />
          </div>
        </div>
        <footer><button type="button" class="maintenance-btn secondary" @click="$emit('close')">Hủy</button><button class="maintenance-btn primary" type="submit" :disabled="submitting">{{ submitting ? 'Đang lưu…' : 'Tạo phiếu' }}</button></footer>
      </form>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import MaintenanceVehicleSummary from './MaintenanceVehicleSummary.vue'
const props = defineProps({ open: Boolean, buses: { type: Array, default: () => [] }, records: { type: Array, default: () => [] }, submitting: Boolean })
const emit = defineEmits(['close', 'submit'])
const error = ref('')
const toLocalInput = date => { const d = new Date(date.getTime() - date.getTimezoneOffset() * 60000); return d.toISOString().slice(0, 16) }
const initialForm = () => { const start = new Date(); start.setMinutes(start.getMinutes() + 30); const end = new Date(start); end.setHours(end.getHours() + 4); return { busId: '', maintenanceType: 'PERIODIC', maintenanceIntervalKm: 10000, scheduledStart: toLocalInput(start), expectedEnd: toLocalInput(end), garageName: '', estimatedCost: null, description: '' } }
const form = reactive(initialForm())
const availableBuses = computed(() => props.buses.filter(bus => bus.status !== 'BẢO TRÌ'))
const selectedBus = computed(() => props.buses.find(bus => Number(bus.id) === Number(form.busId)) || null)
watch(() => props.open, value => { if (value) { Object.assign(form, initialForm()); error.value = '' } })
watch(selectedBus, bus => { if (bus?.maintenanceIntervalKm) form.maintenanceIntervalKm = Number(bus.maintenanceIntervalKm) })
const submit = () => {
  error.value = ''
  if (form.expectedEnd && form.expectedEnd < form.scheduledStart) { error.value = 'Thời gian hoàn thành phải sau thời gian bắt đầu.'; return }
  emit('submit', { ...form, scheduledStart: form.scheduledStart || null, expectedEnd: form.expectedEnd || null })
}
</script>
