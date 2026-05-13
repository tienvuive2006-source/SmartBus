<template>
  <div class="pb-32 -mx-container-margin px-container-margin -mt-stack-space pt-stack-space">
    <!-- Header -->
    <header class="bg-surface sticky top-0 z-40 h-touch-target-min flex items-center justify-between shadow-sm left-0 right-0 px-container-margin max-w-7xl mx-auto -mx-container-margin">
      <button @click="$router.back()" class="text-on-surface-variant hover:bg-surface-variant rounded-full w-10 h-10 flex items-center justify-center transition-colors">
        <span class="material-symbols-outlined">arrow_back</span>
      </button>
      <h1 class="text-headline-md font-headline-md text-on-surface flex-1 text-center pr-10">Thanh toán</h1>
    </header>

    <main class="max-w-md mx-auto py-stack-space space-y-stack-space">
      <!-- Progress Stepper -->
      <div class="flex items-center justify-center gap-2 mb-6">
        <div class="h-1 w-8 bg-surface-variant rounded-full"></div>
        <div class="h-1 w-8 bg-surface-variant rounded-full"></div>
        <div class="h-1 w-8 bg-surface-variant rounded-full"></div>
        <div class="h-1 w-8 bg-secondary-container rounded-full"></div>
      </div>

      <!-- Trip Summary Card -->
      <section class="bg-surface-container-lowest rounded-xl p-4 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant">
        <h2 class="text-headline-sm font-headline-sm mb-4">Tóm tắt chuyến đi</h2>
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-primary-container/10 rounded-lg flex items-center justify-center text-primary-container">
            <span class="material-symbols-outlined">directions_bus</span>
          </div>
          <div>
            <p class="text-body-md font-body-md font-semibold">Sao Việt Express</p>
            <p class="text-label-md font-label-md text-on-surface-variant">Xe giường nằm 34 chỗ</p>
          </div>
        </div>
        <div class="relative pl-6 py-2 border-l-2 border-dashed border-outline-variant ml-3 mb-4 space-y-6">
          <div class="relative">
            <div class="absolute -left-[31px] top-1 w-3 h-3 rounded-full bg-primary-container border-2 border-surface-container-lowest"></div>
            <p class="text-body-md font-body-md font-semibold">Hà Nội</p>
            <p class="text-label-md font-label-md text-on-surface-variant">22:00 • Bến xe Mỹ Đình</p>
          </div>
          <div class="relative">
            <div class="absolute -left-[31px] top-1 w-3 h-3 rounded-full bg-secondary-container border-2 border-surface-container-lowest"></div>
            <p class="text-body-md font-body-md font-semibold">Sapa</p>
            <p class="text-label-md font-label-md text-on-surface-variant">04:30 (Hôm sau) • VP Sapa</p>
          </div>
        </div>
        <div class="bg-surface-container-low rounded-lg p-3 flex justify-between items-center mt-4">
          <div>
            <p class="text-label-md font-label-md text-on-surface-variant">Số ghế</p>
            <p class="text-ticket-number font-ticket-number text-primary">A12</p>
          </div>
          <div class="text-right">
            <p class="text-label-md font-label-md text-on-surface-variant">Hành khách</p>
            <p class="text-body-md font-body-md font-semibold">1 Người lớn</p>
          </div>
        </div>
      </section>

      <!-- Price Details Card -->
      <section class="bg-surface-container-lowest rounded-xl p-4 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant">
        <h2 class="text-headline-sm font-headline-sm mb-4">Chi tiết giá</h2>
        <div class="space-y-3 mb-4">
          <div class="flex justify-between items-center text-body-md font-body-md text-on-surface-variant">
            <span>Giá vé gốc (1 vé)</span>
            <span>350.000đ</span>
          </div>
          <div class="flex justify-between items-center text-body-md font-body-md text-secondary-container">
            <span>Mã giảm giá (NEWUSER)</span>
            <span>-50.000đ</span>
          </div>
        </div>
        <hr class="border-dashed border-outline-variant my-3"/>
        <div class="flex justify-between items-center">
          <span class="text-body-lg font-body-lg font-semibold">Tổng thanh toán</span>
          <span class="text-headline-md font-headline-md text-primary">300.000đ</span>
        </div>
      </section>

      <!-- Payment Methods Card -->
      <section class="bg-surface-container-lowest rounded-xl p-4 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-surface-variant">
        <h2 class="text-headline-sm font-headline-sm mb-4">Phương thức thanh toán</h2>
        <div class="space-y-3">
          <label class="flex items-center gap-3 p-3 rounded-lg border-2 border-primary bg-primary-fixed/20 cursor-pointer transition-colors">
            <input checked class="text-primary focus:ring-primary w-5 h-5 border-outline" name="payment_method" type="radio"/>
            <div class="w-8 h-8 rounded bg-[#A50064] flex items-center justify-center text-white font-bold text-xs">
              MoMo
            </div>
            <div class="flex-1">
              <p class="text-body-md font-body-md font-medium text-on-surface">Ví điện tử MoMo</p>
            </div>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-surface-variant hover:bg-surface-container-low cursor-pointer transition-colors">
            <input class="text-primary focus:ring-primary w-5 h-5 border-outline" name="payment_method" type="radio"/>
            <div class="w-8 h-8 rounded bg-[#0068FF] flex items-center justify-center text-white font-bold text-xs">
              Zalo
            </div>
            <div class="flex-1">
              <p class="text-body-md font-body-md font-medium text-on-surface">ZaloPay</p>
            </div>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-surface-variant hover:bg-surface-container-low cursor-pointer transition-colors">
            <input class="text-primary focus:ring-primary w-5 h-5 border-outline" name="payment_method" type="radio"/>
            <div class="w-8 h-8 rounded bg-surface-variant flex items-center justify-center text-on-surface-variant">
              <span class="material-symbols-outlined text-[20px]">account_balance</span>
            </div>
            <div class="flex-1">
              <p class="text-body-md font-body-md font-medium text-on-surface">Thẻ ATM / Internet Banking</p>
            </div>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-surface-variant hover:bg-surface-container-low cursor-pointer transition-colors">
            <input class="text-primary focus:ring-primary w-5 h-5 border-outline" name="payment_method" type="radio"/>
            <div class="w-8 h-8 rounded bg-surface-variant flex items-center justify-center text-on-surface-variant">
              <span class="material-symbols-outlined text-[20px]">credit_card</span>
            </div>
            <div class="flex-1">
              <p class="text-body-md font-body-md font-medium text-on-surface">Thẻ tín dụng (Visa/Mastercard)</p>
            </div>
          </label>
        </div>
      </section>
    </main>

    <!-- Bottom Action Bar -->
    <div class="fixed bottom-0 left-0 w-full bg-surface-container-lowest p-4 shadow-[0px_-4px_12px_rgba(0,0,0,0.05)] border-t border-surface-variant z-50">
      <div class="max-w-md mx-auto flex items-center gap-4">
        <div class="flex-1">
          <p class="text-label-md font-label-md text-on-surface-variant">Tổng tiền</p>
          <p class="text-headline-sm font-headline-sm text-primary font-bold">300.000đ</p>
        </div>
        <button @click="$router.push('/booking/payment-success')" class="bg-secondary-container text-on-secondary-container px-8 py-3 rounded-full text-body-md font-body-md font-semibold shadow-md hover:bg-secondary-container/90 transition-colors active:scale-95">
          Thanh toán ngay
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
