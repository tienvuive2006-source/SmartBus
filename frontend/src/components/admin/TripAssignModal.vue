<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="close"></div>
      
      <!-- Modal Content -->
      <div class="relative w-full max-w-6xl bg-white rounded-[2rem] shadow-2xl overflow-hidden flex flex-col max-h-[90vh] animate-fade-in-up">
        
        <!-- Header -->
        <div class="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white shrink-0">
          <div>
            <h3 class="text-title-lg font-black text-slate-800 flex items-center gap-2">
              <span class="material-symbols-outlined text-primary text-[28px]">smart_toy</span>
              Phân Công Thông Minh
            </h3>
            <p class="text-body-sm text-slate-500 mt-1">Hệ thống tự động phân tích Không gian & Thời gian: Khớp bến nối chuyến nghỉ 1 tiếng, Sai bến (phải di chuyển rỗng) ép nghỉ 12 tiếng.</p>
          </div>
          <button @click="close" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-rose-100 text-slate-600 hover:text-rose-600 flex items-center justify-center transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <!-- Trip Summary Strip -->
        <div class="bg-primary/5 border-b border-primary/10 px-6 py-4 flex flex-col sm:flex-row gap-4 sm:items-center justify-between shrink-0">
           <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-white shadow-sm border border-primary/20 flex flex-col items-center justify-center text-primary">
                 <span class="text-[10px] font-black uppercase tracking-widest">{{ trip?.departureDate?.split('-')[2] }}/{{ trip?.departureDate?.split('-')[1] }}</span>
                 <span class="text-sm font-black leading-none">{{ trip?.departureTime }}</span>
              </div>
              <div>
                 <h4 class="font-bold text-slate-800">{{ trip?.departurePoint?.split(',')[0] }} <span class="text-slate-400 mx-1">➔</span> {{ trip?.arrivalPoint?.split(',')[0] }}</h4>
                 <p class="text-xs text-slate-500 font-medium">Giờ đến dự kiến: <strong class="text-slate-700">{{ trip?.arrivalTime || 'Chưa rõ' }}</strong></p>
              </div>
           </div>
        </div>

        <!-- Body: 2 Columns -->
        <div class="flex-1 overflow-hidden flex flex-col md:flex-row bg-slate-50/50">
          
          <!-- Column 1: Chọn Tài Xế -->
          <div class="flex-1 flex flex-col min-w-0">
            <div class="p-4 border-b border-slate-200 bg-white shrink-0">
              <h4 class="font-black text-sm text-slate-800 uppercase tracking-widest flex items-center gap-2 mb-3">
                 <span class="material-symbols-outlined text-amber-500">badge</span>
                 Chọn Tài Xế
              </h4>
              <div class="grid grid-cols-2 gap-2 mb-3 p-1 rounded-xl bg-slate-100">
                <button type="button" @click="activeDriverRole = 'PRIMARY'" class="rounded-lg px-3 py-2 text-xs font-black transition-colors" :class="activeDriverRole === 'PRIMARY' ? 'bg-amber-500 text-white shadow-sm' : 'text-slate-500'">
                  Tài xế chính
                  <span v-if="selectedDriver" class="block mt-0.5 truncate text-[10px] opacity-90">{{ selectedDriver.fullName }}</span>
                </button>
                <button type="button" @click="activeDriverRole = 'SECONDARY'" class="rounded-lg px-3 py-2 text-xs font-black transition-colors" :class="activeDriverRole === 'SECONDARY' ? 'bg-sky-600 text-white shadow-sm' : 'text-slate-500'">
                  Tài xế phụ
                  <span class="block mt-0.5 truncate text-[10px] opacity-90">{{ selectedSecondaryDriver?.fullName || 'Không bố trí' }}</span>
                </button>
              </div>
              <button v-if="selectedSecondaryDriver" type="button" @click="selectedSecondaryDriver = null" class="mb-3 w-full rounded-lg border border-sky-200 bg-sky-50 px-3 py-1.5 text-[11px] font-bold text-sky-700 hover:bg-sky-100">
                Bỏ tài xế phụ khỏi chuyến
              </button>
              <div class="relative shadow-sm">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
                <input v-model="driverSearch" type="text" placeholder="Tìm tên tài xế..." class="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none" />
              </div>
            </div>
            
            <div class="flex-1 overflow-y-auto p-4 space-y-2">
               <div v-if="filteredDrivers.length === 0" class="text-center text-sm text-slate-500 py-8">Không có tài xế nào.</div>
               
               <div 
                  v-for="driver in filteredDrivers" :key="driver.id"
                  @click="!driver.conflict && selectDriverForRole(driver)"
                  class="p-3 rounded-xl border transition-all flex items-center justify-between"
                  :class="[
                     driver.conflict ? 'bg-slate-50 border-slate-100 opacity-60 cursor-not-allowed' : 'bg-white border-slate-200 cursor-pointer hover:border-primary/50',
                     activeSelectedDriver?.id === driver.id ? 'ring-2 ring-primary border-primary bg-primary/5' : ''
                  ]"
               >
                  <div class="flex items-center gap-3">
                     <div class="w-10 h-10 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold text-sm shrink-0 overflow-hidden relative border border-slate-200">
                        <img v-if="driver.avatarUrl" :src="driver.avatarUrl" class="w-full h-full object-cover" />
                        <span v-else>{{ driver.fullName.charAt(0) }}</span>
                     </div>
                     <div>
                        <p class="font-bold text-sm text-slate-800">{{ driver.fullName }}</p>
                        <p class="text-xs text-slate-500">{{ driver.phone }}</p>
                     </div>
                  </div>
                  
                  <div class="max-w-[48%] shrink-0 text-right">
                     <span v-if="driver.isCurrentAssignee" class="text-[10px] font-black px-2 py-0.5 rounded uppercase" :class="driver.leaveWarning ? 'text-rose-600 bg-rose-50' : 'text-blue-600 bg-blue-50'">
                        Đang gán {{ driver.leaveWarning ? `(${driver.leaveWarning})` : '' }}
                     </span>
                     <span v-else-if="driver.conflict" class="text-[10px] font-black text-rose-600 bg-rose-50 px-2 py-0.5 rounded uppercase">
                        {{ driver.conflictReason }}
                     </span>
                     <div v-else class="flex flex-col items-end gap-1">
                       <span class="inline-flex rounded-md bg-emerald-50 px-2 py-0.5 text-[10px] font-black uppercase tracking-wider text-emerald-600">
                         Rảnh
                       </span>
                       <span v-if="driver.lastKnownLocation" class="max-w-full text-wrap text-[10px] font-semibold leading-tight text-slate-500" :title="(driver.isLocationProjected ? 'Dự kiến tại ' : 'Đang ở ') + driver.lastKnownLocation">
                         {{ driver.isLocationProjected ? 'Dự kiến tại' : 'Đang ở' }} {{ driver.lastKnownLocation }}
                       </span>
                     </div>
                     
                     <div v-if="activeSelectedDriver?.id === driver.id" class="mt-1 flex justify-end">
                        <span class="material-symbols-outlined text-primary text-[18px]">check_circle</span>
                     </div>
                  </div>
               </div>
            </div>
          </div>
          
          <!-- Column 2: Chọn Phương Tiện -->
          <div class="flex-1 flex flex-col min-w-0 border-l border-slate-200">
             <div class="p-4 border-b border-slate-200 bg-white shrink-0">
              <h4 class="font-black text-sm text-slate-800 uppercase tracking-widest flex items-center gap-2 mb-3">
                 <span class="material-symbols-outlined text-emerald-500">directions_bus</span>
                 Chọn Phương Tiện
              </h4>
              <div class="relative shadow-sm">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
                <input v-model="busSearch" type="text" placeholder="Tìm biển số xe..." class="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none" />
              </div>
            </div>
            
            <div class="flex-1 overflow-y-auto p-4 space-y-2">
               <div v-if="filteredBuses.length === 0" class="text-center text-sm text-slate-500 py-8">Không có phương tiện nào phù hợp với dòng xe của lộ trình.</div>
               <div 
                  v-for="bus in filteredBuses" :key="bus.id"
                  @click="!bus.conflict && (selectedBus = bus)"
                  class="p-3 rounded-xl border transition-all flex items-center justify-between"
                  :class="[
                     bus.conflict ? 'bg-slate-50 border-slate-100 opacity-60 cursor-not-allowed' : 'bg-white border-slate-200 cursor-pointer hover:border-primary/50',
                     selectedBus?.id === bus.id ? 'ring-2 ring-primary border-primary bg-primary/5' : ''
                  ]"
               >
                   <div class="flex items-center gap-3">
                     <div class="w-10 h-10 rounded-full bg-slate-100 text-slate-500 flex items-center justify-center font-bold text-sm shrink-0 border border-slate-200">
                        <span class="material-symbols-outlined">directions_bus</span>
                     </div>
                     <div>
                        <p class="font-bold text-sm text-slate-800">{{ bus.licensePlate }}</p>
                        <p class="text-[10px] text-slate-500 font-semibold">{{ bus.busType }}</p>
                     </div>
                  </div>
                  <div class="text-right">
                     <span v-if="bus.isCurrentAssignee" class="text-[10px] font-black text-blue-600 bg-blue-50 px-2 py-0.5 rounded uppercase block truncate max-w-[120px]">Đang gán</span>
                     <span v-else-if="bus.conflict" class="text-[10px] font-black text-rose-600 bg-rose-50 px-2 py-0.5 rounded uppercase block truncate max-w-[120px]" :title="bus.conflictReason">
                        {{ bus.conflictReason }}
                     </span>
                     <span v-else class="text-[10px] font-black text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded uppercase">Rảnh</span>
                     
                     <div v-if="selectedBus?.id === bus.id" class="mt-1 flex justify-end">
                        <span class="material-symbols-outlined text-primary text-[18px]">check_circle</span>
                     </div>
                  </div>
               </div>
            </div>
          </div>

          <!-- Column 3: Chọn Lơ Xe -->
          <div class="flex-1 flex flex-col min-w-0 border-l border-slate-200">
            <div class="p-4 border-b border-slate-200 bg-white shrink-0">
              <h4 class="font-black text-sm text-slate-800 uppercase tracking-widest flex items-center gap-2 mb-3">
                <span class="material-symbols-outlined text-violet-500">support_agent</span>
                Chọn Lơ Xe
              </h4>
              <div class="relative shadow-sm">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
                <input v-model="inspectorSearch" type="text" placeholder="Tìm tên lơ xe..." class="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none" />
              </div>
            </div>

            <div class="flex-1 overflow-y-auto p-4 space-y-2">
              <!-- Tùy chọn: không gán lơ xe -->
              <div
                @click="selectedInspector = null"
                class="p-3 rounded-xl border transition-all flex items-center justify-between cursor-pointer"
                :class="selectedInspector === null ? 'ring-2 ring-primary border-primary bg-primary/5' : 'bg-white border-slate-200 hover:border-primary/50'"
              >
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center shrink-0 border border-slate-200">
                    <span class="material-symbols-outlined text-[18px]">do_not_disturb</span>
                  </div>
                  <p class="font-bold text-sm text-slate-500">Không có lơ xe</p>
                </div>
                <div v-if="selectedInspector === null">
                  <span class="material-symbols-outlined text-primary text-[18px]">check_circle</span>
                </div>
              </div>

              <div v-if="filteredInspectors.length === 0" class="text-center text-sm text-slate-500 py-4">Không tìm thấy lơ xe.</div>

              <div
                v-for="inspector in filteredInspectors" :key="inspector.id"
                @click="!inspector.conflict && (selectedInspector = inspector)"
                class="p-3 rounded-xl border transition-all flex items-center justify-between"
                :class="[
                   inspector.conflict ? 'bg-slate-50 border-slate-100 opacity-60 cursor-not-allowed' : 'bg-white border-slate-200 cursor-pointer hover:border-primary/50',
                   selectedInspector?.id === inspector.id ? 'ring-2 ring-primary border-primary bg-primary/5' : ''
                ]"
              >
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-violet-800 text-white flex items-center justify-center font-bold text-sm shrink-0 border border-violet-700">
                    {{ inspector.fullName.charAt(0) }}
                  </div>
                  <div>
                    <p class="font-bold text-sm text-slate-800">{{ inspector.fullName }}</p>
                    <p class="text-xs text-slate-500">{{ inspector.phone }}</p>
                    <p v-if="inspector.employeeCode" class="text-[10px] text-violet-600 font-semibold">{{ inspector.employeeCode }}</p>
                  </div>
                </div>
                <div class="text-right">
                  <span v-if="inspector.isCurrentAssignee" class="text-[10px] font-black px-2 py-0.5 rounded uppercase" :class="inspector.leaveWarning ? 'text-rose-600 bg-rose-50' : 'text-blue-600 bg-blue-50'">
                     Đang gán {{ inspector.leaveWarning ? `(${inspector.leaveWarning})` : '' }}
                  </span>
                  <span v-else-if="inspector.conflict" class="text-[10px] font-black text-rose-600 bg-rose-50 px-2 py-0.5 rounded uppercase">
                    {{ inspector.conflictReason }}
                  </span>
                  <span v-else class="text-[10px] font-black text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded uppercase">Rảnh</span>
                  
                  <div v-if="selectedInspector?.id === inspector.id" class="mt-1 flex justify-end">
                    <span class="material-symbols-outlined text-primary text-[18px]">check_circle</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-slate-100 bg-white flex justify-end gap-3 shrink-0">
          <button @click="close" class="px-5 py-2.5 rounded-xl text-slate-600 font-bold hover:bg-slate-100 transition-colors">
            Hủy bỏ
          </button>
          <button 
            @click="submit" 
            :disabled="!selectedDriver || !selectedBus || submitting"
            class="bg-slate-900 hover:bg-slate-800 text-white px-8 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
          >
            <span v-if="submitting" class="material-symbols-outlined animate-spin text-[18px]">sync</span>
            <span v-else class="material-symbols-outlined text-[18px]">save</span>
            Lưu Phân Công
          </button>
        </div>

      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  trip: Object,
  allDrivers: Array,
  allBuses: Array,
  allTrips: Array,
  allLeaves: Array,
  allInspectors: Array // Danh sách lơ xe
});

