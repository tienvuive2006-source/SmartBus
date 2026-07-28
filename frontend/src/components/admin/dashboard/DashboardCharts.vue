<template>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    
    <!-- Biểu đồ Xu hướng Doanh thu (Line Chart) -->
    <div class="lg:col-span-2 bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden">
      <div class="flex items-center justify-between mb-6 relative z-10">
        <div>
          <h3 class="text-lg font-black text-gray-900 uppercase tracking-widest">Biểu đồ xu hướng tuần</h3>
          <p class="text-sm font-medium text-gray-500 mt-1">Doanh thu 7 ngày gần nhất</p>
        </div>
        <div class="bg-emerald-50 text-emerald-700 px-3 py-1.5 rounded-xl text-xs font-bold flex items-center gap-2 border border-emerald-100">
          <span class="material-symbols-outlined text-[16px]">show_chart</span>
          TRỰC TUYẾN
        </div>
      </div>
      
      <div class="relative z-10 h-72 w-full">
        <Line v-if="chartData.datasets.length" :data="chartData" :options="chartOptions" />
      </div>
    </div>

    <!-- Biểu đồ Cơ cấu Dòng tiền (Doughnut Chart) -->
    <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden">
      <div class="flex items-center justify-between mb-6 relative z-10">
        <h3 class="text-lg font-black text-gray-900 uppercase tracking-widest">Cơ cấu dòng tiền</h3>
        <span class="material-symbols-outlined text-gray-400">pie_chart</span>
      </div>

      <div class="relative z-10 h-48 w-full flex justify-center mb-6">
        <Doughnut v-if="doughnutData.datasets.length" :data="doughnutData" :options="doughnutOptions" />
      </div>
      
      <div class="space-y-3 relative z-10 mt-auto">
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-2 font-bold text-gray-600">
            <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
            Tiền mặt
          </div>
          <span class="font-black text-gray-900">{{ formatCurrency(stats.cashRevenue) }}</span>
        </div>
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-2 font-bold text-gray-600">
            <span class="w-2.5 h-2.5 rounded-full bg-blue-500"></span>
            Chuyển khoản
          </div>
          <span class="font-black text-gray-900">{{ formatCurrency(stats.bankRevenue) }}</span>
        </div>
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-2 font-bold text-gray-600">
            <span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
            Ví điện tử
          </div>
          <span class="font-black text-gray-900">{{ formatCurrency(stats.walletRevenue) }}</span>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Line, Doughnut } from 'vue-chartjs';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
  ArcElement,
  Filler
} from 'chart.js';

// Register Chart.js components
ChartJS.register(
  Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale, ArcElement, Filler
);

const props = defineProps({
  stats: {
    type: Object,
    required: true
  }
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
};

// Line Chart Data & Options
const chartData = computed(() => {
  if (!props.stats || !props.stats.weeklyRevenue) return { labels: [], datasets: [] };
  
  return {
    labels: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'],
    datasets: [
      {
        label: 'Doanh thu (VNĐ)',
        data: props.stats.weeklyRevenue,
        borderColor: '#10b981', // emerald-500
        backgroundColor: 'rgba(16, 185, 129, 0.1)',
        borderWidth: 3,
        pointBackgroundColor: '#ffffff',
        pointBorderColor: '#10b981',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
        fill: true,
        tension: 0.4 // Làm mượt đường
      }
    ]
  };
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: '#1f2937',
      padding: 12,
      titleFont: { size: 13, family: 'Inter, sans-serif' },
      bodyFont: { size: 14, weight: 'bold', family: 'Inter, sans-serif' },
      callbacks: {
        label: (context) => formatCurrency(context.raw)
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: '#f3f4f6',
        drawBorder: false,
      },
      ticks: {
        font: { family: 'Inter, sans-serif', size: 11, weight: 'bold' },
        color: '#9ca3af',
        callback: (value) => {
          if (value >= 1000000) return (value / 1000000) + ' Tr';
          if (value >= 1000) return (value / 1000) + ' K';
          return value;
        }
      }
    },
    x: {
      grid: {
        display: false,
        drawBorder: false,
      },
      ticks: {
        font: { family: 'Inter, sans-serif', size: 12, weight: 'bold' },
        color: '#6b7280'
      }
    }
  },
  interaction: {
    intersect: false,
    mode: 'index',
  }
};

// Doughnut Chart Data & Options
const doughnutData = computed(() => {
  if (!props.stats) return { labels: [], datasets: [] };
  
  return {
    labels: ['Tiền mặt', 'Chuyển khoản', 'Ví điện tử'],
    datasets: [
      {
        data: [
          props.stats.cashRevenue || 0,
          props.stats.bankRevenue || 0,
          props.stats.walletRevenue || 0
        ],
        backgroundColor: [
          '#10b981', // Tiền mặt: emerald-500
          '#3b82f6', // Chuyển khoản: blue-500
          '#f59e0b'  // Ví: amber-500
        ],
        borderWidth: 0,
        hoverOffset: 4
      }
    ]
  };
});

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '75%', // Độ rỗng của vòng tròn
  plugins: {
    legend: {
      display: false // Ẩn legend mặc định vì đã custom ở dưới
    },
    tooltip: {
      backgroundColor: '#1f2937',
      padding: 12,
      bodyFont: { size: 14, weight: 'bold', family: 'Inter, sans-serif' },
      callbacks: {
        label: (context) => ` ${context.label}: ${formatCurrency(context.raw)}`
      }
    }
  }
};
</script>
