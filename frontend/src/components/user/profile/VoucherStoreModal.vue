<template>
  <div
    v-if="show"
    class="voucher-overlay fixed inset-0 z-[9999] flex items-center justify-center p-3 sm:p-6"
    role="dialog"
    aria-modal="true"
    aria-labelledby="voucher-store-title"
    @click.self="$emit('close')"
  >
    <section class="voucher-store flex max-h-[92dvh] w-full max-w-5xl flex-col overflow-hidden rounded-[28px] bg-[#f7faf9] shadow-2xl">
      <header class="store-header relative overflow-hidden px-5 py-4 text-white sm:px-7 sm:py-5">
        <div class="store-pattern" aria-hidden="true"></div>
        <div class="relative flex items-center justify-between gap-4">
          <div class="min-w-0">
            <p class="mb-1 flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.08em] text-emerald-100/80">
              <span class="material-symbols-outlined text-[15px]">workspace_premium</span>
              Đặc quyền thành viên
            </p>
            <h2 id="voucher-store-title" class="text-xl font-black tracking-[-0.035em] sm:text-2xl">Đổi điểm lấy ưu đãi</h2>
            <p class="mt-1 text-xs font-medium text-emerald-50/70">5.000đ thanh toán thực tế = 1 điểm</p>
          </div>
          <div class="header-actions">
            <div class="compact-balance">
              <span class="material-symbols-outlined">stars</span>
              <div><small>Điểm hiện có</small><strong>{{ formatNumber(pointsBalance) }}</strong></div>
            </div>
            <div class="compact-affordable"><strong>{{ affordableCount }}</strong><span>voucher đổi ngay</span></div>
            <button type="button" class="close-button" aria-label="Đóng cửa hàng đổi điểm" @click="$emit('close')">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
        </div>
      </header>

      <div class="flex min-h-0 flex-1 flex-col">
        <div class="border-b border-emerald-950/8 bg-white px-5 py-3 sm:px-7">
          <div class="toolbar-row">
            <label class="search-box">
              <span class="material-symbols-outlined">search</span>
              <input v-model.trim="searchTerm" type="search" placeholder="Tìm theo mã hoặc giá trị giảm" aria-label="Tìm voucher" />
              <button v-if="searchTerm" type="button" aria-label="Xóa từ khóa" @click="searchTerm = ''">
                <span class="material-symbols-outlined">close</span>
              </button>
            </label>
            <div class="filter-tabs" role="group" aria-label="Lọc voucher">
              <button v-for="item in filters" :key="item.value" type="button" :class="{ active: activeFilter === item.value }" @click="activeFilter = item.value">
                {{ item.label }}
              </button>
            </div>
            <button type="button" class="guide-compact" :aria-expanded="showGuide" aria-controls="points-guide-content" @click="showGuide = !showGuide">
              <span class="material-symbols-outlined">help</span>
              <span>Hướng dẫn</span>
              <span class="material-symbols-outlined chevron" :class="{ 'rotate-180': showGuide }">expand_more</span>
            </button>
          </div>

          <aside v-show="showGuide" id="points-guide-content" class="points-guide mt-3" aria-label="Hướng dẫn tích và sử dụng điểm">
            <div class="guide-content">
              <div class="guide-step">
                <span>1</span>
                <div><strong>Tích điểm sau thanh toán</strong><p>Mỗi 5.000đ thực trả được 1 điểm. Hệ thống tính trên số tiền sau khi áp dụng voucher.</p></div>
              </div>
              <div class="guide-step">
                <span>2</span>
                <div><strong>Điểm vào đúng tài khoản</strong><p>Đăng nhập khi đặt vé. Điểm được cộng khi vé đã thanh toán hoặc khách đã lên xe.</p></div>
              </div>
              <div class="guide-step">
                <span>3</span>
                <div><strong>Đổi và sử dụng voucher</strong><p>Chọn ưu đãi, xác nhận đổi rồi dùng mã trong ví voucher khi thanh toán vé.</p></div>
              </div>
              <div class="guide-step">
                <span>4</span>
                <div><strong>Khi hủy hoặc đổi vé</strong><p>Hủy vé sẽ trừ lại điểm đã nhận. Đổi vé sẽ điều chỉnh điểm theo giá vé mới.</p></div>
              </div>
            </div>
          </aside>
        </div>

        <div v-if="feedback?.message" class="mx-5 mt-4 flex items-start gap-3 rounded-xl px-4 py-3 text-sm font-semibold sm:mx-8" :class="feedback.type === 'success' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-50 text-rose-700'" role="status">
          <span class="material-symbols-outlined text-[20px]">{{ feedback.type === 'success' ? 'check_circle' : 'error' }}</span>
          <span>{{ feedback.message }}</span>
        </div>

        <div class="custom-scrollbar min-h-0 flex-1 overflow-y-auto px-5 pb-7 pt-5 sm:px-8">
          <div v-if="loading" class="grid gap-4 md:grid-cols-2">
            <div v-for="index in 4" :key="index" class="voucher-skeleton animate-pulse">
              <div class="h-4 w-24 rounded bg-slate-200"></div>
              <div class="mt-4 h-8 w-36 rounded bg-slate-200"></div>
              <div class="mt-7 h-10 rounded bg-slate-100"></div>
            </div>
          </div>

          <div v-else-if="filteredVouchers.length === 0" class="empty-state">
            <span class="material-symbols-outlined">confirmation_number</span>
            <h3>{{ availableVouchers.length ? 'Không tìm thấy voucher phù hợp' : 'Chưa có voucher để đổi' }}</h3>
            <p>{{ availableVouchers.length ? 'Thử từ khóa khác hoặc chọn bộ lọc “Tất cả”.' : 'Các ưu đãi mới sẽ xuất hiện tại đây khi được phát hành.' }}</p>
            <button v-if="availableVouchers.length" type="button" @click="resetFilters">Xem tất cả voucher</button>
          </div>

          <div v-else class="grid items-start gap-4 md:grid-cols-2">
            <article v-for="voucher in filteredVouchers" :key="voucher.id" class="voucher-card" :class="{ unavailable: !canRedeem(voucher) }">
              <div class="voucher-card-top">
                <div>
                  <span class="voucher-code">{{ voucher.code }}</span>
                  <p class="mt-3 text-sm font-medium text-slate-500">Giảm trực tiếp</p>
                  <strong class="discount-value">{{ formatMoney(voucher.discountAmount) }}</strong>
                </div>
                <div class="ticket-mark" aria-hidden="true"><span class="material-symbols-outlined">local_activity</span></div>
              </div>

              <div class="mt-5 flex flex-wrap gap-x-4 gap-y-2 text-xs font-medium text-slate-500">
                <span class="flex items-center gap-1.5"><span class="material-symbols-outlined text-[16px] text-[#087269]">directions_bus</span>Áp dụng khi đặt vé</span>
                <span class="flex items-center gap-1.5"><span class="material-symbols-outlined text-[16px] text-[#087269]">counter_1</span>Dùng một lần</span>
              </div>

              <div class="points-progress mt-5">
                <div class="mb-2 flex justify-between gap-3 text-xs font-semibold">
                  <span class="text-slate-500">Cần {{ formatNumber(voucher.pointsCost) }} điểm</span>
                  <span :class="canRedeem(voucher) ? 'text-emerald-700' : 'text-amber-700'">{{ redeemStatus(voucher) }}</span>
                </div>
                <div class="h-1.5 overflow-hidden rounded-full bg-slate-100">
                  <div class="h-full rounded-full" :class="canRedeem(voucher) ? 'bg-emerald-600' : 'bg-amber-400'" :style="{ width: `${progressPercent(voucher)}%` }"></div>
                </div>
              </div>

              <div class="mt-5 flex items-end justify-between gap-4 border-t border-slate-100 pt-4">
                <div>
                  <p class="text-[11px] font-semibold text-slate-400">Giá trị mỗi 100 điểm</p>
                  <p class="mt-0.5 text-sm font-bold tabular-nums text-slate-700">{{ formatMoney(valuePerHundredPoints(voucher)) }}</p>
                </div>
                <button type="button" class="redeem-button" :disabled="redeeming || !canRedeem(voucher)" @click="selectedVoucher = voucher">
                  <span>{{ canRedeem(voucher) ? 'Chọn đổi' : 'Chưa đủ điểm' }}</span>
                  <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
                </button>
              </div>
            </article>
          </div>
        </div>
      </div>

      <div v-if="selectedVoucher" class="confirm-overlay absolute inset-0 z-10 flex items-end justify-center p-3 sm:items-center sm:p-6" @click.self="selectedVoucher = null">
        <div class="confirm-card w-full max-w-md rounded-[24px] bg-white p-5 shadow-2xl sm:p-6">
          <div class="flex items-start justify-between gap-4">
            <div class="grid h-12 w-12 place-items-center rounded-2xl bg-amber-100 text-amber-700">
              <span class="material-symbols-outlined">redeem</span>
            </div>
            <button type="button" class="rounded-full p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" aria-label="Đóng xác nhận" @click="selectedVoucher = null">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          <h3 class="mt-5 text-xl font-black tracking-tight text-slate-900">Xác nhận đổi {{ selectedVoucher.code }}</h3>
          <p class="mt-2 text-sm leading-6 text-slate-500">Voucher giảm <b class="text-slate-800">{{ formatMoney(selectedVoucher.discountAmount) }}</b> sẽ được thêm vào ví của bạn ngay sau khi đổi.</p>
          <dl class="mt-5 overflow-hidden rounded-2xl bg-[#f4f8f7] text-sm">
            <div class="flex justify-between px-4 py-3"><dt class="text-slate-500">Điểm hiện có</dt><dd class="font-bold tabular-nums">{{ formatNumber(pointsBalance) }}</dd></div>
            <div class="flex justify-between border-t border-emerald-950/5 px-4 py-3"><dt class="text-slate-500">Điểm sử dụng</dt><dd class="font-bold tabular-nums text-rose-600">− {{ formatNumber(selectedVoucher.pointsCost) }}</dd></div>
            <div class="flex justify-between border-t border-emerald-950/5 px-4 py-3"><dt class="text-slate-700">Còn lại sau khi đổi</dt><dd class="font-black tabular-nums text-emerald-700">{{ formatNumber(pointsBalance - selectedVoucher.pointsCost) }}</dd></div>
          </dl>
          <p class="mt-4 flex gap-2 text-xs leading-5 text-slate-500"><span class="material-symbols-outlined text-[17px]">info</span>Điểm đã dùng để đổi voucher không thể hoàn lại.</p>
          <div class="mt-5 grid grid-cols-2 gap-3">
            <button type="button" class="cancel-button" :disabled="redeeming" @click="selectedVoucher = null">Quay lại</button>
            <button type="button" class="confirm-button" :disabled="redeeming" @click="confirmRedeem">
              <span v-if="redeeming" class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
              {{ redeeming ? 'Đang xử lý' : 'Xác nhận đổi' }}
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';

