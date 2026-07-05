<template>
  <header class="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
    <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between gap-4">
      <div class="flex items-center gap-6 flex-1">
        <div @click="$router.push('/')" class="cursor-pointer flex items-center group -ml-2">
           <img src="/logo2.png" alt="Trung Nam" class="h-12 w-auto object-contain transform scale-[1.3] origin-left transition-transform duration-500 group-hover:scale-[1.4]" />
        </div>
        
        <div class="hidden md:flex items-center bg-white border border-zinc-200/80 rounded-full shadow-[0_8px_30px_rgb(0,0,0,0.06)] hover:shadow-[0_8px_30px_rgb(0,0,0,0.12)] transition-shadow duration-300 max-w-3xl mx-auto flex-1 h-14">
           <div class="px-5 flex items-center gap-3 flex-1 h-full hover:bg-zinc-50 transition-colors group relative border-r border-zinc-100 cursor-pointer rounded-l-full">
              <span class="material-symbols-outlined text-zinc-400 text-lg shrink-0 group-hover:text-[#075955] transition-colors">location_on</span>
              <div class="flex flex-col justify-center flex-1 w-full h-full">
                <span class="text-[9px] font-black text-zinc-400 uppercase tracking-widest leading-none mb-0.5">Điểm đón</span>
                <select 
                  :value="$route.query.from" 
                  @change="$emit('location-change', 'from', $event.target.value)"
                  class="w-full bg-transparent text-[13px] font-bold text-zinc-800 outline-none cursor-pointer appearance-none z-10 truncate leading-none p-0 m-0"
                >
                  <option value="" disabled selected>Chọn nơi đi</option>
                  <option v-for="loc in availableLocations" :key="loc" :value="loc">{{ loc }}</option>
                </select>
              </div>
              
              <!-- Nút đổi chiều -->
              <div 
                 @click.stop="$emit('swap-locations')" 
                 class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2 w-8 h-8 bg-white border border-zinc-200 rounded-full flex items-center justify-center shadow-sm hover:shadow-md hover:bg-zinc-50 hover:text-[#075955] text-zinc-400 transition-all z-20 hover:scale-105"
                 title="Đổi chiều"
              >
                 <span class="material-symbols-outlined text-lg">swap_horiz</span>
              </div>
           </div>
           <div class="px-5 flex items-center gap-3 flex-1 h-full hover:bg-zinc-50 transition-colors group relative border-r border-zinc-100 cursor-pointer">
              <span class="material-symbols-outlined text-zinc-400 text-lg shrink-0 group-hover:text-[#075955] transition-colors">near_me</span>
              <div class="flex flex-col justify-center flex-1 w-full h-full">
                <span class="text-[9px] font-black text-zinc-400 uppercase tracking-widest leading-none mb-0.5">Điểm đến</span>
                <select 
                  :value="$route.query.to" 
                  @change="$emit('location-change', 'to', $event.target.value)"
                  class="w-full bg-transparent text-[13px] font-bold text-zinc-800 outline-none cursor-pointer appearance-none z-10 truncate leading-none p-0 m-0"
                >
                  <option value="" disabled selected>Chọn nơi đến</option>
                  <option v-for="loc in availableLocations" :key="loc" :value="loc">{{ loc }}</option>
                </select>
              </div>
           </div>
           <div class="pl-5 pr-2 flex items-center justify-between gap-3 flex-1 h-full hover:bg-zinc-50 transition-colors group relative cursor-pointer rounded-r-full" title="Đổi ngày đi">
              <div class="flex items-center gap-3">
                 <span class="material-symbols-outlined text-zinc-400 text-lg group-hover:text-[#075955] transition-colors">calendar_month</span>
                 <div class="flex flex-col justify-center flex-1">
                   <span class="text-[9px] font-black text-zinc-400 uppercase tracking-widest leading-none mb-0.5">Khởi hành</span>
                   <span class="text-[13px] font-bold text-zinc-800 leading-none">{{ formatDateDisplay($route.query.date) }}</span>
                 </div>
                 <input 
                   type="date" 
                   :value="$route.query.date"
                   @change="$emit('date-change', $event)"
                   onclick="this.showPicker()"
                   class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                   style="z-index: 10;"
                 />
              </div>
              <button @click="$router.push('/')" class="w-10 h-10 bg-[#075955] hover:bg-emerald-700 text-white rounded-full flex items-center justify-center transition-transform active:scale-95 shrink-0 shadow-md z-20">
                 <span class="material-symbols-outlined text-[20px]">search</span>
              </button>
           </div>
        </div>
      </div>
      
      <div class="flex items-center gap-2 sm:gap-4">
         <button @click="$router.push(authStore.isLoggedIn ? '/profile' : '/auth/login')" class="relative overflow-hidden group px-4 py-1.5 rounded-full flex items-center gap-2.5 hover:-translate-y-0.5 active:translate-y-px transition-all duration-500 cursor-pointer border border-transparent hover:border-yellow-100 bg-white shadow-sm hover:shadow-md">
            <div class="w-8 h-8 rounded-full flex items-center justify-center transition-all duration-300 relative z-10 border bg-yellow-50 border-yellow-200 text-yellow-600 group-hover:bg-yellow-100 group-hover:border-yellow-400">
              <span class="material-symbols-outlined text-[18px]">person_outline</span>
            </div>
            <span class="font-bold text-[13px] tracking-wide relative z-10 transition-colors duration-500 whitespace-nowrap text-zinc-700 group-hover:text-yellow-700">
              {{ authStore.currentUser?.fullName?.toUpperCase() || 'ĐĂNG NHẬP' }}
            </span>
         </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';

defineProps({
  availableLocations: { type: Array, required: true },
  formatDateDisplay: { type: Function, required: true }
});

defineEmits(['location-change', 'date-change', 'swap-locations']);

const authStore = useAuthStore();
</script>
