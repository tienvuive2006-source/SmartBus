<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-800">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 h-16 flex items-center justify-between">
        
        <div class="flex items-center gap-8">
          <div class="flex items-center gap-2 cursor-pointer" @click="$router.push('/')">
            <span class="material-symbols-outlined text-white text-4xl">directions_bus</span>
            <div class="flex flex-col">
              <span class="text-xl font-bold leading-none tracking-tight">Trung - Nam</span>
              <span class="text-[9px] uppercase tracking-wider font-semibold">Nhà xe chuyên tuyến Miền Trung - Nam</span>
            </div>
          </div>
          
          <div class="hidden lg:flex items-center gap-5 text-[13px] font-semibold">
            <button @click="scrollToSection('searchSection')" class="hover:text-yellow-300 transition-colors flex items-center gap-1 bg-transparent border-none outline-none cursor-pointer text-white font-semibold">
              <span class="material-symbols-outlined text-[16px]">home</span> Mua vé
            </button>
            <button @click="openInfoModal('benxe')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Bến Xe</button>
            <button @click="openInfoModal('nhaxe')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Nhà Xe</button>
            <button @click="openInfoModal('diemden')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Điểm đến</button>
            <button @click="openInfoModal('thongtin')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Thông tin ngành vận tải</button>
            <button @click="scrollToSection('searchSection')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Các tuyến đường chính</button>
          </div>
        </div>

        <div class="flex items-center gap-6">
          <div class="hidden md:flex items-center gap-2 text-yellow-400 font-bold text-lg">
            1900.59.99.97
          </div>

          <!-- Component Chuông thông báo -->
          <NotificationBell />

          <button @click="$router.push('/profile')" class="flex items-center gap-2 text-sm font-semibold hover:text-yellow-300 transition-colors ml-2">
            <span class="material-symbols-outlined text-2xl">person_outline</span>
            {{ currentUser?.fullName || '' }}
          </button>
          <div class="flex items-center gap-1 cursor-pointer">
            <img src="https://flagcdn.com/w20/vn.png" alt="VN" class="w-5 h-3.5 object-cover" />
            <img src="https://flagcdn.com/w20/gb.png" alt="EN" class="w-5 h-3.5 object-cover opacity-50 hover:opacity-100" />
          </div>
        </div>
      </div>
    </nav>

    <header class="relative pt-16 pb-16 flex flex-col items-center justify-center min-h-[420px]">
      <div 
        class="absolute inset-0 bg-cover bg-center z-0"
        :style="`background-image: url('${heroBannerUrl}');`"
      ></div>

      <!-- Nút Đổi Ảnh Bìa (Chỉ dành cho Admin) -->
      <div v-if="authStore.isAdmin" class="absolute top-4 right-4 z-30">
        <label for="banner-upload" class="flex items-center gap-2 px-4 py-2 bg-white/20 hover:bg-white/40 backdrop-blur-md border border-white/30 text-white rounded-xl font-bold text-sm cursor-pointer transition-all shadow-lg hover:shadow-xl group">
          <span class="material-symbols-outlined text-[18px] group-hover:scale-110 transition-transform">add_a_photo</span>
          {{ isUploadingBanner ? 'Đang tải lên...' : 'Đổi Ảnh Bìa' }}
        </label>
        <input 
          id="banner-upload" 
          type="file" 
          accept="image/*" 
          class="hidden" 
          @change="handleBannerUpload" 
          :disabled="isUploadingBanner"
        />
      </div>
    </header>

    <div id="searchSection" class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 mb-4 mt-10">
      <!-- Section Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-200 pb-4 mb-6">
        <div class="flex flex-col sm:flex-row sm:items-center gap-3">
          <h3 class="text-xl font-black text-slate-800 tracking-tight">Các tuyến đường chính</h3>
          <span v-if="popularRoutes.length > 0" class="bg-[#075955]/10 text-[#075955] text-xs font-bold px-3 py-1 rounded-full border border-[#075955]/15 flex items-center gap-1.5 w-fit">
            <span class="material-symbols-outlined text-sm">directions_bus</span>
            Hiện có {{ popularRoutes.length }} tuyến đường
          </span>
        </div>
        <p class="text-slate-500 text-sm font-semibold italic md:text-right hidden sm:block">
          Nền tảng đặt xe uy tín - giá gốc 100% từ nhà xe
        </p>
      </div>

      <!-- Super Optimized Filter Bar -->
      <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2 text-slate-700 font-bold text-xs uppercase tracking-wider">
            <span class="material-symbols-outlined text-base">tune</span>
            Bộ lọc chuyến đi
          </div>
          <!-- Reset Filters Button -->
          <button 
            v-if="filterDate || filterFrom || filterTo || filterCompany"
            @click="clearAllFilters" 
            class="text-xs font-bold text-red-500 hover:text-red-600 flex items-center gap-1 bg-transparent border-none outline-none cursor-pointer hover:underline transition-colors"
          >
            <span class="material-symbols-outlined text-sm">filter_alt_off</span>
            Xóa bộ lọc
          </button>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          <!-- Filter Date -->
          <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
            <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">calendar_month</span>
            <div class="flex-1 text-left">
              <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Ngày đi</label>
              <input type="date" v-model="filterDate" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0" />
            </div>
          </div>

          <!-- Filter From -->
          <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
            <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">location_on</span>
            <div class="flex-1 text-left">
              <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Nơi đi</label>
              <select v-model="filterFrom" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0 appearance-none">
                <option value="">Tất cả điểm đi</option>
                <option v-for="loc in allDeparturePoints" :key="loc" :value="loc">{{ simplifyLocation(loc) }}</option>
              </select>
            </div>
            <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
          </div>

          <!-- Filter To -->
          <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
            <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">pin_drop</span>
            <div class="flex-1 text-left">
              <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Nơi đến</label>
              <select v-model="filterTo" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0 appearance-none">
                <option value="">Tất cả điểm đến</option>
                <option v-for="loc in allArrivalPoints" :key="loc" :value="loc">{{ simplifyLocation(loc) }}</option>
              </select>
            </div>
            <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
          </div>

          <!-- Filter Company -->
          <div class="flex items-center gap-2 bg-slate-50 border border-slate-200/80 rounded-xl px-3 py-2 text-sm focus-within:ring-2 focus-within:ring-[#075955]/20 focus-within:border-[#075955] transition-all">
            <span class="material-symbols-outlined text-[#075955] text-lg shrink-0">directions_bus</span>
            <div class="flex-1 text-left">
              <label class="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Hãng xe</label>
              <select v-model="filterCompany" class="w-full bg-transparent border-none outline-none font-bold text-slate-700 text-xs cursor-pointer p-0 m-0 appearance-none">
                <option value="">Tất cả hãng xe</option>
                <option v-for="company in uniqueCompanies" :key="company.name" :value="company.name">{{ company.name }}</option>
              </select>
            </div>
            <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
          </div>
        </div>
      </div>
    </div>

    <main v-if="popularRoutes.length > 0" class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 pb-8">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 gap-y-8 md:gap-8">
        <div v-for="route in popularRoutes" :key="route.id" @click="quickSearch(route.from, route.to, '')" class="group rounded-2xl overflow-hidden hover:shadow-2xl hover:-translate-y-1.5 transition-all duration-300 cursor-pointer flex flex-col shadow-md">
          <div class="relative h-56 sm:h-48 md:h-56 overflow-hidden bg-slate-200">
            <template v-if="route.image">
              <img 
                :src="route.image" 
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ease-out"
                @error="(e) => e.target.style.display = 'none'"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
            </template>
            <template v-else>
              <div class="w-full h-full flex flex-col items-center justify-center text-slate-400 group-hover:scale-105 transition-transform duration-500 ease-out">
                <span class="material-symbols-outlined text-4xl mb-2 opacity-30">directions_bus</span>
                <span class="text-xs font-semibold uppercase tracking-wider opacity-60">Chưa có ảnh</span>
              </div>
              <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"></div>
            </template>

            <div class="absolute bottom-3 left-4 right-4 text-white">
              <h3 class="font-bold text-[17px] truncate" :title="route.customName || `${route.shortFrom} ➝ ${route.shortTo}`">
                {{ route.customName || `${route.shortFrom} ➝ ${route.shortTo}` }}
              </h3>
            </div>
          </div>

        </div>
      </div>
    </main>


    <!-- Khu vực "Khách hàng nói gì về chúng tôi" -->
    <div v-if="topReviews.length > 0" class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 py-16">
      <div class="text-center mb-12">
        <h2 class="text-2xl md:text-3xl font-black text-slate-800 tracking-tight">Khách hàng nói gì về Trung Nam?</h2>
        <p class="text-slate-500 font-medium mt-2 text-sm md:text-base">Đánh giá chân thực từ những hành khách đã trải nghiệm dịch vụ</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8">
        <div v-for="review in topReviews" :key="review.id" class="bg-white rounded-3xl p-6 lg:p-8 shadow-sm border border-slate-100 relative hover:shadow-lg transition-all duration-300">
          <!-- Quote Icon -->
          <span class="material-symbols-outlined text-[#075955]/10 text-6xl absolute top-4 right-4 z-0">format_quote</span>
          
          <!-- Rating -->
          <div class="flex gap-1 text-amber-400 mb-4 relative z-10">
            <span v-for="i in review.rating" :key="i" class="material-symbols-outlined text-xl" style="font-variation-settings: 'FILL' 1;">star</span>
          </div>

          <!-- Comment -->
          <p class="text-slate-600 font-medium italic leading-relaxed mb-6 min-h-[80px] relative z-10">
            "{{ review.comment }}"
          </p>

          <!-- User Info -->
          <div class="flex items-center gap-4 mt-auto border-t border-slate-100 pt-4 relative z-10">
            <div class="w-12 h-12 rounded-full bg-gradient-to-br from-emerald-100 to-teal-50 flex items-center justify-center font-bold text-teal-700 border border-teal-100">
              {{ review.user?.fullName?.charAt(0)?.toUpperCase() || 'K' }}
            </div>
            <div>
              <p class="font-bold text-slate-800">{{ review.user?.fullName || 'Khách hàng ẩn danh' }}</p>
              <p v-if="review.booking?.trip" class="text-xs text-slate-400 mt-0.5">
                Đã đi tuyến: <span class="font-semibold text-slate-500">{{ review.booking.trip.departurePoint?.split(',')[0] }} ➝ {{ review.booking.trip.arrivalPoint?.split(',')[0] }}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Info Modal for Bến Xe, Nhà Xe, Điểm Đến, Thông Tin (ĐỒNG BỘ ADMIN ĐĂNG) -->
    <div v-if="showInfoModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- Backdrop with smooth blur -->
      <div @click="showInfoModal = false" class="absolute inset-0 bg-slate-900/60 backdrop-blur-md transition-opacity duration-300"></div>
      
      <!-- Modal Box -->
      <div class="relative bg-white/95 backdrop-blur-lg rounded-[32px] shadow-2xl border border-slate-100 max-w-2xl w-full overflow-hidden z-10 transform transition-all duration-300 scale-100 p-8 flex flex-col gap-6">
        <!-- Close Button -->
        <button @click="showInfoModal = false" class="absolute top-6 right-6 w-10 h-10 rounded-full hover:bg-slate-100 flex items-center justify-center transition-all bg-transparent border-none cursor-pointer">
          <span class="material-symbols-outlined text-slate-500">close</span>
        </button>

        <!-- Modal Header -->
        <div class="flex items-center gap-4 border-b border-slate-100 pb-4">
          <div class="w-12 h-12 bg-[#075955]/10 text-[#075955] rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl">
              {{ activeModalType === 'benxe' ? 'map' : (activeModalType === 'nhaxe' ? 'directions_bus' : (activeModalType === 'diemden' ? 'explore' : 'newspaper')) }}
            </span>
          </div>
          <div class="text-left">
            <h3 class="text-xl font-black text-slate-800 tracking-tight uppercase">
              {{ activeModalType === 'benxe' ? 'Hệ Thống Bến Xe (Admin đăng)' : (activeModalType === 'nhaxe' ? 'Danh Sách Nhà Xe (Admin đăng)' : (activeModalType === 'diemden' ? 'Các Điểm Đến Phổ Biến' : 'Thông Tin Ngành Vận Tải')) }}
            </h3>
            <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5">Dữ liệu đồng bộ trực tiếp từ hệ thống</p>
          </div>
        </div>

        <!-- Modal Body Content -->
        <div class="text-slate-600 text-sm leading-relaxed overflow-y-auto max-h-[380px] pr-2 space-y-4 text-left">
          
          <!-- BẾN XE DYNAMIC LIST -->
          <div v-if="activeModalType === 'benxe'" class="space-y-4">
            <!-- Hướng dẫn thông minh bằng Banner -->
            <div class="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-start gap-2.5 text-xs font-semibold">
              <span class="material-symbols-outlined text-emerald-600 mt-0.5">info</span>
              <div>
                <p class="m-0 text-emerald-900 font-bold">💡 Bộ lọc nhanh Trang Chủ</p>
                <p class="text-slate-500 font-normal mt-1 mb-0">
                  Bấm nút bên dưới để lập tức lọc danh sách <strong>Các tuyến đường chính</strong> tại trang chủ theo bến xe bạn chọn!
                </p>
              </div>
            </div>

            <p class="font-semibold text-slate-800">
              Dưới đây là các bến xe/điểm dừng thực tế đang hoạt động được Admin cập nhật trên hệ thống:
            </p>
            <div v-if="uniqueStations.length === 0" class="text-center py-8 text-gray-400">
              <span class="material-symbols-outlined text-4xl block mb-2">info</span>
              Chưa có dữ liệu bến xe nào do Admin đăng.
            </div>
            <ul v-else class="space-y-3">
              <li v-for="station in uniqueStations" :key="station.name" class="flex flex-col sm:flex-row sm:items-center justify-between p-4 bg-slate-50 hover:bg-[#075955]/5 rounded-2xl border border-slate-100 transition-colors gap-3">
                <div class="flex items-start gap-3">
                  <span class="material-symbols-outlined text-[#075955] mt-0.5">location_on</span>
                  <div>
                    <strong class="text-slate-800 block text-base">{{ station.name }}</strong>
                    <div class="flex gap-4 mt-1">
                      <span class="text-xs text-gray-500 font-medium">🛫 Nơi đi: {{ station.depCount }} chuyến</span>
                      <span class="text-xs text-gray-500 font-medium">🛬 Nơi đến: {{ station.arrCount }} chuyến</span>
                    </div>
                  </div>
                </div>
                <div class="flex gap-2 shrink-0">
                  <button @click="selectStation(station.name, 'from')" :class="['px-3 py-1.5 rounded-lg font-bold text-xs uppercase transition-colors cursor-pointer border-none', filterFrom === station.name ? 'bg-[#075955] text-white' : 'bg-[#075955]/10 text-[#075955] hover:bg-[#075955] hover:text-white']">
                    Lọc Nơi Đi
                  </button>
                  <button @click="selectStation(station.name, 'to')" :class="['px-3 py-1.5 rounded-lg font-bold text-xs uppercase transition-colors cursor-pointer border-none', filterTo === station.name ? 'bg-[#075955] text-white' : 'bg-[#075955]/10 text-[#075955] hover:bg-[#075955] hover:text-white']">
                    Lọc Nơi Đến
                  </button>
                </div>
              </li>
            </ul>
          </div>

          <!-- NHÀ XE DYNAMIC LIST -->
          <div v-if="activeModalType === 'nhaxe'" class="space-y-4">
            <p class="font-semibold text-slate-800">
              Danh sách các hãng xe đang vận hành các chuyến đi thực tế do Admin đăng trên hệ thống:
            </p>
            <div v-if="uniqueCompanies.length === 0" class="text-center py-8 text-gray-400">
              <span class="material-symbols-outlined text-4xl block mb-2">info</span>
              Chưa có dữ liệu nhà xe nào do Admin đăng.
            </div>
            <ul v-else class="space-y-3">
              <li v-for="company in uniqueCompanies" :key="company.name" class="flex flex-col sm:flex-row sm:items-center justify-between p-4 bg-slate-50 hover:bg-[#075955]/5 rounded-2xl border border-slate-100 transition-colors gap-3">
                <div class="flex items-start gap-3">
                  <span class="material-symbols-outlined text-[#075955] mt-0.5 text-base">directions_bus</span>
                  <div>
                    <strong class="text-slate-800 block text-base">{{ company.name }}</strong>
                    <div class="flex gap-4 mt-1">
                      <span class="text-xs text-gray-500 font-medium">🛣️ Số chuyến đang chạy: {{ company.count }}</span>
                      <span class="text-xs text-gray-500 font-medium">💰 Giá vé từ: {{ company.minPrice?.toLocaleString() }}đ</span>
                    </div>
                  </div>
                </div>
                <button @click="selectCompany(company.name)" class="px-4 py-2 bg-[#075955] hover:bg-[#05403d] text-white rounded-lg font-bold text-xs uppercase transition-colors cursor-pointer border-none shrink-0">
                  Lọc nhà xe
                </button>
              </li>
            </ul>
          </div>

          <!-- DIEM DEN DYNAMIC LIST -->
          <div v-if="activeModalType === 'diemden'" class="space-y-4">
            <p class="font-semibold text-slate-800">Khám phá các điểm đến phổ biến trên lộ trình chuyên tuyến của Nhà xe Trung - Nam:</p>
            <div class="grid grid-cols-2 gap-4">
              <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
                <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Đà Nẵng</strong>
                <p class="text-xs text-slate-500">Thành phố đáng sống nhất Việt Nam với Cầu Vàng Bà Nà Hills, bán đảo Sơn Trà và bãi biển Mỹ Khê tuyệt đẹp.</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
                <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Quy Nhơn</strong>
                <p class="text-xs text-slate-500">Xứ nẫu thanh bình với Kỳ Co, Eo Gió thơ mộng và những tháp Chăm cổ kính nhuốm màu thời gian.</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
                <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Nha Trang</strong>
                <p class="text-xs text-slate-500">Vịnh biển thiên đường với VinWonders, đảo Hòn Tre cát trắng và thế giới san hô đa sắc màu.</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left">
                <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Sài Gòn</strong>
                <p class="text-xs text-slate-500">Đô thị sầm uất, sôi động ngày đêm với các điểm check-in lịch sử và trung tâm ẩm thực độc đáo.</p>
              </div>
            </div>
          </div>

          <!-- THONG TIN DYNAMIC LIST -->
          <div v-if="activeModalType === 'thongtin'" class="space-y-4">
            <p class="font-semibold text-slate-800">Cập nhật các quy định, thông tin an toàn đường bộ và cẩm nang đi xe mới nhất:</p>
            <ul class="space-y-3">
              <li class="p-4 bg-[#075955]/5 rounded-2xl border border-[#075955]/10 text-left">
                <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Quy định về hành lý ký gửi</strong>
                <p class="text-xs text-slate-600">Mỗi hành khách được miễn cước tối đa 20kg hành lý. Hành lý quá khổ hoặc chất lỏng nguy hại phải được thông báo trước để đóng gói an toàn theo quy định ngành vận tải và giao thông đường bộ.</p>
              </li>
              <li class="p-4 bg-[#075955]/5 rounded-2xl border border-[#075955]/10 text-left">
                <strong class="text-[#075955] text-xs uppercase tracking-wider block mb-1">Chính sách bảo hiểm hành khách</strong>
                <p class="text-xs text-slate-600">100% vé bán ra từ hệ thống trực tuyến Nhà xe Trung - Nam đều đã bao gồm phí bảo hiểm tai nạn giao thông đường bộ mức đền bù tối đa 100.000.000đ/vụ.</p>
              </li>
            </ul>
          </div>

        </div>

        <!-- Modal Footer -->
        <div class="flex justify-end gap-3 border-t border-slate-100 pt-4">
          <button @click="showInfoModal = false" class="px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl font-bold text-xs uppercase tracking-widest transition-colors border-none cursor-pointer">
            Đóng lại
          </button>
          <button @click="scrollToSection('searchSection'); showInfoModal = false" class="px-6 py-2.5 bg-[#075955] hover:bg-[#05403d] text-white rounded-xl font-bold text-xs uppercase tracking-widest transition-colors flex items-center gap-2 border-none cursor-pointer">
            <span class="material-symbols-outlined text-sm">directions_bus</span> Đặt vé ngay
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import AppFooter from '@/components/AppFooter.vue';
import NotificationBell from '@/components/NotificationBell.vue';
import { useLocationSearch } from '@/composables/useLocationSearch';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

