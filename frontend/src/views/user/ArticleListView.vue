<template>
  <div class="bg-white min-h-screen pt-24 pb-20">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      
      <!-- Header -->
      <div class="text-center mb-16">
        <h1 class="text-4xl md:text-5xl font-black text-gray-900 tracking-tight mb-4">Tin tức & Khuyến mãi</h1>
        <p class="text-lg text-gray-500 max-w-2xl mx-auto font-medium">Cập nhật những thông tin mới nhất về lịch trình, chính sách và các chương trình ưu đãi đặc biệt từ SmartBus.</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div v-for="i in 6" :key="i" class="animate-pulse flex flex-col">
          <div class="bg-gray-200 h-64 rounded-2xl mb-4 w-full"></div>
          <div class="bg-gray-200 h-6 rounded w-1/3 mb-3"></div>
          <div class="bg-gray-200 h-8 rounded w-3/4 mb-4"></div>
          <div class="bg-gray-200 h-4 rounded w-full mb-2"></div>
          <div class="bg-gray-200 h-4 rounded w-5/6"></div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else-if="articles.length === 0" class="text-center py-20 bg-gray-50 rounded-3xl border border-gray-100">
        <span class="material-symbols-outlined text-6xl text-gray-300 mb-4 block">newspaper</span>
        <h3 class="text-xl font-bold text-gray-900 mb-2">Chưa có bài viết nào</h3>
        <p class="text-gray-500">Chúng tôi sẽ sớm cập nhật các tin tức mới nhất. Vui lòng quay lại sau!</p>
      </div>

      <!-- Article Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <router-link 
          v-for="article in articles" 
          :key="article.id" 
          :to="`/tin-tuc/${article.slug}`"
          class="group flex flex-col bg-white rounded-3xl overflow-hidden border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_8px_30px_rgb(0,0,0,0.08)] hover:-translate-y-1 transition-all duration-300"
        >
          <!-- Thumbnail -->
          <div class="relative h-64 w-full overflow-hidden bg-gray-100">
            <img 
              v-if="article.imageUrl" 
              :src="article.imageUrl" 
              :alt="article.title"
              class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
            />
            <div v-else class="w-full h-full flex items-center justify-center">
              <span class="material-symbols-outlined text-4xl text-gray-300">image</span>
            </div>
            
            <!-- Category Badge (Mock) -->
            <div class="absolute top-4 left-4 bg-white/90 backdrop-blur-sm px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-widest text-[#075955] shadow-sm">
              Tin Mới
            </div>
          </div>

          <!-- Content -->
          <div class="p-6 flex flex-col flex-1">
            <div class="flex items-center text-xs font-bold text-gray-400 uppercase tracking-widest mb-3 gap-2">
              <span class="material-symbols-outlined text-[16px]">calendar_today</span>
              {{ new Date(article.createdAt).toLocaleDateString('vi-VN') }}
            </div>
            
            <h2 class="text-xl font-black text-gray-900 mb-3 line-clamp-2 group-hover:text-[#075955] transition-colors leading-snug">
              {{ article.title }}
            </h2>
            
            <p class="text-gray-600 mb-6 line-clamp-3 text-sm font-medium flex-1">
              {{ article.summary || stripHtml(article.content).substring(0, 150) + '...' }}
            </p>
            
            <div class="mt-auto flex items-center text-[#075955] font-bold text-sm uppercase tracking-widest">
              Đọc tiếp
              <span class="material-symbols-outlined text-[18px] ml-1 transform group-hover:translate-x-1 transition-transform">arrow_forward</span>
            </div>
          </div>
        </router-link>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();
const articles = ref([]);
const loading = ref(true);

const fetchArticles = async () => {
  try {
    const res = await api.get('/articles');
    articles.value = res.data;
  } catch (error) {
    console.error("Lỗi tải bài viết:", error);
  } finally {
    loading.value = false;
  }
};

const stripHtml = (html) => {
  if (!html) return '';
  const doc = new DOMParser().parseFromString(html, 'text/html');
  return doc.body.textContent || "";
};

onMounted(() => {
  fetchArticles();
  window.scrollTo(0, 0);
});
</script>
