<template>
  <div id="searchSection" class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 mb-4 mt-10">
    <!-- Section Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-200 pb-4 mb-6">
      <div class="flex flex-col sm:flex-row sm:items-center gap-3">
        <h3 class="text-xl font-black text-slate-800 tracking-tight">Các tuyến đường chính</h3>
        <span v-if="popularRoutesCount > 0" class="bg-[#075955]/10 text-[#075955] text-xs font-bold px-3 py-1 rounded-full border border-[#075955]/15 flex items-center gap-1.5 w-fit">
          <span class="material-symbols-outlined text-sm">directions_bus</span>
          Hiện có {{ popularRoutesCount }} tuyến đường
        </span>
      </div>
      <p class="text-slate-500 text-sm font-semibold italic md:text-right hidden sm:block">
        Nền tảng đặt xe uy tín - giá gốc 100% từ nhà xe
      </p>
    </div>

    <!-- Super Optimized Filter Bar -->
    <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-2 text-slate-700 font-bold text-xs uppercase tracking-wider">
          <span class="material-symbols-outlined text-base">tune</span>
          Bộ lọc chuyến đi
        </div>
        <!-- Reset Filters Button -->
        <button 
          v-if="filterDate || filterFrom || filterTo || filterCompany"
          @click="$emit('clear-filters')" 
          class="text-xs font-bold text-red-500 hover:text-red-600 flex items-center gap-1 bg-transparent border-none outline-none cursor-pointer hover:underline transition-colors"
        >
          <span class="material-symbols-outlined text-sm">filter_alt_off</span>
          Xóa bộ lọc
        </button>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <!-- Filter Date -->
        <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
          <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">calendar_month</span>
          <div class="flex-1 text-left">
            <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Ngày đi</label>
            <input type="date" :value="filterDate" @input="$emit('update:filterDate', $event.target.value)" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0" />
          </div>
        </div>

        <!-- Filter From -->
        <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
          <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">location_on</span>
          <div class="flex-1 text-left">
            <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Nơi đi</label>
            <select :value="filterFrom" @change="$emit('update:filterFrom', $event.target.value)" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0 appearance-none">
              <option value="">Tất cả điểm đi</option>
              <option v-for="loc in allDeparturePoints" :key="loc" :value="loc">{{ simplifyLocation(loc) }}</option>
            </select>
          </div>
          <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
        </div>

        <!-- Filter To -->
        <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
          <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">pin_drop</span>
          <div class="flex-1 text-left">
            <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Nơi đến</label>
            <select :value="filterTo" @change="$emit('update:filterTo', $event.target.value)" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0 appearance-none">
              <option value="">Tất cả điểm đến</option>
              <option v-for="loc in allArrivalPoints" :key="loc" :value="loc">{{ simplifyLocation(loc) }}</option>
            </select>
          </div>
          <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
        </div>

        <!-- Filter Company -->
        <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
          <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">directions_bus</span>
          <div class="flex-1 text-left">
            <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Hãng xe</label>
            <select :value="filterCompany" @change="$emit('update:filterCompany', $event.target.value)" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0 appearance-none">
              <option value="">Tất cả hãng xe</option>
              <option v-for="company in uniqueCompanies" :key="company.name" :value="company.name">{{ company.name }}</option>
            </select>
          </div>
          <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  popularRoutesCount: Number,
  filterDate: String,
  filterFrom: String,
  filterTo: String,
  filterCompany: String,
  allDeparturePoints: Array,
  allArrivalPoints: Array,
  uniqueCompanies: Array
});

defineEmits(['update:filterDate', 'update:filterFrom', 'update:filterTo', 'update:filterCompany', 'clear-filters']);

const simplifyLocation = (loc) => {
  if (!loc) return '';
  const parts = loc.split(',');
  let s = parts[parts.length - 1].trim();
  s = s.replace(/\b(Thành phố|TP|Tỉnh|Hà Nội|Hồ Chí Minh|Đà Nẵng|Cần Thơ|Hải Phòng)\b/gi, (match) => {
    if (match.toLowerCase() === 'thành phố' || match.toLowerCase() === 'tp' || match.toLowerCase() === 'tỉnh') return '';
    return match;
  }).trim();
  if (!s) s = parts[0].replace(/\b(Bến xe|Phường|Quận|Huyện|Xã|TT)\b/gi, '').trim();
  return s;
};
</script>
