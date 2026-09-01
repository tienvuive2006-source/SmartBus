<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-3">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="close"></div>

      <section class="assignment-modal relative flex flex-col overflow-hidden bg-white animate-fade-in-up">
        <AssignmentModalHeader @close="close" />

        <TripAssignmentOverview :trip="trip" :ready="Boolean(selectedDriver && selectedBus)" @suggest="applyAutomaticSuggestion" />

        <div class="assignment-grid-scroll min-h-0 flex-1 bg-slate-100">
          <div class="assignment-grid">
            <DriverTabbedSelectionColumn
              v-model:active-role="activeDriverRole"
              :primary-drivers="filteredDrivers"
              :secondary-drivers="filteredSecondaryDrivers"
              :primary-selected="selectedDriver"
              :secondary-selected="selectedSecondaryDriver"
              v-model:primary-search="driverSearch"
              v-model:secondary-search="secondaryDriverSearch"
              :allow-outside="!routeDriverConfig.unrestricted"
              v-model:show-outside="showOutsideRouteDrivers"
              @select-primary="selectPrimaryDriver"
              @select-secondary="selectSecondaryDriver"
              @clear-secondary="!readOnly && (selectedSecondaryDriver = null)"
            />
            <VehicleSelectionColumn
              :buses="filteredBuses"
              :selected="selectedBus"
              v-model:search="busSearch"
              :loading="routeVehicleLoading"
              :route-count="compatibleRouteVehicleCount"
              :allow-outside="!routeVehicleConfig.unrestricted"
              v-model:show-outside="showOutsideRoute"
              @select="!readOnly && (selectedBus = $event)"
            />
            <InspectorSelectionColumn
              :inspectors="filteredInspectors"
              :selected="selectedInspector"
              v-model:search="inspectorSearch"
              :loading="routeInspectorLoading"
              :allow-outside="!routeInspectorConfig.unrestricted"
              v-model:show-outside="showOutsideRouteInspectors"
              @select="!readOnly && (selectedInspector = $event)"
              @clear="!readOnly && (selectedInspector = null)"
            />
            <AssignmentSummaryPanel
              :driver="selectedDriver"
              :secondary-driver="selectedSecondaryDriver"
              :bus="selectedBus"
              :inspector="selectedInspector"
              :conflict-free="assignmentConflictFree"
              :has-return="Boolean(suggestedReturnTrip)"
              :trip="trip"
              :score="assignmentScore"
            />
          </div>
        </div>

        <AssignmentInsights
          :driver="selectedDriver"
          :bus="selectedBus"
          :conflict-free="assignmentConflictFree"
          @suggest="applyAutomaticSuggestion"
        />

        <ReturnTripSuggestion
          v-if="suggestedReturnTrip"
          v-model="assignSuggestedReturn"
          :outbound-trip="trip"
          :return-trip="suggestedReturnTrip"
          :driver-name="selectedDriver?.fullName || 'Chưa chọn tài xế'"
          :license-plate="selectedBus?.licensePlate || 'Chưa chọn xe'"
        />

        <AssignmentModalFooter
          v-model:note="assignmentNote"
          :has-assignment="hasAssignment"
          :confirming-unassign="confirmingUnassign"
          :submitting="submitting"
          :can-submit="Boolean(selectedDriver && selectedBus && assignmentConflictFree)"
          :read-only="readOnly"
          @close="close"
          @submit="submit"
          @request-unassign="confirmingUnassign = true"
          @cancel-unassign="confirmingUnassign = false"
          @unassign="unassign"
        />
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useApi } from '@/composables/useApi';
import { useRouteVehicleApi } from '@/services/routeVehicleApi';
import { useRouteDriverApi } from '@/services/routeDriverApi';
import { useRouteInspectorApi } from '@/services/routeInspectorApi';
import ReturnTripSuggestion from '@/components/admin/assignment/ReturnTripSuggestion.vue';
import TripAssignmentOverview from '@/components/admin/assignment/TripAssignmentOverview.vue';
import DriverTabbedSelectionColumn from '@/components/admin/assignment/DriverTabbedSelectionColumn.vue';
import VehicleSelectionColumn from '@/components/admin/assignment/VehicleSelectionColumn.vue';
import InspectorSelectionColumn from '@/components/admin/assignment/InspectorSelectionColumn.vue';
import AssignmentSummaryPanel from '@/components/admin/assignment/AssignmentSummaryPanel.vue';
import AssignmentModalHeader from '@/components/admin/assignment/AssignmentModalHeader.vue';
import AssignmentInsights from '@/components/admin/assignment/AssignmentInsights.vue';
import AssignmentModalFooter from '@/components/admin/assignment/AssignmentModalFooter.vue';
import { findSuggestedReturnTrip } from '@/utils/tripReturnSuggestion';

