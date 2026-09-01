<template>
  <section class="route-config-card space-y-4">
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="text-[10px] font-black uppercase tracking-wider text-slate-600">
          Dòng xe được phép khai thác
        </p>
        <p class="mt-1 text-[10px] font-semibold text-slate-400">
          Khi tạo chuyến, hệ thống chỉ hiển thị các dòng xe đã chọn.
        </p>
      </div>
      <label class="route-config-toggle flex shrink-0 items-center gap-2 text-[10px] font-black text-[#075955] cursor-pointer">
        <input
          :checked="unrestricted"
          type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-[#075955] focus:ring-[#075955]"
          @change="toggleUnrestricted"
        />
        Tất cả dòng xe
      </label>
    </div>

    <div v-if="loading" class="py-3 text-center text-xs font-bold text-slate-400">
      Đang tải cấu hình dòng xe...
    </div>

    <div v-else-if="!busTypes.length" class="rounded-xl bg-amber-50 px-3 py-2 text-xs font-bold text-amber-700">
      Chưa có dòng xe nào trong catalog.
    </div>

    <div v-else class="grid grid-cols-2 gap-2" :class="{ 'opacity-50': unrestricted }">
      <label
        v-for="busType in busTypes"
        :key="busType.id"
        class="route-bus-option flex min-w-0 items-center gap-2 rounded-xl border bg-white px-3 py-3 transition-all"
        :class="isSelected(busType.id) ? 'is-selected border-[#075955]/40' : 'border-slate-200'"
      >
        <input
          type="checkbox"
          :checked="isSelected(busType.id)"
          :disabled="unrestricted"
          class="h-4 w-4 rounded border-slate-300 text-[#075955] focus:ring-[#075955]"
          @change="toggleBusType(busType.id)"
        />
        <span class="min-w-0 flex-1 truncate text-xs font-extrabold text-slate-700">{{ busType.name }}</span>
        <span class="shrink-0 text-[9px] font-bold text-slate-400">{{ busType.seatCount }} ghế</span>
      </label>
    </div>

    <div v-if="!unrestricted && modelValue.length" class="flex items-center gap-3 border-t border-slate-200 pt-3">
      <label class="shrink-0 text-[10px] font-black uppercase tracking-wider text-slate-500">
        Dòng mặc định
      </label>
      <select
        :value="defaultBusTypeId || modelValue[0]"
        class="min-w-0 flex-1 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 outline-none focus:border-[#075955]"
        @change="$emit('update:defaultBusTypeId', Number($event.target.value))"
      >
        <option v-for="busType in selectedBusTypes" :key="busType.id" :value="busType.id">
          {{ busType.name }}
        </option>
      </select>
    </div>

    <p v-if="!unrestricted && !modelValue.length" class="text-[10px] font-bold text-rose-500">
      Vui lòng chọn ít nhất một dòng xe.
    </p>
  </section>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  busTypes: { type: Array, default: () => [] },
  modelValue: { type: Array, default: () => [] },
  unrestricted: { type: Boolean, default: true },
  defaultBusTypeId: { type: Number, default: null },
  loading: { type: Boolean, default: false }
});

const emit = defineEmits([
  'update:modelValue',
  'update:unrestricted',
  'update:defaultBusTypeId'
]);

const selectedBusTypes = computed(() =>
  props.busTypes.filter(busType => props.modelValue.includes(busType.id))
);

const isSelected = id => props.modelValue.includes(id);

const toggleUnrestricted = event => {
  emit('update:unrestricted', event.target.checked);
};

const toggleBusType = id => {
  const next = isSelected(id)
    ? props.modelValue.filter(value => value !== id)
    : [...props.modelValue, id];
  emit('update:modelValue', next);

  if (!next.includes(props.defaultBusTypeId)) {
    emit('update:defaultBusTypeId', next[0] || null);
  }
};
</script>

<style scoped>
.route-config-card {
  border: 1px solid #dce7e5;
  border-radius: 18px;
  background: #fff;
  padding: 1.25rem;
  box-shadow: 0 8px 24px rgba(18, 78, 73, 0.055);
}

.route-config-toggle {
  border-radius: 10px;
  background: #eef7f5;
  padding: 0.55rem 0.7rem;
}

.route-bus-option {
  cursor: pointer;
}

.route-bus-option:hover:not(.is-selected) {
  border-color: #aac8c4;
  background: #fbfdfd;
  transform: translateY(-1px);
}

.route-bus-option.is-selected {
  background: #eff8f6;
  box-shadow: inset 3px 0 #075955;
}
</style>
