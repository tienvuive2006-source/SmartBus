<template>
  <article class="bg-white rounded-[2rem] border border-zinc-100 hover:border-emerald-100 transition-all duration-500 shadow-[0_8px_30px_rgb(0,0,0,0.04)] hover:shadow-[0_20px_40px_-15px_rgba(5,150,105,0.15)] overflow-hidden flex flex-col group/card relative">
    
    <div class="flex flex-col md:flex-row">
      <!-- Cột Hình Ảnh (Trái) -->
      <div class="md:w-64 lg:w-80 xl:w-96 bg-zinc-900 relative overflow-hidden shrink-0 min-h-[200px] md:min-h-full">
         <img 
           :src="computedImageUrl" 
           class="absolute inset-0 w-full h-full object-cover group-hover/card:scale-105 group-hover/card:rotate-1 transition-all duration-700 opacity-90 group-hover/card:opacity-100" 
           @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=600'"
         />
         <!-- Gradient lót dưới text cho dễ đọc -->
         <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
         
         <!-- Badges Góc Trên -->
         <div class="absolute top-4 left-4 flex flex-col gap-2 z-10 items-start">
            <div class="bg-emerald-500/90 backdrop-blur-md text-white text-[10px] font-black px-2.5 py-1 rounded-lg flex items-center gap-1.5 shadow-lg border border-white/20 uppercase tracking-wider">
               <span class="material-symbols-outlined text-[12px]">verified</span> Xác nhận tức thì
            </div>
            <div v-if="companyStats[trip.companyName + '|' + trip.busType]?.averageRating" class="bg-amber-400/95 backdrop-blur-md text-zinc-900 text-[11px] font-black px-2.5 py-1 rounded-lg shadow-lg border border-white/20 flex items-center gap-1">
               <span class="material-symbols-outlined text-[12px]">star</span> {{ companyStats[trip.companyName + '|' + trip.busType].averageRating.toFixed(1) }}
               <span class="text-[9px] font-bold ml-0.5 opacity-70">({{ companyStats[trip.companyName + '|' + trip.busType].totalReviews }})</span>
            </div>
            <div v-else class="bg-amber-400/95 backdrop-blur-md text-zinc-900 text-[10px] font-black px-2.5 py-1 rounded-lg shadow-lg border border-white/20 flex items-center gap-1 uppercase tracking-wider">
               <span class="material-symbols-outlined text-[12px]">star</span> Mới
            </div>
         </div>
      </div>

      <!-- Cột Thông Tin Giữa -->
      <div class="flex-1 min-w-0 p-5 md:p-6 flex flex-col justify-between border-r border-zinc-100 bg-white">
         <div>
            <div class="flex justify-between items-start mb-5">
               <div>
                   <h3 class="text-lg md:text-xl font-black text-zinc-900 tracking-tight leading-none">{{ trip.companyName }}</h3>
                   <p class="text-[11px] font-bold text-zinc-400 mt-1 uppercase tracking-widest">{{ trip.busType.replace(/Luxyry/g, 'Luxury') }}</p>
                </div>
            </div>
            
            <div class="flex items-center gap-2 md:gap-4 mb-5">
               <div class="flex flex-col items-start shrink-0">
                  <span class="text-2xl font-black text-zinc-900 tracking-tighter">{{ trip.departureTime }}</span>
                   <span class="text-[10px] font-black text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md mt-1 uppercase tracking-widest border border-emerald-100">{{ formatDate(trip.departureDate) }}</span>
               </div>

               <div class="flex-1 flex flex-col items-center group relative pt-2 min-w-[60px]">
                  <span class="text-[8.5px] lg:text-[10px] font-bold text-zinc-400 mb-2 uppercase tracking-widest bg-white px-1 sm:px-2 relative z-10 whitespace-nowrap">{{ trip.duration }}</span>
                  <div class="w-full h-0.5 bg-zinc-100 rounded-full relative">
                     <!-- Điểm nối animation -->
                     <div class="absolute left-0 top-1/2 -translate-y-1/2 w-2 h-2 rounded-full border-2 border-[#075955] bg-white"></div>
                     <div class="absolute right-0 top-1/2 -translate-y-1/2 w-2 h-2 rounded-full bg-zinc-300"></div>
                     <!-- Thanh chạy -->
                     <div class="absolute left-0 top-0 h-full bg-gradient-to-r from-[#075955] to-emerald-400 w-0 group-hover/card:w-full transition-all duration-1000 ease-out rounded-full opacity-30"></div>
                     
                     <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2 z-10 text-zinc-300 group-hover/card:text-emerald-500 transition-colors duration-500">
                       <span class="material-symbols-outlined text-[16px]">directions_bus</span>
                     </div>
                  </div>
               </div>

               <div class="flex flex-col items-end shrink-0">
                  <span class="text-2xl font-black text-zinc-900 tracking-tighter">{{ trip.arrivalTime }}</span>
                  <span class="text-[10px] font-black text-zinc-400 mt-1 uppercase tracking-widest opacity-0 group-hover/card:opacity-100 transition-opacity">Đến nơi</span>
               </div>
            </div>

             <div class="flex items-center justify-between text-xs font-semibold text-zinc-600 bg-zinc-50 p-2.5 rounded-xl border border-zinc-100 gap-2">
                <span class="flex items-center gap-1.5 min-w-0 flex-1"><span class="material-symbols-outlined text-[14px] text-[#075955] shrink-0">trip_origin</span> <span class="truncate">{{ trip.departurePoint }}</span></span>
                <span class="text-zinc-300 material-symbols-outlined text-sm shrink-0">arrow_forward</span>
                <span class="flex items-center gap-1.5 justify-end min-w-0 flex-1"><span class="truncate text-right">{{ trip.arrivalPoint }}</span> <span class="material-symbols-outlined text-[14px] text-red-500 shrink-0">location_on</span></span>
             </div>
          </div>
          
          <div class="mt-4 pt-4 border-t border-zinc-100 flex flex-col gap-3">
             <div class="flex items-center justify-between">
                <div class="flex gap-4">
                   <button 
                     @click="trip.showInfo = !trip.showInfo"
                     class="flex items-center gap-1 text-[10px] font-bold text-zinc-500 hover:text-[#075955] transition-colors uppercase tracking-wider"
                   >
                     <span class="material-symbols-outlined text-[12px]">info</span> {{ trip.showInfo ? 'Đóng' : 'Thông tin' }}
                   </button>
                   <button 
                     @click="$emit('open-map', trip)"
                     class="flex items-center gap-1 text-[10px] font-bold text-zinc-500 hover:text-blue-600 transition-colors uppercase tracking-wider"
                   >
                     <span class="material-symbols-outlined text-[12px]">map</span> Lộ trình
                   </button>
                    <button 
                      @click="$emit('open-reviews', trip)"
                      class="flex items-center gap-1 text-[10px] font-bold text-zinc-500 hover:text-amber-500 transition-colors uppercase tracking-wider"
                    >
                      <span class="material-symbols-outlined text-[12px]">star</span> Đánh giá
                    </button>
                </div>
             </div>
          </div>
       </div>

      <!-- Cột Giá (Phải) -->
      <div class="md:w-48 lg:w-56 shrink-0 p-5 md:p-6 bg-zinc-50/50 flex flex-col justify-center items-center md:items-end relative overflow-hidden">
         <!-- Decal mờ trang trí -->
         <span class="material-symbols-outlined absolute -bottom-10 -right-10 text-[120px] text-zinc-100 -rotate-12 pointer-events-none select-none">confirmation_number</span>
         
         <div class="text-center md:text-right mb-5 relative z-10">
            <span class="hidden sm:inline-flex items-center gap-1 text-[9px] font-black text-white bg-red-500 px-2 py-0.5 rounded-md mb-2 uppercase tracking-widest shadow-sm">
               <span class="material-symbols-outlined text-[10px]">sell</span> Giảm 10%
            </span>
            <p class="text-2xl font-black text-[#075955] tracking-tight">{{ trip.price.toLocaleString() }}<span class="text-base ml-0.5 underline decoration-2 underline-offset-2">đ</span></p>
            <p class="text-[10px] font-black text-emerald-500 mt-1 uppercase tracking-widest flex items-center justify-center md:justify-end gap-1">
              <span class="w-1 h-1 rounded-full bg-emerald-500 animate-pulse"></span> Còn {{ trip.availableSeats }} chỗ
            </p>
         </div>
         
         <button 
           @click="!isTripPassed(trip) && $emit('select-trip', trip)"
           :class="[
             'w-full py-3 rounded-xl font-black text-[11px] uppercase tracking-widest transition-all duration-300 relative z-10 overflow-hidden group/btn shadow-md', 
             isTripPassed(trip) 
               ? 'bg-zinc-200 text-zinc-400 cursor-not-allowed shadow-none' 
               : 'bg-[#f03a17] hover:bg-[#d63314] text-white hover:shadow-[0_10px_20px_-10px_rgba(240,58,23,0.6)] active:scale-[0.98]'
           ]"
           :disabled="isTripPassed(trip)"
         >
           <span class="relative z-10 flex items-center justify-center gap-1.5">
             {{ isTripPassed(trip) ? 'Đã khởi hành' : 'Chọn chỗ ngay' }}
             <span v-if="!isTripPassed(trip)" class="material-symbols-outlined text-[14px] group-hover/btn:translate-x-1 transition-transform">arrow_forward</span>
           </span>
         </button>
         <p class="text-[8px] font-bold text-zinc-400 mt-3 uppercase tracking-widest text-center w-full relative z-10">Không cần thanh toán trước</p>
      </div>
    </div>
    
    <!-- Expandable Info (Giữ nguyên cấu trúc nhưng tinh chỉnh UI) -->
    <div v-if="trip.showInfo" class="p-5 md:p-6 bg-zinc-50 border-t border-zinc-100 animate-fade-in text-sm relative">
       <!-- Nút đóng -->
       <button @click="trip.showInfo = false" class="absolute top-4 right-4 w-8 h-8 rounded-full bg-zinc-200/50 hover:bg-zinc-200 text-zinc-600 flex items-center justify-center transition-all">
          <span class="material-symbols-outlined text-[18px]">close</span>
       </button>
       
       <div class="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          <div>
             <p class="text-[10px] font-black text-emerald-600 uppercase tracking-widest mb-3 flex items-center gap-2">
               <span class="material-symbols-outlined text-[16px] text-emerald-500">directions_bus</span> Phương tiện
             </p>
             <p class="text-xl font-black text-zinc-900 tracking-tight mb-4">{{ trip.busType.replace(/Luxyry/g, 'Luxury') }}</p>
             <div class="space-y-3">
                <div class="flex items-center justify-between bg-white px-4 py-2.5 rounded-lg border border-zinc-100 shadow-[0_2px_10px_rgb(0,0,0,0.02)]">
                   <span class="text-xs font-bold text-zinc-500 uppercase tracking-widest">Biển số</span>
                   <span class="text-sm font-black text-zinc-800">{{ trip.assignedLicensePlate || 'Chưa cập nhật' }}</span>
                </div>
                <div class="flex items-center justify-between bg-white px-4 py-2.5 rounded-lg border border-zinc-100 shadow-[0_2px_10px_rgb(0,0,0,0.02)]">
                   <span class="text-xs font-bold text-zinc-500 uppercase tracking-widest">Tài xế</span>
                   <span class="text-sm font-black text-zinc-800">{{ trip.assignedDriverFullName || 'Chưa phân công' }}</span>
                </div>
             </div>
          </div>
          <div>
             <p class="text-[10px] font-black text-amber-600 uppercase tracking-widest mb-3 flex items-center gap-2">
               <span class="material-symbols-outlined text-[16px] text-amber-500">stars</span> Tiện ích trên xe
             </p>
              <div class="grid grid-cols-2 gap-3 mt-4">
               <template v-if="getBusUtilities(trip.busType)">
                 <div v-for="(util, index) in getBusUtilities(trip.busType).split(',')" :key="index" class="text-[13px] font-bold text-zinc-700 flex items-center gap-2">
                   <div class="w-6 h-6 rounded-full bg-emerald-50 border border-emerald-100 flex items-center justify-center shrink-0">
                     <span class="material-symbols-outlined text-[14px] text-emerald-600">{{ getUtilityIcon(util.trim()) }}</span>
                   </div>
                   {{ util.trim() }}
                 </div>
               </template>
               <div v-else class="text-[13px] font-bold text-zinc-700 flex items-center gap-2">
                 <div class="w-6 h-6 rounded-full bg-emerald-50 border border-emerald-100 flex items-center justify-center shrink-0">
                   <span class="material-symbols-outlined text-[14px] text-emerald-600">check_circle</span>
                 </div>
                 Xe tiêu chuẩn
               </div>
             </div>
           </div>
       </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  trip: { type: Object, required: true },
  companyStats: { type: Object, required: true },
  allBuses: { type: Array, required: true },
  allBusTypes: { type: Array, required: true }
});