const props = defineProps({
  isOpen: Boolean,
  trip: Object,
  allDrivers: Array,
  allBuses: Array,
  allTrips: Array,
  allLeaves: Array,
  allInspectors: Array, // Danh sách lơ xe
  readOnly: Boolean
});

const emit = defineEmits(['close', 'submit', 'unassign']);
const api = useApi();
const routeVehicleApi = useRouteVehicleApi();
const routeDriverApi = useRouteDriverApi();
const routeInspectorApi = useRouteInspectorApi();

const driverSearch = ref('');
const secondaryDriverSearch = ref('');
const busSearch = ref('');
const inspectorSearch = ref('');
const selectedDriver = ref(null);
const selectedSecondaryDriver = ref(null);
const activeDriverRole = ref('PRIMARY');
const selectedBus = ref(null);
const selectedInspector = ref(null);
const submitting = ref(false);
const confirmingUnassign = ref(false);
const assignSuggestedReturn = ref(true);
const routeVehicleLoading = ref(false);
const showOutsideRoute = ref(false);
const routeVehicleConfig = ref({ unrestricted: true, vehicles: [] });
const routeDriverLoading = ref(false);
const showOutsideRouteDrivers = ref(false);
const routeDriverConfig = ref({ unrestricted: true, drivers: [] });
const routeInspectorLoading = ref(false);
const showOutsideRouteInspectors = ref(false);
const routeInspectorConfig = ref({ unrestricted: true, inspectors: [] });
const assignmentNote = ref('');
const latestBuses = ref([]);

const normalizePoint = value => String(value || '')
  .trim()
  .toLocaleLowerCase('vi-VN')
  .replace(/\s+/g, ' ');

const resolveRouteId = async () => {
  if (props.trip?.routeId) return props.trip.routeId;
  const response = await api.get('/routes');
  const routes = Array.isArray(response.data) ? response.data : [];
  const departure = normalizePoint(props.trip?.departurePoint);
  const arrival = normalizePoint(props.trip?.arrivalPoint);
  const route = routes.find(item =>
    (normalizePoint(item.departurePoint) === departure && normalizePoint(item.arrivalPoint) === arrival) ||
    (normalizePoint(item.departurePoint) === arrival && normalizePoint(item.arrivalPoint) === departure)
  );
  return route?.id || null;
};

const loadRouteVehicleConfig = async () => {
  routeVehicleConfig.value = { unrestricted: true, vehicles: [] };
  showOutsideRoute.value = false;
  if (!props.trip) return;

  routeVehicleLoading.value = true;
  try {
    const routeId = await resolveRouteId();
    if (!routeId) return;
    const response = await routeVehicleApi.getConfig(routeId);
    routeVehicleConfig.value = response.data;
  } catch (error) {
    console.error('Không tải được nhóm xe của tuyến:', error);
  } finally {
    routeVehicleLoading.value = false;
  }
};

const loadRouteDriverConfig = async () => {
  routeDriverConfig.value = { unrestricted: true, drivers: [] };
  showOutsideRouteDrivers.value = false;
  if (!props.trip) return;

  routeDriverLoading.value = true;
  try {
    const routeId = await resolveRouteId();
    if (!routeId) return;
    const response = await routeDriverApi.getConfig(routeId);
    routeDriverConfig.value = response.data;
  } catch (error) {
    console.error('Không tải được nhóm tài xế của tuyến:', error);
  } finally {
    routeDriverLoading.value = false;
  }
};

const loadRouteInspectorConfig = async () => {
  routeInspectorConfig.value = { unrestricted: true, inspectors: [] };
  showOutsideRouteInspectors.value = false;
  if (!props.trip) return;

  routeInspectorLoading.value = true;
  try {
    const routeId = await resolveRouteId();
    if (!routeId) return;
    const response = await routeInspectorApi.getConfig(routeId);
    routeInspectorConfig.value = response.data;
  } catch (error) {
    console.error('Không tải được nhóm lơ xe của tuyến:', error);
  } finally {
    routeInspectorLoading.value = false;
  }
};

