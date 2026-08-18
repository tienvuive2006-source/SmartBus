<template>
  <div class="fixed bottom-6 right-6 z-[9999] font-sans">
    <!-- Chat Window -->
    <div 
      v-if="isOpen" 
      class="absolute bottom-16 right-0 w-[350px] sm:w-[400px] h-[500px] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden animate-slide-up"
    >
      <!-- Header -->
      <div class="bg-gradient-to-r from-[#075955] to-[#0a7a75] p-4 text-white flex justify-between items-center shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm">
            <span class="material-symbols-outlined text-white">smart_toy</span>
          </div>
          <div>
            <h3 class="font-bold text-sm">SmartBus AI Assistant</h3>
            <p class="text-[10px] text-emerald-100 flex items-center gap-1">
              <span class="w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse"></span> Online
            </p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button @click="clearChat" class="hover:bg-white/20 p-1.5 rounded-full transition-colors flex items-center justify-center" title="Xóa lịch sử trò chuyện">
            <span class="material-symbols-outlined text-sm">delete</span>
          </button>
          <button @click="toggleChat" class="hover:bg-white/20 p-1.5 rounded-full transition-colors flex items-center justify-center" title="Đóng">
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 p-4 overflow-y-auto bg-slate-50 space-y-4" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" :class="['flex', msg.isBot ? 'justify-start' : 'justify-end']">
          <div 
            :class="[
              'max-w-[85%] rounded-2xl p-3 text-sm shadow-sm', 
              msg.isBot ? 'bg-white text-gray-800 border border-gray-100 rounded-tl-sm' : 'bg-[#075955] text-white rounded-tr-sm'
            ]"
          >
            <!-- Nếu có nội dung text -->
            <p v-if="msg.text" class="leading-relaxed whitespace-pre-wrap" v-html="formatText(msg.text)"></p>
            
            <!-- Hiển thị Preview Chuyến xe -->
            <div v-if="msg.tripsPreview && msg.tripsPreview.length > 0" class="mt-3 space-y-2">
               <div 
                 v-for="trip in msg.tripsPreview" 
                 :key="trip.id" 
                 @click="navigateToBooking(msg.params, trip.id)"
                 class="bg-white border border-gray-100 rounded-lg p-2.5 shadow-sm hover:border-[#075955] hover:shadow-md transition-all cursor-pointer group"
               >
                  <div class="flex justify-between items-start mb-1">
                     <span class="font-bold text-gray-800 text-[13px] group-hover:text-[#075955] transition-colors">{{ trip.companyName }}</span>
                     <span class="font-bold text-[#f03a17] text-[13px]">{{ trip.price.toLocaleString() }}đ</span>
                  </div>
                  <div class="flex items-center gap-3 text-[11px] text-gray-500">
                     <span class="flex items-center gap-1 font-semibold"><span class="material-symbols-outlined text-[12px]">schedule</span> {{ trip.departureTime }}</span>
                     <span class="truncate max-w-[120px]">{{ trip.busType }}</span>
                  </div>
               </div>
               
               <button 
                 @click="navigateToBooking(msg.params)"
                 class="w-full mt-2 py-2 bg-[#075955]/10 text-[#075955] text-xs font-bold rounded-lg hover:bg-[#075955] hover:text-white transition-colors"
               >
                 Xem tất cả chuyến xe & Đặt vé
               </button>
            </div>

            <!-- Nếu có action tùy chỉnh -->
            <button 
              v-if="msg.action === 'navigate_history'"
              @click="router.push('/history'); isOpen = false;"
              class="w-full mt-2 py-2 bg-rose-50 text-rose-600 border border-rose-100 text-xs font-bold rounded-lg hover:bg-rose-500 hover:text-white transition-colors"
            >
              Đi đến Lịch sử giao dịch
            </button>

            <!-- Nếu là suggest buttons -->
            <div v-if="msg.suggestions" class="mt-2 flex flex-wrap gap-2">
              <button 
                v-for="sug in msg.suggestions" :key="sug"
                @click="sendSuggested(sug)"
                class="text-[11px] bg-emerald-50 text-[#075955] border border-emerald-100 px-2.5 py-1.5 rounded-full hover:bg-[#075955] hover:text-white transition-colors text-left leading-tight"
              >
                {{ sug }}
              </button>
            </div>
            
            <!-- Loader khi đang gõ -->
            <div v-if="msg.isLoading" class="flex gap-1 items-center py-1">
               <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce"></span>
               <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></span>
               <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="p-3 bg-white border-t border-gray-100 shrink-0">
        <form @submit.prevent="sendMessage" class="flex items-center gap-2 bg-slate-50 border border-gray-200 rounded-full p-1 pl-4 focus-within:border-[#075955] focus-within:ring-2 focus-within:ring-[#075955]/20 transition-all">
          <input 
            v-model="inputText" 
            type="text" 
            placeholder="Nhập yêu cầu tìm vé..." 
            class="flex-1 bg-transparent border-none outline-none text-sm text-gray-700 placeholder-gray-400"
            :disabled="isWaiting"
          />
          <button 
            type="submit" 
            :disabled="!inputText.trim() || isWaiting"
            :class="['w-8 h-8 rounded-full flex items-center justify-center transition-colors', inputText.trim() && !isWaiting ? 'bg-[#075955] text-white shadow-md' : 'bg-gray-200 text-gray-400']"
          >
            <span class="material-symbols-outlined text-sm">send</span>
          </button>
        </form>
      </div>
    </div>

    <!-- Floating Buttons Container -->
    <div class="flex items-center gap-4">
      <!-- Live Chat Zalo Button -->
      <div class="relative group">
        <a 
          href="https://zalo.me/0367093771" 
          target="_blank"
          class="w-14 h-14 bg-white rounded-full shadow-2xl flex items-center justify-center transition-all duration-300 transform hover:scale-110 active:scale-95 z-50 hover:bg-gray-50 border border-gray-100"
        >
          <!-- Logo Zalo CSS (Không bao giờ bị lỗi ảnh) -->
          <div class="w-10 h-10 bg-[#0068ff] rounded-full flex items-center justify-center shadow-inner">
            <span class="text-white font-bold text-[13px] tracking-tight">Zalo</span>
          </div>
          <!-- Notification dot -->
          <span class="absolute top-0 right-0 w-3.5 h-3.5 bg-rose-500 border-2 border-white rounded-full animate-pulse"></span>
        </a>
        <!-- Tooltip -->
        <div class="absolute -top-10 left-1/2 -translate-x-1/2 bg-gray-800 text-white text-xs px-3 py-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
          Chat qua Zalo
        </div>
      </div>

      <!-- AI Chatbot Floating Button -->
      <div class="relative group">
        <button 
          @click="toggleChat"
          :class="['w-14 h-14 rounded-full shadow-2xl flex items-center justify-center transition-all duration-300 transform hover:scale-110 active:scale-95 z-50', isOpen ? 'bg-white text-[#075955] rotate-90 border border-gray-200' : 'bg-[#075955] text-white hover:bg-[#0a7a75]']"
        >
          <span class="material-symbols-outlined text-3xl transition-transform duration-300">{{ isOpen ? 'expand_more' : 'smart_toy' }}</span>
          <!-- Notification dot -->
          <span v-if="!isOpen && hasUnread" class="absolute top-0 right-0 w-3.5 h-3.5 bg-rose-500 border-2 border-white rounded-full animate-pulse"></span>
        </button>
        <!-- Tooltip -->
        <div v-if="!isOpen" class="absolute -top-10 left-1/2 -translate-x-1/2 bg-gray-800 text-white text-xs px-3 py-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
          Trợ lý AI
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from '../composables/useApi';
import { useAuthStore } from '@/stores/auth';
import { useHomeSummary } from '@/composables/useHomeSummary';

