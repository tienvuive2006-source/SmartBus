<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <!-- Table heading -->
    <div class="p-4 border-b border-slate-100 bg-slate-50/50">
      <h3 class="text-sm font-bold text-slate-800 flex items-center gap-2">
         <span class="material-symbols-outlined text-[#075955] text-[20px]">list_alt</span>
         Danh sách Đơn hàng
      </h3>
    </div>

    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="w-full min-w-[1080px] table-fixed text-left border-collapse">
        <colgroup>
          <col class="w-[13%]" />
          <col class="w-[17%]" />
          <col class="w-[34%]" />
          <col class="w-[14%]" />
          <col class="w-[13%]" />
          <col class="w-[9%]" />
        </colgroup>
        <thead>
          <tr class="bg-slate-50/50 border-b border-slate-100">
            <th class="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest">Mã Vé & Ngày đặt</th>
            <th class="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest">Khách hàng</th>
            <th class="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest">Thông tin lộ trình</th>
            <th class="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest">Thanh toán</th>
            <th class="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest text-center">Trạng thái</th>
            <th class="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="booking in paginatedBookings" :key="booking.id" class="group hover:bg-[#075955]/[0.02] transition-all duration-200">
            <td class="px-5 py-5 border-b border-slate-50 align-top">
              <div class="font-bold text-slate-800 text-sm group-hover:text-[#075955] transition-colors">#{{ booking.id }}</div>
              <div class="text-[10px] font-semibold text-slate-400 uppercase mt-0.5 tracking-tighter">{{ formatDate(booking.createdAt) }}</div>
              <span v-if="booking.exchange" class="mt-2 inline-flex items-center gap-1 whitespace-nowrap rounded-md border border-violet-100 bg-violet-50 px-2 py-1 text-[8px] font-black uppercase tracking-wider text-violet-600" :title="booking.exchange.exchangeType === 'SEAT' ? 'Vé đã đổi ghế' : 'Vé đã đổi chuyến hoặc ngày đi'">
                <span class="material-symbols-outlined text-[11px]">swap_horiz</span>
                Đã đổi vé
              </span>
            </td>
            <td class="px-5 py-5 border-b border-slate-50 align-top">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-[#075955]/10 rounded-xl flex items-center justify-center text-[#075955] font-bold text-sm border border-[#075955]/5">
                  {{ booking.customerName.charAt(0) }}
                </div>
                <div>
                  <div class="font-bold text-slate-800 text-sm">{{ booking.customerName }}</div>
                  <div class="text-[11px] font-semibold text-slate-400 tracking-tight">{{ booking.customerPhone }}</div>
                </div>
              </div>
            </td>
            <td class="px-5 py-5 border-b border-slate-50 align-top">
              <div class="min-w-0">
                <div class="flex items-start gap-2 text-xs font-extrabold leading-5 text-slate-800">
                  <span class="material-symbols-outlined mt-0.5 shrink-0 text-[15px] text-[#075955]">near_me</span>
                  <span>{{ simplifyLocation(booking.route.split(' ➔ ')[0]) }} <span class="mx-1 text-slate-300">→</span> {{ simplifyLocation(booking.route.split(' ➔ ')[1]) }}</span>
                </div>

                <div class="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-[10px] font-semibold text-slate-500">
                  <span class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[13px] text-slate-400">schedule</span>{{ booking.departureTime }}</span>
                  <BookingSeatBadges :seat-numbers="booking.seats" :seat-types="booking.seatTypes" />
                  <span v-if="booking.trip?.busType" class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[13px] text-violet-500">directions_bus</span>{{ booking.trip.busType }}</span>
                </div>

                <div v-if="booking.stopSelection" class="mt-2.5 grid gap-1 rounded-lg bg-slate-50 px-2.5 py-2 text-[9px] font-semibold text-slate-600 ring-1 ring-inset ring-slate-100">
                  <p class="flex min-w-0 items-center gap-1.5"><span class="h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500"></span><b class="text-emerald-700">Đón</b><span class="truncate">{{ booking.stopSelection.pickupName }}</span></p>
                  <p class="flex min-w-0 items-center gap-1.5"><span class="h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span><b class="text-blue-700">Trả</b><span class="truncate">{{ booking.stopSelection.dropoffName }}</span></p>
                </div>
              </div>
            </td>
            <td class="px-5 py-5 border-b border-slate-50 align-top">
              <div class="font-bold text-[#075955] text-base tabular-nums">{{ booking.totalPrice.toLocaleString('vi-VN') }}<span class="text-[10px] ml-0.5">đ</span></div>
              <div class="flex items-center gap-1.5 mt-0.5">
                <span class="text-[9px] text-slate-400 uppercase font-bold tracking-widest">{{ booking.paymentMethod }}</span>
                <span v-if="booking.discountAmount > 0" class="flex items-center gap-0.5 text-[9px] font-bold text-amber-600 bg-amber-50 px-1.5 py-0.5 rounded border border-amber-100" title="Đã áp dụng mã giảm giá">
                  <span class="material-symbols-outlined text-[10px]">redeem</span>
                  -{{ booking.discountAmount.toLocaleString('vi-VN') }}đ
                </span>
              </div>
            </td>
            <td class="px-5 py-5 text-center align-top">
              <span :class="[
                'px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest border shadow-sm inline-block min-w-[120px]',
                statusStyles[booking.status]
              ]">
                {{ statusLabels[booking.status] }}
              </span>
            </td>
            <td class="px-4 py-5 align-top">
              <div class="flex items-center justify-center gap-2">
                <button 
                  @click="$emit('view', booking)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-slate-400 hover:bg-[#075955] hover:text-white transition-all border border-slate-100 shadow-sm"
                  title="Xem chi tiết"
                >
                  <span class="material-symbols-outlined text-lg">visibility</span>
                </button>
                <button 
                  v-if="booking.status === 'PENDING'" 
                  @click="$emit('update-status', booking.id, 'PAID')" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-emerald-500 hover:bg-emerald-500 hover:text-white transition-all border border-slate-100 shadow-sm"
                  title="Xác nhận thanh toán"
                >
                  <span class="material-symbols-outlined text-lg">check_circle</span>
                </button>
                <button 
                  v-if="booking.status === 'PENDING'" 
                  @click="$emit('update-status', booking.id, 'CANCELLED')" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all border border-slate-100 shadow-sm"
                  title="Hủy vé"
                >
                  <span class="material-symbols-outlined text-lg">block</span>
                </button>
                <button 
                  v-if="booking.status === 'CANCELLED' && booking.cancellationReason" 
                  @click="$emit('view-reason', booking.cancellationReason)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-rose-50 text-rose-600 hover:bg-rose-500 hover:text-white transition-all border border-rose-100 shadow-sm"
                  title="Xem lý do hủy"
                >
                  <span class="material-symbols-outlined text-lg">info</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div v-if="bookings.length === 0" class="py-24 text-center">
      <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border-2 border-dashed border-slate-200">
         <span class="material-symbols-outlined text-slate-300 text-5xl">receipt_long</span>
      </div>
      <h4 class="text-lg font-black text-slate-400 uppercase tracking-widest">Không có dữ liệu đơn hàng</h4>
      <p class="text-xs text-slate-400 mt-1">Dữ liệu từ SQL Server đang trống hoặc không khớp với bộ lọc</p>
    </div>

    <!-- Pagination Controls -->
    <div v-if="totalPages > 1" class="p-4 border-t border-slate-100 flex items-center justify-between bg-slate-50/50">
      <span class="text-xs font-semibold text-slate-500">
        Đang xem {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, bookings.length) }} trong tổng số {{ bookings.length }} vé
      </span>
      <div class="flex items-center gap-2">
        <button 
          @click="currentPage--" 
          :disabled="currentPage === 1"
          class="w-8 h-8 flex items-center justify-center rounded bg-white border border-slate-200 text-slate-600 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 transition-colors shadow-sm"
        >
          <span class="material-symbols-outlined text-sm">chevron_left</span>
        </button>
        
        <span class="text-xs font-bold text-slate-700 px-3">
          Trang {{ currentPage }} / {{ totalPages }}
        </span>

        <button 
          @click="currentPage++" 
          :disabled="currentPage === totalPages"
          class="w-8 h-8 flex items-center justify-center rounded bg-white border border-slate-200 text-slate-600 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 transition-colors shadow-sm"
        >
          <span class="material-symbols-outlined text-sm">chevron_right</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import BookingSeatBadges from './BookingSeatBadges.vue';

const props = defineProps({
  bookings: Array,
  statusLabels: Object,
  statusStyles: Object
});

defineEmits(['view', 'update-status', 'view-reason']);

// Pagination Logic
const currentPage = ref(1);
const itemsPerPage = 20;

const totalPages = computed(() => Math.ceil(props.bookings.length / itemsPerPage));

const paginatedBookings = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return props.bookings.slice(start, end);
});

// Reset về trang 1 khi lọc hoặc tìm kiếm (khi danh sách bookings thay đổi)
watch(() => props.bookings, () => {
  currentPage.value = 1;
});

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleString('vi-VN', { 
    day: '2-digit', month: '2-digit', year: 'numeric', 
    hour: '2-digit', minute: '2-digit' 
  });
};

const simplifyLocation = (loc) => {
  if (!loc) return '';
  const parts = loc.split(',');
  return parts[parts.length - 1].trim().replace(/\b(Thành phố|TP|Tỉnh)\b/gi, '').trim();
};
</script>