const router = useRouter();
const authStore = useAuthStore();
const api = useApi();
const currentUser = authStore.currentUser; // reactive computed
const trips = ref([]);
const fromQuery = ref('');
const toQuery = ref('');
const dateQuery = ref(new Date().toISOString().split('T')[0]);
const busCompanyQuery = ref('all'); // Khai báo thêm cho trường 'Nhà xe yêu thích'

// Bộ lọc tại danh sách hàng ngày
const filterDate = ref('');
const filterFrom = ref('');
const filterTo = ref('');
const filterCompany = ref('');

const clearAllFilters = () => {
  filterDate.value = '';
  filterFrom.value = '';
  filterTo.value = '';
  filterCompany.value = '';
};

// 🚀 CHUẨN ĐỒ ÁN: Quản lý xem chi tiết thông tin nhà xe, bến xe, điểm đến qua modal (ĐỒNG BỘ TỪ ADMIN ĐĂNG)
const showInfoModal = ref(false);
const activeModalType = ref(''); // '', 'benxe', 'nhaxe', 'diemden', 'thongtin'

const uniqueStations = computed(() => {
  if (!trips.value) return [];
  const stations = [...new Set([...trips.value.map(t => t.departurePoint), ...trips.value.map(t => t.arrivalPoint)])].filter(Boolean);
  return stations.map(name => {
    const depCount = trips.value.filter(t => t.departurePoint === name).length;
    const arrCount = trips.value.filter(t => t.arrivalPoint === name).length;
    return { name, depCount, arrCount };
  });
});

