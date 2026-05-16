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
                  <span class="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-emerald-100/50 shadow-sm">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    ĐÃ THANH TOÁN
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

              <button 
                @click="openQrModal(ticket)"
                class="p-3 bg-[#075955]/5 border border-[#075955]/10 rounded-2xl text-[#075955] hover:bg-[#075955] hover:text-white transition-all duration-300 shadow-sm active:scale-95 group-hover:scale-105 flex flex-col items-center gap-0.5"
              >
                <span class="material-symbols-outlined text-[28px] font-black">qr_code_2</span>
                <span class="text-[9px] font-black tracking-widest">QUÉT MÃ</span>
              </button>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const activeTab = ref('upcoming');
const upcomingTickets = ref([]);
const isModalOpen = ref(false);
const selectedTicket = ref(null);

// Nạp lịch sử thật từ Local Browser Storage
const loadHistory = () => {
  // Đã cập nhật key localstorage sang trungnam_history cho đồng bộ thương hiệu. 
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