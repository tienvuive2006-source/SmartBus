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
      :totalCount="bookings.length"
      :paidCount="paidCount"
      :pendingCount="pendingCount"
      :checkedInCount="checkedInCount"
      :cancelledCount="cancelledCount"
    />

    <!-- 2. Main Content: Table -->
    <BookingTable 
      :bookings="filteredBookings"
      v-model:searchQuery="searchQuery"
      v-model:statusFilter="statusFilter"
      :statusLabels="statusLabels"
      :statusStyles="statusStyles"
      @view="viewDetail"
      @update-status="updateStatus"
      @view-reason="viewReason"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';
import BookingStats from '../../components/admin/booking/BookingStats.vue';
import BookingTable from '../../components/admin/booking/BookingTable.vue';

const api = useApi();
const bookings = ref([]);
const searchQuery = ref('');
const statusFilter = ref('ALL');

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

const fetchBookings = async () => {
  try {
    const response = await api.get('/admin/bookings');
    bookings.value = response.data.map(b => {
      let mappedStatus = b.status ? b.status.toUpperCase() : 'PENDING';
      if (mappedStatus === 'SUCCESS') {
        mappedStatus = 'PAID';
      }
      return {
        ...b,
        status: mappedStatus,
        route: b.trip ? `${b.trip.departurePoint} ➔ ${b.trip.arrivalPoint}` : 'N/A',
        departureTime: b.trip ? `${b.trip.departureTime} - ${b.trip.departureDate ? b.trip.departureDate.split('-').reverse().join('/') : ''}` : '',
        seats: b.seatNumbers || []
      };
    }).sort((a, b) => b.id - a.id);
  } catch (error) {
    console.error('Lỗi khi tải danh sách vé:', error);
  }
};

const filteredBookings = computed(() => {
  return bookings.value.filter(b => {
    const query = searchQuery.value.toLowerCase();
    const matchesSearch = b.customerName.toLowerCase().includes(query) ||
                          b.customerPhone.includes(query) ||
                          b.id.toString().includes(query);
    
    const matchesStatus = statusFilter.value === 'ALL' || b.status === statusFilter.value;
    
    return matchesSearch && matchesStatus;
  });
});

const paidCount = computed(() => bookings.value.filter(b => b.status === 'PAID').length);
const pendingCount = computed(() => bookings.value.filter(b => b.status === 'PENDING').length);
const checkedInCount = computed(() => bookings.value.filter(b => b.status === 'CHECKED_IN').length);
const cancelledCount = computed(() => bookings.value.filter(b => b.status === 'CANCELLED').length);

const updateStatus = async (id, newStatus) => {
  if (!confirm(`Xác nhận thay đổi trạng thái đơn #${id}?`)) return;
  try {
    await api.post(`/admin/bookings/${id}/status`, { status: newStatus });
    fetchBookings();
    alert('Cập nhật thành công!');
  } catch (error) { alert('Lỗi hệ thống!'); }
};

const viewDetail = (booking) => {
  let msg = `CHI TIẾT VÉ #${booking.id}\n------------------\nKhách: ${booking.customerName}\nSĐT: ${booking.customerPhone}\nTuyến: ${booking.route}\nGhế: ${booking.seats.join(', ')}\nTổng tiền: ${booking.totalPrice.toLocaleString()}đ`;
  if (booking.discountAmount > 0) {
    msg += `\nĐã giảm giá: -${booking.discountAmount.toLocaleString()}đ (Mã Voucher)`;
  }
  if (booking.status === 'CANCELLED' && booking.cancellationReason) {
    msg += `\nLý do hủy: ${booking.cancellationReason}`;
  }
  alert(msg);
};

const viewReason = (reason) => {
  alert(`LÝ DO HỦY VÉ:\n\n${reason}`);
};

onMounted(fetchBookings);
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
</style>
