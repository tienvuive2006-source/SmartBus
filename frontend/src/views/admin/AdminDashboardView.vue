<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto space-y-8">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-headline-lg font-headline-lg text-on-background">Tổng quan hệ thống</h1>
        <p class="text-body-md font-body-md text-on-surface-variant mt-1">Cập nhật lúc 14:30, Hôm nay</p>
      </div>
      <div class="flex gap-4">
        <button class="bg-surface-variant text-on-surface-variant px-4 py-2 rounded-lg text-label-md font-label-md flex items-center gap-2 hover:bg-surface-container-high transition-colors">
          <span class="material-symbols-outlined text-[18px]">calendar_month</span>
          Hôm nay
        </button>
        <button class="bg-primary text-on-primary px-4 py-2 rounded-lg text-label-md font-label-md font-medium hover:bg-surface-tint transition-colors shadow-sm flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px]">download</span>
          Báo cáo
        </button>
      </div>
    </div>

    <!-- Key Metrics Bento Grid -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- Revenue Card -->
      <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant relative overflow-hidden">
        <div class="absolute -right-6 -top-6 w-24 h-24 bg-primary/5 rounded-full blur-xl"></div>
        <div class="flex justify-between items-start mb-4">
          <div class="p-3 bg-primary-fixed rounded-lg text-on-primary-fixed">
            <span class="material-symbols-outlined">payments</span>
          </div>
          <span class="flex items-center text-secondary-container text-label-md font-label-md bg-secondary-fixed/50 px-2 py-1 rounded-full">
            <span class="material-symbols-outlined text-[14px] mr-1">trending_up</span>
            +12.5%
          </span>
        </div>
        <p class="text-body-md font-body-md text-on-surface-variant mb-1">Doanh thu ngày</p>
        <h3 class="text-headline-lg font-headline-lg text-on-background">45,230,000 ₫</h3>
      </div>

      <!-- Tickets Sold Card -->
      <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant relative overflow-hidden">
        <div class="absolute -right-6 -top-6 w-24 h-24 bg-secondary-container/5 rounded-full blur-xl"></div>
        <div class="flex justify-between items-start mb-4">
          <div class="p-3 bg-secondary-fixed rounded-lg text-on-secondary-fixed">
            <span class="material-symbols-outlined">confirmation_number</span>
          </div>
          <span class="flex items-center text-secondary-container text-label-md font-label-md bg-secondary-fixed/50 px-2 py-1 rounded-full">
            <span class="material-symbols-outlined text-[14px] mr-1">trending_up</span>
            +5.2%
          </span>
        </div>
        <p class="text-body-md font-body-md text-on-surface-variant mb-1">Tổng vé bán ra</p>
        <h3 class="text-headline-lg font-headline-lg text-on-background">1,248 vé</h3>
      </div>

      <!-- Active Fleet Card -->
      <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant relative overflow-hidden">
        <div class="absolute -right-6 -top-6 w-24 h-24 bg-tertiary/5 rounded-full blur-xl"></div>
        <div class="flex justify-between items-start mb-4">
          <div class="p-3 bg-surface-container-high rounded-lg text-on-surface">
            <span class="material-symbols-outlined">directions_bus</span>
          </div>
          <span class="flex items-center text-outline text-label-md font-label-md bg-surface-variant px-2 py-1 rounded-full">
            Bình thường
          </span>
        </div>
        <p class="text-body-md font-body-md text-on-surface-variant mb-1">Số xe đang hoạt động</p>
        <div class="flex items-baseline gap-2">
          <h3 class="text-headline-lg font-headline-lg text-on-background">42</h3>
          <span class="text-body-md font-body-md text-on-surface-variant">/ 50 xe</span>
        </div>
      </div>
    </div>

    <!-- Charts and Notifications Row -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Revenue Chart Area -->
      <div class="lg:col-span-2 bg-surface-container-lowest rounded-xl p-6 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-headline-sm font-headline-sm text-on-background">Xu hướng doanh thu tuần</h2>
          <button class="text-on-surface-variant hover:text-primary transition-colors">
            <span class="material-symbols-outlined">more_horiz</span>
          </button>
        </div>
        <div class="h-64 w-full relative flex items-end justify-between gap-2 pt-8">
          <div class="absolute left-0 top-0 h-full flex flex-col justify-between text-label-md font-label-md text-outline pr-4 pb-8">
            <span>50M</span>
            <span>37.5M</span>
            <span>25M</span>
            <span>12.5M</span>
            <span>0</span>
          </div>
          <div class="flex-1 ml-10 h-full relative flex items-end justify-between px-2 pb-8 border-b border-l border-surface-variant">
            <div class="absolute inset-0 flex flex-col justify-between pt-2">
              <div class="border-t border-surface-variant/50 w-full"></div>
              <div class="border-t border-surface-variant/50 w-full"></div>
              <div class="border-t border-surface-variant/50 w-full"></div>
              <div class="border-t border-surface-variant/50 w-full"></div>
            </div>
            <div class="w-full flex justify-between items-end h-full z-10 px-4">
              <div class="w-8 bg-primary/20 rounded-t-sm h-[40%] hover:bg-primary/40 transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">20M</div>
              </div>
              <div class="w-8 bg-primary/30 rounded-t-sm h-[55%] hover:bg-primary/50 transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">27M</div>
              </div>
              <div class="w-8 bg-primary/40 rounded-t-sm h-[45%] hover:bg-primary/60 transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">22M</div>
              </div>
              <div class="w-8 bg-primary/60 rounded-t-sm h-[80%] hover:bg-primary/80 transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">40M</div>
              </div>
              <div class="w-8 bg-primary/50 rounded-t-sm h-[65%] hover:bg-primary/70 transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">32M</div>
              </div>
              <div class="w-8 bg-primary/70 rounded-t-sm h-[85%] hover:bg-primary/90 transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">42M</div>
              </div>
              <div class="w-8 bg-primary rounded-t-sm h-[90%] hover:bg-surface-tint transition-colors relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface text-label-md px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">45M</div>
              </div>
            </div>
          </div>
          <div class="absolute bottom-0 left-10 right-0 flex justify-between px-6 text-label-md font-label-md text-outline">
            <span>T2</span>
            <span>T3</span>
            <span>T4</span>
            <span>T5</span>
            <span>T6</span>
            <span>T7</span>
            <span>CN</span>
          </div>
        </div>
      </div>

      <!-- Notifications List -->
      <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant flex flex-col">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-headline-sm font-headline-sm text-on-background">Thông báo mới</h2>
          <a class="text-primary text-label-md font-label-md font-medium hover:underline" href="#">Xem tất cả</a>
        </div>
        <div class="flex-1 space-y-4">
          <!-- Alert Item -->
          <div class="flex gap-3 p-3 rounded-lg hover:bg-surface-container-low transition-colors cursor-pointer border border-transparent hover:border-surface-variant">
            <div class="w-10 h-10 rounded-full bg-error-container text-on-error-container flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">warning</span>
            </div>
            <div>
              <h4 class="text-body-md font-body-md font-medium text-on-background">Xe 29B-123.45 cần bảo trì</h4>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Đến hạn bảo dưỡng định kỳ 10,000km. Vui lòng sắp xếp lịch.</p>
              <span class="text-label-md font-label-md text-outline mt-1 block">10 phút trước</span>
            </div>
          </div>
          <!-- Info Item -->
          <div class="flex gap-3 p-3 rounded-lg hover:bg-surface-container-low transition-colors cursor-pointer border border-transparent hover:border-surface-variant">
            <div class="w-10 h-10 rounded-full bg-primary-fixed text-on-primary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">info</span>
            </div>
            <div>
              <h4 class="text-body-md font-body-md font-medium text-on-background">Tuyến HN-HP tăng chuyến</h4>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Đã thêm 2 chuyến xe phụ phục vụ dịp cuối tuần.</p>
              <span class="text-label-md font-label-md text-outline mt-1 block">1 giờ trước</span>
            </div>
          </div>
          <!-- Success Item -->
          <div class="flex gap-3 p-3 rounded-lg hover:bg-surface-container-low transition-colors cursor-pointer border border-transparent hover:border-surface-variant">
            <div class="w-10 h-10 rounded-full bg-surface-container-high text-on-surface flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">check_circle</span>
            </div>
            <div>
              <h4 class="text-body-md font-body-md font-medium text-on-background">Cập nhật phần mềm thành công</h4>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Hệ thống quét QR đã được nâng cấp lên phiên bản 2.1.</p>
              <span class="text-label-md font-label-md text-outline mt-1 block">3 giờ trước</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
