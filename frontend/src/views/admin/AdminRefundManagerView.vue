<template>
  <main class="refund-page" :class="{ embedded }">
    <header v-if="!embedded" class="page-heading">
      <div>
        <p>Đối soát sau bán</p>
        <h1>Yêu cầu hoàn tiền</h1>
        <span>Duyệt và ghi nhận các khoản hoàn chuyển khoản thủ công.</span>
      </div>
      <button type="button" :disabled="loading" @click="loadRefunds">
        <span class="material-symbols-outlined" :class="{ 'animate-spin': loading }">refresh</span>Làm mới
      </button>
    </header>

    <section class="stats-strip" aria-label="Thống kê hoàn tiền">
      <div><span>Chờ xử lý</span><strong>{{ statusCount('PENDING') }}</strong></div>
      <div><span>Đã duyệt</span><strong>{{ statusCount('APPROVED') }}</strong></div>
      <div><span>Đã hoàn</span><strong>{{ statusCount('COMPLETED') }}</strong></div>
      <div><span>Tổng tiền chưa hoàn</span><strong>{{ money(pendingAmount) }}</strong></div>
    </section>

    <section class="refund-panel">
      <div class="toolbar">
        <label class="search-input"><span class="material-symbols-outlined">search</span><input v-model.trim="search" type="search" placeholder="Tìm mã vé, khách hàng, số tài khoản" /></label>
        <div class="status-tabs" role="group" aria-label="Lọc trạng thái">
          <button v-for="filter in filters" :key="filter.value" type="button" :class="{ active: activeStatus === filter.value }" @click="activeStatus = filter.value">{{ filter.label }}</button>
        </div>
      </div>

      <div v-if="loadError" class="state-message error-state"><span class="material-symbols-outlined">cloud_off</span><b>Không tải được yêu cầu hoàn tiền</b><p>{{ loadError }}</p><button type="button" @click="loadRefunds">Thử lại</button></div>
      <div v-else-if="loading" class="skeleton-list"><div v-for="item in 6" :key="item"></div></div>
      <div v-else-if="!paginatedRefunds.length" class="state-message"><span class="material-symbols-outlined">price_check</span><b>Không có yêu cầu phù hợp</b><p>Các yêu cầu hoàn tiền mới sẽ xuất hiện tại đây.</p></div>

      <div v-else class="table-wrap">
        <table>
          <thead><tr><th>Yêu cầu</th><th>Khách hàng</th><th>Nhận tiền</th><th>Số tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
          <tbody>
            <tr v-for="refund in paginatedRefunds" :key="refund.id">
              <td><strong>#RF{{ refund.id }}</strong><small>Vé #{{ refund.booking?.id }}</small><time>{{ dateTime(refund.createdAt) }}</time></td>
              <td><strong>{{ refund.user?.fullName || refund.booking?.customerName }}</strong><small>{{ refund.user?.phone || refund.booking?.customerPhone }}</small></td>
              <td>
                <template v-if="refund.refundMethod === 'BANK_TRANSFER'">
                  <strong>{{ refund.bankName }}</strong><code>{{ refund.bankAccountNumber }}</code><small>{{ refund.bankAccountName }}</small>
                </template>
                <template v-else><strong>Ví Trung Nam</strong><small>Đã xử lý tự động</small></template>
              </td>
              <td><strong class="amount">{{ money(refund.refundAmount) }}</strong><small>Phí hủy đã trừ</small></td>
              <td><span class="status-label" :class="statusClass(refund.status)">{{ statusText(refund.status) }}</span><small v-if="refund.processedBy">Bởi {{ refund.processedBy }}</small></td>
              <td>
                <div class="actions" v-if="refund.refundMethod === 'BANK_TRANSFER' && ['PENDING', 'APPROVED'].includes(refund.status)">
                  <button v-if="refund.status === 'PENDING'" type="button" class="approve" title="Duyệt yêu cầu" @click="approveRefund(refund)"><span class="material-symbols-outlined">check</span></button>
                  <button type="button" class="complete" title="Xác nhận đã chuyển" @click="openAction(refund, 'complete')"><span class="material-symbols-outlined">payments</span></button>
                  <button type="button" class="reject" title="Yêu cầu bổ sung thông tin" @click="openAction(refund, 'requestInfo')"><span class="material-symbols-outlined">edit_note</span></button>
                </div>
                <button v-else-if="refund.proofUrl" type="button" class="proof-link" @click="openProof(refund.proofUrl)">Xem biên lai</button>
                <span v-else class="muted-action">Đã xử lý</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminPagination :page="page" :total-pages="totalPages" :total-elements="filteredRefunds.length" :page-size="pageSize" :current-count="paginatedRefunds.length" @update:page="page = $event" />
    </section>

    <div v-if="actionRefund" class="action-overlay" @click.self="closeAction">
      <form class="action-modal" @submit.prevent="submitAction">
        <div class="modal-heading">
          <span class="material-symbols-outlined">{{ actionType === 'complete' ? 'account_balance' : 'report' }}</span>
          <div><h2>{{ actionType === 'complete' ? 'Xác nhận đã chuyển khoản' : 'Yêu cầu bổ sung thông tin' }}</h2><p>Yêu cầu #RF{{ actionRefund.id }} · {{ money(actionRefund.refundAmount) }}</p></div>
          <button type="button" aria-label="Đóng" @click="closeAction"><span class="material-symbols-outlined">close</span></button>
        </div>

        <template v-if="actionType === 'complete'">
          <dl class="bank-summary"><div><dt>Ngân hàng</dt><dd>{{ actionRefund.bankName }}</dd></div><div><dt>Số tài khoản</dt><dd>{{ actionRefund.bankAccountNumber }}</dd></div><div><dt>Chủ tài khoản</dt><dd>{{ actionRefund.bankAccountName }}</dd></div></dl>
          <label class="form-field"><span>Mã đối soát hệ thống</span><input v-model="actionForm.transactionCode" readonly aria-readonly="true" /></label>
          <label class="form-field"><span>Biên lai chuyển khoản (tùy chọn)</span><input ref="proofInput" class="hidden" type="file" accept="image/*" @change="uploadProof" /><button type="button" class="upload-button" :disabled="uploading" @click="proofInput?.click()"><span class="material-symbols-outlined">{{ uploading ? 'progress_activity' : actionForm.proofUrl ? 'check_circle' : 'upload' }}</span>{{ uploading ? 'Đang tải' : actionForm.proofUrl ? 'Đã tải biên lai' : 'Chọn ảnh biên lai' }}</button></label>
        </template>

        <label class="form-field"><span>{{ actionType === 'requestInfo' ? 'Thông tin khách cần bổ sung' : 'Ghi chú nội bộ' }}</span><textarea v-model.trim="actionForm.note" rows="3" maxlength="500" :placeholder="actionType === 'requestInfo' ? 'Ví dụ: Số tài khoản chưa chính xác, vui lòng kiểm tra lại' : 'Không bắt buộc'"></textarea></label>
        <p v-if="actionError" class="action-error"><span class="material-symbols-outlined">error</span>{{ actionError }}</p>
        <div class="modal-actions"><button type="button" @click="closeAction">Quay lại</button><button type="submit" :disabled="submitting || uploading">{{ submitting ? 'Đang xử lý' : actionType === 'complete' ? 'Xác nhận đã chuyển' : 'Gửi yêu cầu bổ sung' }}</button></div>
      </form>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'