const emit = defineEmits(['close', 'submit']);

const driverSearch = ref('');
const busSearch = ref('');
const inspectorSearch = ref('');
const selectedDriver = ref(null);
const selectedSecondaryDriver = ref(null);
const activeDriverRole = ref('PRIMARY');
const selectedBus = ref(null);
const selectedInspector = ref(null);
const submitting = ref(false);

// Reset state when opened
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    driverSearch.value = '';
    busSearch.value = '';
    inspectorSearch.value = '';
    selectedDriver.value = null;
    selectedSecondaryDriver.value = null;
    activeDriverRole.value = 'PRIMARY';
    selectedBus.value = null;
    selectedInspector.value = null;
    submitting.value = false;
    
    // Nếu trip đã có gán, pre-select
    if (props.trip?.assignedDriverUsername) {
      selectedDriver.value = props.allDrivers?.find(d => d.phone === props.trip.assignedDriverUsername) || null;
    }
    if (props.trip?.secondaryDriverUsername) {
      selectedSecondaryDriver.value = props.allDrivers?.find(d => d.phone === props.trip.secondaryDriverUsername) || null;
    }
    if (props.trip?.assignedLicensePlate) {
      selectedBus.value = props.allBuses?.find(b => b.licensePlate === props.trip.assignedLicensePlate) || null;
    }
    if (props.trip?.inspector?.id) {
      selectedInspector.value = props.allInspectors?.find(i => i.id === props.trip.inspector.id) || null;
    }
  }
});

