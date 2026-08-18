<template>
  <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
    <div class="flex flex-col gap-4 border-b border-slate-100 bg-slate-50/50 p-4 md:flex-row md:items-center md:justify-between">
      <h3 class="flex items-center gap-2 text-sm font-bold text-slate-800">
        <span class="material-symbols-outlined text-[20px] text-[#075955]">swap_horiz</span>
        Lịch sử đổi vé
      </h3>
      <div class="relative w-full md:w-72">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-sm text-slate-400">search</span>
        <input v-model="search" class="w-full rounded-lg border border-slate-200 bg-white py-2 pl-9 pr-4 text-xs outline-none focus:border-[#075955]" placeholder="Tìm mã vé, khách hàng, SĐT..." />
      </div>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full min-w-[1050px] text-left">
        <thead class="border-b border-slate-100 bg-slate-50/50">
          <tr>
            <th class="px-5 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Mã đổi / Vé</th>
            <th class="px-5 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Khách hàng</th>
            <th class="px-5 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Loại đổi</th>
            <th class="px-5 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Thay đổi</th>
            <th class="px-5 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Chênh lệch</th>
            <th class="px-5 py-4 text-center text-[10px] font-black uppercase tracking-widest text-slate-400">Trạng thái</th>
            <th class="px-5 py-4 text-center text-[10px] font-black uppercase tracking-widest text-slate-400">Chi tiết</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="item in paginatedItems" :key="item.id" class="hover:bg-[#075955]/[0.02]">
            <td class="px-5 py-4">
              <p class="font-black text-slate-800">#DX{{ item.id }}</p>
              <p class="mt-1 text-[10px] font-bold text-[#075955]">Vé #{{ item.bookingId }}</p>
              <p class="mt-1 text-[10px] text-slate-400">{{ formatDateTime(item.exchangedAt) }}</p>
            </td>
            <td class="px-5 py-4">
              <p class="text-sm font-bold text-slate-800">{{ item.customerName || 'Khách hàng' }}</p>
              <p class="mt-1 text-[11px] font-semibold text-slate-400">{{ item.customerPhone }}</p>
            </td>
            <td class="px-5 py-4">
              <span :class="['inline-flex rounded-full border px-3 py-1 text-[9px] font-black uppercase tracking-wider', item.exchangeType === 'SEAT' ? 'border-blue-100 bg-blue-50 text-blue-600' : 'border-violet-100 bg-violet-50 text-violet-600']">
                {{ item.exchangeType === 'SEAT' ? 'Đổi ghế' : 'Đổi chuyến' }}
              </span>
            </td>
            <td class="px-5 py-4">
              <template v-if="item.exchangeType === 'SEAT'">
                <p class="text-xs font-bold text-slate-500">{{ item.oldSeatNumbers }} <span class="mx-1 text-[#075955]">→</span> <strong class="text-slate-800">{{ item.newSeatNumbers }}</strong></p>
              </template>
              <template v-else>
                <p class="text-xs font-bold text-slate-500">Chuyến #{{ item.oldTripId }} <span class="mx-1 text-[#075955]">→</span> <strong class="text-slate-800">#{{ item.newTripId }}</strong></p>
                <p class="mt-1 text-[10px] font-semibold text-slate-400">Ghế {{ item.oldSeatNumbers }} → {{ item.newSeatNumbers }}</p>
              </template>
            </td>
            <td class="px-5 py-4">
              <p :class="['font-black', item.priceDifference > 0 ? 'text-rose-600' : item.priceDifference < 0 ? 'text-emerald-600' : 'text-slate-500']">
                {{ item.priceDifference > 0 ? '+' : '' }}{{ money(item.priceDifference) }}
              </p>
              <p class="mt-1 text-[9px] font-black uppercase tracking-widest text-slate-400">{{ paymentLabel(item) }}</p>
              <p class="mt-1 text-[10px] font-semibold text-slate-400">
                Vé cũ {{ money(item.oldPrice) }} → Vé mới {{ money(item.newPrice) }}
              </p>
            </td>
            <td class="px-5 py-4 text-center">
              <span :class="['inline-flex min-w-[115px] justify-center rounded-full border px-3 py-1 text-[9px] font-black uppercase tracking-wider', statusClass(item.status)]">
                {{ statusLabel(item.status) }}
              </span>
            </td>
            <td class="px-5 py-4 text-center">
              <button type="button" class="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-slate-100 bg-slate-50 text-slate-400 transition hover:bg-[#075955] hover:text-white" @click="showDetail(item)">
                <span class="material-symbols-outlined text-lg">visibility</span>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="filteredItems.length === 0" class="py-20 text-center">
      <span class="material-symbols-outlined text-5xl text-slate-300">swap_horiz</span>
      <h4 class="mt-3 font-black uppercase tracking-widest text-slate-400">Chưa có lịch sử đổi vé</h4>
    </div>

    <div v-if="totalPages > 1" class="flex items-center justify-between border-t border-slate-100 bg-slate-50/50 p-4">
      <span class="text-xs font-semibold text-slate-500">{{ filteredItems.length }} lượt đổi vé</span>
      <div class="flex items-center gap-2">
        <button :disabled="page === 1" class="h-8 w-8 rounded border border-slate-200 bg-white disabled:opacity-40" @click="page--">‹</button>
        <span class="text-xs font-bold">{{ page }} / {{ totalPages }}</span>
        <button :disabled="page === totalPages" class="h-8 w-8 rounded border border-slate-200 bg-white disabled:opacity-40" @click="page++">›</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({ exchanges: { type: Array, default: () => [] } })
