<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-950/55 px-4 py-6 backdrop-blur-sm"
      @click.self="close"
    >
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="seat-layout-title"
        class="flex max-h-[92dvh] w-full max-w-5xl flex-col overflow-hidden rounded-[2rem] bg-[#f7f9f8] shadow-[0_30px_90px_rgba(3,40,36,0.28)]"
      >
        <header class="flex shrink-0 items-start justify-between border-b border-slate-200 bg-white px-6 py-5 md:px-8">
          <div>
            <p class="mb-1 text-[11px] font-bold uppercase tracking-[0.18em] text-emerald-700">Mẫu áp dụng cho chuyến mới</p>
            <h2 id="seat-layout-title" class="text-xl font-black tracking-tight text-slate-900 md:text-2xl">
              Cấu hình ghế · {{ busType?.name }}
            </h2>
            <p class="mt-1 text-sm text-slate-500">Chọn loại ghế, sau đó nhấp vào vị trí cần gán.</p>
          </div>
          <button
            type="button"
            aria-label="Đóng"
            class="flex h-10 w-10 items-center justify-center rounded-xl text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 active:scale-95"
            @click="close"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </header>

        <div class="flex-1 overflow-y-auto px-5 py-6 md:px-8">
          <div v-if="loading" class="grid min-h-80 place-items-center">
            <div class="text-center">
              <div class="mx-auto mb-3 h-9 w-9 animate-spin rounded-full border-4 border-emerald-100 border-t-emerald-700"></div>
              <p class="text-sm font-semibold text-slate-500">Đang tải mẫu sơ đồ...</p>
            </div>
          </div>

          <div v-else-if="errorMessage && seats.length === 0" class="grid min-h-80 place-items-center">
            <div class="max-w-md rounded-2xl border border-red-200 bg-red-50 p-5 text-center">
              <span class="material-symbols-outlined mb-2 text-3xl text-red-500">cloud_off</span>
              <p class="font-bold text-red-800">{{ errorMessage }}</p>
              <button type="button" class="mt-4 text-sm font-black text-red-700 underline underline-offset-4" @click="loadLayout">
                Thử tải lại
              </button>
            </div>
          </div>

          <template v-else>
            <div v-if="errorMessage" class="mb-4 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-800">
              <span class="material-symbols-outlined text-[19px]">error</span>
              <span>{{ errorMessage }}</span>
            </div>
            <div class="mb-6 flex flex-col gap-4 rounded-2xl bg-white p-4 shadow-[0_8px_24px_rgba(13,65,58,0.05)] lg:flex-row lg:items-center lg:justify-between">
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="option in seatTypeOptions"
                  :key="option.value"
                  type="button"
                  :class="[
                    'flex items-center gap-2 rounded-xl border-2 px-4 py-2.5 text-sm font-bold transition active:scale-[0.98]',
                    activeType === option.value ? option.activeClass : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300'
                  ]"
                  @click="activeType = option.value"
                >
                  <span class="material-symbols-outlined text-[18px]">{{ option.icon }}</span>
                  {{ option.label }}
                </button>
              </div>

              <div class="flex items-center gap-4 text-xs font-bold tabular-nums text-slate-500">
                <span><strong class="text-amber-700">{{ priorityCount }}</strong> ưu tiên</span>
                <span><strong class="text-sky-700">{{ childCount }}</strong> dành cho trẻ em</span>
                <button type="button" class="text-emerald-800 underline underline-offset-4" @click="resetLayout">Đặt lại</button>
              </div>
            </div>

            <div class="grid gap-5 md:grid-cols-2">
              <SeatFloorEditor title="Tầng dưới" :seats="floor1Seats" @select="assignType" />
              <SeatFloorEditor title="Tầng trên" :seats="floor2Seats" @select="assignType" />
            </div>

            <p class="mt-5 rounded-xl border border-emerald-100 bg-emerald-50/70 px-4 py-3 text-xs font-medium leading-relaxed text-emerald-900">
              Loại ghế được đồng bộ cho mọi chuyến cùng dòng xe. Trạng thái đã bán, đang giữ và thông tin đặt vé không bị thay đổi.
            </p>
          </template>
        </div>

        <footer class="flex shrink-0 flex-col-reverse gap-3 border-t border-slate-200 bg-white px-6 py-4 sm:flex-row sm:items-center sm:justify-between md:px-8">
          <p class="text-xs font-medium text-slate-500">
            {{ configured ? 'Mẫu tùy chỉnh đã được lưu.' : 'Đang dùng sơ đồ mặc định.' }}
          </p>
          <div class="flex gap-3">
            <button type="button" class="rounded-xl px-5 py-2.5 text-sm font-bold text-slate-600 transition hover:bg-slate-100" @click="close">
              Hủy
            </button>
            <button
              type="button"
              :disabled="loading || saving || !!errorMessage"
              class="flex min-w-36 items-center justify-center gap-2 rounded-xl bg-emerald-800 px-5 py-2.5 text-sm font-black text-white transition hover:bg-emerald-900 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
              @click="saveLayout"
            >
              <span class="material-symbols-outlined text-[18px]">{{ saving ? 'progress_activity' : 'save' }}</span>
              {{ saving ? 'Đang lưu...' : 'Lưu sơ đồ' }}
            </button>
          </div>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useApi } from '@/composables/useApi';
