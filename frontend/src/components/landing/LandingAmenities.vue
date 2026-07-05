<template>
  <section class="py-10 md:py-16 px-4 md:px-8 bg-[#FDFBF7] relative overflow-hidden font-sans">
    
    <div class="max-w-[1400px] mx-auto relative z-10">
      
      <!-- Top Section: Header & Bus Image -->
      <div class="flex flex-col lg:flex-row items-center gap-8 lg:gap-12 mb-12 relative">
        <div class="w-full lg:w-5/12 z-10 pt-6">
          <div class="flex items-center gap-4 mb-4">
            <p class="text-[#B58A44] text-[10px] md:text-xs font-black uppercase tracking-[0.2em] m-0">Tiện ích vượt trội</p>
            <div class="h-[1px] w-10 bg-[#B58A44]/50"></div>
          </div>
          <h2 class="text-[2.2rem] md:text-[3.2rem] font-bold text-[#142A38] tracking-tight leading-[1.1] mb-6">
            Chuẩn mực <br/>
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-[#C79C56] to-[#E3C586]">di chuyển mới.</span>
          </h2>
          <p class="text-slate-500 text-[13px] md:text-sm leading-relaxed max-w-sm">
            Không chỉ là một chuyến đi. Chúng tôi tái định nghĩa sự thoải mái bằng những tiện ích vốn chỉ có ở khoang hạng thương gia.
          </p>
        </div>
        
        <div class="w-full lg:w-7/12 relative h-[220px] lg:h-[320px]">
          <img src="/images/luxury_bus.png" class="w-full h-full object-cover object-center [mask-image:linear-gradient(to_right,transparent,black_40%)]" alt="Luxury VIP Bus" />
        </div>
      </div>

      <!-- Bento Grid Section -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
        
        <!-- Card 1: Seats (Span 2) -->
        <div class="lg:col-span-2 md:col-span-2 bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex flex-col justify-between relative overflow-hidden group">
          <div class="absolute top-0 right-0 w-[55%] h-full">
            <img src="/images/vip_seats.png" class="w-full h-full object-cover object-left" alt="VIP Seats" />
            <div class="absolute inset-0 bg-gradient-to-r from-white via-white/80 to-transparent w-[40%] pointer-events-none"></div>
          </div>
          
          <div class="relative z-10 w-[60%] flex flex-col h-full">
            <div class="w-12 h-12 rounded-2xl bg-white border border-slate-100 shadow-sm flex items-center justify-center mb-6 group-hover:-translate-y-1 transition-transform">
              <span class="material-symbols-outlined text-[#B58A44] text-[22px]">airline_seat_recline_extra</span>
            </div>
            <div>
              <h3 class="text-[#142A38] font-bold text-lg mb-2">Ghế Khoang Thương Gia</h3>
              <p class="text-slate-500 text-xs leading-relaxed max-w-[280px]">Nội thất bọc da thật cao cấp, thiết kế công thái học ngả sâu lên đến 180 độ mang lại sự thoải mái tuyệt đối cho từng hành khách.</p>
            </div>
          </div>
        </div>

        <!-- Card 2: Wi-Fi -->
        <div class="bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex flex-col justify-between group relative overflow-hidden">
          <div class="absolute -right-6 -top-6 opacity-5 group-hover:scale-110 transition-transform duration-500 pointer-events-none">
             <span class="material-symbols-outlined text-[100px] text-emerald-600">wifi</span>
          </div>
          <div class="relative z-10">
            <div class="w-12 h-12 rounded-2xl bg-white border border-slate-100 shadow-sm flex items-center justify-center mb-6 group-hover:-translate-y-1 transition-transform">
              <span class="material-symbols-outlined text-emerald-600 text-[22px]">wifi</span>
            </div>
            <h3 class="text-[#142A38] font-bold text-base mb-2">Phủ sóng Wi-Fi 5G</h3>
            <p class="text-slate-500 text-xs leading-relaxed">Làm việc từ xa hay phát trực tuyến mượt mà dọc theo suốt chiều dài chuyến đi.</p>
          </div>
        </div>

        <!-- Card 3: TV -->
        <div class="bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex flex-col justify-between group relative overflow-hidden">
          <div class="absolute -right-6 -top-6 opacity-5 group-hover:scale-110 transition-transform duration-500 pointer-events-none">
             <span class="material-symbols-outlined text-[100px] text-emerald-600">movie</span>
          </div>
          <div class="relative z-10">
            <div class="w-12 h-12 rounded-2xl bg-white border border-slate-100 shadow-sm flex items-center justify-center mb-6 group-hover:-translate-y-1 transition-transform">
              <span class="material-symbols-outlined text-emerald-600 text-[22px]">tv</span>
            </div>
            <h3 class="text-[#142A38] font-bold text-base mb-2">Giải trí cá nhân</h3>
            <p class="text-slate-500 text-xs leading-relaxed">Màn hình sắc nét tích hợp tai nghe chống ồn. Kho phim ảnh và âm nhạc được cập nhật liên tục.</p>
          </div>
        </div>

        <!-- Card 4: Power -->
        <div class="bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex flex-col justify-between group relative overflow-hidden">
          <div class="absolute -right-6 -bottom-6 opacity-5 group-hover:scale-110 transition-transform duration-500 pointer-events-none">
             <span class="material-symbols-outlined text-[100px] text-[#B58A44]">bolt</span>
          </div>
          <div class="relative z-10">
            <div class="w-12 h-12 rounded-2xl bg-white border border-slate-100 shadow-sm flex items-center justify-center mb-6 group-hover:-translate-y-1 transition-transform">
              <span class="material-symbols-outlined text-[#B58A44] text-[22px]">bolt</span>
            </div>
            <h3 class="text-[#142A38] font-bold text-base mb-2">Nguồn điện tại chỗ</h3>
            <p class="text-slate-500 text-xs leading-relaxed">Trạm sạc đa cổng (220V, Type-C) được bố trí ngay tại vị trí để tay. Sẵn sàng cho mọi thiết bị.</p>
          </div>
        </div>

        <!-- Card 5: Shield -->
        <div class="bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex flex-col justify-between group relative overflow-hidden">
          <div class="absolute -right-5 -bottom-5 opacity-5 group-hover:scale-110 transition-transform duration-500 pointer-events-none">
             <span class="material-symbols-outlined text-[110px] text-emerald-600">health_and_safety</span>
          </div>
          <div class="relative z-10">
            <div class="w-12 h-12 rounded-2xl bg-white border border-slate-100 shadow-sm flex items-center justify-center mb-6 group-hover:-translate-y-1 transition-transform">
              <span class="material-symbols-outlined text-emerald-600 text-[22px]">health_and_safety</span>
            </div>
            <h3 class="text-[#142A38] font-bold text-base mb-2">An tâm tuyệt đối</h3>
            <p class="text-slate-500 text-xs leading-relaxed">Gói bảo hiểm toàn diện đã được tự động kích hoạt ngay khi bạn hoàn tất đặt vé.</p>
          </div>
        </div>
        
        <!-- Card 6: Service (Span 2) -->
        <div class="lg:col-span-2 md:col-span-2 bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex flex-col justify-between relative overflow-hidden group">
          <div class="absolute bottom-0 right-0 w-[55%] h-full">
            <img src="/images/amenity_tray.png" class="w-full h-full object-cover object-center" alt="Premium Service" />
            <div class="absolute inset-0 bg-gradient-to-r from-white via-white/80 to-transparent w-[40%] pointer-events-none"></div>
          </div>
          
          <div class="relative z-10 w-[60%] flex flex-col h-full">
            <div class="w-12 h-12 rounded-2xl bg-white border border-slate-100 shadow-sm flex items-center justify-center mb-6 group-hover:-translate-y-1 transition-transform">
              <span class="material-symbols-outlined text-[#B58A44] text-[22px]">local_cafe</span>
            </div>
            <div>
              <h3 class="text-[#142A38] font-bold text-lg mb-2">Phục vụ chu đáo</h3>
              <p class="text-slate-500 text-xs leading-relaxed max-w-[280px]">Nước tinh khiết, khăn lạnh và thức ăn nhẹ luôn sẵn sàng. Đội ngũ nhân viên túc trực 24/7 trên từng chuyến đi.</p>
            </div>
          </div>
        </div>

      </div>
    </div>
  </section>
</template>

<script setup>
</script>
