<template>
  <div class="pb-32 -mx-container-margin px-container-margin -mt-stack-space pt-stack-space">
    <!-- Top App Bar -->
    <header class="fixed top-0 w-full z-50 bg-surface shadow-sm flex items-center justify-between px-container-margin h-touch-target-min left-0 right-0 max-w-7xl mx-auto">
      <div class="flex items-center gap-4">
        <button @click="$router.back()" class="active:scale-95 duration-100 p-2 rounded-full hover:bg-surface-container-high transition-colors">
          <span class="material-symbols-outlined text-primary">arrow_back</span>
        </button>
        <h1 class="font-headline-sm text-headline-sm font-bold text-primary">Chi tiết chuyến xe</h1>
      </div>
      <button class="active:scale-95 duration-100 p-2 rounded-full hover:bg-surface-container-high transition-colors">
        <span class="material-symbols-outlined text-primary">share</span>
      </button>
    </header>

    <main class="mt-8 space-y-6">
      <!-- Progress Stepper -->
      <div class="py-4 flex items-center justify-center">
        <div class="flex items-center w-full max-w-md">
          <div class="flex flex-col items-center flex-1">
            <div class="w-8 h-8 rounded-full bg-secondary-container text-white flex items-center justify-center font-bold text-sm">1</div>
            <span class="text-label-md font-label-md mt-1 text-secondary">Chọn ghế</span>
          </div>
          <div class="h-0.5 flex-1 bg-outline-variant"></div>
          <div class="flex flex-col items-center flex-1">
            <div class="w-8 h-8 rounded-full bg-surface-variant text-on-surface-variant flex items-center justify-center font-bold text-sm">2</div>
            <span class="text-label-md font-label-md mt-1 text-on-surface-variant">Thông tin</span>
          </div>
          <div class="h-0.5 flex-1 bg-outline-variant"></div>
          <div class="flex flex-col items-center flex-1">
            <div class="w-8 h-8 rounded-full bg-surface-variant text-on-surface-variant flex items-center justify-center font-bold text-sm">3</div>
            <span class="text-label-md font-label-md mt-1 text-on-surface-variant">Thanh toán</span>
          </div>
        </div>
      </div>

      <!-- Trip Summary Card -->
      <section class="bg-white rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden border border-outline-variant/20">
        <div class="p-4 bg-primary-container text-white">
          <div class="flex justify-between items-start">
            <div>
              <p class="font-label-md text-label-md opacity-80">Hải Phòng → Hà Nội</p>
              <h2 class="font-headline-sm text-headline-sm">Xe Limousine 9 Chỗ</h2>
            </div>
            <div class="text-right">
              <p class="font-label-md text-label-md opacity-80">Khởi hành</p>
              <p class="font-headline-sm text-headline-sm">08:30</p>
            </div>
          </div>
        </div>
        <div class="p-4 grid grid-cols-3 gap-2">
          <div class="flex flex-col items-center p-2 rounded-lg bg-surface-container-low">
            <span class="material-symbols-outlined text-primary mb-1">wifi</span>
            <span class="text-[10px] font-medium uppercase text-on-surface-variant">Wifi Free</span>
          </div>
          <div class="flex flex-col items-center p-2 rounded-lg bg-surface-container-low">
            <span class="material-symbols-outlined text-primary mb-1">ac_unit</span>
            <span class="text-[10px] font-medium uppercase text-on-surface-variant">Điều hòa</span>
          </div>
          <div class="flex flex-col items-center p-2 rounded-lg bg-surface-container-low">
            <span class="material-symbols-outlined text-primary mb-1">local_drink</span>
            <span class="text-[10px] font-medium uppercase text-on-surface-variant">Nước lọc</span>
          </div>
        </div>
      </section>

      <!-- Main Layout: Bento Style -->
      <div class="grid grid-cols-1 md:grid-cols-12 gap-6">
        <!-- Seat Selector -->
        <section class="md:col-span-7 bg-white p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20">
          <div class="flex justify-between items-center mb-6">
            <h3 class="font-headline-sm text-headline-sm text-on-surface">Chọn chỗ ngồi</h3>
            <div class="flex gap-4">
              <div class="flex items-center gap-1">
                <div class="w-3 h-3 rounded-sm border border-outline-variant"></div>
                <span class="text-label-md font-label-md text-on-surface-variant">Trống</span>
              </div>
              <div class="flex items-center gap-1">
                <div class="w-3 h-3 rounded-sm bg-primary-container"></div>
                <span class="text-label-md font-label-md text-on-surface-variant">Đang chọn</span>
              </div>
              <div class="flex items-center gap-1">
                <div class="w-3 h-3 rounded-sm bg-surface-dim"></div>
                <span class="text-label-md font-label-md text-on-surface-variant">Đã đặt</span>
              </div>
            </div>
          </div>

          <!-- Vehicle Map View -->
          <div class="relative bg-surface-container-lowest border-2 border-dashed border-outline-variant/30 rounded-2xl p-8 flex flex-col items-center">
            <div class="w-full flex justify-end mb-12 px-12">
              <div class="flex flex-col items-center opacity-40">
                <span class="material-symbols-outlined text-3xl">directions_run</span>
                <span class="text-[10px] font-bold">TÀI XẾ</span>
              </div>
            </div>
            
            <div class="grid grid-cols-[1fr_1fr_24px_1fr_1fr] gap-3">
              <button class="w-12 h-12 rounded-lg border-2 border-primary-container bg-primary-container text-white flex items-center justify-center font-bold">A1</button>
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">A2</button>
              <div class="w-6"></div> <!-- Aisle -->
              <button class="w-12 h-12 rounded-lg bg-surface-dim text-on-surface-variant flex items-center justify-center font-bold cursor-not-allowed">A3</button>
              <button class="w-12 h-12 rounded-lg bg-surface-dim text-on-surface-variant flex items-center justify-center font-bold cursor-not-allowed">A4</button>
              
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">B1</button>
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">B2</button>
              <div class="w-6"></div> <!-- Aisle -->
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">B3</button>
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">B4</button>
              
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">C1</button>
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">C2</button>
              <div class="w-6"></div> <!-- Aisle -->
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">C3</button>
              <button class="w-12 h-12 rounded-lg border-2 border-outline-variant text-on-surface-variant flex items-center justify-center font-bold">C4</button>
            </div>
          </div>
        </section>

        <!-- Route Details -->
        <div class="md:col-span-5 space-y-6">
          <section class="bg-white p-6 rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20">
            <h3 class="font-headline-sm text-headline-sm text-on-surface mb-4">Điểm đón & trả</h3>
            <div class="space-y-6 relative">
              <div class="absolute left-3 top-2 bottom-2 w-0.5 bg-outline-variant/30"></div>
              <div class="relative pl-10">
                <div class="absolute left-0 top-0 w-6 h-6 rounded-full bg-primary-container flex items-center justify-center border-4 border-white shadow-sm z-10">
                  <div class="w-1.5 h-1.5 rounded-full bg-white"></div>
                </div>
                <p class="font-ticket-number text-ticket-number text-primary">08:30 • 15 Tháng 5</p>
                <h4 class="font-body-lg text-body-lg font-bold">Bến xe Niệm Nghĩa</h4>
                <p class="text-body-md font-body-md text-on-surface-variant">273 Trần Nguyên Hãn, Lê Chân, Hải Phòng</p>
              </div>
              <div class="relative pl-10">
                <div class="absolute left-0 top-0 w-6 h-6 rounded-full bg-secondary-container flex items-center justify-center border-4 border-white shadow-sm z-10">
                  <div class="w-1.5 h-1.5 rounded-full bg-white"></div>
                </div>
                <p class="font-ticket-number text-ticket-number text-secondary">10:30 • 15 Tháng 5</p>
                <h4 class="font-body-lg text-body-lg font-bold">Bến xe Mỹ Đình</h4>
                <p class="text-body-md font-body-md text-on-surface-variant">Số 20 Phạm Hùng, Mỹ Đình, Từ Liêm, Hà Nội</p>
              </div>
            </div>
          </section>

          <section class="rounded-xl overflow-hidden h-40 relative group">
            <img alt="Bản đồ lộ trình" class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBWmpb5KuW7_iI3VfpdA3KTbi9mL-0uus0IjBIXg3QjrZNDrQJMJwLVt6XOirrsmMJuLfYcNrnrUVUfyMJ7KMVvG44LKPM-ksDuzcJ6ESTXD_-VFvH61Ul4MUHU5ZVnIkOmrMSIisB6FX6KiXrh6Y3FoibnyURiJpnzHezfusvt6gA0geSu91SSRYgX78BELus9PSCosg4d8JIs5j5UQ8leaWjLhcT7H_gY6qj7Q8v2vgzDvDe7CX6n3S7tHU9kJZGPzvWVeiSf2tzP"/>
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex items-end p-4">
              <span class="text-white font-label-md text-label-md flex items-center gap-2">
                <span class="material-symbols-outlined text-sm">map</span>
                Xem lộ trình chi tiết
              </span>
            </div>
          </section>
        </div>
      </div>
    </main>

    <!-- Bottom Navigation Payment Bar -->
    <footer class="fixed bottom-0 left-0 w-full z-50 bg-surface shadow-[0px_-4px_12px_rgba(0,0,0,0.05)] px-container-margin py-4">
      <div class="max-w-4xl mx-auto flex items-center justify-between">
        <div class="flex flex-col">
          <div class="flex items-center gap-2">
            <span class="text-on-surface-variant font-label-md text-label-md">Ghế đã chọn:</span>
            <span class="px-2 py-0.5 bg-primary-container text-white rounded font-bold text-xs">A1</span>
          </div>
          <div class="flex items-baseline gap-1 mt-1">
            <span class="text-on-surface font-headline-md text-headline-md">250.000</span>
            <span class="text-on-surface-variant font-label-md text-label-md">VNĐ</span>
          </div>
        </div>
        <button @click="$router.push('/booking/payment')" class="bg-secondary-container hover:bg-secondary transition-all active:scale-90 duration-200 text-on-primary-container px-8 py-3 rounded-xl font-headline-sm text-headline-sm flex items-center gap-2 shadow-lg shadow-secondary-container/20">
          Tiếp tục
          <span class="material-symbols-outlined">arrow_forward</span>
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup>
</script>
