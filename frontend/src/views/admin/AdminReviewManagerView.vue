<template>
  <main class="review-manager">
    <header class="page-header">
      <div class="title-block">
        <span class="title-icon material-symbols-outlined">reviews</span>
        <div>
          <h1>Quản lý đánh giá</h1>
          <p>Xem, phản hồi và theo dõi trải nghiệm của hành khách.</p>
        </div>
      </div>
      <div class="header-actions">
        <button type="button" :disabled="loading" @click="fetchReviews">
          <span class="material-symbols-outlined" :class="{ spin: loading }">refresh</span>Làm mới
        </button>
        <button type="button" @click="exportCsv">
          <span class="material-symbols-outlined text-emerald-700">table_view</span>Xuất Excel
        </button>
      </div>
    </header>

    <section class="overview-grid" aria-label="Thống kê đánh giá">
      <article class="metric-card metric-blue">
        <span class="metric-icon material-symbols-outlined">group</span>
        <div><small>Tổng đánh giá</small><strong>{{ reviews.length.toLocaleString('vi-VN') }}</strong><p>Dữ liệu từ hành khách đã đặt vé</p></div>
      </article>
      <article class="metric-card metric-green">
        <span class="metric-icon material-symbols-outlined">forum</span>
        <div><small>Đã phản hồi</small><strong>{{ repliedCount.toLocaleString('vi-VN') }}</strong><p>Tỷ lệ phản hồi {{ responseRate }}%</p></div>
      </article>
      <article class="metric-card metric-amber">
        <span class="metric-icon material-symbols-outlined filled">star</span>
        <div><small>Điểm trung bình</small><strong>{{ averageRating }}<em>/ 5</em></strong><p class="mini-stars">★★★★★</p></div>
      </article>

      <article class="rating-distribution">
        <div class="distribution-bars">
          <h2>Phân bố số sao</h2>
          <div v-for="star in [5,4,3,2,1]" :key="star" class="bar-row">
            <span>{{ star }} sao</span>
            <div><i :class="`bar-${star}`" :style="{ width: ratingPercent(star) + '%' }"></i></div>
            <b>{{ ratingCounts[star] }}</b><small>{{ ratingPercent(star) }}%</small>
          </div>
        </div>
        <div class="sentiment-panel">
          <h2>Chỉ số hài lòng</h2>
          <div class="sentiment-content">
            <div class="donut" :style="donutStyle"><strong>{{ satisfactionRate }}%</strong><span>Hài lòng</span></div>
            <dl>
              <div><dt><i class="dot positive"></i>Tích cực</dt><dd>{{ positiveRate }}%</dd></div>
              <div><dt><i class="dot neutral"></i>Trung lập</dt><dd>{{ neutralRate }}%</dd></div>
              <div><dt><i class="dot negative"></i>Tiêu cực</dt><dd>{{ negativeRate }}%</dd></div>
            </dl>
          </div>
        </div>
      </article>
    </section>

    <section class="filter-panel" aria-label="Bộ lọc đánh giá">
      <label class="search-field"><span>Tìm kiếm</span><div><input v-model="search" type="search" placeholder="Tên khách, mã vé hoặc nội dung..."/><span class="material-symbols-outlined">search</span></div></label>
      <label><span>Dòng xe</span><select v-model="selectedBusType"><option value="ALL">Tất cả dòng xe</option><option v-for="type in uniqueBusTypes" :key="type" :value="type">{{ type }}</option></select></label>
      <label><span>Trạng thái</span><select v-model="replyStatus"><option value="ALL">Tất cả</option><option value="UNREPLIED">Chưa phản hồi</option><option value="REPLIED">Đã phản hồi</option></select></label>
      <label><span>Mức sao</span><select v-model="ratingFilter"><option value="ALL">Tất cả sao</option><option v-for="star in [5,4,3,2,1]" :key="star" :value="String(star)">{{ star }} sao</option></select></label>
      <label><span>Từ ngày</span><input v-model="dateFrom" type="date" /></label>
      <label><span>Đến ngày</span><input v-model="dateTo" type="date" /></label>
      <button class="clear-filter" type="button" @click="clearFilters"><span class="material-symbols-outlined">filter_alt_off</span>Xóa lọc</button>
    </section>

    <nav class="view-tabs" aria-label="Nhóm đánh giá">
      <button v-for="tab in tabs" :key="tab.value" type="button" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value">
        {{ tab.label }} <span>{{ tabCount(tab.value) }}</span>
      </button>
      <label class="sort-control">Sắp xếp:<select v-model="sortOrder"><option value="NEWEST">Mới nhất</option><option value="OLDEST">Cũ nhất</option><option value="HIGHEST">Điểm cao nhất</option><option value="LOWEST">Điểm thấp nhất</option></select></label>
    </nav>

    <div class="content-grid">
      <section class="reviews-panel">
        <div v-if="loading" class="skeleton-list"><div v-for="item in 5" :key="item"></div></div>
        <div v-else-if="loadError" class="state-box error"><span class="material-symbols-outlined">cloud_off</span><strong>Không tải được đánh giá</strong><p>{{ loadError }}</p><button @click="fetchReviews">Thử lại</button></div>
        <div v-else-if="!paginatedReviews.length" class="state-box"><span class="material-symbols-outlined">speaker_notes_off</span><strong>Không có đánh giá phù hợp</strong><p>Thử thay đổi từ khóa hoặc bộ lọc.</p></div>

        <article v-for="review in paginatedReviews" :key="review.id" class="review-row" :class="{ complaint: review.rating <= 2 }">
          <div class="customer-column">
            <img :src="avatar(review)" :alt="`Ảnh đại diện ${review.user?.fullName || 'khách hàng'}`" />
            <div>
              <div class="customer-name"><strong>{{ review.user?.fullName || 'Khách hàng' }}</strong><span v-if="review.rating === 5">Nổi bật</span><span v-if="review.rating <= 2" class="danger">Cần xử lý</span></div>
              <p>{{ formatDateTime(review.createdAt) }}</p>
              <div class="stars"><span v-for="star in 5" :key="star" :class="{ muted: star > review.rating }">★</span><b>{{ Number(review.rating).toFixed(1) }}</b></div>
              <small>Vé #{{ review.booking?.id || '—' }}</small>
            </div>
          </div>

          <div class="review-body">
            <div class="review-tags"><span>{{ review.busType || 'Chưa xác định dòng xe' }}</span><span>{{ routeLabel(review) }}</span></div>
            <blockquote>{{ review.comment || 'Khách hàng không để lại nội dung.' }}</blockquote>
            <div v-if="review.adminReply" class="reply-preview"><span class="material-symbols-outlined">verified_user</span><div><strong>{{ review.repliedBy?.fullName || 'Admin nhà xe' }}</strong><p>{{ review.adminReply }}</p></div></div>
          </div>

          <div class="review-actions">
            <span class="reply-status" :class="review.adminReply ? 'done' : 'pending'"><i></i>{{ review.adminReply ? 'Đã phản hồi' : 'Chưa phản hồi' }}</span>
            <button v-if="!review.adminReply" class="primary" type="button" @click="openReply(review)"><span class="material-symbols-outlined">reply</span>Phản hồi</button>
            <button v-else type="button" @click="openReply(review)"><span class="material-symbols-outlined">edit_note</span>Sửa phản hồi</button>
            <button class="danger-button" type="button" title="Xóa đánh giá" @click="reviewToDelete = review"><span class="material-symbols-outlined">delete</span></button>
          </div>
        </article>

        <footer v-if="filteredReviews.length" class="pagination">
          <p>Hiển thị {{ pageStart }}–{{ pageEnd }} trong {{ filteredReviews.length }} đánh giá</p>
          <div><button :disabled="page === 1" @click="page--">‹</button><span>{{ page }} / {{ totalPages }}</span><button :disabled="page === totalPages" @click="page++">›</button></div>
        </footer>
      </section>

      <aside class="insight-sidebar">
        <section class="response-card">
          <small>Tỷ lệ phản hồi</small><div><strong>{{ responseRate }}%</strong><span>{{ unrepliedCount ? `${unrepliedCount} đánh giá đang chờ` : 'Đã phản hồi tất cả' }}</span></div><progress :value="responseRate" max="100"></progress>
        </section>
        <section class="templates-card">
          <header><div><small>Hỗ trợ trả lời</small><h2>Mẫu phản hồi nhanh</h2></div><span class="material-symbols-outlined">quick_phrases</span></header>
          <button v-for="template in replyTemplates" :key="template.title" type="button" @click="useTemplate(template.content)"><span class="material-symbols-outlined">chat_bubble</span><div><strong>{{ template.title }}</strong><p>{{ template.content }}</p></div></button>
          <p class="template-hint">Chọn một đánh giá trước, sau đó bấm mẫu để điền nhanh nội dung.</p>
        </section>
      </aside>
    </div>

    <Teleport to="body">
      <div v-if="selectedReview" class="modal-overlay" @click.self="closeReply">
        <form class="reply-modal" @submit.prevent="submitReply">
          <header><span class="material-symbols-outlined">reply</span><div><small>Phản hồi đánh giá</small><h2>{{ selectedReview.user?.fullName || 'Khách hàng' }}</h2></div><button type="button" @click="closeReply"><span class="material-symbols-outlined">close</span></button></header>
          <blockquote>{{ selectedReview.comment || 'Khách hàng không để lại nội dung.' }}</blockquote>
          <div class="modal-templates"><button v-for="template in replyTemplates" :key="template.title" type="button" @click="replyContent = template.content">{{ template.title }}</button></div>
          <label><span>Nội dung phản hồi</span><textarea v-model="replyContent" rows="5" maxlength="1000" placeholder="Nhập phản hồi của nhà xe..."></textarea><small>{{ replyContent.length }}/1000 ký tự</small></label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <footer><button type="button" @click="closeReply">Hủy</button><button class="submit" :disabled="submitting" type="submit"><span class="material-symbols-outlined" :class="{ spin: submitting }">{{ submitting ? 'progress_activity' : 'send' }}</span>{{ selectedReview.adminReply ? 'Lưu thay đổi' : 'Gửi phản hồi' }}</button></footer>
        </form>
      </div>

      <div v-if="reviewToDelete" class="modal-overlay" @click.self="reviewToDelete = null">
        <section class="delete-modal"><span class="material-symbols-outlined">delete_forever</span><h2>Xóa đánh giá #{{ reviewToDelete.id }}?</h2><p>Đánh giá và phản hồi đi kèm sẽ bị xóa vĩnh viễn.</p><div><button @click="reviewToDelete = null">Giữ lại</button><button :disabled="deleting" @click="deleteReview">Xóa đánh giá</button></div></section>
      </div>
    </Teleport>

    <div v-if="toast.message" class="toast" :class="toast.type"><span class="material-symbols-outlined">{{ toast.type === 'error' ? 'error' : 'check_circle' }}</span>{{ toast.message }}</div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

