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
            
            <div class="space-y-1.5 mb-6">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex items-center justify-between">
                 <span>
                   Chọn Tuyến Đường Cố Định <span class="text-rose-500">*</span>
                   <span v-if="isEditMode && form.originalAvailableSeats < form.originalTotalSeats" class="text-rose-500 lowercase normal-case italic ml-1">(Đã có khách đặt, không thể đổi)</span>
                 </span>
                 <span v-if="!savedRoutes || savedRoutes.length === 0" class="text-rose-400 text-[9px] italic">Chưa có tuyến mẫu nào</span>
              </label>
              <select 
                :value="form.departurePoint && form.arrivalPoint ? `${form.departurePoint.split(',')[0]} ➔ ${form.arrivalPoint.split(',')[0]}` : ''"
                @change="(e) => {
                  const selectedRoute = savedRoutes.find(r => r.name === e.target.value);
                  if(selectedRoute) $emit('apply-template', selectedRoute);
                }"
                required
                :disabled="isEditMode && form.originalAvailableSeats < form.originalTotalSeats"
                class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3.5 text-sm font-bold outline-none transition-all cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
              >
                <option value="" disabled>-- Vui lòng chọn một tuyến đường --</option>
                <option v-for="route in savedRoutes" :key="route.name" :value="route.name">
                  {{ route.name }}
                </option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Hãng xe vận hành</label>
                <input v-model="form.companyName" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">
                  Dòng xe khai thác
                  <span v-if="isEditMode && form.originalAvailableSeats < form.originalTotalSeats" class="text-rose-500 lowercase normal-case italic ml-1">(Đã có khách đặt, không thể đổi)</span>
                </label>
                <select 
                  v-model="form.busType" 
                  required 
                  @change="(e) => {
                    if (form.assignedLicensePlate) {
                      const assignedBus = buses?.find(b => b.licensePlate === form.assignedLicensePlate);
                      if (assignedBus && assignedBus.busType !== e.target.value) {
                        form.assignedLicensePlate = '';
                      }
                    }
                  }"
                  :disabled="isEditMode && form.originalAvailableSeats < form.originalTotalSeats"
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  <option v-for="t in busTypes" :key="t.id" :value="t.name">{{ t.name }} ({{ t.seatCount }} Ghế)</option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Phân công Xe & Tài xế (Tùy chọn)</label>
                <select 
                  v-model="form.assignedLicensePlate" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all"
                  @change="(e) => {
                    const bus = buses?.find(b => b.licensePlate === e.target.value);
                    if(bus && (!isEditMode || form.availableSeats === form.totalSeats)) { form.busType = bus.busType; }
                  }"
                >
                  <option value="">-- Chưa phân công --</option>
                  <option v-for="bus in (form.busType ? buses.filter(b => b.busType === form.busType) : buses)" :key="bus.id" :value="bus.licensePlate">
                    {{ bus.licensePlate }} - Tài xế: {{ bus.driverName || 'N/A' }}
                  </option>
                </select>
              </div>

              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Phân công Lơ xe (Tùy chọn)</label>
                <select 
                  v-model="form.inspectorId" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all"
                >
                  <option value="">-- Chưa phân công --</option>
                  <option v-for="inspector in inspectors" :key="inspector.id" :value="inspector.id">
                    {{ inspector.fullName }}
                  </option>
                </select>
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
                <input 
                  v-model="form.departureTime" 
                  type="text"
                  placeholder="15:00"
                  @change="() => {
                    let timeVal = form.departureTime?.trim() || '';
                    if (/^\d{1,2}$/.test(timeVal)) timeVal = `${timeVal.padStart(2, '0')}:00`;
                    else if (/^\d{3,4}$/.test(timeVal)) timeVal = `${timeVal.length === 3 ? '0' + timeVal[0] : timeVal.slice(0,2)}:${timeVal.slice(-2)}`;
                    else if (/^\d{1,2}:\d{1,2}$/.test(timeVal)) {
                       const parts = timeVal.split(':');
                       timeVal = `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
                    }
                    form.departureTime = timeVal;

                    if (form.departureTime && form.duration) {
                      const match = form.duration.match(/(\d+)h(?:\s*(\d+)m)?/);
                      if (match) {
                        const durationH = parseInt(match[1]) || 0;
                        const durationM = parseInt(match[2]) || 0;
                        const [depH, depM] = form.departureTime.split(':').map(Number);
                        if (!isNaN(depH) && !isNaN(depM)) {
                          let arrH = depH + durationH;
                          let arrM = depM + durationM;
                          if (arrM >= 60) {
                            arrH += Math.floor(arrM / 60);
                            arrM = arrM % 60;
                          }
                          arrH = arrH % 24;
                          form.arrivalTime = `${String(arrH).padStart(2, '0')}:${String(arrM).padStart(2, '0')}`;
                        }
                      }
                    }
                  }"
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" 
                />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Giờ đến</label>
                <input v-model="form.arrivalTime" type="text" placeholder="19:00" class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Số ghế</label>
                <input 
                  v-model.number="form.availableSeats" 
                  type="number" 
                  :disabled="isEditMode && form.originalAvailableSeats < form.originalTotalSeats"
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center disabled:opacity-60 disabled:cursor-not-allowed" 
                />
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
  buses: Array,
  inspectors: Array,
  geocoding: Object,
  mapLoading: Boolean,
  savedRoutes: Array
});

defineEmits(['close', 'submit', 'geocode', 'from-focus', 'to-focus', 'upload-click', 'swap-route', 'save-template', 'apply-template', 'delete-template']);
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
