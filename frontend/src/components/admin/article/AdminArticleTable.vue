<template>
  <div class="bg-white rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100 overflow-hidden">
    <div v-if="articles.length === 0" class="p-12 text-center text-gray-400 font-medium">
      Chưa có bài viết nào. Hãy thêm bài viết đầu tiên!
    </div>
    
    <div v-else class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-gray-50 border-b border-gray-100">
            <th class="py-4 px-6 text-xs font-black text-gray-400 uppercase tracking-widest">Tiêu đề / Ảnh</th>
            <th class="py-4 px-6 text-xs font-black text-gray-400 uppercase tracking-widest">Đường dẫn (Slug)</th>
            <th class="py-4 px-6 text-xs font-black text-gray-400 uppercase tracking-widest text-center">Trạng thái</th>
            <th class="py-4 px-6 text-xs font-black text-gray-400 uppercase tracking-widest text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="article in articles" :key="article.id" class="hover:bg-gray-50/50 transition-colors">
            <td class="py-4 px-6">
              <div class="flex items-center gap-4">
                <div class="w-16 h-12 bg-gray-100 rounded-lg overflow-hidden shrink-0 border border-gray-200">
                  <img v-if="article.imageUrl" :src="article.imageUrl" class="w-full h-full object-cover" />
                  <div v-else class="w-full h-full flex items-center justify-center">
                    <span class="material-symbols-outlined text-gray-300">image</span>
                  </div>
                </div>
                <div>
                  <p class="font-bold text-gray-900 line-clamp-1" :title="article.title">{{ article.title }}</p>
                  <p class="text-xs text-gray-500 mt-0.5">{{ new Date(article.createdAt).toLocaleDateString('vi-VN') }}</p>
                </div>
              </div>
            </td>
            <td class="py-4 px-6">
              <p class="text-xs font-mono text-emerald-600 bg-emerald-50 px-2 py-1 rounded inline-block truncate max-w-[200px]" :title="article.slug">
                /tin-tuc/{{ article.slug }}
              </p>
            </td>
            <td class="py-4 px-6 text-center">
              <span 
                class="px-3 py-1.5 text-[10px] font-black rounded-lg uppercase tracking-widest shadow-sm whitespace-nowrap inline-block"
                :class="article.isActive ? 'bg-emerald-500 text-white' : 'bg-gray-200 text-gray-500'"
              >
                {{ article.isActive ? 'Hiển thị' : 'Đã ẩn' }}
              </span>
            </td>
            <td class="py-4 px-6">
              <div class="flex items-center justify-center gap-2">
                <button @click="$emit('toggle-status', article)" class="w-8 h-8 flex items-center justify-center rounded-xl transition-all border" :class="article.isActive ? 'bg-amber-50 text-amber-500 hover:bg-amber-500 hover:text-white border-amber-100' : 'bg-emerald-50 text-emerald-500 hover:bg-emerald-500 hover:text-white border-emerald-100'" :title="article.isActive ? 'Ẩn bài viết' : 'Hiện bài viết'">
                  <span class="material-symbols-outlined text-lg">{{ article.isActive ? 'visibility_off' : 'visibility' }}</span>
                </button>
                <button @click="$emit('edit', article)" class="w-8 h-8 flex items-center justify-center rounded-xl bg-blue-50 text-blue-500 hover:bg-blue-500 hover:text-white transition-all shadow-sm border border-blue-100" title="Chỉnh sửa">
                  <span class="material-symbols-outlined text-lg">edit</span>
                </button>
                <button @click="$emit('delete', article.id)" class="w-8 h-8 flex items-center justify-center rounded-xl bg-red-50 text-red-500 hover:bg-red-500 hover:text-white transition-all shadow-sm border border-red-100" title="Xóa">
                  <span class="material-symbols-outlined text-lg">delete</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
defineProps({
  articles: {
    type: Array,
    required: true
  }
});
defineEmits(['edit', 'delete', 'toggle-status']);
</script>
