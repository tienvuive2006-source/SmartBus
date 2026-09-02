<template>
  <div class="p-6 md:p-8 max-w-7xl mx-auto">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-extrabold text-slate-800 tracking-tight">Quản lý Đặt vé</h1>
        <p class="text-xs font-semibold text-slate-500 mt-1">Kiểm soát đơn hàng và trạng thái giao dịch</p>
      </div>
      <div class="flex items-center gap-3">
        <button class="bg-white border border-slate-200 text-slate-700 px-4 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 hover:bg-slate-50 hover:text-[#075955] hover:border-[#075955]/30 transition-all shadow-sm">
          <span class="material-symbols-outlined text-[18px]">download</span> Báo cáo PDF
        </button>
      </div>
    </div>

    <!-- 1. Statistics Cards -->
    <BookingStats 
      v-if="activeTab === 'bookings'"
      :totalCount="totalElements"
      :paidCount="paidCount"
      :pendingCount="pendingCount"
      :checkedInCount="checkedInCount"
      :cancelledCount="cancelledCount"
    />

    <div class="mb-6 flex w-fit rounded-xl border border-slate-200 bg-white p-1 shadow-sm">
      <button type="button" :class="tabClass('bookings')" @click="activeTab = 'bookings'">
        <span class="material-symbols-outlined text-lg">receipt_long</span>
        Danh sách đặt vé
      </button>
      <button type="button" :class="tabClass('exchanges')" @click="openExchangeTab">
        <span class="material-symbols-outlined text-lg">swap_horiz</span>
        Lịch sử đổi vé
        <span class="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-black text-slate-500">{{ exchanges.length }}</span>
      </button>
    </div>

    <BookingFilters
      v-if="activeTab === 'bookings'"
      v-model:search-query="searchQuery"
      v-model:status-filter="statusFilter"
      v-model:payment-filter="paymentFilter"
      v-model:route-filter="routeFilter"
      v-model:date-from="dateFrom"
      v-model:date-to="dateTo"
      :route-options="routeOptions"
      @reset="resetFilters"
      @refresh="refreshBookings"
    />

    <!-- 2. Main Content: Table -->
    <BookingTable 
      v-if="activeTab === 'bookings'"
      :bookings="filteredBookings"
      :statusLabels="statusLabels"
      :statusStyles="statusStyles"
      @view="viewDetail"
      @update-status="updateStatus"
      @view-reason="viewReason"
    />
    <AdminPagination
      v-if="activeTab === 'bookings'"
      :page="currentPage"
      :total-pages="totalPages"
      :total-elements="totalElements"
      :page-size="pageSize"
      :current-count="bookings.length"
      @update:page="changePage"
    />
    <BookingExchangeTable v-else :exchanges="exchanges" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useApi } from '@/composables/useApi';
import BookingStats from '../../components/admin/booking/BookingStats.vue';
import BookingTable from '../../components/admin/booking/BookingTable.vue';
import BookingFilters from '../../components/admin/booking/BookingFilters.vue';
import BookingExchangeTable from '../../components/admin/booking/BookingExchangeTable.vue';
import AdminPagination from '@/components/admin/common/AdminPagination.vue';
import { useRouteStopApi } from '@/services/routeStopApi';
import { formatSeatWithType } from '@/utils/seatTypePresentation';

const api = useApi();
const routeStopApi = useRouteStopApi();
const bookings = ref([]);
const searchQuery = ref('');
const statusFilter = ref('ALL');
const paymentFilter = ref('ALL');
const routeFilter = ref('ALL');
const dateFrom = ref('');
const dateTo = ref('');
const routeOptions = ref([]);
const activeTab = ref('bookings');
const exchanges = ref([]);
const currentPage = ref(0);
const pageSize = 20;
const totalPages = ref(0);
const totalElements = ref(0);
const statusCounts = ref({});
let searchTimer = null;
let requestSequence = 0;

const statusLabels = {
  'PAID': 'Đã thanh toán',
  'PENDING': 'Chờ thanh toán',
  'CANCELLED': 'Đã hủy bỏ',
  'CHECKED_IN': 'Đã lên xe'
};

