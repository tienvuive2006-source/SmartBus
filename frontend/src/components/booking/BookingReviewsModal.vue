<template>
  <div class="reviews-overlay" @click.self="emit('close')">
    <section
      class="reviews-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="reviews-title"
    >
      <header class="reviews-header">
        <div class="reviews-header__identity">
          <span class="reviews-header__icon material-symbols-outlined">reviews</span>
          <div>
            <p>Đánh giá từ hành khách</p>
            <h2 id="reviews-title">{{ trip?.companyName }}</h2>
            <span>{{ trip?.busType || 'Tất cả loại xe' }} · Chỉ hiển thị đánh giá từ khách đã đặt vé</span>
          </div>
        </div>
        <button type="button" class="reviews-close" aria-label="Đóng bảng đánh giá" @click="emit('close')">
          <span class="material-symbols-outlined">close</span>
        </button>
      </header>

      <div v-if="reviewsLoading" class="reviews-loading" aria-label="Đang tải đánh giá">
        <div class="summary-skeleton skeleton"></div>
        <div v-for="item in 3" :key="item" class="review-skeleton">
          <span class="skeleton"></span>
          <div><i class="skeleton"></i><i class="skeleton"></i><i class="skeleton"></i></div>
        </div>
      </div>

      <div v-else-if="loadError" class="reviews-state">
        <span class="material-symbols-outlined">cloud_off</span>
        <h3>Chưa tải được đánh giá</h3>
        <p>Kết nối có thể đang gián đoạn. Bạn thử tải lại nhé.</p>
        <button type="button" @click="fetchReviews">Thử lại</button>
      </div>

      <div v-else-if="currentReviews.length === 0" class="reviews-state">
        <span class="material-symbols-outlined">rate_review</span>
        <h3>Chưa có đánh giá cho chuyến này</h3>
        <p>Đánh giá đầu tiên sẽ xuất hiện sau khi hành khách hoàn thành chuyến đi.</p>
      </div>

      <template v-else>
        <div class="reviews-summary">
          <div class="score-block">
            <span>Điểm trung bình</span>
            <strong>{{ averageRating }}</strong>
            <div class="stars" :aria-label="`${averageRating} trên 5 sao`">
              <span
                v-for="star in 5"
                :key="star"
                class="material-symbols-outlined"
                :class="star <= Math.round(Number(averageRating)) ? 'is-filled' : ''"
              >star</span>
            </div>
            <small>{{ currentReviews.length }} đánh giá đã xác thực</small>
          </div>

          <div class="rating-bars" aria-label="Phân bố số sao">
            <button
              v-for="star in [5, 4, 3, 2, 1]"
              :key="star"
              type="button"
              :class="{ active: selectedRating === star }"
              :aria-pressed="selectedRating === star"
              @click="toggleRating(star)"
            >
              <span>{{ star }} <i class="material-symbols-outlined">star</i></span>
              <span class="rating-track"><i :style="{ width: `${ratingPercent(star)}%` }"></i></span>
              <strong>{{ ratingCount(star) }}</strong>
            </button>
          </div>
        </div>

        <div class="reviews-toolbar">
          <label class="review-search">
            <span class="material-symbols-outlined">search</span>
            <input v-model.trim="searchQuery" type="search" placeholder="Tìm trong nội dung đánh giá" />
          </label>
          <label class="review-sort">
            <span class="material-symbols-outlined">swap_vert</span>
            <select v-model="sortBy" aria-label="Sắp xếp đánh giá">
              <option value="newest">Mới nhất</option>
              <option value="highest">Điểm cao nhất</option>
              <option value="lowest">Điểm thấp nhất</option>
            </select>
          </label>
        </div>

        <main class="reviews-content">
          <div class="reviews-result-line">
            <p><strong>{{ filteredReviews.length }}</strong> đánh giá phù hợp</p>
            <button v-if="hasActiveFilter" type="button" @click="clearFilters">Xóa bộ lọc</button>
          </div>

          <div v-if="filteredReviews.length === 0" class="reviews-state reviews-state--compact">
            <span class="material-symbols-outlined">search_off</span>
            <h3>Không tìm thấy đánh giá phù hợp</h3>
            <p>Thử từ khóa khác hoặc xóa bộ lọc số sao.</p>
            <button type="button" @click="clearFilters">Xóa bộ lọc</button>
          </div>

          <article v-for="review in filteredReviews" v-else :key="review.id" class="review-card">
            <div class="review-avatar" aria-hidden="true">{{ getInitial(review) }}</div>
            <div class="review-body">
              <div class="review-topline">
                <div>
                  <h3>{{ review.user?.fullName || 'Khách hàng ẩn danh' }}</h3>
                  <span><i class="material-symbols-outlined">verified</i> Đã hoàn thành chuyến đi</span>
                </div>
                <time :datetime="review.createdAt">{{ formatDateDisplay(review.createdAt) }}</time>
              </div>

              <div class="review-rating">
                <div class="stars" :aria-label="`${review.rating} trên 5 sao`">
                  <span
                    v-for="star in 5"
                    :key="star"
                    class="material-symbols-outlined"
                    :class="star <= review.rating ? 'is-filled' : ''"
                  >star</span>
                </div>
                <strong>{{ ratingLabel(review.rating) }}</strong>
              </div>

              <p class="review-comment">{{ review.comment || 'Khách hàng không để lại bình luận.' }}</p>

              <div class="review-meta">
                <span><i class="material-symbols-outlined">directions_bus</i>{{ review.busType || trip?.busType }}</span>
                <span v-if="reviewRoute(review)"><i class="material-symbols-outlined">route</i>{{ reviewRoute(review) }}</span>
              </div>

              <div v-if="review.adminReply" class="operator-reply">
                <span class="material-symbols-outlined">subdirectory_arrow_right</span>
                <div>
                  <strong>Phản hồi từ {{ review.repliedBy?.fullName || trip?.companyName }}</strong>
                  <p>{{ review.adminReply }}</p>
                </div>
              </div>
            </div>
          </article>
        </main>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useApi } from '@/composables/useApi';

