<template>
  <div class="min-h-screen bg-[#f4f7f6] font-sans text-slate-800 pb-20 relative">
    
    <!-- Overlay Thanh toán thành công (Giữ nguyên) -->
    <div v-if="paymentSuccess" class="fixed inset-0 z-[100] flex items-center justify-center bg-[#075955]/95 backdrop-blur-sm transition-all duration-500">
      <div class="flex flex-col items-center justify-center text-white animate-fade-in-up">
        <div class="w-24 h-24 bg-white rounded-full flex items-center justify-center mb-6 shadow-2xl animate-bounce">
          <span class="material-symbols-outlined text-[#075955] text-6xl font-black">check_circle</span>
        </div>
        <h2 class="text-3xl font-black uppercase tracking-widest mb-2 text-center px-4">{{ selectedMethod === 'CASH' ? 'Giữ chỗ thành công!' : 'Thanh toán thành công!' }}</h2>
        <p class="text-emerald-100 font-medium text-lg text-center">Đang xuất vé & chuyển hướng...</p>
      </div>
    </div>

    <!-- Header Navbar -->
    <nav class="bg-white text-slate-800 border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <button @click="$router.back()" class="w-10 h-10 rounded-full hover:bg-gray-100 flex items-center justify-center transition-all border border-gray-200">
            <span class="material-symbols-outlined text-lg font-bold text-gray-600">arrow_back</span>
          </button>
          <div class="flex flex-col">
            <span class="text-lg font-black text-gray-800 leading-none tracking-tight">Thanh toán an toàn</span>
            <span class="text-[10px] text-gray-500 font-bold uppercase tracking-widest mt-1">Hoàn tất đặt vé của bạn</span>
          </div>
        </div>

      </div>
    </nav>

    <div class="max-w-6xl mx-auto px-4 py-8">
      <div v-if="loading" class="flex justify-center py-32">
        <div class="w-10 h-10 border-4 border-gray-200 border-t-[#075955] rounded-full animate-spin"></div>
      </div>

      <div v-else-if="trip" class="flex flex-col lg:flex-row gap-8">
        
        <!-- CỘT TRÁI: THÔNG TIN & THANH TOÁN -->
        <div class="flex-1 space-y-6">
          
          <!-- Thông tin hành khách -->
          <div class="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
            <div class="flex items-center gap-3 mb-6">
              <div class="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center">
                 <span class="material-symbols-outlined text-xl">person</span>
              </div>
              <h2 class="text-lg font-black text-gray-900 uppercase tracking-widest">Thông tin hành khách</h2>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div class="md:col-span-2 relative">
                <label class="block text-[10px] font-bold text-gray-500 mb-1.5 uppercase tracking-widest">Họ và tên</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">badge</span>
                  <input v-model="customerName" type="text" placeholder="Ví dụ: Nguyễn Văn A" class="w-full bg-gray-50 border border-gray-200 rounded-xl pl-12 pr-4 py-3.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-[#075955]/20 focus:border-[#075955] transition-all text-gray-800 placeholder:text-gray-400 placeholder:font-medium"/>
                </div>
              </div>
              <div class="relative">
                <label class="block text-[10px] font-bold text-gray-500 mb-1.5 uppercase tracking-widest">Số điện thoại</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">call</span>
                  <input v-model="customerPhone" type="tel" placeholder="SĐT nhận thông báo vé" class="w-full bg-gray-50 border border-gray-200 rounded-xl pl-12 pr-4 py-3.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-[#075955]/20 focus:border-[#075955] transition-all text-gray-800 placeholder:text-gray-400 placeholder:font-medium"/>
                </div>
              </div>
              <div class="relative">
                <div class="flex items-center justify-between mb-1.5">
                  <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest">Địa chỉ Email</label>
                  <label class="flex items-center gap-2 cursor-pointer group">
                    <span class="text-[9px] font-bold text-gray-400 uppercase group-hover:text-emerald-600 transition-colors">Nhận vé qua Email</span>
                    <div class="relative inline-block w-8 h-4 transition duration-200 ease-in-out">
                      <input type="checkbox" v-model="wantsEmail" class="peer absolute w-0 h-0 opacity-0" />
                      <div class="block w-8 h-4 bg-gray-200 rounded-full peer-checked:bg-[#075955] transition-colors shadow-inner"></div>
                      <div class="absolute left-0.5 top-0.5 w-3 h-3 bg-white rounded-full transition-transform peer-checked:translate-x-4 shadow-sm"></div>
                    </div>
                  </label>
                </div>
                <div class="relative transition-all duration-300" :class="!wantsEmail ? 'opacity-40 grayscale pointer-events-none' : ''">
                  <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2" :class="wantsEmail ? 'text-gray-400' : 'text-gray-300'">mail</span>
                  <input v-model="customerEmail" type="email" placeholder="Bắt buộc để nhận vé điện tử" class="w-full bg-gray-50 border border-gray-200 rounded-xl pl-12 pr-4 py-3.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-[#075955]/20 focus:border-[#075955] transition-all text-gray-800 placeholder:text-gray-400 placeholder:font-medium"/>
                </div>
              </div>
            </div>
          </div>

          <!-- Phương thức thanh toán -->
          <div class="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
             <div class="flex items-center gap-3 mb-6">
                <div class="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center">
                   <span class="material-symbols-outlined text-xl">payments</span>
                </div>
                <h2 class="text-lg font-black text-gray-900 uppercase tracking-widest">Phương thức thanh toán</h2>
             </div>
             
             <div class="space-y-4">
                <div 
                  v-for="method in paymentMethods" :key="method.id"
                  @click="selectedMethod = method.id"
                  :class="['p-4 rounded-2xl border-2 transition-all cursor-pointer flex items-center justify-between group', selectedMethod === method.id ? 'border-[#075955] bg-emerald-50/30' : 'border-gray-100 hover:border-emerald-200 bg-white']"
                >
                  <div class="flex items-center gap-4">
                     <div :class="['w-12 h-12 flex items-center justify-center rounded-xl transition-all duration-300', selectedMethod === method.id ? 'bg-[#075955] text-white shadow-md scale-105' : 'bg-gray-50 text-gray-400 group-hover:bg-emerald-50 group-hover:text-emerald-500']">
                        <span class="material-symbols-outlined text-2xl">{{ method.icon }}</span>
                     </div>
                     <div>
                        <span class="text-sm font-bold uppercase tracking-widest" :class="selectedMethod === method.id ? 'text-[#075955]' : 'text-gray-700'">{{ method.name }}</span>
                        <p v-if="method.id === 'WALLET' && currentUser" class="text-[11px] font-bold text-emerald-600 mt-1 flex items-center gap-1">
                          <span class="material-symbols-outlined text-[13px]">account_balance_wallet</span>
                          Số dư: {{ currentUser.walletBalance?.toLocaleString() }}đ
                        </p>
                        <p v-else class="text-[10px] text-gray-400 mt-1 font-semibold uppercase tracking-wider">Thanh toán tự động 24/7</p>
                     </div>
                  </div>
                  <div :class="['w-6 h-6 rounded-full border-2 flex items-center justify-center transition-colors', selectedMethod === method.id ? 'border-[#075955] bg-[#075955]' : 'border-gray-300']">
                     <div class="w-2.5 h-2.5 bg-white rounded-full transition-transform duration-300" :class="selectedMethod === method.id ? 'scale-100' : 'scale-0'"></div>
                  </div>
                </div>
             </div>

             <!-- Khối QR Code xịn xò -->
             <div v-if="selectedMethod === 'QR'" class="mt-6 transition-all duration-500 overflow-hidden" :class="showQrCode ? 'max-h-[800px] opacity-100' : 'max-h-[200px] opacity-100'">
                <div v-if="!showQrCode" class="p-6 border-2 border-dashed border-amber-200 rounded-2xl bg-amber-50/50 flex flex-col items-center justify-center text-center mt-2">
                   <div class="w-12 h-12 bg-amber-100 text-amber-500 rounded-full flex items-center justify-center mb-3">
                      <span class="material-symbols-outlined text-2xl animate-bounce">assignment_late</span>
                   </div>
                   <h3 class="text-sm font-black uppercase tracking-widest text-amber-700 mb-1">Xác nhận thông tin</h3>
                   <p class="text-xs font-semibold text-amber-600/80 max-w-xs">Vui lòng điền thông tin hành khách và bấm nút <b>Tạo mã QR</b> ở cột bên phải để tiếp tục.</p>
                </div>
                
                <div v-else class="mt-4 p-6 border border-gray-200 rounded-2xl bg-white shadow-sm flex flex-col md:flex-row items-center gap-8 relative overflow-hidden group">
                   <!-- Nền gradient chìm -->
                   <div class="absolute inset-0 bg-gradient-to-br from-emerald-50/50 to-transparent opacity-50"></div>
                   
                   <div class="relative w-48 h-48 sm:w-56 sm:h-56 shrink-0 bg-white p-2 rounded-2xl shadow-md border border-gray-100 group-hover:shadow-lg transition-shadow">
                      <img :src="`https://img.vietqr.io/image/mb-0367093771-compact2.png?amount=${totalAmount}&addInfo=VEXE${seatNames.replace(/[, \-]/g, '')}&accountName=HUYNH%20DUC%20TIEN`" alt="QR Code" class="w-full h-full rounded-xl object-contain" />
                      <!-- Hiệu ứng quét laser (CSS class tự định nghĩa dưới style) -->
                      <div class="absolute top-0 left-0 w-full h-1 bg-[#075955]/80 shadow-[0_0_8px_rgba(7,89,85,0.8)] qr-scan-line rounded-full hidden md:block"></div>
                   </div>
                   
                   <div class="relative flex-1 flex flex-col w-full text-left">
                      <div class="inline-flex items-center gap-1.5 text-[#075955] mb-3 bg-emerald-50 px-3 py-1.5 rounded-lg border border-emerald-100 w-max">
                         <span class="material-symbols-outlined text-[15px] animate-pulse">qr_code_scanner</span>
                         <span class="text-[10px] font-black uppercase tracking-widest">Mã QR Tự Động</span>
                      </div>
                      <h3 class="text-lg font-black text-gray-900 mb-4 uppercase tracking-tight">Mở App Ngân Hàng Quét Mã</h3>
                      
                      <div class="w-full bg-gray-50 border border-gray-200 rounded-xl p-4 mb-4">
                         <div class="flex justify-between items-center mb-3">
                           <span class="text-[10px] text-gray-500 font-bold uppercase tracking-widest">Số tiền</span>
                           <span class="text-lg font-black text-[#f03a17]">{{ totalAmount.toLocaleString() }}đ</span>
                         </div>
                         <div class="flex justify-between items-center">
                           <span class="text-[10px] text-gray-500 font-bold uppercase tracking-widest">Nội dung <span class="text-red-500 normal-case font-medium ml-1">(Bắt buộc)</span></span>
                           <span class="text-xs font-black text-gray-800 bg-white px-2 py-1 rounded border border-gray-300 shadow-sm uppercase tracking-widest">VEXE{{ seatNames.replace(/[, \-]/g, '') }}</span>
                         </div>
                      </div>
                      
                      <p class="text-[10px] text-gray-500 font-bold uppercase tracking-widest flex items-center gap-1 mt-auto">
                         <span class="material-symbols-outlined text-emerald-500 text-[14px]">verified</span>
                         Tự động gạch nợ sau 3-5 giây
                      </p>
                   </div>
                </div>
             </div>
          </div>
        </div>

        <!-- CỘT PHẢI: TÓM TẮT ĐƠN HÀNG (STICKY) -->
        <div class="w-full lg:w-[380px] shrink-0">
          <div class="sticky top-24 bg-white rounded-3xl p-6 lg:p-8 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100">
            <h3 class="text-sm font-black text-gray-900 border-b border-gray-100 pb-4 mb-6 uppercase tracking-widest">Tóm tắt đơn hàng</h3>
            
            <div class="flex gap-4 mb-6">
              <div class="w-12 h-12 bg-gray-50 rounded-xl flex items-center justify-center border border-gray-200 shrink-0">
                <span class="material-symbols-outlined text-gray-700">directions_bus</span>
              </div>
              <div>
                <p class="font-black text-gray-900 text-sm mb-1">{{ trip.companyName }}</p>
                <p class="text-xs text-gray-500 font-bold tracking-wide">{{ trip.departureTime }} • {{ trip.departureDate?.split('T')[0] || new Date().toISOString().split('T')[0] }}</p>
              </div>
            </div>

            <div class="space-y-4 text-sm border-b border-gray-100 pb-6 mb-6">
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Lộ trình</span>
                <span class="font-bold text-gray-800 text-sm">{{ trip.departurePoint }} <span class="text-gray-400 font-medium mx-1">→</span> {{ trip.arrivalPoint }}</span>
              </div>
              <div class="flex justify-between items-center bg-gray-50 p-3 rounded-xl border border-gray-100">
                <span class="text-[10px] text-gray-500 font-bold uppercase tracking-widest">Ghế ngồi</span>
                <span class="font-black text-[#075955] tracking-widest">{{ seatNames }}</span>
              </div>
            </div>

            <div class="flex justify-between items-end mb-8">
              <div>
                <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mb-1.5">Tổng thanh toán</p>
                <div class="flex items-center gap-2">
                  <p class="text-3xl font-black text-[#f03a17] leading-none">{{ totalAmount.toLocaleString() }}<span class="text-xl ml-0.5">đ</span></p>
                </div>
              </div>
            </div>

            <!-- Nút hoặc Đồng hồ chờ -->
            <div class="mt-2">
              <div v-if="!isFormComplete" class="w-full bg-gray-50 border border-dashed border-gray-200 rounded-2xl p-5 flex flex-col items-center justify-center gap-2 text-center">
                 <span class="material-symbols-outlined text-gray-300 text-3xl">edit_document</span>
                 <p class="text-xs font-bold text-gray-500">Vui lòng điền thông tin hành khách để tiếp tục</p>
              </div>

              <div v-else-if="selectedMethod === 'QR' && showQrCode" class="w-full bg-[#075955]/5 border border-[#075955]/20 rounded-2xl p-5 flex flex-col items-center justify-center gap-2 relative overflow-hidden group">
                <div class="absolute inset-0 bg-[#075955]/5 translate-y-full group-hover:translate-y-0 transition-transform duration-500"></div>
                <p class="text-[10px] text-[#075955] font-black uppercase tracking-widest relative z-10">Thời gian giữ chỗ</p>
                <div class="text-4xl font-black tabular-nums transition-colors duration-300 tracking-tight relative z-10" :class="timeLeft <= 60 ? 'text-red-500 animate-pulse' : 'text-[#075955]'">
                  {{ formattedTime }}
                </div>
                <div class="flex items-center gap-2 mt-2 relative z-10">
                   <div class="w-3.5 h-3.5 border-2 border-[#075955] border-t-transparent rounded-full animate-spin"></div>
                   <span class="text-[10px] font-bold text-[#075955] uppercase tracking-widest">Đang chờ nhận tiền...</span>
                </div>
              </div>

              <button v-else-if="selectedMethod === 'QR' && !showQrCode"
                @click="generateQrCode" :disabled="!isFormComplete"
                class="w-full bg-[#f03a17] hover:bg-[#d63314] text-white py-4 rounded-2xl font-black text-sm uppercase tracking-widest transition-all shadow-[0_4px_14px_rgba(240,58,23,0.3)] hover:shadow-[0_6px_20px_rgba(240,58,23,0.4)] hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none flex items-center justify-center gap-2"
              >
                Tạo mã QR thanh toán
                <span class="material-symbols-outlined text-lg">qr_code</span>
              </button>
              
              <button v-else
                @click="processPayment" :disabled="isProcessing || !isFormComplete"
                class="w-full bg-[#f03a17] hover:bg-[#d63314] text-white py-4 rounded-2xl font-black text-sm uppercase tracking-widest transition-all shadow-[0_4px_14px_rgba(240,58,23,0.3)] hover:shadow-[0_6px_20px_rgba(240,58,23,0.4)] hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none flex items-center justify-center gap-2"
              >
                <span v-if="isProcessing" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                {{ isProcessing ? 'Đang xử lý...' : (selectedMethod === 'CASH' ? 'Hoàn tất đặt vé' : 'Thanh toán ngay') }}
                <span v-if="!isProcessing" class="material-symbols-outlined text-lg">arrow_forward</span>
              </button>
            </div>
            
            <p class="text-[10px] text-gray-400 font-semibold text-center mt-5 uppercase tracking-wide">
              Bạn đồng ý với <a href="#" class="text-[#075955] font-bold hover:underline">Điều khoản</a> của nhà xe.
            </p>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const api = useApi();