const statusStyles = {
  'PAID': 'bg-emerald-50 text-emerald-600 border-emerald-100',
  'PENDING': 'bg-amber-50 text-amber-600 border-amber-100',
  'CANCELLED': 'bg-rose-50 text-rose-600 border-rose-100',
  'CHECKED_IN': 'bg-blue-50 text-blue-600 border-blue-100'
};

const mapBooking = b => {
  let mappedStatus = b.status ? b.status.toUpperCase() : 'PENDING';
  if (mappedStatus === 'SUCCESS') mappedStatus = 'PAID';
  return {
    ...b,
    status: mappedStatus,
    exchange: exchanges.value.find(item => Number(item.bookingId) === Number(b.id) && item.status === 'COMPLETED') || null,
    route: b.trip ? `${b.trip.departurePoint} ➔ ${b.trip.arrivalPoint}` : 'N/A',
    departureTime: b.trip ? `${b.trip.departureTime} - ${b.trip.departureDate ? b.trip.departureDate.split('-').reverse().join('/') : ''}` : '',
    seats: b.seatNumbers || []
  };
};

const fetchBookings = async () => {
  const sequence = ++requestSequence;
  try {
    const [departurePoint, arrivalPoint] = parseRouteFilter(routeFilter.value);
    const response = await api.get('/admin/bookings/page', {
      params: {
        page: currentPage.value,
        size: pageSize,
        search: searchQuery.value || undefined,
        status: statusFilter.value,
        paymentMethod: paymentFilter.value,
        dateFrom: dateFrom.value || undefined,
        dateTo: dateTo.value || undefined,
        departurePoint: departurePoint || undefined,
        arrivalPoint: arrivalPoint || undefined
      }
    });
    if (sequence !== requestSequence) return;
    const payload = response.data || {};
    bookings.value = (payload.content || []).map(mapBooking);
    if (bookings.value.length) {
      const selectionsResponse = await routeStopApi.getAdminBookingSelections(bookings.value.map(item => item.id));
      const selectionMap = new Map((selectionsResponse.data || []).map(item => [Number(item.bookingId), item]));
      bookings.value = bookings.value.map(item => ({ ...item, stopSelection: selectionMap.get(Number(item.id)) || null }));
    }
    totalPages.value = Number(payload.totalPages || 0);
    totalElements.value = Number(payload.totalElements || 0);
    statusCounts.value = payload.statusCounts || {};
    if (Array.isArray(payload.routeOptions)) routeOptions.value = payload.routeOptions;
  } catch (error) {
    console.error('Lỗi khi tải danh sách vé:', error);
    try {
      const legacyResponse = await api.get('/admin/bookings');
      if (sequence !== requestSequence) return;
      const allBookings = (legacyResponse.data || []).map(mapBooking);
      statusCounts.value = allBookings.reduce((counts, booking) => {
        counts[booking.status] = (counts[booking.status] || 0) + 1;
        return counts;
      }, {});
      const query = searchQuery.value.trim().toLowerCase();
      const [departurePoint, arrivalPoint] = parseRouteFilter(routeFilter.value);
      const filtered = allBookings.filter(booking => {
        const matchesStatus = statusFilter.value === 'ALL' || booking.status === statusFilter.value;
        const matchesPayment = paymentFilter.value === 'ALL' || booking.paymentMethod === paymentFilter.value;
        const createdDate = booking.createdAt ? booking.createdAt.slice(0, 10) : '';
        const matchesDateFrom = !dateFrom.value || createdDate >= dateFrom.value;
        const matchesDateTo = !dateTo.value || createdDate <= dateTo.value;
        const matchesRoute = !departurePoint || (
          booking.trip?.departurePoint === departurePoint && booking.trip?.arrivalPoint === arrivalPoint
        );
        const matchesSearch = !query
          || String(booking.id).includes(query)
          || String(booking.ticketCode || '').toLowerCase().includes(query)
          || String(booking.customerName || '').toLowerCase().includes(query)
          || String(booking.customerPhone || '').includes(query);
        return matchesStatus && matchesPayment && matchesDateFrom && matchesDateTo && matchesRoute && matchesSearch;
      });
      totalElements.value = filtered.length;
      totalPages.value = Math.ceil(filtered.length / pageSize);
      const start = currentPage.value * pageSize;
      bookings.value = filtered.slice(start, start + pageSize);
    } catch (legacyError) {
      console.error('API danh sách vé dự phòng cũng không khả dụng:', legacyError);
      bookings.value = [];
    }
  }
};

