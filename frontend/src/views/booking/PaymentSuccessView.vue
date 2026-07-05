<template>
  <div class="min-h-screen bg-[#f4f7f6] font-sans text-slate-800 pb-20 no-print">


    <!-- Main Content Container -->
    <div class="flex flex-col items-center pt-8 px-4">
      <main class="w-full max-w-md animate-fade-in-up">
        
        <!-- Header Banner Success -->
        <div class="text-center mb-8">
          <div class="w-20 h-20 bg-emerald-500 text-white rounded-full flex items-center justify-center mx-auto mb-5 shadow-[0_0_30px_rgba(16,185,129,0.3)] relative overflow-hidden group">
            <span class="material-symbols-outlined text-4xl font-black animate-checkmark">check</span>
            <div class="absolute inset-0 bg-white/20 -translate-x-full group-hover:translate-x-full transition-transform duration-1000 ease-out"></div>
          </div>
          <h1 class="text-2xl font-black text-gray-900 tracking-tight uppercase mb-2">{{ status === 'PENDING' ? 'Giữ chỗ thành công!' : 'Thanh toán thành công!' }}</h1>
          <p class="text-sm font-bold rounded-full px-4 py-1.5 inline-block shadow-sm"
             :class="status === 'PENDING' ? 'text-amber-600 bg-amber-50 border border-amber-100' : 'text-emerald-600 bg-emerald-50 border border-emerald-100'">
            {{ status === 'PENDING' ? 'Vui lòng thanh toán tiền mặt khi lên xe' : 'Cảm ơn bạn đã đồng hành cùng Trung - Nam' }}
          </p>
        </div>

        <!-- Majestic Ticket Card -->
        <div class="bg-white rounded-3xl shadow-[0_10px_40px_rgba(0,0,0,0.06)] border border-gray-100 overflow-hidden relative print-card">
          
          <!-- Semicircular Ticket Punches -->
          <div class="absolute left-0 top-[30%] -ml-3 w-6 h-6 bg-[#f4f7f6] rounded-full border-r border-gray-200 z-10 no-print shadow-inner"></div>
          <div class="absolute right-0 top-[30%] -mr-3 w-6 h-6 bg-[#f4f7f6] rounded-full border-l border-gray-200 z-10 no-print shadow-inner"></div>

          <!-- Ticket Header -->
          <div class="p-6 md:p-8 bg-emerald-50/30 border-b border-dashed border-gray-200 flex flex-wrap justify-between items-start md:items-center relative gap-4">
            <div class="flex flex-col gap-1 min-w-0 flex-1">
              <span class="text-[10px] font-bold text-gray-500 uppercase tracking-widest">Mã số vé</span>
              <span class="text-xl md:text-2xl font-black text-gray-900 tracking-widest break-all">{{ bookingId }}</span>
            </div>
            <div class="shrink-0">
              <span class="inline-flex items-center gap-1.5 text-[10px] font-black uppercase border px-3 py-1.5 rounded-xl shadow-sm animate-pulse-subtle"
                    :class="status === 'PENDING' ? 'text-amber-600 bg-amber-50 border-amber-100' : 'text-emerald-600 bg-emerald-50 border-emerald-100'">
                <span class="material-symbols-outlined text-sm">{{ status === 'PENDING' ? 'schedule' : 'verified' }}</span>
                {{ status === 'PENDING' ? 'Chờ thanh toán' : 'Đã thanh toán' }}
              </span>
            </div>
          </div>
          
          <!-- Ticket Body -->
          <div class="p-6 md:p-8 space-y-6">
            <!-- Timeline Route -->
            <div class="flex gap-4">
              <div class="flex flex-col items-center shrink-0 mt-1">
                <div class="w-6 h-6 rounded-full bg-[#075955] flex items-center justify-center text-white shadow-md">
                  <span class="material-symbols-outlined text-[14px]">my_location</span>
                </div>
                <div class="w-1 h-12 border-l-2 border-dashed border-gray-300 my-1"></div>
                <div class="w-6 h-6 rounded-full bg-[#f03a17] flex items-center justify-center text-white shadow-md">
                  <span class="material-symbols-outlined text-[14px]">location_on</span>
                </div>
              </div>
              <div class="flex-grow flex flex-col justify-between py-1">
                <div class="flex flex-col">
                  <p class="text-[10px] font-bold tracking-widest text-gray-400 uppercase mb-1">Điểm đi</p>
                  <h4 class="text-base font-black text-gray-900">{{ fromCity }}</h4>
                </div>
                <div class="flex flex-col mt-4">
                  <p class="text-[10px] font-bold tracking-widest text-gray-400 uppercase mb-1">Điểm đến</p>
                  <h4 class="text-base font-black text-gray-900">{{ toCity }}</h4>
                </div>
              </div>
            </div>

            <!-- Details Grid -->
            <div class="grid grid-cols-2 gap-4 pt-6 border-t border-gray-100">
              <div class="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-1">Khởi hành</p>
                <p class="text-sm font-black text-gray-900 flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-[#075955] text-base">schedule</span>
                  {{ depTime }}
                </p>
                <p class="text-xs text-gray-500 font-semibold mt-1">{{ depDate }}</p>
              </div>
              <div class="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-1">Số ghế</p>
                <p class="text-sm font-black text-[#075955] flex items-center gap-1.5 uppercase tracking-widest">
                  <span class="material-symbols-outlined text-[#f03a17] text-base">airline_seat_recline_normal</span>
                  {{ seatsSelected }}
                </p>
                <p class="text-xs text-gray-500 font-semibold mt-1">Loại xe giường nằm</p>
              </div>
            </div>
            
            <!-- Passenger Info -->
            <div class="bg-gray-50 p-4 rounded-2xl border border-gray-100 flex flex-col">
               <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-1.5">Hành khách</p>
               <p class="text-sm font-black text-gray-900 uppercase tracking-wide">{{ customerName }}</p>
               <p class="text-xs text-gray-500 font-semibold mt-1">{{ customerPhone }}</p>
            </div>
          </div>

          <!-- Ticket Footer / QR Code -->
          <div class="py-6 px-6 md:px-8 bg-gray-50 border-t border-gray-200 flex flex-col items-center relative">
            <!-- Decorative Barcode Line -->
            <div class="absolute top-0 left-6 right-6 h-px bg-dashed-line"></div>
            
            <div class="bg-white p-3 border border-gray-200 rounded-2xl mb-4 shadow-sm hover:shadow-md hover:scale-105 transition-all duration-300">
              <img :src="qrCodeUrl" class="w-36 h-36" alt="Mã vé QR" />
            </div>
            <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest flex items-center gap-1.5">
              <span class="material-symbols-outlined text-emerald-500 text-[14px]">qr_code_scanner</span>
              Vui lòng xuất trình mã khi lên xe
            </p>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="mt-8 flex gap-4 no-print">
          <button @click="windowPrint" class="flex-1 bg-white border-2 border-gray-200 hover:bg-gray-50 hover:border-gray-300 text-gray-700 py-4 rounded-2xl font-black text-[11px] uppercase tracking-widest transition-all duration-200 active:scale-95 shadow-sm flex items-center justify-center gap-2">
            <span class="material-symbols-outlined text-lg">print</span>
            Lưu / In vé
          </button>
          <button @click="$router.push('/')" class="flex-1 bg-[#075955] hover:bg-[#05403d] text-white py-4 rounded-2xl font-black text-[11px] uppercase tracking-widest transition-all duration-200 active:scale-95 shadow-[0_4px_14px_rgba(7,89,85,0.3)] hover:shadow-lg flex items-center justify-center gap-2">
            Trang chủ
            <span class="material-symbols-outlined text-lg">arrow_forward</span>
          </button>
        </div>
      </main>
    </div>
  </div>

  <!-- Print Only Layout (Only displays during print) -->
  <div class="hidden print-only">
    <div class="flex flex-col items-center p-8 bg-white max-w-lg mx-auto border-2 border-slate-200 rounded-3xl">
      <div class="text-center mb-6">
        <h2 class="text-2xl font-black text-[#075955] uppercase tracking-widest">VÉ XE KHÁCH TRUNG - NAM</h2>
        <p class="text-sm text-slate-500 font-semibold mt-1">Chuyên Tuyến Miền Trung - Nam</p>
      </div>
      <div class="w-full border-t-2 border-b-2 border-slate-200 py-6 my-6 space-y-4">
        <div class="flex justify-between items-center"><span class="text-slate-500 uppercase font-bold text-xs">Mã vé:</span><strong class="text-slate-900 text-xl font-black">{{ bookingId }}</strong></div>
        <div class="flex justify-between"><span class="text-slate-500 uppercase font-bold text-xs">Khách hàng:</span><strong class="text-slate-900 text-sm font-black">{{ customerName }} - {{ customerPhone }}</strong></div>
        <div class="flex justify-between"><span class="text-slate-500 uppercase font-bold text-xs">Khởi hành:</span><strong class="text-slate-900 text-sm font-bold">{{ fromCity }}</strong></div>
        <div class="flex justify-between"><span class="text-slate-500 uppercase font-bold text-xs">Điểm đến:</span><strong class="text-slate-900 text-sm font-bold">{{ toCity }}</strong></div>
        <div class="flex justify-between"><span class="text-slate-500 uppercase font-bold text-xs">Thời gian:</span><strong class="text-slate-900 text-sm font-bold">{{ depTime }} - {{ depDate }}</strong></div>
        <div class="flex justify-between"><span class="text-slate-500 uppercase font-bold text-xs">Số ghế:</span><strong class="text-[#075955] text-lg font-black uppercase">{{ seatsSelected }}</strong></div>
      </div>
      <div class="flex flex-col items-center mt-6">
        <img :src="qrCodeUrl" class="w-48 h-48 border-2 border-slate-200 p-2 rounded-2xl" />
        <p class="text-[10px] font-bold text-slate-500 mt-4 uppercase tracking-widest">Đưa mã QR này cho nhân viên soát vé khi lên xe</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

