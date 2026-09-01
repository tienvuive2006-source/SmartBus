<template>
  <section class="overflow-hidden rounded-2xl border" :class="urgent ? 'border-rose-200 bg-rose-50' : 'border-emerald-800/10 bg-[#075955]'">
    <div class="flex items-center justify-between gap-4 px-4 py-3 text-white" :class="urgent ? 'text-rose-700' : ''">
      <div class="flex min-w-0 items-center gap-2.5">
        <span class="material-symbols-outlined text-[20px]">timer</span>
        <div class="min-w-0">
          <p class="text-[9px] font-black uppercase tracking-[0.16em]" :class="urgent ? 'text-rose-500' : 'text-emerald-200'">
            {{ pending ? 'Thời gian giữ chỗ' : 'Đang tạm giữ chỗ' }}
          </p>
          <p class="truncate text-[11px] font-semibold" :class="urgent ? 'text-rose-600' : 'text-white/80'">
            {{ pending ? 'Bắt đầu sau khi tiếp tục thanh toán' : statusText }}
          </p>
        </div>
      </div>

      <strong class="shrink-0 font-mono text-xl font-black tabular-nums tracking-tight" :class="urgent ? 'animate-pulse text-rose-600' : 'text-amber-300'">
        {{ formattedTime }}
      </strong>
    </div>

    <div class="h-1 bg-black/10">
      <div
        class="h-full transition-[width] duration-1000 ease-linear"
        :class="urgent ? 'bg-rose-500' : 'bg-amber-400'"
        :style="{ width: `${progress}%` }"
      ></div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue';

const props = defineProps({
  expiresAt: { type: [String, Number, Date], default: null },
  durationSeconds: { type: Number, default: 600 },
  pending: { type: Boolean, default: false },
  statusText: { type: String, default: 'Vui lòng hoàn tất thanh toán' }
});

const emit = defineEmits(['expired', 'tick']);
const timeLeft = ref(props.durationSeconds);
let timerId = null;
let emittedExpired = false;

const formattedTime = computed(() => {
  const minutes = Math.floor(timeLeft.value / 60).toString().padStart(2, '0');
  const seconds = Math.floor(timeLeft.value % 60).toString().padStart(2, '0');
  return `${minutes}:${seconds}`;
});

const progress = computed(() => Math.max(0, Math.min(100, (timeLeft.value / props.durationSeconds) * 100)));
const urgent = computed(() => !props.pending && timeLeft.value <= 60);

const stopTimer = () => {
  if (timerId) clearInterval(timerId);
  timerId = null;
};

const updateTime = () => {
  if (props.pending || !props.expiresAt) {
    timeLeft.value = props.durationSeconds;
    return;
  }

  const deadline = new Date(props.expiresAt).getTime();
  timeLeft.value = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
  emit('tick', timeLeft.value);

  if (timeLeft.value === 0 && !emittedExpired) {
    emittedExpired = true;
    stopTimer();
    emit('expired');
  }
};

const startTimer = () => {
  stopTimer();
  emittedExpired = false;
  updateTime();
  if (!props.pending && props.expiresAt && timeLeft.value > 0) {
    timerId = setInterval(updateTime, 1000);
  }
};

watch(() => [props.expiresAt, props.pending], startTimer, { immediate: true });
onBeforeUnmount(stopTimer);
</script>