const api = useApi()
const authStore = useAuthStore()
const reviews = ref([])
const loading = ref(true)
const loadError = ref('')
const search = ref('')
const selectedBusType = ref('ALL')
const replyStatus = ref('ALL')
const ratingFilter = ref('ALL')
const dateFrom = ref('')
const dateTo = ref('')
const activeTab = ref('ALL')
const sortOrder = ref('NEWEST')
const page = ref(1)
const pageSize = 10
const selectedReview = ref(null)
const replyContent = ref('')
const formError = ref('')
const submitting = ref(false)
const reviewToDelete = ref(null)
const deleting = ref(false)
const toast = ref({ message: '', type: 'success' })
let toastTimer

const replyTemplates = [
  { title: 'Cảm ơn đánh giá tích cực', content: 'Cảm ơn Anh/Chị đã tin tưởng và lựa chọn Trung Nam. Chúng tôi rất vui khi hành trình đã mang lại trải nghiệm tốt cho Anh/Chị.' },
  { title: 'Ghi nhận góp ý', content: 'Cảm ơn Anh/Chị đã góp ý. Trung Nam đã ghi nhận thông tin và sẽ chuyển đến bộ phận phụ trách để cải thiện dịch vụ.' },
  { title: 'Xin lỗi về trải nghiệm', content: 'Trung Nam rất tiếc vì trải nghiệm chưa đáp ứng mong đợi của Anh/Chị. Chúng tôi sẽ kiểm tra và khắc phục trong thời gian sớm nhất.' }
]
const tabs = [
  { value: 'ALL', label: 'Tất cả' }, { value: 'UNREPLIED', label: 'Chưa phản hồi' },
  { value: 'FIVE_STAR', label: '5 sao' }, { value: 'COMPLAINT', label: 'Khiếu nại (1–2 sao)' }
]

