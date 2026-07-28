<template>
  <div class="w-full">
    <div class="bg-white rounded-3xl border border-outline-variant/30 shadow-[0px_4px_24px_rgba(0,0,0,0.02)] overflow-hidden">
      
      <div v-if="loading" class="p-12 text-center">
        <div class="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
        <p class="text-body-md text-on-surface-variant">Đang tải danh sách voucher...</p>
      </div>

      <div v-else-if="vouchers.length === 0" class="p-12 text-center border-dashed border-outline-variant/50">
        <div class="w-20 h-20 bg-surface-container-high rounded-full flex items-center justify-center mx-auto mb-4">
          <span class="material-symbols-outlined text-5xl text-primary/50">redeem</span>
        </div>
        <h4 class="text-headline-sm font-bold text-on-surface">Chưa có mã khuyến mãi nào</h4>
        <p class="text-body-md text-on-surface-variant mt-1 max-w-sm mx-auto">Hãy tạo mã ưu đãi đầu tiên để thu hút khách hàng.</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-surface-container-lowest border-b border-outline-variant/30">
              <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider">Mã Voucher</th>
              <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider">Giảm giá</th>
              <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider">Điểm cần đổi</th>
              <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider text-center">Lượt dùng</th>
              <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider">Trạng thái</th>
              <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-outline-variant/10 text-body-md text-on-surface">
            <tr 
              v-for="voucher in vouchers" 
              :key="voucher.id"
              class="hover:bg-surface-container-lowest/50 transition-colors"
            >
              <td class="py-4 px-6 font-bold text-primary font-mono text-lg">{{ voucher.code }}</td>
              <td class="py-4 px-6 font-bold text-error">-{{ voucher.discountAmount.toLocaleString('vi-VN') }}đ</td>
              <td class="py-4 px-6 font-bold text-amber-500 flex items-center gap-1">
                <span class="material-symbols-outlined text-[18px]">stars</span>
                {{ voucher.pointsCost.toLocaleString('vi-VN') }} pts
              </td>
              <td class="py-4 px-6 text-center font-bold text-on-surface">
                {{ voucher.usageCount || 0 }}
              </td>
              <td class="py-4 px-6">
                <span 
                  class="px-3 py-1 text-label-sm font-black rounded-lg uppercase tracking-wider shadow-sm"
                  :class="voucher.isActive ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-500'"
                >
                  {{ voucher.isActive ? 'Kích hoạt' : 'Đã khóa' }}
                </span>
              </td>
              <td class="py-4 px-6 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button 
                    @click="openEditModal(voucher)"
                    class="p-2 text-primary hover:bg-primary/10 rounded-xl transition-colors"
                    title="Sửa"
                  >
                    <span class="material-symbols-outlined">edit</span>
                  </button>
                  <button 
                    @click="deleteVoucher(voucher.id)"
                    class="p-2 text-error hover:bg-error/10 rounded-xl transition-colors"
                    title="Xóa"
                  >
                    <span class="material-symbols-outlined">delete</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Form Modal (For Vouchers) -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
      <div class="bg-surface rounded-3xl p-6 w-full max-w-md shadow-2xl relative animate-fade-in-up">
        
        <button @click="showModal = false" class="absolute top-4 right-4 p-2 text-on-surface-variant hover:bg-surface-container-high rounded-full transition-colors">
          <span class="material-symbols-outlined">close</span>
        </button>

        <h3 class="text-headline-sm font-black text-on-surface mb-6">{{ isEditing ? 'Cập nhật Voucher' : 'Tạo Voucher mới' }}</h3>

        <form @submit.prevent="saveVoucher" class="space-y-4">
          
          <div>
            <label class="block text-label-md font-bold text-on-surface-variant mb-1">Mã Voucher (Code)</label>
            <input 
              v-model="formData.code" 
              type="text" 
              required
              placeholder="VD: TET2024, GIAM50K"
              class="w-full px-4 py-3 bg-surface-container-lowest border border-outline-variant/50 rounded-xl text-body-lg text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary uppercase font-mono"
            >
          </div>

          <div>
            <label class="block text-label-md font-bold text-on-surface-variant mb-1">Số tiền giảm (VNĐ)</label>
            <input 
              v-model.number="formData.discountAmount" 
              type="number" 
              required
              min="0"
              class="w-full px-4 py-3 bg-surface-container-lowest border border-outline-variant/50 rounded-xl text-body-lg text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            >
          </div>

          <div>
            <label class="block text-label-md font-bold text-on-surface-variant mb-1">Số điểm cần thiết để đổi (pts)</label>
            <input 
              v-model.number="formData.pointsCost" 
              type="number" 
              required
              min="0"
              class="w-full px-4 py-3 bg-surface-container-lowest border border-outline-variant/50 rounded-xl text-body-lg text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            >
          </div>

          <div class="flex items-center gap-3 pt-2">
            <input 
              type="checkbox" 
              id="isActive" 
              v-model="formData.isActive"
              class="w-5 h-5 rounded text-primary focus:ring-primary"
            >
            <label for="isActive" class="text-body-lg text-on-surface font-medium cursor-pointer">
              Kích hoạt Voucher này
            </label>
          </div>

          <div class="pt-6">
            <button 
              type="submit" 
              :disabled="saving"
              class="w-full bg-primary text-white py-3 rounded-xl font-bold tracking-wide hover:bg-primary/90 active:scale-95 transition-all disabled:opacity-50 disabled:pointer-events-none flex items-center justify-center gap-2"
            >
              <span v-if="saving" class="material-symbols-outlined animate-spin">progress_activity</span>
              {{ saving ? 'Đang lưu...' : 'Lưu Voucher' }}
            </button>
          </div>
        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();

const vouchers = ref([]);
const loading = ref(true);
const saving = ref(false);

const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
  id: null,
  code: '',
  discountAmount: 0,
  pointsCost: 0,
  isActive: true
});

const fetchVouchers = async () => {
  loading.value = true;
  try {
    const response = await api.get('/vouchers/admin');
    vouchers.value = response.data;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  isEditing.value = false;
  formData.value = {
    id: null,
    code: '',
    discountAmount: 50000,
    pointsCost: 1000,
    isActive: true
  };
  showModal.value = true;
};

const openEditModal = (voucher) => {
  isEditing.value = true;
  formData.value = { ...voucher };
  showModal.value = true;
};

const saveVoucher = async () => {
  saving.value = true;
  try {
    if (isEditing.value) {
      await api.put(`/vouchers/admin/${formData.value.id}`, formData.value);
    } else {
      await api.post('/vouchers/admin', formData.value);
    }
    showModal.value = false;
    await fetchVouchers();
  } catch (error) {
    console.error(error);
    alert("Lưu thất bại: " + (error.response?.data?.message || error.message));
  } finally {
    saving.value = false;
  }
};

const deleteVoucher = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa Voucher này?')) return;
  try {
    await api.delete(`/vouchers/admin/${id}`);
    await fetchVouchers();
  } catch (error) {
    console.error(error);
    alert("Xóa thất bại");
  }
};

defineExpose({
  openCreateModal
});

onMounted(() => {
  fetchVouchers();
});
</script>
