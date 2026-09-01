<template>
  <footer class="modal-footer">
    <template v-if="readOnly">
      <div class="readonly-notice">
        <span class="material-symbols-outlined">lock</span>
        <span>Chuyến đã bắt đầu hoặc hoàn thành — chỉ xem thông tin phân công.</span>
      </div>
      <div class="footer-actions">
        <button type="button" class="btn-cancel" @click="$emit('close')">Đóng</button>
      </div>
    </template>
    <template v-else-if="confirmingUnassign">
      <div class="unassign-alert">
        <span class="material-symbols-outlined icon-alert">warning</span>
        <span>Bạn có chắc chắn muốn bỏ toàn bộ phân công (Tài xế, Xe, Lơ xe) khỏi chuyến này?</span>
      </div>
      <div class="footer-actions">
        <button type="button" class="btn-cancel" :disabled="submitting" @click="$emit('cancel-unassign')">Quay lại</button>
        <button type="button" class="btn-confirm-danger" :disabled="submitting" @click="$emit('unassign')">Xác nhận hủy</button>
      </div>
    </template>
    <template v-else>
      <div class="note-box">
        <span class="note-label">Ghi chú phân công</span>
        <div class="note-input-wrap">
          <input
            :value="note"
            maxlength="300"
            placeholder="Nhập ghi chú cho điều hành hoặc tài xế (nếu có)..."
            @input="$emit('update:note', $event.target.value)"
          />
          <small class="note-count">{{ note.length }}/300</small>
        </div>
      </div>

      <div class="footer-actions">
        <button v-if="hasAssignment" type="button" class="btn-unassign" @click="$emit('request-unassign')">
          <span class="material-symbols-outlined">person_off</span>
          <span>Hủy phân công</span>
        </button>

        <button type="button" class="btn-cancel" @click="$emit('close')">Đóng</button>

        <button type="button" class="btn-submit" :disabled="!canSubmit || submitting" @click="$emit('submit')">
          <span class="material-symbols-outlined" :class="{ spinning: submitting }">
            {{ submitting ? 'sync' : 'check_circle' }}
          </span>
          <span>{{ submitting ? 'Đang lưu...' : 'Xác nhận phân công' }}</span>
        </button>
      </div>
    </template>
  </footer>
</template>

<script setup>
defineProps({
  note: { type: String, default: '' },
  hasAssignment: Boolean,
  confirmingUnassign: Boolean,
  submitting: Boolean,
  canSubmit: Boolean,
  readOnly: Boolean
})
defineEmits(['update:note', 'close', 'submit', 'request-unassign', 'cancel-unassign', 'unassign'])
</script>

<style scoped>
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 4.2rem;
  padding: 0.75rem 1.25rem;
  border-top: 1px solid #e2e8f0;
  background: #ffffff;
  flex: none;
}

.note-box {
  display: flex;
  flex-direction: column;
  flex: 1;
  max-width: 26rem;
}

.note-label {
  color: #64748b;
  font-size: 0.64rem;
  font-weight: 700;
  margin-bottom: 0.2rem;
}

.note-input-wrap {
  position: relative;
  width: 100%;
}

.note-input-wrap input {
  width: 100%;
  height: 2.2rem;
  padding: 0 3.2rem 0 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.55rem;
  background: #f8fafc;
  font-size: 0.72rem;
  color: #0f172a;
  outline: none;
  box-sizing: border-box;
}

.note-input-wrap input:focus {
  border-color: #0f766e;
  background: #ffffff;
}

.note-count {
  position: absolute;
  right: 0.65rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 0.6rem;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  margin-left: auto;
}

.btn-cancel {
  padding: 0.55rem 1.1rem;
  border-radius: 0.6rem;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #475569;
  font-size: 0.75rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.btn-unassign {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.55rem 0.9rem;
  border-radius: 0.6rem;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #dc2626;
  font-size: 0.72rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-unassign:hover {
  background: #fee2e2;
  border-color: #fca5a5;
}

.btn-unassign span {
  font-size: 1rem;
}

.btn-submit {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.6rem 1.35rem;
  border-radius: 0.6rem;
  border: none;
  background: #0d9488;
  color: #ffffff;
  font-size: 0.78rem;
  font-weight: 850;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.25);
  transition: all 0.2s ease;
}

.btn-submit:hover:not(:disabled) {
  background: #0f766e;
  box-shadow: 0 6px 16px rgba(15, 118, 110, 0.3);
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-submit span {
  font-size: 1.1rem;
}

.unassign-alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #dc2626;
  font-size: 0.75rem;
  font-weight: 800;
}

.icon-alert {
  font-size: 1.2rem;
}

.readonly-notice {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  color: #475569;
  font-size: 0.75rem;
  font-weight: 800;
}

.readonly-notice .material-symbols-outlined {
  color: #64748b;
  font-size: 1.1rem;
}

.btn-confirm-danger {
  padding: 0.55rem 1.1rem;
  border-radius: 0.6rem;
  border: none;
  background: #dc2626;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 850;
  cursor: pointer;
}

.btn-confirm-danger:hover {
  background: #b91c1c;
}

.spinning {
  animation: spin 0.75s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
