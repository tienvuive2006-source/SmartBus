<template>
  <div v-if="show" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 lg:p-8">
    <div class="bg-[#f4f7f6] rounded-3xl w-full max-w-6xl shadow-2xl overflow-hidden flex flex-col max-h-[95vh] animate-modal-in">
      
      <!-- Header -->
      <div class="bg-white p-6 border-b border-gray-100 flex justify-between items-center shrink-0">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-emerald-50 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-[#075955] text-2xl">account_balance_wallet</span>
          </div>
          <div>
            <h3 class="text-xl font-black text-gray-900 tracking-tight">Nạp tiền vào ví</h3>
            <p class="text-xs font-bold text-gray-500">Nạp tiền nhanh chóng, thanh toán tiện lợi cho mọi chuyến đi</p>
          </div>
        </div>
        
        <div class="flex items-center gap-6">
          <!-- Balance info -->
          <div class="hidden sm:flex items-center gap-3 bg-emerald-50/50 px-4 py-2 rounded-xl border border-emerald-100">
            <span class="material-symbols-outlined text-emerald-600">account_balance_wallet</span>
            <div>
              <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest">Số dư hiện tại</p>
              <p class="text-lg font-black text-emerald-700 leading-none">{{ (user.walletBalance || 0).toLocaleString('vi-VN') }}đ</p>
            </div>
          </div>
          
          <button @click="handleClose" class="w-10 h-10 hover:bg-gray-100 rounded-full flex items-center justify-center transition-colors text-gray-400 hover:text-gray-600">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
      </div>
      
      <!-- Body (3 columns) -->
      <div v-if="!isSuccess" class="p-6 overflow-y-auto custom-scrollbar flex-1 bg-white">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
          
          <!-- Column 1: Amount & Payment Method -->
          <TopupAmountSelection 
             v-model="localTopupAmount" 
             :isConfirmed="isConfirmed"
             :bonusAmount="bonusAmount"
             @confirm="handleConfirm"
             @edit="handleEdit"
          />

          <!-- Column 2: QR Code Center -->
          <div v-if="!isConfirmed" class="lg:col-span-6 flex flex-col items-center justify-center text-center p-8 bg-slate-50 border-2 border-dashed border-slate-200 rounded-3xl min-h-[400px]">
             <div class="w-20 h-20 bg-white shadow-sm border border-slate-100 rounded-full flex items-center justify-center mb-6">
                <span class="material-symbols-outlined text-4xl text-slate-300">qr_code_scanner</span>
             </div>
             <h4 class="text-base font-black text-slate-700 uppercase tracking-widest mb-3">Chưa tạo mã QR</h4>
             <p class="text-sm font-semibold text-slate-500 leading-relaxed max-w-[250px]">Vui lòng chọn số tiền cần nạp và bấm <strong class="text-[#075955]">Xác nhận</strong> ở cột bên trái để tạo mã thanh toán.</p>
          </div>
          <TopupQRCode v-else :topupAmount="localTopupAmount" :user="user" :formattedTime="formattedTime" />

          <!-- Column 3: Transaction Info -->
          <div v-if="!isConfirmed" class="lg:col-span-3 opacity-40 pointer-events-none grayscale blur-[1px] transition-all duration-300">
             <TopupTransactionInfo :topupAmount="localTopupAmount" :bonusAmount="bonusAmount" />
          </div>
          <TopupTransactionInfo v-else :topupAmount="localTopupAmount" :bonusAmount="bonusAmount" />
          
        </div>
      </div>

      <!-- Success State -->
      <div v-else class="p-12 flex-1 bg-white flex flex-col items-center justify-center animate-modal-in min-h-[400px]">
        <div class="w-24 h-24 bg-emerald-100 rounded-full flex items-center justify-center mb-6 shadow-inner shadow-emerald-200">
          <span class="material-symbols-outlined text-emerald-600 text-6xl animate-bounce">check_circle</span>
        </div>
        <h2 class="text-3xl font-black text-slate-800 mb-2">Giao dịch thành công!</h2>
        <p class="text-slate-500 font-medium mb-8 text-center max-w-md">Số tiền <strong class="text-emerald-600">{{ localTopupAmount.toLocaleString('vi-VN') }}đ</strong> đã được cộng vào ví của bạn. Cửa sổ này sẽ tự động đóng lại trong giây lát...</p>
        <div class="w-8 h-8 border-4 border-emerald-100 border-t-emerald-600 rounded-full animate-spin"></div>
      </div>
      
      <!-- Footer Note -->
      <div class="bg-amber-50/50 p-4 border-t border-amber-100 flex items-start gap-3 shrink-0">
        <span class="material-symbols-outlined text-amber-500 mt-0.5">info</span>
        <div>
          <h5 class="text-sm font-black text-amber-900">Lưu ý quan trọng:</h5>
          <p class="text-xs font-medium text-amber-800/80">Sau khi thanh toán thành công, vui lòng đợi hệ thống cập nhật. Số dư sẽ được cộng vào ví của bạn ngay lập tức.</p>
        </div>
      </div>
      
    </div>
  </div>
</template>

<script setup>
import { onUnmounted, watch, ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';
import TopupAmountSelection from './topup/TopupAmountSelection.vue';
import TopupQRCode from './topup/TopupQRCode.vue';
import TopupTransactionInfo from './topup/TopupTransactionInfo.vue';

const props = defineProps({
  show: Boolean,
  user: {
    type: Object,
    required: true
  },
  topupAmount: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['close', 'success', 'update:topupAmount']);
const authStore = useAuthStore();
const isSuccess = ref(false);
const isConfirmed = ref(false);

const localTopupAmount = ref(props.topupAmount);

watch(() => props.topupAmount, (newVal) => {
  localTopupAmount.value = newVal;
});

watch(localTopupAmount, (newVal) => {
  emit('update:topupAmount', newVal);
});

const api = useApi();
const topupPromotions = ref([]);

const fetchTopupPromotions = async () => {
  try {
    const res = await api.get('/settings/TOPUP_PROMOTIONS');
    if (res.data && res.data.value) {
      topupPromotions.value = JSON.parse(res.data.value);
    }
  } catch (e) {
    console.error('Lỗi tải cấu hình khuyến mãi', e);
  }
};

const bonusAmount = computed(() => {
  for (const tier of topupPromotions.value) {
    const min = tier.minAmount || 0;
    const max = tier.maxAmount || 0;
    
    if (localTopupAmount.value >= min && (max === 0 || localTopupAmount.value <= max)) {
      return (localTopupAmount.value * tier.bonusPercent) / 100;
    }
  }
  return 0;
});

// Countdown Timer Logic
const timeLeft = ref(600); // 10 minutes in seconds
let timerInterval = null;

const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0');
  const s = (timeLeft.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

const startTimer = () => {
  timeLeft.value = 600;
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      clearInterval(timerInterval);
      handleClose(); // Auto close when expired (optional behavior, can also just show expired state)
    }
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
};

// Polling logic
let topupInterval = null;
let initialBalance = 0;
const sessionStartTime = ref(null);

const startPolling = () => {
  initialBalance = props.user.walletBalance || 0;
  if (topupInterval) clearInterval(topupInterval);
  
  topupInterval = setInterval(async () => {
    try {
      // 1. Polling directly from backend for localhost demo
      const authHeaders = {
        'Authorization': `Bearer ${authStore.token}`,
        'Accept': 'application/json'
      };
      
      const pollUrl = `${import.meta.env.VITE_API_BASE_URL}/users/check-topup?userId=${props.user.id}&expectedAmount=${localTopupAmount.value}&sessionStartTime=${sessionStartTime.value}`;
      
      const response = await fetch(pollUrl, { headers: authHeaders });
      const result = await response.json();
      
      if (result.success) {
        // Backend says payment found & processed
        clearInterval(topupInterval);
        topupInterval = null;
        stopTimer();
        
        // Refresh authStore to get new balance
        const freshUser = await authStore.fetchMe();
        
        isSuccess.value = true;
        setTimeout(() => {
          emit('success', freshUser);
          isSuccess.value = false;
        }, 3000);
        return;
      }

      // 2. Fallback: check if balance increased (in case Webhook worked in production)
      const freshUser = await authStore.fetchMe();
      const newBalance = freshUser.walletBalance || 0;
      if (newBalance > initialBalance) {
        clearInterval(topupInterval);
        topupInterval = null;
        stopTimer();
        isSuccess.value = true;
        setTimeout(() => {
          emit('success', freshUser);
          isSuccess.value = false;
        }, 3000);
      }
    } catch (e) {
      console.error("Lỗi kiểm tra số dư ví", e);
    }
  }, 5000); // Check every 5 seconds
};

const stopPolling = () => {
  if (topupInterval) {
    clearInterval(topupInterval);
    topupInterval = null;
  }
};

const handleConfirm = () => {
  if (!localTopupAmount.value || localTopupAmount.value < 10000) {
    alert("Vui lòng chọn hoặc nhập số tiền nạp tối thiểu là 10.000đ");
    return;
  }
  isConfirmed.value = true;
  sessionStartTime.value = new Date().toISOString();
  startPolling();
  startTimer();
};

const handleEdit = () => {
  isConfirmed.value = false;
  stopPolling();
  stopTimer();
};

watch(() => props.show, (newVal) => {
  if (newVal) {
    isConfirmed.value = false; // Luôn reset về chưa xác nhận khi mở lại modal
    localTopupAmount.value = null; // Bỏ chọn số tiền mặc định
    fetchTopupPromotions();
  } else {
    stopPolling();
    stopTimer();
  }
});

onUnmounted(() => {
  stopPolling();
  stopTimer();
});

const handleClose = () => {
  stopPolling();
  stopTimer();
  emit('close');
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
@keyframes modalIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-modal-in {
  animation: modalIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