const props = defineProps({
  show: Boolean,
  user: { type: Object, required: true },
  availableVouchers: { type: Array, default: () => [] },
  loading: Boolean,
  redeeming: Boolean,
  feedback: { type: Object, default: null }
});

const emit = defineEmits(['close', 'redeem']);
const searchTerm = ref('');
const activeFilter = ref('all');
const selectedVoucher = ref(null);
const showGuide = ref(false);
const filters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'affordable', label: 'Đổi được ngay' },
  { value: 'best-value', label: 'Lợi nhất' }
];

const pointsBalance = computed(() => Number(props.user?.loyaltyPoints || 0));
const canRedeem = (voucher) => pointsBalance.value >= Number(voucher.pointsCost || 0);
const affordableCount = computed(() => props.availableVouchers.filter(canRedeem).length);
const maxAffordableDiscount = computed(() => Math.max(0, ...props.availableVouchers.filter(canRedeem).map((voucher) => Number(voucher.discountAmount || 0))));

const valuePerHundredPoints = (voucher) => Number(voucher.pointsCost) > 0 ? (Number(voucher.discountAmount || 0) / Number(voucher.pointsCost)) * 100 : 0;
const progressPercent = (voucher) => Math.min(100, Math.round((pointsBalance.value / Math.max(1, Number(voucher.pointsCost))) * 100));
const redeemStatus = (voucher) => canRedeem(voucher) ? 'Đủ điểm' : `Thiếu ${formatNumber(Number(voucher.pointsCost) - pointsBalance.value)}`;
const formatNumber = (value) => Number(value || 0).toLocaleString('vi-VN');
const formatMoney = (value) => `${formatNumber(Math.round(Number(value || 0)))}đ`;