defineEmits(['open-map', 'open-reviews', 'select-trip']);

const router = useRouter();

const formatDate = (d) => {
  if (!d) return '';
  const datePart = d.split('T')[0];
  const [year, month, day] = datePart.split('-');
  return `${day}/${month}/${year}`;
};

const computedImageUrl = computed(() => {
  // First check if trip has a specific image (but maybe they updated the bus type)
  // Let's actually prioritize Bus or BusType images if they exist, or just use trip.imageUrl
  // If the user changed the BusType image, they want it to reflect. 
  const bus = props.allBuses.find(b => b.licensePlate === props.trip.assignedLicensePlate);
  const busImageUrl = bus?.imageUrl;
  
  const btName = props.trip.busType?.replace(/Luxyry/g, 'Luxury');
  const busType = props.allBusTypes.find(bt => bt.name === btName || bt.name.replace(/Luxyry/g, 'Luxury') === btName);
  const busTypeImageUrl = busType?.imageUrl;
  
  // Prioritize busType image or bus image over trip image because admin usually edits bus types
  return busTypeImageUrl || busImageUrl || props.trip.imageUrl || 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=600';
});

const isTripPassed = (trip) => {
  if (!trip || !trip.departureDate || !trip.departureTime) return false;
  try {
    const [year, month, day] = trip.departureDate.split('T')[0].split('-');
    const [hour, minute] = trip.departureTime.split(':');
    const depTime = new Date(year, month - 1, day, hour, minute);
    return new Date() > depTime;
  } catch (e) {
    return false;
  }
};


const getBusUtilities = (busTypeName) => {
  if (!busTypeName) return '';
  const btName = busTypeName.toLowerCase().trim().replace(/luxyry/g, 'luxury');
  
  const busType = props.allBusTypes.find(bt => {
    const dbName = bt.name.toLowerCase().trim().replace(/luxyry/g, 'luxury');
    return dbName === btName;
  });
  
  return busType ? busType.description : '';
};

const getUtilityIcon = (name) => {
  const map = {
    'Wifi tốc độ cao': 'wifi',
    'Cổng sạc USB': 'usb',
    'Tủ lạnh mini': 'kitchen',
    'Nhà vệ sinh': 'wc',
    'Ghế massage': 'airline_seat_recline_extra',
    'Màn hình Tivi': 'tv',
    'Tai nghe Bluetooth': 'headphones',
    'Nước uống & Khăn': 'local_drink'
  };
  return map[name] || 'check_circle';
};
</script>
