<template>
  <div class="lg:col-span-7 space-y-5">
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-4 gap-3">
      <h3 class="font-headline-sm text-headline-sm font-black text-on-surface">
        Bảng kê khai hạm đội
        <span class="text-label-md font-black ml-2 bg-primary/5 text-primary px-2.5 py-0.5 rounded-full border border-primary/10">{{ filteredBuses.length }} đầu xe</span>
      </h3>

      <div class="relative w-full sm:w-[240px]">
        <select 
          v-model="selectedBusType" 
          class="w-full appearance-none bg-white border border-outline-variant/30 text-on-surface font-bold py-2.5 px-4 pr-10 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm transition-all"
        >
          <option value="">Tất cả dòng xe</option>
          <option v-for="type in uniqueBusTypes" :key="type" :value="type">
            {{ type }}
          </option>
        </select>
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-outline-variant">expand_more</span>
      </div>
    </div>

    <div v-if="loading" class="p-12 bg-white rounded-2xl text-center border border-outline-variant/20 shadow-sm">
      <div class="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
      <p class="text-body-md text-on-surface-variant">Đang quét cơ sở dữ liệu SQL Server...</p>
    </div>

    <div v-else-if="filteredBuses.length === 0" class="p-12 bg-white rounded-2xl text-center border border-dashed border-outline-variant/50">
      <div class="w-20 h-20 bg-surface-container-high rounded-full flex items-center justify-center mx-auto mb-4">
        <span class="material-symbols-outlined text-5xl text-outline-variant">bus_alert</span>
      </div>
      <h4 class="text-headline-sm font-bold text-on-surface">Kho xe đang trống trơn!</h4>
      <p class="text-body-md text-on-surface-variant mt-1 max-w-sm mx-auto mb-6">Hãng xe của bạn chưa đăng ký chiếc ô tô thật nào. Hãy bấm Thêm Xe Mới để bắt đầu quản lý.</p>
      <button @click="$emit('open-create')" class="px-6 py-2.5 bg-primary text-on-primary rounded-xl font-bold hover:bg-surface-tint active:scale-95 transition-all">
        NHẬP XE ĐẦU TIÊN NGAY
      </button>
    </div>

    <div v-else class="space-y-4">
      <div 
        v-for="bus in filteredBuses" 
        :key="bus.id"
        class="bg-white rounded-2xl shadow-[0px_4px_16px_rgba(0,0,0,0.02)] border border-outline-variant/25 overflow-hidden hover:shadow-lg hover:-translate-y-0.5 transition-all duration-200 group"
      >
        <div class="p-5 flex flex-col sm:flex-row gap-5">
          <div class="relative w-24 h-24 sm:w-32 sm:h-24 rounded-xl overflow-hidden shrink-0 shadow-md">
            <img v-if="bus.imageUrl" :src="bus.imageUrl" class="w-full h-full object-cover" />
            <div v-else :class="[
              'w-full h-full flex flex-col items-center justify-center text-white transition-all',
              bus.status === 'ĐANG CHẠY' ? 'bg-gradient-to-br from-emerald-500 to-emerald-600' :
              bus.status === 'BẢO TRÌ' ? 'bg-gradient-to-br from-amber-500 to-orange-600 animate-pulse' :
              'bg-gradient-to-br from-slate-500 to-slate-600'
            ]">
              <span class="material-symbols-outlined text-3xl">
                {{ bus.status === 'BẢO TRÌ' ? 'build' : 'directions_bus' }}
              </span>
              <span class="text-[10px] font-black tracking-wider mt-1 uppercase">{{ bus.busType.split(' ')[0] }}</span>
            </div>
            
            <div :class="[
                'absolute top-2 left-2 px-2 py-0.5 rounded text-[9px] font-black tracking-wide uppercase shadow-sm backdrop-blur-sm',
                bus.status === 'ĐANG CHẠY' ? 'bg-emerald-500/90 text-white' :
                bus.status === 'BẢO TRÌ' ? 'bg-amber-500/90 text-white' :
                'bg-slate-700/90 text-white'
              ]">
                {{ bus.status }}
            </div>
          </div>
          
          <div class="flex-1">
            <div class="flex justify-between items-start flex-wrap gap-2">
              <div>
                <h4 class="font-black text-headline-sm text-on-surface flex items-center gap-2">
                  <span class="px-3 py-1 bg-amber-400 border-2 border-amber-600 text-on-amber-container rounded-lg font-black text-label-lg tracking-widest font-mono shadow-sm">
                    {{ bus.licensePlate }}
                  </span>
                </h4>
                <p class="text-body-md font-bold text-on-surface-variant mt-1.5 flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm text-primary">airport_shuttle</span>
                  Dòng: <span class="text-primary">{{ bus.busType }}</span>
                </p>
              </div>

              <!-- Status badge is moved to image overlay -->
            </div>


          </div>
        </div>

        <div class="border-t border-dashed border-outline-variant/30 p-3 bg-surface-container-lowest flex justify-end items-center px-5 flex-wrap gap-2">

          <div class="flex items-center gap-2">
            <button 
              @click="$emit('edit', bus)" 
              class="px-3 py-1.5 border border-outline-variant/50 hover:bg-primary/5 hover:text-primary hover:border-primary rounded-lg font-bold text-label-md text-on-surface-variant transition-colors flex items-center gap-1"
            >
              <span class="material-symbols-outlined text-sm">edit</span> Sửa
            </button>
            <button 
              @click="$emit('delete', bus)" 
              class="px-3 py-1.5 border border-error/30 hover:bg-error hover:text-white hover:border-error rounded-lg font-bold text-label-md text-error transition-all flex items-center gap-1"
            >
              <span class="material-symbols-outlined text-sm">delete</span> Xóa
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  buses: {
    type: Array,
    default: () => []
  },
  loading: Boolean
});

defineEmits(['open-create', 'locate', 'edit', 'delete']);

const selectedBusType = ref('');

const uniqueBusTypes = computed(() => {
  const types = props.buses.map(b => b.busType);
  return [...new Set(types)].filter(Boolean);
});

const filteredBuses = computed(() => {
  if (!selectedBusType.value) return props.buses;
  return props.buses.filter(b => b.busType === selectedBusType.value);
});
</script>
