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

            <div id="my-vouchers" ref="myVouchersSection">
              <VoucherList :myVouchers="myVouchers" />
            </div>

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
      :feedback="voucherFeedback"
      @close="closeVoucherStore"
      @redeem="redeemVoucher"
    />
  </div>
</template>

<script setup>
import { nextTick, ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
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
const route = useRoute();
const authStore = useAuthStore();
const api = useApi();

const user = ref(null);
const loading = ref(true);
const transactions = ref([]);
const myVouchers = ref([]);
const availableVouchers = ref([]);
const personalInfoRef = ref(null);
const myVouchersSection = ref(null);

// Modal states
const showLogoutConfirm = ref(false);
const showTopupModal = ref(false);
const showVoucherStore = ref(false);
const topupAmount = ref(50000);
const loadingVouchers = ref(false);
const redeeming = ref(false);
const voucherFeedback = ref(null);

const openRequestedProfileDestination = async () => {
  if (route.query.open === 'voucher-store') showVoucherStore.value = true;
  if (route.query.section === 'my-vouchers') {
    await nextTick();
    myVouchersSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

const closeVoucherStore = () => {
  showVoucherStore.value = false;
  voucherFeedback.value = null;
  if (route.query.open === 'voucher-store') {
    const query = { ...route.query };
    delete query.open;
    router.replace({ query });
  }
};

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
    redeeming.value = true;
    voucherFeedback.value = null;
    try {
        const res = await api.post(`/vouchers/redeem/${voucher.id}`);
        const freshUser = await authStore.fetchMe();
        user.value = freshUser;
        await fetchMyVouchers();
        voucherFeedback.value = { type: 'success', message: `${res.data.message} Mã ${voucher.code} đã được thêm vào ví voucher.` };
    } catch (error) {
        voucherFeedback.value = { type: 'error', message: error.response?.data?.message || 'Không thể đổi voucher lúc này. Vui lòng thử lại.' };
    } finally {
        redeeming.value = false;
    }
};

const fetchTransactions = async () => {
    try {
        const [res, refundsRes] = await Promise.all([
            api.get(`/users/${user.value.id}/bookings?t=${new Date().getTime()}`),
            api.get(`/refund-requests/me?t=${new Date().getTime()}`).catch(() => ({ data: [] }))
        ]);
        const refundsByBooking = new Map(
            (Array.isArray(refundsRes.data) ? refundsRes.data : []).map(item => [Number(item.booking?.id), item])
        );
        const history = [];
        res.data.forEach(b => {
             history.push({
                 id: b.id + '_buy',
                 title: `Thanh toán vé ${b.ticketCode || `#${b.id}`}`,
                 description: `Chuyến: ${b.trip.departurePoint} - ${b.trip.arrivalPoint}`,
                 date: b.createdAt,
                 amount: -b.totalPrice,
                 status: b.status === 'PENDING' ? 'Đang xử lý' : 'Thành công',
                 color: 'text-rose-600'
             });
             if (b.status === 'CANCELLED') {
                 const refundValue = b.refundAmount !== undefined && b.refundAmount !== null ? b.refundAmount : 0;
                 const refundRequest = refundsByBooking.get(Number(b.id));
                 if (refundValue > 0) {
                     const cancelDate = new Date(new Date(b.createdAt).getTime() + 60000).toISOString(); 
                     history.push({
                         id: b.id + '_refund',
                         title: refundRequest?.status === 'COMPLETED' ? 'Hoàn tiền hủy vé' : 'Yêu cầu hoàn tiền',
                         description: refundRequest?.refundMethod === 'BANK_TRANSFER'
                             ? `Chuyển khoản ngân hàng, mã vé ${b.ticketCode || `#${b.id}`}`
                             : `Ví Trung Nam, mã vé ${b.ticketCode || `#${b.id}`}`,
                         date: cancelDate,
                         amount: refundValue,
                         status: ({ PENDING: 'Chờ xử lý', APPROVED: 'Đã duyệt', COMPLETED: 'Thành công', REJECTED: 'Cần liên hệ' }[refundRequest?.status]) || 'Thành công',
                         color: refundRequest && refundRequest.status !== 'COMPLETED' ? 'text-amber-600' : 'text-emerald-600'
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
    await openRequestedProfileDestination();
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

watch(
  () => [route.query.open, route.query.section],
  () => {
    if (!loading.value && user.value) openRequestedProfileDestination();
  }
);
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
