<template>
  <Transition enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition duration-200 ease-in" leave-from-class="opacity-100" leave-to-class="opacity-0">
    <div v-if="isOpen" class="fixed inset-0 z-[999] flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="close"></div>
      
      <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md relative z-10 overflow-hidden transform transition-all">
        <!-- Header -->
        <div class="bg-emerald-600 px-6 py-5 flex items-center justify-between text-white relative overflow-hidden">
          <div class="absolute -right-4 -top-10 text-emerald-500 opacity-20">
            <span class="material-symbols-outlined text-[100px]">confirmation_number</span>
          </div>
          <div>
            <h3 class="text-xl font-black tracking-wide flex items-center gap-2">
              <span class="material-symbols-outlined">search_check</span> Tra Cứu Vé
            </h3>
            <p class="text-emerald-100 text-xs mt-1">Kiểm tra thông tin vé nhanh chóng</p>
          </div>
          <button @click="close" class="w-8 h-8 flex items-center justify-center rounded-full bg-black/20 hover:bg-black/30 transition-colors z-10">
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>

        <div class="p-6">
          <!-- Form nhập liệu -->
          <form v-if="!ticketData" @submit.prevent="handleTrack" class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-zinc-500 uppercase tracking-wider mb-1.5">Mã Vé (ID)</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                  <span class="material-symbols-outlined text-lg">tag</span>
                </div>
                <input v-model="form.code" type="number" required placeholder="Ví dụ: 15" class="w-full pl-10 pr-4 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl font-medium text-zinc-900 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all">
              </div>
            </div>
            
            <div>
              <label class="block text-xs font-bold text-zinc-500 uppercase tracking-wider mb-1.5">Số Điện Thoại</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                  <span class="material-symbols-outlined text-lg">call</span>
                </div>
                <input v-model="form.phone" type="tel" required placeholder="Nhập SĐT đặt vé" class="w-full pl-10 pr-4 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl font-medium text-zinc-900 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all">
              </div>
            </div>

            <div v-if="errorMsg" class="p-3 bg-rose-50 text-rose-600 rounded-xl text-sm flex items-center gap-2 font-medium border border-rose-100">
              <span class="material-symbols-outlined text-base shrink-0">error</span>
              {{ errorMsg }}
            </div>

            <button type="submit" :disabled="isLoading" class="w-full py-3 mt-2 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-xl font-bold tracking-wide transition-colors flex items-center justify-center gap-2">
              <span v-if="isLoading" class="material-symbols-outlined animate-spin text-lg">sync</span>
              <span v-else class="material-symbols-outlined text-lg">search</span>
              {{ isLoading ? 'Đang tra cứu...' : 'Tra cứu ngay' }}
            </button>
          </form>

          <!-- Kết quả tra cứu -->
          <div v-else class="space-y-5 animate-[fadeIn_0.3s_ease-out]">
            <div class="text-center pb-4 border-b border-zinc-100">
              <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-emerald-100 text-emerald-600 mb-2">
                <span class="material-symbols-outlined text-2xl">check_circle</span>
              </div>
              <h4 class="text-lg font-black text-zinc-900">Thông Tin Vé #{{ ticketData.id }}</h4>
              
              <div class="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold" :class="statusConfig[ticketData.status]?.bg || 'bg-zinc-100 text-zinc-600'">
                <span class="material-symbols-outlined text-[14px]">{{ statusConfig[ticketData.status]?.icon || 'info' }}</span>
                {{ statusConfig[ticketData.status]?.text || ticketData.status }}
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="bg-zinc-50 p-3 rounded-xl border border-zinc-100">
                <p class="text-[10px] uppercase font-bold text-zinc-400 mb-1">Hành khách</p>
                <p class="font-bold text-sm text-zinc-900 truncate">{{ ticketData.customerName }}</p>
                <p class="text-xs text-zinc-500 truncate">{{ ticketData.customerPhone }}</p>
              </div>
              <div class="bg-zinc-50 p-3 rounded-xl border border-zinc-100">
                <p class="text-[10px] uppercase font-bold text-zinc-400 mb-1">Chuyến Xe</p>
                <p class="font-bold text-sm text-zinc-900">{{ ticketData.trip?.departureDate }}</p>
                <p class="text-xs text-zinc-500 font-medium text-emerald-600">{{ ticketData.trip?.departureTime }}</p>
              </div>
            </div>

            <div class="bg-emerald-50/50 p-4 rounded-xl border border-emerald-100 flex items-center justify-between">
              <div>
                <p class="text-[10px] uppercase font-bold text-emerald-600/70 mb-0.5">Vị trí ghế</p>
                <p class="font-black text-lg text-emerald-700">{{ ticketData.seatNumbers?.join(', ') || 'Chưa xếp ghế' }}</p>
              </div>
              <div class="text-right">
                <p class="text-[10px] uppercase font-bold text-emerald-600/70 mb-0.5">Tổng tiền</p>
                <p class="font-black text-lg text-emerald-700">{{ ticketData.totalPrice?.toLocaleString() }}đ</p>
              </div>
            </div>
            
            <!-- Biển số xe & Tài xế nếu có -->
            <div v-if="ticketData.trip?.assignedLicensePlate" class="flex items-center gap-3 p-3 bg-zinc-50 rounded-xl border border-zinc-200">
                <div class="w-10 h-10 rounded-full bg-zinc-200 flex items-center justify-center text-zinc-500 shrink-0">
                    <span class="material-symbols-outlined">directions_bus</span>
                </div>
                <div>
                    <p class="text-xs font-bold text-zinc-900">Biển số: {{ ticketData.trip.assignedLicensePlate }}</p>
                    <p class="text-xs text-zinc-500">Tài xế: {{ ticketData.trip.assignedDriverFullName || 'Đang cập nhật' }}</p>
                </div>
            </div>

            <button @click="resetForm" class="w-full py-2.5 mt-2 bg-zinc-100 hover:bg-zinc-200 text-zinc-700 rounded-xl font-bold text-sm transition-colors">
              Tra cứu vé khác
            </button>
          </div>

        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { useApi } from '@/composables/useApi';

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close']);
const api = useApi();