const fetchExchanges = async () => {
  try {
    const response = await api.get('/ticket-exchanges/admin');
    exchanges.value = Array.isArray(response.data) ? response.data : [];
    bookings.value = bookings.value.map(booking => ({
      ...booking,
      exchange: exchanges.value.find(item => Number(item.bookingId) === Number(booking.id) && item.status === 'COMPLETED') || null
    }));
  } catch (error) {
    console.error('Lỗi khi tải lịch sử đổi vé:', error);
  }
};

const openExchangeTab = () => {
  activeTab.value = 'exchanges';
  fetchExchanges();
};

const tabClass = tab => [
  'flex items-center gap-2 rounded-lg px-4 py-2.5 text-xs font-black transition',
  activeTab.value === tab ? 'bg-[#075955] text-white shadow-sm' : 'text-slate-500 hover:bg-slate-50'
];

const filteredBookings = computed(() => bookings.value);

const parseRouteFilter = value => {
  if (!value || value === 'ALL') return ['', ''];
  const separatorIndex = value.indexOf('|||');
  if (separatorIndex < 0) return ['', ''];
  return [value.slice(0, separatorIndex), value.slice(separatorIndex + 3)];
};

const paidCount = computed(() => Number(statusCounts.value.PAID || 0) + Number(statusCounts.value.SUCCESS || 0));
const pendingCount = computed(() => Number(statusCounts.value.PENDING || 0));
const checkedInCount = computed(() => Number(statusCounts.value.CHECKED_IN || 0));
const cancelledCount = computed(() => Number(statusCounts.value.CANCELLED || 0));

const changePage = page => {
  currentPage.value = page;
  fetchBookings();
};

watch([statusFilter, paymentFilter, routeFilter, dateFrom, dateTo], () => {
  currentPage.value = 0;
  fetchBookings();
});

watch(searchQuery, () => {
  currentPage.value = 0;
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(fetchBookings, 300);
});

const resetFilters = () => {
  searchQuery.value = '';
  statusFilter.value = 'ALL';
  paymentFilter.value = 'ALL';
  routeFilter.value = 'ALL';
  dateFrom.value = '';
  dateTo.value = '';
  currentPage.value = 0;
};

const refreshBookings = () => {
  currentPage.value = 0;
  fetchBookings();
};

const updateStatus = async (id, newStatus) => {
  if (!confirm(`Xác nhận thay đổi trạng thái đơn #${id}?`)) return;
  try {
    await api.post(`/admin/bookings/${id}/status`, { status: newStatus });
    fetchBookings();
    alert('Cập nhật thành công!');
  } catch (error) { alert('Lỗi hệ thống!'); }
};

const viewDetail = (booking) => {
  const seatDescription = booking.seats
    .map(seatNumber => formatSeatWithType(seatNumber, booking.seatTypes))
    .join(', ');
  let msg = `CHI TIẾT VÉ ${booking.ticketCode || `#${booking.id}`}\n------------------\nKhách: ${booking.customerName}\nSĐT: ${booking.customerPhone}\nTuyến: ${booking.route}\nGhế: ${seatDescription}\nTổng tiền: ${booking.totalPrice.toLocaleString()}đ`;
  if (booking.discountAmount > 0) {
    msg += `\nĐã giảm giá: -${booking.discountAmount.toLocaleString()}đ (Mã Voucher)`;
  }
  if (booking.status === 'CANCELLED' && booking.cancellationReason) {
    msg += `\nLý do hủy: ${booking.cancellationReason}`;
  }
  if (booking.exchange) {
    msg += `\nĐã đổi vé: ${booking.exchange.exchangeType === 'SEAT' ? 'Đổi ghế' : 'Đổi chuyến/ngày'}`;
    msg += `\nGhế cũ/mới: ${booking.exchange.oldSeatNumbers} → ${booking.exchange.newSeatNumbers}`;
  }
  alert(msg);
};

const viewReason = (reason) => {
  alert(`LÝ DO HỦY VÉ:\n\n${reason}`);
};

onMounted(async () => {
  await Promise.all([fetchExchanges(), fetchBookings()]);
});
onUnmounted(() => {
  if (searchTimer) clearTimeout(searchTimer);
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
</style>