const loadLatestBuses = async () => {
  try {
    const response = await api.get('/buses');
    latestBuses.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    latestBuses.value = [];
    console.error('Không tải được vị trí GPS mới nhất của xe:', error);
  }
};

// Reset state when opened
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    driverSearch.value = '';
    secondaryDriverSearch.value = '';
    busSearch.value = '';
    inspectorSearch.value = '';
    selectedDriver.value = null;
    selectedSecondaryDriver.value = null;
    activeDriverRole.value = 'PRIMARY';
    selectedBus.value = null;
    selectedInspector.value = null;
    submitting.value = false;
    confirmingUnassign.value = false;
    assignSuggestedReturn.value = true;
    assignmentNote.value = '';
    loadRouteVehicleConfig();
    loadRouteDriverConfig();
    loadRouteInspectorConfig();
    loadLatestBuses();
    
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

const suggestedReturnTrip = computed(() => findSuggestedReturnTrip(props.trip, props.allTrips));

const hasAssignment = computed(() => Boolean(
  props.trip?.assignedDriverUsername ||
  props.trip?.secondaryDriverUsername ||
  props.trip?.assignedLicensePlate ||
  props.trip?.inspector
));

const unassign = () => {
  if (!hasAssignment.value) return;
  submitting.value = true;
  emit('unassign');
};

const submit = () => {
  if (!selectedDriver.value || !selectedBus.value) return;
  submitting.value = true;
  emit('submit', {
    driverUsername: selectedDriver.value.phone,
    driverFullName: selectedDriver.value.fullName,
    secondaryDriverUsername: selectedSecondaryDriver.value?.phone || null,
    secondaryDriverFullName: selectedSecondaryDriver.value?.fullName || null,
    licensePlate: selectedBus.value.licensePlate,
    returnTripId: assignSuggestedReturn.value ? suggestedReturnTrip.value?.id || null : null,
    inspector: selectedInspector.value ? { id: selectedInspector.value.id, phone: selectedInspector.value.phone, fullName: selectedInspector.value.fullName, employeeCode: selectedInspector.value.employeeCode } : null
  });
};

const activeSelectedDriver = computed(() => activeDriverRole.value === 'PRIMARY' ? selectedDriver.value : selectedSecondaryDriver.value);

const selectPrimaryDriver = driver => {
  if (props.readOnly) return;
  if (selectedSecondaryDriver.value?.id === driver.id) selectedSecondaryDriver.value = null;
  selectedDriver.value = driver;
};

const selectSecondaryDriver = driver => {
  if (props.readOnly) return;
  if (selectedDriver.value?.id === driver.id) return;
  selectedSecondaryDriver.value = driver;
};

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

const routeEligibleInspectors = computed(() => {
  let list = processedInspectors.value;
  const roleByPhone = new Map(
    (routeInspectorConfig.value.inspectors || []).map(inspector => [inspector.phone, inspector.role])
  );
  if (!routeInspectorConfig.value.unrestricted && !showOutsideRouteInspectors.value) {
    list = list.filter(inspector =>
      roleByPhone.has(inspector.phone) ||
      inspector.isCurrentAssignee ||
      selectedInspector.value?.id === inspector.id
    );
  }
  const roleOrder = { PRIMARY: 0, BACKUP: 1, OUTSIDE: 2 };
  return list
    .map(inspector => ({
      ...inspector,
      routeRole: routeInspectorConfig.value.unrestricted
        ? null
        : (roleByPhone.get(inspector.phone) || 'OUTSIDE')
    }))
    .sort((a, b) => {
      if (a.conflict !== b.conflict) return a.conflict ? 1 : -1;
      return (roleOrder[a.routeRole] ?? 2) - (roleOrder[b.routeRole] ?? 2);
    });
});

