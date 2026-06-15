<template>
  <div class="min-h-screen bg-[#f4f7f6] font-sans text-slate-800 pb-20">
    <!-- Navigation Bar -->
    <nav class="bg-white text-slate-800 border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-3 cursor-pointer" @click="$router.push('/')">
          <div class="w-10 h-10 rounded-full bg-[#075955] flex items-center justify-center text-white">
            <span class="material-symbols-outlined text-xl">directions_bus</span>
          </div>
          <div class="flex flex-col">
            <span class="text-lg font-black text-gray-900 leading-none tracking-tight">Trung - Nam</span>
            <span class="text-[10px] uppercase tracking-widest font-bold text-gray-500 mt-1">Hồ sơ cá nhân</span>
          </div>
        </div>
        <div class="flex items-center gap-6">
          <button @click="$router.push('/')" class="text-[10px] font-black uppercase tracking-widest bg-gray-100 hover:bg-gray-200 text-gray-700 px-5 py-2.5 rounded-xl transition-all duration-200 hidden sm:block">
            Trang chủ
          </button>
        </div>
      </div>
    </nav>
    
    <div class="px-4 py-8 animate-fade-in">
    
    <!-- Loading State -->
    <div v-if="loading" class="max-w-4xl mx-auto min-h-[50vh] flex flex-col items-center justify-center gap-4">
      <div class="w-12 h-12 border-4 border-gray-200 border-t-[#075955] rounded-full animate-spin"></div>
      <p class="text-xs font-bold text-gray-400 uppercase tracking-widest animate-pulse">Đang tải thông tin...</p>
    </div>

    <!-- Main Profile View -->
    <main v-else-if="user" class="max-w-4xl mx-auto">
      
      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-black text-gray-900 tracking-tight">Tài khoản của bạn</h1>
        <p class="text-sm font-semibold text-gray-500 mt-2">Quản lý thông tin cá nhân, ví điện tử và các tùy chọn bảo mật.</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <!-- Left Column: Profile Card -->
        <div class="lg:col-span-1 space-y-8">
          <div class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] text-center relative overflow-hidden">
            <div class="absolute top-0 left-0 w-full h-24 bg-gradient-to-r from-emerald-50 to-teal-50"></div>
            <div class="relative z-10 flex flex-col items-center">
              <div class="relative mb-4">
                <img 
                  alt="Profile Avatar" 
                  class="w-28 h-28 rounded-full object-cover border-4 border-white shadow-md bg-white" 
                  :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=075955&color=ffffff&bold=true&size=128`"
                />
                <div class="absolute bottom-0 right-0 bg-emerald-500 border-2 border-white w-7 h-7 rounded-full flex items-center justify-center shadow-sm">
                  <span class="material-symbols-outlined text-[14px] text-white font-black">verified</span>
                </div>
              </div>
              <h2 class="text-xl font-black text-gray-900">{{ user.fullName }}</h2>
              <p class="text-sm font-semibold text-gray-500 mb-4">
                <span v-if="user.phone?.startsWith('GG_')" class="italic text-gray-400">Chưa cập nhật SĐT</span>
                <span v-else>{{ user.phone }}</span>
              </p>
              
              <span class="bg-emerald-50 text-emerald-700 text-[10px] font-black uppercase tracking-widest px-4 py-1.5 rounded-full border border-emerald-100">
                {{ user.role === 'ADMIN' ? 'Quản trị viên' : 'Thành viên VIP' }}
              </span>
            </div>
          </div>

          <!-- Quick Navigation -->
          <div class="bg-white rounded-3xl p-4 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
             <nav class="flex flex-col space-y-1">
               <button @click="$router.push('/history')" class="flex items-center gap-3 w-full p-4 rounded-2xl hover:bg-gray-50 transition-colors text-left group">
                 <span class="material-symbols-outlined text-gray-400 group-hover:text-[#075955] transition-colors">receipt_long</span>
                 <span class="text-sm font-bold text-gray-700 group-hover:text-gray-900">Lịch sử đặt vé</span>
               </button>
               <button @click="$router.push('/ai-assistant')" class="flex items-center gap-3 w-full p-4 rounded-2xl hover:bg-gray-50 transition-colors text-left group">
                 <span class="material-symbols-outlined text-gray-400 group-hover:text-[#075955] transition-colors">smart_toy</span>
                 <span class="text-sm font-bold text-gray-700 group-hover:text-gray-900">Trợ lý ảo AI</span>
               </button>
               <button class="flex items-center gap-3 w-full p-4 rounded-2xl hover:bg-gray-50 transition-colors text-left group">
                 <span class="material-symbols-outlined text-gray-400 group-hover:text-[#075955] transition-colors">notifications</span>
                 <span class="text-sm font-bold text-gray-700 group-hover:text-gray-900">Cài đặt thông báo</span>
               </button>
               
               <div class="h-[1px] bg-gray-100 my-2"></div>
               
               <button @click="handleLogout" class="flex items-center gap-3 w-full p-4 rounded-2xl hover:bg-red-50 transition-colors text-left group">
                 <span class="material-symbols-outlined text-red-400 group-hover:text-red-600 transition-colors">logout</span>
                 <span class="text-sm font-bold text-red-500 group-hover:text-red-600">Đăng xuất</span>
               </button>
             </nav>
          </div>
        </div>

        <!-- Right Column: Details & Wallet -->
        <div class="lg:col-span-2 space-y-8">
          
          <!-- Personal Information -->
          <section class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
            <div class="flex justify-between items-center mb-6">
              <h3 class="text-sm font-black text-gray-900 uppercase tracking-widest">Thông tin cá nhân</h3>
              <div class="flex gap-2">
                <button v-if="isEditing" @click="isEditing = false" class="text-[10px] font-black text-gray-500 uppercase tracking-widest bg-gray-100 px-3 py-1.5 rounded-lg hover:bg-gray-200 transition-colors">
                  Hủy
                </button>
                <button v-if="isEditing" @click="saveProfile" class="text-[10px] font-black text-white uppercase tracking-widest bg-emerald-600 px-4 py-1.5 rounded-lg hover:bg-emerald-700 transition-colors shadow-sm">
                  Lưu
                </button>
                <button v-else @click="startEdit" class="text-[10px] font-black text-[#075955] uppercase tracking-widest bg-emerald-50 px-3 py-1.5 rounded-lg hover:bg-emerald-100 transition-colors">
                  Chỉnh sửa
                </button>
              </div>
            </div>
            
            <div class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5">Họ và tên</p>
                  <input v-if="isEditing" v-model="editForm.fullName" type="text" class="w-full text-sm font-black text-gray-900 bg-white p-3 rounded-xl border border-emerald-300 focus:ring-2 focus:ring-emerald-500 outline-none transition-all" />
                  <p v-else class="text-sm font-black text-gray-900 bg-gray-50 p-3 rounded-xl border border-gray-100">{{ user.fullName }}</p>
                </div>
                <div>
                  <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5">Số điện thoại</p>
                  <input v-if="isEditing" v-model="editForm.phone" type="text" class="w-full text-sm font-black text-gray-900 bg-white p-3 rounded-xl border border-emerald-300 focus:ring-2 focus:ring-emerald-500 outline-none transition-all" />
                  <p v-else class="text-sm font-black text-gray-900 bg-gray-50 p-3 rounded-xl border border-gray-100">
                    <span v-if="user.phone?.startsWith('GG_')" class="italic text-gray-400 font-normal">Chưa cập nhật SĐT</span>
                    <span v-else>{{ user.phone }}</span>
                  </p>
                </div>
              </div>
              <div>
                  <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5">Email liên hệ</p>
                  <input v-if="isEditing" v-model="editForm.email" type="email" class="w-full text-sm font-black text-gray-900 bg-white p-3 rounded-xl border border-emerald-300 focus:ring-2 focus:ring-emerald-500 outline-none transition-all" />
                  <p v-else class="text-sm font-black text-gray-900 bg-gray-50 p-3 rounded-xl border border-gray-100">{{ user.email || 'Chưa cập nhật' }}</p>
              </div>
            </div>
          </section>

          <!-- Wallet & Points -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <!-- Wallet Card -->
            <section class="bg-[#075955] text-white p-8 rounded-3xl shadow-lg shadow-[#075955]/20 relative overflow-hidden group">
              <div class="absolute -right-8 -top-8 w-32 h-32 bg-white/10 rounded-full group-hover:scale-150 transition-transform duration-500 blur-xl"></div>
              <div class="relative z-10">
                <div class="flex items-center gap-3 mb-6">
                  <span class="material-symbols-outlined text-emerald-300">account_balance_wallet</span>
                  <p class="text-[10px] font-black uppercase tracking-widest text-emerald-100">Ví điện tử</p>
                </div>
                <p class="text-xs font-semibold text-emerald-100 mb-1">Số dư khả dụng</p>
                <h3 class="text-3xl font-black tracking-tight mb-8">
                  {{ user.walletBalance ? user.walletBalance.toLocaleString('vi-VN') : '0' }}<span class="text-lg ml-1 text-emerald-200">₫</span>
                </h3>
                <button class="w-full bg-white text-[#075955] hover:bg-gray-50 text-xs font-black uppercase tracking-widest py-3.5 rounded-xl transition-all duration-200 shadow-sm active:scale-95">
                  Nạp thêm tiền
                </button>
              </div>
            </section>

            <!-- Points Card -->
            <section class="bg-white p-8 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100 relative overflow-hidden">
              <div class="flex items-center gap-3 mb-6">
                <span class="material-symbols-outlined text-amber-500">stars</span>
                <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Điểm thưởng</p>
              </div>
              <p class="text-xs font-semibold text-gray-500 mb-1">Loyalty Points</p>
              <h3 class="text-3xl font-black tracking-tight text-gray-900 mb-8">
                1,250<span class="text-lg ml-1 text-gray-400 font-semibold tracking-normal">pts</span>
              </h3>
              
              <div class="flex items-center gap-3 bg-amber-50 p-4 rounded-xl border border-amber-100">
                <span class="material-symbols-outlined text-amber-600 text-lg">local_activity</span>
                <p class="text-xs font-bold text-amber-800">Bạn có 2 ưu đãi có sẵn</p>
              </div>
            </section>
          </div>

          <!-- Transaction History -->
          <section class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
            <div class="flex justify-between items-center mb-6">
              <h3 class="text-sm font-black text-gray-900 uppercase tracking-widest">Lịch sử giao dịch</h3>
            </div>
            
            <div v-if="transactions.length === 0" class="text-center py-6 text-sm font-bold text-gray-400">
              Chưa có giao dịch nào
            </div>
            <div v-else class="space-y-4 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
              <div v-for="txn in transactions" :key="txn.id" class="flex items-center justify-between p-4 rounded-2xl border border-gray-100 bg-gray-50/50 hover:bg-gray-50 transition-colors">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" :class="txn.amount > 0 ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'">
                    <span class="material-symbols-outlined">{{ txn.amount > 0 ? 'south_west' : 'north_east' }}</span>
                  </div>
                  <div>
                    <h4 class="text-sm font-black text-gray-900">{{ txn.title }}</h4>
                    <p class="text-xs font-medium text-gray-500">{{ txn.description }}</p>
                    <p class="text-[10px] font-bold text-gray-400 mt-1">{{ new Date(txn.date).toLocaleString('vi-VN') }}</p>
                  </div>
                </div>
                <div class="text-right">
                  <p class="text-base font-black tracking-tight" :class="txn.color">
                    {{ txn.amount > 0 ? '+' : '' }}{{ txn.amount.toLocaleString('vi-VN') }}₫
                  </p>
                  <span class="text-[10px] font-black uppercase tracking-widest" :class="txn.status === 'Thành công' ? 'text-emerald-500' : 'text-amber-500'">
                    {{ txn.status }}
                  </span>
                </div>
              </div>
            </div>
          </section>



        </div>
      </div>
    </main>
    </div>
    
    <!-- Custom Logout Confirmation Modal -->
    <div v-if="showLogoutConfirm" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl w-full max-w-sm shadow-2xl border border-slate-200 overflow-hidden transform transition-all">
        <div class="p-6 text-center">
          <div class="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <span class="material-symbols-outlined text-3xl text-red-500">logout</span>
          </div>
          <h3 class="text-lg font-black text-slate-800 mb-2">Xác nhận đăng xuất</h3>
          <p class="text-sm font-medium text-slate-500 mb-6">Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Trung Nam không?</p>
          
          <div class="flex gap-3">
            <button @click="showLogoutConfirm = false" class="flex-1 py-3 px-4 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm font-bold transition-colors">
              Hủy bỏ
            </button>
            <button @click="executeLogout" class="flex-1 py-3 px-4 bg-red-500 hover:bg-red-600 text-white rounded-xl text-sm font-bold shadow-lg shadow-red-500/30 transition-all active:scale-95">
              Đăng xuất
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

const router = useRouter();
const authStore = useAuthStore();
const api = useApi();
const user = ref(null);
const loading = ref(true);
const showLogoutConfirm = ref(false);
const transactions = ref([]);

const fetchTransactions = async () => {
    try {
        const res = await api.get(`/users/${user.value.id}/bookings`);
        const history = [];
        res.data.forEach(b => {
             // Giao dịch mua vé
             history.push({
                 id: b.id + '_buy',
                 title: `Thanh toán vé #${b.id}`,
                 description: `Chuyến: ${b.trip.departurePoint} - ${b.trip.arrivalPoint}`,
                 date: b.createdAt,
                 amount: -b.totalPrice,
                 status: b.status === 'PENDING' ? 'Đang xử lý' : 'Thành công',
                 color: 'text-rose-600'
             });

             // Nếu hủy vé -> Hoàn tiền (90% giá vé, trừ 10% phí)
             if (b.status === 'CANCELLED') {
                 // Dùng một thời gian trễ nhỏ để hiển thị giao dịch hủy diễn ra sau mua
                 const cancelDate = new Date(new Date(b.createdAt).getTime() + 60000).toISOString(); 
                 history.push({
                     id: b.id + '_refund',
                     title: 'Hoàn tiền hủy vé (90%)',
                     description: `Mã đơn hàng: #${b.id}`,
                     date: cancelDate,
                     amount: b.totalPrice * 0.9,
                     status: 'Thành công',
                     color: 'text-emerald-600'
                 });
             }
        });

        // Sắp xếp giảm dần theo ID vé (Mới nhất lên đầu)
        transactions.value = history.sort((a, b) => {
            const idA = parseInt(a.id.split('_')[0]);
            const idB = parseInt(b.id.split('_')[0]);
            
            if (idA === idB) {
                // Nếu cùng 1 vé có cả giao dịch Mua và Hủy, thì Hủy (refund) xếp trên Mua (buy)
                if (a.id.includes('refund')) return -1;
                if (b.id.includes('refund')) return 1;
                return 0;
            }
            return idB - idA;
        });
    } catch (e) {
        console.error("Lỗi lấy lịch sử giao dịch", e);
    }
};