const close = () => emit('close');

const submit = () => {
  if (!selectedDriver.value || !selectedBus.value) return;
  submitting.value = true;
  emit('submit', {
    driverUsername: selectedDriver.value.phone,
    driverFullName: selectedDriver.value.fullName,
    secondaryDriverUsername: selectedSecondaryDriver.value?.phone || null,
    secondaryDriverFullName: selectedSecondaryDriver.value?.fullName || null,
    licensePlate: selectedBus.value.licensePlate,
    inspector: selectedInspector.value ? { id: selectedInspector.value.id, phone: selectedInspector.value.phone, fullName: selectedInspector.value.fullName, employeeCode: selectedInspector.value.employeeCode } : null
  });
};

const activeSelectedDriver = computed(() => activeDriverRole.value === 'PRIMARY' ? selectedDriver.value : selectedSecondaryDriver.value);

const selectDriverForRole = (driver) => {
  if (activeDriverRole.value === 'PRIMARY') {
    if (selectedSecondaryDriver.value?.id === driver.id) selectedSecondaryDriver.value = null;
    selectedDriver.value = driver;
  } else {
    if (selectedDriver.value?.id === driver.id) return;
    selectedSecondaryDriver.value = driver;
  }
};

// Hàm kiểm tra nghỉ phép
const getDriverLeaveStatus = (driverUsername, targetDate) => {
   const leave = props.allLeaves?.find(leave => {
      return leave.driverUsername === driverUsername && 
             (leave.status === 'APPROVED' || leave.status === 'PENDING') &&
             leave.startDate <= targetDate && 
             leave.endDate >= targetDate;
   });
   return leave ? leave.status : null;
};

