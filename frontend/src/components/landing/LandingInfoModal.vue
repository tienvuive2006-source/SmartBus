<template>
  <div class="fixed inset-0 z-[1000] overflow-y-auto" role="dialog" aria-modal="true">
    <!-- Backdrop with smooth blur -->
    <div @click="$emit('close')" class="absolute inset-0 bg-slate-900/60 backdrop-blur-md transition-opacity duration-300"></div>
    
    <div class="flex min-h-full items-center justify-center p-3 text-center sm:p-6">
      <!-- Modal Box -->
      <div class="relative z-10 flex max-h-[calc(100dvh-1.5rem)] w-full max-w-[920px] scale-100 transform flex-col overflow-hidden rounded-[28px] border border-white/70 bg-[#f8faf9]/95 text-left shadow-[0_32px_100px_rgba(6,43,41,0.34)] backdrop-blur-xl transition-all duration-300 sm:max-h-[calc(100dvh-3rem)]">
      <!-- Close Button -->
      <button @click="$emit('close')" aria-label="Đóng cửa sổ" class="absolute right-5 top-5 z-20 flex h-11 w-11 cursor-pointer items-center justify-center rounded-xl border border-slate-200 bg-white/80 text-slate-500 transition-all duration-200 hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:text-slate-800 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-emerald-600/20 sm:right-7 sm:top-7">
        <span class="material-symbols-outlined text-[24px]">close</span>
      </button>

      <!-- Modal Header -->
      <div class="flex shrink-0 items-center gap-4 border-b border-slate-200/80 bg-white/80 px-5 py-5 pr-20 sm:gap-5 sm:px-8 sm:py-7 sm:pr-24">
        <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-[#075955] text-white shadow-[0_10px_24px_rgba(7,89,85,0.2)] sm:h-16 sm:w-16">
          <span class="material-symbols-outlined text-[28px] sm:text-[32px]">
            {{ activeModalType === 'benxe' ? 'map' : (activeModalType === 'nhaxe' ? 'directions_bus' : (activeModalType === 'diemden' ? 'explore' : 'newspaper')) }}
          </span>
        </div>
        <div class="text-left">
          <h3 class="text-xl font-black leading-tight tracking-[-0.03em] text-slate-900 sm:text-[28px]">
            {{ activeModalType === 'benxe' ? 'Hệ thống bến xe' : (activeModalType === 'nhaxe' ? 'Các nhà xe đang hoạt động' : (activeModalType === 'diemden' ? 'Điểm đến phổ biến' : 'Thông tin ngành vận tải')) }}
          </h3>
          <p class="mt-1.5 text-xs font-semibold tracking-wide text-slate-500 sm:text-sm">Dữ liệu được cập nhật trực tiếp từ hệ thống</p>
        </div>
      </div>

      <!-- Modal Body Content -->
      <div class="modal-scrollbar min-h-0 flex-1 space-y-5 overflow-y-auto overscroll-contain px-5 py-5 text-left text-[15px] leading-7 text-slate-600 sm:px-8 sm:py-7">
        
        <!-- BẾN XE DYNAMIC LIST -->
        <div v-if="activeModalType === 'benxe'" class="space-y-4">
          <!-- Hướng dẫn thông minh bằng Banner -->
          <div class="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-start gap-2.5 text-xs font-semibold">
            <span class="material-symbols-outlined text-emerald-600 mt-0.5">info</span>
            <div>
              <p class="m-0 text-emerald-900 font-bold">💡 Bộ lọc nhanh Trang Chủ</p>
              <p class="text-slate-500 font-normal mt-1 mb-0">
                Bấm nút bên dưới để lập tức lọc danh sách <strong>Các tuyến đường chính</strong> tại trang chủ theo bến xe bạn chọn!
              </p>
            </div>
          </div>

          <p class="font-semibold text-slate-800">
            Dưới đây là các bến xe/điểm dừng thực tế đang hoạt động được Admin cập nhật trên hệ thống:
          </p>
          <div v-if="uniqueStations.length === 0" class="text-center py-8 text-gray-400">
            <span class="material-symbols-outlined text-4xl block mb-2">info</span>
            Chưa có dữ liệu bến xe nào do Admin đăng.
          </div>
          <ul v-else class="space-y-3">
            <li v-for="station in uniqueStations" :key="station.name" class="flex flex-col sm:flex-row sm:items-center justify-between p-4 bg-slate-50 hover:bg-[#075955]/5 rounded-2xl border border-slate-100 transition-colors gap-3">
              <div class="flex items-start gap-3">
                <span class="material-symbols-outlined text-[#075955] mt-0.5">location_on</span>
                <div>
                  <strong class="text-slate-800 block text-base">{{ station.name }}</strong>
                  <div class="flex gap-4 mt-1">
                    <span class="text-xs text-gray-500 font-medium">🛫 Nơi đi: {{ station.depCount }} chuyến</span>
                    <span class="text-xs text-gray-500 font-medium">🛬 Nơi đến: {{ station.arrCount }} chuyến</span>
                  </div>
                </div>
              </div>
              <div class="flex gap-2 shrink-0">
                <button @click="$emit('select-station', station.name, 'from')" :class="['px-3 py-1.5 rounded-lg font-bold text-xs uppercase transition-colors cursor-pointer border-none', filterFrom === station.name ? 'bg-[#075955] text-white' : 'bg-[#075955]/10 text-[#075955] hover:bg-[#075955] hover:text-white']">
                  Lọc Nơi Đi
                </button>
                <button @click="$emit('select-station', station.name, 'to')" :class="['px-3 py-1.5 rounded-lg font-bold text-xs uppercase transition-colors cursor-pointer border-none', filterTo === station.name ? 'bg-[#075955] text-white' : 'bg-[#075955]/10 text-[#075955] hover:bg-[#075955] hover:text-white']">
                  Lọc Nơi Đến
                </button>
              </div>
            </li>
          </ul>
        </div>

        <!-- NHÀ XE DYNAMIC LIST -->
        <div v-if="activeModalType === 'nhaxe'" class="space-y-4">
          <p class="font-semibold text-slate-800">
            Danh sách các hãng xe đang vận hành các chuyến đi thực tế do Admin đăng trên hệ thống:
          </p>
          <div v-if="uniqueCompanies.length === 0" class="text-center py-8 text-gray-400">
            <span class="material-symbols-outlined text-4xl block mb-2">info</span>
            Chưa có dữ liệu nhà xe nào do Admin đăng.
          </div>
          <ul v-else class="space-y-3">
            <li v-for="company in uniqueCompanies" :key="company.name" class="flex flex-col sm:flex-row sm:items-center justify-between p-4 bg-slate-50 hover:bg-[#075955]/5 rounded-2xl border border-slate-100 transition-colors gap-3">
              <div class="flex items-start gap-3">
                <span class="material-symbols-outlined text-[#075955] mt-0.5 text-base">directions_bus</span>
                <div>
                  <strong class="text-slate-800 block text-base">{{ company.name }}</strong>
                  <div class="flex gap-4 mt-1">
                    <span class="text-xs text-gray-500 font-medium">🛣️ Số chuyến đang chạy: {{ company.count }}</span>
                    <span class="text-xs text-gray-500 font-medium">💰 Giá vé từ: {{ company.minPrice?.toLocaleString() }}đ</span>
                  </div>
                </div>
              </div>
              <button @click="$emit('select-company', company.name)" class="px-4 py-2 bg-[#075955] hover:bg-[#05403d] text-white rounded-lg font-bold text-xs uppercase transition-colors cursor-pointer border-none shrink-0">
                Lọc nhà xe
              </button>
            </li>
          </ul>
        </div>

        <!-- DIEM DEN DYNAMIC LIST -->
        <div v-if="activeModalType === 'diemden'" class="space-y-4">
          <p class="font-semibold text-slate-800">Khám phá các điểm đến phổ biến trên lộ trình chuyên tuyến của Nhà xe Trung - Nam:</p>
          <div class="grid grid-cols-2 gap-4">
            <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
              <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Đà Nẵng</strong>
              <p class="text-xs text-slate-500">Thành phố đáng sống nhất Việt Nam với Cầu Vàng Bà Nà Hills, bán đảo Sơn Trà và bãi biển Mỹ Khê tuyệt đẹp.</p>
            </div>
            <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
              <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Quy Nhơn</strong>
              <p class="text-xs text-slate-500">Xứ nẫu thanh bình với Kỳ Co, Eo Gió thơ mộng và những tháp Chăm cổ kính nhuốm màu thời gian.</p>
            </div>
            <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
              <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Nha Trang</strong>
              <p class="text-xs text-slate-500">Vịnh biển thiên đường với VinWonders, đảo Hòn Tre cát trắng và thế giới san hô đa sắc màu.</p>
            </div>
            <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
              <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Sài Gòn</strong>
              <p class="text-xs text-slate-500">Đô thị sầm uất, sôi động ngày đêm với các điểm check-in lịch sử và trung tâm ẩm thực độc đáo.</p>
            </div>
          </div>
        </div>

        <!-- THONG TIN DYNAMIC LIST -->
        <div v-if="activeModalType === 'thongtin'" class="space-y-4">
          <div class="max-w-3xl">
            <p class="text-base font-semibold leading-7 text-slate-700 sm:text-lg">Những thông tin quan trọng giúp bạn chuẩn bị tốt hơn trước mỗi hành trình.</p>
            <p class="mt-1 text-sm text-slate-500 sm:text-[15px]">Vui lòng đọc kỹ các quy định bên dưới trước khi đặt vé và lên xe.</p>
          </div>
          <div v-if="loadingInfo" class="flex justify-center py-4">
            <span class="material-symbols-outlined animate-spin text-[#075955]">sync</span>
          </div>
          <ul v-else class="space-y-4">
            <li v-if="transportInfos.length === 0" class="rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center">
              <p class="text-sm font-medium text-slate-500">Chưa có thông tin được cập nhật.</p>
            </li>
            <li v-for="(info, index) in transportInfos" :key="index" class="group grid grid-cols-[44px_minmax(0,1fr)] gap-4 rounded-2xl border border-slate-200/90 bg-white p-4 shadow-[0_8px_28px_rgba(15,55,52,0.05)] transition-all duration-200 hover:-translate-y-0.5 hover:border-emerald-700/25 hover:shadow-[0_12px_34px_rgba(15,55,52,0.09)] sm:grid-cols-[52px_minmax(0,1fr)] sm:gap-5 sm:p-5">
              <span class="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-50 font-mono text-sm font-bold tabular-nums text-[#075955] transition-colors group-hover:bg-[#075955] group-hover:text-white sm:h-13 sm:w-13">{{ String(index + 1).padStart(2, '0') }}</span>
              <div class="min-w-0 pt-0.5">
                <strong class="block text-[14px] font-extrabold leading-5 tracking-wide text-[#075955] sm:text-[15px]">{{ info.title }}</strong>
                <p class="mt-1.5 max-w-[72ch] whitespace-pre-line text-[14px] leading-6 text-slate-600 sm:text-base sm:leading-7">{{ info.content }}</p>
              </div>
            </li>
          </ul>
        </div>

      </div>

      <!-- Modal Footer -->
      <div class="flex shrink-0 flex-col-reverse gap-3 border-t border-slate-200/80 bg-white/85 px-5 py-4 sm:flex-row sm:justify-end sm:px-8 sm:py-5">
        <button @click="$emit('close')" class="min-h-12 cursor-pointer rounded-xl border border-slate-200 bg-white px-6 py-3 text-sm font-bold text-slate-600 transition-all duration-200 hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-slate-300/50 active:translate-y-px">
          Đóng lại
        </button>
        <button @click="$emit('book-now')" class="flex min-h-12 cursor-pointer items-center justify-center gap-2 rounded-xl border border-transparent bg-[#075955] px-7 py-3 text-sm font-bold text-white shadow-[0_10px_24px_rgba(7,89,85,0.2)] transition-all duration-200 hover:-translate-y-0.5 hover:bg-[#064b48] hover:shadow-[0_14px_30px_rgba(7,89,85,0.26)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-emerald-600/25 active:translate-y-px">
          <span class="material-symbols-outlined text-[19px]">directions_bus</span> Đặt vé ngay
        </button>
      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useApi } from '@/composables/useApi';