const props = defineProps({
  trip: { type: Object, required: true }
});

const emit = defineEmits(['close']);
const api = useApi();
const reviewsLoading = ref(true);
const loadError = ref(false);
const currentReviews = ref([]);
const selectedRating = ref(null);
const searchQuery = ref('');
const sortBy = ref('newest');

const averageRating = computed(() => {
  if (!currentReviews.value.length) return '0.0';
  const total = currentReviews.value.reduce((sum, review) => sum + Number(review.rating || 0), 0);
  return (total / currentReviews.value.length).toFixed(1);
});

const hasActiveFilter = computed(() => selectedRating.value !== null || searchQuery.value.length > 0);

const filteredReviews = computed(() => {
  const query = searchQuery.value.toLocaleLowerCase('vi-VN');
  return currentReviews.value
    .filter((review) => selectedRating.value === null || Number(review.rating) === selectedRating.value)
    .filter((review) => {
      if (!query) return true;
      return [review.comment, review.adminReply, review.user?.fullName]
        .filter(Boolean)
        .some((value) => value.toLocaleLowerCase('vi-VN').includes(query));
    })
    .sort((a, b) => {
      if (sortBy.value === 'highest') return Number(b.rating) - Number(a.rating) || new Date(b.createdAt) - new Date(a.createdAt);
      if (sortBy.value === 'lowest') return Number(a.rating) - Number(b.rating) || new Date(b.createdAt) - new Date(a.createdAt);
      return new Date(b.createdAt) - new Date(a.createdAt);
    });
});

const ratingCount = (star) => currentReviews.value.filter((review) => Number(review.rating) === star).length;
const ratingPercent = (star) => (ratingCount(star) / currentReviews.value.length) * 100;
const toggleRating = (star) => { selectedRating.value = selectedRating.value === star ? null : star; };
const clearFilters = () => { selectedRating.value = null; searchQuery.value = ''; };

const formatDateDisplay = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const getInitial = (review) => (review.user?.fullName?.trim()?.charAt(0) || 'K').toLocaleUpperCase('vi-VN');

const ratingLabel = (rating) => ({ 5: 'Rất hài lòng', 4: 'Hài lòng', 3: 'Bình thường', 2: 'Chưa hài lòng', 1: 'Không hài lòng' }[Number(rating)] || 'Đã đánh giá');

