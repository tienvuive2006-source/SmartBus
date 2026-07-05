<template>
  <section class="py-12 md:py-16 px-6 max-w-[1400px] mx-auto relative z-10">
    <div class="bg-white rounded-[2rem] shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-zinc-100 overflow-hidden flex flex-col lg:flex-row">
      <!-- Left: Carousel -->
      <div class="lg:w-[55%] relative min-h-[250px] sm:min-h-[300px] lg:min-h-[350px] overflow-hidden group">
        <!-- Images -->
        <transition-group name="fade" tag="div">
          <div v-for="(img, index) in images" :key="img" 
               v-show="currentIndex === index"
               class="absolute inset-0 w-full h-full">
            <img :src="img" class="w-full h-full object-cover transition-transform duration-[1.5s] ease-out group-hover:scale-105" alt="Xe Limousine Trung Nam" />
            
            <!-- Soft Gradient Overlay to make arrows visible -->
            <div class="absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-transparent opacity-50"></div>
          </div>
        </transition-group>
        
        <!-- Controls -->
        <button @click="prevImage" class="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/40 hover:bg-black/70 text-white flex items-center justify-center backdrop-blur-sm transition-all z-20 border border-white/10 cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">chevron_left</span>
        </button>
        <button @click="nextImage" class="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/40 hover:bg-black/70 text-white flex items-center justify-center backdrop-blur-sm transition-all z-20 border border-white/10 cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">chevron_right</span>
        </button>
        
        <!-- Indicators -->
        <div class="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-20 bg-black/30 px-3 py-1.5 rounded-full backdrop-blur-md border border-white/10">
          <button v-for="(_, index) in images" :key="index"
                  @click="currentIndex = index"
                  class="h-2 rounded-full transition-all duration-300 border-none cursor-pointer p-0"
                  :class="currentIndex === index ? 'bg-red-500 w-4 shadow-[0_0_8px_rgba(239,68,68,0.8)]' : 'bg-white/60 hover:bg-white w-2'"></button>
        </div>
      </div>
      
      <!-- Right: Content -->
      <div class="lg:w-[45%] p-8 md:p-12 flex flex-col justify-center bg-white/50 backdrop-blur-sm">
        <div class="inline-block mb-3">
          <span class="text-[#b91c1c] text-xs font-black uppercase tracking-[0.2em]">ĐỘI XE TRUNG NAM LIMOUSINE</span>
        </div>
        <h2 class="text-[1.75rem] md:text-[2.25rem] font-bold text-zinc-900 tracking-tight leading-[1.1] mb-6">
          Xe Limousine hiện đại
        </h2>
        
        <ul class="space-y-4 mb-10">
          <li v-for="(feature, idx) in features" :key="idx" class="flex items-start gap-3">
            <span class="material-symbols-outlined text-[#b91c1c] font-bold text-[18px] mt-1 shrink-0">check</span>
            <span class="text-zinc-700 font-medium text-[15px] md:text-base">{{ feature }}</span>
          </li>
        </ul>
        
        <div>
          <button @click="showModal = true" class="px-8 py-3.5 bg-[#b91c1c] hover:bg-[#991b1b] text-white rounded-lg font-bold text-sm tracking-widest uppercase transition-all shadow-xl shadow-red-900/20 border-none cursor-pointer hover:-translate-y-1 active:translate-y-0 flex items-center gap-2 w-max">
            Xem chi tiết
            <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
          </button>
        </div>
      </div>
    </div>

    <!-- PREMIUM DETAILS MODAL -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-400 ease-out" enter-from-class="opacity-0 scale-95" enter-to-class="opacity-100 scale-100" leave-active-class="transition duration-300 ease-in" leave-from-class="opacity-100 scale-100" leave-to-class="opacity-0 scale-95">
        <div v-if="showModal" class="fixed inset-0 z-[1000] flex items-center justify-center p-4 sm:p-6 md:p-10">
          <!-- Backdrop -->
          <div class="absolute inset-0 bg-zinc-900/60 backdrop-blur-md transition-opacity" @click="showModal = false"></div>
          
          <!-- Modal Content -->
          <div class="relative w-full max-w-5xl bg-white rounded-[2rem] shadow-2xl overflow-hidden flex flex-col md:flex-row max-h-[90vh] md:max-h-[80vh] z-10 border border-white/20">
            <!-- Close Button -->
            <button @click="showModal = false" class="absolute top-4 right-4 z-20 w-10 h-10 bg-white/80 hover:bg-white rounded-full flex items-center justify-center backdrop-blur-md transition-all cursor-pointer border border-zinc-200 shadow-sm hover:scale-110 active:scale-95">
              <span class="material-symbols-outlined text-zinc-900">close</span>
            </button>

            <!-- Left: Hero Image Gallery -->
            <div class="md:w-1/2 relative bg-zinc-100 min-h-[250px] md:min-h-full">
              <img :src="modalImages[modalCurrentIndex]" class="absolute inset-0 w-full h-full object-cover" alt="Limousine Interior Detail" />
              <!-- Gradient to make text readable if any -->
              <div class="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent"></div>
              
              <!-- Mini Gallery Thumbnails -->
              <div class="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-3 p-2.5 bg-black/40 backdrop-blur-md rounded-2xl border border-white/10 w-max max-w-[90%] overflow-x-auto scrollbar-hide z-20 shadow-2xl">
                <div v-for="(img, idx) in modalImages" :key="idx" 
                     @click="modalCurrentIndex = idx"
                     class="w-20 h-14 sm:w-24 sm:h-16 rounded-xl overflow-hidden cursor-pointer transition-all duration-300 shrink-0 relative"
                     :class="modalCurrentIndex === idx ? 'ring-2 ring-white ring-offset-2 ring-offset-black/50 scale-105 z-10' : 'hover:scale-105 z-0'">
                  <img :src="img" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 transition-colors duration-300" :class="modalCurrentIndex === idx ? 'bg-transparent' : 'bg-black/40 hover:bg-black/20'"></div>
                </div>
              </div>
            </div>

            <!-- Right: Amenities & Details -->
            <div class="md:w-1/2 p-6 sm:p-10 md:p-12 overflow-y-auto bg-white flex flex-col">
              <span class="text-[#b91c1c] text-xs font-black uppercase tracking-[0.2em] mb-2 block">Dòng xe cao cấp</span>
              <h3 class="text-3xl font-black text-zinc-900 tracking-tight mb-4">Limousine 32 Phòng</h3>
              
              <p class="text-zinc-600 leading-relaxed mb-6 font-medium text-sm md:text-[15px]">
                Trải nghiệm không gian cá nhân hoàn hảo với thiết kế 32 phòng nằm riêng biệt. Tích hợp công nghệ giải trí và tiện nghi chăm sóc sức khỏe, mang lại cảm giác như nghỉ dưỡng tại khách sạn 5 sao di động.
              </p>

              <!-- Technical Specs Box -->
              <div class="bg-zinc-50 border border-zinc-100 rounded-xl p-4 mb-8">
                <h4 class="text-xs font-bold text-zinc-900 uppercase tracking-widest mb-3">Thông số kỹ thuật & An toàn</h4>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs md:text-sm text-zinc-700">
                  <div class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Khung gầm Thaco Universe</div>
                  <div class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Phanh ABS & Chống trượt ESP</div>
                  <div class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Hệ thống treo bầu hơi êm ái</div>
                  <div class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Động cơ Weichai công nghệ Áo</div>
                </div>
              </div>

              <!-- Bento Grid Amenities -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-y-6 gap-x-4 mb-10 flex-1">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0 border border-red-100">
                    <span class="material-symbols-outlined text-[#b91c1c]">wifi</span>
                  </div>
                  <div>
                    <p class="font-bold text-sm text-zinc-900">WiFi Tốc Độ Cao</p>
                    <p class="text-[11px] text-zinc-500 font-medium">Miễn phí suốt hành trình</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0 border border-red-100">
                    <span class="material-symbols-outlined text-[#b91c1c]">airline_seat_recline_extra</span>
                  </div>
                  <div>
                    <p class="font-bold text-sm text-zinc-900">Ghế Massage</p>
                    <p class="text-[11px] text-zinc-500 font-medium">Tích hợp đa chế độ</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0 border border-red-100">
                    <span class="material-symbols-outlined text-[#b91c1c]">usb</span>
                  </div>
                  <div>
                    <p class="font-bold text-sm text-zinc-900">Cổng Sạc USB</p>
                    <p class="text-[11px] text-zinc-500 font-medium">Sạc nhanh 2.1A</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0 border border-red-100">
                    <span class="material-symbols-outlined text-[#b91c1c]">tv</span>
                  </div>
                  <div>
                    <p class="font-bold text-sm text-zinc-900">Màn Hình LCD</p>
                    <p class="text-[11px] text-zinc-500 font-medium">Giải trí đa phương tiện</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0 border border-red-100">
                    <span class="material-symbols-outlined text-[#b91c1c]">ac_unit</span>
                  </div>
                  <div>
                    <p class="font-bold text-sm text-zinc-900">Điều Hòa Riêng</p>
                    <p class="text-[11px] text-zinc-500 font-medium">Tùy chỉnh từng phòng</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0 border border-red-100">
                    <span class="material-symbols-outlined text-[#b91c1c]">local_drink</span>
                  </div>
                  <div>
                    <p class="font-bold text-sm text-zinc-900">Nước & Khăn Lạnh</p>
                    <p class="text-[11px] text-zinc-500 font-medium">Phục vụ tiêu chuẩn VIP</p>
                  </div>
                </div>
              </div>

              <!-- CTA -->
              <button @click="$router.push('/booking/search')" class="w-full py-4 mt-auto bg-zinc-900 hover:bg-black text-white rounded-xl font-black text-sm tracking-widest uppercase transition-all shadow-lg hover:-translate-y-1 active:translate-y-0 cursor-pointer">
                Tìm vé xe Limousine
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>

<script setup>
import { ref } from 'vue';

const showModal = ref(false);
const currentIndex = ref(0);
const modalCurrentIndex = ref(0);

const images = [
  '/fleet/Trung-Nam-Limousine.png',
  '/fleet/bus1.png',
  '/fleet/bus2.png',
  '/fleet/bus3.png'
];

const modalImages = [
  '/fleet/bus1.png',
  '/fleet/bus2.png',
  '/fleet/bus3.png'
];

const features = [
  '32 phòng riêng tư, rộng rãi.',
  'Ghế hạng thương gia cao cấp.',
  'Không gian sang trọng.',
  'Điều hòa mát lạnh.',
  'Vệ sinh sạch sẽ.'
];

const nextImage = () => {
  currentIndex.value = (currentIndex.value + 1) % images.length;
};

const prevImage = () => {
  currentIndex.value = (currentIndex.value - 1 + images.length) % images.length;
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.8s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