import SeatFloorEditor from './SeatFloorEditor.vue';

const props = defineProps({
  open: Boolean,
  busType: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'saved']);
const api = useApi();

const seats = ref([]);
const loading = ref(false);
const saving = ref(false);
const configured = ref(false);
const errorMessage = ref('');
const activeType = ref('PRIORITY');

const seatTypeOptions = [
  { value: 'STANDARD', label: 'Ghế thường', icon: 'event_seat', activeClass: 'border-emerald-700 bg-emerald-50 text-emerald-900' },
  { value: 'PRIORITY', label: 'Ghế ưu tiên', icon: 'star', activeClass: 'border-amber-400 bg-amber-50 text-amber-900' },
  { value: 'CHILD', label: 'Dành cho trẻ em', icon: 'child_care', activeClass: 'border-sky-300 bg-sky-50 text-sky-800' }
];

const floor1Seats = computed(() => seats.value.filter(seat => seat.seatFloor === 1));
const floor2Seats = computed(() => seats.value.filter(seat => seat.seatFloor === 2));
const priorityCount = computed(() => seats.value.filter(seat => seat.seatType === 'PRIORITY').length);
const childCount = computed(() => seats.value.filter(seat => seat.seatType === 'CHILD').length);

const loadLayout = async () => {
  if (!props.busType?.id) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    const { data } = await api.get(`/bus-types/${props.busType.id}/seat-layout`);
    seats.value = data.seats.map(seat => ({ ...seat }));
    configured.value = data.configured;
  } catch (error) {
    console.error('Không thể tải mẫu ghế:', error);
    errorMessage.value = error.response?.data?.message || 'Không thể tải sơ đồ ghế. Hãy kiểm tra backend và thử lại.';
  } finally {
    loading.value = false;
  }
};

const assignType = seat => {
  seat.seatType = activeType.value;
};

const resetLayout = () => {
  seats.value.forEach(seat => { seat.seatType = 'STANDARD'; });
};

const saveLayout = async () => {
  saving.value = true;
  errorMessage.value = '';
  try {
    const payload = seats.value.map(({ seatNumber, seatFloor, seatType }) => ({ seatNumber, seatFloor, seatType }));
    const { data } = await api.put(`/bus-types/${props.busType.id}/seat-layout`, payload);
    seats.value = data.seats.map(seat => ({ ...seat }));
    configured.value = true;
    emit('saved', data);
    emit('close');
  } catch (error) {
    console.error('Không thể lưu mẫu ghế:', error);
    errorMessage.value = error.response?.data?.message || error.response?.data?.error || 'Không thể lưu sơ đồ ghế.';
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');

watch(
  () => [props.open, props.busType?.id],
  ([open]) => {
    if (open) loadLayout();
  }
);
</script>