defineProps({ embedded: { type: Boolean, default: false } })

const api = useApi()
const authStore = useAuthStore()
const refunds = ref([])
const loading = ref(true)
const loadError = ref('')
const search = ref('')
const activeStatus = ref('ALL')
const page = ref(0)
const pageSize = 10
const actionRefund = ref(null)
const actionType = ref('complete')
const actionForm = ref({ transactionCode: '', proofUrl: '', note: '' })
const actionError = ref('')
const submitting = ref(false)
const uploading = ref(false)
const proofInput = ref(null)
const filters = [{ value: 'ALL', label: 'Tất cả' }, { value: 'PENDING', label: 'Chờ xử lý' }, { value: 'APPROVED', label: 'Đã duyệt' }, { value: 'COMPLETED', label: 'Đã hoàn' }, { value: 'NEEDS_INFO', label: 'Cần bổ sung' }]

const filteredRefunds = computed(() => {
  const keyword = search.value.toLocaleLowerCase('vi-VN')
  return refunds.value.filter(refund => {
    if (activeStatus.value !== 'ALL' && normalizedStatus(refund.status) !== activeStatus.value) return false
    if (!keyword) return true
    return [refund.id, refund.booking?.id, refund.user?.fullName, refund.user?.phone, refund.bankName, refund.bankAccountNumber, refund.bankAccountName]
      .filter(value => value !== null && value !== undefined)
      .some(value => String(value).toLocaleLowerCase('vi-VN').includes(keyword))
  })
})
const totalPages = computed(() => Math.ceil(filteredRefunds.value.length / pageSize))
const paginatedRefunds = computed(() => filteredRefunds.value.slice(page.value * pageSize, (page.value + 1) * pageSize))
const pendingAmount = computed(() => refunds.value.filter(item => ['PENDING', 'APPROVED', 'NEEDS_INFO', 'REJECTED'].includes(item.status) && item.refundMethod === 'BANK_TRANSFER').reduce((sum, item) => sum + Number(item.refundAmount || 0), 0))

