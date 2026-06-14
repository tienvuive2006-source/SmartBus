<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-800">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-2 cursor-pointer" @click="$router.push('/')">
          <span class="material-symbols-outlined text-white text-4xl">directions_bus</span>
          <div class="flex flex-col">
            <span class="text-xl font-bold leading-none tracking-tight">Trung - Nam</span>
            <span class="text-[9px] uppercase tracking-wider font-semibold">Nhà xe chuyên tuyến Miền Trung - Nam</span>
          </div>
        </div>
        <div class="flex items-center gap-6">
          <button @click="$router.push('/')" class="text-sm font-semibold hover:text-yellow-300 transition-colors flex items-center gap-1">
            <span class="material-symbols-outlined text-xl">home</span>
            Trang chủ
          </button>
        </div>
      </div>
    </nav>

    <div class="pb-12 px-4 animate-fade-in bg-[#f2f5f8]">
    <main class="max-w-3xl mx-auto space-y-6 py-6">
      <div class="bg-gradient-to-r from-[#075955] to-[#05403d] p-6 rounded-3xl shadow-md border border-[#05403d] relative overflow-hidden text-white mb-4">
        <div class="absolute -right-6 -bottom-6 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
        <h1 class="text-headline-md font-black flex items-center gap-2">
          <span class="material-symbols-outlined text-yellow-300 text-[32px]">confirmation_number</span>
          Lịch sử đặt vé
        </h1>
        <p class="text-label-md opacity-80 mt-1 tracking-widest font-bold">QUẢN LÝ TOÀN BỘ HÀNH TRÌNH CỦA BẠN</p>
      </div>

      <div class="flex bg-white p-1.5 rounded-2xl border border-gray-200 mb-6 shadow-sm">
        <button 
          @click="activeTab = 'upcoming'"
          :class="['flex-1 py-3 rounded-xl text-body-md font-black transition-all duration-200 flex items-center justify-center gap-1.5', activeTab === 'upcoming' ? 'bg-[#075955] text-white shadow-sm' : 'text-gray-500 hover:bg-gray-100']"
        >
          <span class="material-symbols-outlined text-[18px]">upcoming</span>
          ĐANG HOẠT ĐỘNG ({{ upcomingTickets.length }})
        </button>
        <button 
          @click="activeTab = 'completed'"
          :class="['flex-1 py-3 rounded-xl text-body-md font-black transition-all duration-200 flex items-center justify-center gap-1.5', activeTab === 'completed' ? 'bg-[#075955] text-white shadow-sm' : 'text-gray-500 hover:bg-gray-100']"
        >
          <span class="material-symbols-outlined text-[18px]">done_all</span>
          ĐÃ QUA (0)
        </button>
      </div>

      <div class="flex flex-col gap-6" v-if="activeTab === 'upcoming'">
        <div v-if="upcomingTickets.length === 0" class="flex flex-col items-center justify-center py-16 bg-white border border-dashed border-gray-300 rounded-3xl shadow-inner text-center px-6">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-gray-400">
            <span class="material-symbols-outlined text-5xl">search_off</span>
          </div>
          <h3 class="text-headline-sm font-black text-gray-800">Chưa có vé nào ở đây!</h3>
          <p class="text-body-md text-gray-500 mt-1 max-w-sm">Bạn chưa mua vé nào, hoặc lịch sử của trình duyệt đã bị xóa sạch.</p>
          <button @click="$router.push('/')" class="mt-6 bg-[#f03a17] text-white px-6 py-2.5 rounded-xl font-black text-body-md shadow-md hover:bg-[#d63314] transition-all active:scale-95">
            MUA VÉ NGAY
          </button>
        </div>

        <div 
          v-for="(ticket, index) in upcomingTickets" 
          :key="ticket.id || index"
          class="bg-white rounded-3xl shadow-[0px_8px_30px_rgba(0,0,0,0.03)] overflow-hidden border border-gray-200 relative group animate-slide-up hover:shadow-md transition-all duration-300"
          :style="`animation-delay: ${index * 0.1}s`"
        >
          <div class="h-1.5 w-full bg-emerald-500"></div>

          <div class="p-6 relative">
            <div class="flex justify-between items-start mb-5">
              <div class="flex flex-col">
                <div class="flex items-center gap-2 mb-3">
                  <span class="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-emerald-100/50 shadow-sm" v-if="ticket.status === 'PAID'">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    ĐÃ THANH TOÁN
                  </span>
                  <span class="inline-flex items-center gap-1 bg-amber-50 text-amber-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-amber-100/50 shadow-sm" v-else-if="ticket.status === 'PENDING'">
                    <span class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                    CHỜ THANH TOÁN
                  </span>
                  <span class="inline-flex items-center gap-1 bg-rose-50 text-rose-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-rose-100/50 shadow-sm" v-else-if="ticket.status === 'CANCELLED'">
                    <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                    ĐÃ HỦY
                  </span>
                  <span class="inline-flex items-center gap-1 bg-blue-50 text-blue-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-blue-100/50 shadow-sm" v-else-if="ticket.status === 'CHECKED_IN'">
                    <span class="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
                    ĐÃ LÊN XE
                  </span>
                  <span class="text-[11px] font-bold text-gray-400">#{{ ticket.id }}</span>
                </div>
                <h2 class="text-headline-sm font-black text-gray-800 flex items-center gap-2 leading-tight">
                  {{ ticket.from }} 
                  <span class="material-symbols-outlined text-[#075955] text-[20px] animate-pulse">east</span> 
                  {{ ticket.to }}
                </h2>
                <p class="text-body-sm font-bold text-gray-600 mt-1 bg-gray-50 px-2 py-1 rounded-lg w-fit border border-gray-100">
                  🚀 Trung - Nam Premium • {{ ticket.busType }}
                </p>
              </div>

              <div class="flex gap-2">
                
                <button 
                  v-if="(ticket.status === 'PAID' || ticket.status === 'CHECKED_IN' || ticket.status === 'COMPLETED') && !ticket.isReviewed"
                  @click="openReviewModal(ticket)"
                  class="p-3 bg-amber-50 border border-amber-100 rounded-2xl text-amber-500 hover:bg-amber-500 hover:text-white transition-all duration-300 shadow-sm active:scale-95 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black" style="font-variation-settings: 'FILL' 1;">star</span>
                  <span class="text-[9px] font-black tracking-widest">ĐÁNH GIÁ</span>
                </button>
                <div 
                  v-else-if="(ticket.status === 'PAID' || ticket.status === 'CHECKED_IN' || ticket.status === 'COMPLETED') && ticket.isReviewed"
                  class="p-3 bg-gray-50 border border-gray-100 rounded-2xl text-gray-400 flex flex-col items-center gap-0.5 cursor-not-allowed opacity-70"
                >
                  <span class="material-symbols-outlined text-[28px] font-black" style="font-variation-settings: 'FILL' 1;">check_circle</span>
                  <span class="text-[9px] font-black tracking-widest">ĐÃ ĐÁNH GIÁ</span>
                </div>