const filteredVouchers = computed(() => {
  const keyword = searchTerm.value.toLocaleLowerCase('vi-VN');
  let vouchers = props.availableVouchers.filter((voucher) => {
    const searchable = `${voucher.code} ${voucher.discountAmount} ${formatMoney(voucher.discountAmount)}`.toLocaleLowerCase('vi-VN');
    return !keyword || searchable.includes(keyword);
  });
  if (activeFilter.value === 'affordable') vouchers = vouchers.filter(canRedeem);
  if (activeFilter.value === 'best-value') vouchers = [...vouchers].sort((a, b) => valuePerHundredPoints(b) - valuePerHundredPoints(a));
  else vouchers = [...vouchers].sort((a, b) => Number(a.pointsCost) - Number(b.pointsCost));
  return vouchers;
});

const resetFilters = () => {
  searchTerm.value = '';
  activeFilter.value = 'all';
};

const confirmRedeem = () => emit('redeem', selectedVoucher.value);

watch(() => props.feedback, (feedback) => {
  if (feedback?.type === 'success') selectedVoucher.value = null;
});

watch(() => props.show, (show) => {
  if (!show) {
    selectedVoucher.value = null;
    resetFilters();
  }
});
</script>

<style scoped>
.voucher-overlay { background: rgba(8, 28, 27, .68); backdrop-filter: blur(9px); }
.voucher-store { position: relative; box-shadow: 0 30px 90px rgba(0, 49, 46, .32); }
.store-header { background: radial-gradient(circle at 82% 0, rgba(30, 143, 127, .8), transparent 38%), #034d48; }
.store-pattern { position: absolute; inset: 0; opacity: .17; background-image: radial-gradient(circle at center, rgba(255,255,255,.7) 1px, transparent 1px); background-size: 18px 18px; mask-image: linear-gradient(90deg, transparent 20%, #000); }
.close-button { display: grid; width: 2.5rem; height: 2.5rem; flex: none; place-items: center; border: 1px solid rgba(255,255,255,.18); border-radius: 999px; color: rgba(255,255,255,.8); background: rgba(255,255,255,.08); transition: .2s ease; }
.close-button:hover { color: #fff; background: rgba(255,255,255,.16); transform: rotate(4deg); }
.close-button:focus-visible, button:focus-visible, input:focus-visible { outline: 3px solid rgba(241, 190, 62, .55); outline-offset: 2px; }
.header-actions { display: flex; flex: none; align-items: center; gap: .65rem; }
.compact-balance { display: flex; min-width: 8.5rem; align-items: center; gap: .55rem; border: 1px solid rgba(255,255,255,.14); border-radius: 13px; padding: .55rem .7rem; background: rgba(255,255,255,.09); }
.compact-balance > span { color: #ffe29a; font-size: 1.25rem; }
.compact-balance div { display: flex; flex-direction: column; }
.compact-balance small { color: rgba(236,255,251,.64); font-size: .58rem; font-weight: 650; }
.compact-balance strong { color: #fff; font-size: .9rem; font-weight: 900; font-variant-numeric: tabular-nums; line-height: 1.15; }
.compact-affordable { display: flex; align-items: baseline; gap: .35rem; color: rgba(236,255,251,.72); font-size: .62rem; font-weight: 650; }
.compact-affordable strong { color: #ffe29a; font-size: .9rem; font-variant-numeric: tabular-nums; }
.toolbar-row { display: grid; grid-template-columns: minmax(13rem, 1fr) auto auto; align-items: center; gap: .7rem; }
.guide-compact { display: flex; min-height: 2.65rem; align-items: center; gap: .35rem; border: 1px solid #dbe6e3; border-radius: 11px; padding: 0 .7rem; color: #526965; background: #f7faf9; font-size: .68rem; font-weight: 800; transition: .2s ease; }
.guide-compact:hover { border-color: #a9cbc6; color: #075955; background: #eef7f5; }
.guide-compact > span:first-child { color: #087269; font-size: 1rem; }
.guide-compact .chevron { font-size: 1rem; transition: transform .2s ease; }
.points-balance, .summary-stat { border: 1px solid rgba(255,255,255,.14); background: rgba(255,255,255,.09); backdrop-filter: blur(8px); }
.points-balance { display: flex; align-items: center; gap: .9rem; padding: 1rem 1.1rem; border-radius: 18px; box-shadow: inset 0 1px rgba(255,255,255,.08); }
.points-balance > span { display: grid; width: 2.5rem; height: 2.5rem; place-items: center; border-radius: 13px; color: #ffe29a; background: rgba(255,210,96,.14); }
.points-balance p, .summary-stat span { color: rgba(236,255,251,.7); font-size: 11px; font-weight: 600; }
.points-balance strong { display: block; margin-top: .1rem; color: #fff; font-size: 1.45rem; font-variant-numeric: tabular-nums; }
.points-balance small { color: #b8ddd8; font-size: .7rem; }
.summary-stat { display: flex; flex-direction: column; justify-content: center; padding: .85rem 1rem; border-radius: 16px; }
.summary-stat strong { margin-top: .2rem; color: #fff; font-size: 1.05rem; font-variant-numeric: tabular-nums; }
.summary-stat small { margin-top: .1rem; color: rgba(236,255,251,.55); font-size: 10px; }
.search-box { display: flex; width: 100%; max-width: 25rem; align-items: center; gap: .65rem; padding: .7rem .85rem; border: 1px solid #dae5e2; border-radius: 13px; color: #6c7d79; background: #f7faf9; transition: .2s ease; }
.search-box:focus-within { border-color: #5a9992; background: #fff; box-shadow: 0 0 0 3px rgba(8,114,105,.09); }
.search-box > span { font-size: 20px; }
.search-box input { min-width: 0; flex: 1; color: #203432; background: transparent; font-size: 13px; font-weight: 600; outline: none; }
.search-box input::placeholder { color: #98a6a3; }
.search-box button { display: grid; place-items: center; color: #82908e; }
.search-box button span { font-size: 17px; }
.filter-tabs { display: flex; gap: .25rem; padding: .25rem; border-radius: 12px; background: #eff4f3; }
.filter-tabs button { padding: .58rem .8rem; border-radius: 9px; color: #647572; font-size: 11px; font-weight: 700; transition: .2s ease; }
.filter-tabs button:hover { color: #075955; }
.filter-tabs button.active { color: #075955; background: #fff; box-shadow: 0 3px 10px rgba(0,63,59,.08); }
.points-guide { overflow: hidden; border: 1px solid #d9e9e6; border-radius: 14px; background: #f3f9f7; }
.guide-toggle { display: flex; width: 100%; align-items: center; gap: .7rem; padding: .7rem .85rem; color: #365653; transition: background .2s ease; }
.guide-toggle:hover { background: #eaf5f2; }
.guide-toggle-icon { display: grid; width: 2rem; height: 2rem; flex: none; place-items: center; border-radius: 10px; color: #087269; background: #dff1ed; font-size: 19px; }
.guide-toggle strong { display: block; color: #173b37; font-size: 12px; font-weight: 800; }
.guide-toggle small { display: block; margin-top: .08rem; color: #6d817e; font-size: 10px; font-weight: 600; }
.guide-content { display: grid; gap: .55rem; padding: 0 .75rem .75rem; border-top: 1px solid #dcebe8; animation: guide-in .2s ease; grid-template-columns: repeat(4, minmax(0, 1fr)); }
.guide-step { display: flex; gap: .55rem; padding: .75rem .6rem .15rem; }
.guide-step > span { display: grid; width: 1.35rem; height: 1.35rem; flex: none; place-items: center; border-radius: 7px; color: #fff; background: #087269; font-size: 9px; font-weight: 800; }
.guide-step strong { display: block; color: #28433f; font-size: 10px; font-weight: 800; line-height: 1.35; }
.guide-step p { margin-top: .25rem; color: #71827f; font-size: 9px; font-weight: 500; line-height: 1.45; }
.voucher-card, .voucher-skeleton { padding: 1.25rem; border: 1px solid #deE8e6; border-radius: 20px; background: #fff; }
.voucher-card { position: relative; overflow: hidden; box-shadow: 0 8px 24px rgba(10,65,60,.055); transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease; }
.voucher-card::before, .voucher-card::after { position: absolute; top: 53%; width: 18px; height: 18px; border-radius: 50%; background: #f7faf9; content: ''; }
.voucher-card::before { left: -10px; box-shadow: inset -1px 0 #dee8e6; }
.voucher-card::after { right: -10px; box-shadow: inset 1px 0 #dee8e6; }
.voucher-card:not(.unavailable):hover { border-color: #9bcac4; box-shadow: 0 15px 32px rgba(7,89,85,.11); transform: translateY(-2px); }
.voucher-card.unavailable { background: #fbfcfc; }
.voucher-card-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; }
.voucher-code { display: inline-block; padding: .35rem .55rem; border-radius: 7px; color: #087269; background: #e9f7f4; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .83rem; font-weight: 800; letter-spacing: .03em; }
.discount-value { display: block; color: #16312e; font-size: 1.7rem; line-height: 1.1; letter-spacing: -.04em; font-variant-numeric: tabular-nums; }
.ticket-mark { display: grid; width: 2.7rem; height: 2.7rem; flex: none; place-items: center; border-radius: 14px; color: #bd8211; background: #fff4d8; }
.points-progress div div { transition: width .45s cubic-bezier(.16, 1, .3, 1); }
.redeem-button { display: flex; align-items: center; gap: .35rem; padding: .65rem .85rem; border-radius: 11px; color: #fff; background: #087269; font-size: 12px; font-weight: 800; transition: .2s ease; }
.redeem-button:not(:disabled):hover { background: #055d56; transform: translateY(-1px); }
.redeem-button:not(:disabled):active { transform: translateY(1px) scale(.98); }
.redeem-button:disabled { cursor: not-allowed; color: #94a19f; background: #e9eeed; }
.empty-state { display: flex; min-height: 18rem; flex-direction: column; align-items: center; justify-content: center; text-align: center; }
.empty-state > span { display: grid; width: 3.5rem; height: 3.5rem; place-items: center; border-radius: 18px; color: #087269; background: #e5f4f1; font-size: 28px; }
.empty-state h3 { margin-top: 1rem; color: #1d302e; font-size: 1rem; font-weight: 800; }
.empty-state p { max-width: 24rem; margin-top: .4rem; color: #7d8b88; font-size: .82rem; line-height: 1.5; }
.empty-state button { margin-top: 1rem; color: #087269; font-size: .78rem; font-weight: 800; }
.confirm-overlay { background: rgba(7, 33, 31, .58); backdrop-filter: blur(5px); }
.confirm-card { animation: confirm-in .22s cubic-bezier(.16, 1, .3, 1); }
.cancel-button, .confirm-button { display: flex; min-height: 2.8rem; align-items: center; justify-content: center; gap: .4rem; border-radius: 12px; font-size: 13px; font-weight: 800; transition: .2s ease; }
.cancel-button { border: 1px solid #dbe4e2; color: #536461; background: #fff; }
.cancel-button:hover { background: #f3f6f5; }
.confirm-button { color: #fff; background: #087269; box-shadow: 0 8px 18px rgba(8,114,105,.22); }
.confirm-button:hover { background: #055d56; transform: translateY(-1px); }
.cancel-button:disabled, .confirm-button:disabled { cursor: wait; opacity: .65; }
.custom-scrollbar { scrollbar-width: thin; scrollbar-color: #aac3bf transparent; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { border-radius: 99px; background: #aac3bf; }
@keyframes confirm-in { from { opacity: 0; transform: translateY(14px) scale(.98); } to { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes guide-in { from { opacity: 0; transform: translateY(-4px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width: 900px) {
  .toolbar-row { grid-template-columns: 1fr auto; }
  .search-box { grid-column: 1 / -1; max-width: none; }
  .guide-content { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 639px) {
  .voucher-store { max-height: 96dvh; border-radius: 22px; }
  .store-header { padding-bottom: 1.15rem; }
  .summary-stat { display: none; }
  .compact-affordable { display: none; }
  .compact-balance { min-width: 0; }
  .compact-balance small { display: none; }
  .toolbar-row { display: flex; align-items: stretch; flex-direction: column; }
  .filter-tabs { width: 100%; overflow-x: auto; }
  .filter-tabs button { flex: 1; white-space: nowrap; }
  .search-box { max-width: none; }
  .guide-compact { justify-content: center; }
  .guide-content { grid-template-columns: 1fr; }
}
</style>
