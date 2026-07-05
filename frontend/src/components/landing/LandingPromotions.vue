<template>
  <section class="py-16 px-6 bg-white relative z-10">
    <div class="max-w-[1200px] mx-auto">
      <div class="flex items-end justify-between mb-10">
        <div>
          <h2 class="text-3xl font-black text-zinc-900 tracking-tight mb-2">Ưu đãi nổi bật</h2>
          <p class="text-zinc-500">Chớp ngay các mã giảm giá độc quyền dành riêng cho bạn</p>
        </div>
        <a href="#" class="text-emerald-600 font-bold hover:text-emerald-700 hover:underline hidden sm:block">Xem tất cả →</a>
      </div>

      <!-- Khối Grid chứa các thẻ Khuyến Mãi -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        
        <!-- Promo Card 1 -->
        <div class="bg-gradient-to-br from-emerald-500 to-teal-700 rounded-2xl p-6 text-white shadow-lg hover:-translate-y-1 transition-transform cursor-pointer relative overflow-hidden group">
          <div class="absolute right-0 top-0 w-32 h-32 bg-white/10 rounded-bl-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
          <span class="inline-block px-3 py-1 bg-white/20 backdrop-blur-md rounded-full text-xs font-bold uppercase tracking-widest mb-4">Khách mới</span>
          <h3 class="text-2xl font-black mb-2 drop-shadow-sm">Giảm 20%</h3>
          <p class="text-emerald-50 mb-6 text-sm opacity-90">Áp dụng cho lần đầu đặt vé qua hệ thống SmartBus</p>
          <div class="flex items-center justify-between mt-auto">
            <span class="font-mono bg-black/20 px-3 py-1.5 rounded text-sm font-bold tracking-widest border border-white/20">NEW20</span>
            <button class="bg-white text-emerald-700 px-4 py-2 rounded-full text-xs font-bold uppercase hover:bg-emerald-50 transition-colors">Copy mã</button>
          </div>
        </div>

        <!-- Promo Card 2 -->
        <div class="bg-gradient-to-br from-amber-400 to-orange-500 rounded-2xl p-6 text-white shadow-lg hover:-translate-y-1 transition-transform cursor-pointer relative overflow-hidden group">
          <div class="absolute right-0 top-0 w-32 h-32 bg-white/10 rounded-bl-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
          <span class="inline-block px-3 py-1 bg-white/20 backdrop-blur-md rounded-full text-xs font-bold uppercase tracking-widest mb-4">Mùa Lễ Hội</span>
          <h3 class="text-2xl font-black mb-2 drop-shadow-sm">Hoàn tiền 50K</h3>
          <p class="text-amber-50 mb-6 text-sm opacity-90">Khi thanh toán qua ví VNPay hoặc Momo cho tuyến Đà Lạt</p>
          <div class="flex items-center justify-between mt-auto">
            <span class="font-mono bg-black/20 px-3 py-1.5 rounded text-sm font-bold tracking-widest border border-white/20">FESTIVAL</span>
            <button class="bg-white text-amber-600 px-4 py-2 rounded-full text-xs font-bold uppercase hover:bg-amber-50 transition-colors">Copy mã</button>
          </div>
        </div>

        <!-- Promo Card 3 -->
        <div class="bg-gradient-to-br from-zinc-800 to-zinc-900 rounded-2xl p-6 text-white shadow-lg hover:-translate-y-1 transition-transform cursor-pointer relative overflow-hidden group">
          <div class="absolute right-0 top-0 w-32 h-32 bg-white/5 rounded-bl-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
          <span class="inline-block px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-bold uppercase tracking-widest mb-4 text-emerald-400">Thành viên VIP</span>
          <h3 class="text-2xl font-black mb-2 drop-shadow-sm">Đồng giá 199K</h3>
          <p class="text-zinc-400 mb-6 text-sm opacity-90">Độc quyền cho tài khoản Vàng trên toàn hệ thống Limousine</p>
          <div class="flex items-center justify-between mt-auto">
            <span class="font-mono bg-black/30 px-3 py-1.5 rounded text-sm font-bold tracking-widest border border-white/10 text-zinc-300">VIP199K</span>
            <button class="bg-emerald-600 text-white px-4 py-2 rounded-full text-xs font-bold uppercase hover:bg-emerald-500 transition-colors">Áp dụng</button>
          </div>
        </div>

      </div>
      
      <!-- Hướng dẫn 3 bước đặt vé siêu nhanh -->
      <div class="mt-20 pt-16 border-t border-zinc-100">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-black text-zinc-900 tracking-tight mb-2">Trải nghiệm đặt vé thông minh</h2>
          <p class="text-zinc-500">Chỉ với 3 thao tác đơn giản, giữ chỗ ngay lập tức</p>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-10 text-center relative">
          <!-- Đường line nối các bước (ẩn trên mobile) -->
          <div class="hidden md:block absolute top-8 left-[16%] right-[16%] h-[2px] bg-zinc-100 -z-10"></div>
          
          <!-- Bước 1 -->
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4 shadow-sm border border-emerald-100">
              <span class="material-symbols-outlined text-[32px]">search</span>
            </div>
            <h4 class="font-bold text-lg text-zinc-900 mb-2">1. Tìm kiếm lộ trình</h4>
            <p class="text-sm text-zinc-500 leading-relaxed px-4">Nhập điểm đi, điểm đến và thời gian. Hệ thống sẽ gợi ý các chuyến xe trống ghế.</p>
          </div>

          <!-- Bước 2 -->
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mb-4 shadow-sm border border-amber-100">
              <span class="material-symbols-outlined text-[32px]">event_seat</span>
            </div>
            <h4 class="font-bold text-lg text-zinc-900 mb-2">2. Chọn ghế yêu thích</h4>
            <p class="text-sm text-zinc-500 leading-relaxed px-4">Xem sơ đồ xe trực quan, nhấp chọn ghế VIP, ghế thường và xem giá tức thì.</p>
          </div>

          <!-- Bước 3 -->
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center mb-4 shadow-sm border border-blue-100">
              <span class="material-symbols-outlined text-[32px]">credit_score</span>
            </div>
            <h4 class="font-bold text-lg text-zinc-900 mb-2">3. Thanh toán & Nhận vé</h4>
            <p class="text-sm text-zinc-500 leading-relaxed px-4">Thanh toán an toàn qua VNPAY, MOMO. Mã vé điện tử sẽ được gửi ngay qua Email/SMS.</p>
          </div>
        </div>
      </div>
      
    </div>
  </section>
</template>

<script setup>
</script>
