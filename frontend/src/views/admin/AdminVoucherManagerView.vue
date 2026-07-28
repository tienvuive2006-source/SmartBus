<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto relative">
    
    <!-- Header Page Title -->
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4 border-b border-outline-variant/20 pb-6">
      <div>
        <h2 class="text-headline-lg font-headline-lg font-black text-on-background flex items-center gap-3">
          <span class="material-symbols-outlined text-4xl text-primary">redeem</span>
          Quản Lý Voucher & Mã Khuyến Mãi
        </h2>
        <p class="text-body-lg font-body-lg text-on-surface-variant">Tạo và quản lý các mã ưu đãi dành cho khách hàng tích điểm & Nạp ví</p>
      </div>
      
      <div class="flex items-center gap-3" v-if="activeTab === 'vouchers'">
        <button 
          @click="openCreateModal"
          class="bg-primary text-white hover:bg-primary/90 active:scale-95 px-5 py-3 rounded-xl font-bold tracking-wide transition-all shadow-md flex items-center gap-2"
        >
          <span class="material-symbols-outlined">add_circle</span>
          Tạo Voucher Mới
        </button>
      </div>
    </div>

    <!-- Tabs Header -->
    <div class="flex items-center gap-6 mb-6 border-b border-outline-variant/30 px-2">
      <button 
        @click="activeTab = 'vouchers'"
        class="pb-3 text-label-lg font-black tracking-wide border-b-2 transition-all"
        :class="activeTab === 'vouchers' ? 'border-primary text-primary' : 'border-transparent text-on-surface-variant hover:text-on-surface'"
      >
        Mã Giảm Giá (Đổi Điểm)
      </button>
      <button 
        @click="activeTab = 'topup'"
        class="pb-3 text-label-lg font-black tracking-wide border-b-2 transition-all"
        :class="activeTab === 'topup' ? 'border-primary text-primary' : 'border-transparent text-on-surface-variant hover:text-on-surface'"
      >
        Khuyến Mãi Nạp Ví
      </button>
    </div>

    <!-- VOUCHERS TAB -->
    <AdminVoucherTab ref="voucherTabRef" v-if="activeTab === 'vouchers'" />

    <!-- TOPUP PROMOTIONS TAB -->
    <AdminTopupPromotionTab v-else />

  </div>
</template>

<script setup>
import { ref } from 'vue';
import AdminVoucherTab from '@/components/admin/voucher/AdminVoucherTab.vue';
import AdminTopupPromotionTab from '@/components/admin/voucher/AdminTopupPromotionTab.vue';

const activeTab = ref('vouchers');
const voucherTabRef = ref(null);

const openCreateModal = () => {
  if (voucherTabRef.value) {
    voucherTabRef.value.openCreateModal();
  }
};
</script>
