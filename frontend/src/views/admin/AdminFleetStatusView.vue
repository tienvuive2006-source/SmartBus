<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto pb-24 md:pb-8 relative">
    <!-- Stats Overview Bento Grid -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-surface-container-lowest p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] flex flex-col justify-between">
        <span class="text-on-surface-variant font-label-md text-label-md">Tổng số xe</span>
        <div class="flex items-end justify-between mt-2">
          <span class="font-headline-lg text-headline-lg text-primary">48</span>
          <span class="material-symbols-outlined text-primary-fixed-dim text-4xl">inventory_2</span>
        </div>
      </div>
      <div class="bg-surface-container-lowest p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border-l-4 border-green-500">
        <span class="text-on-surface-variant font-label-md text-label-md">Đang chạy</span>
        <div class="flex items-end justify-between mt-2">
          <span class="font-headline-lg text-headline-lg text-green-600">32</span>
          <span class="material-symbols-outlined text-green-100 text-4xl">bolt</span>
        </div>
      </div>
      <div class="bg-surface-container-lowest p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border-l-4 border-secondary-container">
        <span class="text-on-surface-variant font-label-md text-label-md">Bảo trì</span>
        <div class="flex items-end justify-between mt-2">
          <span class="font-headline-lg text-headline-lg text-secondary">6</span>
          <span class="material-symbols-outlined text-secondary-fixed text-4xl">build</span>
        </div>
      </div>
      <div class="bg-surface-container-lowest p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border-l-4 border-outline">
        <span class="text-on-surface-variant font-label-md text-label-md">Đang nghỉ</span>
        <div class="flex items-end justify-between mt-2">
          <span class="font-headline-lg text-headline-lg text-on-surface-variant">10</span>
          <span class="material-symbols-outlined text-surface-variant text-4xl">bedtime</span>
        </div>
      </div>
    </div>

    <!-- Fleet List & Map Section -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <!-- Fleet Cards Column -->
      <div class="lg:col-span-7 space-y-4">
        <div class="flex items-center justify-between mb-2">
          <h3 class="font-headline-sm text-headline-sm text-on-surface">Danh sách xe hạm đội</h3>
          <div class="flex gap-2">
            <button class="bg-surface-variant px-3 py-1 rounded-full text-label-md font-label-md text-on-surface-variant active:scale-95 transition-all">Tất cả</button>
            <button class="bg-primary px-3 py-1 rounded-full text-label-md font-label-md text-on-primary active:scale-95 transition-all">Đang chạy</button>
          </div>
        </div>

        <!-- Bus Card 1 -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden group hover:shadow-md transition-shadow">
          <div class="p-4 flex gap-4">
            <div class="w-24 h-24 rounded-lg bg-surface-container-high overflow-hidden flex-shrink-0">
              <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDV-qYTXXzoCRW-LjQ4hFo6uHpKsaKMXFccAdtTB7XU4g1rFQpSsu6iT-KLMFj7Z26M303GnhmORXDK3AQOre5Kn07eEsWAhc6y2NCqDyK-B1Nz8Avqyj1hP2Q9-NgQDk9VuVfq5jN1UmiW2qUTS44_NGtVIlRnurjOiWPhjFRFIHifRc_NKUGTI0YcSuWDqvLfgG051k8oTBFkaBPO11Z62fhqut5RJViWvm-P0_5mtLZUWeDFwGTvvFGohVKmFMYNCb_qU4F8n0Im"/>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start">
                <div>
                  <h4 class="font-headline-sm text-headline-sm text-on-surface">TL-2940 Express</h4>
                  <p class="font-body-md text-body-md text-on-surface-variant">Biển số: 29A-888.88</p>
                </div>
                <div class="px-2 py-1 bg-green-100 text-green-700 rounded text-[10px] font-bold uppercase tracking-wider">ĐANG CHẠY</div>
              </div>
              <div class="mt-3 flex items-center gap-4 text-on-surface-variant">
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">person</span>
                  <span class="text-label-md">Tài xế: Nguyễn Văn A</span>
                </div>
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">route</span>
                  <span class="text-label-md">Tuyến: Hà Nội - Hải Phòng</span>
                </div>
              </div>
            </div>
          </div>
          <div class="border-t border-dashed border-outline-variant p-3 bg-surface-container-low flex justify-between items-center">
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
              <span class="text-label-md text-on-surface">Vị trí: Cao tốc 5B - km 42</span>
            </div>
            <button class="text-primary font-bold text-label-md flex items-center gap-1">
              CHI TIẾT <span class="material-symbols-outlined text-xs">arrow_forward</span>
            </button>
          </div>
        </div>

        <!-- Bus Card 2 -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden group hover:shadow-md transition-shadow">
          <div class="p-4 flex gap-4">
            <div class="w-24 h-24 rounded-lg bg-surface-container-high overflow-hidden flex-shrink-0">
              <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDMagmqEEYc6llbvzZ88TOLES5rXcsTooTNlZeaLTe2N5A3J14f23urxojF1UfGhg05jFDJeSrUDiognSBIY52RxO89ynZdNVaA03OD2qlhgXjC51BmFX4L63Aguoc_y79QPtLDvN7gcov4XthXtNXKeDtranbADMA1ee-2c5nLVrsidCjIuz-DA0tR_gIezE2J2KRvneFZIrdUyo7D7Juc2MLSSfQ1_kIrwdXqPYaGQUVcezkexsB3bRF0AnDZfpjAUfJagjBGjrtT"/>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start">
                <div>
                  <h4 class="font-headline-sm text-headline-sm text-on-surface">TL-1502 sleeper</h4>
                  <p class="font-body-md text-body-md text-on-surface-variant">Biển số: 15B-123.45</p>
                </div>
                <div class="px-2 py-1 bg-orange-100 text-orange-700 rounded text-[10px] font-bold uppercase tracking-wider">BẢO TRÌ</div>
              </div>
              <div class="mt-3 flex items-center gap-4 text-on-surface-variant">
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">engineering</span>
                  <span class="text-label-md">Thay dầu định kỳ</span>
                </div>
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">schedule</span>
                  <span class="text-label-md">Dự kiến xong: 16:00 Hôm nay</span>
                </div>
              </div>
            </div>
          </div>
          <div class="border-t border-dashed border-outline-variant p-3 bg-surface-container-low flex justify-between items-center">
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-orange-500"></span>
              <span class="text-label-md text-on-surface">Vị trí: Gara Trung Tâm - Cầu Giấy</span>
            </div>
            <button class="text-primary font-bold text-label-md flex items-center gap-1">
              CHI TIẾT <span class="material-symbols-outlined text-xs">arrow_forward</span>
            </button>
          </div>
        </div>

        <!-- Bus Card 3 -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden group hover:shadow-md transition-shadow">
          <div class="p-4 flex gap-4">
            <div class="w-24 h-24 rounded-lg bg-surface-container-high overflow-hidden flex-shrink-0">
              <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBLASFqQkIzzUjessDazuJWQ3VYAmgjmfukAkth7QRxwJquS4XV84IbPYviBicdpJG6HQsWhKP8WIYbR9hnQ2RCJTEv47-bRgfk2T7siF-ZR7tDPDQVHt8KbeWFD9NMOEisebCVZvnzE9ajANXm6s94Qu4pQKK8PC4a7-rxDTulezzCKgKXsIz83ryl725f24BfWmnuOaUCit2G6va7JgTewoPTiUFPROOHTGdm7qVgqlNCr-GQkQXr7WG8kWFkNAJzkrOaZB9tnKa2"/>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start">
                <div>
                  <h4 class="font-headline-sm text-headline-sm text-on-surface">TL-3011 Luxury</h4>
                  <p class="font-body-md text-body-md text-on-surface-variant">Biển số: 30F-999.01</p>
                </div>
                <div class="px-2 py-1 bg-surface-variant text-on-surface-variant rounded text-[10px] font-bold uppercase tracking-wider">ĐANG NGHỈ</div>
              </div>
              <div class="mt-3 flex items-center gap-4 text-on-surface-variant">
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">person_off</span>
                  <span class="text-label-md">Chưa phân công tài xế</span>
                </div>
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">history</span>
                  <span class="text-label-md">Vừa về từ: Sapa</span>
                </div>
              </div>
            </div>
          </div>
          <div class="border-t border-dashed border-outline-variant p-3 bg-surface-container-low flex justify-between items-center">
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-outline"></span>
              <span class="text-label-md text-on-surface">Vị trí: Bãi đỗ xe Mỹ Đình</span>
            </div>
            <button class="text-primary font-bold text-label-md flex items-center gap-1">
              CHI TIẾT <span class="material-symbols-outlined text-xs">arrow_forward</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Side Widgets Column -->
      <div class="lg:col-span-5 space-y-6">
        <!-- Real-time GPS Map Widget -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden h-80 flex flex-col">
          <div class="p-4 border-b border-surface-container flex justify-between items-center">
            <h4 class="font-headline-sm text-headline-sm text-on-surface">Vị trí thời gian thực</h4>
            <span class="bg-primary-container text-on-primary-container px-2 py-1 rounded text-[10px] font-bold">LIVE</span>
          </div>
          <div class="flex-1 bg-surface-container-high relative overflow-hidden">
            <!-- Placeholder for Map -->
            <img class="w-full h-full object-cover opacity-80 grayscale-[0.5]" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBinl2Qek8Q8sV5l54_EDw_53ImWZtiMGSHZccB5h6xT5atIps2wI81BCSyha44jPjPNQ-_3E7u3wY4wvHClEr-jHNU_gXx2T3A-RkC3AwyVoZmJEFXgj-lxrFQb12KA9VhM5kXKrTpDHy_t6ijiSZsYM1uvGA15Ty61jlZJsUv2onv6JaaYf8ypw3u327sR0oc0utjJO1vrUT6EqRnIdv-dUemlD59M6jDrzbEI1wuoINr6FXe0vB-NCKS511SUuXGAHH7rbrDRTqT"/>
            <!-- Simulated Markers -->
            <div class="absolute top-1/2 left-1/3 bg-primary w-4 h-4 rounded-full border-2 border-white shadow-lg"></div>
            <div class="absolute top-1/4 left-2/3 bg-green-500 w-4 h-4 rounded-full border-2 border-white shadow-lg"></div>
            <div class="absolute bottom-1/3 right-1/4 bg-primary w-4 h-4 rounded-full border-2 border-white shadow-lg"></div>
            <div class="absolute top-10 right-10 bg-white p-2 rounded shadow-md flex items-center gap-2">
              <div class="w-3 h-3 rounded-full bg-primary"></div>
              <span class="text-[10px] font-bold">TL-2940</span>
            </div>
          </div>
        </div>

        <!-- Maintenance Schedule Widget -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] p-6">
          <h4 class="font-headline-sm text-headline-sm text-on-surface mb-4">Lịch bảo trì sắp tới</h4>
          <div class="space-y-4">
            <div class="flex gap-4">
              <div class="flex flex-col items-center justify-center w-12 h-12 rounded bg-primary-fixed text-primary">
                <span class="font-bold text-lg">24</span>
                <span class="text-[10px] uppercase">T.10</span>
              </div>
              <div class="flex-1">
                <p class="font-body-md text-body-md font-semibold text-on-surface">Bảo dưỡng động cơ - TL-3011</p>
                <p class="text-label-md text-on-surface-variant">Định kỳ 50.000km</p>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant">chevron_right</span>
            </div>
            <div class="flex gap-4">
              <div class="flex flex-col items-center justify-center w-12 h-12 rounded bg-surface-container-high text-on-surface-variant">
                <span class="font-bold text-lg">26</span>
                <span class="text-[10px] uppercase">T.10</span>
              </div>
              <div class="flex-1">
                <p class="font-body-md text-body-md font-semibold text-on-surface">Kiểm tra phanh - TL-1502</p>
                <p class="text-label-md text-on-surface-variant">An toàn định kỳ</p>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant">chevron_right</span>
            </div>
            <div class="flex gap-4">
              <div class="flex flex-col items-center justify-center w-12 h-12 rounded bg-surface-container-high text-on-surface-variant">
                <span class="font-bold text-lg">01</span>
                <span class="text-[10px] uppercase">T.11</span>
              </div>
              <div class="flex-1">
                <p class="font-body-md text-body-md font-semibold text-on-surface">Thay lốp toàn diện - TL-4566</p>
                <p class="text-label-md text-on-surface-variant">Nâng cấp mùa đông</p>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant">chevron_right</span>
            </div>
          </div>
          <button class="w-full mt-6 border border-primary text-primary font-bold py-2 rounded-xl text-label-md hover:bg-primary/5 transition-colors">
            XEM TẤT CẢ LỊCH TRÌNH
          </button>
        </div>
      </div>
    </div>

    <!-- Floating Action Button -->
    <button class="fixed bottom-20 right-6 md:bottom-8 md:right-8 bg-secondary-container text-on-secondary-container w-14 h-14 rounded-full shadow-lg flex items-center justify-center active:scale-90 transition-transform z-40">
      <span class="material-symbols-outlined text-3xl">add</span>
    </button>
  </div>
</template>

<script setup>
</script>
