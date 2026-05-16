<template>
  <div class="lg:col-span-5 space-y-6">
    <div class="bg-white rounded-3xl shadow-[0px_12px_32px_rgba(0,0,0,0.05)] border border-outline-variant/20 overflow-hidden flex flex-col h-[550px] sticky top-4">
      <div class="p-5 border-b border-outline-variant/20 flex justify-between items-center shrink-0 bg-surface-bright">
        <div>
          <h4 class="font-black text-body-lg text-on-surface flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">public</span>
            Giám Sát Hạm Đội Trực Tuyến
          </h4>
          <p class="text-[11px] font-medium text-on-surface-variant">Tín hiệu xe thật liên tục gửi về</p>
        </div>
        <span class="bg-emerald-500 text-white px-2.5 py-1 rounded-full text-[10px] font-black shadow-sm animate-pulse">LIVE GPS</span>
      </div>
      
      <div class="flex-1 bg-slate-100 relative w-full h-full z-0">
        <div id="leaflet-map" class="w-full h-full"></div>
        
        <div v-if="mapLoading" class="absolute inset-0 bg-white/80 backdrop-blur-sm flex flex-col items-center justify-center z-[1000]">
          <div class="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mb-2"></div>
          <span class="text-body-md font-bold text-on-surface">Đang thiết lập vệ tinh...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  mapLoading: Boolean
});
</script>

<style scoped>
:deep(.leaflet-popup-content-wrapper) {
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15) !important;
  padding: 0 !important;
}
:deep(.leaflet-popup-content) {
  margin: 10px !important;
}
</style>