const filteredInspectors = computed(() => {
  let list = routeEligibleInspectors.value;
  if (inspectorSearch.value) {
    const q = inspectorSearch.value.toLocaleLowerCase('vi-VN');
    list = list.filter(i => i.fullName.toLocaleLowerCase('vi-VN').includes(q) || i.phone.includes(q));
  }
  return list;
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
  return new Date(year, month - 1, day, h, m, 0, 0).getTime() / 60000;
};

const getTripArrivalMinutes = trip => {
  if (!trip) return 0;
  const departure = parseAbsoluteMinutes(trip.departureDate, trip.departureTime);
  if (!trip.arrivalTime) return departure + 120;
  let arrival = parseAbsoluteMinutes(trip.departureDate, trip.arrivalTime);
  if (arrival <= departure) arrival += 1440;
  return arrival;
};

const formatTripArrival = trip => {
  const arrivalMinutes = getTripArrivalMinutes(trip);
  if (!arrivalMinutes) return '';
  const value = new Date(arrivalMinutes * 60000);
  const pad = number => String(number).padStart(2, '0');
  return `${pad(value.getHours())}:${pad(value.getMinutes())} ngày ${pad(value.getDate())}/${pad(value.getMonth() + 1)}/${value.getFullYear()}`;
};

const checkConflict = (targetTrip, testTrip) => {
  if (!targetTrip || !testTrip) return false;
  if (targetTrip.id === testTrip.id) return false;

  const targetStart = parseAbsoluteMinutes(targetTrip.departureDate, targetTrip.departureTime);
  const targetEnd = getTripArrivalMinutes(targetTrip);

  const testStart = parseAbsoluteMinutes(testTrip.departureDate, testTrip.departureTime);
  const testEnd = getTripArrivalMinutes(testTrip);

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
    let lastKnownAt = '';

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
       const tEnd = getTripArrivalMinutes(t);
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
       lastKnownAt = formatTripArrival(closestTripBefore);
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

    return { ...driver, conflict, conflictReason, isCurrentAssignee, leaveWarning, lastKnownLocation, lastKnownAt, isLocationProjected };
  });
});

const routeEligibleDrivers = computed(() => {
  let list = processedDrivers.value;
  const roleByDriverId = new Map(
    (routeDriverConfig.value.drivers || []).map(driver => [driver.driverId, driver.role])
  );
  if (!routeDriverConfig.value.unrestricted && !showOutsideRouteDrivers.value) {
    list = list.filter(driver =>
      roleByDriverId.has(driver.id) ||
      driver.isCurrentAssignee ||
      selectedDriver.value?.id === driver.id ||
      selectedSecondaryDriver.value?.id === driver.id
    );
  }
  const roleOrder = { PRIMARY: 0, BACKUP: 1, OUTSIDE: 2 };
  return list
    .map(driver => ({
      ...driver,
      routeRole: routeDriverConfig.value.unrestricted
        ? null
        : (roleByDriverId.get(driver.id) || 'OUTSIDE')
    }))
    .sort((a, b) => {
      if (a.conflict !== b.conflict) return a.conflict ? 1 : -1;
      return (roleOrder[a.routeRole] ?? 2) - (roleOrder[b.routeRole] ?? 2);
    });
});

const matchesDriverSearch = (driver, search) => {
  const query = String(search || '').trim().toLocaleLowerCase('vi-VN');
  if (!query) return true;
  return driver.fullName?.toLocaleLowerCase('vi-VN').includes(query) || driver.phone?.includes(query);
};

const filteredDrivers = computed(() =>
  routeEligibleDrivers.value.filter(driver => matchesDriverSearch(driver, driverSearch.value))
);

const filteredSecondaryDrivers = computed(() =>
  routeEligibleDrivers.value
    .filter(driver => matchesDriverSearch(driver, secondaryDriverSearch.value))
    .map(driver => driver.id === selectedDriver.value?.id
      ? { ...driver, conflict: true, conflictReason: 'Đã chọn làm tài xế chính' }
      : driver)
);

const assignmentConflictFree = computed(() =>
  !selectedDriver.value?.conflict &&
  !selectedSecondaryDriver.value?.conflict &&
  !selectedBus.value?.conflict &&
  !selectedInspector.value?.conflict
);

const assignmentScore = computed(() => {
  let score = 20;
  if (selectedDriver.value) score += 35;
  if (selectedBus.value) score += 30;
  if (assignmentConflictFree.value && selectedDriver.value && selectedBus.value) score += 15;
  return Math.min(score, 100);
});

const applyAutomaticSuggestion = () => {
  if (props.readOnly) return;
  const suggestedDriver = filteredDrivers.value.find(driver => !driver.conflict);
  const suggestedBus = filteredBuses.value.find(bus => !bus.conflict);
  if (suggestedDriver) selectPrimaryDriver(suggestedDriver);
  if (suggestedBus) selectedBus.value = suggestedBus;
};

