<template>
  <div class="min-h-screen bg-[#f4f7f6] font-sans text-slate-800 pb-20">
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
          
          <!-- Left Column -->
          <div class="lg:col-span-1">
            <ProfileSidebar 
              :user="user" 
              @logout="showLogoutConfirm = true" 
            />
          </div>

          <!-- Right Column -->
          <div class="lg:col-span-2 space-y-8">
            
            <PersonalInfoCard 
              ref="personalInfoRef"
              :user="user"
              @save="saveProfile"
            />

            <WalletAndPoints 
              :user="user"
              @open-topup="showTopupModal = true"
              @open-voucher-store="showVoucherStore = true"
            />

            <VoucherList :myVouchers="myVouchers" />

            <TransactionHistory :transactions="transactions" />

          </div>
        </div>
      </main>
    </div>
    
    <!-- Modals -->
    <LogoutConfirmModal 
      :show="showLogoutConfirm" 
      @close="showLogoutConfirm = false" 
      @confirm="executeLogout" 
    />

    <TopupModal 
      :show="showTopupModal"
      :user="user"
      v-model:topupAmount="topupAmount"
      @close="showTopupModal = false"
      @success="handleTopupSuccess"
    />

    <VoucherStoreModal 
      :show="showVoucherStore"
      :user="user"
      :availableVouchers="availableVouchers"
      :loading="loadingVouchers"
      :redeeming="redeeming"
      @close="showVoucherStore = false"
      @redeem="redeemVoucher"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

// Import Components
import ProfileSidebar from '@/components/user/profile/ProfileSidebar.vue';
import PersonalInfoCard from '@/components/user/profile/PersonalInfoCard.vue';
import WalletAndPoints from '@/components/user/profile/WalletAndPoints.vue';
import VoucherList from '@/components/user/profile/VoucherList.vue';
import TransactionHistory from '@/components/user/profile/TransactionHistory.vue';
import TopupModal from '@/components/user/profile/TopupModal.vue';
import VoucherStoreModal from '@/components/user/profile/VoucherStoreModal.vue';
import LogoutConfirmModal from '@/components/user/profile/LogoutConfirmModal.vue';

const router = useRouter();
const authStore = useAuthStore();
const api = useApi();

const user = ref(null);
const loading = ref(true);
const transactions = ref([]);
const myVouchers = ref([]);
const availableVouchers = ref([]);
const personalInfoRef = ref(null);

// Modal states
const showLogoutConfirm = ref(false);
const showTopupModal = ref(false);
const showVoucherStore = ref(false);
const topupAmount = ref(50000);
const loadingVouchers = ref(false);
const redeeming = ref(false);

const fetchMyVouchers = async () => {
    try {
        const res = await api.get(`/vouchers/my-vouchers`);
        myVouchers.value = res.data;
    } catch (e) {
        console.error("Lỗi lấy ví voucher", e);
    }
};

const fetchAvailableVouchers = async () => {
    loadingVouchers.value = true;
    try {
        const res = await api.get(`/vouchers/available`);
        availableVouchers.value = res.data;
    } catch (e) {
        console.error("Lỗi lấy danh sách voucher", e);
    } finally {
        loadingVouchers.value = false;
    }
};

const redeemVoucher = async (voucher) => {
    if (!confirm(`Bạn có chắc muốn dùng ${voucher.pointsCost} điểm để đổi mã ${voucher.code}?`)) return;
    redeeming.value = true;
    try {
        const res = await api.post(`/vouchers/redeem/${voucher.id}`);
        alert(res.data.message);
        const freshUser = await authStore.fetchMe();
        user.value = freshUser;
        await fetchMyVouchers();
    } catch (error) {
        alert("Đổi thất bại: " + (error.response?.data?.message || error.message));
    } finally {
        redeeming.value = false;
    }
};

const fetchTransactions = async () => {
    try {
        const res = await api.get(`/users/${user.value.id}/bookings?t=${new Date().getTime()}`);
        const history = [];
        res.data.forEach(b => {
             history.push({
                 id: b.id + '_buy',
                 title: `Thanh toán vé #${b.id}`,
                 description: `Chuyến: ${b.trip.departurePoint} - ${b.trip.arrivalPoint}`,
                 date: b.createdAt,
                 amount: -b.totalPrice,
                 status: b.status === 'PENDING' ? 'Đang xử lý' : 'Thành công',
                 color: 'text-rose-600'
             });
             if (b.status === 'CANCELLED') {
                 const refundValue = b.refundAmount !== undefined && b.refundAmount !== null ? b.refundAmount : 0;
                 if (refundValue > 0) {
                     const cancelDate = new Date(new Date(b.createdAt).getTime() + 60000).toISOString(); 
                     history.push({
                         id: b.id + '_refund',
                         title: 'Hoàn tiền hủy vé',
                         description: `Mã đơn hàng: #${b.id}`,
                         date: cancelDate,
                         amount: refundValue,
                         status: 'Thành công',
                         color: 'text-emerald-600'
                     });
                 }
             }
        });
        transactions.value = history.sort((a, b) => {
            const idA = parseInt(a.id.split('_')[0]);
            const idB = parseInt(b.id.split('_')[0]);
            if (idA === idB) {
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

const saveProfile = async (formData) => {
  try {
    const updatedUser = {
      ...user.value,
      ...formData
    };
    await api.put(`/users/${user.value.id}`, updatedUser);
    user.value = { ...user.value, ...updatedUser };
    authStore.updateUser(user.value);
    if (personalInfoRef.value) {
      personalInfoRef.value.cancelEdit();
    }
  } catch (error) {
    console.error("Lỗi khi lưu profile:", error);
    alert("Có lỗi xảy ra khi cập nhật thông tin!");
  }
};

const handleTopupSuccess = async (freshUser) => {
  user.value = freshUser;
  showTopupModal.value = false;
  await fetchTransactions(); // Cập nhật lại lịch sử
};

const checkAuth = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/auth/login');
    loading.value = false;
    return;
  }
  try {
    const freshUser = await authStore.fetchMe();
    user.value = freshUser || authStore.currentUser;
    await fetchTransactions();
    await fetchMyVouchers();
    await fetchAvailableVouchers();
  } catch (err) {
    console.error("Lỗi kết nối server, dùng cache:", err);
    user.value = authStore.currentUser;
    if (!user.value) router.push('/auth/login');
  } finally {
    loading.value = false;
  }
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
</style>