const router = useRouter();
const api = useApi();
const authStore = useAuthStore();
const { getHomeSummary } = useHomeSummary();

const isOpen = ref(false);
const hasUnread = ref(true);
const inputText = ref('');
const isWaiting = ref(false);
const messagesContainer = ref(null);

const sessionId = ref('');
const messages = ref([]);
const suggestionsLoaded = ref(false);

onMounted(async () => {
  // 1. Phục hồi Session ID
  const savedSession = localStorage.getItem('smartbus_chat_session');
  if (savedSession) {
    sessionId.value = savedSession;
  } else {
    sessionId.value = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    localStorage.setItem('smartbus_chat_session', sessionId.value);
  }

  // 2. Phục hồi tin nhắn
  const savedMessages = localStorage.getItem('smartbus_chat_messages');
  if (savedMessages) {
    try {
      messages.value = JSON.parse(savedMessages);
    } catch (e) {
      resetToWelcome();
    }
  } else {
    resetToWelcome();
  }

});

const formatText = (text) => {
  if (!text) return '';
  // Format bold
  let html = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  // Format image: ![alt](url)
  html = html.replace(/!\[([^\]]+)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="w-44 h-44 mx-auto my-2 rounded-xl shadow-sm border border-gray-200" />');
  return html;
};

const resetToWelcome = () => {
  messages.value = [{ 
    isBot: true, 
    text: 'Xin chào! Tôi là trợ lý AI của SmartBus Trung Nam. Đang tải các tuyến đường phổ biến...',
    suggestions: []
  }];
};

const loadSuggestions = async () => {
  try {
    const summary = await getHomeSummary();
    const trips = summary.uniqueTrips || [];
    if (trips.length > 0) {
      // Lọc ra các tuyến đường độc nhất
      const uniqueRoutes = [];
      const seen = new Set();
      
      trips.forEach(t => {
        let fromCity = t.departurePoint.split(',').pop().trim();
        let toCity = t.arrivalPoint.split(',').pop().trim();
        
        // Chuẩn hóa tên thành phố để AI dễ hiểu
        if (fromCity.includes("Hồ Chí Minh")) fromCity = "Hồ Chí Minh";
        if (toCity.includes("Hồ Chí Minh")) toCity = "Hồ Chí Minh";
        if (fromCity.includes("Đà Nẵng")) fromCity = "Đà Nẵng";
        if (toCity.includes("Đà Nẵng")) toCity = "Đà Nẵng";
        
        const routeKey = fromCity + '-' + toCity;
        if (!seen.has(routeKey)) {
          seen.add(routeKey);
          uniqueRoutes.push({ from: fromCity, to: toCity });
        }
      });
      
      // Xáo trộn mảng (Shuffle)
      uniqueRoutes.sort(() => 0.5 - Math.random());
      
      const promptTemplates = [
        (r) => `Tìm vé ${r.from} đi ${r.to}`,
        (r) => `Có chuyến ${r.from} đi ${r.to} ngày mai không?`,
        (r) => `Từ ${r.from} đi ${r.to} xe giường nằm`,
        (r) => `Giá vé đi ${r.to} rẻ nhất`,
        (r) => `Tìm chuyến sớm nhất đi ${r.to}`
      ];
      
      let allSuggestions = [];
      
      // 1. Tạo 4 gợi ý tìm tuyến đường từ DB
      uniqueRoutes.slice(0, 4).forEach(r => {
        const template = promptTemplates[Math.floor(Math.random() * promptTemplates.length)];
        allSuggestions.push(template(r));
      });
      
      // 2. Tạo 2 gợi ý động dựa vào Hãng xe và Loại xe thực tế trong DB
      const uniqueCompanies = [...new Set(trips.map(t => t.companyName))].filter(Boolean);
      const uniqueBusTypes = [...new Set(trips.map(t => t.busType))].filter(Boolean);
      
      if (uniqueCompanies.length > 0) {
        uniqueCompanies.sort(() => 0.5 - Math.random());
        const targetCity = uniqueRoutes[0]?.to || "Hồ Chí Minh";
        allSuggestions.push(`Vé đi ${targetCity} của nhà xe ${uniqueCompanies[0]}`);
      }
      
      if (uniqueBusTypes.length > 0) {
        uniqueBusTypes.sort(() => 0.5 - Math.random());
        const targetCity = uniqueRoutes[0]?.to || "Hồ Chí Minh";
        allSuggestions.push(`Tìm vé đi ${targetCity} bằng xe ${uniqueBusTypes[0]}`);
      }
      
      // 3. Thêm 2 gợi ý tính năng cá nhân/FAQ
      if (authStore.isAuthenticated) {
        const userActions = ["Gửi mã vé xe sắp tới", "Xe của tôi bao giờ chạy?", "Tôi muốn hủy vé", "Vé của tôi đâu"];
        userActions.sort(() => 0.5 - Math.random());
        allSuggestions.push(userActions[0], userActions[1]);
      } else {
        const faqs = ["Quy định hành lý tối đa", "Nhà xe có các loại xe nào?", "Chính sách hủy vé ra sao?", "Hướng dẫn thanh toán"];
        faqs.sort(() => 0.5 - Math.random());
        allSuggestions.push(faqs[0], faqs[1]);
      }
      
      // Xáo trộn lại toàn bộ 8 gợi ý
      allSuggestions.sort(() => 0.5 - Math.random());
      
      // Giới hạn hiển thị 8 gợi ý
      allSuggestions = allSuggestions.slice(0, 8);
      
      if (messages.value.length === 1 && messages.value[0].isBot) {
        messages.value[0].text = 'Xin chào! Tôi là trợ lý AI của SmartBus Trung Nam. Tôi có thể giúp gì cho bạn?';
        messages.value[0].suggestions = allSuggestions;
        saveMessages();
      }
    }
    suggestionsLoaded.value = true;
  } catch (error) {
    console.error('Không thể tải gợi ý tuyến đường:', error);
    if (messages.value.length === 1 && messages.value[0].isBot) {
      messages.value[0].text = 'Xin chào! Tôi là trợ lý AI của SmartBus Trung Nam. Bạn cần tìm vé xe đi đâu?';
      messages.value[0].suggestions = ['Tìm vé Sài Gòn đi Đà Lạt', 'Từ Đà Nẵng đi Nha Trang'];
      suggestionsLoaded.value = true;
      saveMessages();
    }
  }
};

const clearChat = (force = false) => {
  if (!force && !confirm('Bạn có chắc chắn muốn xóa toàn bộ lịch sử trò chuyện?')) return;
  
  localStorage.removeItem('smartbus_chat_messages');
  localStorage.removeItem('smartbus_chat_session');
  
  sessionId.value = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
  localStorage.setItem('smartbus_chat_session', sessionId.value);
  
  resetToWelcome();
  suggestionsLoaded.value = false;
  if (isOpen.value) loadSuggestions();
};

// Theo dõi sự thay đổi tài khoản (Đăng nhập / Đăng xuất)
watch(() => authStore.currentUser?.id, (newId, oldId) => {
  if (newId !== oldId) {
    // Tài khoản thay đổi -> Tự động xóa lịch sử chat để bảo mật
    clearChat(true);
  }
});

const saveMessages = () => {
  localStorage.setItem('smartbus_chat_messages', JSON.stringify(messages.value));
};

const toggleChat = async () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    hasUnread.value = false;
    if (!suggestionsLoaded.value) await loadSuggestions();
    scrollToBottom();
  }
};

