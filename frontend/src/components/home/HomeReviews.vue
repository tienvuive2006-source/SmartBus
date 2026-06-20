<template>
  <div v-if="topReviews.length > 0" class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 py-16">
    <div class="text-center mb-12">
      <h2 class="text-2xl md:text-3xl font-black text-slate-800 tracking-tight">Khách hàng nói gì về Trung Nam?</h2>
      <p class="text-slate-500 font-medium mt-2 text-sm md:text-base">Đánh giá chân thực từ những hành khách đã trải nghiệm dịch vụ</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8">
      <div v-for="review in topReviews" :key="review.id" class="bg-white rounded-3xl p-6 lg:p-8 shadow-sm border border-slate-100 relative hover:shadow-lg transition-all duration-300">
        <!-- Quote Icon -->
        <span class="material-symbols-outlined text-[#075955]/10 text-6xl absolute top-4 right-4 z-0">format_quote</span>
        
        <!-- Rating -->
        <div class="flex gap-1 text-amber-400 mb-4 relative z-10">
          <span v-for="i in review.rating" :key="i" class="material-symbols-outlined text-xl" style="font-variation-settings: 'FILL' 1;">star</span>
        </div>

        <!-- Comment -->
        <p class="text-slate-600 font-medium italic leading-relaxed mb-6 min-h-[80px] relative z-10">
          "{{ review.comment }}"
        </p>

        <!-- User Info -->
        <div class="flex items-center gap-4 mt-auto border-t border-slate-100 pt-4 relative z-10">
          <div class="w-12 h-12 rounded-full bg-gradient-to-br from-emerald-100 to-teal-50 flex items-center justify-center font-bold text-teal-700 border border-teal-100">
            {{ review.user?.fullName?.charAt(0)?.toUpperCase() || 'K' }}
          </div>
          <div>
            <p class="font-bold text-slate-800">{{ review.user?.fullName || 'Khách hàng ẩn danh' }}</p>
            <p v-if="review.booking?.trip" class="text-xs text-slate-400 mt-0.5">
              Đã đi tuyến: <span class="font-semibold text-slate-500">{{ review.booking.trip.departurePoint?.split(',')[0] }} ➝ {{ review.booking.trip.arrivalPoint?.split(',')[0] }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  topReviews: {
    type: Array,
    required: true
  }
});
</script>