const search = ref('')
const page = ref(1)
const pageSize = 20

const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase()
  if (!query) return props.exchanges
  return props.exchanges.filter(item => [item.bookingId, item.id, item.customerName, item.customerPhone]
    .some(value => String(value || '').toLowerCase().includes(query)))
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize)))
const paginatedItems = computed(() => filteredItems.value.slice((page.value - 1) * pageSize, page.value * pageSize))
watch([search, () => props.exchanges], () => { page.value = 1 })

const money = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const formatDateTime = value => value ? new Date(value).toLocaleString('vi-VN') : ''
const paymentLabel = item => {
  const difference = Number(item?.priceDifference || 0)
  const method = String(item?.paymentMethod || '').toUpperCase()

  if (difference === 0) return 'Không phát sinh'
  if (difference < 0) return 'Hoàn vào ví nội bộ'
  if (method === 'QR' || method === 'BANK_TRANSFER') return 'Thu thêm qua chuyển khoản QR'
  if (method === 'WALLET') return 'Thu thêm từ ví nội bộ'
  return 'Chưa ghi nhận phương thức thu'
}
const statusLabel = value => ({ COMPLETED: 'Hoàn thành', PENDING_PAYMENT: 'Chờ thanh toán', EXPIRED: 'Hết hạn' }[value] || value)
const statusClass = value => ({
  COMPLETED: 'border-emerald-100 bg-emerald-50 text-emerald-600',
  PENDING_PAYMENT: 'border-amber-100 bg-amber-50 text-amber-600',
  EXPIRED: 'border-slate-200 bg-slate-100 text-slate-500'
}[value] || 'border-slate-200 bg-slate-50 text-slate-500')

const showDetail = item => {
  alert(`CHI TIẾT ĐỔI VÉ #DX${item.id}\n------------------\nMã vé: #${item.bookingId}\nKhách: ${item.customerName}\nLoại: ${item.exchangeType === 'SEAT' ? 'Đổi ghế' : 'Đổi ngày/chuyến'}\nChuyến: #${item.oldTripId} → #${item.newTripId}\nGhế: ${item.oldSeatNumbers} → ${item.newSeatNumbers}\nGiá cũ: ${money(item.oldPrice)}\nGiá mới: ${money(item.newPrice)}\nChênh lệch: ${money(item.priceDifference)}\nThanh toán: ${paymentLabel(item)}\nTrạng thái: ${statusLabel(item.status)}\nLý do: ${item.reason || 'Không có'}`)
}
</script>