const openLiveChat = () => {
  alert('Giao diện Chat với nhà xe đang được tích hợp. Bạn muốn tích hợp qua Zalo hay xây dựng hệ thống chat nội bộ?');
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const sendSuggested = (text) => {
  inputText.value = text;
  sendMessage();
};

const navigateToBooking = (params, tripId = null) => {
  if (!params) return;
  isOpen.value = false; // Đóng chat
  
  const query = {
    from: params.from,
    to: params.to,
    date: params.date,
    sort: params.sort || 'default',
    busType: params.busType || 'all',
    company: params.company || 'all'
  };
  
  if (tripId) {
    query.tripId = tripId;
  }
  
  router.push({
    path: '/booking/search',
    query
  });
};

const sendMessage = async () => {
  if (!inputText.value.trim() || isWaiting.value) return;
  
  const userText = inputText.value.trim();
  messages.value.push({ isBot: false, text: userText });
  inputText.value = '';
  isWaiting.value = true;
  saveMessages();
  scrollToBottom();

  // Hiển thị loader của Bot
  messages.value.push({ isBot: true, isLoading: true });
  scrollToBottom();

  try {
    const response = await api.post('/ai/chat', { 
        message: userText,
        sessionId: sessionId.value
    });
    
    messages.value.pop(); // Bỏ loader
    messages.value.push({ 
      isBot: true, 
      text: response.data.text,
      tripsPreview: response.data.tripsPreview,
      params: response.data.params,
      action: response.data.action
    });
    saveMessages();
    
  } catch (error) {
    console.error('Lỗi AI:', error);
    messages.value.pop();
    messages.value.push({ 
      isBot: true, 
      text: 'Xin lỗi, hệ thống AI đang quá tải. Bạn vui lòng thử lại sau giây lát nhé!' 
    });
    saveMessages();
  } finally {
    isWaiting.value = false;
    scrollToBottom();
  }
};
</script>

<style scoped>
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px) scale(0.95); transform-origin: bottom right; }
  to { opacity: 1; transform: translateY(0) scale(1); transform-origin: bottom right; }
}
.animate-slide-up {
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
