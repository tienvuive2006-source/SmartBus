<template>
  <Teleport to="body"><div v-if="bus" class="maintenance-modal-layer"><button class="maintenance-modal-backdrop" type="button" aria-label="Đóng" @click="$emit('close')"></button><form class="maintenance-modal maintenance-modal--small" @submit.prevent="$emit('submit', form)">
    <header><span class="material-symbols-outlined">speed</span><div><h3>Điều chỉnh kilomet</h3><p>{{ bus.licensePlate }}</p></div><button type="button" @click="$emit('close')"><span class="material-symbols-outlined">close</span></button></header>
    <div class="maintenance-modal__body"><label><span>Công-tơ-mét hiện tại *</span><div class="maintenance-input-suffix"><input v-model.number="form.currentMileage" min="0" step="0.1" required type="number" /><i>km</i></div></label><label><span>Lý do điều chỉnh</span><textarea v-model.trim="form.note" rows="3" maxlength="500" placeholder="Đối chiếu công-tơ-mét thực tế"></textarea></label></div>
    <footer><button type="button" class="maintenance-btn secondary" @click="$emit('close')">Hủy</button><button class="maintenance-btn primary" type="submit" :disabled="submitting">Lưu kilomet</button></footer>
  </form></div></Teleport>
</template>
<script setup>
import { reactive, watch } from 'vue'
const props = defineProps({ bus: Object, submitting: Boolean })
defineEmits(['close', 'submit'])
const form = reactive({ currentMileage: 0, note: '' })
watch(() => props.bus, bus => { if (bus) Object.assign(form, { currentMileage: Number(bus.currentMileage || 0), note: '' }) }, { immediate: true })
</script>