// Inspector list
const processedInspectors = computed(() => {
  if (!props.trip) return [];
  return (props.allInspectors || []).map(inspector => {
    let conflict = false;
    let conflictReason = '';
    let isCurrentAssignee = false;
    let leaveWarning = '';

    const leaveStatus = getDriverLeaveStatus(inspector.phone, props.trip.departureDate);
    if (leaveStatus === 'PENDING') leaveWarning = 'Xin nghỉ';
    else if (leaveStatus === 'APPROVED') leaveWarning = 'Nghỉ phép';

    if (props.trip.inspector?.id === inspector.id) {
        isCurrentAssignee = true;
    } else {
        if (leaveWarning) {
            conflict = true;
            conflictReason = leaveWarning;
        } else {
            const conflictingTrip = (props.allTrips || []).find(t => 
              t.inspector?.id === inspector.id && 
              checkConflict(props.trip, t)
            );
            if (conflictingTrip) {
              conflict = true;
              conflictReason = `Kẹt chuyến ${conflictingTrip.departureTime}`;
            }
        }
    }

    return { ...inspector, conflict, conflictReason, isCurrentAssignee, leaveWarning };
  });
});

const filteredInspectors = computed(() => {
  let list = processedInspectors.value;
  if (inspectorSearch.value) {
    const q = inspectorSearch.value.toLowerCase();
    list = list.filter(i => i.fullName.toLowerCase().includes(q) || i.phone.includes(q));
  }
  return list.sort((a, b) => (a.conflict === b.conflict ? 0 : a.conflict ? 1 : -1));
});

