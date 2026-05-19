<template>
  <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
    <!-- List Header -->
    <div class="p-6 bg-[#075955] text-white flex justify-between items-center">
      <div class="flex items-center gap-4">
        <div class="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center">
          <span class="material-symbols-outlined text-white">list_alt</span>
        </div>
        <div>
          <h3 class="text-base font-black uppercase tracking-widest">Danh sách vận hành</h3>
          <p class="text-[10px] text-white/60 font-bold tracking-wider">Tổng cộng {{ trips.length }} chuyến xe đang khai thác</p>
        </div>
      </div>
      <div class="flex items-center gap-2 px-4 py-1.5 bg-white/10 rounded-full border border-white/10 backdrop-blur-sm">
        <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
        <span class="text-[9px] font-black uppercase tracking-widest">Live Sync</span>
      </div>
    </div>

    <!-- Table Content -->
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">ID</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Lộ trình & Nhà xe</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Dòng xe</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Thời gian</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-right">Giá vé</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Trạng thái</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody v-if="trips.length > 0">
          <tr 
            v-for="trip in trips" 
            :key="trip.id" 
            class="group border-b border-slate-50 hover:bg-[#075955]/[0.02] transition-all duration-200"
          >
            <td class="px-6 py-5 font-mono text-[11px] font-black text-slate-300 group-hover:text-[#075955]">#{{ trip.id }}</td>
            <td class="px-6 py-5">
              <div class="flex items-start gap-3">
                <div class="flex flex-col gap-1 relative pl-4 mt-1">
                  <div class="absolute left-1 top-2 bottom-2 w-[2px] bg-slate-200"></div>
                  <div class="flex items-center gap-2">
                    <div class="w-2 h-2 rounded-full bg-[#075955] shrink-0 -ml-[17px] z-10 border-2 border-white shadow-sm"></div>
                    <span class="font-black text-slate-800 text-sm leading-none">{{ simplifyLocation(trip.departurePoint) }}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <div class="w-2 h-2 rounded-full bg-slate-400 shrink-0 -ml-[17px] z-10 border-2 border-white shadow-sm"></div>
                    <span class="font-bold text-slate-500 text-sm leading-none">{{ simplifyLocation(trip.arrivalPoint) }}</span>
                  </div>
                </div>
                <div class="ml-auto shrink-0">
                   <span class="text-[9px] font-black text-[#075955] bg-[#075955]/5 px-2.5 py-1 rounded border border-[#075955]/10 uppercase tracking-tighter whitespace-nowrap">{{ trip.companyName }}</span>
                </div>
              </div>
            </td>
            <td class="px-6 py-5">
              <div class="flex flex-col gap-1">
                <span class="text-[10px] font-black text-slate-700 uppercase">{{ trip.busType }}</span>
                <div class="flex gap-1">
                   <span v-for="i in 3" :key="i" class="w-1.5 h-1.5 rounded-full bg-emerald-400/30"></span>
                </div>
              </div>
            </td>
            <td class="px-6 py-5">
              <div class="flex flex-col leading-tight">
                <span class="text-sm font-black text-slate-800">{{ trip.departureTime }}</span>
                <span class="text-[10px] text-slate-400 font-bold uppercase mt-0.5 tracking-tighter">{{ formatDate(trip.departureDate) }}</span>
              </div>
            </td>
            <td class="px-6 py-5 text-right font-black text-[#075955] text-base tabular-nums">
              {{ trip.price.toLocaleString() }}<span class="text-[10px] ml-0.5">đ</span>
            </td>
            <td class="px-6 py-5 text-center">
              <div 
                :class="[
                  'inline-flex px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest border shadow-sm',
                  trip.availableSeats <= 0 
                    ? 'bg-rose-50 border-rose-100 text-rose-600' 
                    : trip.availableSeats < 10 
                      ? 'bg-amber-50 border-amber-100 text-amber-600'
                      : 'bg-emerald-50 border-emerald-100 text-emerald-600'
                ]"
              >
                {{ trip.availableSeats }} chỗ trống
              </div>
            </td>
            <td class="px-6 py-5">
              <div class="flex items-center justify-center gap-2">
                <button 
                  @click="$emit('edit', trip)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-slate-600 hover:bg-[#075955] hover:text-white transition-all shadow-sm border border-slate-100"
                >
                  <span class="material-symbols-outlined text-lg">edit_note</span>
                </button>
                <button 
                  @click="$emit('delete', trip.id)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all shadow-sm border border-slate-100"
                >
                  <span class="material-symbols-outlined text-lg">delete_sweep</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="7" class="py-20 text-center">
              <div class="flex flex-col items-center">
                <div class="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-4xl text-slate-300">search_off</span>
                </div>
                <h4 class="font-black text-slate-400 uppercase tracking-widest">Không có dữ liệu lộ trình</h4>
                <p class="text-xs text-slate-400 mt-1">Hãy bấm "Tạo lộ trình mới" để bắt đầu vận hành</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
defineProps({
  trips: Array
});

defineEmits(['edit', 'delete']);

const simplifyLocation = (loc) => {
  if (!loc) return '';
  const parts = loc.split(',');
  return parts[parts.length - 1].trim().replace(/\b(Thành phố|TP|Tỉnh)\b/gi, '').trim();
};

const formatDate = (d) => {
  if (!d) return '';
  return d.split('T')[0].split('-').reverse().join('/');
};
</script>
