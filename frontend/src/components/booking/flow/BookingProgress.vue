<template>
  <nav class="rounded-xl border border-slate-200/80 bg-white/95 px-4 py-3 shadow-[0_6px_20px_rgba(7,89,85,0.04)] sm:px-5" aria-label="Tiến trình đặt vé">
    <ol class="grid grid-cols-4">
      <li v-for="(step, index) in steps" :key="step.id" class="flex min-w-0 items-start">
        <div class="flex min-w-0 shrink-0 flex-col items-center text-center sm:flex-row sm:text-left">
          <span
            class="grid h-7 w-7 shrink-0 place-items-center rounded-full border text-[11px] font-extrabold transition-colors duration-200"
            :class="stepClass(index + 1)"
          >
            <span v-if="activeStep > index + 1" class="material-symbols-outlined text-[17px]">check</span>
            <span v-else>{{ index + 1 }}</span>
          </span>

          <span class="mt-2 min-w-0 sm:ml-3 sm:mt-0">
            <strong class="block truncate text-[10px] font-extrabold tracking-tight sm:text-[11px]" :class="activeStep >= index + 1 ? 'text-slate-800' : 'text-slate-400'">
              {{ step.title }}
            </strong>
            <small class="mt-0.5 hidden text-[9px] font-medium text-slate-400 lg:block">{{ step.description }}</small>
          </span>
        </div>

        <span
          v-if="index < steps.length - 1"
          class="mx-3 mt-3.5 hidden h-px min-w-4 flex-1 sm:block lg:mx-5"
          :class="activeStep > index + 1 ? 'bg-[#075955]' : 'bg-slate-200'"
          aria-hidden="true"
        ></span>
      </li>
    </ol>
  </nav>
</template>

<script setup>
const props = defineProps({
  activeStep: {
    type: Number,
    default: 1,
    validator: value => value >= 1 && value <= 4
  }
});

const steps = [
  { id: 'seats', title: 'Chọn ghế', description: 'Chọn vị trí phù hợp' },
  { id: 'passenger', title: 'Hành khách', description: 'Nhập thông tin liên hệ' },
  { id: 'payment', title: 'Thanh toán', description: 'Xác nhận và thanh toán' },
  { id: 'complete', title: 'Hoàn tất', description: 'Nhận vé điện tử' }
];

const stepClass = step => {
  if (step < props.activeStep) return 'border-[#075955] bg-[#075955] text-white';
  if (step === props.activeStep) return 'border-[#075955] bg-[#075955] text-white shadow-[0_3px_10px_rgba(7,89,85,0.22)]';
  return 'border-slate-200 bg-slate-100 text-slate-400';
};
</script>