const tripId = route.query.tripId;
const selectedSeatsStr = route.query.seats;
const seatsArray = computed(() => selectedSeatsStr ? selectedSeatsStr.split(',').map(s => s.trim()) : []);
const seatNames = computed(() => seatsArray.value.join(', '));
const totalAmount = computed(() => parseInt(route.query.total) || 0);

const trip = ref(null);
const loading = ref(true);
const isProcessing = ref(false);
const selectedMethod = ref('QR');
const showQrCode = ref(false);

const paymentSuccess = ref(false);
const timeLeft = ref(600); // 10 phút
let countdownTimer = null;

const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0');
  const s = (timeLeft.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

const startCountdown = () => {
  if (countdownTimer) clearInterval(countdownTimer);
  timeLeft.value = 600;
  countdownTimer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      clearInterval(countdownTimer);
      stopRealBankWebhook();
      alert('Thời gian giữ chỗ đã hết! Vui lòng đặt vé lại.');
      router.push('/');
    }
  }, 1000);
};
const customerName = ref('');
const customerPhone = ref('');
const customerEmail = ref('');

const wantsEmail = ref(true);

const isFormComplete = computed(() => {
  return customerName.value.trim() !== '' && 
         customerPhone.value.trim() !== '' && 
         (!wantsEmail.value || customerEmail.value.trim() !== '');
});

