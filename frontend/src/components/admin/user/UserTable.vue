<template>
  <table class="w-full border-collapse text-left min-w-[800px]">
    <thead>
      <tr class="bg-slate-50 text-[11px] font-black uppercase tracking-wider text-slate-500 border-b border-slate-100">
        <th class="px-3 py-4">ID</th>
        <th class="px-3 py-4 whitespace-nowrap">Họ và Tên</th>
        <th class="px-3 py-4">Liên hệ</th>
        <th class="px-3 py-4 whitespace-nowrap">Số vé</th>
        <th class="px-3 py-4 whitespace-nowrap">Quyền hạn</th>
        <th class="px-3 py-4 text-center">Nguồn</th>
        <th class="px-3 py-4 text-center">Trạng thái</th>
        <th class="px-3 py-4 text-right">Số dư Ví SkyPay</th>
        <th class="px-3 py-4 text-center">Thao tác</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-slate-50">
      <tr 
        v-for="user in users" 
        :key="user.id"
        class="hover:bg-slate-50/50 transition-colors duration-150"
      >
        <!-- ID -->
        <td class="px-3 py-4">
          <span class="text-body-sm font-bold text-slate-400">#{{ user.id }}</span>
        </td>

        <!-- Avatar + Name -->
        <td class="px-3 py-4">
          <div class="flex items-center gap-2">
            <img 
              :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=f1f5f9&color=64748b&bold=true`" 
              alt="Avatar" 
              class="w-8 h-8 rounded-full shadow-sm"
            />
            <span class="text-body-md font-black text-slate-800">{{ user.fullName }}</span>
          </div>
        </td>

        <!-- Contact (Phone & Email) -->
        <td class="px-3 py-4">
          <div class="flex flex-col gap-1">
            <div class="text-body-md font-bold text-slate-600 flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[16px] text-slate-400 shrink-0">call</span>
              <span v-if="user.phone?.startsWith('GG_')" class="italic text-orange-500 font-mono text-xs">{{ user.phone }}</span>
              <span v-else>{{ user.phone }}</span>
            </div>
            <div class="text-[12px] font-medium text-slate-500 flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[14px] text-slate-400 shrink-0">mail</span>
              <span v-if="user.email" class="whitespace-nowrap">{{ user.email }}</span>
              <span v-else class="italic text-slate-400 font-normal">Chưa có Email</span>
            </div>
          </div>
        </td>
        <!-- Tickets -->
        <td class="px-3 py-4">
          <span class="text-body-md font-bold text-slate-900 whitespace-nowrap">{{ user.ticketCount || 0 }} vé</span>
        </td>

        <!-- Role Badge -->
        <td class="px-3 py-4">
          <span 
            :class="[
              'px-2 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-block border shadow-sm',
              user.role === 'ADMIN' 
                ? 'bg-red-50 text-red-700 border-red-100' 
                : 'bg-blue-50 text-blue-700 border-blue-100'
            ]"
          >
            {{ user.role }}
          </span>
        </td>

        <!-- Auth Provider Badge -->
        <td class="px-3 py-4">
          <div class="flex justify-center">
            <span v-if="user.authProvider === 'GOOGLE'" class="px-2 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-flex items-center gap-1 border shadow-sm bg-red-50 text-red-600 border-red-100">
              <svg class="w-3 h-3" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              GG
            </span>
            <span v-else class="px-2 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-flex items-center gap-1 border shadow-sm bg-slate-50 text-slate-600 border-slate-200">
              <span class="material-symbols-outlined text-[12px]">password</span>
              LOCAL
            </span>
          </div>
        </td>

        <!-- Status -->
        <td class="px-3 py-4">
          <div class="flex justify-center">
            <span v-if="user.isLocked" class="px-2 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-flex items-center gap-1 border shadow-sm bg-red-50 text-red-600 border-red-200">
              <span class="material-symbols-outlined text-[12px]">lock</span>
              KHÓA
            </span>
            <span v-else class="px-2 py-1 rounded-full text-[9px] font-black tracking-wider uppercase inline-flex items-center gap-1 border shadow-sm bg-emerald-50 text-emerald-600 border-emerald-200">
              <span class="material-symbols-outlined text-[12px]">check_circle</span>
              HĐ
            </span>
          </div>
        </td>

        <!-- Wallet -->
        <td class="px-3 py-4 text-right">
          <span class="text-body-md font-black text-emerald-600">
            {{ user.walletBalance ? user.walletBalance.toLocaleString('vi-VN') : '0' }} đ
          </span>
        </td>

        <!-- Action Controls -->
        <td class="px-3 py-4">
          <div class="flex items-center justify-center gap-1">
            <button 
              @click="$emit('history', user)"
              class="p-2 bg-indigo-50 hover:bg-indigo-500 hover:text-white text-indigo-600 rounded-xl transition-all active:scale-90 shadow-sm"
              title="Xem lịch sử đặt vé"
            >
              <span class="material-symbols-outlined text-sm">receipt_long</span>
            </button>
            <button 
              @click="$emit('edit', user)"
              class="p-2 bg-slate-100 hover:bg-primary hover:text-white text-slate-600 rounded-xl transition-all active:scale-90 shadow-sm"
              title="Sửa thông tin & Nạp tiền"
            >
              <span class="material-symbols-outlined text-sm">edit</span>
            </button>
            <button 
              v-if="user.role !== 'ADMIN'"
              @click="$emit('toggle-lock', user)"
              :class="['p-2 rounded-xl transition-all active:scale-90 shadow-sm', user.isLocked ? 'bg-red-100 text-red-600 hover:bg-red-200' : 'bg-slate-100 text-slate-600 hover:bg-amber-100 hover:text-amber-600']"
              :title="user.isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'"
            >
              <span class="material-symbols-outlined text-sm">{{ user.isLocked ? 'lock' : 'lock_open' }}</span>
            </button>
            <button 
              v-if="user.role !== 'ADMIN' && (!user.ticketCount || user.ticketCount === 0)"
              @click="$emit('delete', user.id)"
              class="p-2 bg-red-50 hover:bg-red-500 hover:text-white text-red-500 rounded-xl transition-all active:scale-90 shadow-sm"
              title="Xoá người dùng"
            >
              <span class="material-symbols-outlined text-sm">delete</span>
            </button>
            <button 
              v-else
              disabled
              class="p-2 bg-slate-50 text-slate-300 rounded-xl cursor-not-allowed shadow-sm"
              title="Không thể xoá tài khoản Admin hoặc khách đã mua vé"
            >
              <span class="material-symbols-outlined text-sm">delete</span>
            </button>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
defineProps({
  users: {
    type: Array,
    required: true
  }
});

defineEmits(['history', 'edit', 'toggle-lock', 'delete']);
</script>
