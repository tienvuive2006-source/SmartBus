<template>
  <div class="flex-1 flex flex-col w-full max-w-3xl mx-auto relative pb-[140px] md:pb-24 pt-4 px-4 gap-6">
    <!-- Date Header -->
    <div class="text-center text-label-md font-label-md text-outline py-2">
      Hôm nay
    </div>

    <!-- AI Message -->
    <div class="flex gap-3 max-w-[85%]">
      <div class="flex-shrink-0 w-10 h-10 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center shadow-sm">
        <span class="material-symbols-outlined">smart_toy</span>
      </div>
      <div class="flex flex-col gap-2">
        <div class="bg-surface-container-low text-on-surface p-4 rounded-2xl rounded-tl-none shadow-sm text-body-lg font-body-lg border border-surface-container-highest">
          Xin chào! Tôi là Trợ lý AI của Trung Nam. Tôi có thể giúp gì cho chuyến đi tiếp theo của bạn?
        </div>
        <!-- Quick Actions / Suggestions -->
        <div class="flex flex-wrap gap-2 mt-2">
          <button @click="$router.push('/booking/search')" class="bg-surface border border-outline-variant text-primary hover:bg-primary-fixed-dim/20 transition-colors px-4 py-2 rounded-full text-label-md font-label-md shadow-sm">
            Tìm chuyến đi gần nhất
          </button>
          <button @click="$router.push('/history')" class="bg-surface border border-outline-variant text-primary hover:bg-primary-fixed-dim/20 transition-colors px-4 py-2 rounded-full text-label-md font-label-md shadow-sm">
            Kiểm tra vé của tôi
          </button>
          <button class="bg-surface border border-outline-variant text-primary hover:bg-primary-fixed-dim/20 transition-colors px-4 py-2 rounded-full text-label-md font-label-md shadow-sm">
            Hủy vé
          </button>
        </div>
      </div>
    </div>

    <!-- User Message Example -->
    <div class="flex gap-3 max-w-[85%] self-end">
      <div class="bg-primary text-on-primary p-4 rounded-2xl rounded-tr-none shadow-sm text-body-lg font-body-lg">
        Tôi muốn tìm vé đi Đà Lạt vào ngày mai.
      </div>
    </div>

    <!-- AI Typing Indicator -->
    <div class="flex gap-3 max-w-[85%]">
      <div class="flex-shrink-0 w-10 h-10 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center shadow-sm">
        <span class="material-symbols-outlined">smart_toy</span>
      </div>
      <div class="bg-surface-container-low p-4 rounded-2xl rounded-tl-none shadow-sm border border-surface-container-highest flex items-center gap-1">
        <div class="w-2 h-2 bg-outline rounded-full animate-bounce"></div>
        <div class="w-2 h-2 bg-outline rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
        <div class="w-2 h-2 bg-outline rounded-full animate-bounce" style="animation-delay: 0.4s"></div>
      </div>
    </div>

    <!-- Message Input Area -->
    <div class="fixed bottom-[72px] md:bottom-0 left-0 w-full bg-surface-bright/90 backdrop-blur-md border-t border-surface-container shadow-[0px_-4px_12px_rgba(0,0,0,0.05)] z-40">
      <div class="max-w-3xl mx-auto px-4 py-3 flex items-end gap-2">
        <button class="text-on-surface-variant p-2 hover:bg-surface-variant rounded-full transition-colors flex-shrink-0">
          <span class="material-symbols-outlined">attach_file</span>
        </button>
        <div class="flex-1 bg-[#F1F3F4] rounded-xl relative focus-within:ring-2 focus-within:ring-primary transition-all">
          <textarea class="w-full bg-transparent border-none focus:ring-0 resize-none py-3 px-4 text-body-lg font-body-lg text-on-surface placeholder:text-outline max-h-32 no-scrollbar" placeholder="Nhập tin nhắn của bạn..." rows="1"></textarea>
        </div>
        <button class="bg-primary text-on-primary p-3 rounded-xl hover:bg-primary/90 transition-colors shadow-sm flex-shrink-0">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">send</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
