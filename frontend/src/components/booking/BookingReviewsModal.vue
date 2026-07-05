<template>
  <div class="fixed inset-0 z-[1000] bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-slate-50 w-full max-w-3xl h-[75vh] rounded-[2rem] overflow-hidden shadow-[0_20px_50px_rgba(0,0,0,0.3)] flex flex-col relative animate-scale-up">
      
      <!-- Premium Header -->
      <div class="bg-gradient-to-r from-amber-500 to-orange-500 px-6 py-5 flex justify-between items-center relative overflow-hidden shrink-0">
        <!-- Decorative circle -->
        <div class="absolute -right-10 -top-10 w-32 h-32 bg-white opacity-10 rounded-full blur-2xl"></div>
        <div class="absolute -left-10 -bottom-10 w-24 h-24 bg-amber-300 opacity-20 rounded-full blur-xl"></div>
        
        <div class="relative z-10 flex items-center gap-4">
           <div class="flex items-center justify-center w-12 h-12 bg-white/20 backdrop-blur-sm rounded-xl text-white shadow-inner shrink-0">
             <span class="material-symbols-outlined text-[24px]">reviews</span>
           </div>
           <div>
             <h3 class="text-lg sm:text-xl font-black text-white tracking-tight leading-tight">
               Đánh giá Nhà xe <span class="text-amber-100">{{ trip?.companyName }}</span>
               <span class="text-xs sm:text-sm font-medium opacity-80 ml-1">- {{ trip?.busType }}</span>
             </h3>
             <p class="text-[10px] font-bold text-amber-50/80 uppercase tracking-widest mt-1">
               Tổng hợp trải nghiệm từ khách hàng
             </p>
           </div>
        </div>
        
        <button @click="$emit('close')" class="relative z-10 w-8 h-8 rounded-full bg-black/10 hover:bg-black/20 text-white flex items-center justify-center transition-all backdrop-blur-md shrink-0 ml-4">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      
      <!-- Reviews List -->
      <div class="flex-1 overflow-y-auto p-6 md:p-8">
        <div v-if="reviewsLoading" class="flex flex-col items-center justify-center h-full text-amber-500">
           <div class="w-12 h-12 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mb-4 shadow-lg"></div>
           <span class="text-sm font-bold uppercase tracking-widest text-slate-400">Đang tải dữ liệu...</span>
        </div>
        
        <div v-else-if="currentReviews.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400">
           <div class="w-24 h-24 bg-slate-100 rounded-full flex items-center justify-center mb-4">
             <span class="material-symbols-outlined text-5xl text-slate-300">speaker_notes_off</span>
           </div>
           <span class="text-base font-bold text-slate-600">Chưa có đánh giá nào!</span>
           <span class="text-sm font-medium text-slate-400 mt-1">Nhà xe này hiện chưa nhận được phản hồi từ khách hàng.</span>
        </div>
        
        <div v-else class="space-y-0 divide-y divide-slate-200 bg-white px-2">
           <div v-for="review in currentReviews" :key="review.id" class="py-6 flex gap-4">
              
              <!-- Avatar -->
              <div class="w-10 h-10 rounded-full bg-blue-400 text-white flex items-center justify-center font-bold text-lg shrink-0">
                {{ review.user?.fullName?.charAt(0).toUpperCase() || 'U' }}
              </div>
              
              <!-- Content -->
              <div class="flex-1">
                 <div class="flex items-center flex-wrap gap-x-2 gap-y-1 mb-1">
                    <span class="font-semibold text-slate-800 text-[15px] uppercase">{{ review.user?.fullName || 'Người dùng ẩn danh' }}</span>
                    <div class="flex items-center text-emerald-600 text-[13px] font-medium gap-1">
                       <span class="material-symbols-outlined text-[16px]">check_circle</span>
                       Đã đi • {{ formatDateDisplay(review.createdAt) }}
                    </div>
                 </div>
                 
                 <div class="flex items-center gap-0.5 mb-2">
                    <span v-for="s in 5" :key="s" class="material-symbols-outlined text-[18px]" :class="s <= review.rating ? 'text-amber-400' : 'text-slate-200'" style="font-variation-settings: 'FILL' 1;">star</span>
                 </div>
                 
                 <p class="text-[14px] text-slate-800 leading-relaxed mb-3">
                   {{ review.comment || 'Khách hàng không để lại bình luận.' }}
                 </p>

                 <div class="flex flex-wrap gap-x-6 gap-y-1 text-[12px] text-slate-400 mb-4 font-medium">
                    <span>Loại xe: {{ review.busType || trip?.busType }}</span>
                    <span>Nhà xe: {{ review.companyName || trip?.companyName }}</span>
                 </div>
                 
                 <!-- Hiển thị phản hồi của Admin -->
                 <div v-if="review.adminReply" class="bg-slate-50 rounded-lg p-4 border border-slate-100">
                   <div class="font-bold text-slate-800 mb-1.5 text-[14px]">Phản hồi của nhà xe</div>
                   <p class="text-[14px] text-slate-600 leading-relaxed">{{ review.adminReply }}</p>
                 </div>
              </div>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const props = defineProps({
  trip: { type: Object, required: true }
});

defineEmits(['close']);

const api = useApi();
const reviewsLoading = ref(true);
const currentReviews = ref([]);

const formatDateDisplay = (d) => {
  if (!d) return '';
  return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

onMounted(async () => {
  if (!props.trip) return;
  try {
    const res = await api.get(`/reviews/company/${props.trip.companyName}`, {
      params: { busType: props.trip.busType }
    });
    currentReviews.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (e) {
    console.error("Lỗi lấy danh sách đánh giá:", e);
  } finally {
    reviewsLoading.value = false;
  }
});
</script>