// Cho phép parent component reset nút khi API lỗi
defineExpose({ resetSubmitting: () => { submitting.value = false; } });

// === LOGIC TÍNH TOÁN XUNG ĐỘT (BUFFER THÔNG MINH - HỖ TRỢ QUA NGÀY) ===
// Cùng ngày: buffer 60 phút (không ảnh hưởng chuyến ngắn cùng ngày)
// Khác ngày: buffer 720 phút (12 tiếng) — đảm bảo tài xế kết thúc
// chuyến dài (VD: Đà Nẵng→HCM 21:48) không bị gán chuyến sáng
// hôm sau ở Đà Nẵng vì họ không thể về kịp điểm xuất phát.

// Chuyển ngày + giờ thành số phút tuyệt đối
const parseAbsoluteMinutes = (dateStr, timeStr) => {
  if (!dateStr || !timeStr) return 0;
  const [year, month, day] = dateStr.split('-').map(Number);
  const [h, m] = timeStr.split(':').map(Number);
  const dayMinutes = (year * 365 + month * 31 + day) * 1440;
  return dayMinutes + h * 60 + m;
};

const checkConflict = (targetTrip, testTrip) => {
  if (!targetTrip || !testTrip) return false;
  if (targetTrip.id === testTrip.id) return false;

  const targetStart = parseAbsoluteMinutes(targetTrip.departureDate, targetTrip.departureTime);
  const targetEnd   = targetTrip.arrivalTime
    ? parseAbsoluteMinutes(targetTrip.departureDate, targetTrip.arrivalTime)
    : targetStart + 120;

  const testStart = parseAbsoluteMinutes(testTrip.departureDate, testTrip.departureTime);
  const testEnd   = testTrip.arrivalTime
    ? parseAbsoluteMinutes(testTrip.departureDate, testTrip.arrivalTime)
    : testStart + 120;

  // Lấy tên tỉnh/thành phố ngắn gọn để so sánh (VD: "Đà Nẵng", "Cà Mau")
  const getCity = (point) => {
    if (!point) return '';
    return point.split(',').pop().replace(/\b(Thành phố|TP|Tỉnh)\b/gi, '').trim().toLowerCase();
  };
  
  // Xác định thứ tự thời gian để kiểm tra khớp tuyến (Khứ hồi / Nối chuyến)
  let isLocationMatch = false;
  if (testStart >= targetEnd) {
      // testTrip chạy SAU targetTrip -> Điểm xuất phát của testTrip phải trùng điểm đến của targetTrip
      isLocationMatch = getCity(testTrip.departurePoint) === getCity(targetTrip.arrivalPoint);
  } else if (targetStart >= testEnd) {
      // targetTrip chạy SAU testTrip -> Điểm xuất phát của targetTrip phải trùng điểm đến của testTrip
      isLocationMatch = getCity(targetTrip.departurePoint) === getCity(testTrip.arrivalPoint);
  } else {
      // Bị trùng giờ thẳng vào nhau
      isLocationMatch = false; 
  }

  // Buffer thông minh kết hợp KHÔNG GIAN & THỜI GIAN:
  // - Nếu nối chuyến đúng bến (Khứ hồi) trong cùng ngày: chỉ cần nghỉ 60 phút
  // - Nếu sai bến (Phải chạy xe rỗng đến tỉnh khác) HOẶC khác ngày: Ép nghỉ 12 tiếng (720 phút)
  const isSameDay = targetTrip.departureDate === testTrip.departureDate;
  const buffer = (isLocationMatch && isSameDay) ? 60 : 720;

  return (testStart - buffer) < targetEnd && (testEnd + buffer) > targetStart;
};