const generateQrCode = () => {
  showQrCode.value = true;
  startCountdown();
  startRealBankWebhook();
};

// currentUser reactive từ Pinia
const currentUser = authStore.currentUser;

const paymentMethods = computed(() => {
  const methods = [
    { id: 'QR', name: 'Quét mã QR', icon: 'qr_code_scanner' },
    { id: 'CASH', name: 'Thanh toán khi lên xe', icon: 'payments' }
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
  if (!customerName.value || !customerPhone.value) {
    return alert('Vui lòng nhập đầy đủ họ tên và số điện thoại hành khách!');
  }

  // 📧 KIỂM TRA ĐỊNH DẠNG EMAIL NẾU KHÁCH YÊU CẦU
  if (wantsEmail.value) {
    if (!customerEmail.value) {
      return alert('Vui lòng nhập Email để nhận vé điện tử!');
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(customerEmail.value.trim())) {
      return alert('Địa chỉ Email không đúng định dạng! Vui lòng kiểm tra lại.');
    }
  }

  // 📱 KIỂM TRA SỐ ĐIỆN THOẠI (KHÔNG NHẬN MÃ GG VÀ YÊU CẦU LÀ SỐ)
  const phoneRegex = /^[0-9]{10,11}$/;
  if (!phoneRegex.test(customerPhone.value.trim())) {
    return alert('Số điện thoại không hợp lệ! Vui lòng điền đúng 10-11 số thật của bạn để nhà xe liên hệ đón khách.');
  }
  
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
      customerEmail: wantsEmail.value ? customerEmail.value : 'no-email@smartbus.com',
      sendEmail: wantsEmail.value,
      seatNumbers: seatsArray.value,
      totalPrice: totalAmount.value,
      paymentMethod: selectedMethod.value,
      status: selectedMethod.value === 'CASH' ? 'PENDING' : 'PAID',
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

      paymentSuccess.value = true;

      setTimeout(() => {
        router.push({ 
          path: '/booking/payment-success', 
          query: { 
            bookingId: res.data.id, 
            from: trip.value.departurePoint, 
            to: trip.value.arrivalPoint, 
            time: trip.value.departureTime, 
            seats: seatNames.value, 
            total: totalAmount.value, 
            method: selectedMethod.value,
            customerName: customerName.value,
            customerPhone: customerPhone.value,
            date: trip.value.departureDate?.split('T')[0] || new Date().toISOString().split('T')[0],
            status: res.data.status || 'PAID'
          } 
        });
      }, 2500);
    }
  } catch (err) { alert('Thanh toán thất bại: ' + (err.response?.data?.error || err.message)); }
  finally { isProcessing.value = false; }
};

