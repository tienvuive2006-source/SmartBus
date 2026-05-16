<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-900 pb-20">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-2 cursor-pointer" @click="$router.push('/')">
          <span class="material-symbols-outlined text-white text-4xl">directions_bus</span>
          <div class="flex flex-col">
            <span class="text-xl font-bold leading-none tracking-tight">Trung - Nam</span>
            <span class="text-[9px] uppercase tracking-wider font-semibold opacity-70">Nhà xe chuyên tuyến Miền Trung - Nam</span>
          </div>
        </div>
        <div class="flex items-center gap-6">
          <button @click="$router.push('/')" class="text-sm font-semibold hover:text-yellow-300 transition-colors">
            Xong
          </button>
        </div>
      </div>
    </nav>

    <div class="flex flex-col items-center pt-12">
    <main class="w-full max-w-md px-4">
      <div class="text-center mb-10">
        <div class="w-16 h-16 bg-emerald-50 text-emerald-500 rounded-full flex items-center justify-center mx-auto mb-6 border border-emerald-100 shadow-sm">
           <span class="material-symbols-outlined text-4xl font-bold">check</span>
        </div>
        <h1 class="text-2xl font-bold tracking-tight">Thanh toán thành công</h1>
        <p class="text-xs font-bold text-gray-500 uppercase mt-2">Cảm ơn bạn đã sử dụng dịch vụ Trung - Nam</p>
      </div>

      <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div class="p-6 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
           <span class="text-[10px] font-bold text-gray-500 uppercase">Mã vé: <span class="text-gray-900">{{ bookingId }}</span></span>
           <span class="text-[10px] font-bold text-emerald-600 uppercase bg-emerald-50 px-2 py-1 rounded">Đã xác nhận</span>
        </div>
        
        <div class="p-6 space-y-6">
           <div class="flex items-center justify-between">
              <div>
                 <p class="text-[9px] font-bold text-gray-400 uppercase mb-1">Khởi hành</p>
                 <p class="font-bold text-gray-900">{{ fromCity }}</p>
              </div>
              <span class="material-symbols-outlined text-gray-300">arrow_forward</span>
              <div class="text-right">
                 <p class="text-[9px] font-bold text-gray-400 uppercase mb-1">Điểm đến</p>
                 <p class="font-bold text-gray-900">{{ toCity }}</p>
              </div>
           </div>

           <div class="grid grid-cols-2 gap-6 pt-6 border-t border-gray-100">
              <div>
                 <p class="text-[9px] font-bold text-gray-400 uppercase mb-1">Thời gian</p>
                 <p class="text-sm font-bold text-gray-900">{{ depTime }}</p>
              </div>
              <div class="text-right">
                 <p class="text-[9px] font-bold text-gray-400 uppercase mb-1">Số ghế</p>
                 <p class="text-sm font-bold text-[#f03a17]">{{ seatsSelected }}</p>
              </div>
           </div>
        </div>

        <div class="p-8 bg-gray-50 border-t border-gray-100 flex flex-col items-center">
           <div class="bg-white p-3 border border-gray-200 rounded-lg mb-4 shadow-sm">
              <img :src="qrCodeUrl" class="w-32 h-32" />
           </div>
           <p class="text-[9px] font-bold text-gray-500 uppercase tracking-widest">Đưa mã này khi lên xe</p>
        </div>
      </div>

      <div class="mt-8 space-y-3">
        <button @click="$router.push('/')" class="w-full bg-[#f03a17] hover:bg-[#d63314] text-white py-4 rounded-md font-bold text-sm uppercase tracking-widest transition-all shadow-md">Trang chủ</button>
        <button @click="windowPrint" class="w-full bg-white border border-gray-200 hover:bg-gray-50 text-gray-700 py-4 rounded-md font-bold text-sm uppercase tracking-widest transition-all">In vé</button>
      </div>
    </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
// Đổi tiền tố mặc định thành SAO-
const bookingId = computed(() => route.query.bookingId || 'TN-' + Math.floor(100000 + Math.random() * 900000));
const fromCity = computed(() => route.query.from || 'Hành trình');
const toCity = computed(() => route.query.to || 'Điểm đến');
const depTime = computed(() => route.query.time || 'Hôm nay');
const seatsSelected = computed(() => route.query.seats || '---');

const qrCodeUrl = computed(() => {
  const data = `BOOKING:${bookingId.value}|SEATS:${seatsSelected.value}`;
  // Đổi màu mã QR sang màu xanh ngọc của Trung - Nam (075955)
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(data)}&color=075955&bgcolor=ffffff`;
});

const windowPrint = () => { window.print(); };
</script>