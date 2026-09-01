<template>
  <label class="block">
    <span class="mb-2 ml-0.5 block text-[10px] font-black uppercase tracking-[0.12em] text-slate-500">{{ label }} <b v-if="required" class="text-rose-500">*</b></span>
    <slot></slot>
  </label>
</template>
<script setup>
defineProps({ label: { type: String, required: true }, required: Boolean })
</script>
