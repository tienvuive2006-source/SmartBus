<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl w-full max-w-6xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[90vh] animate-scale-up">
        <div class="p-6 bg-[#075955] text-white flex justify-between items-center shrink-0">
          <div class="flex items-center gap-3">
             <span class="material-symbols-outlined">{{ isEditMode ? 'edit_square' : 'add_circle' }}</span>
             <h3 class="text-sm font-black uppercase tracking-widest">{{ isEditMode ? 'Cập nhật lộ trình' : 'Tạo lộ trình mới' }}</h3>
          </div>
          <button @click="$emit('close')" class="hover:rotate-90 transition-transform bg-white/10 p-1.5 rounded-full flex items-center justify-center">
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
        
        <div class="flex-1 flex overflow-hidden">
          <!-- Left: Form Column -->
          <form @submit.prevent="$emit('submit')" class="w-1/2 p-8 space-y-6 overflow-y-auto border-r border-slate-100 bg-white">
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Hãng xe vận hành</label>
                <input v-model="form.companyName" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Dòng xe khai thác</label>
                <select v-model="form.busType" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all">
                  <option v-for="t in busTypes" :key="t.id" :value="t.name">{{ t.name }} ({{ t.seatCount }} Ghế)</option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-5 relative">
              <div class="space-y-1.5 relative">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex justify-between">
                  <span>Điểm khởi hành</span>
                  <span v-if="geocoding.departure" class="text-[#075955] animate-pulse text-[8px]">Đang lấy tọa độ...</span>
                </label>
                <div class="relative flex items-center">
                  <input 
                    v-model="form.departurePoint" 
                    @focus="$emit('from-focus')" 
                    @keyup.enter="$emit('geocode', form.departurePoint, 'departure')"
                    required placeholder="Ví dụ: Bến xe Đà Nẵng" 
                    class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-10 py-3.5 text-sm font-bold outline-none transition-all" 
                  />
                  <button type="button" @click="$emit('geocode', form.departurePoint, 'departure')" class="absolute right-3 text-slate-400 hover:text-[#075955] p-1">
                    <span class="material-symbols-outlined text-lg">my_location</span>
                  </button>
                </div>
                <!-- Suggestions handled by parent via slot or direct injection if complex -->
                <slot name="from-suggestions"></slot>
              </div>

              <!-- 🔄 Nút đảo chiều lộ trình khứ hồi thông minh ở giữa -->
              <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 z-[1002]">
                <button 
                  type="button"
                  @click="$emit('swap-route')" 
                  class="w-8 h-8 rounded-full bg-[#075955] text-white hover:bg-[#0a7a75] hover:rotate-180 active:scale-95 transition-all duration-300 flex items-center justify-center cursor-pointer shadow-md border border-white"
                  title="Đảo chiều lộ trình (Khứ hồi)"
                >
                  <span class="material-symbols-outlined text-sm">sync_alt</span>
                </button>
              </div>
  
              <div class="space-y-1.5 relative">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex justify-between">
                  <span>Điểm kết thúc</span>
                  <span v-if="geocoding.arrival" class="text-[#075955] animate-pulse text-[8px]">Đang lấy tọa độ...</span>
                </label>
                <div class="relative flex items-center">
                  <input 
                    v-model="form.arrivalPoint" 
                    @focus="$emit('to-focus')" 
                    @keyup.enter="$emit('geocode', form.arrivalPoint, 'arrival')"
                    required placeholder="Ví dụ: Bến xe Miền Tây" 
                    class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-10 py-3.5 text-sm font-bold outline-none transition-all" 
                  />
                  <button type="button" @click="$emit('geocode', form.arrivalPoint, 'arrival')" class="absolute right-3 text-slate-400 hover:text-[#075955] p-1">
                    <span class="material-symbols-outlined text-lg">my_location</span>
                  </button>
                </div>
                <slot name="to-suggestions"></slot>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Ngày vận hành</label>
                <input v-model="form.departureDate" type="date" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Giá vé niêm yết (VNĐ)</label>
                <input v-model.number="form.price" type="number" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all font-mono" />
              </div>
            </div>

            <div class="grid grid-cols-3 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Giờ đi</label>
                <input v-model="form.departureTime" placeholder="08:00" class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Giờ đến</label>
                <input v-model="form.arrivalTime" placeholder="12:00" class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Số ghế</label>
                <input v-model.number="form.availableSeats" type="number" class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" />
              </div>
            </div>



            <div class="pt-8 flex justify-end gap-3 shrink-0">
              <button type="button" @click="$emit('close')" class="px-8 py-3 text-xs font-black uppercase text-slate-400 hover:text-slate-900 transition-colors">Hủy bỏ</button>
              <button @click="$emit('submit')" type="button" class="bg-[#075955] text-white px-12 py-3.5 rounded-xl text-xs font-black uppercase tracking-widest shadow-xl hover:shadow-[#075955]/20 active:scale-95 transition-all">Lưu & Đăng tải</button>
            </div>
          </form>

          <!-- Right: Map Preview Column -->
          <div class="w-1/2 bg-slate-50 relative flex flex-col border-l border-slate-100">
              <div class="absolute top-5 left-5 right-5 z-[1000] bg-white shadow-xl rounded-2xl p-4 border border-slate-200">
                 <div class="flex items-center justify-between mb-2">
                    <h4 class="text-[11px] font-black text-[#075955] uppercase flex items-center gap-2">
                       <span class="material-symbols-outlined text-[#075955] text-base">explore</span> Bản đồ lộ trình
                    </h4>
                    <span class="text-[9px] font-black text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-100 tracking-tighter">GPS ACTIVE</span>
                 </div>
                 <div v-if="form.departureLat && form.arrivalLat" class="text-[10px] font-bold text-slate-500 leading-tight">
                     <div class="flex items-center gap-1 text-[#075955] mb-2">
                       <span class="material-symbols-outlined text-[12px]">check_circle</span>
                       Tọa độ lộ trình đã xác nhận chuẩn xác
                     </div>
                     <div v-if="form.duration" class="bg-slate-50 border border-slate-100 p-2.5 rounded-xl flex gap-4 text-xs">
                       <div class="flex items-center gap-1 text-slate-700">
                         <span class="material-symbols-outlined text-slate-400 text-sm">route</span>
                         <span>Lộ trình: <strong class="text-[#075955] font-black">{{ form.duration }}</strong></span>
                       </div>
                     </div>
                     <div class="mt-2 text-[9px] italic text-slate-400">Mẹo: Click lên bản đồ để tinh chỉnh vị trí nếu cần</div>
                  </div>
                 <div v-else class="text-[10px] font-bold text-rose-500 flex flex-col gap-1 animate-pulse">
                    <div class="flex items-center gap-1">
                      <span class="material-symbols-outlined text-[12px]">warning</span> Chờ xác định tọa độ...
                    </div>
                    <div class="text-[9px] text-slate-400 font-normal">Hãy nhập địa chỉ hoặc click trực tiếp lên bản đồ</div>
                 </div>
              </div>

             <div id="admin-route-map" class="flex-1 w-full h-full"></div>
             
             <div v-if="mapLoading" class="absolute inset-0 bg-slate-900/10 backdrop-blur-[2px] flex flex-col items-center justify-center z-[1001]">
                <div class="w-10 h-10 border-4 border-[#075955] border-t-transparent rounded-full animate-spin"></div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  isOpen: Boolean,
  isEditMode: Boolean,
  form: Object,
  busTypes: Array,
  geocoding: Object,
  mapLoading: Boolean
});

defineEmits(['close', 'submit', 'geocode', 'from-focus', 'to-focus', 'upload-click', 'swap-route']);
</script>

<style scoped>
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.98) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
