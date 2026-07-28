<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div @click="$emit('close')" class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fade-in"></div>
      
      <!-- Modal Content -->
      <div class="bg-white w-full max-w-xl max-h-[95vh] rounded-[32px] shadow-[0_32px_64px_-12px_rgba(0,0,0,0.2)] border border-slate-100 overflow-hidden animate-scale-up relative flex flex-col">
        <!-- Header -->
        <div class="p-8 pb-4 flex justify-between items-start">
          <div>
            <div class="w-12 h-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-4">
              <span class="material-symbols-outlined text-3xl">manage_accounts</span>
            </div>
            <h3 class="text-2xl font-black text-slate-800">Thông tin tài khoản</h3>
            <p class="text-xs font-medium text-slate-500 mt-1 uppercase tracking-widest" v-if="form.id">ID: #{{ form.id }}</p>
          </div>
          <button @click="$emit('close')" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-slate-100 transition-colors">
            <span class="material-symbols-outlined text-slate-400">close</span>
          </button>
        </div>

        <!-- Form Body -->
        <form @submit.prevent="handleSubmit" autocomplete="off" class="overflow-y-auto hide-scrollbar flex-1 p-8 pt-2 space-y-6">
          <div class="space-y-4">
            <div>
              <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Họ và Tên {{ form.role === 'USER' ? 'khách hàng' : 'nhân viên' }}</label>
              <input 
                v-model="form.fullName" 
                type="text" 
                required
                :disabled="!isCreateMode"
                class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary focus:ring-4 focus:ring-primary/5 outline-none transition-all disabled:opacity-50"
              />
            </div>

            <div>
              <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Số điện thoại liên hệ</label>
              <input 
                v-model="form.phone" 
                type="tel" 
                required
                :disabled="!isCreateMode"
                class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary focus:ring-4 focus:ring-primary/5 outline-none transition-all font-mono disabled:opacity-50"
              />
            </div>

            <div v-if="form.role === 'USER' || form.role === 'ADMIN'">
              <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Địa chỉ Email (Tuỳ chọn)</label>
              <input 
                v-model="form.email" 
                type="email" 
                autocomplete="off"
                data-lpignore="true"
                :disabled="!isCreateMode && form.role === 'USER'"
                class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary focus:ring-4 focus:ring-primary/5 outline-none transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Mật khẩu {{ isCreateMode ? '(Mặc định: 123456)' : '(Để trống nếu không đổi)' }}</label>
              <input 
                v-model="form.password" 
                type="password" 
                autocomplete="new-password"
                data-lpignore="true"
                :disabled="!isCreateMode && form.role === 'USER'"
                :placeholder="(!isCreateMode && form.role === 'USER') ? 'Không thể đổi mật khẩu khách hàng' : (isCreateMode ? 'Nhập mật khẩu hoặc để trống' : 'Nhập mật khẩu mới')"
                class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary focus:ring-4 focus:ring-primary/5 outline-none transition-all font-mono disabled:opacity-50 disabled:cursor-not-allowed"
              />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Phân quyền</label>
                <select 
                  v-model="form.role" 
                  class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary outline-none transition-all cursor-pointer appearance-none"
                >
                  <option value="USER">Khách hàng</option>
                  <option value="ADMIN">Quản trị viên</option>
                  <option value="INSPECTOR">Lơ xe (Soát vé)</option>
                  <option value="DRIVER">Tài xế (Lái xe)</option>
                </select>
              </div>
              <div v-if="form.role === 'USER'">
                <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Số dư ví (đ)</label>
                <input 
                  v-model.number="form.walletBalance" 
                  type="number" 
                  step="any"
                  required
                  class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-black text-emerald-600 focus:bg-white focus:border-emerald-500 outline-none transition-all"
                />
              </div>
            </div>
          </div>

          <!-- Image Upload (3x4 ratio) - Chỉ hiển thị cho nhân viên -->
          <div v-if="form.role === 'INSPECTOR' || form.role === 'DRIVER'" class="flex flex-col items-center mt-2 mb-4">
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
                placeholder="Hoặc dán link ảnh trực tiếp từ web..." 
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

          <!-- Footer Buttons -->
          <div class="flex gap-3 pt-4">
            <button 
              type="button" 
              @click="$emit('close')" 
              class="flex-1 px-6 py-4 rounded-2xl text-sm font-black text-slate-500 hover:bg-slate-100 transition-all active:scale-95"
            >
              HỦY BỎ
            </button>
            <button 
              type="submit" 
              :disabled="submitting"
              class="flex-[2] bg-slate-900 text-white px-6 py-4 rounded-2xl text-sm font-black shadow-lg shadow-slate-200 hover:bg-slate-800 transition-all active:scale-95 disabled:opacity-50"
            >
              {{ submitting ? 'ĐANG LƯU...' : 'CẬP NHẬT' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const props = defineProps({
  isOpen: Boolean,
  isCreateMode: Boolean,
  form: Object,
  submitting: Boolean
});

const emit = defineEmits(['close', 'submit']);

const handleSubmit = () => {
  emit('submit');
};


const fileInput = ref(null);
const uploading = ref(false);
const imageUrlInput = ref('');

const handleFileUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  uploading.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset'); // Cloudinary preset

  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    // Cập nhật form.avatarUrl
    props.form.avatarUrl = res.data.secure_url;
  } catch (err) {
    console.error(err);
    alert("Lỗi upload ảnh. Vui lòng thử lại!");
  } finally {
    uploading.value = false;
    e.target.value = ''; // Reset input
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
    props.form.avatarUrl = res.data.secure_url;
    imageUrlInput.value = '';
  } catch (err) {
    console.error(err);
    alert("Lỗi tải ảnh từ URL. Đảm bảo link ảnh hiển thị hợp lệ và có quyền truy cập công khai.");
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
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes scaleUp {
  from { transform: scale(0.92); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}
</style>