<button 
                  v-if="ticket.status === 'PAID' || ticket.status === 'PENDING'"
                  @click="openCancelModal(ticket)"
                  class="p-3 bg-rose-50 border border-rose-100 rounded-2xl text-rose-500 hover:bg-rose-500 hover:text-white transition-all duration-300 shadow-sm active:scale-95 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black">free_cancellation</span>
                  <span class="text-[9px] font-black tracking-widest">HỦY VÉ</span>
                </button>
                <button 
                  v-if="ticket.status !== 'CANCELLED'"
                  @click="openQrModal(ticket)"
                  class="p-3 bg-[#075955]/5 border border-[#075955]/10 rounded-2xl text-[#075955] hover:bg-[#075955] hover:text-white transition-all duration-300 shadow-sm active:scale-95 group-hover:scale-105 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black">qr_code_2</span>
                  <span class="text-[9px] font-black tracking-widest">QUÉT MÃ</span>
                </button>
              </div>
            </div>

            <div class="border-t border-dashed border-gray-300 my-4 relative">
              <div class="absolute -left-[34px] top-1/2 -translate-y-1/2 w-4 h-4 bg-slate-50 rounded-full border border-gray-200 shadow-inner"></div>
              <div class="absolute -right-[34px] top-1/2 -translate-y-1/2 w-4 h-4 bg-slate-50 rounded-full border border-gray-200 shadow-inner"></div>
            </div>

            <div class="flex justify-between items-end">
              <div class="grid grid-cols-2 gap-x-8 gap-y-2">
                <div>
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Giờ khởi hành</p>
                  <p class="text-body-md font-black text-gray-800 flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm text-[#075955]">schedule</span>
                    {{ ticket.time }} ({{ ticket.date }})
                  </p>
                </div>
                <div>
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Vị trí Ghế</p>
                  <p class="text-body-md font-black text-[#f03a17] uppercase flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chair</span>
                    {{ ticket.seats }}
                  </p>
                </div>
              </div>
              <div class="text-right border-l border-gray-100 pl-4">
                <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Đã trả qua {{ ticket.method || 'Ví MoMo' }}</p>
                <p class="text-headline-sm font-black text-[#075955] tracking-tight">{{ parseFloat(ticket.total).toLocaleString('vi-VN') }}đ</p>
              </div>
            </div>
            
            <!-- Review Section (if already reviewed) -->
            <div v-if="ticket.userReview" class="mt-4 p-4 bg-amber-50/50 rounded-2xl border border-amber-100/50 flex flex-col gap-2">
              <div class="flex items-center gap-1.5 mb-1">
                <span class="text-[10px] font-black uppercase tracking-widest text-amber-600/70">Đánh giá của bạn</span>
                <div class="flex">
                  <span v-for="star in ticket.userReview.rating" :key="star" class="material-symbols-outlined text-[14px] text-amber-400" style="font-variation-settings: 'FILL' 1;">star</span>
                  <span v-for="star in (5 - ticket.userReview.rating)" :key="'empty'+star" class="material-symbols-outlined text-[14px] text-gray-300" style="font-variation-settings: 'FILL' 1;">star</span>
                </div>
              </div>
              <p v-if="ticket.userReview.comment" class="text-sm font-semibold text-gray-700 italic">"{{ ticket.userReview.comment }}"</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="flex flex-col items-center justify-center py-20 bg-white border border-gray-200 rounded-3xl shadow-sm text-center px-6 animate-fade-in">
        <span class="material-symbols-outlined text-6xl text-gray-300 mb-4 animate-pulse">history_toggle_off</span>
        <h3 class="text-headline-sm font-black text-gray-800">Không có dữ liệu quá khứ</h3>
        <p class="text-body-md text-gray-500 mt-1">Các chuyến đi sau khi hoàn thành lộ trình sẽ tự động lưu tại đây.</p>
      </div>
    </main>

    <div v-if="isModalOpen && selectedTicket" class="fixed inset-0 bg-slate-900/85 backdrop-blur-sm z-[999] flex items-center justify-center p-6 animate-fade-in" @click.self="closeModal">
      <div class="bg-white w-full max-w-sm rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.3)] overflow-hidden border border-white/20 animate-scale-up flex flex-col items-center relative">
        <button @click="closeModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-900 w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center transition-all active:scale-90">
          <span class="material-symbols-outlined">close</span>
        </button>

        <div class="p-6 w-full text-center">
          <h3 class="text-headline-sm font-black text-gray-900 mb-1">Mã Lên Xe Chi Nhánh</h3>
          <p class="text-[11px] font-bold text-[#075955] uppercase tracking-widest mb-6">{{ selectedTicket.id }}</p>
          
          <div class="bg-gray-50 p-5 rounded-3xl border border-gray-200 mb-4 inline-block shadow-inner relative group">
            <img 
              :src="`https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent('BOOKING:' + selectedTicket.id + '|SEATS:' + selectedTicket.seats + '|FROM:' + selectedTicket.from)}&color=075955&bgcolor=f8fafc`" 
              alt="Modal QR" 
              class="w-44 h-44 group-hover:scale-105 transition-transform duration-300 mix-blend-multiply"
            />
          </div>
          <p class="text-[11px] font-black text-gray-500 uppercase tracking-widest leading-relaxed px-4">
            VUI LÒNG ĐƯA MÃ NÀY CHO TÀI XẾ KHI BƯỚC LÊN XE
          </p>
        </div>
        
        <div class="w-full bg-gray-50 px-6 py-4 border-t border-gray-200 text-center">
          <p class="text-body-md font-black text-gray-800">
            {{ selectedTicket.from }} <span class="text-[#075955]">→</span> {{ selectedTicket.to }}
          </p>
          <p class="text-[10px] font-bold text-gray-500 uppercase tracking-wider mt-0.5">
            Ghế {{ selectedTicket.seats }} • Khởi hành {{ selectedTicket.time }}
          </p>
        </div>
      </div>
    </div>

    
    <!-- Review Modal -->
    <div v-if="isReviewModalOpen && selectedTicket" class="fixed inset-0 bg-slate-900/85 backdrop-blur-sm z-[999] flex items-center justify-center p-6 animate-fade-in" @click.self="closeReviewModal">
      <div class="bg-white w-full max-w-sm rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.3)] border border-white/20 animate-scale-up p-6 relative">
        <button @click="closeReviewModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-900 w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center transition-all active:scale-90">
          <span class="material-symbols-outlined">close</span>
        </button>

        <h3 class="text-headline-sm font-black text-amber-500 mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">stars</span> Đánh giá Nhà xe
        </h3>
        <p class="text-xs text-gray-500 font-medium mb-4 leading-relaxed">
          Chuyến: <b class="text-gray-800">{{ selectedTicket.from }} ➝ {{ selectedTicket.to }}</b>.<br/>
          Dòng xe: <b class="text-amber-600">{{ selectedTicket.busType }}</b>
        </p>

        <div class="flex items-center gap-3 bg-gray-50 p-3 rounded-xl mb-6 border border-gray-100">
           <img :src="selectedTicket.imageUrl || 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=400'" 
                class="w-16 h-16 object-cover rounded-lg shadow-sm" alt="Bus Image" />
           <div class="flex flex-col">
              <span class="text-xs text-gray-500 uppercase font-bold tracking-widest">Tài xế</span>
              <span class="text-sm font-black text-gray-800">{{ selectedTicket.driverName }}</span>
              <span class="text-[11px] font-semibold text-gray-500 mt-0.5 flex items-center gap-1">
                 <span class="material-symbols-outlined text-[12px]">directions_bus</span> BKS: {{ selectedTicket.licensePlate || 'Chưa xếp xe' }}
              </span>
           </div>
        </div>

        <div class="flex justify-center gap-2 mb-6">
          <span 
            v-for="star in 5" 
            :key="star"
            @click="reviewForm.rating = star"
            class="material-symbols-outlined text-5xl cursor-pointer transition-colors"
            :class="star <= reviewForm.rating ? 'text-amber-400' : 'text-gray-200'"
            style="font-variation-settings: 'FILL' 1;"
          >star</span>
        </div>

        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">Bình luận (Tùy chọn)</label>
        <textarea v-model="reviewForm.comment" rows="3" placeholder="Nhà xe phục vụ như thế nào?..." class="w-full border border-gray-200 rounded-xl px-4 py-3 bg-gray-50 text-sm font-semibold text-gray-700 outline-none focus:border-amber-500 mb-6 resize-none"></textarea>

        <button 
          @click="submitReview" 
          :disabled="reviewForm.rating === 0 || isSubmittingReview"
          class="w-full bg-amber-500 text-white font-black py-3.5 rounded-xl text-sm uppercase tracking-widest shadow-md hover:bg-amber-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all active:scale-95"
        >
          <span v-if="isSubmittingReview" class="w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin"></span>
          {{ isSubmittingReview ? 'Đang gửi...' : 'Gửi Đánh Giá' }}
        </button>
      </div>
    </div>

    <!-- Hủy Vé Modal -->
    <div v-if="isCancelModalOpen && selectedTicket" class="fixed inset-0 bg-slate-900/85 backdrop-blur-sm z-[999] flex items-center justify-center p-6 animate-fade-in" @click.self="closeCancelModal">
      <div class="bg-white w-full max-w-sm rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.3)] border border-white/20 animate-scale-up p-6 relative">
        <button @click="closeCancelModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-900 w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center transition-all active:scale-90">
          <span class="material-symbols-outlined">close</span>
        </button>

        <h3 class="text-headline-sm font-black text-rose-600 mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined">warning</span> Hủy vé xe
        </h3>
        <p class="text-xs text-gray-500 font-medium mb-6 leading-relaxed">
          Mã vé: <b>#{{ selectedTicket.id }}</b>. {{ selectedTicket.status === 'PAID' && selectedTicket.method !== 'CASH' ? 'Bạn sẽ được hoàn lại 90% số tiền vào Ví Trung Nam.' : 'Thao tác này không thể hoàn tác.' }}
        </p>

        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">Lý do hủy vé (Bắt buộc)</label>
        <select v-model="cancelReason" class="w-full border border-gray-200 rounded-xl px-4 py-3 bg-gray-50 text-sm font-semibold text-gray-700 outline-none focus:border-[#075955] mb-4">
          <option value="" disabled>-- Chọn lý do --</option>
          <option value="Thay đổi lịch trình">Thay đổi lịch trình</option>
          <option value="Tìm được xe khác phù hợp hơn">Tìm được xe khác phù hợp hơn</option>
          <option value="Đặt nhầm ngày/giờ">Đặt nhầm ngày/giờ</option>
          <option value="Lý do cá nhân">Lý do cá nhân</option>
        </select>

        <div v-if="selectedTicket.status === 'PAID' && selectedTicket.method !== 'CASH'" class="bg-amber-50 p-3 rounded-xl border border-amber-100 mb-6 flex items-start gap-2">
           <span class="material-symbols-outlined text-amber-500 text-lg">account_balance_wallet</span>
           <div>
             <p class="text-xs font-bold text-amber-700">Số tiền hoàn lại (90%)</p>
             <p class="text-base font-black text-amber-600">+{{ (selectedTicket.total * 0.9).toLocaleString('vi-VN') }}đ</p>
           </div>
        </div>

        <button 
          @click="confirmCancel" 
          :disabled="!cancelReason || isCancelling"
          class="w-full bg-rose-500 text-white font-black py-3.5 rounded-xl text-sm uppercase tracking-widest shadow-md hover:bg-rose-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all active:scale-95"
        >
          <span v-if="isCancelling" class="w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin"></span>
          {{ isCancelling ? 'Đang xử lý...' : 'Xác nhận Hủy Vé' }}
        </button>
      </div>
    </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

