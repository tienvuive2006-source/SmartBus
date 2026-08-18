<template>
  <aside class="filter-sidebar hidden self-start lg:block">
    <div class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-[0_8px_26px_rgba(20,62,58,0.06)]">
      <div class="flex items-center justify-between border-b border-slate-100 px-5 py-5">
        <h2 class="text-[15px] font-extrabold uppercase tracking-wide text-slate-800">Bộ lọc tìm kiếm</h2>
        <button type="button" class="flex items-center gap-1 text-[11px] font-semibold text-slate-500 transition hover:text-red-500" @click="$emit('clear-filters')">
          <span class="material-symbols-outlined text-[16px]">history</span>Xóa bộ lọc
        </button>
      </div>

      <div class="space-y-5 px-5 py-4">
        <section>
          <h3 class="filter-title">Sắp xếp theo</h3>
          <select
            :value="modelValueSort"
            class="mt-2 w-full rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-[13px] font-semibold text-slate-700 outline-none transition focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/10"
            @change="$emit('update:modelValueSort', $event.target.value)"
          >
            <option v-for="option in sortOptions" :key="option.id" :value="option.id">{{ option.name }}</option>
          </select>
        </section>

        <section>
          <div class="flex items-center justify-between">
            <h3 class="filter-title">Giá vé</h3>
            <span class="text-xs font-bold text-[#075955]">{{ formatPrice(modelValuePriceMax) }}</span>
          </div>
          <input
            :value="modelValuePriceMax"
            type="range"
            min="300000"
            max="1200000"
            step="50000"
            class="price-range mt-4 w-full"
            aria-label="Giá vé tối đa"
            @input="$emit('update:modelValuePriceMax', Number($event.target.value))"
          />
          <div class="mt-1 flex justify-between text-[10px] font-medium text-slate-400">
            <span>300.000đ</span><span>1.200.000đ</span>
          </div>
        </section>

        <section>
          <h3 class="filter-title">Khung giờ khởi hành</h3>
          <div class="mt-2 space-y-1">
            <FilterCheck
              v-for="slot in timeOptions"
              :key="slot.value"
              :checked="modelValueTime.includes(slot.value)"
              :label="slot.label"
              :description="slot.description"
              @change="updateArray('time', slot.value, $event)"
            />
          </div>
        </section>

        <section>
          <h3 class="filter-title">Loại xe</h3>
          <div class="mt-2 space-y-1">
            <FilterCheck
              v-for="type in busTypeOptions"
              :key="type.value"
              :checked="modelValueBusTypes.includes(type.value)"
              :label="type.label"
              @change="updateArray('bus', type.value, $event)"
            />
          </div>
        </section>

        <section>
          <h3 class="filter-title">Hạng ghế / giường</h3>
          <div class="mt-2 space-y-1">
            <FilterCheck
              v-for="seat in seatClassOptions"
              :key="seat.value"
              :checked="modelValueSeatClasses.includes(seat.value)"
              :label="seat.label"
              @change="updateArray('seat', seat.value, $event)"
            />
          </div>
        </section>

        <section>
          <h3 class="filter-title">Tiện ích</h3>
          <div class="mt-2 space-y-1">
            <FilterCheck
              v-for="utility in utilityOptions"
              :key="utility.value"
              :checked="modelValueUtilities.includes(utility.value)"
              :label="utility.label"
              :icon="utility.icon"
              @change="updateArray('utility', utility.value, $event)"
            />
          </div>
        </section>
      </div>

      <button type="button" class="flex w-full items-center justify-center gap-2 border-t border-slate-100 py-4 text-xs font-bold text-slate-600 transition hover:bg-slate-50 hover:text-[#075955]">
        Xem thêm bộ lọc<span class="material-symbols-outlined text-[17px]">expand_more</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { defineComponent, h } from 'vue';

const props = defineProps({
  modelValueSort: { type: String, required: true },
  modelValueTime: { type: Array, required: true },
  modelValuePriceMax: { type: Number, required: true },
  modelValueBusTypes: { type: Array, required: true },
  modelValueSeatClasses: { type: Array, required: true },
  modelValueUtilities: { type: Array, required: true },
  sortOptions: { type: Array, required: true },
  busTypeOptions: { type: Array, default: () => [] },
  seatClassOptions: { type: Array, default: () => [] },
  utilityOptions: { type: Array, default: () => [] }
});

const emit = defineEmits([
  'update:modelValueSort',
  'update:modelValueTime',
  'update:modelValuePriceMax',
  'update:modelValueBusTypes',
  'update:modelValueSeatClasses',
  'update:modelValueUtilities',
  'clear-filters'
]);

const timeOptions = [
  { value: 'early', label: '00:00 - 06:00', description: 'Sáng sớm' },
  { value: 'morning', label: '06:00 - 12:00', description: 'Buổi sáng' },
  { value: 'afternoon', label: '12:00 - 18:00', description: 'Buổi chiều' },
  { value: 'evening', label: '18:00 - 24:00', description: 'Buổi tối' }
];
const FilterCheck = defineComponent({
  props: {
    checked: Boolean,
    label: String,
    description: String,
    icon: String
  },
  emits: ['change'],
  setup(componentProps, { emit: componentEmit }) {
    return () => h('label', { class: 'flex cursor-pointer items-center gap-2 rounded-md py-1.5 text-[12px] text-slate-600 transition hover:text-[#075955]' }, [
      h('input', {
        type: 'checkbox',
        checked: componentProps.checked,
        class: 'h-4 w-4 rounded border-slate-300 accent-[#075955]',
        onChange: (event) => componentEmit('change', event.target.checked)
      }),
      componentProps.icon ? h('span', { class: 'material-symbols-outlined text-[17px] text-slate-500' }, componentProps.icon) : null,
      h('span', { class: 'font-semibold' }, componentProps.label),
      componentProps.description ? h('span', { class: 'ml-auto text-[10px] text-slate-400' }, componentProps.description) : null
    ]);
  }
});

const updateArray = (type, value, checked) => {
  const source = type === 'time'
    ? props.modelValueTime
    : type === 'bus'
      ? props.modelValueBusTypes
      : type === 'seat'
        ? props.modelValueSeatClasses
        : props.modelValueUtilities;
  const updated = checked ? [...new Set([...source, value])] : source.filter((item) => item !== value);
  const eventName = type === 'time'
    ? 'update:modelValueTime'
    : type === 'bus'
      ? 'update:modelValueBusTypes'
      : type === 'seat'
        ? 'update:modelValueSeatClasses'
        : 'update:modelValueUtilities';
  emit(eventName, updated);
};

const formatPrice = (value) => `${Number(value || 0).toLocaleString('vi-VN')}đ`;
</script>

<style scoped>
.filter-sidebar { position: sticky; top: 154px; }
.filter-title { color: #334155; font-size: 11px; font-weight: 800; letter-spacing: .04em; text-transform: uppercase; }
.price-range { accent-color: #d7a92e; cursor: pointer; }
</style>
