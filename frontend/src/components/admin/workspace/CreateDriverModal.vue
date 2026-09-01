<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl w-full max-w-sm shadow-2xl overflow-hidden animate-scale-up border border-slate-100">
        <div class="p-5 bg-slate-900 text-white flex justify-between items-center">
          <h3 class="font-black tracking-wide flex items-center gap-2 uppercase">
            <span class="material-symbols-outlined">person_add</span> Thêm Tài Xế
          </h3>
          <button @click="$emit('close')" class="text-white/50 hover:text-white p-1 rounded-full transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <form @submit.prevent="submitForm" class="p-5 space-y-4">
          <div class="space-y-1">
            <label class="text-xs font-black uppercase text-slate-500">Họ và Tên</label>
            <input v-model="form.fullName" required type="text" class="w-full border-2 border-slate-200 rounded-xl px-4 py-2 font-bold focus:border-primary outline-none" placeholder="Nguyễn Văn A" />
          </div>
          <div class="space-y-1">
            <label class="text-xs font-black uppercase text-slate-500">Tên đăng nhập</label>
            <input v-model.trim="form.username" required type="text" class="w-full border-2 border-slate-200 rounded-xl px-4 py-2 font-bold focus:border-primary outline-none" placeholder="Ví dụ: taixe1" />
          </div>
          <div class="space-y-1">
            <label class="text-xs font-black uppercase text-slate-500">Số Điện Thoại</label>
            <input v-model.trim="form.phone" required type="tel" inputmode="tel" class="w-full border-2 border-slate-200 rounded-xl px-4 py-2 font-bold focus:border-primary outline-none" placeholder="0901234567" />
          </div>
          <div class="space-y-1">
            <label class="text-xs font-black uppercase text-slate-500">Mật khẩu khởi tạo</label>
            <input v-model="form.password" required type="password" class="w-full border-2 border-slate-200 rounded-xl px-4 py-2 font-bold focus:border-primary outline-none" placeholder="******" />
          </div>
          
          <!-- Image Upload (3x4 ratio) -->
          <div class="flex flex-col items-center mt-4 border-t border-slate-100 pt-4">
            <div class="relative group mb-3">
              <div class="w-24 h-32 bg-slate-50 border-2 border-dashed border-slate-300 rounded-2xl flex flex-col items-center justify-center overflow-hidden cursor-pointer hover:border-primary transition-colors shadow-sm" @click="$refs.fileInput.click()">
                <img v-if="form.avatarUrl" :src="form.avatarUrl" class="w-full h-full object-cover" />
                <div v-else class="text-center p-2">
                  <span class="material-symbols-outlined text-slate-300 text-[32px]">add_photo_alternate</span>
                  <p class="text-[9px] text-slate-500 font-black uppercase mt-2 tracking-widest">Ảnh 3x4</p>
                </div>
              </div>
              <div v-if="form.avatarUrl" class="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity rounded-2xl flex items-center justify-center cursor-pointer backdrop-blur-sm" @click="$refs.fileInput.click()">
                <span class="material-symbols-outlined text-white">edit</span>
              </div>
              <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleFileUpload" />
              <div v-if="uploading" class="absolute inset-0 bg-white/90 backdrop-blur-sm rounded-2xl flex flex-col items-center justify-center z-10">
                 <div class="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin mb-2"></div>
                 <span class="text-[9px] font-black uppercase tracking-widest text-primary animate-pulse">Đang tải</span>
              </div>
            </div>
            
            <div class="w-full flex items-center gap-2">
              <input 
                type="url" 
                placeholder="Hoặc dán link ảnh..." 
                class="flex-1 px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-medium text-slate-800 focus:bg-white focus:border-primary outline-none transition-all"
                v-model="imageUrlInput"
                @keydown.enter.prevent="handleUrlUpload"
              />
              <button 
                type="button" 
                @click="handleUrlUpload" 
                :disabled="!imageUrlInput || uploading"
                class="px-4 py-2.5 bg-indigo-50 text-indigo-600 hover:bg-indigo-600 hover:text-white rounded-xl text-xs font-black tracking-widest uppercase transition-all disabled:opacity-50"
              >
                Tải lên
              </button>
            </div>
          </div>
          <button type="submit" :disabled="submittingDriver" class="w-full mt-2 bg-primary text-white font-black py-3 rounded-xl hover:bg-surface-tint flex items-center justify-center gap-2 transition-all active:scale-95">
            <span class="material-symbols-outlined text-[20px]">{{ submittingDriver ? 'hourglass_empty' : 'check_circle' }}</span>
            {{ submittingDriver ? 'ĐANG TẠO...' : 'TẠO TÀI KHOẢN' }}
          </button>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue';
import axios from 'axios';

const props = defineProps({
  isOpen: { type: Boolean, default: false },
  submittingDriver: { type: Boolean, default: false }
});
const emit = defineEmits(['close', 'submit']);

const form = ref({ fullName: '', username: '', phone: '', password: '', avatarUrl: '' });
const fileInput = ref(null);
const uploading = ref(false);
const imageUrlInput = ref('');

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    form.value = { fullName: '', username: '', phone: '', password: '', avatarUrl: '' };
    imageUrlInput.value = '';
  }
});

const handleFileUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  uploading.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset');

  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    form.value.avatarUrl = res.data.secure_url;
  } catch (err) {
    console.error(err);
    alert("Lỗi upload ảnh. Vui lòng thử lại!");
  } finally {
    uploading.value = false;
    e.target.value = '';
  }
};

const handleUrlUpload = async () => {
  if (!imageUrlInput.value) return;
  
  uploading.value = true;
  const formData = new FormData();
  formData.append('file', imageUrlInput.value);
  formData.append('upload_preset', 'skybus_preset');

  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    form.value.avatarUrl = res.data.secure_url;
    imageUrlInput.value = '';
  } catch (err) {
    console.error(err);
    alert("Lỗi tải ảnh từ URL!");
  } finally {
    uploading.value = false;
  }
};

const submitForm = () => {
  emit('submit', form.value);
};
</script>

<style scoped>
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes scaleUp {
  0% { opacity: 0; transform: scale(0.95); }
  100% { opacity: 1; transform: scale(1); }
}
</style>
