<template>
  <div class="min-h-screen bg-[#f2f5f8] px-4 py-8 text-slate-800">
    <ExchangeSuccessToast
      :show="successToast.show"
      :title="successToast.title"
      :message="successToast.message"
    />
    <main class="mx-auto max-w-6xl">
      <button type="button" class="mb-5 inline-flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-[#075955]" @click="router.push('/history')">
        <span class="material-symbols-outlined">arrow_back</span>
        Quay lại lịch sử vé
      </button>

      <div v-if="loading" class="rounded-3xl border border-slate-200 bg-white p-16 text-center shadow-sm">
        <span class="material-symbols-outlined animate-spin text-5xl text-[#075955]">progress_activity</span>
        <p class="mt-4 font-bold text-slate-500">Đang kiểm tra điều kiện đổi vé...</p>
      </div>

      <div v-else-if="loadError" class="rounded-3xl border border-rose-200 bg-white p-10 text-center shadow-sm">
        <span class="material-symbols-outlined text-5xl text-rose-500">event_busy</span>
        <h1 class="mt-4 text-2xl font-black">Không thể đổi vé</h1>
        <p class="mx-auto mt-2 max-w-lg text-slate-500">{{ loadError }}</p>
        <button class="mt-6 rounded-xl bg-[#075955] px-6 py-3 font-black text-white" @click="router.push('/history')">Về lịch sử vé</button>
      </div>

      <template v-else-if="options">
        <header class="mb-6 overflow-hidden rounded-3xl bg-[#075955] p-7 text-white shadow-lg">
          <p class="text-xs font-black uppercase tracking-[0.25em] text-emerald-200">Đổi vé #{{ booking.id }}</p>
          <div class="mt-3 flex flex-col justify-between gap-5 md:flex-row md:items-end">
            <div>
              <h1 class="text-3xl font-black">{{ booking.departurePoint }} → {{ booking.arrivalPoint }}</h1>
              <p class="mt-2 font-semibold text-white/75">
                {{ formatDate(booking.departureDate) }} · {{ booking.departureTime }} · Ghế {{ booking.seatNumbers.join(', ') }}
              </p>
            </div>
            <div class="rounded-2xl bg-white/10 px-5 py-3 backdrop-blur-sm">
              <p class="text-[10px] font-black uppercase tracking-widest text-white/60">Số dư ví</p>
              <p class="text-xl font-black">{{ money(walletBalance) }}</p>
            </div>
          </div>
        </header>

        <div class="mb-6 grid grid-cols-2 gap-3 rounded-2xl border border-slate-200 bg-white p-2 shadow-sm">
          <button type="button" :class="modeClass('SEAT')" @click="selectMode('SEAT')">
            <span class="material-symbols-outlined">event_seat</span>
            Đổi ghế cùng chuyến
          </button>
          <button type="button" :class="modeClass('TRIP')" @click="selectMode('TRIP')">
            <span class="material-symbols-outlined">calendar_month</span>
            Đổi ngày / chuyến
          </button>
        </div>

        <div class="grid gap-6 lg:grid-cols-[1fr_340px]">
          <section class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm md:p-8">
            <template v-if="mode === 'TRIP'">
              <div class="mb-7">
                <h2 class="text-xl font-black">1. Chọn ngày và chuyến mới</h2>
                <p class="mt-1 text-sm font-medium text-slate-500">Chỉ hiển thị chuyến cùng tuyến và còn đủ ghế.</p>
              </div>

              <div v-if="options.alternativeTrips.length" class="mb-8 grid gap-3 sm:grid-cols-2">
                <button
                  v-for="trip in options.alternativeTrips"
                  :key="trip.id"
                  type="button"
                  :class="tripClass(trip)"
                  @click="chooseTrip(trip)"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="text-left">
                      <p class="text-base font-black">{{ formatDate(trip.departureDate) }}</p>
                      <p class="mt-1 text-sm font-bold text-slate-500">{{ trip.departureTime }} → {{ trip.arrivalTime || 'Đang cập nhật' }}</p>
                    </div>
                    <span class="rounded-lg bg-emerald-50 px-2 py-1 text-xs font-black text-emerald-700">{{ trip.availableSeats }} ghế</span>
                  </div>
                  <div class="mt-4 flex items-end justify-between border-t border-dashed border-slate-200 pt-3">
                    <span class="text-xs font-bold text-slate-500">{{ trip.busType }}</span>
                    <span class="font-black text-[#f03a17]">{{ money(trip.price) }}/ghế</span>
                  </div>
                </button>
              </div>
              <div v-else class="mb-8 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-8 text-center font-bold text-slate-500">
                Hiện chưa có chuyến thay thế phù hợp trên cùng tuyến.
              </div>

              <div v-if="selectedTrip">
                <h2 class="mb-5 text-xl font-black">2. Chọn {{ booking.seatCount }} ghế mới</h2>
                <div v-if="loadingSeats" class="py-12 text-center text-slate-500">Đang tải sơ đồ ghế...</div>
                <ExchangeSeatPicker v-else v-model="selectedSeats" :seats="displayedSeats" :required-count="booking.seatCount" />
              </div>
            </template>

            <template v-else>
              <div class="mb-7">
                <h2 class="text-xl font-black">Chọn {{ booking.seatCount }} ghế thay thế</h2>
                <p class="mt-1 text-sm font-medium text-slate-500">Ghế hiện tại được đánh dấu màu vàng. Bạn có thể giữ lại một phần ghế.</p>
              </div>
              <ExchangeSeatPicker v-model="selectedSeats" :seats="displayedSeats" :required-count="booking.seatCount" />
            </template>
          </section>

          <aside class="h-fit rounded-3xl border border-slate-200 bg-white p-6 shadow-sm lg:sticky lg:top-24">
            <h2 class="text-lg font-black">Xác nhận thay đổi</h2>
            <div class="mt-5 space-y-4 text-sm">
              <div class="flex justify-between gap-4">
                <span class="font-semibold text-slate-500">Loại thay đổi</span>
                <span class="text-right font-black">{{ mode === 'SEAT' ? 'Đổi ghế' : 'Đổi ngày/chuyến' }}</span>
              </div>
              <div class="flex justify-between gap-4">
                <span class="font-semibold text-slate-500">Ghế mới</span>
                <span class="text-right font-black text-[#075955]">{{ selectedSeats.length ? selectedSeats.join(', ') : 'Chưa chọn' }}</span>
              </div>
              <div v-if="selectionIncomplete" class="rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs font-bold leading-relaxed text-rose-700">
                Bạn đang chọn {{ selectedSeats.length }}/{{ booking.seatCount }} ghế, còn thiếu {{ missingSeatCount }} ghế. Đổi vé không hỗ trợ giảm số lượng; nếu muốn bỏ bớt ghế, cần sử dụng chức năng hủy ghế riêng.
              </div>
              <div v-if="selectedTrip" class="flex justify-between gap-4">
                <span class="font-semibold text-slate-500">Ngày mới</span>
                <span class="text-right font-black">{{ formatDate(selectedTrip.departureDate) }} · {{ selectedTrip.departureTime }}</span>
              </div>
              <div class="border-t border-dashed border-slate-200 pt-4">
                <div class="mb-2 flex justify-between gap-4">
                  <span class="font-semibold text-slate-500">Giá vé hiện tại</span>
                  <span class="font-black text-slate-700">{{ money(booking.totalPrice) }}</span>
                </div>
                <div class="flex justify-between gap-4">
                  <span class="font-semibold text-slate-500">Giá vé mới</span>
                  <span class="font-black">{{ selectionIncomplete ? 'Chọn đủ ghế để tính' : money(calculatedNewPrice) }}</span>
                </div>
                <div v-if="!selectionIncomplete" class="mt-2 flex justify-between gap-4">
                  <span class="font-semibold text-slate-500">Chênh lệch</span>
                  <span :class="['font-black', priceDifference > 0 ? 'text-rose-600' : priceDifference < 0 ? 'text-emerald-600' : 'text-slate-600']">
                    {{ priceDifference > 0 ? '+' : '' }}{{ money(priceDifference) }}
                  </span>
                </div>
              </div>
            </div>

            <div v-if="!selectionIncomplete && priceDifference < 0" class="mt-5 rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
              <div class="flex items-start gap-3">
                <span class="material-symbols-outlined rounded-xl bg-emerald-100 p-2 text-emerald-600">savings</span>
                <div>
                  <p class="text-xs font-black uppercase tracking-widest text-emerald-700">Bạn được hoàn tiền</p>
                  <p class="mt-1 text-2xl font-black text-emerald-700">{{ money(Math.abs(priceDifference)) }}</p>
                  <p class="mt-1 text-xs font-semibold text-emerald-700/80">Hoàn vào Ví Trung – Nam sau khi đổi vé thành công.</p>
                  <div class="mt-3 flex items-center justify-between rounded-xl border border-emerald-200 bg-white/70 px-3 py-2">
                    <span class="text-xs font-bold text-emerald-800">Biến động số dư</span>
                    <strong class="text-base font-black text-emerald-600">+{{ money(Math.abs(priceDifference)) }}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div v-else-if="!selectionIncomplete && priceDifference > 0" class="mt-5 rounded-2xl border border-amber-200 bg-amber-50 p-4">
              <p class="text-xs font-black uppercase tracking-widest text-amber-700">Cần thanh toán thêm</p>
              <p class="mt-1 text-2xl font-black text-amber-700">{{ money(priceDifference) }}</p>
              <p class="mt-1 text-xs font-semibold text-amber-700/80">Chọn Ví hoặc chuyển khoản QR để thanh toán phần chênh lệch.</p>
            </div>

            <div v-else-if="!selectionIncomplete && priceDifference === 0" class="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-xs font-bold text-slate-600">
              Vé mới bằng giá vé hiện tại, bạn không cần thanh toán thêm và không phát sinh hoàn tiền.
            </div>

            <div v-if="priceDifference > 0 && !pendingPayment" class="mt-6">
              <p class="text-xs font-black uppercase tracking-widest text-slate-500">Hình thức thanh toán chênh lệch</p>
              <div class="mt-3 grid gap-2">
                <button
                  v-for="method in paymentMethods"
                  :key="method.id"
                  type="button"
                  :class="paymentMethodClass(method.id)"
                  @click="paymentMethod = method.id"
                >
                  <span class="material-symbols-outlined">{{ method.icon }}</span>
                  <span class="flex-1 text-left">
                    <strong class="block">{{ method.name }}</strong>
                    <small v-if="method.id === 'WALLET'" class="font-bold opacity-70">Số dư: {{ money(walletBalance) }}</small>
                    <small v-else class="font-bold opacity-70">Xác nhận tự động qua SePay</small>
                  </span>
                  <span class="material-symbols-outlined text-lg">{{ paymentMethod === method.id ? 'radio_button_checked' : 'radio_button_unchecked' }}</span>
                </button>
              </div>
            </div>

            <div v-if="pendingPayment" class="mt-6 rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-center">
              <p class="text-xs font-black uppercase tracking-widest text-emerald-700">Quét QR để thanh toán</p>
              <img
                :src="`https://img.vietqr.io/image/mb-0367093771-compact2.png?amount=${pendingPayment.amount}&addInfo=${pendingPayment.paymentCode}&accountName=HUYNH%20DUC%20TIEN`"
                alt="QR thanh toán chênh lệch đổi vé"
                class="mx-auto mt-3 aspect-square w-52 rounded-xl border border-emerald-100 bg-white p-2 shadow-sm"
              />
              <div class="mt-3 rounded-xl bg-white p-3 text-left text-xs">
                <div class="flex justify-between gap-3"><span class="font-bold text-slate-500">Số tiền</span><strong class="text-rose-600">{{ money(pendingPayment.amount) }}</strong></div>
                <div class="mt-2 flex justify-between gap-3"><span class="font-bold text-slate-500">Nội dung</span><strong>{{ pendingPayment.paymentCode }}</strong></div>
              </div>
              <p class="mt-3 flex items-center justify-center gap-2 text-xs font-bold text-emerald-700">
                <span class="material-symbols-outlined animate-spin text-base">progress_activity</span>
                Đang chờ giao dịch · {{ formattedPaymentTime }}
              </p>
            </div>

            <template v-if="!pendingPayment">
              <label class="mt-6 block text-xs font-black uppercase tracking-widest text-slate-500">Lý do đổi vé</label>
              <textarea v-model="reason" rows="3" maxlength="500" class="mt-2 w-full resize-none rounded-xl border border-slate-200 bg-slate-50 p-3 text-sm font-semibold outline-none focus:border-[#075955]" placeholder="Ví dụ: Thay đổi lịch trình cá nhân"></textarea>
            </template>

            <div class="mt-4 rounded-xl bg-amber-50 p-3 text-xs font-semibold leading-relaxed text-amber-800">
              Mỗi vé được đổi một lần và phải còn ít nhất {{ options.minimumHoursBeforeDeparture }} giờ trước khi khởi hành. Có thể thanh toán phần tăng thêm bằng Ví hoặc QR; phần giảm được hoàn vào Ví.
            </div>

            <p v-if="submitError" class="mt-4 rounded-xl bg-rose-50 p-3 text-sm font-bold text-rose-700">{{ submitError }}</p>

            <button v-if="!pendingPayment" type="button" :disabled="!canSubmit || submitting" class="mt-5 flex w-full items-center justify-center gap-2 rounded-2xl bg-[#f03a17] py-4 font-black uppercase tracking-widest text-white shadow-md transition hover:bg-[#d63314] disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-400 disabled:shadow-none" @click="submitExchange">
              <span v-if="submitting" class="material-symbols-outlined animate-spin">progress_activity</span>
              {{ submitting ? 'Đang xử lý' : submitButtonLabel }}
            </button>
          </aside>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTicketExchangeApi } from '@/services/ticketExchangeApi'
