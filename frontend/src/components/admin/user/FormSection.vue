<template>
  <section class="rounded-2xl border border-slate-200 bg-white p-4 sm:p-5">
    <header class="mb-4 flex gap-3 border-b border-slate-100 pb-4">
      <span class="material-symbols-outlined grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-teal-50 text-[20px] text-teal-700">{{ icon }}</span>
      <div><h4 class="text-sm font-black text-slate-800">{{ title }}</h4><p class="mt-0.5 text-[11px] leading-4 text-slate-400">{{ description }}</p></div>
    </header>
    <slot></slot>
  </section>
</template>
<script setup>
defineProps({ icon: { type: String, required: true }, title: { type: String, required: true }, description: { type: String, default: '' } })
</script>