// Đọc mã vé từ query hoặc tự sinh mã Trung - Nam chuyên nghiệp
const bookingId = computed(() => {
  if (route.query.bookingId) return route.query.bookingId;
  return 'TN-' + Math.floor(100000 + Math.random() * 900000);
});

const fromCity = computed(() => route.query.from || 'Bến xe Khởi hành');
const toCity = computed(() => route.query.to || 'Bến xe Điểm đến');
const depTime = computed(() => route.query.time || '08:00');
const seatsSelected = computed(() => route.query.seats || route.query.seat || 'B02, B05');
const depDate = computed(() => route.query.date || new Date().toISOString().split('T')[0]);
const customerName = computed(() => route.query.customerName || 'Khách hàng');
const customerPhone = computed(() => route.query.customerPhone || 'N/A');
const status = computed(() => route.query.status || 'PAID');

const qrCodeUrl = computed(() => {
  const formattedPrice = Number(route.query.total || 0).toLocaleString('vi-VN');

  const data = `Mã đặt vé: #${bookingId.value}
Khách hàng: ${customerName.value}
SĐT: ${customerPhone.value}
Hành trình: ${fromCity.value} -> ${toCity.value}
Ngày đi: ${depDate.value}
Giờ đi: ${depTime.value}
Số ghế: ${seatsSelected.value}
Tổng tiền: ${formattedPrice} VND
Trạng thái: ${status.value}`;

  // Sinh mã QR cao cấp sử dụng tông màu chính của nhà xe Trung - Nam (#075955)
  return `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(data)}&color=075955&bgcolor=ffffff`;
});

const windowPrint = () => {
  window.print();
};
</script>

<style scoped>
/* Hiệu ứng Fade-in-up mượt mà khi tải trang */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes checkmark {
  0% {
    transform: scale(0.85);
    opacity: 0;
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.animate-checkmark {
  animation: checkmark 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

@keyframes pulseSubtle {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 0 8px rgba(16, 185, 129, 0.12);
  }
}

.animate-pulse-subtle {
  animation: pulseSubtle 2.5s infinite ease-in-out;
}

/* Đường chấm đứt nét trang trí phong cách cuống vé */
.bg-dashed-line {
  background-image: linear-gradient(to right, #cbd5e1 50%, rgba(255, 255, 255, 0) 0%);
  background-position: bottom;
  background-size: 6px 1px;
  background-repeat: repeat-x;
}

/* 🖨️ CSS tối ưu riêng cho chế độ IN (Print) chuyên nghiệp */
@media print {
  body {
    background-color: white !important;
  }
  .no-print {
    display: none !important;
  }
  .print-only {
    display: block !important;
  }
  .print-card {
    border: none !important;
    box-shadow: none !important;
    padding: 0 !important;
    margin: 0 !important;
  }
}
</style>