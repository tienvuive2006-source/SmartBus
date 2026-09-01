<template>
  <Teleport to="body">
    <div v-if="record" class="maintenance-modal-layer">
      <button class="maintenance-modal-backdrop" type="button" aria-label="Đóng" @click="$emit('close')"></button>
      <form class="maintenance-modal maintenance-modal--small" @submit.prevent="$emit('submit', form)">
        <header><span class="material-symbols-outlined">task_alt</span><div><h3>Hoàn thành bảo trì</h3><p>{{ record.bus?.licensePlate }} · chốt công-tơ-mét mới</p></div><button type="button" @click="$emit('close')"><span class="material-symbols-outlined">close</span></button></header>
        <div class="maintenance-modal__body">
          <label><span>Số kilomet thực tế *</span><div class="maintenance-input-suffix"><input v-model.number="form.currentMileage" :min="record.bus?.lastMaintenanceMileage || 0" step="0.1" required type="number" /><i>km</i></div></label>
          <label><span>Chi phí thực tế *</span><div class="maintenance-input-suffix"><input v-model.number="form.actualCost" min="0" step="1000" required type="number" /><i>đ</i></div></label>
          <label><span>Trừ từ quỹ *</span><select v-model="form.fundType" required><option value="CASH">Quỹ tiền mặt</option><option value="BANK_TRANSFER">Quỹ ngân hàng / chuyển khoản</option><option value="WALLET">Ví nội bộ SkyPay</option></select></label>
          <label><span>Kết quả và ghi chú</span><textarea v-model.trim="form.notes" rows="4" maxlength="1000" placeholder="Hạng mục đã thực hiện, phụ tùng đã thay"></textarea></label>
          <p class="maintenance-reset-note"><span class="material-symbols-outlined">payments</span>Chi phí thực tế sẽ được ghi là khoản chi của quỹ đã chọn.</p>
          <p class="maintenance-reset-note"><span class="material-symbols-outlined">restart_alt</span>Mốc nhắc bảo trì mới sẽ tính từ số kilomet này.</p>
        </div>
        <footer><button type="button" class="maintenance-btn secondary" @click="$emit('close')">Hủy</button><button class="maintenance-btn primary" type="submit" :disabled="submitting">{{ submitting ? 'Đang lưu…' : 'Xác nhận hoàn thành' }}</button></footer>
      </form>
    </div>
  </Teleport>
</template>
<script setup>
import { reactive, watch } from 'vue'
const props = defineProps({ record: Object, submitting: Boolean })
defineEmits(['close', 'submit'])
const form = reactive({ currentMileage: 0, actualCost: null, fundType: 'CASH', notes: '' })
watch(() => props.record, record => { if (record) Object.assign(form, { currentMileage: Number(record.bus?.currentMileage || 0), actualCost: record.estimatedCost ?? null, fundType: 'CASH', notes: '' }) }, { immediate: true })
</script>
