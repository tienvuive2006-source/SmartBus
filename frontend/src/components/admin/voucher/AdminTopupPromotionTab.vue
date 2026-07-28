<template>
  <div class="w-full">
    <div class="bg-white rounded-3xl border border-outline-variant/30 shadow-[0px_4px_24px_rgba(0,0,0,0.02)] p-8">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="text-title-lg font-black text-on-surface mb-2">Mốc Khuyến Mãi Nạp Ví</h3>
          <p class="text-body-md text-on-surface-variant">Thiết lập các khoảng tiền nạp (Từ ... Đến ...) để tự động cộng thưởng cho khách.</p>
        </div>
        <button 
          v-if="isEditing"
          @click="addPromotionTier"
          class="bg-surface-container-high text-primary hover:bg-primary/10 active:scale-95 px-4 py-2 rounded-xl font-bold tracking-wide transition-all flex items-center gap-2"
        >
          <span class="material-symbols-outlined">add</span>
          Thêm mốc mới
        </button>
      </div>

      <div v-if="loadingPromotions" class="py-8 text-center">
        <div class="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto"></div>
      </div>

      <div v-else>
        <div v-if="topupPromotions.length === 0" class="py-12 text-center text-on-surface-variant bg-surface-container-lowest rounded-2xl border border-dashed border-outline-variant/50 mb-6">
          Không có cấu hình khuyến mãi nạp ví nào. Hãy thêm mốc mới!
        </div>

          <div class="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-8">
            <div 
              v-for="(tier, index) in topupPromotions" 
              :key="index"
              class="relative bg-white p-6 rounded-2xl border border-outline-variant/50 shadow-[0px_4px_20px_rgba(0,0,0,0.03)] hover:border-primary/30 hover:shadow-lg transition-all duration-300"
            >
              <div class="flex items-center justify-between mb-5">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-primary/10 text-primary flex items-center justify-center font-black text-lg">
                    {{ index + 1 }}
                  </div>
                  <h4 class="text-title-md font-bold text-on-surface">Mốc Khuyến Mãi</h4>
                </div>
                <button 
                  v-if="isEditing"
                  @click="removePromotionTier(index)"
                  class="w-8 h-8 rounded-full bg-error/10 text-error hover:bg-error hover:text-white flex items-center justify-center transition-colors"
                  title="Xóa mốc này"
                >
                  <span class="material-symbols-outlined text-sm">close</span>
                </button>
              </div>

              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-label-md font-bold text-on-surface-variant mb-1">Từ (VNĐ)</label>
                    <input 
                      v-model.number="tier.minAmount" 
                      type="number" min="0" :disabled="!isEditing"
                      class="w-full px-4 py-3 bg-surface-container-lowest border border-outline-variant/50 rounded-xl text-body-lg font-black focus:outline-none focus:border-primary focus:bg-white disabled:opacity-60 disabled:cursor-not-allowed transition-all"
                      :class="!isEditing ? 'text-slate-500' : 'text-primary'"
                    >
                  </div>
                  <div>
                    <label class="block text-label-md font-bold text-on-surface-variant mb-1">Đến (VNĐ) <span class="font-normal text-xs text-slate-400 opacity-80">(Trống = Vô hạn)</span></label>
                    <input 
                      v-model.number="tier.maxAmount" 
                      type="number" min="0" placeholder="Vô hạn" :disabled="!isEditing"
                      class="w-full px-4 py-3 bg-surface-container-lowest border border-outline-variant/50 rounded-xl text-body-lg font-black focus:outline-none focus:border-primary focus:bg-white disabled:opacity-60 disabled:cursor-not-allowed transition-all"
                      :class="!isEditing ? 'text-slate-500' : 'text-primary'"
                    >
                  </div>
                </div>

                <div class="bg-emerald-50/50 p-4 rounded-xl border border-emerald-100">
                  <label class="block text-label-md font-bold text-emerald-700 mb-2">Tỷ lệ thưởng (%)</label>
                  <div class="relative w-1/2">
                    <input 
                      v-model.number="tier.bonusPercent" 
                      type="number" min="0" max="100" :disabled="!isEditing"
                      class="w-full px-4 py-3 bg-white border border-emerald-200 rounded-xl text-title-md font-black text-emerald-600 focus:outline-none focus:border-emerald-500 pr-10 disabled:opacity-60 disabled:cursor-not-allowed"
                    >
                    <span class="absolute right-4 top-1/2 -translate-y-1/2 text-emerald-600 font-bold">%</span>
                  </div>
                </div>

                <div class="bg-surface-container-lowest p-4 rounded-xl border border-dashed border-outline-variant/50">
                  <h5 class="text-label-md font-bold text-on-surface mb-3 flex items-center gap-2">
                    <span class="material-symbols-outlined text-sm text-primary">calendar_month</span>
                    Thời gian áp dụng
                  </h5>
                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <label class="block text-xs font-bold text-on-surface-variant mb-1">Từ ngày</label>
                      <input 
                        v-model="tier.startDate" 
                        type="date" :disabled="!isEditing"
                        class="w-full px-3 py-2 bg-white border border-outline-variant/50 rounded-lg text-body-sm font-bold focus:outline-none focus:border-primary disabled:opacity-60 disabled:cursor-not-allowed"
                        :class="!isEditing ? 'text-slate-500' : 'text-primary'"
                      >
                    </div>
                    <div>
                      <label class="block text-xs font-bold text-on-surface-variant mb-1">Đến ngày</label>
                      <input 
                        v-model="tier.endDate" 
                        type="date" :disabled="!isEditing"
                        class="w-full px-3 py-2 bg-white border border-outline-variant/50 rounded-lg text-body-sm font-bold focus:outline-none focus:border-primary disabled:opacity-60 disabled:cursor-not-allowed"
                        :class="!isEditing ? 'text-slate-500' : 'text-primary'"
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

        <div class="flex justify-end pt-4 border-t border-outline-variant/20 gap-3">
          <button 
            v-if="!isEditing"
            @click="isEditing = true"
            class="bg-surface-container-high text-primary hover:bg-primary/10 active:scale-95 px-8 py-3 rounded-xl font-bold tracking-wide transition-all flex items-center gap-2"
          >
            <span class="material-symbols-outlined">edit</span>
            Chỉnh Sửa
          </button>
          
          <template v-else>
            <button 
              @click="cancelEdit"
              class="bg-surface-container-lowest text-on-surface-variant hover:bg-surface-container-high active:scale-95 px-6 py-3 rounded-xl font-bold tracking-wide transition-all border border-outline-variant/30 flex items-center gap-2"
            >
              Hủy
            </button>
            <button 
              @click="saveTopupPromotions"
              :disabled="savingPromotions"
              class="bg-primary text-white hover:bg-primary/90 active:scale-95 px-8 py-3 rounded-xl font-bold tracking-wide transition-all shadow-md flex items-center gap-2 disabled:opacity-50"
            >
              <span v-if="savingPromotions" class="material-symbols-outlined animate-spin">progress_activity</span>
              <span v-else class="material-symbols-outlined">save</span>
              {{ savingPromotions ? 'Đang lưu...' : 'Lưu Cấu Hình' }}
            </button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();