// --- TÍCH HỢP SEPAY THẬT (REAL BANK WEBHOOK) ---
let qrPollingTimer = null;

// Lấy thời điểm mở trang thanh toán để ngăn việc nhận nhầm giao dịch cũ
const sessionStartTime = new Date().toISOString();

const checkRealBankTransfer = async () => {
  // Tạo chuỗi nội dung giống hệt mã QR để so khớp
  const expectedContent = `Ve xe ${seatNames.value}`.replace(/[, \-]/g, '').toLowerCase();
  
  try {
    const res = await api.get('/admin/bookings/check-payment', {
      params: {
        expectedContent: expectedContent,
        expectedAmount: totalAmount.value,
        sessionStartTime: sessionStartTime
      }
    });
    
    if (res.data && res.data.success) {
      // Nhận được tiền thật -> Xử lý lưu vé
      stopRealBankWebhook();
      processPayment();
    }
  } catch (err) {
    console.error("Lỗi kiểm tra giao dịch SePay:", err);
  }
};

const startRealBankWebhook = () => {
  stopRealBankWebhook();
  if (!isFormComplete.value) return; // Không chạy nếu form chưa điền
  
  // Mỗi 3 giây gọi API lên Backend kiểm tra một lần
  qrPollingTimer = setInterval(() => {
    if (selectedMethod.value === 'QR' && !isProcessing.value) {
       checkRealBankTransfer();
    }
  }, 3000);
};