const repliedCount = computed(() => reviews.value.filter(item => item.adminReply?.trim()).length)
const unrepliedCount = computed(() => reviews.value.length - repliedCount.value)
const responseRate = computed(() => reviews.value.length ? Math.round(repliedCount.value / reviews.value.length * 100) : 0)
const averageRating = computed(() => reviews.value.length ? (reviews.value.reduce((sum, item) => sum + Number(item.rating || 0), 0) / reviews.value.length).toFixed(1) : '0.0')
const ratingCounts = computed(() => [0,1,2,3,4,5].reduce((result, star) => ({ ...result, [star]: reviews.value.filter(item => Number(item.rating) === star).length }), {}))
const ratingPercent = star => reviews.value.length ? Math.round(ratingCounts.value[star] / reviews.value.length * 100) : 0
const satisfactionRate = computed(() => reviews.value.length
  ? Math.round(reviews.value.reduce((sum, item) => sum + Number(item.rating || 0), 0) / (reviews.value.length * 5) * 100)
  : 0)
const positiveRate = computed(() => reviews.value.length ? Math.round(reviews.value.filter(item => item.rating >= 4).length / reviews.value.length * 100) : 0)
const neutralRate = computed(() => reviews.value.length ? Math.round(reviews.value.filter(item => item.rating === 3).length / reviews.value.length * 100) : 0)
const negativeRate = computed(() => Math.max(0, 100 - positiveRate.value - neutralRate.value))
const donutStyle = computed(() => ({ background: `conic-gradient(#0c9b78 0 ${satisfactionRate.value}%, #e8efed ${satisfactionRate.value}% 100%)` }))
const uniqueBusTypes = computed(() => [...new Set(reviews.value.map(item => item.busType).filter(Boolean))].sort((a,b) => a.localeCompare(b, 'vi')))

