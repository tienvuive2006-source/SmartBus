<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-white p-6 rounded-[24px] shadow-sm border border-slate-100">
      <div>
        <h2 class="text-2xl font-black text-slate-800 tracking-tight flex items-center gap-3">
          <div class="w-12 h-12 bg-amber-50 text-amber-500 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl">star_rate</span>
          </div>
          Quản lý Đánh giá
        </h2>
        <p class="text-sm font-medium text-slate-500 mt-2 ml-1">Xem, phản hồi và quản lý đánh giá của hành khách.</p>
      </div>
      <button @click="fetchReviews" class="px-5 py-2.5 bg-slate-50 hover:bg-slate-100 text-slate-700 rounded-xl text-sm font-bold transition-colors flex items-center gap-2 border border-slate-200">
        <span class="material-symbols-outlined text-xl">refresh</span>
        Làm mới
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- Total -->
      <div class="bg-white p-6 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex items-center gap-5 relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-blue-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="w-14 h-14 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center border border-blue-100 relative z-10">
          <span class="material-symbols-outlined text-2xl">reviews</span>
        </div>
        <div class="relative z-10">
          <p class="text-[10px] font-black uppercase tracking-widest text-slate-400 mb-1">Tổng đánh giá</p>
          <p class="text-3xl font-extrabold text-slate-800 tracking-tight">{{ reviews.length }}</p>
        </div>
      </div>
      <!-- Replied -->
      <div class="bg-white p-6 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex items-center gap-5 relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-emerald-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="w-14 h-14 bg-emerald-50 text-[#075955] rounded-2xl flex items-center justify-center border border-emerald-100 relative z-10">
          <span class="material-symbols-outlined text-2xl">check_circle</span>
        </div>
        <div class="relative z-10">
          <p class="text-[10px] font-black uppercase tracking-widest text-slate-400 mb-1">Đã phản hồi</p>
          <p class="text-3xl font-extrabold text-slate-800 tracking-tight">{{ reviews.filter(r => r.adminReply).length }}</p>
        </div>
      </div>
      <!-- Average -->
      <div class="bg-white p-6 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 flex items-center gap-5 relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-amber-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="w-14 h-14 bg-amber-50 text-amber-500 rounded-2xl flex items-center justify-center border border-amber-100 relative z-10">
          <span class="material-symbols-outlined text-2xl">star</span>
        </div>
        <div class="relative z-10">
          <p class="text-[10px] font-black uppercase tracking-widest text-slate-400 mb-1">Điểm trung bình</p>
          <p class="text-3xl font-extrabold text-slate-800 tracking-tight">{{ averageRating }} <span class="text-lg text-slate-400 font-semibold">/ 5.0</span></p>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center bg-white p-4 rounded-2xl border border-slate-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] gap-4">
      <span class="text-xs font-black uppercase tracking-widest text-slate-500 flex items-center gap-2">
        <span class="material-symbols-outlined text-[18px]">filter_list</span>
        Bộ lọc:
      </span>
      <div class="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
        <select v-model="selectedBusType" class="w-full sm:w-auto bg-slate-50 border border-slate-200 text-slate-700 text-sm rounded-xl focus:ring-blue-500 focus:border-blue-500 block px-4 py-2 font-bold outline-none cursor-pointer transition-all">
          <option value="ALL">Tất cả dòng xe</option>
          <option v-for="type in uniqueBusTypes" :key="type" :value="type">{{ type }}</option>
        </select>
        <select v-model="replyStatusFilter" class="w-full sm:w-auto bg-slate-50 border border-slate-200 text-slate-700 text-sm rounded-xl focus:ring-blue-500 focus:border-blue-500 block px-4 py-2 font-bold outline-none cursor-pointer transition-all">
          <option value="ALL">Tất cả trạng thái</option>
          <option value="UNREPLIED">Chưa phản hồi</option>
          <option value="REPLIED">Đã phản hồi</option>
        </select>
      </div>
    </div>

    <!-- Reviews List -->
    <div class="space-y-4">
      <div v-if="loading" class="bg-white rounded-3xl p-12 text-center text-slate-400 border border-slate-100 shadow-sm">
        <span class="material-symbols-outlined animate-spin text-4xl mb-2">refresh</span>
        <p class="font-medium">Đang tải dữ liệu...</p>
      </div>

      <div v-else-if="reviews.length === 0" class="bg-white rounded-3xl p-16 text-center text-slate-400 border border-slate-100 shadow-sm">
        <div class="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-100">
          <span class="material-symbols-outlined text-4xl text-slate-300">speaker_notes_off</span>
        </div>
        <p class="font-black text-xl text-slate-600 tracking-tight">Chưa có đánh giá nào</p>
        <p class="text-sm font-medium mt-2">Hệ thống chưa ghi nhận đánh giá nào từ khách hàng.</p>
      </div>

      <div v-else class="space-y-8">
        <div v-for="(typeReviews, busType) in filteredGroupedReviews" :key="busType">
          <!-- Nhóm dòng xe Header -->
          <div class="flex items-center gap-3 mb-4 pl-2">
            <template v-if="getGroupImageUrl(typeReviews)">
              <div class="w-16 h-10 rounded-xl border border-slate-200 overflow-hidden shadow-sm shrink-0 bg-slate-100">
                 <img :src="getGroupImageUrl(typeReviews)" class="w-full h-full object-cover" />
              </div>
            </template>
            <template v-else>
              <div class="w-8 h-8 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center shadow-sm border border-blue-100">
                <span class="material-symbols-outlined text-[18px]">directions_bus</span>
              </div>
            </template>
            <h3 class="text-lg font-black text-slate-800 tracking-tight uppercase">{{ busType }}</h3>
            <span class="text-[10px] font-bold text-slate-500 bg-slate-100 border border-slate-200 px-2 py-0.5 rounded-full">{{ typeReviews.length }} đánh giá</span>
          </div>

          <div class="space-y-4">
            <div v-for="review in typeReviews" :key="review.id" class="bg-white rounded-3xl p-6 border border-slate-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative group transition-all duration-300 hover:shadow-lg flex flex-col">
              <!-- Top Row: Avatar & Name & Delete -->
              <div class="flex justify-between items-start mb-4">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-full bg-amber-50 border border-amber-100 overflow-hidden shrink-0 flex items-center justify-center">
                    <img :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(review.user.fullName)}&background=f59e0b&color=fff`" />
                  </div>
                  <div>
                    <h4 class="font-bold text-slate-800">{{ review.user.fullName }}</h4>
                    <div class="flex items-center gap-2 mt-0.5">
                      <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest">{{ new Date(review.createdAt).toLocaleDateString('vi-VN') }}</span>
                      <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                      <div class="flex items-center gap-0.5">
                        <span v-for="i in 5" :key="i" class="material-symbols-outlined text-[12px]" :class="i <= review.rating ? 'text-amber-400' : 'text-slate-200'" style="font-variation-settings: 'FILL' 1;">star</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <button @click="deleteReview(review.id)" class="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 hover:bg-red-500 hover:text-white hover:shadow-md hover:shadow-red-500/20 transition-all opacity-0 group-hover:opacity-100">
                  <span class="material-symbols-outlined text-[18px]">delete</span>
                </button>
              </div>

              <!-- Tags -->
              <div class="flex flex-wrap items-center gap-2 mb-4">
                <span class="text-[10px] font-black uppercase tracking-widest text-[#075955] bg-[#075955]/10 px-2.5 py-1 rounded-lg border border-[#075955]/20">{{ review.companyName }}</span>
                <span v-if="review.busType" class="text-[10px] font-black uppercase tracking-widest text-blue-600 bg-blue-50 px-2.5 py-1 rounded-lg border border-blue-100">{{ review.busType }}</span>
                <span class="text-[10px] font-black text-slate-500 bg-slate-100 px-2.5 py-1 rounded-lg border border-slate-200">VÉ #{{ review.booking.id }}</span>
              </div>

              <!-- User Comment -->
              <div class="bg-slate-50 rounded-2xl p-4 mb-4 flex-1">
                <p class="text-sm font-medium text-slate-700 leading-relaxed italic">"{{ review.comment || 'Không có bình luận.' }}"</p>
              </div>

              <!-- Admin Reply Section -->
              <div class="mt-auto">
                <div v-if="review.adminReply" class="bg-emerald-50/50 rounded-2xl p-4 border border-emerald-100 relative group/reply">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-[10px] font-black uppercase tracking-widest text-[#075955] flex items-center gap-1.5">
                      <span class="material-symbols-outlined text-[14px]">admin_panel_settings</span>
                      {{ review.repliedBy?.fullName || 'ADMIN NHÀ XE' }}
                    </span>
                    <button @click="openReplyModal(review)" class="text-[10px] font-bold text-emerald-600 hover:text-emerald-800 uppercase tracking-widest bg-emerald-100/50 px-2 py-0.5 rounded opacity-0 group-hover/reply:opacity-100 transition-opacity">Chỉnh sửa</button>
                  </div>
                  <p class="text-sm text-slate-700 font-medium leading-relaxed">{{ review.adminReply }}</p>
                </div>
                
                <button v-else @click="openReplyModal(review)" class="w-full py-2.5 bg-blue-50 hover:bg-blue-500 text-blue-600 hover:text-white rounded-xl text-xs font-bold uppercase tracking-widest transition-all border border-blue-100 hover:border-blue-500 flex items-center justify-center gap-2 group/btn">
                  <span class="material-symbols-outlined text-[16px] group-hover/btn:-translate-y-0.5 transition-transform">reply</span>
                  Phản hồi khách hàng
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reply Modal -->
    <Teleport to="body">
      <div v-if="showReplyModal" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-[24px] w-full max-w-lg shadow-2xl border border-slate-200 overflow-hidden">
        <!-- Header -->
        <div class="p-5 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <h3 class="text-base font-black text-slate-800 flex items-center gap-2">
            <span class="material-symbols-outlined text-blue-500">reply</span>
            Phản hồi khách hàng
          </h3>
          <button @click="showReplyModal = false" class="w-8 h-8 rounded-full hover:bg-slate-200 flex items-center justify-center text-slate-400 transition-colors">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>
        
        <!-- Body -->
        <div class="p-5 space-y-5 bg-slate-50/50">
          <!-- Quote -->
          <div class="relative pl-2">
            <div class="absolute -left-1 -top-2 text-4xl text-slate-200 font-serif leading-none opacity-50">"</div>
            <p class="text-sm font-medium text-slate-600 leading-relaxed bg-white p-4 rounded-2xl border border-slate-100 shadow-sm relative z-10 italic">
              {{ selectedReview?.comment || 'Khách hàng không để lại bình luận.' }}
            </p>
          </div>

          <!-- Textarea -->
          <div class="bg-white rounded-2xl border border-blue-100 shadow-[0_0_15px_rgba(59,130,246,0.05)] focus-within:border-blue-400 focus-within:ring-4 focus-within:ring-blue-500/10 transition-all overflow-hidden relative">
            <div class="px-4 py-2.5 border-b border-slate-50 bg-slate-50/50">
              <span class="text-[10px] font-black uppercase tracking-widest text-blue-600">Nội dung phản hồi</span>
            </div>
            <textarea 
              v-model="replyContent"
              rows="4"
              placeholder="Nhập câu trả lời của nhà xe..."
              class="w-full p-4 bg-transparent text-sm font-medium outline-none resize-none placeholder:text-slate-300 text-slate-700"
            ></textarea>
          </div>
        </div>

        <!-- Footer -->
        <div class="p-5 bg-slate-50/50 border-t border-slate-100 flex gap-3">
          <button @click="showReplyModal = false" class="flex-1 py-2.5 bg-white hover:bg-slate-100 text-slate-600 border border-slate-200 rounded-xl text-sm font-bold transition-colors">Hủy</button>
          <button @click="submitReply" class="flex-1 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-bold transition-all shadow-md shadow-blue-600/20 flex items-center justify-center gap-2 active:scale-95" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
            <span v-else class="material-symbols-outlined text-[18px]">send</span>
            Gửi phản hồi
          </button>
        </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useApi } from '@/composables/useApi';
import { useAuthStore } from '@/stores/auth';

const api = useApi();
const authStore = useAuthStore();
const reviews = ref([]);
const loading = ref(true);

const showReplyModal = ref(false);
const selectedReview = ref(null);
const replyContent = ref('');
const isSubmitting = ref(false);

const averageRating = computed(() => {
  if (reviews.value.length === 0) return '0.0';
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0);
  return (sum / reviews.value.length).toFixed(1);
});

const groupedReviews = computed(() => {
  const groups = {};
  reviews.value.forEach(r => {
    if (replyStatusFilter.value === 'UNREPLIED' && r.adminReply) return;
    if (replyStatusFilter.value === 'REPLIED' && !r.adminReply) return;

    const type = r.busType || 'Khác';
    if (!groups[type]) groups[type] = [];
    groups[type].push(r);
  });
  return groups;
});

const selectedBusType = ref('ALL');
const replyStatusFilter = ref('ALL');

const uniqueBusTypes = computed(() => {
  const types = new Set();
  reviews.value.forEach(r => types.add(r.busType || 'Khác'));
  return Array.from(types);
});

const filteredGroupedReviews = computed(() => {
  if (selectedBusType.value === 'ALL') {
    return groupedReviews.value;
  }
  const filtered = {};
  if (groupedReviews.value[selectedBusType.value]) {
    filtered[selectedBusType.value] = groupedReviews.value[selectedBusType.value];
  }
  return filtered;
});

const getGroupImageUrl = (reviewsInGroup) => {
  const reviewWithImage = reviewsInGroup.find(r => r.booking?.trip?.imageUrl);
  return reviewWithImage ? reviewWithImage.booking.trip.imageUrl : null;
};

const fetchReviews = async () => {
  loading.value = true;
  try {
    const res = await api.get('/reviews/all');
    // Sắp xếp mới nhất lên đầu
    reviews.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (error) {
    console.error('Lỗi tải đánh giá:', error);
    alert('Không thể tải dữ liệu đánh giá!');
  } finally {
    loading.value = false;
  }
};

const openReplyModal = (review) => {
  selectedReview.value = review;
  replyContent.value = review.adminReply || '';
  showReplyModal.value = true;
};

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    alert('Vui lòng nhập nội dung phản hồi!');
    return;
  }
  isSubmitting.value = true;
  try {
    await api.put(`/reviews/${selectedReview.value.id}/reply`, { 
      reply: replyContent.value,
      adminId: authStore.user.id
    });
    alert('Đã gửi phản hồi!');
    showReplyModal.value = false;
    fetchReviews();
  } catch (error) {
    console.error('Lỗi gửi phản hồi:', error);
    alert('Có lỗi xảy ra khi gửi phản hồi!');
  } finally {
    isSubmitting.value = false;
  }
};

const deleteReview = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa đánh giá này? Hành động này không thể hoàn tác!')) return;
  
  try {
    await api.delete(`/reviews/${id}`);
    alert('Xóa đánh giá thành công!');
    fetchReviews();
  } catch (error) {
    console.error('Lỗi xóa đánh giá:', error);
    alert('Có lỗi xảy ra khi xóa!');
  }
};

onMounted(() => {
  fetchReviews();
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}
</style>
