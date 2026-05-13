<template>
  <div class="space-y-stack-space">
    <!-- API Connection Status Banner -->
    <div :class="[
      'flex items-center gap-3 px-4 py-3 rounded-xl border text-body-md font-body-md transition-all duration-300 shadow-[0px_4px_12px_rgba(0,0,0,0.02)]',
      isConnected === true ? 'bg-success/10 text-success border-success/20' : 
      isConnected === false ? 'bg-error/10 text-error border-error/20' : 
      'bg-surface-container text-on-surface-variant border-outline-variant/30'
    ]">
      <span class="relative flex h-3 w-3">
        <span v-if="isConnected === true" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-success opacity-75"></span>
        <span :class="[
          'relative inline-flex rounded-full h-3 w-3',
          isConnected === true ? 'bg-success' : 
          isConnected === false ? 'bg-error' : 
          'bg-outline animate-pulse'
        ]"></span>
      </span>
      <p class="font-medium flex-1">
        <span v-if="isConnected === true">Đã kết nối Spring Boot: </span>
        <span v-else-if="isConnected === false">Mất kết nối API: </span>
        <span v-else>Đang kiểm tra Backend: </span>
        <span class="font-normal opacity-90">{{ backendMessage }}</span>
      </p>
    </div>

    <!-- Hero / Search Section -->
    <section class="bg-surface-container-low rounded-xl p-6 shadow-sm border border-outline-variant/30">
      <h1 class="text-headline-lg font-headline-lg text-on-surface mb-6">Bạn muốn đi đâu?</h1>
      <form class="space-y-4" @submit.prevent="$router.push('/booking/search')">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- From -->
          <div class="relative">
            <label class="block text-label-md font-label-md text-on-surface-variant mb-1" for="from">Điểm đi</label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-outline">trip_origin</span>
              <input class="w-full pl-10 pr-4 py-3 bg-[#F1F3F4] rounded-lg border-transparent focus:border-primary focus:ring-0 text-body-md font-body-md transition-colors" id="from" placeholder="Chọn điểm đi" type="text" value="Hà Nội"/>
            </div>
          </div>
          <!-- To -->
          <div class="relative">
            <label class="block text-label-md font-label-md text-on-surface-variant mb-1" for="to">Điểm đến</label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-outline">location_on</span>
              <input class="w-full pl-10 pr-4 py-3 bg-[#F1F3F4] rounded-lg border-transparent focus:border-primary focus:ring-0 text-body-md font-body-md transition-colors" id="to" placeholder="Chọn điểm đến" type="text" value="Sapa"/>
            </div>
          </div>
        </div>
        <!-- Date -->
        <div class="relative">
          <label class="block text-label-md font-label-md text-on-surface-variant mb-1" for="date">Ngày khởi hành</label>
          <div class="relative flex items-center">
            <span class="material-symbols-outlined absolute left-3 text-outline">calendar_today</span>
            <input class="w-full pl-10 pr-4 py-3 bg-[#F1F3F4] rounded-lg border-transparent focus:border-primary focus:ring-0 text-body-md font-body-md transition-colors" id="date" type="date" value="2024-05-20"/>
          </div>
        </div>
        <button class="w-full bg-primary text-on-primary py-3 rounded-lg text-body-md font-body-md font-bold shadow-[0px_4px_12px_rgba(0,0,0,0.05)] hover:bg-primary/90 transition-colors mt-2 flex items-center justify-center gap-2" type="submit">
          <span class="material-symbols-outlined">search</span>
          Tìm Kiếm Chuyến Xe
        </button>
      </form>
    </section>

    <!-- Quick Links / Promotions -->
    <section>
      <h2 class="text-headline-sm font-headline-sm text-on-surface mb-4">Khuyến mãi & Tuyến phổ biến</h2>
      <div class="flex overflow-x-auto gap-4 pb-2 snap-x hide-scrollbar">
        <div @click="$router.push('/booking/seat')" class="snap-start shrink-0 w-64 relative rounded-xl overflow-hidden shadow-sm h-32 group cursor-pointer">
          <img alt="Scenic view of Sapa mountains with mist." class="absolute inset-0 w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDjesXSEB0Lu_EX4UF-wqOzK0N1CwPMQKOppG094zsagnkhLFqH5iIS_-AjwOD_BNdzToFo4MoTraMq0Wg2DlVU4Rg5g-5sma6Lc1Lxh3JXqfD9Hm4WcfNEHxzCgspvjQc0Q0lsVmeF3RBN_o5meZlLSgMFStM7R4CcMwO5HgDn_6tiEvySpb3TSYxpnSfF_KvwK3IeOLznp40aBgNBGQ2g6Z25Mak9PpkeePvxpzkjxAgNDu5C3wPR_5FUL9BI-KSArE--J9i9aFCh"/>
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
          <div class="absolute bottom-3 left-3 text-white">
            <p class="text-label-md font-label-md opacity-90">Hà Nội ➔ Sapa</p>
            <p class="text-body-md font-body-md font-bold">Giảm 20%</p>
          </div>
        </div>
        <div @click="$router.push('/booking/seat')" class="snap-start shrink-0 w-64 relative rounded-xl overflow-hidden shadow-sm h-32 group cursor-pointer">
          <img alt="Halong Bay view." class="absolute inset-0 w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuA-oumxP8NM8nwMGjPyyrTuxD3CbwqsaBtMH2LTDePuGN2IqIVUDWKn0MbGc6eG2ezILZT5GshLFC3ABJ-cX1rFoI-2mNO47K8tHQfN3SvBwa2OpucMHLDk1QrjixnV_RvllaoGoj9qHQ40_m_vrTQbS4125wxVJYy2NPd5n4od2sHRfT8XdwSJVYiqrJ039F6i1EF9YK-UdcI6Vfs3ucvEK1Ct3q6jXuQG5ltAFcMORR_g7RjquEcEes3M2j6eGHiAC074adBAfbHz"/>
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
          <div class="absolute bottom-3 left-3 text-white">
            <p class="text-label-md font-label-md opacity-90">Hà Nội ➔ Hạ Long</p>
            <p class="text-body-md font-body-md font-bold">Chỉ từ 150k</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Recommended -->
    <section>
      <h2 class="text-headline-sm font-headline-sm text-on-surface mb-4">Đề xuất cho bạn</h2>
      <div class="space-y-4">
        <!-- Bus Card 1 -->
        <div @click="$router.push('/booking/seat')" class="bg-surface-container-lowest rounded-xl p-4 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20 flex flex-col gap-3 cursor-pointer hover:shadow-md transition-shadow">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                <span class="material-symbols-outlined">directions_bus</span>
              </div>
              <div>
                <h3 class="text-body-lg font-body-lg font-semibold text-on-surface">Sao Việt Express</h3>
                <div class="flex items-center text-label-md font-label-md text-on-surface-variant gap-1">
                  <span class="material-symbols-outlined text-[14px] text-secondary-container" style="font-variation-settings: 'FILL' 1;">star</span>
                  4.8 (1.2k đánh giá)
                </div>
              </div>
            </div>
            <div class="text-right">
              <p class="text-headline-sm font-headline-sm text-primary">350.000đ</p>
              <p class="text-label-md font-label-md text-on-surface-variant">Giường nằm 34 chỗ</p>
            </div>
          </div>
          <div class="border-t border-dashed border-outline-variant/50 pt-3 flex justify-between items-center">
            <div class="flex items-center gap-2">
              <span class="bg-primary/10 text-primary px-2 py-1 rounded-full text-label-md font-label-md font-bold">Sắp khởi hành</span>
              <span class="text-body-md font-body-md text-on-surface">22:00 Hôm nay</span>
            </div>
          </div>
        </div>
        <!-- Bus Card 2 -->
        <div @click="$router.push('/booking/seat')" class="bg-surface-container-lowest rounded-xl p-4 shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20 flex flex-col gap-3 cursor-pointer hover:shadow-md transition-shadow">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                <span class="material-symbols-outlined">directions_bus</span>
              </div>
              <div>
                <h3 class="text-body-lg font-body-lg font-semibold text-on-surface">Hà Sơn Hải Vân</h3>
                <div class="flex items-center text-label-md font-label-md text-on-surface-variant gap-1">
                  <span class="material-symbols-outlined text-[14px] text-secondary-container" style="font-variation-settings: 'FILL' 1;">star</span>
                  4.6 (850 đánh giá)
                </div>
              </div>
            </div>
            <div class="text-right">
              <p class="text-headline-sm font-headline-sm text-primary">310.000đ</p>
              <p class="text-label-md font-label-md text-on-surface-variant">Giường nằm 40 chỗ</p>
            </div>
          </div>
          <div class="border-t border-dashed border-outline-variant/50 pt-3 flex justify-between items-center">
            <div class="flex items-center gap-2">
              <span class="text-body-md font-body-md text-on-surface">23:30 Hôm nay</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const backendMessage = ref('Đang kiểm tra kết nối API...');
const isConnected = ref(null); // null = loading, true = success, false = failed

onMounted(async () => {
  try {
    // Gọi tới Backend Spring Boot trên cổng 8080
    const response = await axios.get('http://localhost:8080/api/health');
    if (response.data && response.data.status === 'UP') {
      isConnected.value = true;
      backendMessage.value = response.data.message;
    } else {
      isConnected.value = false;
      backendMessage.value = 'Lỗi: Phản hồi API không hợp lệ';
    }
  } catch (error) {
    isConnected.value = false;
    backendMessage.value = 'Mất kết nối! Hãy đảm bảo Spring Boot đang chạy tại localhost:8080';
    console.error('API connection error:', error);
  }
});
</script>
