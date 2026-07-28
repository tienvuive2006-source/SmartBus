<template>
  <div class="lg:col-span-3 space-y-8">
    <section>
      <h4 class="text-sm font-black text-slate-800 mb-4 uppercase tracking-widest flex items-center gap-2">
        <span class="w-5 h-5 rounded-full bg-slate-800 text-white flex items-center justify-center text-[10px]">4</span>
        Thông tin giao dịch
      </h4>
      
      <div class="bg-white border border-slate-100 rounded-2xl p-6 shadow-[0_4px_20px_rgb(0,0,0,0.03)] space-y-4">
        <div class="flex justify-between items-center pb-4 border-b border-dashed border-slate-200">
          <span class="text-sm font-semibold text-slate-500">Phương thức</span>
          <div class="flex items-center gap-1.5 bg-slate-50 px-2 py-1 rounded">
            <span class="material-symbols-outlined text-emerald-600 text-[16px]">account_balance</span>
            <span class="text-xs font-black text-slate-800">VietQR</span>
          </div>
        </div>
        
        <div class="flex justify-between items-center pb-4 border-b border-dashed border-slate-200">
          <span class="text-sm font-semibold text-slate-500">Số tiền nạp</span>
          <span class="text-sm font-black text-slate-900 whitespace-nowrap">{{ (topupAmount || 0).toLocaleString('vi-VN') }} ₫</span>
        </div>
        
        <div class="flex justify-between items-center pb-4 border-b border-dashed border-slate-200">
          <span class="text-sm font-semibold text-slate-500">Phí giao dịch</span>
          <span class="text-sm font-black text-emerald-600">Miễn phí</span>
        </div>

        <div v-if="bonusAmount > 0" class="flex justify-between items-center pb-4 border-b border-dashed border-slate-200">
          <span class="text-sm font-semibold text-emerald-600 flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px]">redeem</span>
            Tiền thưởng KM
          </span>
          <span class="text-sm font-black text-emerald-600 whitespace-nowrap">+{{ bonusAmount.toLocaleString('vi-VN') }} ₫</span>
        </div>
        
        <div class="flex justify-between items-center pt-2">
          <span class="text-base font-black text-slate-900">Tổng thanh toán</span>
          <span class="text-xl font-black text-slate-900 whitespace-nowrap">{{ (topupAmount || 0).toLocaleString('vi-VN') }} ₫</span>
        </div>
        
        <div v-if="bonusAmount > 0" class="flex justify-between items-center mt-2 p-3 bg-emerald-50 rounded-xl border border-emerald-100">
          <span class="text-sm font-black text-emerald-800">Thực nhận vào ví</span>
          <span class="text-xl font-black text-emerald-700 whitespace-nowrap">{{ ((topupAmount || 0) + (bonusAmount || 0)).toLocaleString('vi-VN') }} ₫</span>
        </div>
      </div>
    </section>

    <!-- Security Info -->
    <section class="bg-emerald-50/50 border border-emerald-100 rounded-2xl p-6">
      <h4 class="text-sm font-black text-emerald-800 flex items-center gap-2 mb-4">
        <span class="material-symbols-outlined">gpp_good</span>
        Giao dịch an toàn & bảo mật
      </h4>
      <ul class="space-y-3 text-xs font-semibold text-emerald-700/80">
        <li class="flex items-start gap-2">
          <span class="material-symbols-outlined text-[16px] text-emerald-500">check</span>
          Mã QR được mã hóa theo chuẩn VietQR.
        </li>
        <li class="flex items-start gap-2">
          <span class="material-symbols-outlined text-[16px] text-emerald-500">check</span>
          Thông tin giao dịch được bảo mật tuyệt đối.
        </li>
        <li class="flex items-start gap-2">
          <span class="material-symbols-outlined text-[16px] text-emerald-500">check</span>
          Hệ thống ghi nhận tiền vào ví trong 3-5 giây.
        </li>
        <li class="flex items-start gap-2">
          <span class="material-symbols-outlined text-[16px] text-emerald-500">check</span>
          Đảm bảo hoàn tiền 100% nếu có sự cố.
        </li>
      </ul>
    </section>

    <!-- Support Block -->
    <section class="bg-slate-50 border border-slate-200 rounded-2xl p-6">
      <h4 class="text-sm font-black text-slate-800 flex items-center gap-2 mb-4">
        <span class="material-symbols-outlined text-amber-500">support_agent</span>
        Hỗ trợ khi cần thiết
      </h4>
      <ul class="space-y-3 text-xs font-semibold text-slate-600">
        <li class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[16px]">call</span>
          Hotline: <span class="font-black text-slate-800 text-sm">1900 59 99 97</span>
        </li>
        <li class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[16px]">mail</span>
          Email: <span class="text-blue-600">support@trungnam.vn</span>
        </li>
        <li class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[16px]">schedule</span>
          Thời gian: 06:00 - 22:00 (Tất cả các ngày)
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
defineProps({
  topupAmount: {
    type: Number,
    required: true
  },
  bonusAmount: {
    type: Number,
    default: 0
  }
});
</script>
