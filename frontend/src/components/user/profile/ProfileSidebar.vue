<template>
  <div class="space-y-8">
    <div class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] text-center relative overflow-hidden">
      <div class="absolute top-0 left-0 w-full h-24 bg-gradient-to-r from-emerald-50 to-teal-50"></div>
      <div class="relative z-10 flex flex-col items-center">
        <div class="relative mb-4">
          <img 
            alt="Profile Avatar" 
            class="w-28 h-28 rounded-full object-cover border-4 border-white shadow-md bg-white" 
            :src="user.avatarUrl || createAvatarFallback(user.fullName)"
            referrerpolicy="no-referrer"
            @error="handleAvatarError($event, user.fullName)"
          />
          <div class="absolute bottom-0 right-0 bg-emerald-500 border-2 border-white w-7 h-7 rounded-full flex items-center justify-center shadow-sm">
            <span class="material-symbols-outlined text-[14px] text-white font-black">verified</span>
          </div>
        </div>
        <h2 class="text-xl font-black text-gray-900">{{ user.fullName }}</h2>
        <p class="text-sm font-semibold text-gray-500 mb-4">
          <span v-if="user.phone?.startsWith('GG_')" class="italic text-orange-500 font-mono text-xs">{{ user.phone }}</span>
          <span v-else>{{ user.phone }}</span>
        </p>
        <div class="flex flex-col items-center gap-2">
          <span class="bg-emerald-50 text-emerald-700 text-[10px] font-black uppercase tracking-widest px-4 py-1.5 rounded-full border border-emerald-100 shadow-sm">
            {{ user.role === 'ADMIN' ? 'Quản trị viên' : 'Thành viên VIP' }}
          </span>
          
          <span v-if="user.authProvider === 'GOOGLE' || user.phone?.startsWith('GG_')" class="px-3 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-flex items-center gap-1.5 border shadow-sm bg-red-50 text-red-600 border-red-100 mt-1">
            <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" class="w-3 h-3" />
            Đăng nhập bằng Google
          </span>
          <span v-else class="px-3 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-flex items-center gap-1.5 border shadow-sm bg-slate-50 text-slate-600 border-slate-200 mt-1">
            <span class="material-symbols-outlined text-[12px]">password</span>
            Đăng ký Truyền thống
          </span>
        </div>
      </div>
    </div>

    <!-- Quick Navigation -->
    <div class="bg-white rounded-3xl p-4 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
       <nav class="flex flex-col space-y-1">
         <button @click="$router.push('/history')" class="flex items-center gap-3 w-full p-4 rounded-2xl hover:bg-gray-50 transition-colors text-left group">
           <span class="material-symbols-outlined text-gray-400 group-hover:text-[#075955] transition-colors">receipt_long</span>
           <span class="text-sm font-bold text-gray-700 group-hover:text-gray-900">Lịch sử đặt vé</span>
         </button>
         <div class="h-[1px] bg-gray-100 my-2"></div>
         
         <button @click="$emit('logout')" class="flex items-center gap-3 w-full p-4 rounded-2xl hover:bg-red-50 transition-colors text-left group">
           <span class="material-symbols-outlined text-red-400 group-hover:text-red-600 transition-colors">logout</span>
           <span class="text-sm font-bold text-red-500 group-hover:text-red-600">Đăng xuất</span>
         </button>
       </nav>
    </div>
  </div>
</template>

<script setup>
import { createAvatarFallback, handleAvatarError } from '@/utils/avatar'

defineProps({
  user: {
    type: Object,
    required: true
  }
});
defineEmits(['logout']);
</script>
