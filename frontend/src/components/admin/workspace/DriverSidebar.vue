<template>
  <div class="w-full md:w-80 bg-white border-r border-slate-200 flex flex-col h-full shrink-0 z-20">

    <!-- Search & Filters -->
    <div class="p-5 border-b border-slate-100 shrink-0 bg-slate-50/50">
      <div class="relative w-full shadow-sm mb-3">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
        <input
          :value="searchQuery"
          @input="$emit('update:searchQuery', $event.target.value)"
          type="text"
          placeholder="Tìm tên tài xế..."
          class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm font-bold focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
        />
      </div>
      <div class="flex gap-2">
        <select
          :value="filterStatus"
          @change="$emit('update:filterStatus', $event.target.value)"
          class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold focus:border-primary outline-none shadow-sm"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="FREE">Đang rảnh</option>
          <option value="ASSIGNED">Đã có lịch</option>
          <option value="DRIVING">Đang chạy</option>
          <option value="ON_LEAVE">Nghỉ phép</option>
        </select>
      </div>
    </div>

    <!-- Driver List -->
    <div class="flex-1 overflow-y-auto p-3 space-y-2 bg-slate-50/30">

      <!-- Show All -->
      <div
        @click="$emit('select-driver', null)"
        class="p-3 rounded-xl cursor-pointer transition-all flex items-center gap-3 border"
        :class="selectedDriver === null ? 'bg-primary/10 border-primary/30 shadow-sm' : 'bg-white border-slate-100 hover:border-slate-300 hover:shadow-sm'"
      >
        <div class="w-10 h-10 rounded-full bg-slate-200 flex items-center justify-center text-slate-500 shrink-0">
          <span class="material-symbols-outlined">dashboard</span>
        </div>
        <div class="flex-1">
          <p class="font-bold text-sm text-slate-800">Hiển thị tất cả</p>
          <p class="text-xs text-slate-500">Xem lịch toàn bộ tài xế</p>
        </div>
      </div>

      <!-- Driver Items -->
      <div
        v-for="driver in filteredDrivers" :key="driver.id"
        @click="$emit('select-driver', driver)"
        class="p-3 rounded-xl cursor-pointer transition-all flex items-center gap-3 border group"
        :class="selectedDriver?.id === driver.id ? 'bg-primary/10 border-primary/30 shadow-sm' : 'bg-white border-slate-100 hover:border-slate-300 hover:shadow-sm'"
      >
        <div class="w-10 h-10 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold shrink-0 uppercase shadow-sm overflow-hidden border border-slate-200">
          <img v-if="driver.avatarUrl" :src="driver.avatarUrl" class="w-full h-full object-cover" />
          <span v-else>{{ driver.fullName.charAt(0) }}</span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm text-slate-800 truncate group-hover:text-primary transition-colors">{{ driver.fullName }}</p>
          <p class="text-[11px] text-slate-500 font-semibold mt-0.5">{{ driver.phone }}</p>
        </div>
        <div class="shrink-0">
          <span :class="['px-2 py-1 text-[10px] font-black uppercase rounded-md tracking-wider', driver.statusColor || 'bg-slate-100 text-slate-600']">
            {{ driver.computedStatusLabel || 'FREE' }}
          </span>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="p-8 flex justify-center">
        <div class="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- Inspector Section -->
      <div v-if="processedInspectors.length > 0" class="pt-2 pb-1">
        <p class="text-[10px] font-black uppercase tracking-widest text-slate-400 px-2 mb-2 flex items-center gap-1">
          <span class="material-symbols-outlined text-[13px]">support_agent</span>
          Lơ Xe ({{ processedInspectors.length }})
        </p>
        <div
          v-for="inspector in processedInspectors" :key="'insp-' + inspector.id"
          class="p-3 rounded-xl cursor-default flex items-center gap-3 border bg-white border-slate-100 mb-1"
        >
          <div class="w-10 h-10 rounded-full bg-violet-800 text-white flex items-center justify-center font-bold shrink-0 uppercase shadow-sm border border-violet-700">
            {{ inspector.fullName.charAt(0) }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-bold text-sm text-slate-800 truncate">{{ inspector.fullName }}</p>
            <p class="text-[11px] text-slate-500 font-semibold mt-0.5">{{ inspector.employeeCode || inspector.phone }}</p>
          </div>
          <span :class="['px-2 py-1 text-[10px] font-black uppercase rounded-md tracking-wider', inspector.statusColor]">
            {{ inspector.statusLabel }}
          </span>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
defineProps({
  searchQuery: { type: String, default: '' },
  filterStatus: { type: String, default: '' },
  selectedDriver: { type: Object, default: null },
  filteredDrivers: { type: Array, default: () => [] },
  processedInspectors: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
});

defineEmits(['update:searchQuery', 'update:filterStatus', 'select-driver', 'add-driver']);
</script>
