<template>
  <div class="pb-8">
    <main class="max-w-2xl mx-auto space-y-6">
      <!-- Profile Header Section -->
      <section class="bg-surface-container-lowest p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex items-center gap-6">
        <div class="relative">
          <img alt="Profile Avatar" class="w-24 h-24 rounded-full object-cover border-4 border-surface-container" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDmchl0XYvd2BF96f3Jgx_T08BYD2BQOvpIgw9RrKHEW4Xi57atHQhL_Sht9AO17KZ0qn44pyiQtwb1KkIFfsPXDlLmU-Nfav-DJa8YRngKX8zvfZMhRD7ivoqI9mlwD8PDOZ3W__CiqzcCbzzqjzGbnhunAvD51fDnPEHzU5LVdBzWWTcG_0ExMHDymfEAwdG1wpjj4C0d0XfONntfwXgiWKRpFCLOcx193XTguLiU-g7FI9kNCxyQGgxPyiiEPZQ2T2EYzpXvQTbZ"/>
          <button class="absolute bottom-0 right-0 bg-primary p-1.5 rounded-full text-white shadow-lg active:scale-90 duration-100">
            <span class="material-symbols-outlined text-sm">edit</span>
          </button>
        </div>
        <div class="flex-1">
          <h2 class="font-headline-md text-headline-md text-on-surface">Nguyễn Minh Tâm</h2>
          <p class="font-body-md text-on-surface-variant flex items-center gap-1 mt-1">
            <span class="material-symbols-outlined text-[18px]">call</span>
            +84 908 123 456
          </p>
          <div class="mt-3 flex gap-2">
            <span class="bg-primary-fixed text-on-primary-fixed font-label-md text-label-md px-3 py-1 rounded-full">Hạng Vàng</span>
            <span class="bg-secondary-fixed text-on-secondary-fixed font-label-md text-label-md px-3 py-1 rounded-full">1,250 Điểm</span>
          </div>
        </div>
      </section>

      <!-- Wallet & Quick Actions -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Linked Wallet Card -->
        <section class="bg-primary-container text-on-primary-container p-6 rounded-xl shadow-lg relative overflow-hidden">
          <div class="relative z-10">
            <div class="flex justify-between items-start mb-4">
              <p class="font-label-md text-label-md opacity-90">Ví điện tử liên kết</p>
              <span class="material-symbols-outlined">account_balance_wallet</span>
            </div>
            <h3 class="font-headline-md text-headline-md font-bold mb-1">2,450,000 đ</h3>
            <p class="font-label-md text-label-md opacity-80">Liên kết với MoMo</p>
            <button class="mt-4 bg-white/20 hover:bg-white/30 text-white font-label-md text-label-md px-4 py-2 rounded-lg transition-colors active:scale-95 duration-100">
              Nạp tiền ngay
            </button>
          </div>
          <div class="absolute -right-4 -bottom-4 w-32 h-32 bg-white/10 rounded-full blur-2xl"></div>
        </section>

        <!-- My Offers Chip Grid -->
        <section class="bg-surface-container-lowest p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex flex-col justify-between">
          <div>
            <div class="flex justify-between items-center mb-4">
              <h3 class="font-headline-sm text-headline-sm text-on-surface">Ưu đãi của tôi</h3>
              <span class="text-primary font-label-md text-label-md font-bold cursor-pointer">Xem tất cả</span>
            </div>
            <div class="flex flex-wrap gap-2">
              <div class="flex items-center gap-2 bg-tertiary-fixed text-on-tertiary-fixed px-3 py-2 rounded-lg">
                <span class="material-symbols-outlined text-[18px]">sell</span>
                <span class="font-label-md text-label-md">-20% Vé xe</span>
              </div>
              <div class="flex items-center gap-2 bg-secondary-fixed text-on-secondary-fixed px-3 py-2 rounded-lg">
                <span class="material-symbols-outlined text-[18px]">local_activity</span>
                <span class="font-label-md text-label-md">Tặng 50k</span>
              </div>
            </div>
          </div>
          <p class="font-body-md text-on-surface-variant mt-4 italic">3 mã sẽ hết hạn trong 2 ngày</p>
        </section>
      </div>

      <!-- Settings List -->
      <section class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden">
        <div class="p-4 border-b border-surface-variant">
          <h3 class="font-headline-sm text-headline-sm text-on-surface">Cài đặt & Hỗ trợ</h3>
        </div>
        
        <div @click="$router.push('/notifications')" class="flex items-center justify-between p-4 hover:bg-surface-container transition-colors cursor-pointer group">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-primary group-hover:bg-primary-container group-hover:text-white transition-colors">
              <span class="material-symbols-outlined">notifications</span>
            </div>
            <div>
              <p class="font-body-lg text-body-lg text-on-surface">Cài đặt thông báo</p>
              <p class="font-body-md text-body-md text-on-surface-variant">Quản lý các thông báo từ ứng dụng</p>
            </div>
          </div>
          <span class="material-symbols-outlined text-on-surface-variant">chevron_right</span>
        </div>

        <div @click="$router.push('/ai-assistant')" class="flex items-center justify-between p-4 hover:bg-surface-container transition-colors cursor-pointer group">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-primary group-hover:bg-primary-container group-hover:text-white transition-colors">
              <span class="material-symbols-outlined">help</span>
            </div>
            <div>
              <p class="font-body-lg text-body-lg text-on-surface">Trung tâm hỗ trợ</p>
              <p class="font-body-md text-body-md text-on-surface-variant">Câu hỏi thường gặp & Chat hỗ trợ</p>
            </div>
          </div>
          <span class="material-symbols-outlined text-on-surface-variant">chevron_right</span>
        </div>

        <div class="flex items-center justify-between p-4 hover:bg-surface-container transition-colors cursor-pointer group">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-primary group-hover:bg-primary-container group-hover:text-white transition-colors">
              <span class="material-symbols-outlined">verified_user</span>
            </div>
            <div>
              <p class="font-body-lg text-body-lg text-on-surface">Chính sách bảo mật</p>
              <p class="font-body-md text-body-md text-on-surface-variant">Cách chúng tôi bảo vệ dữ liệu của bạn</p>
            </div>
          </div>
          <span class="material-symbols-outlined text-on-surface-variant">chevron_right</span>
        </div>

        <div @click="$router.push('/')" class="flex items-center justify-between p-4 hover:bg-error-container/30 transition-colors cursor-pointer group">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-full bg-error-container flex items-center justify-center text-error">
              <span class="material-symbols-outlined">logout</span>
            </div>
            <div>
              <p class="font-body-lg text-body-lg text-error">Đăng xuất</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Secondary CTA -->
      <section class="bg-surface-variant/50 p-6 rounded-xl border border-dashed border-outline-variant flex flex-col items-center text-center">
        <span class="material-symbols-outlined text-4xl text-primary mb-2">support_agent</span>
        <h4 class="font-headline-sm text-headline-sm text-on-surface">Bạn cần hỗ trợ gấp?</h4>
        <p class="font-body-md text-body-md text-on-surface-variant mb-4">Tổng đài hỗ trợ hành khách luôn trực tuyến 24/7 để giúp bạn có chuyến đi suôn sẻ nhất.</p>
        <button class="bg-primary text-on-primary font-body-lg text-body-lg px-8 py-3 rounded-full flex items-center gap-2 active:scale-95 transition-all">
          <span class="material-symbols-outlined">call</span>
          1900 6868
        </button>
      </section>

      <!-- Version Footer -->
      <footer class="pb-8 text-center">
        <p class="font-label-md text-label-md text-on-surface-variant opacity-50">TransLink Pro Version 2.4.0 • Made with ❤️ in Vietnam</p>
      </footer>
    </main>
  </div>
</template>

<script setup>
</script>