watch([search, activeStatus], () => { page.value = 0 })
watch(totalPages, pages => { if (pages && page.value >= pages) page.value = pages - 1 })

const loadRefunds = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const response = await api.get('/refund-requests/admin')
    refunds.value = Array.isArray(response.data) ? response.data : []
    syncPendingCount()
  } catch (error) {
    loadError.value = error.response?.data?.message || 'Không thể kết nối đến máy chủ.'
  } finally { loading.value = false }
}

const approveRefund = async refund => {
  try {
    const response = await api.put(`/refund-requests/admin/${refund.id}/approve`, { note: '' })
    replaceRefund(response.data)
  } catch (error) { window.alert(error.response?.data?.message || 'Không thể duyệt yêu cầu.') }
}

const openAction = (refund, type) => {
  actionRefund.value = refund
  actionType.value = type
  actionForm.value = { transactionCode: type === 'complete' ? generateTransactionCode(refund.id) : '', proofUrl: '', note: '' }
  actionError.value = ''
}
const closeAction = () => { if (!submitting.value) actionRefund.value = null }

const uploadProof = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.value = true
  actionError.value = ''
  const data = new FormData()
  data.append('file', file)
  data.append('upload_preset', 'skybus_preset')
  try {
    const response = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', data)
    actionForm.value.proofUrl = response.data.secure_url
  } catch { actionError.value = 'Không tải được ảnh biên lai.' }
  finally { uploading.value = false }
}

const submitAction = async () => {
  if (actionType.value === 'requestInfo' && !actionForm.value.note) { actionError.value = 'Vui lòng ghi rõ thông tin khách cần bổ sung.'; return }
  submitting.value = true
  actionError.value = ''
  try {
    const endpoint = actionType.value === 'complete' ? 'complete' : 'request-info'
    const response = await api.put(`/refund-requests/admin/${actionRefund.value.id}/${endpoint}`, actionForm.value)
    replaceRefund(response.data)
    closeAction()
  } catch (error) { actionError.value = error.response?.data?.message || 'Không thể xử lý yêu cầu.' }
  finally { submitting.value = false; if (!actionError.value) actionRefund.value = null }
}

