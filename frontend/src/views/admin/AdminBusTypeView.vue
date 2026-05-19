<template>
  <div class="p-container-margin md:p-8 max-w-6xl mx-auto pb-24 md:pb-8 relative animate-fade-in">
    
    <!-- Header Panel -->
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4 border-b border-outline-variant/20 pb-6">
      <div>
        <h2 class="text-headline-lg font-headline-lg font-black text-on-background flex items-center gap-3">
          <span class="material-symbols-outlined text-4xl text-primary">local_shipping</span>
          Danh Mục Dòng Xe
        </h2>
        <p class="text-body-lg font-body-lg text-on-surface-variant">Quản lý các loại cấu hình xe (Số ghế, Tiện ích) dùng chung cho toàn hệ thống</p>
      </div>
      
      <button 
        @click="openCreateModal"
        class="bg-primary text-on-primary hover:bg-surface-tint hover:shadow-lg active:scale-95 px-6 py-3 rounded-2xl font-black tracking-wide shadow-md transition-all flex items-center gap-2 shrink-0"
      >
        <span class="material-symbols-outlined">add_box</span>
        THÊM DÒNG XE MỚI
      </button>
    </div>

    <!-- Stats & List Container -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      
      <!-- Left Sidebar Stats Grid -->
      <div class="lg:col-span-4 space-y-4">
        <div class="bg-surface-container-low p-6 rounded-3xl border border-outline-variant/20 flex items-center gap-4 shadow-[0px_8px_24px_rgba(0,0,0,0.02)]">
          <div class="w-14 h-14 bg-primary/10 text-primary rounded-2xl flex items-center justify-center shrink-0">
            <span class="material-symbols-outlined text-3xl font-black">directions_bus</span>
          </div>
          <div>
            <span class="text-on-surface-variant font-black text-label-sm uppercase tracking-wider">Tổng loại cấu hình</span>
            <p class="text-4xl font-black text-on-surface mt-0.5">{{ busTypes.length }}</p>
          </div>
        </div>

        <div class="bg-white p-6 rounded-3xl border border-outline-variant/25 shadow-sm relative overflow-hidden">
          <div class="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full translate-x-10 -translate-y-10"></div>
          <h4 class="font-black text-body-lg text-primary flex items-center gap-2 mb-2">
            <span class="material-symbols-outlined">help</span>
            Cách thức hoạt động?
          </h4>
          <p class="text-body-md text-on-surface-variant leading-relaxed">
            Các dòng xe được bạn khai báo tại đây (Ví dụ: <i>Limousine VIP 21 Phòng</i>) sẽ **tự động đồng bộ hóa** sang các ô lựa chọn trên trang **Tạo chuyến xe** và **Quản lý hạm đội**. 
            <br><br>
            Giúp bạn nhập liệu cực nhanh và chuẩn hóa thông số ghế ngồi!
          </p>
        </div>
      </div>

      <!-- Main Data Grid Section -->
      <div class="lg:col-span-8">
        
        <!-- Loading Skeleton -->
        <div v-if="loading" class="bg-white rounded-3xl p-12 text-center border border-outline-variant/20 shadow-sm">
          <div class="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
          <span class="text-body-md font-bold text-on-surface-variant">Đang truy xuất danh mục xe...</span>
        </div>

        <!-- Empty State -->
        <div v-else-if="busTypes.length === 0" class="bg-white rounded-3xl p-12 text-center border-2 border-dashed border-outline-variant/50">
          <div class="w-20 h-20 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-4">
            <span class="material-symbols-outlined text-5xl">airport_shuttle</span>
          </div>
          <h3 class="text-headline-sm font-black text-on-surface mb-1">Chưa có dòng xe nào!</h3>
          <p class="text-body-md text-on-surface-variant mb-6">Hệ thống cần tối thiểu 1 dòng xe mẫu để bắt đầu vận hành khai thác.</p>
          <button @click="openCreateModal" class="px-6 py-2.5 bg-primary text-on-primary rounded-xl font-black hover:shadow-md transition-all">
            KHỞI TẠO DÒNG ĐẦU TIÊN 🚀
          </button>
        </div>

        <!-- Main Catalog Cards Grid -->
        <div v-else class="space-y-4">
          <div 
            v-for="item in busTypes" 
            :key="item.id"
            class="bg-white rounded-2xl border border-outline-variant/25 hover:border-primary/50 hover:shadow-[0px_12px_32px_rgba(0,0,0,0.04)] p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all group"
          >
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-slate-100 to-slate-200 text-on-surface-variant border border-outline-variant/20 flex items-center justify-center group-hover:scale-110 group-hover:from-primary/10 group-hover:to-primary/20 group-hover:text-primary transition-all shrink-0">
                <span class="material-symbols-outlined">airline_seat_recline_extra</span>
              </div>
              <div>
                <h4 class="font-black text-headline-sm text-on-surface group-hover:text-primary transition-colors">{{ item.name }}</h4>
                <div class="flex items-center gap-3 mt-1 text-body-md text-on-surface-variant font-medium">
                  <span class="bg-secondary-container text-on-secondary-container text-[11px] px-2.5 py-0.5 rounded-full font-black">
                    {{ item.seatCount || 24 }} GHẾ TIÊU CHUẨN
                  </span>
                  <span v-if="item.description" class="truncate max-w-[250px] text-[12px]">● {{ item.description }}</span>
                </div>
              </div>
            </div>

            <!-- Controls -->
            <div class="flex items-center gap-2 shrink-0 border-t sm:border-t-0 pt-3 sm:pt-0 border-outline-variant/20">
              <button 
                @click="openEditModal(item)"
                class="flex items-center justify-center w-10 h-10 border border-outline-variant/50 hover:bg-primary/5 hover:text-primary hover:border-primary rounded-xl transition-all text-on-surface-variant"
                title="Sửa"
              >
                <span class="material-symbols-outlined text-sm">edit</span>
              </button>
              <button 
                @click="handleDelete(item)"
                class="flex items-center justify-center w-10 h-10 border border-error/30 hover:bg-error hover:text-white hover:border-error rounded-xl transition-all text-error"
                title="Xóa"
              >
                <span class="material-symbols-outlined text-sm">delete</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- =============================================================== -->
    <!-- TELEPORT MODAL: THÊM / SỬA DÒNG XE CATOLOG                      -->
    <!-- =============================================================== -->
    <Teleport to="body">
      <div 
        v-if="isModalOpen" 
        class="fixed inset-0 z-[9999] overflow-y-auto flex items-center justify-center px-4 py-6 bg-black/60 backdrop-blur-sm animate-fade-in"
      >
        <!-- Modal Container Box -->
        <div class="bg-white rounded-3xl w-full max-w-md shadow-[0px_24px_64px_rgba(0,0,0,0.3)] border border-outline-variant/30 animate-scale-up flex flex-col max-h-[90vh] overflow-hidden">
          
          <!-- Header -->
          <div :class="[
            'p-5 text-white flex justify-between items-center shrink-0 transition-all duration-300',
            isEditMode ? 'bg-surface-tint' : 'bg-primary'
          ]">
            <h3 class="text-headline-sm font-headline-sm font-black tracking-wide uppercase flex items-center gap-2">
              <span>{{ isEditMode ? '✏️' : '🛠️' }}</span>
              {{ isEditMode ? 'CẬP NHẬT DÒNG XE' : 'KHAI BÁO DÒNG XE' }}
            </h3>
            <button 
              @click="closeModal" 
              class="text-white/70 hover:text-white bg-white/10 hover:bg-white/25 p-1.5 rounded-full transition-all"
            >
              <span class="material-symbols-outlined text-sm">close</span>
            </button>
          </div>

          <!-- Form Container (Cuộn nội bộ) -->
          <form @submit.prevent="handleSubmit" class="flex-1 overflow-y-auto p-6 space-y-5">
            
            <!-- Tên Dòng Xe -->
            <div class="space-y-1.5">
              <label class="text-label-md font-black text-on-surface-variant uppercase flex items-center gap-1">
                Tên Dòng Xe / Catalog
              </label>
              <input 
                v-model="form.name" 
                type="text" 
                required
                placeholder="Ví dụ: Limousine VIP 21 Phòng"
                class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-3 focus:outline-none font-bold transition-colors text-lg"
              />
            </div>

            <!-- Số Lượng Ghế Mặc Định -->
            <div class="space-y-1.5">
              <label class="text-label-md font-black text-on-surface-variant uppercase">Số Lượng Ghế Cấu Hình</label>
              <div class="relative">
                <input 
                  v-model.number="form.seatCount" 
                  type="number" 
                  min="1"
                  max="100"
                  required
                  class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-3 pr-12 focus:outline-none font-bold transition-colors"
                />
                <span class="absolute right-4 top-1/2 -translate-y-1/2 font-black text-sm text-on-surface-variant">GHẾ</span>
              </div>
              <p class="text-[11px] text-on-surface-variant mt-1">ℹ️ Số lượng ghế tiêu chuẩn trên thân xe để hệ thống sắp xếp sơ đồ.</p>
            </div>

            <!-- Mô tả nhanh -->
            <div class="space-y-1.5">
              <label class="text-label-md font-black text-on-surface-variant uppercase">Mô tả tiện ích (Tùy chọn)</label>
              <textarea 
                v-model="form.description" 
                rows="2"
                placeholder="Có tủ lạnh mini, cổng sạc USB, tai nghe bluetooth..."
                class="w-full border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-3 focus:outline-none font-medium transition-colors resize-none"
              ></textarea>
            </div>

            <div class="space-y-1.5">
              <label class="text-label-md font-black text-on-surface-variant uppercase flex items-center gap-1">
                <span class="material-symbols-outlined text-sm">image</span>
                Hình Ảnh Minh Họa Dòng Xe
              </label>
              <div class="flex gap-2">
                <input 
                  v-model="form.imageUrl" 
                  type="url" 
                  placeholder="Dán link ảnh hoặc tải lên..."
                  class="flex-1 border-2 border-outline-variant/50 focus:border-primary rounded-xl px-4 py-3 focus:outline-none font-bold transition-colors"
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
                <img :src="form.imageUrl" class="w-full h-full object-cover" @error="(e) => e.target.style.display = 'none'" />
              </div>
            </div>
          </form>

          <!-- Footer Controls -->
          <div class="p-5 border-t border-outline-variant/20 flex items-center justify-end gap-3 bg-surface-bright shrink-0">
            <button 
              type="button"
              @click="closeModal"
              class="px-5 py-2 border-2 border-outline-variant hover:bg-surface-container text-on-surface-variant rounded-xl font-bold transition-all"
            >
              Hủy bỏ
            </button>
            <button 
              @click="handleSubmit"
              type="button"
              class="px-8 py-3 bg-primary text-on-primary hover:bg-surface-tint hover:shadow-md active:scale-95 rounded-xl font-black tracking-wide transition-all flex items-center gap-2"
            >
              <span class="material-symbols-outlined text-sm">save_as</span>
              {{ isEditMode ? 'CẬP NHẬT NGAY' : 'LƯU DANH MỤC' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
    <!-- Hidden File Input for Image Upload -->
    <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleImageUpload" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useApi } from '@/composables/useApi';

const api = useApi();
const fileInput = ref(null);
const uploading = ref(false);

const busTypes = ref([]);
const loading = ref(true);

const isModalOpen = ref(false);
const isEditMode = ref(false);
const form = ref({
  id: null,
  name: '',
  seatCount: 24,
  description: '',
  imageUrl: ''
});

const fetchBusTypes = async () => {
  loading.value = true;
  try {
    const response = await api.get('/bus-types');
    busTypes.value = response.data;
  } catch (error) {
    console.error("Lỗi fetch danh mục dòng xe:", error);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  isEditMode.value = false;
  form.value = { id: null, name: '', seatCount: 24, description: '', imageUrl: '' };
  isModalOpen.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.value = { ...item };
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const handleSubmit = async () => {
  if (!form.value.name || !form.value.seatCount) {
    alert("Vui lòng nhập đầy đủ Tên dòng xe và Số ghế!");
    return;
  }

  try {
    if (isEditMode.value) {
      await api.put(`/bus-types/${form.value.id}`, form.value);
    } else {
      await api.post('/bus-types', form.value);
    }
    closeModal();
    fetchBusTypes();
  } catch (error) {
    console.error("Lỗi khi lưu dòng xe:", error);
    alert("Lỗi hệ thống: Trùng lặp Tên dòng xe hoặc lỗi Database!");
  }
};

const handleImageUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  
  uploading.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset'); // Sử dụng chung preset
  
  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    form.value.imageUrl = res.data.secure_url;
  } catch (err) {
    console.error("Lỗi tải ảnh Cloudinary:", err);
    alert("Không thể tải ảnh lên! Vui lòng kiểm tra kết nối mạng.");
  } finally {
    uploading.value = false;
  }
};

const handleDelete = async (item) => {
  if (confirm(`Bạn có thực sự muốn XÓA dòng xe [${item.name}] khỏi catalog vĩnh viễn không?`)) {
    try {
      await api.delete(`/bus-types/${item.id}`);
      fetchBusTypes();
    } catch (error) {
      console.error("Lỗi xóa dòng xe:", error);
      alert("Không thể xóa dòng xe này vì có thể nó đang được sử dụng!");
    }
  }
};

onMounted(() => {
  fetchBusTypes();
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.97); }
  to { opacity: 1; transform: scale(1); }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}
.animate-scale-up {
  animation: scaleUp 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
