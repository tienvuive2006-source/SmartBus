<template>
  <div class="topup-manager">
    <div v-if="notice.text" class="notice" :class="notice.type" role="status">
      <span class="material-symbols-outlined">{{ notice.type === 'error' ? 'error' : 'check_circle' }}</span>
      <span>{{ notice.text }}</span>
      <button type="button" aria-label="Đóng thông báo" @click="notice.text = ''">
        <span class="material-symbols-outlined">close</span>
      </button>
    </div>

    <div class="workspace-grid">
      <section class="editor-panel" aria-labelledby="topup-form-title">
        <div class="panel-heading">
          <div>
            <p class="eyebrow">Cấu hình mốc thưởng</p>
            <h2 id="topup-form-title">{{ editingIndex === null ? 'Tạo mốc khuyến mãi' : 'Chỉnh sửa mốc khuyến mãi' }}</h2>
          </div>
          <span class="edit-state">{{ editingIndex === null ? 'Mới' : `Mốc ${editingIndex + 1}` }}</span>
        </div>

        <form class="promotion-form" @submit.prevent="saveDraft">
          <div class="form-section">
            <h3>Khoảng nạp tiền</h3>
            <div class="field-grid two-cols">
              <label class="field">
                <span>Từ (VNĐ) <b>*</b></span>
                <input v-model.number="draft.minAmount" type="number" min="0" step="1000" required placeholder="100.000" />
              </label>
              <label class="field">
                <span>Đến (VNĐ)</span>
                <input v-model.number="draft.maxAmount" type="number" min="0" step="1000" placeholder="Không giới hạn" />
              </label>
            </div>
          </div>

          <div class="form-section">
            <h3>Giá trị thưởng</h3>
            <div class="reward-choice">
              <span class="radio-dot"></span>
              <div>
                <strong>Tỷ lệ thưởng (%)</strong>
                <small>Tiền thưởng được tính theo số tiền khách nạp.</small>
              </div>
            </div>
            <label class="field reward-field">
              <span>Tỷ lệ thưởng (%) <b>*</b></span>
              <div class="input-suffix">
                <input v-model.number="draft.bonusPercent" type="number" min="0" max="100" step="0.1" required placeholder="10" />
                <span>%</span>
              </div>
            </label>
          </div>

          <div class="form-section">
            <h3>Thời gian áp dụng</h3>
            <div class="field-grid two-cols">
              <label class="field">
                <span>Từ ngày</span>
                <input v-model="draft.startDate" type="date" />
              </label>
              <label class="field">
                <span>Đến ngày</span>
                <input v-model="draft.endDate" type="date" />
              </label>
            </div>
          </div>

          <div class="preview-strip">
            <span class="material-symbols-outlined">visibility</span>
            <div>
              <small>Xem trước kết quả</small>
              <strong>Nạp {{ formatMoney(previewAmount) }} <span>→</span> Nhận thêm {{ formatMoney(previewBonus) }}</strong>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="button ghost" @click="resetEditor">Hủy</button>
            <button type="submit" class="button save" :disabled="savingPromotions">
              <span class="material-symbols-outlined">{{ savingPromotions ? 'progress_activity' : 'save' }}</span>
              {{ savingPromotions ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
            <button type="button" class="button add" @click="startCreate">
              <span class="material-symbols-outlined">add</span> Thêm mốc
            </button>
          </div>
        </form>
      </section>

      <div class="list-column">
        <section class="list-panel" aria-labelledby="promotion-list-title">
          <div class="panel-heading list-heading">
            <div>
              <p class="eyebrow">Thiết lập đang áp dụng</p>
              <h2 id="promotion-list-title">Danh sách mốc khuyến mãi</h2>
            </div>
            <span class="record-count">{{ filteredPromotions.length }} / {{ topupPromotions.length }} mốc</span>
          </div>

          <div class="table-tools">
            <label class="search-box">
              <span class="material-symbols-outlined">search</span>
              <input v-model.trim="searchTerm" type="search" placeholder="Tìm theo khoảng tiền, tỷ lệ thưởng..." />
            </label>
            <select v-model="statusFilter" aria-label="Lọc trạng thái">
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="EXPIRING">Sắp hết hạn</option>
              <option value="SCHEDULED">Sắp diễn ra</option>
              <option value="EXPIRED">Hết hạn</option>
            </select>
            <button type="button" class="icon-button" title="Làm mới" @click="refresh">
              <span class="material-symbols-outlined">refresh</span>
            </button>
          </div>

          <div v-if="loadingPromotions" class="loading-state">
            <span class="material-symbols-outlined">progress_activity</span>
            Đang tải cấu hình khuyến mãi...
          </div>

          <div v-else-if="topupPromotions.length === 0" class="empty-state">
            <span class="material-symbols-outlined">loyalty</span>
            <h3>Chưa có mốc khuyến mãi</h3>
            <p>Thêm mốc đầu tiên để tự động thưởng khi khách hàng nạp ví.</p>
            <button type="button" class="button add" @click="startCreate">Tạo mốc đầu tiên</button>
          </div>

          <div v-else class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Khoảng nạp tiền</th>
                  <th>Tỷ lệ thưởng</th>
                  <th>Thời gian áp dụng</th>
                  <th>Trạng thái</th>
                  <th class="actions-cell">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in filteredPromotions" :key="item.index" :class="{ selected: item.index === editingIndex }">
                  <td><strong>{{ formatRange(item.tier) }}</strong></td>
                  <td>
                    <strong class="bonus-value">{{ cleanNumber(item.tier.bonusPercent) }}%</strong>
                    <small>{{ bonusExample(item.tier) }}</small>
                  </td>
                  <td>
                    <strong>{{ formatPeriod(item.tier) }}</strong>
                    <small>{{ periodCaption(item.tier) }}</small>
                  </td>
                  <td><span class="status-badge" :class="statusOf(item.tier).key.toLowerCase()">{{ statusOf(item.tier).label }}</span></td>
                  <td class="actions-cell">
                    <button type="button" title="Chỉnh sửa" @click="editTier(item.index)"><span class="material-symbols-outlined">edit</span></button>
                    <button type="button" class="danger" title="Xóa" @click="removePromotionTier(item.index)"><span class="material-symbols-outlined">delete</span></button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredPromotions.length === 0" class="no-result">Không tìm thấy mốc phù hợp với bộ lọc.</div>
          </div>
        </section>

        <div class="insight-grid">
          <section class="insight overview">
            <h3>Tổng quan chương trình</h3>
            <div class="mini-stats">
              <div><span>Tổng số mốc</span><strong>{{ topupPromotions.length }}</strong></div>
              <div><span>Đang hoạt động</span><strong>{{ statusCounts.ACTIVE }}</strong></div>
              <div><span>Sắp hết hạn</span><strong>{{ statusCounts.EXPIRING }}</strong></div>
              <div><span>Hết hạn</span><strong>{{ statusCounts.EXPIRED }}</strong></div>
            </div>
          </section>

          <section class="insight warning">
            <h3><span class="material-symbols-outlined">warning</span> Cảnh báo & kiểm tra</h3>
            <template v-if="validationIssues.length">
              <strong>Phát hiện {{ validationIssues.length }} điểm cần lưu ý</strong>
              <ul><li v-for="issue in validationIssues.slice(0, 3)" :key="issue">{{ issue }}</li></ul>
            </template>
            <div v-else class="healthy"><span class="material-symbols-outlined">verified</span><p>Cấu hình hợp lệ, các khoảng tiền không chồng chéo.</p></div>
          </section>

          <section class="insight activity">
            <h3>Hoạt động trong phiên</h3>
            <ol v-if="activities.length">
              <li v-for="activity in activities.slice(0, 3)" :key="activity.id">
                <span></span><div><strong>{{ activity.text }}</strong><small>{{ activity.time }}</small></div>
              </li>
            </ol>
            <div v-else class="healthy muted"><span class="material-symbols-outlined">history</span><p>Chưa có thay đổi nào trong phiên làm việc này.</p></div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useApi } from '@/composables/useApi'

const emit = defineEmits(['summary'])
const api = useApi()
const topupPromotions = ref([])
const loadingPromotions = ref(false)
const savingPromotions = ref(false)
const editingIndex = ref(null)
const searchTerm = ref('')
const statusFilter = ref('ALL')
const activities = ref([])
const notice = reactive({ type: 'success', text: '' })
const blankTier = () => ({ minAmount: 0, maxAmount: null, bonusPercent: 10, startDate: null, endDate: null })
const draft = ref(blankTier())

const cleanNumber = value => Number(value || 0).toLocaleString('vi-VN', { maximumFractionDigits: 2 })
const formatMoney = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const formatDate = value => value ? new Intl.DateTimeFormat('vi-VN').format(new Date(`${value}T00:00:00`)) : 'Không giới hạn'
const todayString = () => new Date().toISOString().slice(0, 10)

const statusOf = tier => {
  const today = new Date(`${todayString()}T00:00:00`)
  const start = tier.startDate ? new Date(`${tier.startDate}T00:00:00`) : null
  const end = tier.endDate ? new Date(`${tier.endDate}T23:59:59`) : null
  if (end && end < today) return { key: 'EXPIRED', label: 'Hết hạn' }
  if (start && start > today) return { key: 'SCHEDULED', label: 'Sắp diễn ra' }
  if (end && end.getTime() - today.getTime() <= 7 * 86400000) return { key: 'EXPIRING', label: 'Sắp hết hạn' }
  return { key: 'ACTIVE', label: 'Đang hoạt động' }
}

const statusCounts = computed(() => topupPromotions.value.reduce((counts, tier) => {
  counts[statusOf(tier).key] += 1
  return counts
}, { ACTIVE: 0, EXPIRING: 0, SCHEDULED: 0, EXPIRED: 0 }))

const publishSummary = () => emit('summary', {
  total: topupPromotions.value.length,
  active: statusCounts.value.ACTIVE,
  expiring: statusCounts.value.EXPIRING,
  usage: 0
})

const filteredPromotions = computed(() => {
  const term = searchTerm.value.toLowerCase()
  return topupPromotions.value
    .map((tier, index) => ({ tier, index }))
    .filter(({ tier }) => statusFilter.value === 'ALL' || statusOf(tier).key === statusFilter.value)
    .filter(({ tier }) => !term || `${formatRange(tier)} ${tier.bonusPercent}%`.toLowerCase().includes(term))
})

const validationIssues = computed(() => {
  const issues = []
  const sorted = topupPromotions.value.map((tier, index) => ({ ...tier, index })).sort((a, b) => Number(a.minAmount) - Number(b.minAmount))
  sorted.forEach((tier, position) => {
    if (tier.maxAmount != null && Number(tier.maxAmount) <= Number(tier.minAmount)) issues.push(`Mốc ${tier.index + 1}: số tiền kết thúc phải lớn hơn số tiền bắt đầu.`)
    if (tier.startDate && tier.endDate && tier.startDate > tier.endDate) issues.push(`Mốc ${tier.index + 1}: ngày kết thúc đang trước ngày bắt đầu.`)
    const next = sorted[position + 1]
    if (next && (tier.maxAmount == null || Number(tier.maxAmount) >= Number(next.minAmount))) issues.push(`Mốc ${tier.index + 1} và ${next.index + 1} đang có khoảng tiền chồng chéo.`)
  })
  return issues
})

const previewAmount = computed(() => Number(draft.value.minAmount) || 0)
const previewBonus = computed(() => Math.round(previewAmount.value * (Number(draft.value.bonusPercent) || 0) / 100))
const formatRange = tier => tier.maxAmount == null || tier.maxAmount === ''
  ? `Từ ${formatMoney(tier.minAmount)}`
  : `${formatMoney(tier.minAmount)} – ${formatMoney(tier.maxAmount)}`
const formatPeriod = tier => `${formatDate(tier.startDate)} – ${formatDate(tier.endDate)}`
const periodCaption = tier => !tier.startDate && !tier.endDate ? 'Không giới hạn thời gian' : statusOf(tier).label
const bonusExample = tier => `Ví dụ 1.000.000đ → +${formatMoney(1000000 * Number(tier.bonusPercent || 0) / 100)}`

const showNotice = (text, type = 'success') => {
  notice.text = text
  notice.type = type
}

const recordActivity = text => activities.value.unshift({ id: Date.now(), text, time: new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit' }).format(new Date()) })

const fetchTopupPromotions = async () => {
  loadingPromotions.value = true
  try {
    const response = await api.get('/settings/TOPUP_PROMOTIONS')
    topupPromotions.value = response.data?.value ? JSON.parse(response.data.value) : []
    publishSummary()
    if (editingIndex.value !== null && !topupPromotions.value[editingIndex.value]) resetEditor()
  } catch (error) {
    console.error(error)
    showNotice('Không thể tải cấu hình khuyến mãi nạp ví.', 'error')
  } finally {
    loadingPromotions.value = false
  }
}

const persist = async (message) => {
  savingPromotions.value = true
  try {
    await api.put('/settings/TOPUP_PROMOTIONS', { value: JSON.stringify(topupPromotions.value) })
    publishSummary()
    recordActivity(message)
    showNotice('Đã lưu cấu hình khuyến mãi nạp ví.')
    return true
  } catch (error) {
    console.error(error)
    showNotice(`Không thể lưu cấu hình: ${error.response?.data?.message || error.message}`, 'error')
    return false
  } finally {
    savingPromotions.value = false
  }
}

const validateDraft = () => {
  if (Number(draft.value.minAmount) < 0 || Number(draft.value.bonusPercent) < 0 || Number(draft.value.bonusPercent) > 100) return 'Giá trị tiền và tỷ lệ thưởng chưa hợp lệ.'
  if (draft.value.maxAmount !== null && draft.value.maxAmount !== '' && Number(draft.value.maxAmount) <= Number(draft.value.minAmount)) return 'Số tiền kết thúc phải lớn hơn số tiền bắt đầu.'
  if (draft.value.startDate && draft.value.endDate && draft.value.startDate > draft.value.endDate) return 'Ngày kết thúc phải sau ngày bắt đầu.'
  return ''
}

const saveDraft = async () => {
  const error = validateDraft()
  if (error) return showNotice(error, 'error')
  const normalized = {
    minAmount: Number(draft.value.minAmount || 0),
    maxAmount: draft.value.maxAmount === '' || draft.value.maxAmount == null ? null : Number(draft.value.maxAmount),
    bonusPercent: Number(draft.value.bonusPercent || 0),
    startDate: draft.value.startDate || null,
    endDate: draft.value.endDate || null
  }
  const target = editingIndex.value
  if (target === null) topupPromotions.value.push(normalized)
  else topupPromotions.value.splice(target, 1, normalized)
  const saved = await persist(target === null ? `Đã tạo mốc ${formatRange(normalized)}` : `Đã cập nhật mốc ${formatRange(normalized)}`)
  if (saved) {
    editingIndex.value = topupPromotions.value.indexOf(normalized)
    draft.value = { ...normalized }
  } else if (target === null) topupPromotions.value.pop()
}

const editTier = index => {
  editingIndex.value = index
  draft.value = { ...topupPromotions.value[index] }
}

const startCreate = () => {
  editingIndex.value = null
  draft.value = blankTier()
}

const resetEditor = () => {
  if (editingIndex.value !== null && topupPromotions.value[editingIndex.value]) draft.value = { ...topupPromotions.value[editingIndex.value] }
  else startCreate()
  notice.text = ''
}

const removePromotionTier = async index => {
  const tier = topupPromotions.value[index]
  if (!window.confirm(`Xóa mốc ${formatRange(tier)}?`)) return
  topupPromotions.value.splice(index, 1)
  const saved = await persist(`Đã xóa mốc ${formatRange(tier)}`)
  if (!saved) topupPromotions.value.splice(index, 0, tier)
  startCreate()
}

const exportData = () => {
  const rows = [['Khoảng nạp', 'Tỷ lệ thưởng', 'Từ ngày', 'Đến ngày', 'Trạng thái'], ...topupPromotions.value.map(tier => [formatRange(tier), `${tier.bonusPercent}%`, tier.startDate || '', tier.endDate || '', statusOf(tier).label])]
  const csv = `\uFEFF${rows.map(row => row.map(cell => `"${String(cell).replaceAll('"', '""')}"`).join(',')).join('\n')}`
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `khuyen-mai-nap-vi-${todayString()}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const refresh = fetchTopupPromotions
defineExpose({ startCreate, refresh, exportData })
onMounted(fetchTopupPromotions)
</script>

<style scoped>
.topup-manager { position: relative; }
.notice { position: fixed; top: 5rem; right: 1.25rem; z-index: 60; max-width: 26rem; display: flex; align-items: center; gap: .65rem; padding: .8rem 1rem; border: 1px solid #bbebd1; border-radius: .7rem; background: #effcf5; color: #08704b; box-shadow: 0 16px 35px rgba(18, 60, 50, .15); font-size: .8rem; font-weight: 700; }
.notice.error { color: #a33c24; border-color: #fed2c5; background: #fff4ef; }
.notice > button { margin-left: auto; display: grid; place-items: center; }
.notice .material-symbols-outlined { font-size: 1.15rem; }
.workspace-grid { display: grid; grid-template-columns: minmax(320px, .72fr) minmax(640px, 1.45fr); gap: .9rem; align-items: start; }
.editor-panel, .list-panel, .insight { background: #fff; border: 1px solid #e1e6eb; border-radius: .8rem; box-shadow: 0 7px 24px rgba(30, 48, 61, .04); }
.editor-panel { padding: 1rem; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding-bottom: .8rem; border-bottom: 1px solid #edf0f3; }
.panel-heading h2 { margin: .1rem 0 0; color: #18212d; font-size: .92rem; font-weight: 900; }
.eyebrow { margin: 0; color: #8b95a4; font-size: .64rem; font-weight: 800; letter-spacing: .08em; text-transform: uppercase; }
.edit-state, .record-count { padding: .3rem .55rem; border-radius: .35rem; background: #eff8f7; color: #087b76; font-size: .65rem; font-weight: 900; }
.promotion-form { padding-top: .7rem; }
.form-section { padding: .55rem 0 .75rem; }
.form-section + .form-section { border-top: 1px solid #f0f2f4; }
.form-section h3 { margin: 0 0 .55rem; color: #2b3440; font-size: .73rem; font-weight: 900; }
.field-grid { display: grid; gap: .65rem; }
.two-cols { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.field { display: block; min-width: 0; }
.field > span { display: block; margin-bottom: .3rem; color: #465161; font-size: .65rem; font-weight: 800; }
.field b { color: #e65335; }
.field input, .table-tools input, .table-tools select { width: 100%; height: 2.35rem; border: 1px solid #dfe4e9; border-radius: .45rem; background: #fff; padding: 0 .65rem; color: #27313f; font-size: .72rem; font-weight: 650; outline: none; transition: border-color .2s ease, box-shadow .2s ease; }
.field input:focus, .table-tools input:focus, .table-tools select:focus { border-color: #0a8882; box-shadow: 0 0 0 3px rgba(10, 136, 130, .1); }
.reward-choice { display: flex; gap: .55rem; align-items: flex-start; margin-bottom: .65rem; }
.radio-dot { width: .78rem; height: .78rem; margin-top: .15rem; border: 3px solid #0c8781; border-radius: 50%; box-shadow: 0 0 0 1px #0c8781; }
.reward-choice strong { display: block; color: #344050; font-size: .7rem; }
.reward-choice small { display: block; margin-top: .15rem; color: #929ba7; font-size: .61rem; }
.reward-field { max-width: calc(50% - .325rem); }
.input-suffix { position: relative; }
.input-suffix input { padding-right: 2rem; }
.input-suffix > span { position: absolute; right: .7rem; top: 50%; transform: translateY(-50%); color: #087b76; font-size: .72rem; font-weight: 900; }
.preview-strip { display: flex; align-items: flex-start; gap: .55rem; margin-top: .2rem; padding: .7rem; border: 1px solid #dce9e7; border-radius: .55rem; background: #f7fbfa; }
.preview-strip .material-symbols-outlined { color: #087b76; font-size: 1rem; }
.preview-strip small { display: block; color: #6d7887; font-size: .62rem; font-weight: 800; }
.preview-strip strong { display: block; margin-top: .25rem; color: #344050; font-size: .68rem; }
.preview-strip strong span { padding-inline: .35rem; color: #91a0a8; }
.form-actions { display: grid; grid-template-columns: auto 1fr 1fr; gap: .5rem; margin-top: .8rem; padding-top: .75rem; border-top: 1px solid #edf0f3; }
.button { min-height: 2.25rem; display: inline-flex; align-items: center; justify-content: center; gap: .35rem; padding: 0 .75rem; border: 1px solid transparent; border-radius: .45rem; font-size: .68rem; font-weight: 900; transition: transform .18s ease, background-color .18s ease; }
.button:active { transform: scale(.98); }
.button:focus-visible, .actions-cell button:focus-visible, .icon-button:focus-visible { outline: 3px solid rgba(7, 132, 126, .18); outline-offset: 2px; }
.button .material-symbols-outlined { font-size: 1rem; }
.button:disabled { cursor: wait; opacity: .55; }
.button.ghost { border-color: #d9dfe5; color: #667181; background: #fff; }
.button.save { color: #fff; background: #087b76; }
.button.add { color: #2f2f13; background: #f7b916; }
.list-column { min-width: 0; display: grid; gap: .9rem; }
.list-panel { min-width: 0; overflow: hidden; }
.list-heading { padding: 1rem; }
.table-tools { display: grid; grid-template-columns: minmax(220px, 1fr) 10rem 2.35rem; gap: .55rem; padding: .75rem 1rem; }
.search-box { position: relative; }
.search-box .material-symbols-outlined { position: absolute; left: .65rem; top: 50%; transform: translateY(-50%); color: #93a0ad; font-size: 1rem; }
.search-box input { padding-left: 2rem; }
.table-tools select { appearance: auto; }
.icon-button { display: grid; place-items: center; border: 1px solid #dfe4e9; border-radius: .45rem; background: #fff; color: #607080; }
.icon-button:hover { color: #087b76; background: #f0fdfa; }
.icon-button .material-symbols-outlined { font-size: 1rem; }
.table-wrap { overflow-x: auto; border-top: 1px solid #edf0f3; }
table { width: 100%; min-width: 730px; border-collapse: collapse; }
th { padding: .65rem .7rem; background: #f8fafb; color: #536071; text-align: left; font-size: .62rem; font-weight: 900; white-space: nowrap; }
td { padding: .7rem; border-top: 1px solid #edf0f3; color: #3f4a58; font-size: .67rem; vertical-align: middle; }
tbody tr { transition: background-color .18s ease; }
tbody tr:hover, tbody tr.selected { background: #f3fbf9; }
td strong { display: block; color: #27313e; font-weight: 850; white-space: nowrap; }
td small { display: block; margin-top: .18rem; color: #8d98a6; white-space: nowrap; }
.bonus-value { color: #087b76; }
.status-badge { display: inline-flex; padding: .28rem .45rem; border-radius: .32rem; font-size: .58rem; font-weight: 900; white-space: nowrap; }
.status-badge.active { color: #08724f; background: #e8f7ef; border: 1px solid #c7ead8; }
.status-badge.expiring { color: #d36200; background: #fff1e3; border: 1px solid #ffd7ad; }
.status-badge.expired { color: #657180; background: #edf0f3; border: 1px solid #dce1e6; }
.status-badge.scheduled { color: #176bb0; background: #e9f3fc; border: 1px solid #d1e6f8; }
.actions-cell { text-align: right; white-space: nowrap; }
.actions-cell button { width: 1.8rem; height: 1.8rem; display: inline-grid; place-items: center; color: #087b76; border-radius: .35rem; }
.actions-cell button:hover { background: #e9f8f5; }
.actions-cell button.danger { color: #e5483f; }
.actions-cell button.danger:hover { background: #fff0ee; }
.actions-cell .material-symbols-outlined { font-size: 1rem; }
.loading-state, .empty-state, .no-result { display: flex; align-items: center; justify-content: center; gap: .5rem; min-height: 10rem; color: #82909e; font-size: .75rem; }
.loading-state .material-symbols-outlined { animation: spin 1s linear infinite; }
.empty-state { flex-direction: column; padding: 2rem; text-align: center; }
.empty-state > .material-symbols-outlined { font-size: 2.3rem; color: #9bc4bf; }
.empty-state h3 { margin: .25rem 0 0; color: #354250; font-size: .9rem; }
.empty-state p { margin: 0 0 .5rem; }
.no-result { min-height: 5rem; }
.insight-grid { display: grid; grid-template-columns: .9fr 1fr 1.15fr; gap: .9rem; }
.insight { min-height: 10.2rem; padding: .9rem; }
.insight h3 { display: flex; align-items: center; gap: .35rem; margin: 0 0 .7rem; color: #27313e; font-size: .72rem; font-weight: 900; }
.insight h3 .material-symbols-outlined { color: #ea7b0b; font-size: 1rem; }
.mini-stats { display: grid; grid-template-columns: 1fr 1fr; gap: .45rem; }
.mini-stats div { padding: .55rem; border: 1px solid #e4e9ed; border-radius: .45rem; background: #fafcfd; }
.mini-stats span { display: block; color: #7d8997; font-size: .56rem; font-weight: 700; }
.mini-stats strong { display: block; margin-top: .15rem; color: #087b76; font-size: 1.1rem; font-weight: 900; }
.warning { background: #fffdf8; }
.warning > strong { color: #d26707; font-size: .62rem; }
.warning ul { margin: .55rem 0 0; padding-left: 1rem; color: #66717f; font-size: .58rem; line-height: 1.5; }
.healthy { height: 6.5rem; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: .35rem; color: #5e7c71; text-align: center; }
.healthy .material-symbols-outlined { color: #3ca273; }
.healthy p { margin: 0; max-width: 13rem; font-size: .6rem; line-height: 1.45; }
.healthy.muted, .healthy.muted .material-symbols-outlined { color: #94a0ad; }
.activity ol { margin: 0; padding: 0; list-style: none; }
.activity li { position: relative; display: flex; gap: .55rem; padding-bottom: .55rem; }
.activity li > span { width: .45rem; height: .45rem; flex: 0 0 .45rem; margin-top: .18rem; border: 2px solid #07847e; border-radius: 50%; background: #fff; }
.activity li:not(:last-child)::after { content: ''; position: absolute; left: .2rem; top: .65rem; bottom: .05rem; width: 1px; background: #cdd7dc; }
.activity li strong { display: block; color: #586473; font-size: .59rem; line-height: 1.35; }
.activity li small { display: block; margin-top: .12rem; color: #9ba4af; font-size: .54rem; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 1280px) { .workspace-grid { grid-template-columns: 1fr; } .editor-panel { order: 2; } .list-column { order: 1; } }
@media (max-width: 820px) { .insight-grid { grid-template-columns: 1fr; } .table-tools { grid-template-columns: 1fr 2.35rem; } .table-tools select { grid-row: 2; grid-column: 1 / -1; } }
@media (max-width: 560px) { .two-cols, .form-actions { grid-template-columns: 1fr; } .reward-field { max-width: none; } .button.ghost { order: 3; } }
</style>