const routeVehicleRoleByBusId = computed(() => new Map(
  (routeVehicleConfig.value.vehicles || []).map(vehicle => [vehicle.busId, vehicle.role])
));

const busSource = computed(() => latestBuses.value.length ? latestBuses.value : (props.allBuses || []));

const shortStation = value => String(value || '').split(',')[0].trim();

const operationalBusLocation = bus => {
  const busTrips = (props.allTrips || [])
    .filter(candidate =>
      candidate.assignedLicensePlate === bus.licensePlate &&
      candidate.status !== 'CANCELLED'
    );

  const runningTrip = busTrips.find(candidate => candidate.status === 'IN_PROGRESS');
  if (runningTrip) return shortStation(runningTrip.departurePoint);

  const latestCompletedTrip = busTrips
    .filter(candidate => candidate.status === 'COMPLETED')
    .sort((left, right) =>
      parseAbsoluteMinutes(right.departureDate, right.departureTime) -
      parseAbsoluteMinutes(left.departureDate, left.departureTime)
    )[0];
  if (latestCompletedTrip) return shortStation(latestCompletedTrip.arrivalPoint);

  return 'Chưa xác định';
};

const compatibleRouteVehicleCount = computed(() => {
  if (!props.trip) return 0;
  return (props.allBuses || []).filter(bus =>
    bus.busType === props.trip.busType && routeVehicleRoleByBusId.value.has(bus.id)
  ).length;
});

const processedBuses = computed(() => {
  if (!props.trip) return [];
  // Một chuyến chỉ được phân công xe đúng dòng đã chọn khi tạo chuyến.
  let compatibleBuses = busSource.value.filter(bus => bus.busType === props.trip.busType);
  if (!routeVehicleConfig.value.unrestricted && !showOutsideRoute.value) {
    compatibleBuses = compatibleBuses.filter(bus =>
      routeVehicleRoleByBusId.value.has(bus.id) ||
      props.trip.assignedLicensePlate === bus.licensePlate
    );
  }
  
  return compatibleBuses.map(bus => {
    let conflict = false;
    let conflictReason = '';
    let isCurrentAssignee = false;
    const assignmentLocation = operationalBusLocation(bus);

    if (bus.status === 'BẢO TRÌ') {
      conflict = true;
      conflictReason = 'Xe đang bảo trì';
    } else if (props.trip.assignedLicensePlate === bus.licensePlate) {
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

    const configuredRole = routeVehicleRoleByBusId.value.get(bus.id);
    const routeRole = routeVehicleConfig.value.unrestricted
      ? null
      : (configuredRole || 'OUTSIDE');
    return {
      ...bus,
      conflict,
      conflictReason,
      isCurrentAssignee,
      routeRole,
      assignmentLocation
    };
  });
});

const filteredBuses = computed(() => {
  let list = processedBuses.value;
  if (busSearch.value) {
    const q = busSearch.value.toLowerCase();
    list = list.filter(b => b.licensePlate.toLowerCase().includes(q) || b.busType.toLowerCase().includes(q));
  }
  const roleOrder = { PRIMARY: 0, BACKUP: 1, OUTSIDE: 2 };
  return list.sort((a, b) => {
    if (a.conflict !== b.conflict) return a.conflict ? 1 : -1;
    return (roleOrder[a.routeRole] ?? 0) - (roleOrder[b.routeRole] ?? 0);
  });
});
</script>

<style scoped>
.assignment-modal {
  width: calc(100vw - 1rem);
  max-width: 102rem;
  height: min(96vh, 61rem);
  border-radius: 1.15rem;
  box-shadow: 0 30px 90px rgba(15, 23, 42, 0.28);
}

.assignment-grid-scroll {
  overflow-x: auto;
  overflow-y: hidden;
}

.assignment-grid {
  display: grid;
  grid-template-rows: minmax(0, 1fr);
  grid-template-columns:
    minmax(19rem, 1.18fr)
    minmax(17rem, 1fr)
    minmax(17rem, 1fr)
    minmax(17rem, 0.95fr);
  width: 100%;
  min-width: 70rem;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

@media (max-width: 1280px) {
  .assignment-modal {
    width: calc(100vw - 0.5rem);
    height: 97vh;
    border-radius: 0.85rem;
  }
}
</style>
