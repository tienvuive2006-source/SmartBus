<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-800 pb-20">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <button @click="$router.back()" class="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center transition-all border border-white/20">
            <span class="material-symbols-outlined text-sm font-black text-white">arrow_back</span>
          </button>
          <div class="flex flex-col">
            <span class="text-lg font-bold leading-none tracking-tight">Thanh toán</span>
            <span class="text-[9px] uppercase tracking-wider font-semibold opacity-70">Nhà xe Trung - Nam</span>
          </div>
        </div>
        <div class="flex items-center gap-2 text-emerald-400">
           <span class="material-symbols-outlined text-xl">shield_with_heart</span>
           <span class="text-[10px] font-black uppercase tracking-widest">Bảo mật 256-bit</span>
        </div>
      </div>
    </nav>

    <div class="max-w-3xl mx-auto px-4 py-10">
      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-gray-200 border-t-[#075955] rounded-full animate-spin"></div>
      </div>

      <div v-else-if="trip" class="space-y-6">
        <section class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
           <h2 class="font-bold text-gray-900 mb-6 uppercase text-xs tracking-widest pb-2 border-b border-gray-100">Chi tiết hành trình</h2>
           <div class="flex items-center gap-4 mb-6">
              <div class="w-12 h-12 bg-[#075955]/10 rounded-lg flex items-center justify-center border border-[#075955]/20">
                 <span class="material-symbols-outlined text-[#075955]">directions_bus</span>
              </div>
              <div>
                 <p class="font-bold text-gray-900">{{ trip.companyName }}</p>
                 <p class="text-xs text-gray-500 font-medium">{{ trip.busType }}</p>
              </div>
           </div>
           <div class="grid grid-cols-2 gap-8 text-sm">
              <div>
                 <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Khởi hành</p>
                 <p class="font-bold text-gray-900">{{ trip.departureTime }} - {{ trip.departurePoint }}</p>
              </div>
              <div class="text-right">
                 <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Ghế ngồi</p>
                 <p class="font-bold text-[#f03a17] uppercase tracking-widest">{{ seatNames }}</p>
              </div>
           </div>
        </section>

        <div class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h2 class="text-sm font-bold text-gray-900 uppercase tracking-widest mb-4">Thông tin hành khách</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-[10px] font-bold text-gray-400 uppercase mb-1">Họ và tên</label>
              <input v-model="customerName" type="text" placeholder="Nhập tên người đi" class="w-full bg-gray-50 border border-gray-200 rounded-md p-3 text-sm font-semibold focus:outline-[#075955] text-gray-800"/>
            </div>
            <div>
              <label class="block text-[10px] font-bold text-gray-400 uppercase mb-1">Số điện thoại</label>
              <input v-model="customerPhone" type="tel" placeholder="Số điện thoại nhận vé" class="w-full bg-gray-50 border border-gray-200 rounded-md p-3 text-sm font-semibold focus:outline-[#075955] text-gray-800"/>
            </div>
          </div>
        </div>

        <section class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
           <h2 class="font-bold text-gray-900 mb-6 uppercase text-xs tracking-widest pb-2 border-b border-gray-100">Phương thức thanh toán</h2>
            <div class="space-y-3">
              <div 
                v-for="method in paymentMethods" :key="method.id"
                @click="selectedMethod = method.id"
                :class="['p-4 rounded-lg border-2 transition-all cursor-pointer flex items-center justify-between', selectedMethod === method.id ? 'border-[#075955] bg-[#075955]/5' : 'border-gray-100 hover:border-gray-200']"
              >
                <div class="flex items-center gap-4">
                   <div :class="['p-2 rounded-lg', selectedMethod === method.id ? 'bg-[#075955] text-white' : 'bg-gray-100 text-gray-400']">
                      <span class="material-symbols-outlined text-lg">{{ method.icon }}</span>
                   </div>
                   <div>
                      <span class="text-sm font-black text-gray-900 uppercase tracking-widest">{{ method.name }}</span>
                      <p v-if="method.id === 'WALLET' && currentUser" class="text-[10px] font-bold text-emerald-600 mt-0.5">
                        Số dư hiện tại: {{ currentUser.walletBalance?.toLocaleString() }}đ
                      </p>
                   </div>
                </div>
                <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center', selectedMethod === method.id ? 'border-[#075955] bg-[#075955]' : 'border-gray-200']">
                   <div v-if="selectedMethod === method.id" class="w-2 h-2 bg-white rounded-full"></div>
                </div>
              </div>
           </div>
        </section>

        <section class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm flex flex-col md:flex-row items-center justify-between gap-6">
           <div>
              <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Tổng tiền thanh toán</p>
              <p class="text-3xl font-bold text-[#075955] tracking-tighter">{{ totalAmount.toLocaleString() }}đ</p>
           </div>
           <button 
             @click="processPayment" :disabled="isProcessing"
             class="w-full md:w-64 bg-[#f03a17] hover:bg-[#d63314] text-white py-5 rounded-md font-bold text-sm uppercase tracking-widest transition-all shadow-md active:scale-95 disabled:bg-gray-300 disabled:text-gray-500"
           >
             {{ isProcessing ? 'Đang xử lý...' : 'Thanh toán ngay' }}
           </button>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const api = useApi();

const tripId = route.query.tripId;
const selectedSeatsStr = route.query.seats;
const seatsArray = computed(() => selectedSeatsStr ? selectedSeatsStr.split(',') : []);
const seatNames = computed(() => seatsArray.value.join(', '));
const totalAmount = computed(() => parseInt(route.query.total) || 0);

const trip = ref(null);
const loading = ref(true);
const isProcessing = ref(false);
const selectedMethod = ref('ATM');
const customerName = ref('');
const customerPhone = ref('');

// currentUser reactive từ Pinia
const currentUser = authStore.currentUser;

const paymentMethods = computed(() => {
  const methods = [
    { id: 'ATM', name: 'Thẻ Nội Địa / ATM', icon: 'credit_card' },
    { id: 'MOMO', name: 'Ví MoMo', icon: 'wallet' },
    { id: 'VNPAY', name: 'VNPAY-QR', icon: 'qr_code_2' }
  ];
  if (authStore.isLoggedIn) {
    methods.unshift({ id: 'WALLET', name: 'Ví Trung - Nam (Khuyên dùng)', icon: 'account_balance_wallet' });
  }
  return methods;
});

const fetchTrip = async () => {
  try {
    const res = await api.get(`/trips/${tripId}`);
    trip.value = res.data;
  } catch (err) { console.error(err); }
  finally { loading.value = false; }
};

const processPayment = async () => {
  if (!customerName.value || !customerPhone.value) return alert('Vui lòng nhập đầy đủ thông tin hành khách!');
  
  // 🛡️ KIỂM TRA SỐ DƯ VÍ (NẾU CHỌN THANH TOÁN BẰNG VÍ)
  if (selectedMethod.value === 'WALLET') {
    const balance = authStore.currentUser?.walletBalance || 0;
    if (balance < totalAmount.value) {
      return alert(`Số dư Ví Trung - Nam không đủ! Bạn cần thêm ${(totalAmount.value - balance).toLocaleString()}đ nữa để đặt vé này.`);
    }
  }

  isProcessing.value = true;
  try {
    const user = authStore.currentUser;
    const bookingData = {
      customerName: customerName.value,
      customerPhone: customerPhone.value,
      customerEmail: user?.email || 'customer@trungnam.com',
      seatNumbers: seatsArray.value,
      totalPrice: totalAmount.value,
      paymentMethod: selectedMethod.value,
      status: 'PAID',
      trip: { id: parseInt(tripId) },
      user: user?.id ? { id: user.id } : null
    };

    // Dùng api (có JWT token) thay vì axios trực tiếp
    const res = await api.post('/admin/bookings/create', bookingData);
    if (res.status === 200 || res.status === 201) {
      // Cập nhật số dư ví nếu thanh toán bằng ví
      if (res.data.user?.walletBalance !== undefined) {
        authStore.updateWalletBalance(res.data.user.walletBalance);
      }

      // 💾 LƯU VÀO LỊCH SỬ LOCAL
      const newTicket = {
        id: res.data.id,
        from: trip.value.departurePoint,
        to: trip.value.arrivalPoint,
        time: trip.value.departureTime,
        date: trip.value.departureDate?.split('T')[0] || new Date().toISOString().split('T')[0],
        seats: seatNames.value,
        total: totalAmount.value,
        method: selectedMethod.value,
        busType: trip.value.busType
      };
      
      const history = JSON.parse(localStorage.getItem('trungnam_history') || '[]');
      history.unshift(newTicket);
      localStorage.setItem('trungnam_history', JSON.stringify(history));

      router.push({ path: '/booking/payment-success', query: { bookingId: res.data.id, from: trip.value.departurePoint, to: trip.value.arrivalPoint, time: trip.value.departureTime, seats: seatNames.value, total: totalAmount.value, method: selectedMethod.value } });
    }
  } catch (err) { alert('Thanh toán thất bại: ' + (err.response?.data?.error || err.message)); }
  finally { isProcessing.value = false; }
};

onMounted(() => {
  if (authStore.isLoggedIn) {
    customerName.value = authStore.currentUser?.fullName || '';
    customerPhone.value = authStore.currentUser?.phone || '';
    selectedMethod.value = 'WALLET';
  } else {
    selectedMethod.value = 'ATM';
  }
  fetchTrip();
});
</script>