const activeTab = ref('upcoming');
const upcomingTickets = ref([]);
const isModalOpen = ref(false);
const selectedTicket = ref(null);
const allBuses = ref([]);

const authStore = useAuthStore();
const api = useApi();

const loadHistory = async () => {
  if (authStore.isLoggedIn) {
    try {
      const [response, busesRes] = await Promise.all([
        api.get('/auth/me/bookings'),
        api.get('/buses').catch(() => ({ data: [] }))
      ]);
      allBuses.value = busesRes.data;
      if (response.data && Array.isArray(response.data)) {
        upcomingTickets.value = response.data.map(b => {
          const bus = allBuses.value.find(bus => bus.licensePlate === b.trip.assignedLicensePlate);
          return {
            id: b.id,
            from: b.trip.departurePoint,
            to: b.trip.arrivalPoint,
            busType: b.trip.busType,
            time: b.trip.departureTime,
            date: b.trip.departureDate,
            seats: Array.isArray(b.seatNumbers) ? b.seatNumbers.join(', ') : b.seatNumbers,
            method: b.paymentMethod,
            total: b.totalPrice,
            status: b.status,
            imageUrl: b.trip.imageUrl,
            licensePlate: b.trip.assignedLicensePlate,
            driverName: bus ? bus.driverName : 'Đang cập nhật',
            isReviewed: b.reviewed || b.isReviewed || false,
            userReview: b.userReview || null
          };
        });
        return;
      }
    } catch (err) {
      console.error("Lỗi lấy lịch sử từ server:", err);
    }
  }

  // Fallback (cho khách vãng lai hoặc khi lỗi API)
  const stored = localStorage.getItem('trungnam_history') || localStorage.getItem('saomaifly_history') || localStorage.getItem('skybus_history');
  if (stored) {
    try {
      upcomingTickets.value = JSON.parse(stored);
    } catch (err) {
      console.error("Lỗi nạp lịch sử:", err);
      upcomingTickets.value = [];
    }
  } else {
    upcomingTickets.value = [];
  }
};

