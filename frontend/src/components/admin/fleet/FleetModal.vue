<template>
  <Teleport to="body">
    <div 
      v-if="isOpen" 
      class="fixed inset-0 z-[9999] overflow-y-auto flex items-center justify-center px-4 py-6 bg-black/60 backdrop-blur-sm animate-fade-in"
    >
      <div class="bg-white rounded-3xl w-full max-w-md shadow-[0px_24px_64px_rgba(0,0,0,0.3)] border border-outline-variant/30 animate-scale-up flex flex-col max-h-[90vh] overflow-hidden">
        
        <div :class="[
          'p-5 text-white flex justify-between items-center shrink-0 transition-all duration-300',
          isEditMode ? 'bg-surface-tint' : 'bg-primary'
        ]">
          <h3 class="text-headline-sm font-headline-sm font-black tracking-wide uppercase flex items-center gap-2">
            <span>{{ isEditMode ? '✏️' : '🚚' }}</span>
            {{ isEditMode ? 'CẬP NHẬT THÔNG TIN' : 'ĐĂNG KÝ XE MỚI' }}
          </h3>
          <button 
            @click="$emit('close')" 
            class="text-white/70 hover:text-white bg-white/10 hover:bg-white/25 p-1.5 rounded-full active:scale-90 transition-all"
          >
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>

        <form @submit.prevent="$emit('submit')" class="flex-1 overflow-y-auto p-6 space-y-4">
          <div class="space-y-1.5">
            <label class="text-label-md font-black text-on-surface-variant uppercase flex items-center gap-1">
              <span class="material-symbols-outlined text-sm text-amber-600">subtitles</span>
              Biển Số Xe Đăng Ký
            </label>
            <input 
              v-model="form.licensePlate" 
              type="text" 
              required
              placeholder="Ví dụ: 29B-201.99"
              class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-2.5 focus:outline-none font-black text-lg tracking-widest uppercase font-mono transition-colors"
            />
          </div>



          <div class="space-y-1.5">
            <label class="text-label-md font-black text-on-surface-variant uppercase">Dòng Xe / Cấu Hình Ghế</label>
            <select 
              v-model="form.busType" 
              required
              @change="(e) => {
                const selectedType = busTypes.find(t => t.name === e.target.value);
                if(selectedType && selectedType.imageUrl) {
                  form.imageUrl = selectedType.imageUrl;
                }
              }"
              class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-2.5 focus:outline-none font-bold transition-colors"
            >
              <option v-if="busTypes.length === 0" value="" disabled>-- Chưa cấu hình catalog Dòng xe --</option>
              <option 
                v-for="type in busTypes" 
                :key="type.id" 
                :value="type.name"
              >
                {{ type.name }} ({{ type.seatCount }} Ghế)
              </option>
            </select>
          </div>

          <div class="space-y-1.5">
            <label class="text-label-md font-black text-on-surface-variant uppercase">Hạn đăng kiểm</label>
            <input
              v-model="form.inspectionExpiryDate"
              type="date"
              class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-2.5 focus:outline-none font-bold transition-colors"
            />
          </div>





          <div class="space-y-1.5">
            <label class="text-label-md font-black text-on-surface-variant uppercase">Trạng Thái Máy Móc & Khai Thác</label>
            <div class="grid grid-cols-3 gap-2">
              <label class="flex flex-col items-center justify-center border-2 rounded-xl p-2.5 cursor-pointer transition-all"
                :class="form.status === 'ĐANG CHẠY' ? 'border-emerald-500 bg-emerald-50 text-emerald-700 font-black shadow-sm' : 'border-outline-variant/50 text-on-surface-variant'">
                <input type="radio" v-model="form.status" value="ĐANG CHẠY" class="sr-only" />
                <span class="material-symbols-outlined text-xl">local_shipping</span>
                <span class="text-[10px] mt-1">Đang Chạy</span>
              </label>
              <label class="flex flex-col items-center justify-center border-2 rounded-xl p-2.5 cursor-pointer transition-all"
                :class="form.status === 'BẢO TRÌ' ? 'border-amber-500 bg-amber-50 text-amber-700 font-black shadow-sm' : 'border-outline-variant/50 text-on-surface-variant'">
                <input type="radio" v-model="form.status" value="BẢO TRÌ" class="sr-only" />
                <span class="material-symbols-outlined text-xl">build</span>
                <span class="text-[10px] mt-1">Bảo Trì</span>
              </label>
              <label class="flex flex-col items-center justify-center border-2 rounded-xl p-2.5 cursor-pointer transition-all"
                :class="form.status === 'ĐANG NGHỈ' ? 'border-slate-500 bg-slate-50 text-slate-700 font-black shadow-sm' : 'border-outline-variant/50 text-on-surface-variant'">
                <input type="radio" v-model="form.status" value="ĐANG NGHỈ" class="sr-only" />
                <span class="material-symbols-outlined text-xl">garage</span>
                <span class="text-[10px] mt-1">Đang Nghỉ</span>
              </label>
            </div>
          </div>
        </form>

        <div class="p-5 border-t border-outline-variant/20 flex items-center justify-end gap-3 bg-surface-bright shrink-0">
          <button 
            type="button"
            @click="$emit('close')"
            class="px-5 py-2 border-2 border-outline-variant hover:bg-surface-container text-on-surface-variant rounded-xl font-bold transition-all"
          >
            Hủy bỏ
          </button>
          <button 
            @click="$emit('submit')"
            type="button"
            class="px-8 py-2.5 bg-primary text-on-primary hover:bg-surface-tint hover:shadow-md active:scale-95 rounded-xl font-black tracking-wide transition-all flex items-center gap-2"
          >
            <span class="material-symbols-outlined text-sm">save</span>
            {{ isEditMode ? 'LƯU THAY ĐỔI' : 'LƯU VÀO KHO' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const props = defineProps({
  isOpen: Boolean,
  isEditMode: Boolean,
  form: Object,
  busTypes: Array,
  drivers: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['close', 'submit']);

</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}
.animate-scale-up {
  animation: scaleUp 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
