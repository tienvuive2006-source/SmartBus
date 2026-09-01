<template>
  <div class="maintenance-panel">
    <div v-if="error" class="maintenance-page-error"><span class="material-symbols-outlined">error</span>{{ error }}</div>
    <section class="maintenance-summary">
      <article><span class="material-symbols-outlined">engineering</span><div><small>Đang bảo trì</small><strong>{{ inProgressCount }}</strong></div></article>
      <article class="warning"><span class="material-symbols-outlined">notification_important</span><div><small>Sắp đến hạn</small><strong>{{ warningBuses.length }}</strong></div></article>
      <article class="danger"><span class="material-symbols-outlined">report</span><div><small>Đã đến hạn</small><strong>{{ overdueBuses.length }}</strong></div></article>
      <article><span class="material-symbols-outlined">payments</span><div><small>Chi phí đã ghi nhận</small><strong>{{ formatMoney(totalCost) }}</strong></div></article>
    </section>

    <section v-if="dueBuses.length" class="maintenance-due-strip">
      <header><div><span class="material-symbols-outlined">campaign</span><strong>Xe cần chú ý</strong></div><small>Cảnh báo theo kilomet từ lần bảo trì gần nhất</small></header>
      <div class="maintenance-due-list"><article v-for="bus in dueBuses" :key="bus.id" :class="{ overdue: bus.maintenanceAlertLevel >= 100 }">
        <div><strong>{{ bus.licensePlate }}</strong><small>{{ mileageLabel(bus) }}</small></div>
        <div class="maintenance-progress"><i :style="{ width: `${progress(bus)}%` }"></i></div>
        <button type="button" @click="mileageBus = bus"><span class="material-symbols-outlined">speed</span>Đối chiếu km</button>
      </article></div>
    </section>

    <div class="maintenance-toolbar"><div><h3>Nhật ký bảo trì đội xe</h3><p>Tự động cộng kilomet khi chuyến hoàn thành</p></div><button class="maintenance-new-button" type="button" @click="formOpen = true"><span class="material-symbols-outlined">add</span>Lập phiếu bảo trì</button></div>
    <MaintenanceTable :records="records" :loading="loading" @start="startMaintenance" @complete="completeRecord = $event" @cancel="cancelMaintenance" />
    <MaintenanceFormModal :open="formOpen" :buses="buses" :records="records" :submitting="submitting" @close="formOpen = false" @submit="createMaintenance" />
    <MaintenanceCompleteModal :record="completeRecord" :submitting="submitting" @close="completeRecord = null" @submit="completeMaintenance" />
    <MileageAdjustModal :bus="mileageBus" :submitting="submitting" @close="mileageBus = null" @submit="adjustMileage" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useApi } from '@/composables/useApi'
import MaintenanceTable from './MaintenanceTable.vue'
import MaintenanceFormModal from './MaintenanceFormModal.vue'
import MaintenanceCompleteModal from './MaintenanceCompleteModal.vue'
import MileageAdjustModal from './MileageAdjustModal.vue'
import './maintenance.css'

const props = defineProps({ buses: { type: Array, default: () => [] } })
const emit = defineEmits(['refresh-buses'])
const api = useApi()
const records = ref([])
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const formOpen = ref(false)
const completeRecord = ref(null)
const mileageBus = ref(null)
const dueBuses = computed(() => props.buses.filter(bus => Number(bus.maintenanceAlertLevel || 0) >= 80).sort((a,b) => b.maintenanceAlertLevel - a.maintenanceAlertLevel))
const warningBuses = computed(() => dueBuses.value.filter(bus => Number(bus.maintenanceAlertLevel) === 80))
const overdueBuses = computed(() => dueBuses.value.filter(bus => Number(bus.maintenanceAlertLevel) >= 100))
const inProgressCount = computed(() => records.value.filter(item => item.status === 'IN_PROGRESS').length)
const totalCost = computed(() => records.value.filter(item => item.status === 'COMPLETED').reduce((sum,item) => sum + Number(item.actualCost || 0), 0))
const progress = bus => Math.min(100, Math.round(Math.max(0, Number(bus.currentMileage || 0) - Number(bus.lastMaintenanceMileage || 0)) / Math.max(1, Number(bus.maintenanceIntervalKm || 10000)) * 100))
const mileageLabel = bus => `${formatNumber(Math.max(0, Number(bus.currentMileage || 0) - Number(bus.lastMaintenanceMileage || 0)))} / ${formatNumber(bus.maintenanceIntervalKm || 10000)} km`
const formatNumber = value => Number(value || 0).toLocaleString('vi-VN', { maximumFractionDigits: 1 })
const formatMoney = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const messageOf = err => err.response?.data?.message || err.response?.data || 'Không thể xử lý yêu cầu bảo trì.'
const fetchRecords = async () => { loading.value = true; try { records.value = (await api.get('/vehicle-maintenance')).data } catch (err) { error.value = messageOf(err) } finally { loading.value = false } }
const runAction = async action => { submitting.value = true; error.value = ''; try { await action(); await fetchRecords(); emit('refresh-buses') } catch (err) { error.value = typeof messageOf(err) === 'string' ? messageOf(err) : 'Không thể xử lý yêu cầu.' } finally { submitting.value = false } }
const createMaintenance = payload => runAction(async () => { await api.post('/vehicle-maintenance', payload); formOpen.value = false })
const startMaintenance = item => runAction(() => api.patch(`/vehicle-maintenance/${item.id}/start`))
const cancelMaintenance = item => runAction(() => api.patch(`/vehicle-maintenance/${item.id}/cancel`))
const completeMaintenance = payload => runAction(async () => { await api.patch(`/vehicle-maintenance/${completeRecord.value.id}/complete`, payload); completeRecord.value = null })
const adjustMileage = payload => runAction(async () => { await api.patch(`/vehicle-maintenance/buses/${mileageBus.value.id}/mileage`, payload); mileageBus.value = null })
onMounted(fetchRecords)
</script>
