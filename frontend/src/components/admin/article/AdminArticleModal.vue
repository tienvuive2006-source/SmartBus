<template>
  <div v-if="isOpen" :class="['fixed top-0 right-0 bottom-0 left-0 md:left-64 z-[99999] flex items-center justify-center', isEditorFullscreen ? 'p-0' : 'p-4']">
    <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="closeModal"></div>
    <div :class="['relative bg-white shadow-2xl flex flex-col overflow-hidden transition-all duration-300', isEditorFullscreen ? 'w-full h-full rounded-none' : 'w-full max-w-4xl max-h-[90vh] rounded-3xl animate-in fade-in zoom-in-95']">
      <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
        <h3 class="text-xl font-black text-gray-900">{{ isEditing ? 'Chỉnh sửa bài viết' : 'Đăng bài viết mới' }}</h3>
        <button @click="closeModal" class="text-gray-400 hover:text-gray-600 transition-colors w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-200">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <div class="p-6 overflow-y-auto custom-scrollbar flex-1">
        <div class="space-y-6">
          <div>
            <label class="block text-[11px] font-black text-gray-500 uppercase tracking-widest mb-2">Tiêu đề bài viết <span class="text-red-500">*</span></label>
            <input v-model="form.title" type="text" required class="w-full text-base font-bold text-gray-900 bg-gray-50 p-4 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#075955] focus:border-transparent outline-none transition-all placeholder:text-gray-400 placeholder:font-normal" placeholder="Ví dụ: Lịch chạy xe dịp Tết Nguyên Đán 2026..." />
            <p v-if="form.title" class="text-xs text-emerald-600 font-mono mt-2 flex items-center gap-1">
              <span class="material-symbols-outlined text-[14px]">link</span>
              URL dự kiến: domain.com/tin-tuc/{{ previewSlug }}
            </p>
          </div>
          
          <!-- Trợ lý AI Viết bài -->
          <div class="flex flex-col gap-2 p-4 bg-indigo-50/80 border border-indigo-100 rounded-2xl relative overflow-hidden group">
            <div class="absolute inset-0 bg-gradient-to-r from-indigo-500/5 to-purple-500/5 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            <div class="flex items-center gap-2 relative z-10">
              <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
                <span class="material-symbols-outlined text-[18px]">smart_toy</span>
              </div>
              <span class="text-xs font-black text-indigo-800 uppercase tracking-widest">Trợ lý AI Viết bài</span>
            </div>
            <div class="flex items-center gap-3 mt-1 relative z-10">
              <input v-model="aiPrompt" :disabled="isGeneratingAi" type="text" class="flex-1 bg-white text-sm font-medium text-gray-800 p-3 rounded-xl border border-indigo-200 focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-gray-400 shadow-sm" placeholder="Gõ yêu cầu (VD: Viết bài ưu đãi 20% cho sinh viên đi Đà Lạt dịp lễ 30/4)..." @keyup.enter="generateAiArticle" />
              <button @click="generateAiArticle" :disabled="isGeneratingAi || !aiPrompt.trim()" class="bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-300 disabled:cursor-not-allowed text-white px-5 py-3 rounded-xl text-xs font-black uppercase tracking-widest flex items-center gap-2 transition-all shadow-md hover:shadow-lg shrink-0">
                <span v-if="isGeneratingAi" class="material-symbols-outlined text-[18px] animate-spin">sync</span>
                <span v-else class="material-symbols-outlined text-[18px]">auto_awesome</span>
                {{ isGeneratingAi ? 'Đang viết...' : 'Bắt đầu viết' }}
              </button>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-[11px] font-black text-gray-500 uppercase tracking-widest mb-2">Tóm tắt ngắn (Dưới ảnh bìa)</label>
              <textarea v-model="form.summary" rows="3" class="w-full text-sm font-medium text-gray-900 bg-gray-50 p-4 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#075955] outline-none transition-all resize-none" placeholder="Đoạn mô tả ngắn gọn hiển thị ở ngoài trang chủ..."></textarea>
            </div>
            
            <div>
              <label class="block text-[11px] font-black text-gray-500 uppercase tracking-widest mb-2">Link Ảnh bìa (Cloudinary/URL)</label>
              <input v-model="form.imageUrl" type="text" class="w-full text-sm font-medium text-gray-900 bg-gray-50 p-4 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#075955] outline-none transition-all mb-3" placeholder="https://..." />
              
              <!-- Preview Ảnh -->
              <div class="w-full h-24 bg-gray-100 rounded-xl overflow-hidden border border-gray-200 flex items-center justify-center">
                <img v-if="form.imageUrl" :src="form.imageUrl" class="w-full h-full object-cover" />
                <span v-else class="text-xs text-gray-400 font-medium">Preview Ảnh Bìa</span>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-[11px] font-black text-gray-500 uppercase tracking-widest mb-2">Nội dung chi tiết <span class="text-red-500">*</span></label>
            <p class="text-xs text-gray-400 mb-2">Hỗ trợ các công cụ định dạng chuẩn (in đậm, nghiêng, căn lề, hình ảnh...).</p>
            <div :class="['quill-wrapper bg-white rounded-xl border border-gray-200 focus-within:ring-2 focus-within:ring-[#075955] transition-all', isEditorFullscreen ? 'absolute inset-0 z-[50] !h-full !rounded-none !border-none !m-0 !p-0 bg-white flex flex-col' : 'relative']" spellcheck="false">
              <!-- Nút Phóng to / Thu nhỏ -->
              <div class="absolute top-2.5 right-3 z-20">
                <button @click.prevent="isEditorFullscreen = !isEditorFullscreen" type="button" class="w-8 h-8 flex items-center justify-center rounded bg-white hover:bg-gray-100 text-gray-600 transition-colors shadow-sm border border-gray-200" :title="isEditorFullscreen ? 'Thu nhỏ' : 'Phóng to toàn màn hình'">
                  <span class="material-symbols-outlined text-[20px]">{{ isEditorFullscreen ? 'fullscreen_exit' : 'fullscreen' }}</span>
                </button>
              </div>
              
              <QuillEditor 
                ref="quillEditorRef"
                contentType="html" 
                theme="snow" 
                toolbar="full" 
                :modules="modules"
                @ready="onEditorReady"
                placeholder="Gõ nội dung bài viết vào đây..." 
              />
            </div>
          </div>
          
          <div class="flex items-center gap-3 bg-gray-50 p-4 rounded-xl border border-gray-200">
            <input type="checkbox" id="isActive" v-model="form.isActive" class="w-5 h-5 text-[#075955] rounded focus:ring-[#075955]" />
            <label for="isActive" class="text-sm font-bold text-gray-700 cursor-pointer">Hiển thị bài viết ngay lập tức</label>
          </div>
        </div>
      </div>
      
      <div class="p-6 border-t border-gray-100 bg-gray-50 flex justify-end gap-3 shrink-0">
        <button @click="closeModal" class="px-6 py-3 rounded-xl font-bold text-gray-600 hover:bg-gray-200 transition-colors text-sm uppercase tracking-widest">
          Hủy
        </button>
        <button @click="saveArticle" class="px-8 py-3 rounded-xl font-black text-white bg-[#075955] hover:bg-[#064844] transition-colors shadow-lg hover:shadow-xl text-sm uppercase tracking-widest flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px]">save</span>
          Lưu bài viết
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useApi } from '@/composables/useApi';
import { QuillEditor } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import ImageUploader from 'quill-image-uploader';
import axios from 'axios';

