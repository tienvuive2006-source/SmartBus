<template>
  <div v-if="totalPages > 1" class="flex flex-col gap-3 border-t border-slate-100 bg-slate-50/50 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
    <p class="text-xs font-semibold text-slate-500">
      Hiển thị {{ startItem }}–{{ endItem }} trong {{ totalElements.toLocaleString('vi-VN') }} kết quả
    </p>
    <div class="flex items-center gap-2">
      <button type="button" :disabled="page <= 0" class="pagination-button" @click="$emit('update:page', page - 1)">
        <span class="material-symbols-outlined text-base">chevron_left</span>
      </button>
      <span class="px-3 text-xs font-black text-slate-700">Trang {{ page + 1 }} / {{ totalPages }}</span>
      <button type="button" :disabled="page >= totalPages - 1" class="pagination-button" @click="$emit('update:page', page + 1)">
        <span class="material-symbols-outlined text-base">chevron_right</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, default: 0 },
  totalPages: { type: Number, default: 0 },
  totalElements: { type: Number, default: 0 },
  pageSize: { type: Number, default: 20 },
  currentCount: { type: Number, default: 0 }
})

defineEmits(['update:page'])

const startItem = computed(() => props.totalElements ? props.page * props.pageSize + 1 : 0)
const endItem = computed(() => Math.min(props.totalElements, props.page * props.pageSize + props.currentCount))
</script>

<style scoped>
.pagination-button {
  display: inline-flex;
  width: 2rem;
  height: 2rem;
  align-items: center;
  justify-content: center;
  border: 1px solid rgb(226 232 240);
  border-radius: 0.5rem;
  background: white;
  color: rgb(71 85 105);
  transition: background-color 0.2s ease;
}
.pagination-button:hover:not(:disabled) { background: rgb(241 245 249); }
.pagination-button:disabled { cursor: not-allowed; opacity: 0.4; }
</style>
