<template>
  <div class="p-6 md:p-8 max-w-7xl mx-auto bg-slate-50 min-h-screen font-sans">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-2xl font-extrabold text-slate-800 tracking-tight">
          Quản lý Banner
        </h2>
        <p class="text-xs font-semibold text-slate-500 mt-1">Thay đổi ảnh bìa trang chủ dễ dàng</p>
      </div>
      <button 
        @click="fileInput.click()" 
        class="bg-[#075955] hover:bg-[#064a47] text-white px-5 py-2.5 rounded-xl shadow-sm hover:shadow-md active:scale-95 transition-all duration-200 font-bold text-xs flex items-center justify-center gap-2"
        :disabled="uploading"
      >
        <span v-if="uploading" class="material-symbols-outlined text-[18px] animate-spin">sync</span>
        <span v-else class="material-symbols-outlined text-[18px]">add_photo_alternate</span>
        {{ uploading ? 'Đang tải...' : 'Thêm Banner' }}
      </button>
      <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleFileUpload" />
    </div>

    <!-- Main List -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
        <h3 class="text-sm font-bold text-slate-800 flex items-center gap-2">
           <span class="material-symbols-outlined text-[#075955] text-[20px]">view_carousel</span>
           Danh sách Banner hiện tại
        </h3>
        <div class="flex items-center gap-2 px-3 py-1 bg-emerald-50 rounded-lg border border-emerald-100">
          <span class="text-[10px] font-bold text-emerald-700 uppercase">{{ banners.length }} banner</span>
        </div>
      </div>
      
      <div class="p-6">
        <div v-if="loading" class="flex flex-col items-center justify-center py-12">
          <div class="w-10 h-10 border-4 border-gray-200 border-t-[#075955] rounded-full animate-spin mb-3"></div>
          <p class="text-gray-400 font-bold">Đang tải dữ liệu...</p>
        </div>
        
        <div v-else-if="banners.length === 0" class="text-center py-16">
          <div class="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span class="material-symbols-outlined text-4xl text-slate-300">hide_image</span>
          </div>
          <h4 class="font-black text-slate-400 uppercase tracking-widest">Chưa có banner nào</h4>
          <p class="text-xs text-slate-400 mt-1">Trang chủ đang sử dụng ảnh mặc định.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="(banner, index) in banners" :key="index" class="group relative rounded-2xl overflow-hidden shadow-sm border border-slate-200 hover:shadow-lg transition-all">
            <img :src="banner.url" class="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-500" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
            
            <div class="absolute top-3 left-3 bg-black/50 backdrop-blur-md text-white text-[10px] font-bold px-3 py-1 rounded-full border border-white/20">
              #{{ index + 1 }}
            </div>
            
            <div class="absolute top-3 right-3 flex items-center gap-2 z-10">
              <button @click="toggleBanner(index)" :class="banner.isActive !== false ? 'bg-emerald-500 hover:bg-emerald-600' : 'bg-slate-400 hover:bg-slate-500'" class="w-8 h-8 rounded-full text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all shadow-md transform translate-y-2 group-hover:translate-y-0" :title="banner.isActive !== false ? 'Đang bật - Bấm để tắt' : 'Đang tắt - Bấm để bật'">
                <span class="material-symbols-outlined text-sm">{{ banner.isActive !== false ? 'visibility' : 'visibility_off' }}</span>
              </button>
              <button @click="deleteBanner(index)" class="w-8 h-8 rounded-full bg-rose-500 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-rose-600 transition-all shadow-md transform translate-y-2 group-hover:translate-y-0" title="Xóa">
                <span class="material-symbols-outlined text-sm">delete</span>
              </button>
            </div>
            
            <div class="absolute inset-0 bg-black/40 flex items-center justify-center transition-opacity pointer-events-none" :class="banner.isActive === false ? 'opacity-100' : 'opacity-0'">
              <span class="bg-black/80 text-white px-4 py-2 rounded-xl text-xs font-black uppercase tracking-widest border border-white/20">Đã tắt ẩn</span>
            </div>
            
            <div class="absolute bottom-3 left-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity transform translate-y-2 group-hover:translate-y-0 z-20">
              <p class="text-white text-xs font-semibold truncate drop-shadow-md">{{ banner.url }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';
import axios from 'axios';

const api = useApi();
const banners = ref([]);
const loading = ref(true);
const uploading = ref(false);
const fileInput = ref(null);

const SETTING_KEY = 'HERO_BANNERS';

const fetchBanners = async () => {
  loading.value = true;
  try {
    const res = await api.get(`/settings/${SETTING_KEY}`);
    if (res.data && res.data.value) {
      banners.value = JSON.parse(res.data.value);
    } else {
      banners.value = [];
    }
  } catch (err) {
    console.error("Lỗi tải banner:", err);
    banners.value = [];
  } finally {
    loading.value = false;
  }
};

const saveBanners = async () => {
  try {
    await api.put(`/settings/${SETTING_KEY}`, { value: JSON.stringify(banners.value) });
  } catch (err) {
    console.error("Lỗi lưu banner:", err);
    alert("Không thể lưu thay đổi vào máy chủ!");
  }
};

const handleFileUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  uploading.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset');

  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    const newUrl = res.data.secure_url;
    
    // Thêm vào danh sách và lưu
    banners.value.push({ url: newUrl, isActive: true });
    await saveBanners();
    
    // Đồng thời cập nhật cái ảnh bìa cũ (hero_banner_url) để tương thích ngược nếu HomeView vẫn dùng
    await api.put('/settings/hero_banner_url', { value: newUrl });
    
    alert("Thêm banner thành công!");
  } catch (err) {
    console.error(err);
    alert("Lỗi upload ảnh, vui lòng thử lại.");
  } finally {
    uploading.value = false;
    e.target.value = '';
  }
};

const deleteBanner = async (index) => {
  if (confirm("Bạn có chắc chắn muốn xóa banner này?")) {
    banners.value.splice(index, 1);
    await saveBanners();
    
    // Nếu mảng còn ảnh thì cập nhật ảnh bìa cũ về ảnh đầu tiên đang bật
    const activeBanners = banners.value.filter(b => b.isActive !== false);
    const fallbackUrl = activeBanners.length > 0 ? activeBanners[0].url : '';
    await api.put('/settings/hero_banner_url', { value: fallbackUrl });
  }
};

const toggleBanner = async (index) => {
  const currentStatus = banners.value[index].isActive !== false;
  banners.value[index].isActive = !currentStatus;
  await saveBanners();
};

onMounted(() => {
  fetchBanners();
});
</script>
