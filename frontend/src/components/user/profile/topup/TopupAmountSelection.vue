<template>
  <div class="lg:col-span-3 space-y-8">
    <!-- Select Amount -->
    <section>
      <h4 class="text-sm font-black text-slate-800 mb-4 uppercase tracking-widest flex items-center gap-2">
        <span class="w-5 h-5 rounded-full bg-slate-800 text-white flex items-center justify-center text-[10px]">1</span>
        Chọn số tiền nạp
      </h4>
      
      <div class="grid grid-cols-2 gap-3 mb-4">
        <button 
          v-for="amt in [50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000]" 
          :key="amt"
          @click="!isConfirmed && $emit('update:modelValue', amt)"
          class="p-3 rounded-xl border-2 transition-all font-black text-sm text-center"
          :class="[
            modelValue === amt ? 'border-emerald-500 text-emerald-700 bg-emerald-50' : 'border-slate-100 text-slate-600 hover:border-slate-300 hover:bg-slate-50',
            isConfirmed ? 'opacity-50 cursor-not-allowed' : ''
          ]"
          :disabled="isConfirmed"
        >
          {{ amt.toLocaleString('vi-VN') }} ₫
        </button>
      </div>

      <div class="relative mb-4">
        <input 
          :value="modelValue"
          @input="$emit('update:modelValue', Number($event.target.value))"
          @focus="$event.target.select()"
          type="number"
          placeholder="Nhập số tiền khác..."
          class="w-full font-black text-slate-800 bg-slate-50 p-4 rounded-xl border border-slate-200 focus:ring-2 focus:ring-emerald-500 outline-none transition-all pr-12"
          :disabled="isConfirmed"
          :class="isConfirmed ? 'opacity-60 cursor-not-allowed' : ''"
        />
        <span class="absolute right-4 top-1/2 -translate-y-1/2 font-black text-slate-400">₫</span>
      </div>

      <div class="grid grid-cols-3 gap-2 mb-4">
        <button @click="!isConfirmed && $emit('update:modelValue', (modelValue || 0) + 100000)" :disabled="isConfirmed" :class="isConfirmed ? 'opacity-50 cursor-not-allowed' : ''" class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-600 transition-colors">+ 100.000</button>
        <button @click="!isConfirmed && $emit('update:modelValue', (modelValue || 0) + 200000)" :disabled="isConfirmed" :class="isConfirmed ? 'opacity-50 cursor-not-allowed' : ''" class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-600 transition-colors">+ 200.000</button>
        <button @click="!isConfirmed && $emit('update:modelValue', (modelValue || 0) + 500000)" :disabled="isConfirmed" :class="isConfirmed ? 'opacity-50 cursor-not-allowed' : ''" class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-600 transition-colors">+ 500.000</button>
      </div>

      <!-- Bonus Badge -->
      <div v-if="bonusAmount > 0" class="bg-emerald-50 text-emerald-700 p-3 rounded-xl border border-emerald-100 flex items-center justify-between shadow-sm animate-pulse">
        <div class="flex items-center gap-2">
           <span class="material-symbols-outlined text-emerald-600 text-xl">redeem</span>
           <span class="text-xs font-black uppercase tracking-wide">Khuyến mãi nạp ví</span>
        </div>
        <span class="text-sm font-black text-emerald-700">+{{ bonusAmount.toLocaleString('vi-VN') }} ₫</span>
      </div>
    </section>

    <!-- Select Payment Method -->
    <section>
      <h4 class="text-sm font-black text-slate-800 mb-4 uppercase tracking-widest flex items-center gap-2">
        <span class="w-5 h-5 rounded-full bg-slate-800 text-white flex items-center justify-center text-[10px]">2</span>
        Phương thức thanh toán
      </h4>
      
      <div class="space-y-3">
        <div class="p-4 rounded-xl border-2 border-emerald-500 bg-emerald-50/30 flex items-center justify-between cursor-pointer">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-white border border-slate-200 rounded-lg flex items-center justify-center">
              <span class="material-symbols-outlined text-emerald-600 text-[22px]">account_balance</span>
            </div>
            <div>
              <h5 class="font-black text-sm text-slate-800">Chuyển khoản Ngân hàng</h5>
              <p class="text-[10px] font-bold text-slate-500">Quét mã QR qua ứng dụng ngân hàng</p>
            </div>
          </div>
          <span class="material-symbols-outlined text-emerald-500 text-2xl">check_circle</span>
        </div>

        <!-- Disabled methods -->
        <div class="p-4 rounded-xl border-2 border-slate-100 bg-white flex items-center justify-between opacity-50 cursor-not-allowed grayscale">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-white border border-slate-200 rounded-lg flex items-center justify-center">
              <span class="material-symbols-outlined text-slate-400 text-[22px]">account_balance_wallet</span>
            </div>
            <div>
              <h5 class="font-black text-sm text-slate-800">Ví MoMo</h5>
              <p class="text-[10px] font-bold text-slate-500">Thanh toán qua ứng dụng MoMo</p>
            </div>
          </div>
        </div>

        <div class="p-4 rounded-xl border-2 border-slate-100 bg-white flex items-center justify-between opacity-50 cursor-not-allowed grayscale">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-white border border-slate-200 rounded-lg flex items-center justify-center">
              <span class="material-symbols-outlined text-slate-400 text-[22px]">account_balance_wallet</span>
            </div>
            <div>
              <h5 class="font-black text-sm text-slate-800">Ví ZaloPay</h5>
              <p class="text-[10px] font-bold text-slate-500">Thanh toán qua ứng dụng ZaloPay</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="pt-4 border-t border-slate-100">
      <button v-if="!isConfirmed" @click="$emit('confirm')" class="w-full bg-[#075955] hover:bg-[#064e4b] text-white py-4 rounded-xl font-black text-sm uppercase tracking-widest transition-all shadow-[0_4px_14px_rgba(7,89,85,0.3)] hover:shadow-[0_6px_20px_rgba(7,89,85,0.4)] flex items-center justify-center gap-2">
        Xác nhận số tiền
        <span class="material-symbols-outlined text-lg">arrow_forward</span>
      </button>
      <button v-else @click="$emit('edit')" class="w-full bg-slate-100 hover:bg-slate-200 text-slate-600 py-4 rounded-xl font-black text-sm uppercase tracking-widest transition-all flex items-center justify-center gap-2">
        Sửa số tiền
        <span class="material-symbols-outlined text-lg">edit</span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: Number,
    required: true
  },
  isConfirmed: {
    type: Boolean,
    default: false
  },
  bonusAmount: {
    type: Number,
    default: 0
  }
});
defineEmits(['update:modelValue', 'confirm', 'edit']);
</script>