const filteredReviews = computed(() => {
  const keyword = search.value.trim().toLocaleLowerCase('vi-VN')
  const from = dateFrom.value ? new Date(`${dateFrom.value}T00:00:00`) : null
  const to = dateTo.value ? new Date(`${dateTo.value}T23:59:59`) : null
  const result = reviews.value.filter(item => {
    if (selectedBusType.value !== 'ALL' && item.busType !== selectedBusType.value) return false
    if (replyStatus.value === 'REPLIED' && !item.adminReply) return false
    if (replyStatus.value === 'UNREPLIED' && item.adminReply) return false
    if (ratingFilter.value !== 'ALL' && Number(item.rating) !== Number(ratingFilter.value)) return false
    const created = new Date(item.createdAt)
    if (from && created < from) return false
    if (to && created > to) return false
    if (activeTab.value === 'UNREPLIED' && item.adminReply) return false
    if (activeTab.value === 'FIVE_STAR' && Number(item.rating) !== 5) return false
    if (activeTab.value === 'COMPLAINT' && Number(item.rating) > 2) return false
    if (!keyword) return true
    return [item.user?.fullName, item.comment, item.adminReply, item.booking?.id, item.busType, item.companyName]
      .filter(Boolean).some(value => String(value).toLocaleLowerCase('vi-VN').includes(keyword))
  })
  return result.sort((a,b) => {
    if (sortOrder.value === 'HIGHEST') return b.rating - a.rating
    if (sortOrder.value === 'LOWEST') return a.rating - b.rating
    const delta = new Date(b.createdAt) - new Date(a.createdAt)
    return sortOrder.value === 'OLDEST' ? -delta : delta
  })
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredReviews.value.length / pageSize)))
const paginatedReviews = computed(() => filteredReviews.value.slice((page.value - 1) * pageSize, page.value * pageSize))
const pageStart = computed(() => filteredReviews.value.length ? (page.value - 1) * pageSize + 1 : 0)
const pageEnd = computed(() => Math.min(page.value * pageSize, filteredReviews.value.length))

watch([search, selectedBusType, replyStatus, ratingFilter, dateFrom, dateTo, activeTab, sortOrder], () => { page.value = 1 })
watch(totalPages, value => { if (page.value > value) page.value = value })

const tabCount = value => {
  if (value === 'UNREPLIED') return unrepliedCount.value
  if (value === 'FIVE_STAR') return ratingCounts.value[5]
  if (value === 'COMPLAINT') return ratingCounts.value[1] + ratingCounts.value[2]
  return reviews.value.length
}
const routeLabel = review => {
  const trip = review.booking?.trip
  return trip?.departurePoint && trip?.arrivalPoint ? `${shortPlace(trip.departurePoint)} → ${shortPlace(trip.arrivalPoint)}` : review.companyName || 'Trung Nam'
}
const shortPlace = value => String(value).replace(/^Bến xe\s+/i, '').replace(/^Trung tâm\s+/i, '').split(',')[0]
const avatar = review => review.user?.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(review.user?.fullName || 'Khách hàng')}&background=e7f3f0&color=075955&bold=true`
const formatDateTime = value => value ? new Date(value).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' }) : '—'

const fetchReviews = async () => {
  loading.value = true; loadError.value = ''
  try {
    const response = await api.get('/reviews/all')
    reviews.value = Array.isArray(response.data) ? response.data : []
    authStore.clearNewReviewsCount()
  } catch (error) { loadError.value = error.response?.data?.message || 'Không thể kết nối đến máy chủ.' }
  finally { loading.value = false }
}
const clearFilters = () => { search.value = ''; selectedBusType.value = 'ALL'; replyStatus.value = 'ALL'; ratingFilter.value = 'ALL'; dateFrom.value = ''; dateTo.value = ''; activeTab.value = 'ALL' }
const openReply = review => { selectedReview.value = review; replyContent.value = review.adminReply || ''; formError.value = '' }
const closeReply = () => { if (!submitting.value) selectedReview.value = null }
const useTemplate = content => {
  const target = paginatedReviews.value.find(item => !item.adminReply) || paginatedReviews.value[0]
  if (!target) return showToast('Không có đánh giá để phản hồi.', 'error')
  selectedReview.value = target; replyContent.value = content; formError.value = ''
}
const submitReply = async () => {
  if (!replyContent.value.trim()) return formError.value = 'Vui lòng nhập nội dung phản hồi.'
  submitting.value = true; formError.value = ''
  try {
    await api.put(`/reviews/${selectedReview.value.id}/reply`, { reply: replyContent.value.trim(), adminId: String(authStore.currentUser?.id || '') })
    const item = reviews.value.find(review => review.id === selectedReview.value.id)
    if (item) { item.adminReply = replyContent.value.trim(); item.repliedBy = authStore.currentUser }
    selectedReview.value = null; showToast('Đã lưu phản hồi.')
  } catch (error) { formError.value = error.response?.data?.message || 'Không thể gửi phản hồi.' }
  finally { submitting.value = false }
}
const deleteReview = async () => {
  deleting.value = true
  try { await api.delete(`/reviews/${reviewToDelete.value.id}`); reviews.value = reviews.value.filter(item => item.id !== reviewToDelete.value.id); reviewToDelete.value = null; showToast('Đã xóa đánh giá.') }
  catch (error) { showToast(error.response?.data?.message || 'Không thể xóa đánh giá.', 'error') }
  finally { deleting.value = false }
}
const exportCsv = () => {
  const rows = [['Mã đánh giá','Mã vé','Khách hàng','Dòng xe','Số sao','Nội dung','Phản hồi','Ngày đánh giá'], ...filteredReviews.value.map(item => [item.id,item.booking?.id || '',item.user?.fullName || '',item.busType || '',item.rating,item.comment || '',item.adminReply || '',formatDateTime(item.createdAt)])]
  const csv = '\uFEFF' + rows.map(row => row.map(value => `"${String(value).replaceAll('"','""')}"`).join(',')).join('\r\n')
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a'); link.href = url; link.download = `danh-gia-${new Date().toISOString().slice(0,10)}.csv`; link.click(); URL.revokeObjectURL(url)
}
const showToast = (message, type = 'success') => { clearTimeout(toastTimer); toast.value = { message, type }; toastTimer = setTimeout(() => toast.value.message = '', 3000) }