const openQrModal = (ticket) => {
  selectedTicket.value = ticket;
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};


// Đánh giá nhà xe
const isReviewModalOpen = ref(false);
const isSubmittingReview = ref(false);
const reviewForm = ref({ rating: 0, comment: '' });

const openReviewModal = (ticket) => {
  selectedTicket.value = ticket;
  reviewForm.value = { rating: 5, comment: '' };
  isReviewModalOpen.value = true;
};

const closeReviewModal = () => {
  isReviewModalOpen.value = false;
};

const submitReview = async () => {
  if (reviewForm.value.rating === 0) return;
  isSubmittingReview.value = true;
  try {
    const user = authStore.currentUser;
    if (!user) throw new Error("Vui lòng đăng nhập!");
    await api.post(`/reviews/create/${user.id}`, {
      bookingId: selectedTicket.value.id,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment
    });
    alert("Cảm ơn bạn đã đánh giá chuyến đi!");
    if (selectedTicket.value) {
      selectedTicket.value.isReviewed = true;
      selectedTicket.value.userReview = {
        rating: reviewForm.value.rating,
        comment: reviewForm.value.comment
      };
    }
    closeReviewModal();
  } catch (err) {
    alert("Gửi đánh giá thất bại: " + (err.response?.data?.message || err.message));
  } finally {
    isSubmittingReview.value = false;
  }
};

