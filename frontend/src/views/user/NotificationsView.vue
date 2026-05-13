<template>
  <div class="pb-8">
    <main class="px-container-margin max-w-2xl mx-auto space-y-8">
      <!-- Filter Chips -->
      <div class="flex gap-2 mb-6 overflow-x-auto pb-2 no-scrollbar">
        <button class="px-4 py-2 rounded-full bg-primary-container text-on-primary-container font-label-md text-label-md active:scale-95 transition-all whitespace-nowrap">Tất cả</button>
        <button class="px-4 py-2 rounded-full bg-surface-container text-on-surface-variant font-label-md text-label-md hover:bg-surface-container-high active:scale-95 transition-all whitespace-nowrap">Chuyến xe</button>
        <button class="px-4 py-2 rounded-full bg-surface-container text-on-surface-variant font-label-md text-label-md hover:bg-surface-container-high active:scale-95 transition-all whitespace-nowrap">Khuyến mãi</button>
        <button class="px-4 py-2 rounded-full bg-surface-container text-on-surface-variant font-label-md text-label-md hover:bg-surface-container-high active:scale-95 transition-all whitespace-nowrap">Hệ thống</button>
      </div>

      <!-- Section: Today -->
      <section>
        <h2 class="font-headline-sm text-headline-sm mb-4 text-on-surface">Hôm nay</h2>
        <div class="space-y-3">
          <!-- Departure Alert -->
          <div @click="$router.push('/history')" class="bg-surface-container-lowest p-4 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex gap-4 items-start border-l-4 border-primary transition-all hover:bg-surface-container-low cursor-pointer">
            <div class="w-12 h-12 rounded-full bg-primary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-primary" style="font-variation-settings: 'FILL' 1;">directions_bus</span>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start mb-1">
                <h3 class="font-headline-sm text-headline-sm text-on-surface">Chuyến xe sắp khởi hành</h3>
                <span class="text-label-md font-label-md text-outline">10:30</span>
              </div>
              <p class="font-body-md text-body-md text-on-surface-variant mb-3">Chuyến xe TL-452 đi Đà Lạt sẽ khởi hành trong 30 phút nữa tại cổng số 4. Vui lòng có mặt đúng giờ.</p>
              <div class="flex gap-2">
                <span class="px-2 py-1 rounded-lg bg-primary-fixed-dim text-on-primary-fixed font-label-md text-label-md">Gấp</span>
                <span class="px-2 py-1 rounded-lg bg-surface-variant text-on-surface-variant font-label-md text-label-md">Mã vé: #78392</span>
              </div>
            </div>
          </div>

          <!-- Promo Notification -->
          <div class="bg-surface-container-lowest p-4 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex gap-4 items-start transition-all hover:bg-surface-container-low cursor-pointer">
            <div class="w-12 h-12 rounded-full bg-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-secondary" style="font-variation-settings: 'FILL' 1;">sell</span>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start mb-1">
                <h3 class="font-headline-sm text-headline-sm text-on-surface">Ưu đãi cuối tuần cực hời!</h3>
                <span class="text-label-md font-label-md text-outline">08:15</span>
              </div>
              <p class="font-body-md text-body-md text-on-surface-variant mb-3">Nhập mã SUMMER50 để nhận ngay ưu đãi giảm 50.000đ cho mọi chuyến đi liên tỉnh trong tháng này.</p>
              <div class="relative w-full h-32 rounded-lg overflow-hidden mb-3">
                <img alt="Khuyến mãi mùa hè" class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAYzzJAYEg0k44_Zrivdl3KzpjYzssJdb90_o-4PeLreNGOsokvCEs4SJPIGM4ITPU_ga5jh-Xp0qVVoaf5OptZen3PLygJyawod5MMXpzuJjd9OUhIMHpYAIIgVHFe09A6vkfEky9UGm_HK3gsPZiExWghKCrgBlX960VIh3OkxLs5TvvTBQO5XGhxuS2ZCi9UuaEB-ZsYRwUTkcofXDoYm4IEpQA_G2wpWOBpGBvuclRrXKo2WNOpFnY6NqOKIV5TZJp3W251_Vcx"/>
              </div>
              <button class="font-label-md text-label-md text-primary font-bold">XEM CHI TIẾT</button>
            </div>
          </div>
        </div>
      </section>

      <!-- Section: Yesterday -->
      <section>
        <h2 class="font-headline-sm text-headline-sm mb-4 text-on-surface">Hôm qua</h2>
        <div class="space-y-3">
          <!-- Schedule Change -->
          <div class="bg-surface-container-lowest p-4 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex gap-4 items-start border-l-4 border-error transition-all hover:bg-surface-container-low cursor-pointer opacity-80">
            <div class="w-12 h-12 rounded-full bg-error-container flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-error">event_busy</span>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start mb-1">
                <h3 class="font-headline-sm text-headline-sm text-on-surface">Thay đổi lịch trình</h3>
                <span class="text-label-md font-label-md text-outline">15:45</span>
              </div>
              <p class="font-body-md text-body-md text-on-surface-variant">Chuyến xe TL-102 từ TP.HCM đi Cần Thơ đã dời giờ khởi hành từ 08:00 sang 08:30 do điều kiện thời tiết.</p>
            </div>
          </div>

          <!-- System Message -->
          <div class="bg-surface-container-lowest p-4 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex gap-4 items-start transition-all hover:bg-surface-container-low cursor-pointer opacity-80">
            <div class="w-12 h-12 rounded-full bg-tertiary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-tertiary">smart_toy</span>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start mb-1">
                <h3 class="font-headline-sm text-headline-sm text-on-surface">Cập nhật ứng dụng v2.4.0</h3>
                <span class="text-label-md font-label-md text-outline">09:00</span>
              </div>
              <p class="font-body-md text-body-md text-on-surface-variant">Trải nghiệm tính năng AI Help mới giúp bạn tìm kiếm hành trình thông minh hơn bao giờ hết.</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Earlier -->
      <div class="text-center py-4">
        <button class="text-primary font-label-md text-label-md border border-outline-variant px-6 py-2 rounded-full hover:bg-surface-variant transition-colors">
          Xem thêm thông báo cũ
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
</script>