const api = useApi();
const emit = defineEmits(['saved', 'show-toast']);

const isOpen = ref(false);
const isEditing = ref(false);
const editingId = ref(null);
const isEditorFullscreen = ref(false);
const quillEditorRef = ref(null);

const aiPrompt = ref('');
const isGeneratingAi = ref(false);

const form = ref({
  title: '',
  summary: '',
  content: '',
  imageUrl: '',
  isActive: true
});

const modules = [
  {
    name: 'imageUploader',
    module: ImageUploader,
    options: {
      upload: (file) => {
        return new Promise((resolve, reject) => {
          emit('show-toast', 'Đang tải ảnh lên máy chủ...');
          const formData = new FormData();
          formData.append('file', file);
          formData.append('upload_preset', 'skybus_preset'); 
          
          axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData)
            .then(res => {
              emit('show-toast', 'Tải ảnh thành công!');
              if (quillEditorRef.value) {
                const quill = quillEditorRef.value.getQuill();
                const range = quill.getSelection(true) || { index: quill.getLength() };
                quill.insertEmbed(range.index, 'image', res.data.secure_url, 'user');
                quill.setSelection(range.index + 1, 'silent');
              }
              resolve(res.data.secure_url);
            })
            .catch(err => {
              console.error('Lỗi upload ảnh bài viết:', err);
              emit('show-toast', 'Lỗi tải ảnh. Vui lòng thử lại.');
              reject('Upload failed');
            });
        });
      }
    }
  }
];

