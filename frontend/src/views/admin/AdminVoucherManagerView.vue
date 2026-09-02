<template>
  <section class="voucher-page">
    <header class="page-header">
      <div>
        <nav class="breadcrumb" aria-label="Điều hướng">
          <span>Trang chủ</span><span>/</span><span>Khuyến mãi</span><span>/</span>
          <strong>Quản lý khuyến mãi</strong>
        </nav>
        <div class="title-row">
          <span class="material-symbols-outlined title-icon">local_offer</span>
          <div>
            <h1>Quản lý khuyến mãi</h1>
            <p>Tạo và quản lý mã giảm giá, ưu đãi đổi điểm và các mốc thưởng nạp ví.</p>
          </div>
        </div>
      </div>

      <div class="header-actions">
        <button class="btn btn-secondary" type="button" @click="refreshActive">
          <span class="material-symbols-outlined">refresh</span>
          Làm mới
        </button>
        <button class="btn btn-secondary" type="button" @click="exportActive">
          <span class="material-symbols-outlined">download</span>
          Xuất dữ liệu
        </button>
        <button class="btn btn-primary" type="button" @click="createActive">
          <span class="material-symbols-outlined">add</span>
          Tạo chương trình
        </button>
      </div>
    </header>

    <div class="stats-grid" aria-label="Tổng quan khuyến mãi">
      <article v-for="stat in stats" :key="stat.label" class="stat-card">
        <div class="stat-icon" :class="stat.tone">
          <span class="material-symbols-outlined">{{ stat.icon }}</span>
        </div>
        <div>
          <p>{{ stat.label }}</p>
          <strong>{{ stat.value }}</strong>
          <small>{{ stat.caption }}</small>
        </div>
      </article>
    </div>

    <div class="tabs" role="tablist" aria-label="Loại khuyến mãi">
      <button
        type="button"
        role="tab"
        :aria-selected="activeTab === 'vouchers'"
        :class="{ active: activeTab === 'vouchers' }"
        @click="activeTab = 'vouchers'"
      >
        Mã giảm giá (Đổi điểm)
      </button>
      <button
        type="button"
        role="tab"
        :aria-selected="activeTab === 'topup'"
        :class="{ active: activeTab === 'topup' }"
        @click="activeTab = 'topup'"
      >
        Khuyến mãi nạp ví
      </button>
    </div>

    <AdminVoucherTab
      v-show="activeTab === 'vouchers'"
      ref="voucherTabRef"
      @summary="voucherSummary = $event"
    />
    <AdminTopupPromotionTab
      v-show="activeTab === 'topup'"
      ref="topupTabRef"
      @summary="topupSummary = $event"
    />
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import AdminVoucherTab from '@/components/admin/voucher/AdminVoucherTab.vue'
import AdminTopupPromotionTab from '@/components/admin/voucher/AdminTopupPromotionTab.vue'

const activeTab = ref('topup')
const voucherTabRef = ref(null)
const topupTabRef = ref(null)
const voucherSummary = ref({ total: 0, active: 0, expiring: 0, usage: 0 })
const topupSummary = ref({ total: 0, active: 0, expiring: 0, usage: 0 })

const combined = computed(() => ({
  total: voucherSummary.value.total + topupSummary.value.total,
  active: voucherSummary.value.active + topupSummary.value.active,
  expiring: voucherSummary.value.expiring + topupSummary.value.expiring,
  usage: voucherSummary.value.usage + topupSummary.value.usage
}))

const stats = computed(() => [
  { label: 'Tổng chương trình', value: combined.value.total, caption: 'Chương trình', icon: 'redeem', tone: 'teal' },
  { label: 'Đang hoạt động', value: combined.value.active, caption: 'Chương trình', icon: 'check_circle', tone: 'green' },
  { label: 'Sắp hết hạn', value: combined.value.expiring, caption: 'Trong 7 ngày tới', icon: 'schedule', tone: 'amber' },
  { label: 'Tổng lượt sử dụng', value: combined.value.usage.toLocaleString('vi-VN'), caption: 'Lượt đổi voucher', icon: 'group', tone: 'blue' }
])

