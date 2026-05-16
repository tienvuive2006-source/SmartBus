<template>
  <div class="lg:col-span-7 space-y-5">
    <div class="flex items-center justify-between mb-3">
      <h3 class="font-headline-sm text-headline-sm font-black text-on-surface">
        Bảng kê khai hạm đội
        <span class="text-label-md font-black ml-2 bg-primary/5 text-primary px-2.5 py-0.5 rounded-full border border-primary/10">{{ buses.length }} đầu xe</span>
      </h3>
    </div>

    <div v-if="loading" class="p-12 bg-white rounded-2xl text-center border border-outline-variant/20 shadow-sm">
      <div class="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
      <p class="text-body-md text-on-surface-variant">Đang quét cơ sở dữ liệu SQL Server...</p>
    </div>

    <div v-else-if="buses.length === 0" class="p-12 bg-white rounded-2xl text-center border border-dashed border-outline-variant/50">
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
        v-for="bus in buses" 
        :key="bus.id"
        class="bg-white rounded-2xl shadow-[0px_4px_16px_rgba(0,0,0,0.02)] border border-outline-variant/25 overflow-hidden hover:shadow-lg hover:-translate-y-0.5 transition-all duration-200 group"
      >
        <div class="p-5 flex flex-col sm:flex-row gap-5">
          <div :class="[
            'w-20 h-20 rounded-xl flex flex-col items-center justify-center text-white shadow-md shrink-0 transition-all',
            bus.status === 'ĐANG CHẠY' ? 'bg-gradient-to-br from-emerald-500 to-emerald-600' :
            bus.status === 'BẢO TRÌ' ? 'bg-gradient-to-br from-amber-500 to-orange-600 animate-pulse' :
            'bg-gradient-to-br from-slate-500 to-slate-600'
          ]">
            <span class="material-symbols-outlined text-3xl">
              {{ bus.status === 'BẢO TRÌ' ? 'build' : 'directions_bus' }}
            </span>
            <span class="text-[10px] font-black tracking-wider mt-1 uppercase">{{ bus.busType.split(' ')[0] }}</span>
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

              <div :class="[
                'px-3 py-1 rounded-full text-[11px] font-black tracking-wide uppercase border shadow-sm',
                bus.status === 'ĐANG CHẠY' ? 'bg-emerald-50 border-emerald-100 text-emerald-700' :
                bus.status === 'BẢO TRÌ' ? 'bg-amber-50 border-amber-100 text-amber-700' :
                'bg-slate-50 border-slate-200 text-slate-600'
              ]">
                ● {{ bus.status }}
              </div>
            </div>

            <div class="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-on-surface-variant">
              <div class="flex items-center gap-2 bg-surface-container-lowest p-2 rounded-xl border border-outline-variant/20">
                <span class="material-symbols-outlined text-primary text-[20px]">airline_seat_recline_normal</span>
                <div class="flex flex-col">
                  <span class="text-[10px] font-black text-outline uppercase">Tài xế chủ xe</span>
                  <span class="text-label-md font-bold text-on-surface">{{ bus.driverName || 'Chưa phân công' }}</span>
                </div>
              </div>
              <div class="flex items-center gap-2 bg-surface-container-lowest p-2 rounded-xl border border-outline-variant/20">
                <span class="material-symbols-outlined text-secondary text-[20px]">location_on</span>
                <div class="flex flex-col">
                  <span class="text-[10px] font-black text-outline uppercase">Vị trí hiện tại (Trạm)</span>
                  <span class="text-label-md font-bold text-on-surface">{{ bus.currentStation || 'Chưa xác định' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="border-t border-dashed border-outline-variant/30 p-3 bg-surface-container-lowest flex justify-between items-center px-5 flex-wrap gap-2">
          <div class="flex items-center gap-2">
            <button 
              @click="$emit('locate', bus)"
              class="text-primary hover:text-surface-tint font-black text-label-md flex items-center gap-1 px-3 py-1 hover:bg-primary/5 rounded-lg transition-all"
            >
              <span class="material-symbols-outlined text-sm">share_location</span>
              ĐỊNH VỊ GPS ➔
            </button>
          </div>

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
defineProps({
  buses: Array,
  loading: Boolean
});

defineEmits(['open-create', 'locate', 'edit', 'delete']);
</script>
