<template>
  <table class="w-full border-collapse text-left min-w-[800px]">
    <thead>
      <tr class="bg-indigo-50/50 text-[11px] font-black uppercase tracking-wider text-indigo-800 border-b border-indigo-100">
        <th class="px-4 py-4">Mã NV</th>
        <th class="px-4 py-4 whitespace-nowrap">Họ và Tên (Tài xế / Nhân viên)</th>
        <th class="px-4 py-4">Tài khoản / SĐT</th>
        <th class="px-4 py-4 text-center">Trạng thái làm việc</th>
        <th class="px-4 py-4 text-center">Chuyến đang chạy</th>
        <th class="px-4 py-4 text-center">Thao tác</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-slate-50">
      <tr 
        v-for="user in users" 
        :key="user.id"
        class="hover:bg-indigo-50/30 transition-colors duration-150"
      >
        <!-- Mã NV -->
        <td class="px-4 py-4">
          <span class="px-2 py-1 bg-slate-100 text-slate-600 rounded-lg text-xs font-mono font-bold shadow-inner">NV{{ String(user.id).padStart(3, '0') }}</span>
        </td>

        <!-- Avatar + Name + Role Badge -->
        <td class="px-4 py-4">
          <div class="flex items-center gap-4">
            <div class="w-[72px] h-[96px] rounded-lg overflow-hidden shrink-0 shadow-sm border border-slate-200 bg-slate-100 relative group">
              <img v-if="user.avatarUrl" :src="user.avatarUrl" class="w-full h-full object-cover transition-transform group-hover:scale-110" />
              <!-- Default 3x4 Placeholder -->
              <div v-else class="w-full h-full flex flex-col items-center justify-center bg-gradient-to-b from-slate-50 to-slate-200"
                   :class="user.role === 'DRIVER' ? 'from-amber-50 to-amber-100/50' : 'from-indigo-50 to-indigo-100/50'">
                 <span class="material-symbols-outlined text-[36px] opacity-50"
                       :class="user.role === 'DRIVER' ? 'text-amber-600' : 'text-indigo-600'">
                    {{ user.role === 'DRIVER' ? 'account_box' : 'badge' }}
                 </span>
              </div>
            </div>
            <div>
              <span class="text-body-md font-black text-slate-800 block leading-tight">{{ user.fullName }}</span>
              <span class="text-[9px] font-black tracking-wider uppercase px-2 py-0.5 rounded-md inline-block mt-1 shadow-sm border"
                    :class="user.role === 'DRIVER' ? 'bg-amber-50 text-amber-600 border-amber-200' : 'bg-indigo-50 text-indigo-600 border-indigo-200'">
                {{ user.role === 'DRIVER' ? 'Tài xế (Lái xe)' : 'Lơ xe (Soát vé)' }}
              </span>
            </div>
          </div>
        </td>

        <!-- Contact -->
        <td class="px-4 py-4">
          <div class="flex flex-col gap-1">
            <div class="text-body-md font-bold text-indigo-900 flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[16px] text-indigo-400 shrink-0">account_circle</span>
              <span v-if="user.username" class="whitespace-nowrap">{{ user.username }}</span>
              <span v-else class="italic text-slate-400 font-normal">Chưa có username</span>
            </div>
            <div class="text-[12px] font-medium text-slate-500 flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[14px] text-slate-400 shrink-0">call</span>
              <span v-if="user.phone" class="whitespace-nowrap">{{ user.phone }}</span>
              <span v-else class="italic text-slate-400 font-normal">Chưa có SĐT</span>
            </div>
          </div>
        </td>

        <!-- Status -->
        <td class="px-4 py-4">
          <div class="flex justify-center">
            <span v-if="user.isLocked" class="px-3 py-1.5 rounded-xl text-[10px] font-black tracking-wider uppercase inline-flex items-center gap-1.5 border shadow-sm bg-red-50 text-red-600 border-red-200">
              <span class="material-symbols-outlined text-[14px]">block</span>
              ĐÌNH CHỈ
            </span>
            <span v-else class="px-3 py-1.5 rounded-xl text-[10px] font-black tracking-wider uppercase inline-flex items-center gap-1.5 border shadow-sm bg-emerald-50 text-emerald-600 border-emerald-200">
              <span class="material-symbols-outlined text-[14px]">verified</span>
              SẴN SÀNG
            </span>
          </div>
        </td>

        <!-- Assigned Trips Count -->
        <td class="px-4 py-4 text-center">
          <span class="text-body-md font-black" :class="user.activeTripCount > 0 ? 'text-amber-600' : 'text-slate-400'">
            {{ user.activeTripCount || 0 }} chuyến
          </span>
        </td>

        <!-- Action Controls -->
        <td class="px-4 py-4">
          <div class="flex items-center justify-center gap-2">
            <button 
              @click="$emit('edit', user)"
              class="p-2.5 bg-indigo-50 hover:bg-indigo-600 hover:text-white text-indigo-600 rounded-xl transition-all active:scale-90 shadow-sm flex items-center gap-1 text-xs font-bold"
            >
              <span class="material-symbols-outlined text-[16px]">edit_document</span> Sửa
            </button>
            <button 
              @click="$emit('toggle-lock', user)"
              :class="['p-2.5 rounded-xl transition-all active:scale-90 shadow-sm flex items-center gap-1 text-xs font-bold', user.isLocked ? 'bg-red-50 text-red-600 hover:bg-red-600 hover:text-white' : 'bg-slate-50 text-slate-600 hover:bg-slate-800 hover:text-white']"
            >
              <span class="material-symbols-outlined text-[16px]">{{ user.isLocked ? 'lock' : 'lock_open' }}</span>
            </button>
            <button 
              @click="$emit('delete', user.id)"
              class="p-2.5 bg-red-50 hover:bg-red-600 hover:text-white text-red-500 rounded-xl transition-all active:scale-90 shadow-sm"
              title="Xoá nhân viên"
            >
              <span class="material-symbols-outlined text-[16px]">person_remove</span>
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

defineEmits(['edit', 'toggle-lock', 'delete']);
</script>
