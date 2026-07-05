<template>
  <div class="flex-1 flex flex-col font-sans bg-white overflow-hidden">
    
    <!-- Tabs Bar (White) -->
    <div class="bg-white border-b border-slate-200 shrink-0 px-4 flex gap-6 z-10 shadow-sm relative">
      <router-link 
        to="/admin/drivers/workspace" 
        class="px-2 py-3 text-[13px] font-bold tracking-wide transition-all border-b-[3px] flex items-center gap-2"
        :class="$route.name === 'admin-drivers-workspace' ? 'border-[#1e5b86] text-[#1e5b86]' : 'border-transparent text-slate-500 hover:text-slate-800'"
      >
        <span class="material-symbols-outlined text-[18px]">calendar_month</span>
        Lịch trình
      </router-link>
      
      <router-link 
        to="/admin/drivers/leave-requests" 
        class="px-2 py-3 text-[13px] font-bold tracking-wide transition-all border-b-[3px] flex items-center gap-2"
        :class="$route.name === 'admin-leave-requests' ? 'border-[#1e5b86] text-[#1e5b86]' : 'border-transparent text-slate-500 hover:text-slate-800'"
      >
        <span class="material-symbols-outlined text-[18px]">event_busy</span>
        Nghỉ phép
      </router-link>
      
      <router-link 
        to="/admin/drivers/locations" 
        class="px-2 py-3 text-[13px] font-bold tracking-wide transition-all border-b-[3px] flex items-center gap-2"
        :class="$route.name === 'admin-driver-locations' ? 'border-[#1e5b86] text-[#1e5b86]' : 'border-transparent text-slate-500 hover:text-slate-800'"
      >
        <span class="material-symbols-outlined text-[18px]">location_on</span>
        Vị trí
      </router-link>
    </div>

    <!-- Main Content Area (Router View) -->
    <div class="flex-1 overflow-hidden bg-white relative">
      <router-view></router-view>
    </div>

  </div>
</template>

<script setup>
// Wrapper component doesn't need logic, just handles routing tabs.
</script>
