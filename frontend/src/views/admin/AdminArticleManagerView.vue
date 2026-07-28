<template>
  <div class="p-4 md:p-8">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
      <div>
        <h2 class="text-3xl font-black text-gray-900 tracking-tight">Quản lý Tin Tức</h2>
        <p class="text-sm font-semibold text-gray-500 mt-1 uppercase tracking-widest">Blog & Thông báo khuyến mãi</p>
      </div>
      <button 
        @click="openAddModal" 
        class="bg-[#075955] text-white px-6 py-3.5 rounded-xl text-sm font-black uppercase tracking-widest hover:bg-[#064844] hover:shadow-lg transition-all flex items-center gap-2 active:scale-95"
      >
        <span class="material-symbols-outlined text-[20px]">add_circle</span>
        Đăng bài mới
      </button>
    </div>

    <AdminArticleTable 
      :articles="articles"
      @edit="openEditModal"
      @delete="deleteArticle"
      @toggle-status="toggleStatus"
    />

    <AdminArticleModal 
      ref="articleModal"
      @saved="onArticleSaved"
      @show-toast="showToast"
    />

  </div>

  <!-- Toast Notification -->
  <transition name="fade">
    <div v-if="toastMsg" class="fixed bottom-6 right-6 z-[999999] bg-[#075955] text-white px-6 py-3 rounded-xl shadow-2xl flex items-center gap-3 font-bold animate-in slide-in-from-bottom-5">
      <span class="material-symbols-outlined">check_circle</span>
      {{ toastMsg }}
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';
import AdminArticleTable from '@/components/admin/article/AdminArticleTable.vue';
import AdminArticleModal from '@/components/admin/article/AdminArticleModal.vue';

const api = useApi();
const articles = ref([]);
const toastMsg = ref('');
const articleModal = ref(null);

const showToast = (msg) => {
  toastMsg.value = msg;
  setTimeout(() => { toastMsg.value = ''; }, 3000);
};

const fetchArticles = async () => {
  try {
    const res = await api.get('/articles/all');
    articles.value = res.data;
  } catch (error) {
    console.error("Lỗi khi tải danh sách bài viết:", error);
  }
};

const openAddModal = () => {
  if (articleModal.value) {
    articleModal.value.openModal(null);
  }
};

const openEditModal = (article) => {
  if (articleModal.value) {
    articleModal.value.openModal(article);
  }
};

const onArticleSaved = (msg) => {
  showToast(msg);
  fetchArticles();
};

const deleteArticle = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa bài viết này vĩnh viễn?')) {
    try {
      await api.delete(`/articles/${id}`);
      await fetchArticles();
      showToast('Đã xóa bài viết thành công!');
    } catch (error) {
      console.error("Lỗi khi xóa bài viết:", error);
    }
  }
};

const toggleStatus = async (article) => {
  try {
    const updated = { ...article, isActive: !article.isActive };
    await api.put(`/articles/${article.id}`, updated);
    await fetchArticles();
    showToast('Đã cập nhật trạng thái bài viết!');
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
  }
};

onMounted(() => {
  fetchArticles();
});
</script>
