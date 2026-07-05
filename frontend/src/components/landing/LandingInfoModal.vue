<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <!-- Backdrop with smooth blur -->
    <div @click="$emit('close')" class="absolute inset-0 bg-slate-900/60 backdrop-blur-md transition-opacity duration-300"></div>
    
    <!-- Modal Box -->
    <div class="relative bg-white/95 backdrop-blur-lg rounded-[32px] shadow-2xl border border-slate-100 max-w-2xl w-full overflow-hidden z-10 transform transition-all duration-300 scale-100 p-8 flex flex-col gap-6">
      <!-- Close Button -->
      <button @click="$emit('close')" class="absolute top-6 right-6 w-10 h-10 rounded-full hover:bg-slate-100 flex items-center justify-center transition-all bg-transparent border-none cursor-pointer">
        <span class="material-symbols-outlined text-slate-500">close</span>
      </button>

      <!-- Modal Header -->
      <div class="flex items-center gap-4 border-b border-slate-100 pb-4">
        <div class="w-12 h-12 bg-[#075955]/10 text-[#075955] rounded-2xl flex items-center justify-center">
          <span class="material-symbols-outlined text-2xl">
            {{ activeModalType === 'benxe' ? 'map' : (activeModalType === 'nhaxe' ? 'directions_bus' : (activeModalType === 'diemden' ? 'explore' : 'newspaper')) }}
          </span>
        </div>
        <div class="text-left">
          <h3 class="text-xl font-black text-slate-800 tracking-tight uppercase">
            {{ activeModalType === 'benxe' ? 'Hệ Thống Bến Xe (Admin đăng)' : (activeModalType === 'nhaxe' ? 'Danh Sách Nhà Xe (Admin đăng)' : (activeModalType === 'diemden' ? 'Các Điểm Đến Phổ Biến' : 'Thông Tin Ngành Vận Tải')) }}
          </h3>
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5">Dữ liệu đồng bộ trực tiếp từ hệ thống</p>
        </div>
      </div>

      <!-- Modal Body Content -->
      <div class="text-slate-600 text-sm leading-relaxed overflow-y-auto max-h-[380px] pr-2 space-y-4 text-left">
        
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
          <p class="font-semibold text-slate-800">Cập nhật các quy định, thông tin an toàn đường bộ và cẩm nang đi xe mới nhất:</p>
          <div v-if="loadingInfo" class="flex justify-center py-4">
            <span class="material-symbols-outlined animate-spin text-[#075955]">sync</span>
          </div>
          <ul v-else class="space-y-3">
            <li v-if="transportInfos.length === 0" class="p-4 bg-[#075955]/5 rounded-2xl border border-[#075955]/10 text-left text-center">
              <p class="text-xs text-slate-500 italic">Chưa có thông tin được cập nhật.</p>
            </li>
            <li v-for="(info, index) in transportInfos" :key="index" class="p-4 bg-[#075955]/5 rounded-2xl border border-[#075955]/10 text-left">
              <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">{{ info.title }}</strong>
              <p class="text-xs text-slate-600 whitespace-pre-line">{{ info.content }}</p>
            </li>
          </ul>
        </div>

      </div>

      <!-- Modal Footer -->
      <div class="flex justify-end gap-3 border-t border-slate-100 pt-4">
        <button @click="$emit('close')" class="px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl font-bold text-xs uppercase tracking-widest transition-colors border-none cursor-pointer">
          Đóng lại
        </button>
        <button @click="$emit('book-now')" class="px-6 py-2.5 bg-[#075955] hover:bg-[#05403d] text-white rounded-xl font-bold text-xs uppercase tracking-widest transition-colors flex items-center gap-2 border-none cursor-pointer">
          <span class="material-symbols-outlined text-sm">directions_bus</span> Đặt vé ngay
        </button>
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
