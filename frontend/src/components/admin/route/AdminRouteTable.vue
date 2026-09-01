<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <div class="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
      <h3 class="text-sm font-bold text-slate-800 flex items-center gap-2">
         <span class="material-symbols-outlined text-[#075955] text-[20px]">map</span>
         Danh sách Tuyến đường
      </h3>
      <div class="flex items-center gap-2 px-3 py-1 bg-emerald-50 rounded-lg border border-emerald-100">
        <span class="text-[10px] font-bold text-emerald-700 uppercase">{{ routes.length }} mẫu</span>
      </div>
    </div>
    
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="w-14 px-3 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">STT</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Tuyến đường</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Điểm khởi hành</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Điểm kết thúc</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Thời gian</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Giá cơ bản</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Hiển thị</th>
            <th class="px-4 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Khứ hồi</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody v-if="routes.length > 0">
          <tr v-for="(route, idx) in routes" :key="idx" class="group border-b border-slate-50 hover:bg-[#075955]/[0.02] transition-all duration-200">
            <td class="w-14 px-3 py-5 text-center text-xs font-black text-slate-400">{{ idx + 1 }}</td>
            <td class="px-6 py-5">
              <div class="flex items-center gap-3">
                <div v-if="route.imageUrl" class="w-16 h-10 rounded-lg overflow-hidden shrink-0 border border-slate-200 shadow-sm">
                  <img :src="route.imageUrl" class="w-full h-full object-cover" />
                </div>
                <div v-else class="w-16 h-10 rounded-lg bg-slate-50 shrink-0 border border-slate-200 flex items-center justify-center shadow-sm">
                  <span class="material-symbols-outlined text-slate-300 text-xl">landscape</span>
                </div>
                <span class="font-bold text-sm text-slate-800">{{ route.name }}</span>
              </div>
            </td>
            <td class="px-6 py-5 text-sm text-slate-600 truncate max-w-[200px]">{{ route.departurePoint }}</td>
            <td class="px-6 py-5 text-sm text-slate-600 truncate max-w-[200px]">{{ route.arrivalPoint }}</td>
            <td class="px-6 py-5 text-center font-bold text-emerald-600 text-sm">{{ route.duration || '--' }}</td>
            <td class="px-6 py-5 text-center font-bold text-slate-700 text-sm">{{ route.basePrice ? route.basePrice.toLocaleString() + 'đ' : '--' }}</td>
            <td class="px-6 py-5 text-center">
              <button 
                @click="$emit('toggle-visibility', idx)" 
                class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none"
                :class="route.isVisible !== false ? 'bg-[#075955]' : 'bg-slate-200'"
              >
                <span 
                  class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                  :class="route.isVisible !== false ? 'translate-x-6' : 'translate-x-1'"
                />
              </button>
            </td>
            <td class="px-4 py-5 text-center">
              <button
                type="button"
                class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-emerald-500 focus-visible:ring-offset-2"
                :class="route.roundTripEnabled === true ? 'bg-emerald-600' : 'bg-slate-200'"
                :aria-pressed="route.roundTripEnabled === true"
                :title="route.roundTripEnabled === true ? 'Tắt khai thác khứ hồi' : 'Bật khai thác khứ hồi'"
                @click="$emit('toggle-round-trip', idx)"
              >
                <span
                  class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                  :class="route.roundTripEnabled === true ? 'translate-x-6' : 'translate-x-1'"
                />
              </button>
            </td>
            <td class="px-6 py-5 text-center">
              <div class="flex items-center justify-center gap-2">
                <button @click="$emit('manage-stops', idx)" class="w-8 h-8 flex items-center justify-center rounded-xl bg-amber-50 text-amber-600 hover:bg-amber-500 hover:text-white transition-all shadow-sm border border-slate-100" title="Quản lý điểm dừng">
                  <span class="material-symbols-outlined text-lg">add_location_alt</span>
                </button>
                <button @click="$emit('edit-route', idx)" class="w-8 h-8 flex items-center justify-center rounded-xl bg-blue-50 text-blue-500 hover:bg-blue-500 hover:text-white transition-all shadow-sm border border-slate-100" title="Chỉnh sửa">
                  <span class="material-symbols-outlined text-lg">edit</span>
                </button>
                <button @click="$emit('delete-route', idx)" class="w-8 h-8 flex items-center justify-center rounded-xl bg-rose-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all shadow-sm border border-slate-100" title="Xóa">
                  <span class="material-symbols-outlined text-lg">delete_sweep</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="9" class="py-20 text-center">
              <div class="flex flex-col items-center">
                <div class="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-4xl text-slate-300">map_off</span>
                </div>
                <h4 class="font-black text-slate-400 uppercase tracking-widest">Không có tuyến đường nào</h4>
                <p class="text-xs text-slate-400 mt-1">Hãy bấm "Tạo tuyến mẫu" để thêm tuyến mới</p>
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
  routes: {
    type: Array,
    required: true
  }
});

defineEmits(['toggle-visibility', 'toggle-round-trip', 'edit-route', 'delete-route', 'manage-stops']);
</script>
