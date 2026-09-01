<template>
  <article class="summary-item" :class="{missing:required&&!value}">
    <span class="material-symbols-outlined icon">{{ icon }}</span>
    <span class="min-w-0 flex-1"><small>{{ label }}</small><strong class="truncate">{{ value || empty || 'Chưa chọn' }}</strong><em v-if="subvalue" class="truncate">{{ subvalue }}</em></span>
    <span v-if="value" class="material-symbols-outlined done">check_circle</span>
  </article>
</template>
<script setup>
defineProps({icon:String,label:String,value:String,subvalue:String,empty:String,required:Boolean})
</script>
<style scoped>
.summary-item{display:flex;align-items:center;gap:.5rem;border-bottom:1px solid #eef2f6;padding:.55rem .2rem}.summary-item.missing{border-radius:.5rem;background:#fff8ed;padding:.55rem}.icon{font-size:1.05rem;color:#64748b}.summary-item small{display:block;font-size:.52rem;font-weight:700;color:#94a3b8}.summary-item strong{display:block;margin-top:.1rem;font-size:.63rem;font-weight:850;color:#1e293b}.summary-item em{display:block;margin-top:.08rem;font-size:.52rem;font-style:normal;font-weight:600;color:#94a3b8}.done{font-size:.9rem;color:#18a16f}
.summary-item{gap:.65rem;padding:.7rem .15rem}.summary-item.missing{padding:.7rem}.icon{font-size:1.2rem}.summary-item small{font-size:.62rem}.summary-item strong{margin-top:.14rem;font-size:.76rem}.summary-item em{margin-top:.12rem;font-size:.62rem}.done{font-size:1rem}
</style>
