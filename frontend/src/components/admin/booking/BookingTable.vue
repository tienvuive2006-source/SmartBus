<template>
  <div class="bg-white rounded-[32px] border border-slate-100 shadow-xl overflow-hidden">
    <!-- List Header -->
    <div class="p-6 bg-[#075955] text-white flex flex-col md:flex-row justify-between items-center gap-4">
      <div class="flex items-center gap-4">
        <div class="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center">
          <span class="material-symbols-outlined text-white">receipt_long</span>
        </div>
        <div>
          <h3 class="text-base font-black uppercase tracking-widest">Sổ lệnh đặt vé</h3>
          <p class="text-[10px] text-white/60 font-bold tracking-wider uppercase">Live updates from database</p>
        </div>
      </div>
      
      <!-- Filters inside table header for modern look -->
      <div class="flex items-center gap-3 w-full md:w-auto">
        <div class="relative flex-1 md:w-64">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-white/40 text-sm">search</span>
          <input 
            :value="searchQuery"
            @input="$emit('update:searchQuery', $event.target.value)"
            type="text" 
            placeholder="Tìm mã vé, SĐT..."
            class="w-full pl-9 pr-4 py-2 bg-white/10 rounded-xl border border-white/10 focus:bg-white focus:text-[#075955] outline-none transition-all text-xs font-bold placeholder:text-white/30"
          />
        </div>
        <select 
          :value="statusFilter"
          @change="$emit('update:statusFilter', $event.target.value)"
          class="bg-white/10 border border-white/10 rounded-xl px-4 py-2 outline-none font-bold text-xs text-white"
        >
          <option value="ALL" class="text-slate-800">Tất cả</option>
          <option value="PAID" class="text-slate-800">Đã thanh toán</option>
          <option value="PENDING" class="text-slate-800">Chờ xử lý</option>
          <option value="CANCELLED" class="text-slate-800">Đã hủy</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
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
          <tr v-for="booking in bookings" :key="booking.id" class="group hover:bg-[#075955]/[0.02] transition-all duration-200">
            <td class="px-6 py-5 border-b border-slate-50">
              <div class="font-bold text-slate-800 text-sm group-hover:text-[#075955] transition-colors">#{{ booking.id }}</div>
              <div class="text-[10px] font-semibold text-slate-400 uppercase mt-0.5 tracking-tighter">{{ formatDate(booking.createdAt) }}</div>
            </td>
            <td class="px-6 py-5 border-b border-slate-50">
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
            <td class="px-6 py-5 border-b border-slate-50">
              <div class="flex flex-col gap-1">
                <div class="font-bold text-slate-700 text-xs flex items-center gap-1">
                   <span class="material-symbols-outlined text-[14px] text-[#075955]">near_me</span>
                   {{ simplifyLocation(booking.route.split(' ➔ ')[0]) }} ➔ {{ simplifyLocation(booking.route.split(' ➔ ')[1]) }}
                </div>
                <div class="flex items-center gap-2">
                  <span class="px-2 py-0.5 bg-slate-100 text-slate-500 rounded text-[9px] font-bold uppercase border border-slate-200/50 tracking-tighter">
                    Số ghế: {{ booking.seats.join(', ') }}
                  </span>
                  <span class="text-[10px] font-semibold text-slate-400 tracking-tighter">{{ booking.departureTime }}</span>
                </div>
              </div>
            </td>
            <td class="px-6 py-5 border-b border-slate-50">
              <div class="font-bold text-[#075955] text-base tabular-nums">{{ booking.totalPrice.toLocaleString('vi-VN') }}<span class="text-[10px] ml-0.5">đ</span></div>
              <div class="text-[9px] text-slate-400 uppercase font-bold tracking-widest mt-0.5">{{ booking.paymentMethod }}</div>
            </td>
            <td class="px-6 py-5 text-center">
              <span :class="[
                'px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest border shadow-sm inline-block min-w-[120px]',
                statusStyles[booking.status]
              ]">
                {{ statusLabels[booking.status] }}
              </span>
            </td>
            <td class="px-6 py-5">
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
                  v-if="booking.status !== 'CANCELLED'" 
                  @click="$emit('update-status', booking.id, 'CANCELLED')" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all border border-slate-100 shadow-sm"
                  title="Hủy vé"
                >
                  <span class="material-symbols-outlined text-lg">block</span>
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
  </div>
</template>

<script setup>
defineProps({
  bookings: Array,
  searchQuery: String,
  statusFilter: String,
  statusLabels: Object,
  statusStyles: Object
});

defineEmits(['update:searchQuery', 'update:statusFilter', 'view', 'update-status']);

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
