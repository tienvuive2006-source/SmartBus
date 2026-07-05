<template>
  <div class="absolute inset-0 flex flex-col md:flex-row bg-white overflow-hidden font-sans">
    
    <!-- LEFT SIDEBAR: DRIVER LIST -->
    <DriverSidebar 
      v-model:searchQuery="searchQuery"
      v-model:filterStatus="filterStatus"
      :selectedDriver="selectedDriver"
      :filteredDrivers="filteredDrivers"
      :processedInspectors="processedInspectors"
      :loading="loadingDrivers"
      @select-driver="selectDriver"
      @add-driver="isCreateDriverModalOpen = true"
    />

    <!-- RIGHT MAIN: SCHEDULE TIMEGRID -->
    <ScheduleGrid 
      :selectedDriver="selectedDriver"
      :weekDays="weekDays"
      :weekLabel="weekLabel"
      :loadingSchedule="loadingSchedule"
      :getTripsForDay="getTripsForDay"
      :getLeavesForDay="getLeavesForDay"
      @change-week="changeWeek"
      @reset-week="resetToCurrentWeek"
      @open-trip="openTripDetail"
      @open-leave="openLeaveDetail"
    />

    <!-- MODALS -->
    <TripAssignModal 
      ref="assignModalRef"
      :isOpen="isAssignModalOpen"
      :trip="selectedAssignTrip"
      :allDrivers="drivers"
      :allBuses="buses"
      :allTrips="trips"
      :allLeaves="leaves"
      :allInspectors="inspectors"
      @close="isAssignModalOpen = false"
      @submit="handleAssignSubmit"
    />

    <CreateDriverModal 
      :isOpen="isCreateDriverModalOpen"
      :submittingDriver="submittingDriver"
      @close="isCreateDriverModalOpen = false"
      @submit="handleCreateDriver"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';
import DriverSidebar from '@/components/admin/workspace/DriverSidebar.vue';
import ScheduleGrid from '@/components/admin/workspace/ScheduleGrid.vue';
import CreateDriverModal from '@/components/admin/workspace/CreateDriverModal.vue';
import TripAssignModal from '@/components/admin/TripAssignModal.vue';

const api = useApi();
const drivers = ref([]);
const trips = ref([]);
const leaves = ref([]);
const buses = ref([]);
const inspectors = ref([]);

const loadingDrivers = ref(true);
const loadingSchedule = ref(true);

const searchQuery = ref('');
const filterStatus = ref('');
const selectedDriver = ref(null);

// Modal state
const isAssignModalOpen = ref(false);
const selectedAssignTrip = ref(null);
const assignModalRef = ref(null);

const isCreateDriverModalOpen = ref(false);
const submittingDriver = ref(false);

// === TIMEGRID LOGIC ===
const currentStartDate = ref(getMonday(new Date()));

function getMonday(d) {
  d = new Date(d);
  const day = d.getDay(),
      diff = d.getDate() - day + (day == 0 ? -6 : 1);
  return new Date(d.setDate(diff));
}

const weekDays = computed(() => {
  const days = [];
  const start = new Date(currentStartDate.value);
  const dayNames = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
  
  for (let i = 0; i < 7; i++) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    days.push({
      date: d.toISOString().split('T')[0],
      dayName: dayNames[i],
      dateLabel: `${d.getDate()}/${d.getMonth() + 1}`
    });
  }
  return days;
});

const weekLabel = computed(() => {
  const start = weekDays.value[0].dateLabel;
  const end = weekDays.value[6].dateLabel;
  return `${start} - ${end}, ${currentStartDate.value.getFullYear()}`;
});

const changeWeek = (offset) => {
  const newDate = new Date(currentStartDate.value);
  newDate.setDate(newDate.getDate() + offset * 7);
  currentStartDate.value = newDate;
};

const resetToCurrentWeek = () => {
  currentStartDate.value = getMonday(new Date());
};

// === DATA FETCHING ===
const fetchData = async () => {
  loadingDrivers.value = true;
  loadingSchedule.value = true;
  try {
    const [usersRes, tripsRes, leavesRes, busesRes, inspectorsRes] = await Promise.all([
      api.get('/users/role/DRIVER'),
      api.get('/trips'),
      api.get('/leave-requests'),
      api.get('/buses'),
      api.get('/inspector/all')
    ]);
    drivers.value = usersRes.data || [];
    trips.value = tripsRes.data || [];
    leaves.value = leavesRes.data || [];
    buses.value = busesRes.data || [];
    inspectors.value = inspectorsRes.data || [];
  } catch (error) {
    console.error("Lỗi lấy dữ liệu:", error);
  } finally {
    loadingDrivers.value = false;
    loadingSchedule.value = false;
  }
};

onMounted(() => {
  fetchData();
});

