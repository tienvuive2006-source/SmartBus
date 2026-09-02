<template>
  <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
  >
    <div v-if="isOpen" class="fixed inset-0 z-[1000] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-slate-900/70 backdrop-blur-sm" @click="close"></div>

      <!-- Modal Panel -->
      <div class="relative z-10 w-full max-w-lg bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90dvh]">

        <!-- Header -->
        <div class="relative bg-[#075955] px-6 py-5">
          <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 80% 50%, #fff 1px, transparent 1px); background-size: 20px 20px;"></div>
          <div class="relative flex items-start justify-between">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="material-symbols-outlined text-emerald-300 text-[20px]">confirmation_number</span>
                <span class="text-[11px] font-black text-emerald-300 uppercase tracking-[0.18em]">Smart Bus</span>
              </div>
              <h2 class="text-xl font-black text-white tracking-tight">Tra cứu vé xe</h2>
              <p class="text-emerald-100 text-xs mt-0.5 font-medium">Kiểm tra thông tin và trạng thái vé của bạn</p>
            </div>
            <button
              @click="close"
              class="w-8 h-8 flex items-center justify-center rounded-xl bg-white/10 hover:bg-white/20 text-white transition-colors"
            >
              <span class="material-symbols-outlined text-[18px]">close</span>
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="p-6 overflow-y-auto flex-1 overscroll-contain">

          <!-- === FORM STATE === -->
          <form v-if="!ticketData" @submit.prevent="handleTrack" class="space-y-4">

            <!-- Mã vé -->
            <div>
              <label class="block text-xs font-black text-slate-500 uppercase tracking-wider mb-1.5">
                Mã vé
              </label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 text-[18px]">tag</span>
                <input
                  v-model.trim="form.code"
                  type="text"
                  required
                  maxlength="20"
                  autocomplete="off"
                  spellcheck="false"
                  placeholder="Ví dụ: TN-8882A8"
                  class="w-full pl-9 pr-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl text-slate-900 font-bold uppercase text-sm placeholder:text-slate-300 placeholder:normal-case placeholder:font-normal focus:outline-none focus:border-[#075955] focus:bg-white transition-all"
                />
              </div>
            </div>

            <!-- SĐT -->
            <div>
              <label class="block text-xs font-black text-slate-500 uppercase tracking-wider mb-1.5">
                Số điện thoại đặt vé
              </label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 text-[18px]">phone</span>
                <input
                  v-model="form.phone"
                  type="tel"
                  required
                  inputmode="numeric"
                  placeholder="0912 345 678"
                  class="w-full pl-9 pr-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl text-slate-900 font-semibold text-sm placeholder:text-slate-300 placeholder:font-normal focus:outline-none focus:border-[#075955] focus:bg-white transition-all"
                />
              </div>
              <p class="text-[11px] text-slate-400 mt-1.5 font-medium">Nhập đúng số điện thoại đã dùng khi đặt vé</p>
            </div>

            <!-- Error -->
            <div
              v-if="errorMsg"
              class="flex items-start gap-2.5 p-3.5 bg-rose-50 border border-rose-200 rounded-xl text-rose-700"
            >
              <span class="material-symbols-outlined text-[18px] shrink-0 mt-0.5">error</span>
              <p class="text-sm font-semibold leading-snug">{{ errorMsg }}</p>
            </div>

            <!-- Submit -->
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full py-3.5 bg-[#075955] hover:bg-[#064b48] disabled:opacity-60 text-white rounded-xl font-black text-sm tracking-wide transition-all active:scale-[0.98] flex items-center justify-center gap-2 shadow-lg shadow-emerald-950/15"
            >
              <span v-if="isLoading" class="material-symbols-outlined animate-spin text-[18px]">refresh</span>
              <span v-else class="material-symbols-outlined text-[18px]">search</span>
              {{ isLoading ? 'Đang tra cứu...' : 'Tra cứu ngay' }}
            </button>
          </form>

          <!-- === RESULT STATE === -->
          <div v-else class="space-y-4">

            <!-- Ticket Header -->
            <div class="flex items-center justify-between">
              <div>
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Mã vé</p>
                <p class="text-xl font-black text-slate-900 tracking-tight">{{ ticketData.ticketCode }}</p>
              </div>
              <div
                class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-black"
                :class="statusConfig[ticketData.status]?.bg || 'bg-slate-100 text-slate-600'"
              >
                <span class="material-symbols-outlined text-[14px]" style="font-variation-settings: 'FILL' 1">
                  {{ statusConfig[ticketData.status]?.icon || 'info' }}
                </span>
                {{ statusConfig[ticketData.status]?.text || ticketData.status }}
              </div>
            </div>

            <!-- Route Bar -->
            <div class="bg-[#075955]/5 border border-[#075955]/10 rounded-xl p-4 flex items-center gap-3">
              <div class="flex-1">
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-0.5">Khởi hành</p>
                <p class="text-sm font-black text-slate-800 leading-tight">{{ ticketData.trip?.departurePoint }}</p>
              </div>
              <div class="flex flex-col items-center gap-0.5">
                <span class="material-symbols-outlined text-[#075955] text-[20px]">east</span>
                <span class="text-[9px] font-black text-[#075955] uppercase tracking-wider">{{ ticketData.trip?.departureTime }}</span>
              </div>
              <div class="flex-1 text-right">
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-0.5">Điểm đến</p>
                <p class="text-sm font-black text-slate-800 leading-tight">{{ ticketData.trip?.arrivalPoint }}</p>
              </div>
            </div>

            <!-- Info Grid -->
            <div class="grid grid-cols-2 gap-3">
              <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-1">Hành khách</p>
                <p class="text-sm font-black text-slate-800 leading-tight">{{ ticketData.customerName }}</p>
                <p class="text-xs text-slate-500 font-medium mt-0.5">{{ ticketData.customerPhone }}</p>
              </div>
              <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-1">Ngày khởi hành</p>
                <p class="text-sm font-black text-slate-800 leading-tight">{{ formatDate(ticketData.trip?.departureDate) }}</p>
                <p class="text-xs text-[#075955] font-black mt-0.5">{{ ticketData.trip?.departureTime }}</p>
              </div>
              <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-1">Số ghế</p>
                <p class="text-sm font-black text-rose-600">{{ ticketData.seatNumbers?.join(', ') || 'Chưa xếp' }}</p>
              </div>
              <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-1">Tổng tiền</p>
                <p class="text-sm font-black text-emerald-700">{{ ticketData.totalPrice?.toLocaleString('vi-VN') }}đ</p>
              </div>
            </div>

            <!-- Xe & Tài xế -->
            <div v-if="ticketData.trip?.assignedLicensePlate" class="flex items-center gap-3 p-3 bg-slate-50 rounded-xl border border-slate-100">
              <div class="w-9 h-9 rounded-lg bg-slate-200 flex items-center justify-center text-slate-500 shrink-0">
                <span class="material-symbols-outlined text-[18px]">directions_bus</span>
              </div>
              <div>
                <p class="text-xs font-black text-slate-700">BKS: {{ ticketData.trip.assignedLicensePlate }}</p>
                <p class="text-[11px] text-slate-500 font-medium">{{ ticketData.trip.assignedDriverFullName || 'Đang cập nhật tài xế' }}</p>
              </div>
            </div>

            <!-- Cancel Success -->
            <div v-if="cancelSuccess" class="p-4 bg-emerald-50 border border-emerald-200 rounded-xl flex items-start gap-3">
              <span class="material-symbols-outlined text-emerald-600 text-[20px] shrink-0" style="font-variation-settings: 'FILL' 1">check_circle</span>
              <div>
                <p class="text-sm font-black text-emerald-800">Hủy vé thành công</p>
                <p class="text-xs text-emerald-700 mt-0.5">
                  {{ cancelSuccess.refundMethod === 'BANK_TRANSFER' ? 'Yêu cầu hoàn tiền đã gửi, nhà xe sẽ xử lý trong 24 giờ.' : 'Ghế đã được mở lại.' }}
                </p>
              </div>
            </div>

            <!-- Cancel CTA -->
            <button
              v-else-if="canOfferCancellation && !showCancelForm"
              @click="openCancelForm"
              class="w-full py-3 border-2 border-rose-200 text-rose-600 rounded-xl font-black text-sm hover:bg-rose-500 hover:text-white hover:border-rose-500 transition-all active:scale-[0.98]"
            >
              Hủy vé này
            </button>

            <!-- Cancel Form -->
            <form v-if="showCancelForm && !cancelSuccess" class="space-y-3 p-4 rounded-xl border border-rose-100 bg-rose-50/50" @submit.prevent="submitCancellation">
              <p class="text-xs font-black text-rose-700 uppercase tracking-wider">Xác nhận hủy vé</p>

              <select v-model="cancelForm.reason" required class="w-full border border-slate-200 bg-white rounded-xl px-3 py-2.5 text-sm font-semibold outline-none focus:border-rose-400">
                <option value="" disabled>Chọn lý do hủy</option>
                <option value="Thay đổi lịch trình">Thay đổi lịch trình</option>
                <option value="Tìm được xe khác phù hợp hơn">Tìm được xe khác</option>
                <option value="Đặt nhầm ngày/giờ">Đặt nhầm ngày/giờ</option>
                <option value="Lý do cá nhân">Lý do cá nhân</option>
              </select>

              <template v-if="refundEligible">
                <div class="p-3 bg-amber-50 border border-amber-200 rounded-lg">
                  <p class="text-xs font-black text-amber-700">Hoàn tiền {{ refundRate }}% qua ngân hàng</p>
                  <p class="text-xs text-amber-600 mt-0.5">Khoảng <b>{{ Math.round(ticketData.totalPrice * refundRate / 100).toLocaleString('vi-VN') }}đ</b> sau khi nhà xe duyệt</p>
                </div>
                <select v-model="cancelForm.bankName" required class="w-full border border-slate-200 bg-white rounded-xl px-3 py-2.5 text-sm font-semibold outline-none focus:border-rose-400">
                  <option value="" disabled>Chọn ngân hàng nhận tiền</option>
                  <option v-for="bank in vietnameseBanks" :key="bank.code" :value="bank.name">{{ bank.code }} · {{ bank.name }}</option>
                </select>
                <input v-model.trim="cancelForm.bankAccountNumber" required inputmode="numeric" maxlength="30" placeholder="Số tài khoản nhận tiền" class="w-full border border-slate-200 bg-white rounded-xl px-3 py-2.5 text-sm font-semibold outline-none focus:border-rose-400" />
                <input v-model.trim="cancelForm.bankAccountName" required maxlength="150" placeholder="Tên chủ tài khoản (IN HOA)" class="w-full border border-slate-200 bg-white rounded-xl px-3 py-2.5 text-sm font-semibold uppercase outline-none focus:border-rose-400" />
              </template>

              <p v-if="cancelError" class="text-xs font-bold text-rose-700 bg-rose-100 px-3 py-2 rounded-lg">{{ cancelError }}</p>

              <div class="grid grid-cols-2 gap-2 pt-1">
                <button type="button" :disabled="isCancelling" @click="showCancelForm = false" class="py-2.5 rounded-xl bg-slate-100 text-slate-600 text-xs font-black hover:bg-slate-200 transition-colors">Quay lại</button>
                <button type="submit" :disabled="isCancelling || !canSubmitCancellation" class="py-2.5 rounded-xl bg-rose-500 text-white text-xs font-black disabled:opacity-50 hover:bg-rose-600 transition-colors">
                  {{ isCancelling ? 'Đang xử lý...' : 'Xác nhận hủy' }}
                </button>
              </div>
            </form>

            <!-- Too close to departure -->
            <p v-else-if="ticketData.status !== 'CANCELLED' && hoursToDeparture < 12 && !cancelSuccess" class="text-xs font-semibold text-amber-700 bg-amber-50 border border-amber-200 p-3 rounded-xl leading-relaxed">
              Chuyến khởi hành trong vòng 12 giờ nữa, không thể hủy trực tuyến. Vui lòng liên hệ nhà xe.
            </p>

            <!-- Reset -->
            <button @click="resetForm" class="w-full py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl font-bold text-sm transition-colors flex items-center justify-center gap-1.5">
              <span class="material-symbols-outlined text-[16px]">arrow_back</span>
              Tra cứu vé khác
            </button>
          </div>

        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue';
import { useApi } from '@/composables/useApi';

const props = defineProps({ isOpen: Boolean });
const emit = defineEmits(['close']);
const api = useApi();

const form = reactive({ code: '', phone: '' });
const isLoading = ref(false);
const errorMsg = ref('');
const ticketData = ref(null);
const showCancelForm = ref(false);
const isCancelling = ref(false);
const cancelError = ref('');
const cancelSuccess = ref(null);
const cancelForm = reactive({ reason: '', bankName: '', bankAccountNumber: '', bankAccountName: '' });

const vietnameseBanks = [
  { code: 'VCB', name: 'Vietcombank' }, { code: 'BIDV', name: 'BIDV' },
  { code: 'CTG', name: 'VietinBank' }, { code: 'AGR', name: 'Agribank' },
  { code: 'TCB', name: 'Techcombank' }, { code: 'MB', name: 'MB Bank' },
  { code: 'ACB', name: 'ACB' }, { code: 'VPB', name: 'VPBank' },
  { code: 'TPB', name: 'TPBank' }, { code: 'STB', name: 'Sacombank' },
  { code: 'HDB', name: 'HDBank' }, { code: 'VIB', name: 'VIB' },
  { code: 'SHB', name: 'SHB' }, { code: 'OCB', name: 'OCB' }
];

const hoursToDeparture = computed(() => {
  if (!ticketData.value?.trip?.departureDate || !ticketData.value?.trip?.departureTime) return 0;
  return Math.max(0, (new Date(`${ticketData.value.trip.departureDate}T${ticketData.value.trip.departureTime}`).getTime() - Date.now()) / 3600000);
});

const refundEligible = computed(() => ticketData.value?.status === 'PAID' && ticketData.value?.paymentMethod !== 'CASH');
const refundRate = computed(() => hoursToDeparture.value >= 24 ? 95 : 70);
const canOfferCancellation = computed(() => ['PAID', 'PENDING'].includes(ticketData.value?.status) && hoursToDeparture.value >= 12);
const canSubmitCancellation = computed(() => {
  if (!cancelForm.reason) return false;
  if (!refundEligible.value) return true;
  return Boolean(cancelForm.bankName && cancelForm.bankAccountName && /^\d{6,30}$/.test(cancelForm.bankAccountNumber.replace(/\s+/g, '')));
});

const statusConfig = {
  'PENDING':    { text: 'Chờ thanh toán', bg: 'bg-amber-100 text-amber-700',   icon: 'schedule' },
  'PAID':       { text: 'Đã thanh toán',  bg: 'bg-emerald-100 text-emerald-700', icon: 'check_circle' },
  'CANCELLED':  { text: 'Đã hủy',         bg: 'bg-rose-100 text-rose-700',      icon: 'cancel' },
  'CHECKED_IN': { text: 'Đã lên xe',      bg: 'bg-blue-100 text-blue-700',      icon: 'how_to_reg' },
  'COMPLETED':  { text: 'Hoàn thành',     bg: 'bg-slate-100 text-slate-600',    icon: 'task_alt' },
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'Chưa xác định';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
};

const close = () => emit('close');

const resetForm = () => {
  form.code = '';
  form.phone = '';
  errorMsg.value = '';
  ticketData.value = null;
  showCancelForm.value = false;
  cancelSuccess.value = null;
  cancelError.value = '';
};

const openCancelForm = () => {
  Object.assign(cancelForm, { reason: '', bankName: '', bankAccountNumber: '', bankAccountName: '' });
  cancelError.value = '';
  showCancelForm.value = true;
};

const submitCancellation = async () => {
  if (!canSubmitCancellation.value) return;
  isCancelling.value = true;
  cancelError.value = '';
  try {
    const response = await api.post(`/auth/me/bookings/${encodeURIComponent(form.code.trim())}/cancel`, {
      customerPhone: form.phone,
      reason: cancelForm.reason,
      refundMethod: refundEligible.value ? 'BANK_TRANSFER' : 'NONE',
      bankName: refundEligible.value ? cancelForm.bankName : null,
      bankAccountNumber: refundEligible.value ? cancelForm.bankAccountNumber : null,
      bankAccountName: refundEligible.value ? cancelForm.bankAccountName : null
    });
    ticketData.value.status = 'CANCELLED';
    showCancelForm.value = false;
    cancelSuccess.value = response.data;
  } catch (error) {
    cancelError.value = error.response?.data?.message || error.response?.data || 'Không thể hủy vé. Vui lòng kiểm tra lại thông tin.';
  } finally {
    isCancelling.value = false;
  }
};

const handleTrack = async () => {
  if (!form.code || !form.phone) return;
  isLoading.value = true;
  errorMsg.value = '';
  try {
    const res = await api.get('/admin/bookings/track', {
      params: { code: form.code.trim().toUpperCase(), phone: form.phone.replace(/\s+/g, '') }
    });
    if (res.data) ticketData.value = res.data;
  } catch (err) {
    if (err.response?.status === 404) {
      errorMsg.value = 'Không tìm thấy vé. Vui lòng kiểm tra lại mã vé và số điện thoại.';
    } else {
      errorMsg.value = err.response?.data?.message
        || err.response?.data?.error
        || (typeof err.response?.data === 'string' ? err.response.data : null)
        || `Lỗi ${err.response?.status || ''}: Vui lòng thử lại sau.`.trim();
    }
  } finally {
    isLoading.value = false;
  }
};

watch(() => props.isOpen, (newVal) => {
  if (!newVal) setTimeout(resetForm, 300);
});
</script>
