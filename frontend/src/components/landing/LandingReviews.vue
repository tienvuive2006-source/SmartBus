<template>
  <div v-if="topReviews.length > 0" class="w-full relative bg-[#FDFBF7] pt-16 overflow-hidden font-sans border-t border-[#F2EDE4]">
    
    <!-- Background Decor (Faint gradients/curves simulating the image bg) -->
    <div class="absolute inset-0 pointer-events-none overflow-hidden">
      <div class="absolute top-0 right-0 w-full h-[80%] bg-gradient-to-b from-[#FAF7F0] to-transparent"></div>
      <div class="absolute top-1/4 -left-1/4 w-[150%] h-[1px] bg-gradient-to-r from-transparent via-[#D4C3A3] to-transparent rotate-6 opacity-30"></div>
      <div class="absolute top-1/3 -left-1/4 w-[150%] h-[1px] bg-gradient-to-r from-transparent via-[#D4C3A3] to-transparent -rotate-3 opacity-20"></div>
    </div>

    <section class="relative z-10 px-4 md:px-8 max-w-[1400px] mx-auto pb-10">
      
      <!-- Headers -->
      <div class="text-center mb-8 md:mb-12">
        <div class="flex items-center justify-center gap-4 mb-3">
          <div class="h-[1px] w-10 bg-[#B58A44]/50"></div>
          <p class="text-[#075955] font-black text-[10px] md:text-[11px] tracking-[0.2em] uppercase m-0">Trải nghiệm thực tế</p>
          <div class="h-[1px] w-10 bg-[#B58A44]/50"></div>
        </div>
        <h2 class="text-[2rem] md:text-[2.75rem] font-bold tracking-tight leading-[1.1] text-[#142A38] mb-3">
          Khách hàng nói gì về <br/>
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-[#C79C56] to-[#E3C586]">Trung Nam</span>
        </h2>
        <p class="text-slate-500 text-xs md:text-sm max-w-md mx-auto">
          Sự hài lòng của khách hàng là động lực để chúng tôi không ngừng nâng cao chất lượng dịch vụ mỗi ngày.
        </p>
      </div>

      <!-- Main Showcase Area -->
      <div class="relative w-full mx-auto grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-10 min-h-[300px] mb-12 mt-10">
        
        <!-- The Review Cards -->
        <div v-for="review in topReviews.slice(0, 3)" :key="review.id" class="relative w-full transition-all duration-500 group">
          <div class="bg-white rounded-[24px] px-6 md:px-8 pb-8 pt-8 shadow-[0_4px_20px_rgb(0,0,0,0.03)] border border-[#F2EDE4] hover:-translate-y-1 hover:shadow-[0_12px_40px_rgba(212,163,82,0.15)] transition-all text-left flex flex-col h-full relative overflow-hidden">
            
            <!-- Golden Ribbon -->
            <div class="absolute top-0 left-6 md:left-8 w-12 h-14 bg-gradient-to-b from-[#C49B55] to-[#A37B3B] rounded-b-2xl shadow-md flex items-center justify-center">
              <span class="text-white text-4xl font-serif font-black leading-none mt-2">“</span>
            </div>

            <!-- Large Quote Watermark -->
            <span class="absolute top-2 right-4 text-[#F6F1E6] text-[100px] md:text-[120px] font-serif font-black leading-none select-none pointer-events-none group-hover:text-[#F0E5D1] transition-colors duration-500">”</span>

            <div class="relative z-10 flex flex-col h-full mt-1">
              <!-- Stars -->
              <div class="flex gap-1.5 mb-6 text-[#D4A352] pl-[60px] md:pl-[64px]">
                <span v-for="star in review.rating" :key="star" class="material-symbols-outlined text-[18px] md:text-[20px]" style="font-variation-settings: 'FILL' 1;">star</span>
              </div>
              
              <!-- Review Text -->
              <p class="font-bold text-[#142A38] text-[1.2rem] md:text-[1.35rem] leading-snug mb-8 flex-grow">
                “{{ review.comment }}”
              </p>
              
              <!-- Divider -->
              <div class="relative w-full h-[1px] bg-gradient-to-r from-transparent via-[#E0C08A]/60 to-transparent mb-6">
                <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-1.5 h-1 bg-white shadow-[0_0_6px_2px_rgba(212,163,82,0.8)] rounded-full"></div>
              </div>

              <!-- User Info -->
              <div class="flex items-center gap-4 mt-auto">
                <div class="w-12 h-12 rounded-full bg-[#113B36] text-white flex items-center justify-center font-bold text-lg border-[2px] border-[#D4A352] shadow-sm shrink-0">
                  {{ review.user?.fullName?.charAt(0)?.toUpperCase() || 'K' }}
                </div>
                <div class="min-w-0">
                  <p class="font-bold text-[15px] text-[#142A38] mb-1 truncate">
                    {{ review.user?.fullName || 'Khách hàng ẩn danh' }}
                  </p>
                  <p v-if="review.booking?.trip" class="text-[10px] md:text-[11px] font-semibold tracking-wider uppercase text-slate-500 truncate flex items-center gap-1">
                    <span class="material-symbols-outlined text-[#D4A352] text-[14px]">location_on</span>
                    {{ review.booking.trip.departurePoint?.split(',')[0] }} ➝ {{ review.booking.trip.arrivalPoint?.split(',')[0] }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>



      <!-- Trust Bar -->
      <div class="bg-white rounded-2xl border border-slate-100 shadow-[0_8px_30px_rgb(0,0,0,0.02)] p-2 max-w-5xl mx-auto">
        <div class="grid grid-cols-2 md:grid-cols-4 divide-x divide-slate-100">
          
          <div class="p-4 md:p-6 flex items-center gap-3 group">
            <div class="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center group-hover:bg-emerald-600 group-hover:text-white transition-colors">
              <span class="material-symbols-outlined text-[20px]">verified_user</span>
            </div>
            <div>
              <p class="font-bold text-[#142A38] text-xs md:text-sm">An toàn tuyệt đối</p>
              <p class="text-[10px] text-slate-500 hidden md:block">Cam kết an toàn trên mọi hành trình</p>
            </div>
          </div>

          <div class="p-4 md:p-6 flex items-center gap-3 group">
            <div class="w-10 h-10 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <span class="material-symbols-outlined text-[20px]">support_agent</span>
            </div>
            <div>
              <p class="font-bold text-[#142A38] text-xs md:text-sm">Hỗ trợ 24/7</p>
              <p class="text-[10px] text-slate-500 hidden md:block">Luôn sẵn sàng phục vụ</p>
            </div>
          </div>

          <div class="p-4 md:p-6 flex items-center gap-3 group">
            <div class="w-10 h-10 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center group-hover:bg-amber-600 group-hover:text-white transition-colors">
              <span class="material-symbols-outlined text-[20px]">workspace_premium</span>
            </div>
            <div>
              <p class="font-bold text-[#142A38] text-xs md:text-sm">Dịch vụ chất lượng</p>
              <p class="text-[10px] text-slate-500 hidden md:block">Trải nghiệm xứng tầm</p>
            </div>
          </div>

          <div class="p-4 md:p-6 flex items-center gap-3 group">
            <div class="w-10 h-10 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center group-hover:bg-rose-600 group-hover:text-white transition-colors">
              <span class="material-symbols-outlined text-[20px]">favorite</span>
            </div>
            <div>
              <p class="font-bold text-[#142A38] text-xs md:text-sm">Khách hàng là trọng tâm</p>
              <p class="text-[10px] text-slate-500 hidden md:block">Sự hài lòng của bạn là ưu tiên</p>
            </div>
          </div>

        </div>
      </div>
      
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();
const topReviews = ref([]);

const fetchTopReviews = async () => {
  try {
    const res = await api.get('/reviews/all');
    if (res.data && Array.isArray(res.data)) {
      topReviews.value = res.data
        .filter(r => r.rating >= 4 && r.comment && r.comment.trim().length >= 3)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 3); // Lấy 3 đánh giá tốt nhất
    }
  } catch (err) {
    console.error("Lỗi khi tải đánh giá nổi bật:", err);
  }
};

onMounted(() => {
  fetchTopReviews();
});
</script>