const isEditing = ref(false);
const topupPromotions = ref([]);
const loadingPromotions = ref(false);
const savingPromotions = ref(false);

const fetchTopupPromotions = async () => {
  loadingPromotions.value = true;
  try {
    const res = await api.get('/settings/TOPUP_PROMOTIONS');
    if (res.data && res.data.value) {
      topupPromotions.value = JSON.parse(res.data.value);
    } else {
      topupPromotions.value = [];
    }
  } catch (error) {
    console.error(error);
  } finally {
    loadingPromotions.value = false;
  }
};

const addPromotionTier = () => {
  topupPromotions.value.push({ minAmount: 0, maxAmount: null, bonusPercent: 0, startDate: null, endDate: null });
};

const removePromotionTier = (index) => {
  topupPromotions.value.splice(index, 1);
};

const cancelEdit = async () => {
  isEditing.value = false;
  await fetchTopupPromotions(); // Refresh data back to original
};

const saveTopupPromotions = async () => {
  savingPromotions.value = true;
  try {
    await api.put('/settings/TOPUP_PROMOTIONS', {
      value: JSON.stringify(topupPromotions.value)
    });
    alert('Đã lưu cấu hình khuyến mãi nạp ví thành công!');
    isEditing.value = false;
  } catch (error) {
    console.error(error);
    alert('Lỗi lưu cấu hình: ' + (error.response?.data?.message || error.message));
  } finally {
    savingPromotions.value = false;
  }
};

onMounted(() => {
  fetchTopupPromotions();
});
</script>
