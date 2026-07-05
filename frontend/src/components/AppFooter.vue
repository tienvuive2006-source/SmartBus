<template>
  <footer class="relative bg-[#075955] text-white pt-12 pb-4 overflow-hidden border-t border-white/10">
    <!-- Ambient glow -->
    <div class="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[300px] bg-amber-400/10 blur-[120px] rounded-full pointer-events-none"></div>

    <div class="relative z-10 max-w-[1400px] mx-auto px-6">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-8 lg:gap-6 mb-12">
        
        <!-- Column 1: Brand -->
        <div class="lg:col-span-4 flex flex-col items-start pr-0 lg:pr-12">
          <img src="/logo2.png" alt="Trung Nam Logo" class="h-10 md:h-12 w-auto object-contain mb-5 drop-shadow-lg" />
          <p class="text-white/70 text-[12px] leading-relaxed mb-5 font-light max-w-sm">
            Nền tảng đặt vé xe khách cao cấp hàng đầu. Trải nghiệm dịch vụ Limousine chuyên cơ mặt đất với tiện nghi tối đa, không gian riêng tư và phong cách phục vụ đẳng cấp.
          </p>
          <div class="flex gap-2.5">
            <a href="#" class="w-8 h-8 rounded-full border border-white/20 flex items-center justify-center text-white/70 hover:bg-amber-400 hover:text-[#075955] hover:border-amber-400 hover:scale-110 transition-all duration-500 shadow-sm">
              <i class="fab fa-facebook-f text-[10px]"></i>
            </a>
            <a href="#" class="w-8 h-8 rounded-full border border-white/20 flex items-center justify-center text-white/70 hover:bg-amber-400 hover:text-[#075955] hover:border-amber-400 hover:scale-110 transition-all duration-500 shadow-sm">
              <i class="fab fa-instagram text-[10px]"></i>
            </a>
            <a href="#" class="w-8 h-8 rounded-full border border-white/20 flex items-center justify-center text-white/70 hover:bg-amber-400 hover:text-[#075955] hover:border-amber-400 hover:scale-110 transition-all duration-500 shadow-sm">
              <i class="fab fa-youtube text-[10px]"></i>
            </a>
          </div>
        </div>

        <!-- Column 2 & 3: Links -->
        <div class="lg:col-span-2 lg:col-start-6">
          <h4 class="font-bold text-amber-400/90 text-[10px] tracking-[0.2em] uppercase mb-5">Khám phá</h4>
          <ul class="space-y-2">
            <li v-for="link in ['Về Trung Nam', 'Tin tức & Sự kiện', 'Đội xe Limousine', 'Tuyển dụng', 'Liên hệ đối tác']" :key="link">
              <a href="#" class="text-[11.5px] text-white/70 font-medium hover:text-white transition-colors relative group inline-flex items-center">
                <span class="w-0 h-[1px] bg-white absolute left-0 -bottom-1 transition-all duration-500 ease-out group-hover:w-full opacity-50"></span>
                {{ link }}
              </a>
            </li>
          </ul>
        </div>

        <div class="lg:col-span-2">
          <h4 class="font-bold text-amber-400/90 text-[10px] tracking-[0.2em] uppercase mb-5">Hỗ trợ</h4>
          <ul class="space-y-2">
            <li v-for="link in ['Hướng dẫn đặt vé', 'Chính sách bảo mật', 'Câu hỏi thường gặp', 'Quy định hành lý', 'Tra cứu hóa đơn']" :key="link">
              <a href="#" class="text-[11.5px] text-white/70 font-medium hover:text-white transition-colors relative group inline-flex items-center">
                <span class="w-0 h-[1px] bg-white absolute left-0 -bottom-1 transition-all duration-500 ease-out group-hover:w-full opacity-50"></span>
                {{ link }}
              </a>
            </li>
          </ul>
        </div>

        <!-- Column 4: Contact & Newsletter -->
        <div class="lg:col-span-3 lg:col-start-10 flex flex-col">
          <h4 class="font-bold text-amber-400/90 text-[10px] tracking-[0.2em] uppercase mb-5">Liên hệ</h4>
          <ul class="space-y-3 mb-6">
            <li class="flex items-start gap-3 group cursor-pointer">
              <span class="material-symbols-outlined text-[15px] text-white/50 group-hover:text-amber-300 transition-colors mt-0.5">location_on</span>
              <span class="text-[11.5px] text-white/70 font-medium group-hover:text-white transition-colors leading-relaxed">
                123 Nguyễn Văn Linh<br />Thanh Khê, Đà Nẵng
              </span>
            </li>
            <li class="flex items-center gap-3 group cursor-pointer">
              <span class="material-symbols-outlined text-[15px] text-white/50 group-hover:text-amber-300 transition-colors">call</span>
              <span class="text-[13px] text-white font-bold tracking-wider group-hover:text-amber-300 transition-colors">1900.59.99.97</span>
            </li>
            <li class="flex items-center gap-3 group cursor-pointer">
              <span class="material-symbols-outlined text-[15px] text-white/50 group-hover:text-amber-300 transition-colors">mail</span>
              <span class="text-[11.5px] text-white/70 font-medium group-hover:text-white transition-colors">support@trungnam.vn</span>
            </li>
          </ul>

          <div class="mt-auto">
            <p class="text-[8.5px] text-white/50 font-bold tracking-[0.15em] uppercase mb-2">Đăng ký nhận ưu đãi</p>
            <div class="relative group">
              <input type="email" placeholder="Email của bạn..." class="w-full bg-black/20 border border-white/10 rounded-xl py-2.5 pl-4 pr-11 text-[11px] text-white outline-none focus:border-amber-400/50 focus:bg-black/30 transition-all placeholder:text-white/40" />
              <button class="absolute right-1 top-1 bottom-1 w-8 bg-amber-400 hover:bg-amber-300 text-[#075955] rounded-[9px] flex items-center justify-center transition-colors shadow-sm">
                <span class="material-symbols-outlined text-xs font-bold">arrow_forward</span>
              </button>
            </div>
          </div>
        </div>

      </div>

      <!-- Bottom Bar -->
      <div class="flex flex-col-reverse md:flex-row items-center justify-between pt-5 border-t border-white/10 gap-4">
        <p class="text-white/50 text-[10px] font-medium">
          © {{ new Date().getFullYear() }} Trung Nam Limousine. All rights reserved.
        </p>

        <div class="flex items-center gap-4 text-[8.5px] font-bold tracking-[0.15em] uppercase text-white/50">
          <a href="#" class="hover:text-white transition-colors">Điều khoản</a>
          <a href="#" class="hover:text-white transition-colors">Bảo mật</a>
          <a href="#" class="hover:text-white transition-colors">Cookie</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>