const uniqueCompanies = computed(() => {
  if (!trips.value) return [];
  const companies = [...new Set(trips.value.map(t => t.companyName))].filter(Boolean);
  return companies.map(name => {
    const companyTrips = trips.value.filter(t => t.companyName === name);
    const minPrice = companyTrips.length > 0 ? Math.min(...companyTrips.map(t => t.price || 0)) : 0;
    return { name, count: companyTrips.length, minPrice };
  });
});

const scrollToSection = (id) => {
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

const openInfoModal = (type) => {
  activeModalType.value = type;
  showInfoModal.value = true;
};

const selectStation = (stationName, role) => {
  if (role === 'from') {
    filterFrom.value = stationName;
    filterTo.value = ''; // Xoá điểm đến để hiện tất cả chuyến đi từ bến này
  } else {
    filterTo.value = stationName;
    filterFrom.value = ''; // Xoá điểm đi để hiện tất cả chuyến đến bến này
  }
  showInfoModal.value = false;
  scrollToSection('scheduleSection');
};

const selectCompany = (companyName) => {
  filterCompany.value = companyName;
  showInfoModal.value = false;
  scrollToSection('scheduleSection');
};

const { 
  suggestions: fromSuggestions, 
  showDropdown: showFromDropdown, 
  handleFocus: onFromFocus, 
  handleSelect: fromSelect,
  performSearch: searchFrom,
  closeDropdown: closeFrom
} = useLocationSearch();

const { 
  suggestions: toSuggestions, 
  showDropdown: showToDropdown, 
  handleFocus: onToFocus, 
  handleSelect: toSelect,
  performSearch: searchTo,
  closeDropdown: closeTo
} = useLocationSearch();

watch(fromQuery, (val) => searchFrom(val));
watch(toQuery, (val) => searchTo(val));

const selectFromLocation = (loc) => fromSelect(loc, (val) => fromQuery.value = val);
const selectToLocation = (loc) => toSelect(loc, (val) => toQuery.value = val);

const fetchTrips = async () => {
  try {
    const response = await api.get('/trips');
    trips.value = response.data.filter(t => t.isVisible !== false);
  } catch (err) { console.error(err); }
};

const fromContainer = ref(null);
const toContainer = ref(null);
const handleClickOutside = (event) => {
  if (fromContainer.value && !fromContainer.value.contains(event.target)) closeFrom();
  if (toContainer.value && !toContainer.value.contains(event.target)) closeTo();
};

function handleSearch() {
  if (!fromQuery.value || !toQuery.value) return alert('Vui lòng chọn điểm đi/đến!');
  // Có thể truyền thêm busCompanyQuery vào nếu BE hỗ trợ
  router.push({ path: '/booking/search', query: { from: fromQuery.value, to: toQuery.value, date: dateQuery.value, company: busCompanyQuery.value } });
}

const simplifyLocation = (loc) => {
  if (!loc) return '';
  const parts = loc.split(',');
  let s = parts[parts.length - 1].trim();
  s = s.replace(/\b(Thành phố|TP|Tỉnh|Hà Nội|Hồ Chí Minh|Đà Nẵng|Cần Thơ|Hải Phòng)\b/gi, (match) => {
    if (match.toLowerCase() === 'thành phố' || match.toLowerCase() === 'tp' || match.toLowerCase() === 'tỉnh') return '';
    return match;
  }).trim();
  if (!s) s = parts[0].replace(/\b(Bến xe|Phường|Quận|Huyện|Xã|TT)\b/gi, '').trim();
  return s;
};

const quickSearch = (from, to, date) => {
  fromQuery.value = from;
  toQuery.value = to;
  if (date) {
    dateQuery.value = date.split('T')[0];
  } else {
    dateQuery.value = '';
  }
  handleSearch();
};

const allDeparturePoints = computed(() => {
  const points = [...new Set(trips.value.map(t => t.departurePoint))];
  return points.sort();
});

const allArrivalPoints = computed(() => {
  const points = [...new Set(trips.value.map(t => t.arrivalPoint))];
  return points.sort();
});

const debugRoutes = ref([]);
const savedRoutes = ref([]);
const topReviews = ref([]);

const fetchSavedRoutes = async () => {
  try {
    const response = await api.get('/routes');
    savedRoutes.value = response.data;
  } catch (err) { console.error(err); }
};

// Counter tăng mỗi 3 giây → force popularRoutes recompute và đọc localStorage mới nhất
const _syncCounter = ref(0);

const popularRoutes = computed(() => {
  // Hàm chuẩn hóa loại bỏ hoàn toàn dấu, khoảng trắng và ký tự đặc biệt
  const normalizeKey = (s) => {
    if (!s) return '';
    return s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]/g, '');
  };

  const rawImagesMap = JSON.parse(localStorage.getItem('smartbus_route_images') || '{}');
  const routeImagesMap = {};
  for (const k in rawImagesMap) {
    const parts = k.split('||');
    if (parts.length === 2) {
      routeImagesMap[`${normalizeKey(parts[0])}||${normalizeKey(parts[1])}`] = rawImagesMap[k];
    }
  }
  
  const routeNamesMap = {};
  
  savedRoutes.value.forEach(r => {
    const sd = normalizeKey(r.departurePoint.split(',')[0]);
    const sa = normalizeKey(r.arrivalPoint.split(',')[0]);
    
    if (r.imageUrl) {
      routeImagesMap[`${sd}||${sa}`] = r.imageUrl;
    }
    
    // Lưu tên tuỳ chỉnh (bỏ qua tên sinh tự động có dấu ➔)
    if (r.name && !r.name.includes('➔')) {
      routeNamesMap[`${sd}||${sa}`] = r.name;
    }
  });
  
  let filtered = [...trips.value];
  if (filterFrom.value) filtered = filtered.filter(t => t.departurePoint === filterFrom.value);
  if (filterTo.value) filtered = filtered.filter(t => t.arrivalPoint === filterTo.value);
  if (filterCompany.value) filtered = filtered.filter(t => t.companyName === filterCompany.value);
  if (filterDate.value) filtered = filtered.filter(t => t.departureDate?.split('T')[0] === filterDate.value);

  const routes = [];
  const seen = new Set();

  const getCityName = (loc) => {
    if (!loc) return '';
    if (loc.includes('Hồ Chí Minh')) return 'Hồ Chí Minh';
    if (loc.includes('Hà Nội')) return 'Hà Nội';
    if (loc.includes('Đà Nẵng')) return 'Đà Nẵng';
    if (loc.includes('Quy Nhơn')) return 'Quy Nhơn';
    if (loc.includes('Nha Trang')) return 'Nha Trang';
    if (loc.includes('Đà Lạt')) return 'Đà Lạt';
    if (loc.includes('Cần Thơ')) return 'Cần Thơ';
    if (loc.includes('Hải Phòng')) return 'Hải Phòng';
    if (loc.includes('Vũng Tàu')) return 'Vũng Tàu';
    
    let city = loc.replace(/Bến xe Trung tâm/i, '')
                  .replace(/Bến xe Phía Nam/i, '')
                  .replace(/Bến xe Phía Bắc/i, '')
                  .replace(/Bến xe Liên tỉnh/i, '')
                  .replace(/Bến xe/i, '')
                  .trim();
                  
    if (city.includes(',')) {
      const parts = city.split(',');
      city = parts[parts.length - 1].trim();
    }
    return city;
  };

  filtered.sort((a, b) => b.id - a.id).forEach(t => {
    const key = `${t.departurePoint}-${t.arrivalPoint}`;
    if (!seen.has(key)) {
      seen.add(key);
      const rawShortFrom = t.departurePoint.split(',')[0].trim();
      const rawShortTo = t.arrivalPoint.split(',')[0].trim();
      
      const targetKey = `${normalizeKey(rawShortFrom)}||${normalizeKey(rawShortTo)}`;
      
      // Fuzzy tìm kiếm trong routeImagesMap (kể cả chứa một phần tên)
      let matchedImageUrl = routeImagesMap[targetKey];
      if (!matchedImageUrl) {
        // Tìm gần đúng: nếu targetKey chứa 80% key trong map
        for (const mapKey in routeImagesMap) {
          const [mFrom, mTo] = mapKey.split('||');
          if (mFrom && mTo && normalizeKey(rawShortFrom).includes(mFrom) && normalizeKey(rawShortTo).includes(mTo)) {
            matchedImageUrl = routeImagesMap[mapKey];
            break;
          }
        }
      }
      
      const displayFrom = getCityName(t.departurePoint);
      const displayTo = getCityName(t.arrivalPoint);
      
      const routeImageUrl = matchedImageUrl || t.imageUrl || '';
      
      // Fuzzy tìm kiếm trong routeNamesMap
      let matchedName = routeNamesMap[targetKey];
      if (!matchedName) {
        for (const mapKey in routeNamesMap) {
          const [mFrom, mTo] = mapKey.split('||');
          if (mFrom && mTo && normalizeKey(rawShortFrom).includes(mFrom) && normalizeKey(rawShortTo).includes(mTo)) {
            matchedName = routeNamesMap[mapKey];
            break;
          }
        }
      }

      routes.push({ 
        id: t.id, 
        from: t.departurePoint, 
        to: t.arrivalPoint, 
        shortFrom: displayFrom,
        shortTo: displayTo,
        date: t.departureDate,
        price: t.ticketPrice,
        busType: t.busType,
        image: routeImageUrl,
        customName: matchedName || null
      });
    }
  });
  return routes;
});

