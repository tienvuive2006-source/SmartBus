<template>
  <section class="mb-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm" aria-label="Bộ lọc đơn đặt vé">
    <div class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-12">
      <label class="relative block xl:col-span-3">
        <span class="sr-only">Tìm kiếm đơn đặt vé</span>
        <span class="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[17px] text-slate-400">search</span>
        <input
          :value="searchQuery"
          type="search"
          class="filter-control pl-10"
          placeholder="Tìm mã vé, khách hàng, SĐT..."
          @input="$emit('update:searchQuery', $event.target.value)"
        />
      </label>

      <div class="flex min-w-0 items-center rounded-xl border border-slate-200 bg-white px-2 focus-within:border-[#075955] focus-within:ring-1 focus-within:ring-[#075955]/20 md:col-span-2 xl:col-span-3">
        <span class="material-symbols-outlined ml-1 shrink-0 text-[17px] text-slate-400">date_range</span>
        <label class="min-w-0 flex-1">
          <span class="sr-only">Ngày đặt từ</span>
          <input :value="dateFrom" :max="dateTo || undefined" type="date" class="date-control" @input="$emit('update:dateFrom', $event.target.value)" />
        </label>
        <span class="px-1 text-xs font-bold text-slate-300">đến</span>
        <label class="min-w-0 flex-1">
          <span class="sr-only">Ngày đặt đến</span>
          <input :value="dateTo" :min="dateFrom || undefined" type="date" class="date-control" @input="$emit('update:dateTo', $event.target.value)" />
        </label>
      </div>

      <label class="block xl:col-span-2">
        <span class="sr-only">Lọc theo trạng thái</span>
        <select :value="statusFilter" class="filter-control" @change="$emit('update:statusFilter', $event.target.value)">
          <option value="ALL">Tất cả trạng thái</option>
          <option value="PAID">Đã thanh toán</option>
          <option value="PENDING">Chờ thanh toán</option>
          <option value="CHECKED_IN">Đã lên xe</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
      </label>

      <label class="block xl:col-span-2">
        <span class="sr-only">Lọc theo phương thức thanh toán</span>
        <select :value="paymentFilter" class="filter-control" @change="$emit('update:paymentFilter', $event.target.value)">
          <option value="ALL">Tất cả PTTT</option>
          <option value="WALLET">Ví điện tử</option>
          <option value="QR">QR ngân hàng</option>
          <option value="CASH">Tiền mặt</option>
        </select>
      </label>

      <label class="block xl:col-span-2">
        <span class="sr-only">Lọc theo tuyến đường</span>
        <select :value="routeFilter" class="filter-control" @change="$emit('update:routeFilter', $event.target.value)">
          <option value="ALL">Tất cả tuyến đường</option>
          <option v-for="route in routeOptions" :key="routeValue(route)" :value="routeValue(route)">
            {{ route.departurePoint }} → {{ route.arrivalPoint }}
          </option>
        </select>
      </label>
    </div>

    <div class="mt-3 flex flex-wrap items-center gap-2 border-t border-slate-100 pt-3">
      <button type="button" class="filter-action" :disabled="!hasFilters" @click="$emit('reset')">
        <span class="material-symbols-outlined text-[16px]">filter_alt_off</span>
        Xóa bộ lọc
      </button>
      <button type="button" class="filter-action hover:border-[#075955]/30 hover:bg-[#075955]/5 hover:text-[#075955]" @click="$emit('refresh')">
        <span class="material-symbols-outlined text-[16px]">refresh</span>
        Làm mới
      </button>
      <span v-if="hasFilters" class="ml-auto text-[10px] font-bold text-[#075955]">Đang áp dụng {{ activeFilterCount }} bộ lọc</span>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';

const ROUTE_SEPARATOR = '|||';
const props = defineProps({
  searchQuery: { type: String, default: '' },
  statusFilter: { type: String, default: 'ALL' },
  paymentFilter: { type: String, default: 'ALL' },
  routeFilter: { type: String, default: 'ALL' },
  dateFrom: { type: String, default: '' },
  dateTo: { type: String, default: '' },
  routeOptions: { type: Array, default: () => [] }
});

defineEmits(['update:searchQuery', 'update:statusFilter', 'update:paymentFilter', 'update:routeFilter', 'update:dateFrom', 'update:dateTo', 'reset', 'refresh']);

const activeFilterCount = computed(() => [
  props.searchQuery.trim(),
  props.statusFilter !== 'ALL',
  props.paymentFilter !== 'ALL',
  props.routeFilter !== 'ALL',
  props.dateFrom || props.dateTo
].filter(Boolean).length);
const hasFilters = computed(() => activeFilterCount.value > 0);
const routeValue = route => `${route.departurePoint}${ROUTE_SEPARATOR}${route.arrivalPoint}`;
</script>

<style scoped>
.filter-control { width: 100%; height: 42px; border: 1px solid rgb(226 232 240); border-radius: .75rem; background: white; padding: 0 .75rem; color: rgb(51 65 85); font-size: .75rem; font-weight: 600; outline: none; transition: border-color 150ms, box-shadow 150ms; }
.filter-control:focus { border-color: #075955; box-shadow: 0 0 0 1px rgb(7 89 85 / .2); }
.date-control { width: 100%; min-width: 0; height: 40px; border: 0; background: transparent; padding: 0 .25rem; color: rgb(51 65 85); font-size: .7rem; font-weight: 600; outline: none; }
.filter-action { display: inline-flex; height: 36px; align-items: center; gap: .375rem; border: 1px solid rgb(226 232 240); border-radius: .5rem; background: white; padding: 0 .75rem; color: rgb(71 85 105); font-size: .6875rem; font-weight: 700; transition: 150ms; }
.filter-action:hover { background: rgb(248 250 252); }
.filter-action:disabled { cursor: not-allowed; opacity: .4; }
</style>