const isEditing = ref(false);
const editForm = ref({
  fullName: '',
  phone: '',
  email: ''
});

const startEdit = () => {
  editForm.value = {
    fullName: user.value.fullName,
    phone: user.value.phone?.startsWith('GG_') ? '' : user.value.phone,
    email: user.value.email || ''
  };
  isEditing.value = true;
};

const saveProfile = async () => {
  try {
    const updatedUser = {
      ...user.value,
      fullName: editForm.value.fullName,
      phone: editForm.value.phone,
      email: editForm.value.email
    };
    await api.put(`/users/${user.value.id}`, updatedUser);
    user.value = { ...user.value, ...updatedUser };
    authStore.currentUser = user.value; // Đồng bộ với store
    isEditing.value = false;
  } catch (error) {
    console.error("Lỗi khi lưu profile:", error);
    alert("Có lỗi xảy ra khi cập nhật thông tin!");
  }
};

const checkAuth = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/auth/login');
    loading.value = false;
    return;
  }

  try {
    // 📡 Đồng bộ thời gian thực từ server dùng JWT token
    const freshUser = await authStore.fetchMe();
    user.value = freshUser || authStore.currentUser;
    await fetchTransactions(); // Gọi fetch transaction sau khi có user.value
  } catch (err) {
    console.error("Lỗi kết nối server, dùng cache:", err);
    user.value = authStore.currentUser;
    if (!user.value) router.push('/auth/login');
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  showLogoutConfirm.value = true;
};

const executeLogout = () => {
  showLogoutConfirm.value = false;
  authStore.logout();
  router.push('/auth/login');
};

onMounted(() => {
  checkAuth();
});
</script>


<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
</style>