const compactPlace = (value) => value?.split(',')?.[0]?.trim();
const reviewRoute = (review) => {
  const reviewTrip = review.booking?.trip;
  if (!reviewTrip?.departurePoint || !reviewTrip?.arrivalPoint) return '';
  return `${compactPlace(reviewTrip.departurePoint)} → ${compactPlace(reviewTrip.arrivalPoint)}`;
};

const fetchReviews = async () => {
  reviewsLoading.value = true;
  loadError.value = false;
  try {
    const response = await api.get(`/reviews/company/${encodeURIComponent(props.trip.companyName)}`, {
      params: { busType: props.trip.busType }
    });
    currentReviews.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('Không thể tải danh sách đánh giá:', error);
    loadError.value = true;
  } finally {
    reviewsLoading.value = false;
  }
};

const onKeydown = (event) => { if (event.key === 'Escape') emit('close'); };

onMounted(() => {
  document.addEventListener('keydown', onKeydown);
  fetchReviews();
});

onBeforeUnmount(() => document.removeEventListener('keydown', onKeydown));
</script>

<style scoped>
.reviews-overlay { position: fixed; inset: 0; z-index: 10010; display: grid; place-items: center; padding: 1rem; background: rgb(15 23 42 / .62); backdrop-filter: blur(6px); }
.reviews-modal { display: flex; width: min(62rem, 100%); max-height: calc(100dvh - 2rem); flex-direction: column; overflow: hidden; border: 1px solid rgb(255 255 255 / .45); border-radius: 1.5rem; background: #f5f8f7; box-shadow: 0 2rem 5rem rgb(2 44 42 / .32); }
.reviews-header { position: relative; display: flex; flex-shrink: 0; align-items: center; justify-content: space-between; gap: .8rem; overflow: hidden; padding: .8rem 1.15rem; color: white; background: #075955; }
.reviews-header::after { position: absolute; inset: 0; content: ''; opacity: .16; pointer-events: none; background-image: radial-gradient(circle at 1px 1px, white 1px, transparent 0); background-size: 18px 18px; mask-image: linear-gradient(90deg, transparent, black); }
.reviews-header__identity { position: relative; z-index: 1; display: flex; align-items: center; gap: .7rem; min-width: 0; }
.reviews-header__icon { display: grid; width: 2.25rem; height: 2.25rem; flex: 0 0 auto; place-items: center; border: 1px solid rgb(255 255 255 / .18); border-radius: .65rem; color: #f8c84b; background: rgb(255 255 255 / .09); font-size: 1.15rem; }
.reviews-header p { margin-bottom: .05rem; color: #a7f3d0; font-size: .58rem; font-weight: 800; letter-spacing: .09em; text-transform: uppercase; }
.reviews-header h2 { font-size: clamp(1.05rem, 2.5vw, 1.3rem); font-weight: 900; line-height: 1.1; letter-spacing: -.025em; }
.reviews-header__identity > div > span { display: block; margin-top: .2rem; color: rgb(255 255 255 / .68); font-size: .64rem; font-weight: 600; }
.reviews-close { position: relative; z-index: 2; display: grid; width: 2rem; height: 2rem; flex: 0 0 auto; place-items: center; border-radius: .6rem; color: white; background: rgb(255 255 255 / .1); transition: background .2s, transform .2s; }
.reviews-close .material-symbols-outlined { font-size: 1.2rem; }
.reviews-close:hover { background: rgb(255 255 255 / .2); }
.reviews-close:active { transform: scale(.93); }
.reviews-close:focus-visible, .rating-bars button:focus-visible, .reviews-state button:focus-visible { outline: 3px solid #f8c84b; outline-offset: 2px; }
.reviews-summary { display: grid; flex-shrink: 0; grid-template-columns: minmax(12rem, .75fr) minmax(18rem, 1.25fr); gap: 2rem; padding: 1.4rem 1.6rem; border-bottom: 1px solid #dce7e4; background: white; }
.score-block { display: grid; grid-template-columns: auto 1fr; column-gap: 1rem; align-items: end; align-content: center; border-right: 1px solid #e2ebe9; }
.score-block > span { grid-column: 1 / -1; color: #64748b; font-size: .7rem; font-weight: 800; letter-spacing: .07em; text-transform: uppercase; }
.score-block > strong { grid-row: 2 / 4; color: #073f3c; font-size: 3.65rem; font-weight: 900; line-height: .95; letter-spacing: -.07em; font-variant-numeric: tabular-nums; }
.score-block small { color: #64748b; font-size: .72rem; font-weight: 650; }
.stars { display: flex; gap: .08rem; color: #cbd5e1; }
.stars span { font-size: 1.08rem; font-variation-settings: 'FILL' 1; }
.stars .is-filled { color: #e7ab1d; }
.rating-bars { display: grid; gap: .25rem; }
.rating-bars button { display: grid; grid-template-columns: 2.2rem 1fr 1.8rem; align-items: center; gap: .65rem; border-radius: .4rem; padding: .16rem .35rem; color: #64748b; transition: background .2s, color .2s; }
.rating-bars button:hover, .rating-bars button.active { color: #075955; background: #edf8f5; }
.rating-bars button > span:first-child { display: flex; align-items: center; gap: .15rem; font-size: .72rem; font-weight: 800; }
.rating-bars button .material-symbols-outlined { color: #e7ab1d; font-size: .8rem; font-variation-settings: 'FILL' 1; }
.rating-track { height: .38rem; overflow: hidden; border-radius: 1rem; background: #e8eeec; }
.rating-track i { display: block; height: 100%; border-radius: inherit; background: #d9a426; transition: width .3s ease; }
.rating-bars button > strong { font-size: .7rem; font-weight: 800; text-align: right; font-variant-numeric: tabular-nums; }
.reviews-toolbar { display: flex; flex-shrink: 0; gap: .75rem; padding: .8rem 1.5rem; border-bottom: 1px solid #dce7e4; background: #f5f8f7; }
.review-search, .review-sort { display: flex; align-items: center; gap: .55rem; border: 1px solid #d7e2df; border-radius: .7rem; color: #78908a; background: white; transition: border-color .2s, box-shadow .2s; }
.review-search { flex: 1; padding: 0 .8rem; }
.review-sort { padding: 0 .65rem; }
.review-search:focus-within, .review-sort:focus-within { border-color: #0b7a73; box-shadow: 0 0 0 3px rgb(11 122 115 / .1); }
.review-search .material-symbols-outlined, .review-sort .material-symbols-outlined { font-size: 1.15rem; }
.review-search input, .review-sort select { width: 100%; min-height: 2.55rem; outline: none; color: #243b38; background: transparent; font-size: .76rem; font-weight: 650; }
.review-sort select { min-width: 8.5rem; cursor: pointer; }
.reviews-content { min-height: 0; overflow-y: auto; padding: .5rem 1.5rem 1.5rem; }
.reviews-result-line { display: flex; align-items: center; justify-content: space-between; padding: .75rem .1rem .45rem; color: #64748b; font-size: .7rem; }
.reviews-result-line strong { color: #075955; }
.reviews-result-line button { color: #08746d; font-weight: 800; }
.review-card { display: grid; grid-template-columns: 2.8rem minmax(0, 1fr); gap: .9rem; padding: 1.2rem 0; border-bottom: 1px solid #dfe8e6; }
.review-card:last-child { border-bottom: 0; }
.review-avatar { display: grid; width: 2.8rem; height: 2.8rem; place-items: center; border-radius: .78rem; color: white; background: #08746d; font-size: .95rem; font-weight: 900; box-shadow: inset 0 0 0 1px rgb(255 255 255 / .15); }
.review-topline { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; }
.review-topline h3 { color: #172b28; font-size: .88rem; font-weight: 850; }
.review-topline div > span { display: flex; align-items: center; gap: .25rem; margin-top: .22rem; color: #07856f; font-size: .65rem; font-weight: 750; }
.review-topline div > span i { font-size: .85rem; font-style: normal; font-variation-settings: 'FILL' 1; }
.review-topline time { flex: 0 0 auto; color: #84948f; font-size: .67rem; font-weight: 650; font-variant-numeric: tabular-nums; }
.review-rating { display: flex; align-items: center; gap: .65rem; margin-top: .65rem; }
.review-rating strong { color: #7b6852; font-size: .68rem; font-weight: 800; }
.review-comment { max-width: 65ch; margin-top: .65rem; color: #334743; font-size: .82rem; font-weight: 550; line-height: 1.65; white-space: pre-line; }
.review-meta { display: flex; flex-wrap: wrap; gap: .45rem 1rem; margin-top: .8rem; color: #71837f; font-size: .66rem; font-weight: 650; }
.review-meta span { display: flex; align-items: center; gap: .3rem; }
.review-meta i { color: #08746d; font-size: .95rem; font-style: normal; }
.operator-reply { display: flex; gap: .65rem; margin-top: 1rem; border-left: 3px solid #0b8d7e; border-radius: 0 .65rem .65rem 0; padding: .8rem .9rem .85rem .75rem; background: #eaf6f3; }
.operator-reply > span { color: #0b8d7e; font-size: 1.1rem; }
.operator-reply strong { color: #075955; font-size: .7rem; font-weight: 850; }
.operator-reply p { margin-top: .25rem; color: #435b56; font-size: .76rem; line-height: 1.55; }
.reviews-loading { min-height: 28rem; overflow: hidden; padding: 1.5rem; }
.skeleton { display: block; background: linear-gradient(90deg, #e4ebe9 25%, #f2f6f5 45%, #e4ebe9 65%); background-size: 220% 100%; animation: skeleton-wave 1.4s infinite linear; }
.summary-skeleton { height: 7.5rem; border-radius: 1rem; }
.review-skeleton { display: grid; grid-template-columns: 2.8rem 1fr; gap: 1rem; padding: 1.25rem 0; border-bottom: 1px solid #e1e9e7; }
.review-skeleton > span { width: 2.8rem; height: 2.8rem; border-radius: .75rem; }
.review-skeleton div { display: grid; gap: .5rem; }
.review-skeleton i { height: .7rem; border-radius: .25rem; }
.review-skeleton i:nth-child(1) { width: 30%; }.review-skeleton i:nth-child(2) { width: 18%; }.review-skeleton i:nth-child(3) { width: 75%; }
.reviews-state { display: flex; min-height: 28rem; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; text-align: center; }
.reviews-state > span { display: grid; width: 4rem; height: 4rem; place-items: center; border-radius: 1.1rem; color: #0a756d; background: #e4f5f0; font-size: 2rem; }
.reviews-state h3 { margin-top: 1rem; color: #18312d; font-size: 1rem; font-weight: 900; }
.reviews-state p { max-width: 26rem; margin-top: .4rem; color: #71837f; font-size: .75rem; line-height: 1.6; }
.reviews-state button { margin-top: 1rem; border-radius: .7rem; padding: .65rem 1rem; color: white; background: #075955; font-size: .72rem; font-weight: 850; transition: background .2s, transform .2s; }
.reviews-state button:hover { background: #064b48; }.reviews-state button:active { transform: scale(.96); }
.reviews-state--compact { min-height: 15rem; }
@keyframes skeleton-wave { to { background-position-x: -220%; } }
@media (max-width: 640px) {
  .reviews-overlay { padding: 0; }
  .reviews-modal { width: 100%; max-height: 100dvh; min-height: 100dvh; border: 0; border-radius: 0; }
  .reviews-header { padding: .7rem .85rem; }
  .reviews-header__identity > div > span { display: none; }
  .reviews-summary { grid-template-columns: 1fr; gap: 1rem; padding: 1rem; }
  .score-block { padding-bottom: 1rem; border-right: 0; border-bottom: 1px solid #e2ebe9; }
  .reviews-toolbar { flex-direction: column; padding: .75rem 1rem; }
  .review-sort select { min-width: 0; }
  .reviews-content { padding: .35rem 1rem 1rem; }
  .review-card { grid-template-columns: 2.4rem minmax(0, 1fr); gap: .7rem; }
  .review-avatar { width: 2.4rem; height: 2.4rem; }
  .review-topline { gap: .5rem; }
}
</style>
