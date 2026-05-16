<template>
  <footer class="bg-[#075955] text-white pt-16 pb-8 mt-12">
    <div class="max-w-7xl mx-auto px-4 md:px-8">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
        <div class="space-y-6">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-white rounded-lg flex items-center justify-center shadow-lg">
              <span class="material-symbols-outlined text-[#075955] text-2xl">directions_bus</span>
            </div>
            <span class="text-2xl font-black tracking-tighter">Saomaifly</span>
          </div>
          <p class="text-sm text-gray-300 leading-relaxed font-medium">
            Nền tảng đặt vé xe khách hàng đầu Việt Nam. Chúng tôi cung cấp giải pháp đặt vé trực tuyến an toàn, nhanh chóng và tiện lợi nhất cho mọi hành trình của bạn.
          </p>
          <div class="flex gap-4">
            <a href="#" class="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center hover:bg-white/20 transition-all"><i class="fab fa-facebook-f text-sm"></i></a>
            <a href="#" class="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center hover:bg-white/20 transition-all"><i class="fab fa-instagram text-sm"></i></a>
            <a href="#" class="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center hover:bg-white/20 transition-all"><i class="fab fa-youtube text-sm"></i></a>
          </div>
        </div>

        <div>
          <h4 class="font-bold text-white mb-6 uppercase text-xs tracking-[0.2em]">Khám phá</h4>
          <ul class="space-y-4 text-sm text-gray-300 font-medium">
            <li><a href="#" class="hover:text-white transition-colors flex items-center gap-2"><span class="w-1 h-1 bg-white/30 rounded-full"></span> Về Saomaifly</a></li>
            <li><a href="#" class="hover:text-white transition-colors flex items-center gap-2"><span class="w-1 h-1 bg-white/30 rounded-full"></span> Tin tức & Sự kiện</a></li>
            <li><a href="#" class="hover:text-white transition-colors flex items-center gap-2"><span class="w-1 h-1 bg-white/30 rounded-full"></span> Tuyển dụng</a></li>
            <li><a href="#" class="hover:text-white transition-colors flex items-center gap-2"><span class="w-1 h-1 bg-white/30 rounded-full"></span> Liên hệ hợp tác</a></li>
          </ul>
        </div>

        <div>
          <h4 class="font-bold text-white mb-6 uppercase text-xs tracking-[0.2em]">Hỗ trợ khách hàng</h4>
          <ul class="space-y-4 text-sm text-gray-300 font-medium">
            <li><a href="#" class="hover:text-white transition-colors">Hướng dẫn đặt vé</a></li>
            <li><a href="#" class="hover:text-white transition-colors">Chính sách bảo mật</a></li>
            <li><a href="#" class="hover:text-white transition-colors">Câu hỏi thường gặp</a></li>
            <li><a href="#" class="hover:text-white transition-colors">Tra cứu mã vé</a></li>
          </ul>
        </div>

        <div>
          <h4 class="font-bold text-white mb-6 uppercase text-xs tracking-[0.2em]">Liên hệ ngay</h4>
          <ul class="space-y-5 text-sm text-gray-300 font-medium">
            <li class="flex items-start gap-3">
              <span class="material-symbols-outlined text-xl text-white/60">location_on</span>
              <span class="leading-snug">123 Nguyễn Văn Linh, Quận Thanh Khê, Đà Nẵng</span>
            </li>
            <li class="flex items-center gap-3">
              <span class="material-symbols-outlined text-xl text-white/60">call</span>
              <span class="text-xl font-black text-white">1900.59.99.97</span>
            </li>
            <li class="flex items-center gap-3">
              <span class="material-symbols-outlined text-xl text-white/60">mail</span>
              <span class="hover:text-white transition-colors cursor-pointer">support@saomaifly.com</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="flex flex-col md:flex-row justify-between items-center pt-8 border-t border-white/10 gap-6">
        <div class="flex items-center gap-8 text-xs font-bold uppercase tracking-wider text-gray-400">
          <a href="#" class="hover:text-white transition-colors">Điều khoản sử dụng</a>
          <a href="#" class="hover:text-white transition-colors">Chính sách thanh toán</a>
          <a href="#" class="hover:text-white transition-colors">Cookie</a>
        </div>

        <div class="flex items-center gap-3">
          <div class="h-8 bg-white border border-gray-200 rounded px-2 flex items-center justify-center">
            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/1200px-Visa_Inc._logo.svg.png" alt="Visa" class="h-3 object-contain" />
          </div>
          <div class="h-8 bg-white border border-gray-200 rounded px-2 flex items-center justify-center">
            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Mastercard-logo.svg/1200px-Mastercard-logo.svg.png" alt="Mastercard" class="h-4 object-contain" />
          </div>
          <div class="h-8 bg-white border border-gray-200 rounded px-2 flex items-center justify-center">
             <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/4/40/JCB_logo.svg/1200px-JCB_logo.svg.png" alt="JCB" class="h-4 object-contain" />
          </div>
        </div>
      </div>
      
      <div class="text-center mt-6 text-xs text-gray-400">
        © 2026 Saomaifly. All rights reserved. Nền tảng đặt vé xe uy tín nhất.
      </div>
    </div>
  </footer>
</template>

<script setup>
// Không cần logic gì đặc biệt cho footer tĩnh
</script>