const onEditorReady = (quill) => {
  quill.clipboard.addMatcher('IMG', (node, delta) => {
    const src = node.getAttribute('src');
    if (src && src.startsWith('data:image/')) {
      delta.ops = [];
    }
    return delta;
  });
};

const previewSlug = computed(() => {
  if (!form.value.title) return '';
  let str = form.value.title.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  str = str.replace(/đ/g, 'd').replace(/Đ/g, 'D');
  str = str.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '');
  return str + '-xxxx';
});

const openModal = (article = null) => {
  isEditing.value = !!article;
  editingId.value = article ? article.id : null;
  aiPrompt.value = '';
  
  if (article) {
    form.value = {
      title: article.title,
      summary: article.summary,
      content: article.content,
      imageUrl: article.imageUrl,
      isActive: article.isActive
    };
  } else {
    form.value = {
      title: '',
      summary: '',
      content: '',
      imageUrl: '',
      isActive: true
    };
  }
  
  isOpen.value = true;
  
  setTimeout(() => {
    if (quillEditorRef.value) {
      quillEditorRef.value.setHTML(article ? article.content || '' : '');
    }
  }, 100);
};

const closeModal = () => {
  isOpen.value = false;
};

const saveArticle = async () => {
  if (quillEditorRef.value) {
    let editorContent = quillEditorRef.value.getHTML();
    if (editorContent === '<p><br></p>') {
      editorContent = '';
    }
    form.value.content = editorContent;
  }

  if (form.value.content && form.value.content.includes('data:image/')) {
    alert('Lỗi: Bài viết của bạn đang chứa hình ảnh chưa được tải lên máy chủ (ảnh copy/dán bị lỗi).\nVui lòng xóa ảnh đó đi, copy và dán lại, hoặc dùng nút "Thêm ảnh" trên thanh công cụ!');
    return;
  }

  if (!form.value.title || !form.value.content) {
    alert('Vui lòng nhập đủ Tiêu đề và Nội dung!');
    return;
  }

  try {
    if (isEditing.value) {
      await api.put(`/articles/${editingId.value}`, form.value);
    } else {
      await api.post('/articles', form.value);
    }
    emit('saved', isEditing.value ? 'Cập nhật bài viết thành công!' : 'Thêm bài viết mới thành công!');
    closeModal();
  } catch (error) {
    console.error("Lỗi khi lưu bài viết:", error);
    alert('Có lỗi xảy ra khi lưu bài viết!');
  }
};

const generateAiArticle = async () => {
  if (!aiPrompt.value.trim() || isGeneratingAi.value) return;
  
  isGeneratingAi.value = true;
  emit('show-toast', 'AI đang phân tích và viết bài...');
  
  try {
    const res = await api.post('/ai/generate-article', { topic: aiPrompt.value }, { timeout: 60000 });
    if (res.data) {
      if (res.data.title) form.value.title = res.data.title;
      if (res.data.summary) form.value.summary = res.data.summary;
      if (res.data.content) form.value.content = res.data.content;
      
      if (quillEditorRef.value && res.data.content) {
        quillEditorRef.value.setHTML(res.data.content);
      }
      
      emit('show-toast', 'AI đã viết xong bài!');
    }
  } catch (error) {
    console.error("Lỗi AI generate:", error);
    alert(error.response?.data?.error || 'Có lỗi khi kết nối tới AI.');
  } finally {
    isGeneratingAi.value = false;
  }
};

defineExpose({
  openModal,
  closeModal
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 10px;
}

/* Custom CSS for Quill Editor */
.quill-wrapper {
  display: flex;
  flex-direction: column;
  height: 500px;
  overflow: hidden;
}
:deep(.ql-toolbar.ql-snow) {
  border: none;
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
  padding: 12px;
  padding-right: 50px;
  font-family: inherit;
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
:deep(.ql-formats) {
  margin-right: 15px !important;
  margin-bottom: 0 !important;
  display: inline-flex;
  align-items: center;
}
:deep(.ql-container.ql-snow) {
  border: none;
  font-family: inherit;
  flex: 1;
  overflow-y: auto;
}
:deep(.ql-editor) {
  min-height: 100%;
  padding: 16px 24px;
  font-family: inherit;
  font-size: 15px;
}
</style>