const stopRealBankWebhook = () => {
  if (qrPollingTimer) clearInterval(qrPollingTimer);
};

watch(isFormComplete, (newComplete) => {
  if (!newComplete) {
    showQrCode.value = false;
    stopRealBankWebhook();
    if (countdownTimer) {
      clearInterval(countdownTimer);
      countdownTimer = null;
      timeLeft.value = 600;
    }
  }
});

watch(selectedMethod, () => {
  showQrCode.value = false;
  stopRealBankWebhook();
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
    timeLeft.value = 600;
  }
});

onMounted(() => {
  if (authStore.isLoggedIn) {
    customerName.value = authStore.currentUser?.fullName || '';
    customerPhone.value = authStore.currentUser?.phone?.startsWith('GG_') ? '' : (authStore.currentUser?.phone || '');
    customerEmail.value = authStore.currentUser?.email || '';
    selectedMethod.value = 'WALLET';
  } else {
    selectedMethod.value = 'QR';
  }
  fetchTrip();
});

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  stopRealBankWebhook();
});
</script>

<style scoped>
@keyframes scan {
  0%, 100% { transform: translateY(0); opacity: 0; }
  10%, 90% { opacity: 1; }
  50% { transform: translateY(210px); }
}
.qr-scan-line {
  animation: scan 3s ease-in-out infinite;
}
</style>