const processedDrivers = computed(() => {
  if (!props.trip) return [];
  const targetStart = parseAbsoluteMinutes(props.trip.departureDate, props.trip.departureTime);

  return (props.allDrivers || []).map(driver => {
    let conflict = false;
    let conflictReason = '';
    let isCurrentAssignee = false;
    let leaveWarning = '';
    let lastKnownLocation = '';

    const leaveStatus = getDriverLeaveStatus(driver.phone, props.trip.departureDate);
    if (leaveStatus === 'PENDING') leaveWarning = 'Xin nghỉ';
    else if (leaveStatus === 'APPROVED') leaveWarning = 'Nghỉ phép';

    const driverTrips = (props.allTrips || []).filter(t =>
      (t.assignedDriverUsername === driver.phone || t.secondaryDriverUsername === driver.phone) &&
      t.status !== 'CANCELLED'
    );
    
    // Tìm vị trí cuối cùng (thành phố) trước chuyến hiện tại để gợi ý cho Admin
    let closestTripBefore = null;
    let minDiff = Infinity;
    let isLocationProjected = false;
    
    for (const t of driverTrips) {
       if (t.id === props.trip.id) continue;
       const tEnd = t.arrivalTime ? parseAbsoluteMinutes(t.departureDate, t.arrivalTime) : parseAbsoluteMinutes(t.departureDate, t.departureTime) + 120;
       if (tEnd <= targetStart) {
          const diff = targetStart - tEnd;
          if (diff < minDiff) {
             minDiff = diff;
             closestTripBefore = t;
          }
       }
    }
    
    if (closestTripBefore && closestTripBefore.arrivalPoint) {
       lastKnownLocation = closestTripBefore.arrivalPoint.split(',').pop().replace(/\b(Thành phố|TP|Tỉnh)\b/gi, '').trim();
       isLocationProjected = closestTripBefore.status !== 'COMPLETED';
    }

    if (props.trip.assignedDriverUsername === driver.phone || props.trip.secondaryDriverUsername === driver.phone) {
        isCurrentAssignee = true;
    } else {
        if (leaveWarning) {
           conflict = true;
           conflictReason = leaveWarning;
        } else {
          const conflictingTrip = driverTrips.find(t => checkConflict(props.trip, t));
          if (conflictingTrip) {
            conflict = true;
            conflictReason = `Kẹt chuyến ${conflictingTrip.departureTime}`;
          }
        }
    }

    return { ...driver, conflict, conflictReason, isCurrentAssignee, leaveWarning, lastKnownLocation, isLocationProjected };
  });
});

