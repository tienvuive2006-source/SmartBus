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
            <label class="text-label-md font-black text-on-surface-variant uppercase flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">person_pin</span>
              Tên Tài Xế Đảm Nhiệm
            </label>
            <input 
              v-model="form.driverName" 
              type="text" 
              required
              placeholder="Họ và tên lái xe..."
              class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-2.5 focus:outline-none font-bold transition-colors"
            />
          </div>

          <div class="space-y-1.5 p-3 bg-surface-container-lowest rounded-2xl border border-dashed border-primary/30">
            <label class="text-label-md font-black text-primary uppercase flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">location_on</span>
              Vị Trí/Trạm Đậu Xe Hiện Tại
            </label>
            <select 
              v-model="form.currentStation" 
              required
              class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-2.5 focus:outline-none font-bold bg-white transition-colors"
            >
              <option value="Hà Nội">Trạm Hà Nội (Trụ sở)</option>
              <option value="Hải Phòng">Trạm Hải Phòng</option>
              <option value="SaPa">Trạm SaPa</option>
              <option value="Đà Nẵng">Trạm Đà Nẵng</option>
              <option value="Nha Trang">Trạm Nha Trang</option>
              <option value="Sài Gòn">Trạm Sài Gòn</option>
              <option value="Cần Thơ">Trạm Cần Thơ</option>
            </select>
            <p class="text-[10px] font-medium text-on-surface-variant mt-1">💡 Định vị GPS vệ tinh sẽ tự động thả ghim vị trí của xe tại Tỉnh thành này trên bản đồ!</p>
          </div>

          <div class="space-y-1.5">
            <label class="text-label-md font-black text-on-surface-variant uppercase flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">image</span>
              Hình Ảnh Xe Thật
            </label>
            <div class="flex gap-2">
              <input 
                v-model="form.imageUrl" 
                type="url" 
                placeholder="Link ảnh hoặc tải lên..."
                class="flex-1 border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-2.5 focus:outline-none font-bold transition-colors"
              />
              <button 
                type="button"
                @click="fileInput.click()"
                class="px-4 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl flex items-center justify-center transition-all active:scale-95 border border-slate-200"
                :disabled="uploading"
              >
                <span class="material-symbols-outlined">{{ uploading ? 'sync' : 'upload_file' }}</span>
              </button>
            </div>
            <div v-if="form.imageUrl" class="mt-2 relative group overflow-hidden rounded-2xl border-2 border-dashed border-primary/20 aspect-video bg-slate-50 flex items-center justify-center">
              <img :src="form.imageUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" @error="(e) => e.target.style.display = 'none'" />
              <div class="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                <span class="text-white text-[10px] font-black uppercase tracking-widest">Xem trước hình ảnh xe</span>
              </div>
            </div>
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
      <!-- Hidden File Input -->
      <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleImageUpload" />
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
  busTypes: Array
});

const emit = defineEmits(['close', 'submit']);

const fileInput = ref(null);
const uploading = ref(false);

const handleImageUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  uploading.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset');

  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    // Lưu ý: Cần gán trực tiếp vào props.form vì đây là object tham chiếu
    props.form.imageUrl = res.data.secure_url;
  } catch (err) {
    console.error("Lỗi tải ảnh Cloudinary:", err);
    alert("Lỗi tải ảnh lên Cloudinary!");
  } finally {
    uploading.value = false;
  }
};
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