// === FILTERING ===
const processedDriversList = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  
  return drivers.value.map(driver => {
    // 1. Check if on leave
    const onLeave = leaves.value.some(l => l.driverUsername === driver.phone && l.status === 'APPROVED' && l.startDate <= today && l.endDate >= today);
    if (onLeave) {
      return { ...driver, computedStatus: 'ON_LEAVE', computedStatusLabel: 'Nghỉ phép', statusColor: 'bg-rose-100 text-rose-700' };
    }
    
    // 2. Check if driving now
    const drivingTrip = trips.value.find(t => t.assignedDriverUsername === driver.phone && t.status === 'IN_PROGRESS');
    if (drivingTrip) {
      return { ...driver, computedStatus: 'DRIVING', computedStatusLabel: 'Đang chạy', statusColor: 'bg-blue-100 text-blue-700' };
    }
    
    // 3. Check if assigned today or future
    const assignedTrip = trips.value.find(t => t.assignedDriverUsername === driver.phone && (t.status === 'ASSIGNED' || t.status === 'PENDING') && t.departureDate >= today);
    if (assignedTrip) {
      return { ...driver, computedStatus: 'ASSIGNED', computedStatusLabel: 'Đã có lịch', statusColor: 'bg-emerald-100 text-emerald-700' };
    }
    
    // 4. Default: FREE
    return { ...driver, computedStatus: 'FREE', computedStatusLabel: 'Đang rảnh', statusColor: 'bg-slate-100 text-slate-600' };
  });
});

const filteredDrivers = computed(() => {
  let list = processedDriversList.value;
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(d => d.fullName.toLowerCase().includes(q) || d.phone.includes(q));
  }
  if (filterStatus.value) {
    list = list.filter(d => d.computedStatus === filterStatus.value);
  }
  return list;
});

const processedInspectors = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  return inspectors.value.map(inspector => {
    const weekDates = weekDays.value.map(d => d.date);
    const hasThisWeek = trips.value.some(t =>
      t.inspector?.id === inspector.id &&
      weekDates.includes(t.departureDate)
    );
    if (hasThisWeek) {
      return { ...inspector, statusLabel: 'Đã có lịch', statusColor: 'bg-emerald-100 text-emerald-700' };
    }
    return { ...inspector, statusLabel: 'Đang rảnh', statusColor: 'bg-slate-100 text-slate-600' };
  });
});

const selectDriver = (driver) => {
  selectedDriver.value = driver;
};

const visibleTrips = computed(() => {
  return trips.value.filter(trip => {
    // 1. Lọc theo ngày trong tuần hiện tại
    const tripDate = trip.departureDate;
    const isThisWeek = weekDays.value.some(d => d.date === tripDate);
    if (!isThisWeek) return false;
    
    // 2. Lọc theo tài xế được chọn
    if (selectedDriver.value) {
      if (trip.assignedDriverUsername !== selectedDriver.value.phone) return false;
    }
    return true;
  });
});

const visibleLeaves = computed(() => {
  return leaves.value.filter(leave => {
    // 1. Lọc theo tài xế
    if (selectedDriver.value && leave.driverUsername !== selectedDriver.value.phone) return false;
    
    // 2. Lọc theo tuần (Cần giao nhau với tuần hiện tại)
    const weekStart = weekDays.value[0].date;
    const weekEnd = weekDays.value[6].date;
    if (leave.endDate < weekStart || leave.startDate > weekEnd) return false;
    
    return true;
  });
});

const getTripsForDay = (dateStr) => {
  return visibleTrips.value.filter(t => t.departureDate === dateStr).sort((a, b) => {
    return a.departureTime.localeCompare(b.departureTime);
  });
};

const getLeavesForDay = (dateStr) => {
  return []; // Hides leaves from the main calendar as requested
};

// === ACTION LOGIC ===
const openTripDetail = (trip) => {
  if (trip.status === 'PENDING' || trip.status === 'ASSIGNED') {
    selectedAssignTrip.value = trip;
    isAssignModalOpen.value = true;
  } else {
    console.log("Trip is in progress or completed");
  }
};

const handleAssignSubmit = async (assignment) => {
  if (!selectedAssignTrip.value) return;
  
  try {
    const updatedTrip = {
      ...selectedAssignTrip.value,
      assignedDriverUsername: assignment.driverUsername,
      assignedDriverFullName: assignment.driverFullName,
      assignedLicensePlate: assignment.licensePlate,
      inspector: assignment.inspector || null,
      status: 'ASSIGNED'
    };
    
    await api.put(`/trips/${updatedTrip.id}`, updatedTrip);
    alert('Phân công chuyến xe thành công!');
    isAssignModalOpen.value = false;
    fetchData();
  } catch (error) {
    console.error("Lỗi phân công:", error);
    assignModalRef.value?.resetSubmitting();
    alert(error.response?.data?.message || 'Có lỗi xảy ra khi lưu phân công.');
  }
};

const openLeaveDetail = (leave) => {
  console.log("Leave Detail", leave);
};

const handleCreateDriver = async (form) => {
  submittingDriver.value = true;
  try {
    await api.post('/auth/register', { ...form, role: 'USER' });
    
    const response = await api.get('/users');
    const newUser = response.data.find(u => u.phone === form.phone);
    
    if (newUser) {
      await api.put(`/users/${newUser.id}`, {
        ...newUser,
        password: '',
        role: 'DRIVER',
        walletBalance: 0,
        avatarUrl: form.avatarUrl
      });
    }
    
    isCreateDriverModalOpen.value = false;
    alert("Thêm tài xế thành công!");
    await fetchData();
  } catch (error) {
    console.error(error);
    const errorMsg = typeof error.response?.data === 'string' 
      ? error.response.data 
      : (error.response?.data?.message || "Lỗi tạo tài khoản!");
    alert(errorMsg);
  } finally {
    submittingDriver.value = false;
  }
};
</script>