const filteredDrivers = computed(() => {
  let list = processedDrivers.value;
  if (driverSearch.value) {
    const q = driverSearch.value.toLowerCase();
    list = list.filter(d => d.fullName.toLowerCase().includes(q) || d.phone.includes(q));
  }
  if (activeDriverRole.value === 'SECONDARY' && selectedDriver.value) {
    list = list.map(driver => driver.id === selectedDriver.value.id
      ? { ...driver, conflict: true, conflictReason: 'Đã chọn làm tài xế chính' }
      : driver);
  }
  // Sắp xếp: Rảnh lên trước, kẹt xuống dưới
  return list.sort((a, b) => (a.conflict === b.conflict ? 0 : a.conflict ? 1 : -1));
});

const processedBuses = computed(() => {
  if (!props.trip) return [];
  // Lọc chỉ các xe có cùng dòng xe (busType)
  const compatibleBuses = (props.allBuses || []).filter(b => b.busType === props.trip.busType);
  
  return compatibleBuses.map(bus => {
    let conflict = false;
    let conflictReason = '';
    let isCurrentAssignee = false;

    if (props.trip.assignedLicensePlate === bus.licensePlate) {
        isCurrentAssignee = true;
    } else {
        const conflictingTrip = (props.allTrips || []).find(t => 
          t.assignedLicensePlate === bus.licensePlate && 
          checkConflict(props.trip, t)
        );
        if (conflictingTrip) {
          conflict = true;
          conflictReason = `Kẹt chuyến ${conflictingTrip.departureTime}`;
        }
    }

    return { ...bus, conflict, conflictReason, isCurrentAssignee };
  });
});

const filteredBuses = computed(() => {
  let list = processedBuses.value;
  if (busSearch.value) {
    const q = busSearch.value.toLowerCase();
    list = list.filter(b => b.licensePlate.toLowerCase().includes(q) || b.busType.toLowerCase().includes(q));
  }
  return list.sort((a, b) => (a.conflict === b.conflict ? 0 : a.conflict ? 1 : -1));
});
</script>