import ExchangeSeatPicker from '@/components/booking/exchange/ExchangeSeatPicker.vue'
import ExchangeSuccessToast from '@/components/booking/exchange/ExchangeSuccessToast.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const exchangeApi = useTicketExchangeApi()

const loading = ref(true)
const loadingSeats = ref(false)
const submitting = ref(false)
const loadError = ref('')
const submitError = ref('')
const successToast = ref({ show: false, title: '', message: '' })
const options = ref(null)
const mode = ref('SEAT')
const selectedTrip = ref(null)
const displayedSeats = ref([])
const selectedSeats = ref([])
const reason = ref('')
const walletBalance = ref(0)
const paymentMethod = ref('WALLET')
const pendingPayment = ref(null)
const paymentTimeLeft = ref(0)
let paymentPollingTimer = null
let paymentCountdownTimer = null
let redirectTimer = null
let checkingPayment = false

const paymentMethods = [
  { id: 'WALLET', name: 'Ví Trung - Nam', icon: 'account_balance_wallet' },
  { id: 'QR', name: 'Chuyển khoản QR', icon: 'qr_code_scanner' }
]

const booking = computed(() => options.value?.booking || {})
const calculatedNewPrice = computed(() => {
  if (mode.value === 'SEAT') return Number(booking.value.totalPrice || 0)
  if (!selectedTrip.value) return Number(booking.value.totalPrice || 0)
  return Math.max(0, Number(selectedTrip.value.price || 0) * Number(booking.value.seatCount || 0) - Number(booking.value.discountAmount || 0))
})
const priceDifference = computed(() => calculatedNewPrice.value - Number(booking.value.totalPrice || 0))
const missingSeatCount = computed(() => Math.max(0, Number(booking.value.seatCount || 0) - selectedSeats.value.length))
const selectionIncomplete = computed(() => selectedSeats.value.length !== Number(booking.value.seatCount || 0))
const hasChangedSeats = computed(() => {
  const oldSeats = [...(booking.value.seatNumbers || [])].sort().join(',')
  return [...selectedSeats.value].sort().join(',') !== oldSeats
})
const canSubmit = computed(() => {
  if (pendingPayment.value) return false
  if (!reason.value.trim()) return false
  if (selectedSeats.value.length !== Number(booking.value.seatCount || 0)) return false
  if (mode.value === 'SEAT') return hasChangedSeats.value
  return Boolean(selectedTrip.value)
})
const submitButtonLabel = computed(() => {
  if (priceDifference.value > 0 && paymentMethod.value === 'QR') return 'Tạo mã QR thanh toán'
  if (priceDifference.value > 0) return 'Thanh toán và đổi vé'
  return 'Xác nhận đổi vé'
})
const formattedPaymentTime = computed(() => {
  const minutes = Math.floor(paymentTimeLeft.value / 60).toString().padStart(2, '0')
  const seconds = (paymentTimeLeft.value % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

const getErrorMessage = error => error.response?.data?.error || error.response?.data?.message || error.response?.data || error.message || 'Có lỗi xảy ra.'
const money = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const formatDate = value => {
  if (!value) return ''
  const [year, month, day] = value.split('-')
  return `${day}/${month}/${year}`
}

const modeClass = value => [
  'flex items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-black transition',
  mode.value === value ? 'bg-[#075955] text-white shadow-sm' : 'text-slate-500 hover:bg-slate-50'
]

const tripClass = trip => [
  'rounded-2xl border-2 p-4 text-left transition',
  selectedTrip.value?.id === trip.id
    ? 'border-[#075955] bg-emerald-50/50 shadow-sm'
    : 'border-slate-200 bg-white hover:border-emerald-300'
]

const paymentMethodClass = value => [
  'flex items-center gap-3 rounded-xl border-2 p-3 text-sm transition',
  paymentMethod.value === value
    ? 'border-[#075955] bg-emerald-50 text-[#075955]'
    : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-200'
]

const selectMode = value => {
  mode.value = value
  submitError.value = ''
  selectedTrip.value = null
  if (value === 'SEAT') {
    displayedSeats.value = options.value.currentTripSeats
    selectedSeats.value = [...booking.value.seatNumbers]
  } else {
    displayedSeats.value = []
    selectedSeats.value = []
  }
}

const chooseTrip = async trip => {
  selectedTrip.value = trip
  selectedSeats.value = []
  submitError.value = ''
  loadingSeats.value = true
  try {
    const response = await exchangeApi.getTripSeats(route.params.bookingId, trip.id)
    displayedSeats.value = response.data
  } catch (error) {
    submitError.value = getErrorMessage(error)
    displayedSeats.value = []
  } finally {
    loadingSeats.value = false
  }
}

const loadOptions = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const response = await exchangeApi.getOptions(route.params.bookingId)
    options.value = response.data
    walletBalance.value = Number(response.data.walletBalance || 0)
    mode.value = 'SEAT'
    selectedTrip.value = null
    displayedSeats.value = response.data.currentTripSeats
    selectedSeats.value = [...response.data.booking.seatNumbers]
  } catch (error) {
    loadError.value = getErrorMessage(error)
  } finally {
    loading.value = false
  }
}

const submitExchange = async () => {
  if (!canSubmit.value) return
  submitError.value = ''
  submitting.value = true
  try {
    const response = await exchangeApi.exchange(route.params.bookingId, {
      exchangeType: mode.value,
      newTripId: mode.value === 'SEAT' ? booking.value.tripId : selectedTrip.value.id,
      newSeatNumbers: selectedSeats.value,
      reason: reason.value.trim(),
      paymentMethod: priceDifference.value > 0 ? paymentMethod.value : 'WALLET'
    })
    if (response.data.paymentRequired) {
      pendingPayment.value = response.data
      startPaymentTracking()
      return
    }
    finishExchange(response.data)
  } catch (error) {
    submitError.value = getErrorMessage(error)
  } finally {
    submitting.value = false
  }
}

const finishExchange = data => {
  stopPaymentTracking()
  const difference = Number(data.priceDifference || 0)
  const completedPaymentMethod = data.paymentMethod || paymentMethod.value

  authStore.addNotification({
    type: 'EXCHANGE',
    title: 'Đổi vé thành công',
    message: `Vé <b>#${data.bookingId}</b> đã được cập nhật sang chuyến và vị trí ghế mới.`,
    bookingId: data.bookingId,
    date: new Date().toISOString()
  })

  let walletMovement = 0
  if (difference < 0) walletMovement = Math.abs(difference)
  if (difference > 0 && completedPaymentMethod === 'WALLET') walletMovement = -difference

  if (walletMovement !== 0) {
    authStore.addNotification({
      type: 'WALLET',
      title: `Biến động số dư (${walletMovement > 0 ? '+' : ''}${walletMovement.toLocaleString('vi-VN')}đ)`,
      message: walletMovement > 0
        ? `Ví của bạn được cộng <b>${walletMovement.toLocaleString('vi-VN')}đ</b> từ hoàn chênh lệch đổi vé #${data.bookingId}.`
        : `Ví của bạn bị trừ <b>${Math.abs(walletMovement).toLocaleString('vi-VN')}đ</b> để thanh toán chênh lệch đổi vé #${data.bookingId}.`,
      amount: walletMovement,
      bookingId: data.bookingId,
      date: new Date().toISOString()
    })
  }

  if (data.walletBalance !== undefined) {
    authStore.updateWalletBalance(data.walletBalance)
  }
  successToast.value = {
    show: true,
    title: 'Đổi vé thành công',
    message: 'Vé điện tử mới đã được gửi tới email của bạn. Đang chuyển về lịch sử vé...'
  }
  redirectTimer = setTimeout(() => router.replace('/history'), 2200)
}

const checkPaymentStatus = async () => {
  if (!pendingPayment.value || checkingPayment) return
  checkingPayment = true
  try {
    const response = await exchangeApi.checkPayment(pendingPayment.value.exchangeId)
    if (response.data.paid) {
      finishExchange(response.data)
    } else if (response.data.status === 'EXPIRED') {
      stopPaymentTracking()
      pendingPayment.value = null
      submitError.value = response.data.message || 'Phiên thanh toán đã hết hạn.'
      await loadOptions()
    }
  } catch (error) {
    submitError.value = getErrorMessage(error)
  } finally {
    checkingPayment = false
  }
}

const startPaymentTracking = () => {
  stopPaymentTracking()
  const expiresAt = new Date(pendingPayment.value.expiresAt).getTime()
  const updateCountdown = () => {
    paymentTimeLeft.value = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000))
  }
  updateCountdown()
  paymentCountdownTimer = setInterval(updateCountdown, 1000)
  paymentPollingTimer = setInterval(checkPaymentStatus, 3000)
}

const stopPaymentTracking = () => {
  if (paymentPollingTimer) clearInterval(paymentPollingTimer)
  if (paymentCountdownTimer) clearInterval(paymentCountdownTimer)
  paymentPollingTimer = null
  paymentCountdownTimer = null
}

onUnmounted(() => {
  stopPaymentTracking()
  if (redirectTimer) clearTimeout(redirectTimer)
})

onMounted(loadOptions)
</script>
