<template>
  <div class="bg-white min-h-screen pt-8 pb-20">
    <!-- Loading State -->
    <div v-if="loading" class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 animate-pulse">
      <div class="h-8 bg-gray-200 rounded w-3/4 mb-6"></div>
      <div class="h-4 bg-gray-200 rounded w-1/4 mb-8"></div>
      <div class="h-[400px] bg-gray-200 rounded-3xl mb-8 w-full"></div>
      <div class="space-y-4">
        <div class="h-4 bg-gray-200 rounded w-full"></div>
        <div class="h-4 bg-gray-200 rounded w-full"></div>
        <div class="h-4 bg-gray-200 rounded w-5/6"></div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 text-center py-20">
      <span class="material-symbols-outlined text-6xl text-gray-300 mb-4 block">error</span>
      <h2 class="text-2xl font-black text-gray-900 mb-4">Không tìm thấy bài viết!</h2>
      <p class="text-gray-500 mb-8 font-medium">Bài viết có thể đã bị xóa hoặc đường dẫn không chính xác.</p>
      <router-link to="/tin-tuc" class="bg-[#075955] text-white px-8 py-3 rounded-xl font-bold uppercase tracking-widest inline-flex items-center gap-2 hover:bg-[#064844] transition-colors">
        <span class="material-symbols-outlined text-[20px]">arrow_back</span>
        Quay lại danh sách
      </router-link>
    </div>

    <!-- Article Detail -->
    <article v-else-if="article" class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Breadcrumb -->
      <nav class="flex items-center text-[11px] font-bold text-gray-400 uppercase tracking-widest mb-10 gap-2">
        <router-link to="/" class="hover:text-[#075955] transition-colors">Trang chủ</router-link>
        <span class="material-symbols-outlined text-[14px]">chevron_right</span>
        <router-link to="/tin-tuc" class="hover:text-[#075955] transition-colors">Tin Tức</router-link>
        <span class="material-symbols-outlined text-[14px]">chevron_right</span>
        <span class="text-gray-800 truncate max-w-[200px]">{{ article.title }}</span>
      </nav>

      <!-- Header -->
      <header class="mb-10">
        <h1 class="text-4xl md:text-5xl lg:text-[52px] font-extrabold text-[#0a1f1e] leading-[1.15] mb-8 tracking-tight">
          {{ article.title }}
        </h1>
        
        <div class="flex items-center gap-6 py-4 border-t border-b border-gray-200/60">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-[#075955]/10 flex items-center justify-center text-[#075955]">
              <span class="material-symbols-outlined text-xl">person</span>
            </div>
            <div>
              <p class="text-xs font-extrabold uppercase tracking-widest text-[#0a1f1e]">Ban Quản Trị</p>
              <p class="text-[11px] font-bold text-gray-400 uppercase tracking-widest mt-0.5">Tác giả</p>
            </div>
          </div>
          
          <div class="w-px h-8 bg-gray-200"></div>
          
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 border border-gray-100">
              <span class="material-symbols-outlined text-xl">calendar_month</span>
            </div>
            <div>
              <p class="text-xs font-extrabold uppercase tracking-widest text-[#0a1f1e]">{{ new Date(article.createdAt).toLocaleDateString('vi-VN') }}</p>
              <p class="text-[11px] font-bold text-gray-400 uppercase tracking-widest mt-0.5">Ngày đăng</p>
            </div>
          </div>
        </div>
      </header>

      <!-- Hero Image -->
      <figure v-if="article.imageUrl" class="mb-12 rounded-none overflow-hidden shadow-xl border border-gray-200/50 bg-gray-100 w-full lg:w-[70%] mx-auto">
        <img :src="article.imageUrl" :alt="article.title" class="w-full aspect-video object-cover" />
      </figure>

      <!-- Summary -->
      <div v-if="article.summary" class="mb-12 max-w-4xl mx-auto text-center">
        <p class="text-lg md:text-xl font-medium text-gray-600 leading-relaxed italic">
          {{ article.summary }}
        </p>
      </div>

      <!-- Main Content -->
      <div class="ql-snow">
        <div 
          @click="handleContentClick"
          class="ql-editor custom-content-render text-gray-800"
          v-html="formattedContent"
        ></div>
      </div>
      
      <!-- Image Lightbox Modal -->
      <Teleport to="body">
        <div v-if="zoomedImage" class="fixed inset-0 z-[999999] flex items-center justify-center bg-black/95 p-4 md:p-8 backdrop-blur-sm transition-all" @click="zoomedImage = null">
          <button class="absolute top-6 right-6 text-white/50 hover:text-white transition-colors w-12 h-12 flex items-center justify-center bg-black/50 rounded-full hover:bg-white/20">
            <span class="material-symbols-outlined text-3xl">close</span>
          </button>
          <img :src="zoomedImage" class="max-w-full max-h-full object-contain rounded-xl shadow-2xl animate-in zoom-in-95 duration-200" @click.stop />
        </div>
      </Teleport>
      
      <!-- Footer Actions -->
      <div class="mt-12 pt-8 border-t border-gray-200 flex justify-between items-center">
        <router-link to="/tin-tuc" class="text-[#075955] font-bold uppercase tracking-widest text-sm flex items-center gap-2 hover:-translate-x-1 transition-transform">
          <span class="material-symbols-outlined text-[18px]">arrow_back</span>
          Xem bài khác
        </router-link>
        
        <div class="flex items-center gap-3">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-widest">Chia sẻ:</span>
          <button class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-gray-600 hover:bg-[#1877F2] hover:text-white transition-colors">
            <span class="material-symbols-outlined text-sm">share</span>
          </button>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

const route = useRoute();
const router = useRouter();
const api = useApi();

const article = ref(null);
const loading = ref(true);
const error = ref(false);
const zoomedImage = ref(null);

const handleContentClick = (event) => {
  if (event.target.tagName === 'IMG') {
    zoomedImage.value = event.target.src;
  }
};

const formattedContent = computed(() => {
  if (!article.value?.content) return '';
  // Check if it already contains basic HTML tags. If yes, leave it alone.
  if (article.value.content.includes('<p>') || article.value.content.includes('<br>')) {
    return article.value.content;
  }
  // Otherwise, convert newlines to <br> to preserve formatting from plain text
  return article.value.content.replace(/\n/g, '<br />');
});

const fetchArticle = async () => {
  const slug = route.params.slug;
  if (!slug) return;
  
  try {
    const res = await api.get(`/articles/slug/${slug}`);
    article.value = res.data;
    // Cập nhật title trình duyệt
    document.title = `${article.value.title} - SmartBus Tin Tức`;
  } catch (err) {
    console.error("Lỗi tải bài viết:", err);
    error.value = true;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchArticle();
  window.scrollTo(0, 0);
});
</script>

<style>
/* ĐỒNG BỘ 100% VỚI ADMIN EDITOR */
.custom-content-render.ql-editor {
  padding: 0 !important; /* Xóa padding mặc định của quill để khớp với layout public */
  font-family: inherit;
  font-size: 15px; /* Kích thước chuẩn giống hệ thống Admin */
  line-height: 1.7;
}

.custom-content-render.ql-editor p {
  margin-bottom: 0.75em;
}

.custom-content-render.ql-editor img {
  border-radius: 0;
  margin: 1.5em auto;
  cursor: zoom-in;
  transition: opacity 0.2s;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

.custom-content-render.ql-editor img:hover {
  opacity: 0.95;
}

/* Ghi đè lại font size cho Huge và Large để đảm bảo luôn khổng lồ */
.custom-content-render.ql-editor .ql-size-huge {
  font-size: 2.5em !important;
  line-height: 1.2 !important;
  font-weight: 900 !important;
}

.custom-content-render.ql-editor .ql-size-large {
  font-size: 1.5em !important;
  line-height: 1.4 !important;
}

/* Đảm bảo các thẻ heading in đậm */
.custom-content-render.ql-editor h1,
.custom-content-render.ql-editor h2,
.custom-content-render.ql-editor h3 {
  font-weight: 800;
  color: #0a1f1e;
  margin-top: 1.2em;
  margin-bottom: 0.5em;
}
</style>
