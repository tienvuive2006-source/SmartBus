<template>
  <div class="pb-8">
    <main class="max-w-3xl mx-auto space-y-6">
      <!-- Tabs -->
      <div class="flex border-b border-surface-variant mb-6 overflow-x-auto no-scrollbar">
        <button 
          @click="activeTab = 'upcoming'"
          :class="['px-4 py-3 border-b-2 text-body-md font-body-md font-medium whitespace-nowrap transition-colors', activeTab === 'upcoming' ? 'border-primary text-primary' : 'border-transparent text-on-surface-variant hover:text-primary']">
          Sắp đi
        </button>
        <button 
          @click="activeTab = 'completed'"
          :class="['px-4 py-3 border-b-2 text-body-md font-body-md whitespace-nowrap transition-colors', activeTab === 'completed' ? 'border-primary text-primary font-medium' : 'border-transparent text-on-surface-variant hover:text-primary']">
          Đã hoàn thành
        </button>
        <button 
          @click="activeTab = 'cancelled'"
          :class="['px-4 py-3 border-b-2 text-body-md font-body-md whitespace-nowrap transition-colors', activeTab === 'cancelled' ? 'border-primary text-primary font-medium' : 'border-transparent text-on-surface-variant hover:text-primary']">
          Đã hủy
        </button>
      </div>

      <!-- Ticket Cards List -->
      <div class="flex flex-col gap-6" v-if="activeTab === 'upcoming'">
        <!-- Card 1: Sắp đi -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden border border-surface-variant relative">
          <!-- Status Bar Top -->
          <div class="h-1 w-full bg-green-500"></div>
          <div class="p-4 sm:p-6">
            <!-- Header: Status & Route -->
            <div class="flex justify-between items-start mb-4">
              <div class="flex flex-col">
                <span class="inline-flex items-center gap-1 bg-green-50 text-green-700 text-label-md font-label-md px-2 py-1 rounded-full mb-2 w-max">
                  <span class="material-symbols-outlined text-[14px]">schedule</span>
                  Sắp đi
                </span>
                <h2 class="text-headline-sm font-headline-sm text-on-surface">Hà Nội - Hải Phòng</h2>
                <p class="text-body-md font-body-md text-on-surface-variant mt-1">Hải Âu • Limousine 9 chỗ</p>
              </div>
              <button class="p-2 bg-surface-container rounded-lg text-primary hover:bg-primary-fixed-dim/20 transition-colors">
                <span class="material-symbols-outlined text-[28px]">qr_code_2</span>
              </button>
            </div>
            <!-- Divider -->
            <div class="border-t border-dashed border-outline-variant my-4 relative">
              <div class="absolute -left-6 sm:-left-8 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-surface-variant"></div>
              <div class="absolute -right-6 sm:-right-8 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-surface-variant"></div>
            </div>
            <!-- Details Footer -->
            <div class="flex justify-between items-end">
              <div class="grid grid-cols-2 gap-x-6 gap-y-2">
                <div>
                  <p class="text-label-md font-label-md text-on-surface-variant uppercase">Ngày đi</p>
                  <p class="text-body-md font-body-md font-medium text-on-surface">15/10/2023, 08:00</p>
                </div>
                <div>
                  <p class="text-label-md font-label-md text-on-surface-variant uppercase">Ghế</p>
                  <p class="text-ticket-number font-ticket-number text-on-surface">A12</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-label-md font-label-md text-on-surface-variant uppercase mb-1">Tổng tiền</p>
                <p class="text-headline-sm font-headline-sm text-primary">250.000đ</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Card 2: Sắp đi -->
        <div class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] overflow-hidden border border-surface-variant relative">
          <div class="h-1 w-full bg-green-500"></div>
          <div class="p-4 sm:p-6">
            <div class="flex justify-between items-start mb-4">
              <div class="flex flex-col">
                <span class="inline-flex items-center gap-1 bg-green-50 text-green-700 text-label-md font-label-md px-2 py-1 rounded-full mb-2 w-max">
                  <span class="material-symbols-outlined text-[14px]">schedule</span>
                  Sắp đi
                </span>
                <h2 class="text-headline-sm font-headline-sm text-on-surface">Đà Nẵng - Huế</h2>
                <p class="text-body-md font-body-md text-on-surface-variant mt-1">Phương Trang • Giường nằm 40 chỗ</p>
              </div>
              <button class="p-2 bg-surface-container rounded-lg text-primary hover:bg-primary-fixed-dim/20 transition-colors">
                <span class="material-symbols-outlined text-[28px]">qr_code_2</span>
              </button>
            </div>
            <div class="border-t border-dashed border-outline-variant my-4 relative">
              <div class="absolute -left-6 sm:-left-8 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-surface-variant"></div>
              <div class="absolute -right-6 sm:-right-8 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-surface-variant"></div>
            </div>
            <div class="flex justify-between items-end">
              <div class="grid grid-cols-2 gap-x-6 gap-y-2">
                <div>
                  <p class="text-label-md font-label-md text-on-surface-variant uppercase">Ngày đi</p>
                  <p class="text-body-md font-body-md font-medium text-on-surface">20/10/2023, 14:30</p>
                </div>
                <div>
                  <p class="text-label-md font-label-md text-on-surface-variant uppercase">Ghế</p>
                  <p class="text-ticket-number font-ticket-number text-on-surface">B05, B06</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-label-md font-label-md text-on-surface-variant uppercase mb-1">Tổng tiền</p>
                <p class="text-headline-sm font-headline-sm text-primary">300.000đ</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'completed'" class="text-center py-10">
        <span class="material-symbols-outlined text-6xl text-outline-variant mb-4">history</span>
        <p class="text-body-md text-on-surface-variant">Chưa có chuyến đi nào hoàn thành.</p>
      </div>

      <div v-else-if="activeTab === 'cancelled'" class="text-center py-10">
        <span class="material-symbols-outlined text-6xl text-outline-variant mb-4">cancel</span>
        <p class="text-body-md text-on-surface-variant">Chưa có chuyến đi nào bị hủy.</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const activeTab = ref('upcoming');
</script>
