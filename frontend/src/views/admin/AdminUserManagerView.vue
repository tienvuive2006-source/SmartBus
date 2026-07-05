<template>
  <div class="p-6 space-y-6">
    <!-- Header Bar -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-2">
      <div>
        <h1 class="text-headline-md font-black flex items-center gap-2 text-slate-900">
          <span class="material-symbols-outlined text-primary text-[32px]">{{ pageIcon }}</span>
          {{ pageTitle }}
        </h1>
        <p class="text-body-md text-on-surface-variant mt-1">{{ pageSubtitle }}</p>
      </div>
      <!-- Actions -->
      <div class="flex items-center gap-3 w-full md:w-auto">
        <div class="relative flex-1 md:w-80 shadow-sm">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm theo SĐT hoặc Họ tên..." 
            class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-2xl text-body-md font-bold focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
          />
        </div>
        <button 
          @click="openCreateModal"
          class="shrink-0 bg-slate-900 hover:bg-slate-800 text-white px-5 py-2.5 rounded-2xl font-bold flex items-center gap-2 transition-all shadow-sm"
        >
          <span class="material-symbols-outlined text-[20px]">person_add</span>
          <span class="hidden sm:inline">Thêm mới</span>
        </button>
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
        <!-- Components Data Tables -->
        <UserTable 
          v-if="activeTab === 'users' || activeTab === 'admin'"
          :users="filteredUsers"
          @history="openHistoryModal"
          @edit="openEditModal"
          @toggle-lock="toggleLock"
          @delete="handleDelete"
        />

        <StaffTable 
          v-if="activeTab === 'inspector' || activeTab === 'driver'"
          :users="filteredUsers"
          @edit="openEditModal"
          @toggle-lock="toggleLock"
          @delete="handleDelete"
        />

        <!-- Empty Filter State -->
        <div v-if="filteredUsers.length === 0" class="p-12 text-center">
          <span class="material-symbols-outlined text-5xl text-slate-300 animate-pulse">search_off</span>
          <p class="text-body-md font-bold text-slate-500 mt-2">Không tìm thấy người dùng nào phù hợp.</p>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <UserDeleteModal 
      :isOpen="userToDelete !== null"
      :isDeleting="isDeleting"
      @close="cancelDelete"
      @confirm="confirmDelete"
    />

    <UserEditModal 
      :isOpen="isModalOpen"
      :isCreateMode="isCreateMode"
      :form="editForm"
      :submitting="submitting"
      @close="closeModal"
      @submit="submitEdit"
    />

    <UserHistoryModal 
      :isOpen="isHistoryModalOpen"
      :user="historyUser"
      :bookings="userBookings"
      :loading="loadingHistory"
      @close="closeHistoryModal"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useApi } from '@/composables/useApi';

import UserTable from '@/components/admin/user/UserTable.vue';
import StaffTable from '@/components/admin/user/StaffTable.vue';
import UserEditModal from '@/components/admin/user/UserEditModal.vue';
import UserHistoryModal from '@/components/admin/user/UserHistoryModal.vue';
import UserDeleteModal from '@/components/admin/user/UserDeleteModal.vue';
import { useRoute, useRouter } from 'vue-router';

const api = useApi();
const route = useRoute();
const router = useRouter();
const users = ref([]);
const loading = ref(true);
const searchQuery = ref('');
const activeTab = ref(route.query.tab || 'users');

const pageTitle = computed(() => {
  switch (activeTab.value) {
    case 'users': return 'Quản lý Khách Hàng';
    case 'driver': return 'Quản lý Tài Xế';
    case 'inspector': return 'Quản lý Lơ Xe / Phụ Xe';
    case 'admin': return 'Danh sách Quản Trị Viên';
    default: return 'Quản lý Người Dùng';
  }
});

const pageSubtitle = computed(() => {
  switch (activeTab.value) {
    case 'users': return 'Xem thông tin, phân quyền, và quản lý số dư ví của khách hàng.';
    case 'driver': return 'Quản lý hồ sơ tài xế và theo dõi lịch sử phân công chuyến xe.';
    case 'inspector': return 'Quản lý hồ sơ nhân viên soát vé và lơ xe.';
    case 'admin': return 'Quản lý các tài khoản có quyền quản trị tối cao trên hệ thống.';
    default: return 'Quản lý danh sách người dùng trên hệ thống.';
  }
});

const pageIcon = computed(() => {
  switch (activeTab.value) {
    case 'users': return 'groups';
    case 'driver': return 'local_taxi';
    case 'inspector': return 'badge';
    case 'admin': return 'admin_panel_settings';
    default: return 'groups';
  }
});

// Sync URL to activeTab
watch(() => route.query.tab, (newTab) => {
  if (newTab && newTab !== activeTab.value) {
    activeTab.value = newTab;
  }
});

// Sync activeTab to URL
watch(activeTab, (newTab) => {
  if (route.query.tab !== newTab) {
    router.replace({ query: { ...route.query, tab: newTab } });
  }
});

const isModalOpen = ref(false);
const isCreateMode = ref(false);
const submitting = ref(false);

const isHistoryModalOpen = ref(false);
const historyUser = ref(null);
const userBookings = ref([]);
const loadingHistory = ref(false);

// State Delete Modal
const userToDelete = ref(null);
const isDeleting = ref(false);

