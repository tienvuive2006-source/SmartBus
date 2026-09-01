<template>
  <section class="resource-column">
    <header class="resource-header">
      <div class="flex items-center justify-between gap-2">
        <h4><span class="step">{{ step }}</span>{{ title }} <small v-if="optional">(Tùy chọn)</small></h4>
        <span class="count">{{ drivers.length }} tài xế</span>
      </div>
      <div class="search-box">
        <span class="material-symbols-outlined">search</span>
        <input :value="search" :placeholder="placeholder" @input="$emit('update:search', $event.target.value)" />
      </div>
      <label v-if="allowOutside" class="route-toggle">
        <input :checked="showOutside" type="checkbox" @change="$emit('update:showOutside', $event.target.checked)" />
        Hiện tài xế ngoài tuyến
      </label>
    </header>

    <div class="resource-list">
      <button v-if="optional" type="button" class="empty-choice" :class="{ selected: !selected }" @click="$emit('clear')">
        <span class="material-symbols-outlined">person_off</span>
        <span><strong>Không bố trí tài xế phụ</strong><small>Có thể bổ sung khi chuyến dài</small></span>
      </button>
      <p v-if="!drivers.length" class="empty-message">Không có tài xế phù hợp.</p>
      <button
        v-for="driver in drivers"
        :key="driver.id"
        type="button"
        class="resource-card"
        :class="{ selected: selected?.id === driver.id, disabled: driver.conflict }"
        :disabled="driver.conflict"
        @click="$emit('select', driver)"
      >
        <span class="avatar">
          <img v-if="driver.avatarUrl" :src="driver.avatarUrl" :alt="driver.fullName" />
          <span v-else>{{ driver.fullName?.charAt(0) }}</span>
        </span>
        <span class="min-w-0 flex-1 text-left">
          <strong class="truncate">{{ driver.fullName }}</strong>
          <small class="truncate">{{ driver.phone }}</small>
          <small v-if="driver.lastKnownLocation" class="mt-1 truncate text-slate-500">
            Dự kiến tại {{ driver.lastKnownLocation }}<template v-if="driver.lastKnownAt"> · {{ driver.lastKnownAt }}</template>
          </small>
          <small v-else-if="driver.routeRole" class="mt-1 font-bold" :class="driver.routeRole === 'PRIMARY' ? 'text-emerald-600' : 'text-amber-600'">
            {{ driver.routeRole === 'PRIMARY' ? 'Tuyến chính' : 'Dự phòng' }}
          </small>
        </span>
        <span class="status" :class="driver.conflict ? 'danger' : 'ready'">{{ driver.conflict ? driver.conflictReason : 'Rảnh' }}</span>
        <span v-if="selected?.id === driver.id" class="material-symbols-outlined check">check_circle</span>
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  step: Number, title: String, optional: Boolean,
  drivers: { type: Array, default: () => [] },
  selected: { type: Object, default: null },
  search: { type: String, default: '' },
  placeholder: { type: String, default: 'Tìm tài xế...' },
  allowOutside: Boolean, showOutside: Boolean
})
defineEmits(['select', 'clear', 'update:search', 'update:showOutside'])
</script>

<style scoped>
.resource-column { display:flex; min-width:0; flex-direction:column; border-right:1px solid #e2e8f0; background:#f8fafc; }
.resource-header { flex:none; border-bottom:1px solid #e2e8f0; background:white; padding:.85rem; }
h4 { display:flex; align-items:center; gap:.45rem; font-size:.72rem; font-weight:900; color:#0f172a; }
h4 small { font-size:.6rem; color:#94a3b8; }
.step { display:inline-flex; width:1.35rem; height:1.35rem; align-items:center; justify-content:center; border-radius:999px; background:#e6f4f1; color:#047267; }
.count { font-size:.58rem; font-weight:800; color:#64748b; }
.search-box { position:relative; margin-top:.65rem; }
.search-box span { position:absolute; left:.65rem; top:50%; transform:translateY(-50%); font-size:1rem; color:#94a3b8; }
.search-box input { width:100%; border:1px solid #dbe3ec; border-radius:.55rem; background:#f8fafc; padding:.5rem .65rem .5rem 2rem; font-size:.68rem; outline:none; }
.search-box input:focus { border-color:#0f8a7c; box-shadow:0 0 0 2px #d8f3ed; }
.route-toggle { margin-top:.55rem; display:flex; cursor:pointer; align-items:center; gap:.35rem; font-size:.59rem; font-weight:700; color:#64748b; }
.resource-list { min-height:0; flex:1; overflow-y:auto; padding:.65rem; }
.resource-card,.empty-choice { position:relative; margin-bottom:.5rem; display:flex; width:100%; align-items:center; gap:.55rem; border:1px solid #dfe6ee; border-radius:.7rem; background:white; padding:.65rem; transition:.18s ease; }
.resource-card:not(.disabled):hover,.empty-choice:hover { border-color:#76bdb4; transform:translateY(-1px); }
.resource-card.selected,.empty-choice.selected { border-color:#078778; background:#f0faf7; box-shadow:0 0 0 1px #078778; }
.resource-card.disabled { cursor:not-allowed; opacity:.55; }
.avatar { display:flex; width:2.25rem; height:2.25rem; flex:none; align-items:center; justify-content:center; overflow:hidden; border-radius:.65rem; background:#e2e8f0; font-size:.75rem; font-weight:800; color:#475569; }
.avatar img { width:100%; height:100%; object-fit:cover; }
.resource-card strong,.empty-choice strong { display:block; font-size:.68rem; font-weight:850; color:#1e293b; }
.resource-card small,.empty-choice small { display:block; font-size:.56rem; font-weight:600; color:#94a3b8; }
.status { max-width:5.5rem; flex:none; overflow:hidden; text-overflow:ellipsis; border-radius:.35rem; padding:.2rem .35rem; font-size:.52rem; font-weight:900; text-transform:uppercase; white-space:nowrap; }
.ready { background:#e9f9f0; color:#168451; }.danger { background:#fff0f1; color:#d3374b; }
.check { position:absolute; right:.3rem; bottom:.25rem; font-size:.9rem; color:#078778; }
.empty-choice { text-align:left; color:#64748b; }.empty-choice>.material-symbols-outlined { font-size:1.2rem; }
.empty-message { padding:2rem .5rem; text-align:center; font-size:.65rem; color:#94a3b8; }
</style>
