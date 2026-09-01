<template>
  <section class="insights" aria-label="Phân tích và gợi ý phân công">
    <article class="insight suggestion">
      <span class="insight-icon"><span class="material-symbols-outlined">lightbulb</span></span>
      <span class="insight-copy">
        <strong>Gợi ý từ hệ thống</strong>
        <p v-if="driver && bus"><b>{{ driver.fullName }}</b> và xe <b>{{ bus.licensePlate }}</b> phù hợp với chuyến này.</p>
        <p v-else>Chọn “Gợi ý tự động” để hệ thống ghép tài xế và phương tiện đang rảnh.</p>
      </span>
      <button type="button" @click="$emit('suggest')">Gợi ý tự động <span class="material-symbols-outlined">auto_awesome</span></button>
    </article>

    <article class="insight" :class="conflictFree ? 'safe' : 'warning'">
      <span class="insight-icon"><span class="material-symbols-outlined">{{ conflictFree ? 'verified_user' : 'warning' }}</span></span>
      <span class="insight-copy">
        <strong>{{ conflictFree ? 'Không phát hiện xung đột' : 'Cần kiểm tra lại' }}</strong>
        <p>{{ conflictFree ? 'Lịch tài xế và phương tiện đang đáp ứng điều kiện phân công.' : 'Một nguồn lực được chọn đang trùng lịch hoặc chưa đủ điều kiện.' }}</p>
      </span>
    </article>
  </section>
</template>

<script setup>
defineProps({
  driver: { type: Object, default: null },
  bus: { type: Object, default: null },
  conflictFree: { type: Boolean, default: true }
})
defineEmits(['suggest'])
</script>

<style scoped>
.insights{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(0,.85fr);gap:.75rem;flex:none;padding:.65rem 1rem;background:#f6f9f8}.insight{display:flex;min-width:0;align-items:center;gap:.65rem;border:1px solid #dce8e5;border-radius:.75rem;padding:.65rem .75rem;background:#fff}.insight-icon{display:grid;width:2.1rem;height:2.1rem;flex:none;place-items:center;border-radius:50%}.insight-icon span{font-size:1.1rem}.insight-copy{min-width:0;flex:1}.insight-copy strong{display:block;color:#23363b;font-size:.68rem;font-weight:900}.insight-copy p{overflow:hidden;margin-top:.15rem;color:#708185;font-size:.6rem;font-weight:600;text-overflow:ellipsis;white-space:nowrap}.insight-copy b{color:#17695e}.suggestion{border-color:#cfe9df;background:#f1faf6}.suggestion .insight-icon{color:#fff;background:#078166}.suggestion button{display:flex;flex:none;align-items:center;gap:.3rem;color:#08735f;font-size:.6rem;font-weight:850}.suggestion button span{font-size:.9rem}.suggestion button:hover{text-decoration:underline;text-underline-offset:3px}.safe .insight-icon{color:#0a835c;background:#e5f7ef}.warning{border-color:#f2d6a8;background:#fff9ee}.warning .insight-icon{color:#bd6c00;background:#ffedca}@media(max-width:920px){.insights{grid-template-columns:1fr}.insight:nth-child(2){display:none}}
@media(max-height:850px){.insights{display:none}}
</style>