const activeRef = computed(() => activeTab.value === 'vouchers' ? voucherTabRef.value : topupTabRef.value)
const refreshActive = () => activeRef.value?.refresh?.()
const exportActive = () => activeRef.value?.exportData?.()
const createActive = () => activeRef.value?.startCreate?.()
</script>

<style scoped>
.voucher-page { width: 100%; color: #17202b; }
.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 1.5rem; padding: .35rem .25rem 1.25rem; }
.breadcrumb { display: flex; align-items: center; gap: .55rem; color: #8993a3; font-size: .78rem; margin-bottom: .75rem; }
.breadcrumb strong { color: #334155; font-weight: 700; }
.title-row { display: flex; align-items: flex-start; gap: .8rem; }
.title-icon { color: #07847e; font-size: 2rem; transform: rotate(-8deg); }
.title-row h1 { margin: 0; font-size: clamp(1.55rem, 2vw, 2rem); line-height: 1.05; letter-spacing: -.035em; font-weight: 900; }
.title-row p { margin-top: .45rem; color: #718096; font-size: .85rem; }
.header-actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: .65rem; }
.btn { min-height: 2.65rem; padding: 0 1rem; border-radius: .65rem; display: inline-flex; align-items: center; justify-content: center; gap: .5rem; border: 1px solid transparent; font-size: .82rem; font-weight: 800; transition: transform .2s ease, background-color .2s ease, border-color .2s ease; }
.btn .material-symbols-outlined { font-size: 1.15rem; }
.btn:active { transform: translateY(1px) scale(.985); }
.btn:focus-visible { outline: 3px solid rgba(7, 132, 126, .2); outline-offset: 2px; }
.btn-secondary { color: #075955; border-color: #0c8b85; background: #fff; }
.btn-secondary:hover { background: #f0fdfa; }
.btn-primary { color: #27310c; background: #f7b916; border-color: #f7b916; box-shadow: 0 8px 20px rgba(181, 128, 0, .16); }
.btn-primary:hover { background: #ffc52e; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: .85rem; margin-bottom: 1rem; }
.stat-card { min-height: 6.8rem; display: flex; align-items: center; gap: 1rem; padding: 1.1rem 1.25rem; background: #fff; border: 1px solid #e5e9ee; border-radius: .9rem; box-shadow: 0 8px 22px rgba(29, 47, 61, .045); }
.stat-icon { width: 3.2rem; height: 3.2rem; flex: 0 0 3.2rem; display: grid; place-items: center; border-radius: 50%; }
.stat-icon .material-symbols-outlined { font-size: 1.7rem; }
.stat-icon.teal { color: #fff; background: #07847e; }
.stat-icon.green { color: #059669; background: #dcfce7; }
.stat-icon.amber { color: #ea7200; background: #fff0d9; }
.stat-icon.blue { color: #1d7bd8; background: #e4f0fc; }
.stat-card p { margin: 0 0 .25rem; color: #566273; font-size: .78rem; font-weight: 700; }
.stat-card strong { display: block; color: #111827; font-size: 1.45rem; line-height: 1; font-weight: 900; font-variant-numeric: tabular-nums; }
.stat-card small { display: block; margin-top: .35rem; color: #98a1af; font-size: .68rem; }
.tabs { display: flex; gap: .1rem; border-bottom: 1px solid #dce2e8; margin-bottom: .85rem; }
.tabs button { position: relative; padding: .85rem 1.35rem; color: #7b8492; font-size: .82rem; font-weight: 800; transition: color .2s ease; }
.tabs button::after { content: ''; position: absolute; left: 0; right: 0; bottom: -1px; height: 2px; background: transparent; }
.tabs button:hover, .tabs button.active { color: #087b76; }
.tabs button.active::after { background: #087b76; }
.tabs button:focus-visible { outline: 3px solid rgba(7, 132, 126, .18); outline-offset: -3px; }
@media (max-width: 1100px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .page-header { align-items: flex-start; flex-direction: column; } .header-actions { justify-content: flex-start; } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .stat-card { min-height: auto; } .header-actions { display: grid; grid-template-columns: 1fr 1fr; width: 100%; } .header-actions .btn-primary { grid-column: 1 / -1; } .tabs { overflow-x: auto; } .tabs button { white-space: nowrap; padding-inline: .85rem; } }
</style>