// State Form Edit
const editForm = ref({
  id: null,
  fullName: '',
  phone: '',
  email: '',
  password: '',
  role: 'USER',
  walletBalance: 0,
  avatarUrl: ''
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const [usersRes, tripsRes, busesRes] = await Promise.all([
      api.get('/users'),
      api.get('/trips'),
      api.get('/buses')
    ]);
    
    const rawUsers = usersRes.data || [];
    const trips = tripsRes.data || [];
    const buses = busesRes.data || [];

    users.value = rawUsers.map(user => {
      let tripCount = 0;
      if (user.role === 'INSPECTOR') {
         tripCount = trips.filter(t => t.inspector?.phone === user.phone && (t.status === 'ASSIGNED' || t.status === 'PENDING' || t.status === 'IN_PROGRESS')).length;
      } else if (user.role === 'DRIVER') {
         tripCount = trips.filter(t => t.assignedDriverUsername === user.phone && (t.status === 'ASSIGNED' || t.status === 'PENDING' || t.status === 'IN_PROGRESS')).length;
      }
      return { ...user, activeTripCount: tripCount };
    });
  } catch (error) {
    console.error("Lỗi lấy danh sách người dùng:", error);
  } finally {
    loading.value = false;
  }
};

const filteredUsers = computed(() => {
  let list = users.value;

  // 1. Lọc theo Tab
  if (activeTab.value === 'users') {
    list = list.filter(u => u.role === 'USER');
  } else if (activeTab.value === 'driver') {
    list = list.filter(u => u.role === 'DRIVER');
  } else if (activeTab.value === 'inspector') {
    list = list.filter(u => u.role === 'INSPECTOR');
  } else if (activeTab.value === 'admin') {
    list = list.filter(u => u.role === 'ADMIN');
  }

  // 2. Lọc theo Search Query
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(u => 
      u.fullName.toLowerCase().includes(q) || 
      u.phone.includes(q)
    );
  }

  // 3. Sắp xếp theo ID giảm dần (Mới nhất lên đầu)
  list = list.sort((a, b) => b.id - a.id);

  return list;
});

const openEditModal = (user) => {
  isCreateMode.value = false;
  editForm.value = { ...user, phone: user.phone?.startsWith('GG_') ? '' : user.phone, password: '', email: user.email || '', avatarUrl: user.avatarUrl || '' };
  isModalOpen.value = true;
};

const openHistoryModal = async (user) => {
  historyUser.value = user;
  isHistoryModalOpen.value = true;
  loadingHistory.value = true;
  userBookings.value = [];
  try {
    const res = await api.get(`/users/${user.id}/bookings`);
    userBookings.value = res.data.sort((a, b) => b.id - a.id); // Sắp xếp theo ID giảm dần
  } catch (error) {
    console.error("Lỗi lấy lịch sử vé", error);
    alert("Không thể tải lịch sử mua vé");
  } finally {
    loadingHistory.value = false;
  }
};

const closeHistoryModal = () => {
  isHistoryModalOpen.value = false;
  historyUser.value = null;
  userBookings.value = [];
};

const openCreateModal = () => {
  isCreateMode.value = true;
  editForm.value = {
    id: null,
    fullName: '',
    phone: '',
    email: '',
    password: '',
    role: 'USER',
    walletBalance: 0,
    avatarUrl: ''
  };
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const submitEdit = async () => {
  submitting.value = true;
  try {
    if (isCreateMode.value) {
      const payloadEmail = (editForm.value.role === 'DRIVER' || editForm.value.role === 'INSPECTOR') ? '' : editForm.value.email;
      await api.post('/auth/register', {
        fullName: editForm.value.fullName,
        phone: editForm.value.phone,
        email: payloadEmail,
        password: editForm.value.password || '123456'
      });
      const response = await api.get('/users');
      const newUser = response.data.find(u => u.phone === editForm.value.phone);
      if (newUser) {
        await api.put(`/users/${newUser.id}`, {
          ...newUser,
          password: '', // Không gửi lại mật khẩu đã mã hóa từ DB để tránh lỗi mã hóa kép
          role: editForm.value.role,
          walletBalance: editForm.value.walletBalance,
          avatarUrl: editForm.value.avatarUrl
        });
      }
    } else {
      await api.put(`/users/${editForm.value.id}`, editForm.value);
    }
    await fetchUsers();
    closeModal();
  } catch (error) {
    console.error("Lỗi lưu người dùng:", error);
    const errorMsg = typeof error.response?.data === 'string' 
      ? error.response.data 
      : (error.response?.data?.message || "Thao tác thất bại, vui lòng kiểm tra lại!");
    alert(errorMsg);
  } finally {
    submitting.value = false;
  }
};

const handleDelete = (id) => {
  userToDelete.value = id;
};

const cancelDelete = () => {
  userToDelete.value = null;
};

const confirmDelete = async () => {
  if (!userToDelete.value) return;
  isDeleting.value = true;
  try {
    await api.delete(`/users/${userToDelete.value}`);
    await fetchUsers();
    userToDelete.value = null;
    alert("Xóa người dùng thành công!");
  } catch (error) {
    console.error("Lỗi xoá người dùng:", error);
    alert(error.response?.data?.message || "Không thể xoá người dùng! Vui lòng thử lại.");
  } finally {
    isDeleting.value = false;
  }
};

const toggleLock = async (user) => {
  const action = user.isLocked ? 'mở khóa' : 'khóa';
  if (!confirm(`Xác nhận ${action} tài khoản ${user.fullName}?`)) return;
  try {
    await api.put(`/users/${user.id}/lock`);
    await fetchUsers();
    alert(`${action.charAt(0).toUpperCase() + action.slice(1)} tài khoản thành công!`);
  } catch (error) {
    console.error("Lỗi khóa tài khoản:", error);
    alert(error.response?.data?.message || `Không thể ${action} tài khoản!`);
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}
.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