const replaceRefund = updated => {
  const index = refunds.value.findIndex(item => item.id === updated.id)
  if (index >= 0) refunds.value[index] = updated
  syncPendingCount()
}
const syncPendingCount = () => authStore.setNewRefundsCount(
  refunds.value.filter(item => item.refundMethod === 'BANK_TRANSFER' && ['PENDING', 'APPROVED'].includes(item.status)).length
)
const normalizedStatus = status => status === 'REJECTED' ? 'NEEDS_INFO' : status
const statusCount = status => refunds.value.filter(item => normalizedStatus(item.status) === status).length
const statusText = status => ({ PENDING: 'Chờ xử lý', APPROVED: 'Đã duyệt', COMPLETED: 'Đã hoàn', NEEDS_INFO: 'Cần bổ sung', REJECTED: 'Cần bổ sung' }[status] || status)
const statusClass = status => String(normalizedStatus(status) || '').toLowerCase()
const money = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const dateTime = value => value ? new Date(value).toLocaleString('vi-VN') : ''
const openProof = url => window.open(url, '_blank', 'noopener,noreferrer')
const generateTransactionCode = refundId => {
  const now = new Date()
  const stamp = [now.getFullYear(), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0'), String(now.getHours()).padStart(2, '0'), String(now.getMinutes()).padStart(2, '0'), String(now.getSeconds()).padStart(2, '0')].join('')
  return `RF${refundId}-${stamp}`
}

onMounted(loadRefunds)
</script>

<style scoped>
.refund-page { max-width: 78rem; margin: 0 auto; padding: 1.8rem; color: #263936; }.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 1rem; }.page-heading p { color: #087269; font-size: .62rem; font-weight: 850; letter-spacing: .08em; text-transform: uppercase; }.page-heading h1 { margin-top: .15rem; color: #172e2b; font-size: 1.55rem; font-weight: 900; letter-spacing: -.035em; }.page-heading > div > span { display: block; margin-top: .3rem; color: #74837f; font-size: .72rem; }.page-heading > button { display: flex; align-items: center; gap: .4rem; border: 1px solid #d9e3e1; border-radius: .7rem; padding: .6rem .8rem; color: #49605b; background: white; font-size: .68rem; font-weight: 800; }.page-heading > button span { font-size: 1rem; }.stats-strip { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); margin-top: 1.35rem; border: 1px solid #dfe7e5; border-radius: 1rem; background: white; }.stats-strip > div { padding: 1rem 1.15rem; border-right: 1px solid #e7edeb; }.stats-strip > div:last-child { border: 0; }.stats-strip span { display: block; color: #84918e; font-size: .6rem; font-weight: 750; }.stats-strip strong { display: block; margin-top: .25rem; color: #173b37; font-size: 1.15rem; font-weight: 900; font-variant-numeric: tabular-nums; }.refund-panel { margin-top: 1.1rem; overflow: hidden; border: 1px solid #dfe7e5; border-radius: 1rem; background: white; box-shadow: 0 .8rem 2rem rgb(7 70 65 / .05); }.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border-bottom: 1px solid #e7edeb; padding: .9rem 1rem; }.search-input { display: flex; width: min(22rem, 100%); align-items: center; gap: .45rem; border: 1px solid #dbe4e2; border-radius: .65rem; padding: 0 .65rem; color: #81908c; background: #f8faf9; }.search-input span { font-size: 1rem; }.search-input input { width: 100%; min-height: 2.35rem; outline: none; color: #304743; background: transparent; font-size: .68rem; font-weight: 650; }.search-input:focus-within { border-color: #087269; box-shadow: 0 0 0 3px rgb(8 114 105 / .08); }.status-tabs { display: flex; gap: .2rem; padding: .22rem; border-radius: .65rem; background: #eef3f2; }.status-tabs button { border-radius: .5rem; padding: .52rem .65rem; color: #657570; font-size: .62rem; font-weight: 800; white-space: nowrap; }.status-tabs button.active { color: #075955; background: white; box-shadow: 0 .15rem .5rem rgb(7 70 65 / .08); }.table-wrap { overflow-x: auto; }table { width: 100%; border-collapse: collapse; }th { padding: .7rem .85rem; color: #84918e; background: #f7faf9; font-size: .56rem; font-weight: 850; letter-spacing: .05em; text-align: left; text-transform: uppercase; }td { padding: .85rem; border-bottom: 1px solid #edf1f0; vertical-align: top; }tr:last-child td { border-bottom: 0; }td > strong, td > small, td > time, td > code { display: block; }td > strong { color: #30433f; font-size: .68rem; font-weight: 850; }td > small, td > time { margin-top: .18rem; color: #87938f; font-size: .58rem; }td > code { margin-top: .2rem; color: #075955; font-size: .66rem; font-weight: 850; }.amount { color: #075955; font-size: .78rem; font-variant-numeric: tabular-nums; }.status-label { display: inline-flex; border-radius: .5rem; padding: .35rem .5rem; font-size: .58rem; font-weight: 850; }.status-label.pending { color: #986408; background: #fff3d4; }.status-label.approved { color: #236191; background: #eaf5ff; }.status-label.completed { color: #08705e; background: #e8f7f1; }.status-label.rejected { color: #aa354c; background: #fff0f3; }.actions { display: flex; gap: .3rem; }.actions button { display: grid; width: 1.9rem; height: 1.9rem; place-items: center; border-radius: .5rem; transition: transform .2s, background .2s; }.actions button:active { transform: translateY(1px); }.actions span { font-size: 1rem; }.actions .approve { color: #236191; background: #eaf5ff; }.actions .complete { color: #08705e; background: #e8f7f1; }.actions .reject { color: #aa354c; background: #fff0f3; }.proof-link { color: #087269; font-size: .62rem; font-weight: 800; text-decoration: underline; }.muted-action { color: #96a19e; font-size: .6rem; }.state-message { display: flex; min-height: 20rem; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; text-align: center; }.state-message > span { color: #8aa09b; font-size: 2rem; }.state-message b { margin-top: .6rem; font-size: .8rem; }.state-message p { margin-top: .25rem; color: #87938f; font-size: .65rem; }.state-message button { margin-top: .7rem; border-radius: .55rem; padding: .5rem .7rem; color: white; background: #075955; font-size: .63rem; font-weight: 800; }.error-state { color: #a6384d; }.skeleton-list { display: grid; }.skeleton-list div { height: 4.8rem; border-bottom: 1px solid #edf1f0; background: linear-gradient(90deg, #f4f7f6 20%, #e8efed 40%, #f4f7f6 60%); background-size: 220% 100%; animation: shimmer 1.3s linear infinite; }.action-overlay { position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center; padding: 1rem; background: rgb(9 31 29 / .68); backdrop-filter: blur(6px); }.action-modal { width: min(31rem, 100%); max-height: 90dvh; overflow-y: auto; border-radius: 1.1rem; padding: 1.25rem; background: white; box-shadow: 0 2rem 5rem rgb(0 45 42 / .3); }.modal-heading { display: grid; grid-template-columns: 2.5rem 1fr auto; align-items: start; gap: .7rem; }.modal-heading > span { display: grid; width: 2.5rem; height: 2.5rem; place-items: center; border-radius: .7rem; color: white; background: #075955; }.modal-heading h2 { color: #243b37; font-size: .92rem; font-weight: 900; }.modal-heading p { margin-top: .15rem; color: #80908c; font-size: .63rem; }.modal-heading button { color: #83908d; }.bank-summary { display: grid; gap: .1rem; margin-top: 1rem; border-radius: .75rem; padding: .7rem .85rem; background: #f3f7f6; }.bank-summary div { display: flex; justify-content: space-between; gap: 1rem; padding: .3rem 0; }.bank-summary dt { color: #7b8a87; font-size: .63rem; }.bank-summary dd { color: #304540; font-size: .66rem; font-weight: 850; text-align: right; }.form-field { display: grid; gap: .4rem; margin-top: .9rem; }.form-field > span { color: #596d68; font-size: .62rem; font-weight: 800; }.form-field input, .form-field textarea { border: 1px solid #d8e2df; border-radius: .65rem; padding: .65rem .7rem; outline: none; color: #2d433f; background: #fafcfb; font-size: .7rem; font-weight: 650; }.form-field input:focus, .form-field textarea:focus { border-color: #087269; box-shadow: 0 0 0 3px rgb(8 114 105 / .08); }.upload-button { display: flex; align-items: center; justify-content: center; gap: .4rem; border: 1px dashed #aac8c3; border-radius: .65rem; padding: .75rem; color: #087269; background: #f2f9f7; font-size: .68rem; font-weight: 800; }.upload-button span { font-size: 1rem; }.action-error { display: flex; gap: .35rem; margin-top: .8rem; border-radius: .55rem; padding: .55rem; color: #a6384d; background: #fff0f3; font-size: .65rem; font-weight: 750; }.action-error span { font-size: .9rem; }.modal-actions { display: flex; justify-content: flex-end; gap: .55rem; margin-top: 1rem; }.modal-actions button { border-radius: .65rem; padding: .65rem .8rem; color: #536762; background: #eef2f1; font-size: .65rem; font-weight: 850; }.modal-actions button:last-child { color: white; background: #075955; }.modal-actions button:disabled { cursor: wait; opacity: .6; }button:focus-visible, input:focus-visible, textarea:focus-visible { outline: 3px solid rgb(216 173 55 / .5); outline-offset: 2px; }@keyframes shimmer { to { background-position-x: -220%; } }@media (max-width: 820px) { .stats-strip { grid-template-columns: repeat(2, 1fr); }.stats-strip > div:nth-child(2) { border-right: 0; }.toolbar { align-items: stretch; flex-direction: column; }.search-input { width: 100%; }.status-tabs { overflow-x: auto; } }@media (max-width: 600px) { .refund-page { padding: 1rem; }.page-heading { align-items: flex-start; }.stats-strip { grid-template-columns: 1fr 1fr; }.stats-strip > div { padding: .8rem; }.compact-hide { display: none; } }
.status-label.needs_info { color: #aa354c; background: #fff0f3; }
.refund-page.embedded { max-width: none; padding: 0; }
.refund-page.embedded .stats-strip { margin-top: 0; }
</style>