// Hủy vé
const isCancelModalOpen = ref(false);
const cancelReason = ref('');
const isCancelling = ref(false);

const openCancelModal = (ticket) => {
  selectedTicket.value = ticket;
  cancelReason.value = '';
  isCancelModalOpen.value = true;
};

const closeCancelModal = () => {
  isCancelModalOpen.value = false;
};

const confirmCancel = async () => {
  if (!cancelReason.value) return;
  isCancelling.value = true;
  try {
    const res = await api.post(`/auth/me/bookings/${selectedTicket.value.id}/cancel`, {
      reason: cancelReason.value
    });
    
    // Cập nhật số dư ví trong store nếu có
    if (res.data.walletBalance !== undefined) {
      authStore.updateWalletBalance(res.data.walletBalance);
    }
    
    alert(`Đã hủy vé thành công! ${res.data.refundAmount ? 'Bạn được hoàn +' + res.data.refundAmount.toLocaleString('vi-VN') + 'đ vào ví.' : ''}`);
    isCancelModalOpen.value = false;
    loadHistory(); // Tải lại danh sách
  } catch (err) {
    alert("Hủy vé thất bại: " + (err.response?.data || err.message));
  } finally {
    isCancelling.value = false;
  }
};

onMounted(() => {
  loadHistory();
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes scaleUp {
  from { transform: scale(0.9) translateY(10px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-slide-up {
  animation: slideUp 0.45s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>