onMounted(fetchReviews)
</script>

<style scoped>
.review-manager{--green:#075955;--ink:#182a36;--muted:#71808b;display:grid;gap:1.15rem;color:var(--ink)}
.page-header,.title-block,.header-actions,.metric-card,.sentiment-content,.view-tabs,.customer-column,.customer-name,.stars,.review-tags,.reply-preview,.reply-status,.pagination,.pagination div,.reply-modal header,.reply-modal footer,.toast{display:flex;align-items:center}.page-header{justify-content:space-between;gap:1rem}.title-block{gap:.85rem}.title-icon{display:grid;width:3rem;height:3rem;place-items:center;border:1px solid #f2dca0;border-radius:.85rem;color:#d89500;background:#fff8e7}.title-block h1{font-size:1.65rem;font-weight:900;letter-spacing:-.04em}.title-block p{margin-top:.25rem;color:var(--muted);font-size:.78rem;font-weight:600}.header-actions{gap:.65rem}.header-actions button{display:flex;align-items:center;gap:.45rem;border:1px solid #dbe4e7;border-radius:.7rem;padding:.65rem .9rem;color:#41535e;background:white;font-size:.72rem;font-weight:800;transition:.2s}.header-actions button:hover{border-color:#a7c6c1;color:var(--green);transform:translateY(-1px)}
.overview-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(28rem,2.1fr);gap:.85rem}.metric-card,.rating-distribution{min-height:10.7rem;border:1px solid #dde7e9;border-radius:1rem;background:white;box-shadow:0 .7rem 2rem rgb(12 66 61/.045)}.metric-card{gap:1rem;padding:1.15rem}.metric-icon{display:grid;width:3rem;height:3rem;flex:none;place-items:center;border-radius:.8rem}.metric-card small,.response-card small,.templates-card small{color:#6e7c86;font-size:.65rem;font-weight:800}.metric-card strong{display:block;margin-top:.45rem;font-size:1.8rem;font-weight:900;letter-spacing:-.04em;font-variant-numeric:tabular-nums}.metric-card strong em{color:#98a3aa;font-size:.75rem;font-style:normal}.metric-card p{margin-top:.45rem;color:#80909a;font-size:.62rem;font-weight:650}.metric-blue .metric-icon{color:#2969cf;background:#edf4ff}.metric-green .metric-icon{color:#07866a;background:#eaf8f3}.metric-amber .metric-icon{color:#e4a00a;background:#fff6df}.mini-stars{color:#e9a410!important;font-size:1rem!important;letter-spacing:.08em}.filled{font-variation-settings:'FILL' 1}.rating-distribution{display:grid;grid-template-columns:1.3fr .9fr;padding:1rem 1.15rem}.rating-distribution h2{margin-bottom:.7rem;font-size:.7rem;font-weight:900}.distribution-bars{padding-right:1.1rem;border-right:1px solid #e5ebed}.bar-row{display:grid;grid-template-columns:2.5rem 1fr 2.2rem 2rem;align-items:center;gap:.5rem;margin:.42rem 0;color:#71808a;font-size:.56rem}.bar-row>div{height:.42rem;overflow:hidden;border-radius:1rem;background:#edf1f2}.bar-row i{display:block;height:100%;border-radius:1rem;background:#12916f}.bar-row .bar-3,.bar-row .bar-2{background:#efb322}.bar-row .bar-1{background:#e84b4b}.bar-row b{color:#4d606b;text-align:right}.bar-row small{color:#91a0a8}.sentiment-panel{padding-left:1.15rem}.sentiment-content{justify-content:space-between;gap:1rem}.donut{position:relative;display:grid;width:6.2rem;height:6.2rem;flex:none;place-content:center;border-radius:50%;text-align:center}.donut:after{position:absolute;inset:.65rem;border-radius:50%;background:white;content:''}.donut strong,.donut span{position:relative;z-index:1}.donut strong{font-size:1.25rem;font-weight:900}.donut span{color:#73858d;font-size:.55rem}.sentiment-panel dl{display:grid;gap:.45rem;flex:1}.sentiment-panel dl div{display:flex;justify-content:space-between;gap:.7rem;color:#667780;font-size:.6rem;font-weight:700}.sentiment-panel dt{display:flex;align-items:center;gap:.35rem}.dot{width:.45rem;height:.45rem;border-radius:50%}.positive{background:#0c9b78}.neutral{background:#efb322}.negative{background:#e84b4b}
.filter-panel{display:grid;grid-template-columns:1.6fr repeat(3,minmax(7rem,.8fr)) repeat(2,minmax(8rem,.8fr)) auto;align-items:end;gap:.7rem;border:1px solid #dfe7e9;border-radius:.9rem;padding:.85rem;background:white}.filter-panel label{display:grid;gap:.35rem}.filter-panel label>span{color:#52636d;font-size:.58rem;font-weight:800}.filter-panel input,.filter-panel select,.sort-control select{width:100%;height:2.45rem;border:1px solid #d9e3e6;border-radius:.6rem;padding:0 .75rem;outline:none;color:#3e505a;background:#fbfcfc;font-size:.65rem;font-weight:700}.filter-panel input:focus,.filter-panel select:focus,.sort-control select:focus{border-color:#168d7a;box-shadow:0 0 0 3px rgb(7 89 85/.08)}.search-field>div{position:relative}.search-field input{padding-right:2.3rem}.search-field div span{position:absolute;right:.7rem;top:.65rem;color:#71848d;font-size:1rem}.clear-filter{display:flex;height:2.45rem;align-items:center;gap:.35rem;border:1px solid #dbe3e5;border-radius:.6rem;padding:0 .75rem;color:#52646d;background:white;font-size:.62rem;font-weight:800}.clear-filter:hover{color:#b13a4e;background:#fff4f5}
.view-tabs{gap:.45rem;overflow-x:auto;border-bottom:1px solid #dfe6e8;padding-bottom:.7rem}.view-tabs>button{display:flex;align-items:center;gap:.45rem;border:1px solid #dce5e7;border-radius:.62rem;padding:.55rem .75rem;color:#63737c;background:white;font-size:.62rem;font-weight:800;white-space:nowrap}.view-tabs>button span{border-radius:.4rem;padding:.15rem .35rem;color:#55716b;background:#edf5f3;font-variant-numeric:tabular-nums}.view-tabs>button.active{border-color:#168d7a;color:var(--green);box-shadow:inset 0 -2px #168d7a}.sort-control{display:flex;align-items:center;gap:.5rem;margin-left:auto;color:#7a8991;font-size:.6rem;white-space:nowrap}.sort-control select{width:7.7rem;height:2.1rem;background:white}
.content-grid{display:grid;grid-template-columns:minmax(0,1fr) 17rem;gap:1rem}.reviews-panel{overflow:hidden;border:1px solid #dfe7e9;border-radius:1rem;background:white}.review-row{display:grid;grid-template-columns:16rem minmax(0,1fr) 10.5rem;gap:1rem;padding:1rem 1.1rem;border-bottom:1px solid #e9eef0;transition:background .2s}.review-row:hover{background:#fbfdfc}.review-row.complaint{box-shadow:inset 3px 0 #e05b5b}.customer-column{align-items:flex-start;gap:.75rem}.customer-column>img{width:3rem;height:3rem;flex:none;border-radius:.8rem;object-fit:cover}.customer-name{gap:.4rem}.customer-name strong{font-size:.72rem;font-weight:900}.customer-name span{border:1px solid #e2b45a;border-radius:.35rem;padding:.12rem .28rem;color:#b17400;background:#fff8e9;font-size:.48rem;font-weight:850}.customer-name span.danger{border-color:#efadb6;color:#bd3048;background:#fff2f4}.customer-column p,.customer-column small{display:block;margin-top:.2rem;color:#8a989f;font-size:.55rem;font-weight:650}.stars{gap:.08rem;margin-top:.32rem;color:#e9a410;font-size:.78rem}.stars .muted{color:#dce2e4}.stars b{margin-left:.3rem;color:#53656e;font-size:.58rem}.review-body{min-width:0}.review-tags{gap:.35rem}.review-tags span{overflow:hidden;max-width:14rem;border-radius:.35rem;padding:.25rem .45rem;color:#087262;background:#eef7f5;font-size:.5rem;font-weight:850;text-overflow:ellipsis;text-transform:uppercase;white-space:nowrap}.review-tags span:last-child{color:#356db4;background:#eef5ff}.review-body blockquote{margin-top:.6rem;border-radius:.65rem;padding:.65rem .75rem;color:#43565f;background:#f5f8f7;font-size:.64rem;font-weight:600;line-height:1.55}.reply-preview{align-items:flex-start;gap:.45rem;margin-top:.55rem;color:#52736c;font-size:.57rem}.reply-preview>span{font-size:.9rem}.reply-preview strong{font-weight:850}.reply-preview p{margin-top:.1rem;color:#71827d;line-height:1.4}.review-actions{display:flex;align-items:flex-start;align-content:flex-start;justify-content:flex-end;gap:.35rem;flex-wrap:wrap}.reply-status{width:100%;justify-content:flex-end;gap:.35rem;margin-bottom:.15rem;font-size:.55rem;font-weight:850}.reply-status i{width:.42rem;height:.42rem;border-radius:50%}.reply-status.done{color:#078165}.reply-status.done i{background:#10a47f}.reply-status.pending{color:#b17705}.reply-status.pending i{background:#e9a410}.review-actions button{display:flex;height:2rem;align-items:center;justify-content:center;gap:.28rem;border:1px solid #dce5e7;border-radius:.52rem;padding:0 .55rem;color:#50636d;background:white;font-size:.55rem;font-weight:800;transition:.2s}.review-actions button:hover{border-color:#94bbb4;color:var(--green)}.review-actions button.primary{border-color:var(--green);color:white;background:var(--green)}.review-actions button span{font-size:.85rem}.review-actions button.danger-button{width:2rem;padding:0;color:#b24558;background:#fff5f6}.pagination{justify-content:space-between;padding:.75rem 1rem;color:#788890;font-size:.58rem}.pagination div{gap:.5rem}.pagination button{width:1.8rem;height:1.8rem;border:1px solid #dbe4e6;border-radius:.45rem;background:white;font-size:1rem}.pagination button:disabled{opacity:.35}.pagination span{font-weight:800}.skeleton-list div{height:7.7rem;border-bottom:1px solid #e8eeee;background:linear-gradient(90deg,#f7f9f9 20%,#eaf0ef 40%,#f7f9f9 60%);background-size:220% 100%;animation:shimmer 1.3s linear infinite}.state-box{display:grid;min-height:24rem;place-items:center;align-content:center;padding:2rem;text-align:center}.state-box>span{color:#9aadaa;font-size:2.5rem}.state-box strong{margin-top:.6rem;font-size:.8rem}.state-box p{margin-top:.3rem;color:#82918f;font-size:.65rem}.state-box button{margin-top:.8rem;border-radius:.5rem;padding:.5rem .7rem;color:white;background:var(--green);font-size:.6rem;font-weight:800}.state-box.error{color:#a63b50}
.insight-sidebar{display:grid;align-content:start;gap:.8rem}.response-card,.templates-card{border:1px solid #dfe7e9;border-radius:.9rem;padding:1rem;background:white}.response-card>div{display:grid;margin-top:.4rem}.response-card strong{font-size:1.7rem;font-weight:900;letter-spacing:-.04em}.response-card span{color:#798a91;font-size:.57rem}.response-card progress{width:100%;height:.35rem;margin-top:.75rem;overflow:hidden;border:0;border-radius:1rem}.response-card progress::-webkit-progress-bar{background:#e9efee}.response-card progress::-webkit-progress-value{background:#0a8a70}.templates-card header{display:flex;justify-content:space-between;align-items:start}.templates-card h2{margin-top:.15rem;font-size:.75rem;font-weight:900}.templates-card header>span{color:#0a806c}.templates-card>button{display:grid;grid-template-columns:1.5rem 1fr;gap:.45rem;width:100%;margin-top:.65rem;border:1px solid #e0e7e8;border-radius:.65rem;padding:.65rem;text-align:left;transition:.2s}.templates-card>button:hover{border-color:#91c1b8;background:#f4faf8}.templates-card>button>span{color:#1c84b8;font-size:1rem}.templates-card button strong{font-size:.6rem;font-weight:850}.templates-card button p{display:-webkit-box;overflow:hidden;margin-top:.2rem;color:#718188;font-size:.54rem;line-height:1.4;-webkit-box-orient:vertical;-webkit-line-clamp:2}.template-hint{margin-top:.75rem;color:#8a989d;font-size:.52rem;line-height:1.5}
.modal-overlay{position:fixed;inset:0;z-index:60;display:grid;place-items:center;padding:1rem;background:rgb(9 28 31/.62);backdrop-filter:blur(6px)}.reply-modal{width:min(34rem,100%);overflow:hidden;border-radius:1rem;background:white;box-shadow:0 2rem 6rem rgb(0 41 38/.3)}.reply-modal header{gap:.65rem;border-bottom:1px solid #e5ebec;padding:1rem 1.15rem}.reply-modal header>span{display:grid;width:2.5rem;height:2.5rem;place-items:center;border-radius:.65rem;color:white;background:var(--green)}.reply-modal header small{color:#7a898f;font-size:.55rem;font-weight:800}.reply-modal header h2{font-size:.85rem;font-weight:900}.reply-modal header button{margin-left:auto;color:#77878d}.reply-modal>blockquote{margin:1rem 1.15rem 0;border-left:3px solid #e0aa2d;padding:.65rem .8rem;color:#63737a;background:#f8faf9;font-size:.65rem;line-height:1.5}.modal-templates{display:flex;gap:.4rem;overflow-x:auto;padding:.8rem 1.15rem .15rem}.modal-templates button{border:1px solid #dce6e5;border-radius:.5rem;padding:.4rem .55rem;color:#52716b;background:#f7faf9;font-size:.55rem;font-weight:800;white-space:nowrap}.reply-modal label{display:grid;gap:.4rem;padding:.8rem 1.15rem}.reply-modal label>span{font-size:.6rem;font-weight:850}.reply-modal textarea{resize:vertical;border:1px solid #dbe4e5;border-radius:.65rem;padding:.75rem;outline:none;color:#354950;background:#fbfcfc;font-size:.68rem;line-height:1.5}.reply-modal textarea:focus{border-color:#158b78;box-shadow:0 0 0 3px rgb(7 89 85/.08)}.reply-modal label small{text-align:right;color:#88969b;font-size:.52rem}.form-error{margin:0 1.15rem;border-radius:.5rem;padding:.5rem .65rem;color:#b3384e;background:#fff0f3;font-size:.6rem;font-weight:750}.reply-modal footer{justify-content:flex-end;gap:.5rem;border-top:1px solid #e7eced;padding:.9rem 1.15rem}.reply-modal footer button,.delete-modal button{border-radius:.55rem;padding:.6rem .8rem;color:#53666e;background:#edf2f1;font-size:.62rem;font-weight:850}.reply-modal footer button.submit{display:flex;align-items:center;gap:.35rem;color:white;background:var(--green)}.reply-modal footer span{font-size:.9rem}.delete-modal{width:min(25rem,100%);border-radius:1rem;padding:1.4rem;text-align:center;background:white}.delete-modal>span{color:#d94a5d;font-size:2.5rem}.delete-modal h2{margin-top:.5rem;font-size:1rem;font-weight:900}.delete-modal p{margin-top:.35rem;color:#7b898e;font-size:.65rem}.delete-modal div{display:flex;justify-content:center;gap:.5rem;margin-top:1rem}.delete-modal div button:last-child{color:white;background:#c83c52}.toast{position:fixed;right:1.5rem;bottom:1.5rem;z-index:70;gap:.5rem;border:1px solid #b9dfd4;border-radius:.7rem;padding:.75rem 1rem;color:#08705d;background:#f0fbf7;box-shadow:0 .8rem 2.2rem rgb(7 65 60/.14);font-size:.65rem;font-weight:800}.toast.error{border-color:#efc0c7;color:#b0374c;background:#fff2f4}.toast span{font-size:1rem}.spin{animation:spin .8s linear infinite}@keyframes spin{to{transform:rotate(360deg)}}@keyframes shimmer{to{background-position-x:-220%}}
.reply-modal header>span{background:#075955}.reply-modal footer button.submit{display:flex;min-width:8.5rem;align-items:center;justify-content:center;gap:.35rem;color:#fff;background:#075955;box-shadow:0 .35rem .9rem rgb(7 89 85/.18)}.reply-modal footer button.submit:hover{background:#064b48}.reply-modal footer button.submit:disabled{cursor:wait;opacity:.65}
@media(max-width:1200px){.overview-grid{grid-template-columns:repeat(3,1fr)}.rating-distribution{grid-column:1/-1}.filter-panel{grid-template-columns:repeat(4,1fr)}.search-field{grid-column:span 2}.content-grid{grid-template-columns:1fr}.insight-sidebar{grid-template-columns:1fr 1fr}.review-row{grid-template-columns:14rem 1fr 10rem}}
@media(max-width:760px){.page-header{align-items:flex-start;flex-direction:column}.overview-grid{grid-template-columns:1fr}.rating-distribution{grid-template-columns:1fr}.distribution-bars{padding:0 0 1rem;border:0;border-bottom:1px solid #e5ebed}.sentiment-panel{padding:1rem 0 0}.filter-panel{grid-template-columns:1fr 1fr}.search-field{grid-column:1/-1}.clear-filter{width:max-content}.view-tabs{padding-bottom:.6rem}.sort-control{width:100%;margin:.3rem 0 0}.content-grid{display:block}.insight-sidebar{display:none}.review-row{grid-template-columns:1fr}.review-actions{justify-content:flex-start}.reply-status{justify-content:flex-start}.pagination{align-items:flex-start;flex-direction:column;gap:.5rem}}
</style>