const currentUser2 = ref(null);
let _syncInterval = null;

const heroBannerUrl = ref(localStorage.getItem('cached_hero_banner') || '');
const isUploadingBanner = ref(false);

const fetchSettings = async () => {
  try {
    // Tải Banner
    const resBanner = await api.get('/settings/hero_banner_url');
    if (resBanner.data && resBanner.data.value) {
      heroBannerUrl.value = resBanner.data.value;
      localStorage.setItem('cached_hero_banner', resBanner.data.value);
    } else if (!heroBannerUrl.value) {
      heroBannerUrl.value = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?q=80&w=2069&auto=format&fit=crop';
    }
  } catch (err) {
    console.error("Lỗi khi tải cài đặt:", err);
    if (!heroBannerUrl.value) {
      heroBannerUrl.value = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?q=80&w=2069&auto=format&fit=crop';
    }
  }
};

const handleBannerUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  isUploadingBanner.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'skybus_preset');

    // 1. Upload to Cloudinary
    const cloudinaryRes = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    const newUrl = cloudinaryRes.data.secure_url;

    // 2. Save to Backend
    await api.put('/settings/hero_banner_url', { value: newUrl });

    // 3. Update UI
    heroBannerUrl.value = newUrl;
    alert("Cập nhật ảnh bìa thành công!");
  } catch (err) {
    console.error("Lỗi upload ảnh bìa:", err);
    alert("Cập nhật ảnh bìa thất bại! Vui lòng thử lại.");
  } finally {
    isUploadingBanner.value = false;
    event.target.value = '';
  }
};

const fetchTopReviews = async () => {
  try {
    const res = await api.get('/reviews/all');
    if (res.data && Array.isArray(res.data)) {
      topReviews.value = res.data
        .filter(r => r.rating >= 4 && r.comment && r.comment.length > 10)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 3);
    }
  } catch (err) {
    console.error("Lỗi khi tải đánh giá nổi bật:", err);
  }
};

onMounted(() => {
  window.scrollTo(0, 0);
  fetchTrips();
  fetchSavedRoutes();
  fetchSettings();
  fetchTopReviews();
  window.addEventListener('click', handleClickOutside);
  // Force recompute mỗi 3 giây để bắt ảnh mới từ admin
  _syncInterval = setInterval(() => { _syncCounter.value++; }, 3000);
  // Bắt ngay khi localStorage thay đổi từ tab khác
  window.addEventListener('storage', () => { _syncCounter.value++; });
});
onUnmounted(() => {
  window.removeEventListener('click', handleClickOutside);
  if (_syncInterval) clearInterval(_syncInterval);
});
</script>