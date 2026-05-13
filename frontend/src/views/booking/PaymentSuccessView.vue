<template>
  <div class="flex flex-col items-center pb-8 -mx-container-margin px-container-margin -mt-stack-space pt-stack-space">
    <main class="w-full max-w-md flex flex-col items-center">
      <!-- Header Success Animation/Illustration -->
      <div class="flex flex-col items-center mb-8">
        <div class="w-24 h-24 bg-primary/10 rounded-full flex items-center justify-center mb-4">
          <div class="w-16 h-16 bg-primary rounded-full flex items-center justify-center shadow-lg">
            <span class="material-symbols-outlined text-on-primary text-[40px]" style="font-variation-settings: 'FILL' 1;">check_circle</span>
          </div>
        </div>
        <h1 class="text-headline-lg font-headline-lg text-on-background text-center">Thanh toán thành công!</h1>
        <p class="text-body-md font-body-md text-on-surface-variant text-center mt-3 px-4">
          Cảm ơn bạn đã tin tưởng TransLink Pro. Vé của bạn đã được xác nhận và gửi về email.
        </p>
      </div>

      <!-- E-Ticket Card -->
      <div class="w-full bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col relative border border-outline-variant/30">
        <!-- Ticket Top Content -->
        <div class="p-6">
          <div class="flex justify-between items-start mb-6">
            <div>
              <p class="text-label-md font-label-md text-on-surface-variant uppercase tracking-wider">Booking ID</p>
              <p class="text-ticket-number font-ticket-number text-primary">TLP-8829104</p>
            </div>
            <div class="bg-primary/5 px-3 py-1 rounded-full border border-primary/10">
              <span class="text-label-md font-label-md text-primary">Đã xác nhận</span>
            </div>
          </div>
          <div class="flex items-center justify-between gap-4 mb-6">
            <div class="flex-1">
              <p class="text-label-md font-label-md text-on-surface-variant">Khởi hành</p>
              <p class="text-headline-sm font-headline-sm text-on-surface">Hà Nội</p>
            </div>
            <div class="flex flex-col items-center justify-center px-2">
              <span class="material-symbols-outlined text-primary">directions_bus</span>
              <div class="w-12 h-[1px] bg-outline-variant my-1"></div>
            </div>
            <div class="flex-1 text-right">
              <p class="text-label-md font-label-md text-on-surface-variant">Điểm đến</p>
              <p class="text-headline-sm font-headline-sm text-on-surface">Sapa</p>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-y-4 pt-2">
            <div>
              <p class="text-label-md font-label-md text-on-surface-variant">Thời gian</p>
              <p class="text-body-lg font-body-lg font-semibold text-on-surface">22:00, Hôm nay</p>
            </div>
            <div class="text-right">
              <p class="text-label-md font-label-md text-on-surface-variant">Số ghế</p>
              <p class="text-body-lg font-body-lg font-semibold text-on-surface">A12</p>
            </div>
          </div>
        </div>

        <!-- Perforation Line -->
        <div class="relative h-6 flex items-center">
          <div class="absolute left-[-12px] w-6 h-6 rounded-full bg-background border-r border-outline-variant/30"></div>
          <div class="w-full border-t border-dashed border-outline-variant mx-4"></div>
          <div class="absolute right-[-12px] w-6 h-6 rounded-full bg-background border-l border-outline-variant/30"></div>
        </div>

        <!-- Ticket Bottom Content (QR) -->
        <div class="p-6 pt-2 flex flex-col items-center">
          <div class="bg-white p-4 rounded-lg border border-outline-variant/30 mb-4">
            <img alt="Booking QR Code" class="w-32 h-32" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDvcSTc-yopzyL5kkWPAShhLSi6m-QVFQovMoFBbwu81zXCFdA7HI_7MW908ZrnD009iDAcPIHb5--mudKXVHS90QeG9NoFIA1SMLvNTmLoqfRaZjGn-a0Ems4yKj9q3gtVy4sANSpRbqhQ2Rt5x0o_R7m98GV18TQijQPm7F6Su8voMJ2UT9wVoLo-_0DO2r9ZBsnckyAt0KUm_XrD9V15bg1_iKdbjMgSUZt2HuPim9WW9PW3Wlp9W-oqnEfy-HJrFGAFS9FqLiap"/>
          </div>
          <p class="text-label-md font-label-md text-on-surface-variant text-center">Quét mã này khi lên xe</p>
        </div>
      </div>

      <!-- Payment Summary Card -->
      <div class="w-full mt-6 bg-surface-container-high rounded-xl p-5 flex flex-col gap-3">
        <div class="flex justify-between items-center">
          <span class="text-body-md font-body-md text-on-surface-variant">Tổng tiền thanh toán</span>
          <span class="text-body-lg font-body-lg font-bold text-secondary">300.000đ</span>
        </div>
        <div class="w-full h-[1px] bg-outline-variant/20"></div>
        <div class="flex justify-between items-center">
          <span class="text-body-md font-body-md text-on-surface-variant">Phương thức thanh toán</span>
          <div class="flex items-center gap-2">
            <span class="text-body-md font-body-md font-semibold text-on-surface">Ví MoMo</span>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="w-full mt-10 flex flex-col gap-3">
        <button @click="$router.push('/history')" class="w-full h-touch-target-min bg-primary text-on-primary rounded-xl font-semibold shadow-md active:scale-95 transition-transform flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-[20px]">confirmation_number</span>
          Xem vé của tôi
        </button>
        <button @click="$router.push('/')" class="w-full h-touch-target-min border border-primary text-primary rounded-xl font-semibold active:scale-95 transition-transform flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-[20px]">home</span>
          Về trang chủ
        </button>
      </div>

      <!-- Security/Trust Footer -->
      <div class="mt-8 flex items-center gap-2 opacity-60">
        <span class="material-symbols-outlined text-[16px]">verified_user</span>
        <p class="text-label-md font-label-md">Giao dịch an toàn & bảo mật bởi TransLink Pro</p>
      </div>
    </main>
  </div>
</template>

<script setup>
</script>
