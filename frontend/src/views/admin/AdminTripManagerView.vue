<template>
  <div class="p-4 md:p-6 lg:p-8 max-w-4xl mx-auto space-y-6">
    <!-- Page Title -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
      <div>
        <h2 class="text-headline-lg font-headline-lg text-on-surface">Quản lý chuyến đi</h2>
        <p class="text-body-lg font-body-lg text-on-surface-variant mt-1">Đang khởi hành • 14:30, Hôm nay</p>
      </div>
      <button class="flex items-center justify-center gap-2 bg-secondary-container text-on-secondary-container px-6 py-3 rounded-lg hover:bg-secondary-fixed transition-colors shadow-sm font-label-md text-label-md uppercase tracking-wide">
        <span class="material-symbols-outlined">qr_code_scanner</span>
        Quét mã QR Check-in
      </button>
    </div>

    <!-- Bento Grid for Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- Trip Info Card -->
      <div class="col-span-1 md:col-span-2 bg-surface-container-lowest p-5 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20 flex flex-col justify-between">
        <div class="flex justify-between items-start mb-4">
          <div>
            <span class="px-2.5 py-1 bg-primary-fixed-dim/30 text-primary font-ticket-number text-ticket-number rounded-full uppercase">Biển số: 29B-123.45</span>
            <h3 class="text-headline-md font-headline-md text-on-surface mt-3">Hà Nội <span class="text-outline mx-2">→</span> Hải Phòng</h3>
          </div>
          <span class="px-3 py-1 bg-green-100 text-green-800 text-label-md font-label-md rounded-full font-bold">Đang chạy</span>
        </div>
        <div class="flex items-center gap-6 text-on-surface-variant text-body-sm mt-4 pt-4 border-t border-dashed border-outline-variant">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px]">schedule</span>
            <span>Dự kiến: 2h 30m</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px]">route</span>
            <span>Cao tốc HN-HP</span>
          </div>
        </div>
      </div>

      <!-- Occupancy Chart Card -->
      <div class="col-span-1 bg-surface-container-lowest p-5 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20 flex flex-col items-center justify-center text-center">
        <h4 class="text-label-md font-label-md text-on-surface-variant uppercase tracking-wider mb-4">Tỷ lệ lấp đầy</h4>
        <div class="relative w-24 h-24 mb-3">
          <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
            <path class="text-surface-variant" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="currentColor" stroke-width="4"></path>
            <path class="text-primary" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="currentColor" stroke-dasharray="80, 100" stroke-width="4"></path>
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <span class="text-headline-sm font-headline-sm text-on-surface font-bold">32</span>
            <span class="text-[10px] text-on-surface-variant leading-none">/ 40</span>
          </div>
        </div>
        <p class="text-body-md font-body-md text-on-surface">Còn trống 8 ghế</p>
      </div>
    </div>

    <!-- Passenger List Section -->
    <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20 overflow-hidden">
      <div class="p-4 md:p-5 border-b border-outline-variant/20 flex justify-between items-center bg-surface-bright">
        <h3 class="text-headline-sm font-headline-sm text-on-surface">Danh sách hành khách</h3>
        <div class="flex items-center gap-2">
          <span class="w-3 h-3 rounded-full bg-primary inline-block"></span>
          <span class="text-label-md font-label-md text-on-surface-variant">Đã lên xe (28)</span>
        </div>
      </div>
      <div class="divide-y divide-outline-variant/10">
        <!-- Passenger Item 1 (Pending) -->
        <div class="p-4 flex items-center justify-between hover:bg-surface-container-low transition-colors">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-lg bg-surface-variant flex items-center justify-center text-on-surface-variant font-ticket-number text-ticket-number">
              A1
            </div>
            <div>
              <p class="text-body-lg font-body-lg font-medium text-on-surface">Nguyễn Văn An</p>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Mã vé: #VN-8821</p>
            </div>
          </div>
          <button class="bg-primary text-on-primary px-4 py-2 rounded-lg text-label-md font-label-md hover:bg-surface-tint transition-colors shadow-sm">
            Check-in
          </button>
        </div>
        <!-- Passenger Item 2 (Pending) -->
        <div class="p-4 flex items-center justify-between hover:bg-surface-container-low transition-colors">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-lg bg-surface-variant flex items-center justify-center text-on-surface-variant font-ticket-number text-ticket-number">
              A2
            </div>
            <div>
              <p class="text-body-lg font-body-lg font-medium text-on-surface">Trần Thị Bình</p>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Mã vé: #VN-8822</p>
            </div>
          </div>
          <button class="bg-primary text-on-primary px-4 py-2 rounded-lg text-label-md font-label-md hover:bg-surface-tint transition-colors shadow-sm">
            Check-in
          </button>
        </div>
        <!-- Passenger Item 3 (Checked In) -->
        <div class="p-4 flex items-center justify-between bg-primary-fixed/10 opacity-80">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-lg bg-primary/20 flex items-center justify-center text-primary font-ticket-number text-ticket-number">
              B1
            </div>
            <div>
              <p class="text-body-lg font-body-lg font-medium text-on-surface line-through decoration-outline">Lê Hoàng Cường</p>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Mã vé: #VN-8750</p>
            </div>
          </div>
          <div class="flex items-center gap-1 text-primary">
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">check_circle</span>
            <span class="text-label-md font-label-md font-bold">Đã lên</span>
          </div>
        </div>
        <!-- Passenger Item 4 (Checked In) -->
        <div class="p-4 flex items-center justify-between bg-primary-fixed/10 opacity-80">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-lg bg-primary/20 flex items-center justify-center text-primary font-ticket-number text-ticket-number">
              B2
            </div>
            <div>
              <p class="text-body-lg font-body-lg font-medium text-on-surface line-through decoration-outline">Phạm Tường Vy</p>
              <p class="text-label-md font-label-md text-on-surface-variant mt-0.5">Mã vé: #VN-8751</p>
            </div>
          </div>
          <div class="flex items-center gap-1 text-primary">
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">check_circle</span>
            <span class="text-label-md font-label-md font-bold">Đã lên</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
