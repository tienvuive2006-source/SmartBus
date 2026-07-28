<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="$emit('update:show', false)"></div>
      <div class="relative w-full sm:max-w-md bg-white sm:rounded-[2rem] rounded-t-[2rem] shadow-2xl overflow-hidden flex flex-col animate-slide-up sm:animate-fade-in-up">
        
        <div class="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white">
          <h3 class="text-title-md font-black text-error flex items-center gap-2">
            <span class="material-symbols-outlined">warning</span>
            Báo cáo sự cố khẩn cấp
          </h3>
          <button @click="$emit('update:show', false)" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto bg-slate-50 space-y-4">
          <div>
            <label class="block text-label-md font-bold text-slate-700 mb-1">Mức độ sự cố</label>
            <select v-model="form.severity" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-body-md font-bold focus:outline-none focus:border-error focus:ring-2 focus:ring-error/20">
              <option value="" disabled>-- Chọn mức độ sự cố --</option>
              <option value="LOW">Thấp (Trễ giờ, Tắc đường)</option>
              <option value="HIGH">Cao (Hư hỏng nhẹ, Khách ốm)</option>
              <option value="CRITICAL">Khẩn cấp (Tai nạn, Hư hỏng nặng)</option>
            </select>
          </div>
          <div>
            <label class="block text-label-md font-bold text-slate-700 mb-1">Mô tả chi tiết</label>
            <textarea v-model="form.description" rows="4" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-body-md focus:outline-none focus:border-error focus:ring-2 focus:ring-error/20" placeholder="Nhập chi tiết sự cố xảy ra..."></textarea>
          </div>
        </div>

        <div class="p-6 bg-white border-t border-slate-100">
          <button @click="handleSubmit" :disabled="!form.description || !form.severity" class="w-full bg-error text-white font-black py-4 rounded-2xl active:scale-95 transition-transform disabled:opacity-50">
            GỬI BÁO CÁO NGAY
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:show', 'submit']);

const form = ref({
  severity: '',
  description: ''
});

watch(() => props.show, (newVal) => {
  if (newVal) {
    // Reset form when modal opens
    form.value = { severity: '', description: '' };
  }
});

const handleSubmit = () => {
  emit('submit', { ...form.value });
};
</script>
