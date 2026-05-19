<template>
  <div class="p-6 space-y-6 animate-fade-in">
    <!-- Header Bar -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
      <div>
        <h1 class="text-headline-md font-black flex items-center gap-2 text-slate-900">
          <span class="material-symbols-outlined text-primary text-[32px]">groups</span>
          Quản lý Người Dùng
        </h1>
        <p class="text-body-md text-on-surface-variant mt-1">Xem thông tin, phân quyền, và quản lý số dư ví của toàn bộ khách hàng.</p>
      </div>
      <!-- Search box -->
      <div class="relative w-full md:w-80 shadow-sm">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="Tìm theo SĐT hoặc Họ tên..." 
          class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-2xl text-body-md font-bold focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
        />
      </div>
    </div>

    <!-- Main Table Grid Container -->
    <div class="bg-white rounded-3xl border border-slate-100 shadow-[0px_8px_24px_rgba(0,0,0,0.02)] overflow-hidden relative">
      
      <!-- Loading overlay -->
      <div v-if="loading" class="min-h-[400px] flex flex-col items-center justify-center">
        <div class="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mb-2"></div>
        <p class="text-body-sm font-bold text-slate-500 animate-pulse">Đang tải danh sách người dùng...</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full border-collapse text-left min-w-[800px]">
          <thead>
            <tr class="bg-slate-50 text-[11px] font-black uppercase tracking-wider text-slate-500 border-b border-slate-100">
              <th class="px-6 py-4">ID</th>
              <th class="px-6 py-4">Họ và Tên</th>
              <th class="px-6 py-4">Số điện thoại</th>
              <th class="px-6 py-4">Quyền hạn (Role)</th>
              <th class="px-6 py-4 text-right">Số dư Ví SkyPay</th>
              <th class="px-6 py-4 text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr 
              v-for="user in filteredUsers" 
              :key="user.id"
              class="hover:bg-slate-50/50 transition-colors duration-150"
            >
              <!-- ID -->
              <td class="px-6 py-4">
                <span class="text-body-sm font-bold text-slate-400">#{{ user.id }}</span>
              </td>

              <!-- Avatar + Name -->
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <img 
                    :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=f1f5f9&color=64748b&bold=true`" 
                    alt="Avatar" 
                    class="w-9 h-9 rounded-full shadow-sm"
                  />
                  <span class="text-body-md font-black text-slate-800">{{ user.fullName }}</span>
                </div>
              </td>

              <!-- Phone -->
              <td class="px-6 py-4">
                <span class="text-body-md font-bold text-slate-600">{{ user.phone }}</span>
              </td>

              <!-- Role Badge -->
              <td class="px-6 py-4">
                <span 
                  :class="[
                    'px-3 py-1 rounded-full text-[10px] font-black tracking-wider uppercase inline-block border shadow-sm',
                    user.role === 'ADMIN' 
                      ? 'bg-red-50 text-red-700 border-red-100' 
                      : 'bg-blue-50 text-blue-700 border-blue-100'
                  ]"
                >
                  {{ user.role }}
                </span>
              </td>

              <!-- Wallet -->
              <td class="px-6 py-4 text-right">
                <span class="text-body-md font-black text-emerald-600">
                  {{ user.walletBalance ? user.walletBalance.toLocaleString('vi-VN') : '0' }} đ
                </span>
              </td>

              <!-- Action Controls -->
              <td class="px-6 py-4">
                <div class="flex items-center justify-center gap-2">
                  <button 
                    @click="openEditModal(user)"
                    class="p-2 bg-slate-100 hover:bg-primary hover:text-white text-slate-600 rounded-xl transition-all active:scale-90 shadow-sm"
                    title="Sửa thông tin & Nạp tiền"
                  >
                    <span class="material-symbols-outlined text-sm">edit</span>
                  </button>
                  <button 
                    @click="handleDelete(user.id)"
                    class="p-2 bg-red-50 hover:bg-red-500 hover:text-white text-red-500 rounded-xl transition-all active:scale-90 shadow-sm"
                    title="Xoá người dùng"
                  >
                    <span class="material-symbols-outlined text-sm">delete</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Empty Filter State -->
        <div v-if="filteredUsers.length === 0" class="p-12 text-center">
          <span class="material-symbols-outlined text-5xl text-slate-300 animate-pulse">search_off</span>
          <p class="text-body-md font-bold text-slate-500 mt-2">Không tìm thấy người dùng nào phù hợp.</p>
        </div>
      </div>
    </div>

    <!-- 📲 EDIT & WALLET MODAL WINDOW (SỬ DỤNG TELEPORT ĐỂ CHỐNG VỠ LAYOUT) -->
    <Teleport to="body">
      <div v-if="isModalOpen" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div @click="closeModal" class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fade-in"></div>
        
        <!-- Modal Content -->
        <div class="bg-white w-full max-w-md rounded-[32px] shadow-[0_32px_64px_-12px_rgba(0,0,0,0.2)] border border-slate-100 overflow-hidden animate-scale-up relative flex flex-col">
          <!-- Header -->
          <div class="p-8 pb-4 flex justify-between items-start">
            <div>
              <div class="w-12 h-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-4">
                <span class="material-symbols-outlined text-3xl">manage_accounts</span>
              </div>
              <h3 class="text-2xl font-black text-slate-800">Thông tin tài khoản</h3>
              <p class="text-xs font-medium text-slate-500 mt-1 uppercase tracking-widest">ID: #{{ editForm.id }}</p>
            </div>
            <button @click="closeModal" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-slate-100 transition-colors">
              <span class="material-symbols-outlined text-slate-400">close</span>
            </button>
          </div>
  
          <!-- Form Body -->
          <form @submit.prevent="submitEdit" class="p-8 pt-2 space-y-6">
            <div class="space-y-4">
              <div>
                <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Họ và Tên khách hàng</label>
                <input 
                  v-model="editForm.fullName" 
                  type="text" 
                  required
                  class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary focus:ring-4 focus:ring-primary/5 outline-none transition-all"
                />
              </div>
  
              <div>
                <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Số điện thoại liên hệ</label>
                <input 
                  v-model="editForm.phone" 
                  type="tel" 
                  required
                  class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary focus:ring-4 focus:ring-primary/5 outline-none transition-all font-mono"
                />
              </div>
  
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Phân quyền</label>
                  <select 
                    v-model="editForm.role" 
                    class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-800 focus:bg-white focus:border-primary outline-none transition-all cursor-pointer appearance-none"
                  >
                    <option value="USER">Khách hàng</option>
                    <option value="ADMIN">Quản trị viên</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2 ml-1">Số dư ví (đ)</label>
                  <input 
                    v-model.number="editForm.walletBalance" 
                    type="number" 
                    step="any"
                    required
                    class="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-black text-emerald-600 focus:bg-white focus:border-emerald-500 outline-none transition-all"
                  />
                </div>
              </div>
            </div>
  
            <!-- Footer Buttons -->
            <div class="flex gap-3 pt-4">
              <button 
                type="button" 
                @click="closeModal" 
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();
const users = ref([]);
const loading = ref(true);
const searchQuery = ref('');
const isModalOpen = ref(false);
const submitting = ref(false);

// State Form Edit
const editForm = ref({
  id: null,
  fullName: '',
  phone: '',
  role: 'USER',
  walletBalance: 0
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await api.get('/users');
    users.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy danh sách người dùng:", error);
  } finally {
    loading.value = false;
  }
};

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value;
  const q = searchQuery.value.toLowerCase();
  return users.value.filter(u => 
    u.fullName.toLowerCase().includes(q) || 
    u.phone.includes(q)
  );
});

const openEditModal = (user) => {
  editForm.value = { ...user };
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const submitEdit = async () => {
  submitting.value = true;
  try {
    await api.put(`/users/${editForm.value.id}`, editForm.value);
    await fetchUsers(); // Reload danh sách mới
    closeModal();
  } catch (error) {
    console.error("Lỗi cập nhật người dùng:", error);
    alert("Cập nhật thất bại, vui lòng kiểm tra lại!");
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (id) => {
  if (confirm("⚠️ BẠN CÓ CHẮC CHẮN MUỐN XÓA NGƯỜI DÙNG NÀY KHÔNG?\nHành động này không thể khôi phục!")) {
    try {
      await api.delete(`/users/${id}`);
      await fetchUsers();
    } catch (error) {
      console.error("Lỗi xoá người dùng:", error);
      alert("Không thể xoá người dùng! Vui lòng thử lại.");
    }
  }
};

onMounted(() => {
  fetchUsers();
});
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