const props = defineProps({
  activeModalType: {
    type: String,
    required: true
  },
  uniqueStations: {
    type: Array,
    default: () => []
  },
  uniqueCompanies: {
    type: Array,
    default: () => []
  },
  filterFrom: {
    type: String,
    default: ''
  },
  filterTo: {
    type: String,
    default: ''
  }
});

defineEmits(['close', 'book-now', 'select-station', 'select-company']);

const api = useApi();
const transportInfos = ref([]);
const loadingInfo = ref(false);

const fetchTransportInfo = async () => {
  if (props.activeModalType !== 'thongtin') return;
  loadingInfo.value = true;
  try {
    const res = await api.get('/settings/TRANSPORT_INFO');
    if (res.data && res.data.value) {
      transportInfos.value = JSON.parse(res.data.value);
    } else {
      transportInfos.value = [];
    }
  } catch (err) {
    console.error("Lỗi lấy thông tin vận tải:", err);
  } finally {
    loadingInfo.value = false;
  }
};

onMounted(() => {
  fetchTransportInfo();
});

watch(() => props.activeModalType, () => {
  fetchTransportInfo();
});
</script>

<style scoped>
.modal-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: rgba(7, 89, 85, 0.35) transparent;
}

.modal-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.modal-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.modal-scrollbar::-webkit-scrollbar-thumb {
  border: 2px solid transparent;
  border-radius: 999px;
  background: rgba(7, 89, 85, 0.3);
  background-clip: padding-box;
}

.modal-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(7, 89, 85, 0.5);
  background-clip: padding-box;
}
</style>
