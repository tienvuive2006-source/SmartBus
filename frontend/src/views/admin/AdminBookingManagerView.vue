<template>
  <div class="p-6 md:p-10 max-w-7xl mx-auto animate-fade-in">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-8 mb-10 border-b border-slate-200 pb-8">
      <div class="flex items-center gap-5">
        <div class="w-16 h-16 bg-[#075955] rounded-3xl flex items-center justify-center shadow-lg shadow-[#075955]/20">
          <span class="material-symbols-outlined text-white text-4xl">confirmation_number</span>
        </div>
        <div>
          <h1 class="text-3xl font-black text-slate-800 tracking-tight">Quản lý Đặt vé</h1>
          <p class="text-sm font-bold text-slate-400 mt-1 uppercase tracking-widest">Kiểm soát dòng tiền và đơn hàng từ SQL Server</p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <button class="bg-white border border-slate-200 text-[#075955] px-6 py-3.5 rounded-2xl font-black text-xs uppercase tracking-widest flex items-center gap-2 hover:bg-slate-50 transition-all shadow-sm">
          <span class="material-symbols-outlined text-sm">download</span> Xuất báo cáo PDF
        </button>
      </div>
    </div>

    <!-- 1. Statistics Cards -->
    <BookingStats 
      :totalCount="bookings.length"
      :paidCount="paidCount"
      :pendingCount="pendingCount"
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
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import BookingStats from '../../components/admin/booking/BookingStats.vue';
import BookingTable from '../../components/admin/booking/BookingTable.vue';

const bookings = ref([]);
const searchQuery = ref('');
const statusFilter = ref('ALL');

const statusLabels = {
  'PAID': 'Đã thanh toán',
  'PENDING': 'Đang chờ xử lý',
  'CANCELLED': 'Đã hủy bỏ'
};

const statusStyles = {
  'PAID': 'bg-emerald-50 text-emerald-600 border-emerald-100',
  'PENDING': 'bg-amber-50 text-amber-600 border-amber-100',
  'CANCELLED': 'bg-rose-50 text-rose-600 border-rose-100'
};

const fetchBookings = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/bookings');
    bookings.value = response.data.map(b => ({
      ...b,
      route: b.trip ? `${b.trip.departurePoint} ➔ ${b.trip.arrivalPoint}` : 'N/A',
      departureTime: b.trip ? `${b.trip.departureTime} - ${b.trip.departureDate ? b.trip.departureDate.split('-').reverse().join('/') : ''}` : '',
      seats: b.seatNumbers || []
    })).sort((a, b) => b.id - a.id);
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
const cancelledCount = computed(() => bookings.value.filter(b => b.status === 'CANCELLED').length);

const updateStatus = async (id, newStatus) => {
  if (!confirm(`Xác nhận thay đổi trạng thái đơn #${id}?`)) return;
  try {
    await axios.post(`http://localhost:8080/api/admin/bookings/${id}/status`, { status: newStatus });
    fetchBookings();
    alert('Cập nhật thành công!');
  } catch (error) { alert('Lỗi hệ thống!'); }
};

const viewDetail = (booking) => {
  alert(`CHI TIẾT VÉ #${booking.id}\n------------------\nKhách: ${booking.customerName}\nSĐT: ${booking.customerPhone}\nTuyến: ${booking.route}\nGhế: ${booking.seats.join(', ')}\nTổng tiền: ${booking.totalPrice.toLocaleString()}đ`);
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