const form = reactive({
  code: '',
  phone: ''
});

const isLoading = ref(false);
const errorMsg = ref('');
const ticketData = ref(null);

const statusConfig = {
  'PENDING': { text: 'Chờ thanh toán', bg: 'bg-amber-100 text-amber-700', icon: 'pending_actions' },
  'PAID': { text: 'Đã thanh toán', bg: 'bg-emerald-100 text-emerald-700', icon: 'check_circle' },
  'CANCELLED': { text: 'Đã hủy', bg: 'bg-rose-100 text-rose-700', icon: 'cancel' },
  'CHECKED_IN': { text: 'Đã lên xe', bg: 'bg-blue-100 text-blue-700', icon: 'how_to_reg' }
};

const close = () => {
  emit('close');
};

const resetForm = () => {
  form.code = '';
  form.phone = '';
  errorMsg.value = '';
  ticketData.value = null;
};

const handleTrack = async () => {
  if (!form.code || !form.phone) return;
  
  isLoading.value = true;
  errorMsg.value = '';
  
  try {
    const res = await api.get(`/admin/bookings/track?code=${form.code}&phone=${form.phone}`);
    if (res.data) {
      ticketData.value = res.data;
    }
  } catch (err) {
    if (err.response?.status === 404) {
      errorMsg.value = 'Không tìm thấy vé. Vui lòng kiểm tra lại Mã vé và SĐT.';
    } else {
      errorMsg.value = 'Đã có lỗi xảy ra. Vui lòng thử lại sau.';
    }
  } finally {
    isLoading.value = false;
  }
};

watch(() => props.isOpen, (newVal) => {
  if (!newVal) {
    setTimeout(resetForm, 300);
  }
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
