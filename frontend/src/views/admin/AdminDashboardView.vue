<template>
  <div class="space-y-8">
    
    <!-- Header Section -->
    <DashboardHeader :lastUpdated="stats.lastUpdated" :isRefreshing="isRefreshing" @refresh="fetchStats" />

    <!-- Key Metrics Grid -->
    <DashboardStatsCards :stats="stats" />

    <!-- Charts Section -->
    <DashboardCharts :stats="stats" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useApi } from '@/composables/useApi';
import DashboardHeader from '@/components/admin/dashboard/DashboardHeader.vue';
import DashboardStatsCards from '@/components/admin/dashboard/DashboardStatsCards.vue';
import DashboardCharts from '@/components/admin/dashboard/DashboardCharts.vue';

const api = useApi();

const stats = ref({
  totalRevenue: 0,
  cashRevenue: 0,
  bankRevenue: 0,
  walletRevenue: 0,
  totalTickets: 0,
  totalTrips: 0,
  totalBuses: 0,
  activeBuses: 0,
  totalCustomers: 0,
  occupancyRate: 0,
  weeklyRevenue: [0, 0, 0, 0, 0, 0, 0],
  lastUpdated: '--:--'
});



const isRefreshing = ref(false);

const fetchStats = async () => {
  isRefreshing.value = true;
  try {
    const response = await api.get('/dashboard/stats');
    stats.value = response.data;
  } catch (error) {
    console.error("Lỗi tải số liệu Dashboard:", error);
  } finally {
    setTimeout(() => {
      isRefreshing.value = false;
    }, 500); // Thêm delay nhỏ để thấy hiệu ứng xoay
  }
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};



onMounted(() => {
  fetchStats();
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-track {
  background: transparent; 
}
::-webkit-scrollbar-thumb {
  background: #e2e8f0; 
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1; 
}
</style>
