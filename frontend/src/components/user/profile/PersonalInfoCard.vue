<template>
  <section class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-sm font-black text-gray-900 uppercase tracking-widest">Thông tin cá nhân</h3>
      <div class="flex gap-2">
        <button v-if="isEditing" @click="cancelEdit" class="text-[10px] font-black text-gray-500 uppercase tracking-widest bg-gray-100 px-3 py-1.5 rounded-lg hover:bg-gray-200 transition-colors">
          Hủy
        </button>
        <button v-if="isEditing" @click="handleSave" class="text-[10px] font-black text-white uppercase tracking-widest bg-emerald-600 px-4 py-1.5 rounded-lg hover:bg-emerald-700 transition-colors shadow-sm">
          Lưu
        </button>
        <button v-else @click="startEdit" class="text-[10px] font-black text-[#075955] uppercase tracking-widest bg-emerald-50 px-3 py-1.5 rounded-lg hover:bg-emerald-100 transition-colors">
          Chỉnh sửa
        </button>
      </div>
    </div>
    
    <div class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5">Họ và tên</p>
          <input v-if="isEditing" v-model="editForm.fullName" type="text" class="w-full text-sm font-black text-gray-900 bg-white p-3 rounded-xl border border-emerald-300 focus:ring-2 focus:ring-emerald-500 outline-none transition-all" />
          <p v-else class="text-sm font-black text-gray-900 bg-gray-50 p-3 rounded-xl border border-gray-100">{{ user.fullName }}</p>
        </div>
        <div>
          <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5">Số điện thoại</p>
          <input v-if="isEditing" v-model="editForm.phone" type="text" class="w-full text-sm font-black text-gray-900 bg-white p-3 rounded-xl border border-emerald-300 focus:ring-2 focus:ring-emerald-500 outline-none transition-all" />
          <p v-else class="text-sm font-black text-gray-900 bg-gray-50 p-3 rounded-xl border border-gray-100">
            <span v-if="user.phone?.startsWith('GG_')" class="italic text-orange-500 font-mono font-normal">{{ user.phone }}</span>
            <span v-else>{{ user.phone }}</span>
          </p>
        </div>
      </div>
      <div>
          <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5">Email liên hệ</p>
          <input v-if="isEditing" v-model="editForm.email" type="email" class="w-full text-sm font-black text-gray-900 bg-white p-3 rounded-xl border border-emerald-300 focus:ring-2 focus:ring-emerald-500 outline-none transition-all" />
          <p v-else class="text-sm font-black text-gray-900 bg-gray-50 p-3 rounded-xl border border-gray-100">{{ user.email || 'Chưa cập nhật' }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});
const emit = defineEmits(['save']);

const isEditing = ref(false);
const editForm = ref({
  fullName: '',
  phone: '',
  email: ''
});

const startEdit = () => {
  editForm.value = {
    fullName: props.user.fullName,
    phone: props.user.phone?.startsWith('GG_') ? '' : props.user.phone,
    email: props.user.email || ''
  };
  isEditing.value = true;
};

const cancelEdit = () => {
  isEditing.value = false;
};

const handleSave = () => {
  emit('save', { ...editForm.value });
};

// Expose a method to close edit mode if parent api call succeeds
defineExpose({
  cancelEdit
});
</script>
