<template>
  <header class="modal-header">
    <div class="title-block">
      <div class="title-icon-circle">
        <span class="material-symbols-outlined">person</span>
      </div>
      <div class="title-text">
        <h2>Phân công tài xế thông minh</h2>
        <p>Tự động gợi ý và tối ưu phù hợp – Nhanh chóng – Chính xác – An toàn</p>
      </div>
    </div>
    <button type="button" class="close-button" aria-label="Đóng cửa sổ phân công" @click="$emit('close')">
      <span class="material-symbols-outlined">close</span>
    </button>
  </header>
</template>

<script setup>
defineEmits(['close'])
</script>

<style scoped>
.modal-header {
  display: flex;
  min-height: 4.2rem;
  flex: none;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem;
  border-bottom: 1px solid #e2e8f0;
  padding: 0.85rem 1.25rem;
  background: #ffffff;
}

.title-block {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.title-icon-circle {
  display: flex;
  width: 2.6rem;
  height: 2.6rem;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  color: #ffffff;
  background: #0d9488;
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.2);
}

.title-icon-circle span {
  font-size: 1.4rem;
}

.title-text h2 {
  color: #0f172a;
  font-size: 1.15rem;
  font-weight: 900;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.title-text p {
  margin-top: 0.15rem;
  color: #64748b;
  font-size: 0.72rem;
  font-weight: 600;
}

.close-button {
  display: flex;
  width: 2.3rem;
  height: 2.3rem;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 0.65rem;
  border: 1px solid #e2e8f0;
  color: #64748b;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-button:hover {
  color: #ef4444;
  background: #fef2f2;
  border-color: #fca5a5;
}

@media (max-width: 720px) {
  .modal-header {
    padding: 0.75rem 1rem;
  }
  .title-text p {
    display: none;
  }
  .title-text h2 {
    font-size: 1rem;
  }
}
</style>

