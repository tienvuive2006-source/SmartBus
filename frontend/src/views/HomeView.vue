<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-800">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        
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
            <button @click="scrollToSection('scheduleSection')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Lịch xuất bến</button>
          </div>
        </div>

        <div class="flex items-center gap-6">
          <div class="hidden md:flex items-center gap-2 text-yellow-400 font-bold text-lg">
            1900.59.99.97
          </div>
          <button @click="$router.push('/profile')" class="flex items-center gap-2 text-sm font-semibold hover:text-yellow-300 transition-colors">
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

    <header class="relative bg-slate-900 pt-16 pb-36 flex flex-col items-center justify-center min-h-[450px]">
      <div 
        class="absolute inset-0 bg-cover bg-center z-0 opacity-80"
        style="background-image: url('https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?q=80&w=2069&auto=format&fit=crop');"
      ></div>
      <div class="absolute inset-0 bg-gradient-to-b from-[#075955]/60 to-transparent z-10"></div>

      <div class="relative z-20 max-w-5xl mx-auto px-4 text-center mt-[-40px]">
        <h2 
          class="text-red-500 text-5xl md:text-6xl mb-2 drop-shadow-md" 
          style="font-family: 'Brush Script MT', 'Dancing Script', cursive;"
        >
          Giảm giá - Khuyến mãi
        </h2>
        <h1 class="text-3xl md:text-4xl lg:text-5xl font-black text-white mb-2 drop-shadow-lg tracking-tight">
          NHÀ XE TRUNG - NAM <br/>
          <span class="text-white">VÉ XE KHÁCH - MUA VÉ TRỰC TUYẾN</span>
        </h1>
        <p class="text-white/90 text-sm md:text-base font-medium drop-shadow mt-4">
          Tính năng chọn chỗ, thanh toán và in vé điện tử
        </p>
      </div>
    </header>

    <div id="searchSection" class="max-w-6xl mx-auto px-4 relative z-30 -mt-6 mb-8">
      <div class="bg-white rounded-2xl shadow-xl p-3 flex flex-col md:flex-row items-stretch gap-2 border border-gray-200">
        
        <div class="flex-1 flex flex-col md:flex-row divide-y md:divide-y-0 md:divide-x divide-gray-200 bg-white">
          
          <div class="flex-1 p-3 relative group flex items-center gap-3 hover:bg-gray-50 transition-colors md:rounded-l-xl rounded-t-xl md:rounded-tr-none" ref="fromContainer">
            <span class="material-symbols-outlined text-gray-400">location_on</span>
            <div class="flex-1">
              <label class="block text-[11px] text-gray-500 mb-0.5">Nơi đi</label>
              <input v-model="fromQuery" @focus="onFromFocus" placeholder="Chọn nơi đi..." class="w-full font-semibold text-gray-800 focus:outline-none bg-transparent placeholder-gray-400 text-sm"/>
            </div>
            <ul v-if="showFromDropdown && fromSuggestions.length" class="absolute left-0 right-0 top-full mt-2 bg-white border border-gray-200 shadow-xl rounded-xl z-50 overflow-hidden">
              <li v-for="loc in fromSuggestions" :key="loc" @click="selectFromLocation(loc)" class="px-4 py-3 hover:bg-gray-100 cursor-pointer text-sm text-gray-700 flex items-center gap-2">
                <span class="material-symbols-outlined text-gray-400 text-sm">history</span>
                {{ loc.replace('@@WARNING@@', '') }}
              </li>
            </ul>
          </div>

          <div class="hidden md:flex items-center justify-center -mx-3 z-10">
            <button class="w-8 h-8 bg-white border border-gray-200 rounded-full flex items-center justify-center text-gray-500 hover:text-[#075955] shadow-sm hover:rotate-180 transition-transform">
              <span class="material-symbols-outlined text-sm">swap_horiz</span>
            </button>
          </div>

          <div class="flex-1 p-3 relative group flex items-center gap-3 hover:bg-gray-50 transition-colors" ref="toContainer">
            <span class="material-symbols-outlined text-gray-400">location_on</span>
            <div class="flex-1">
              <label class="block text-[11px] text-gray-500 mb-0.5">Nơi đến</label>
              <input v-model="toQuery" @focus="onToFocus" placeholder="Chọn nơi đến..." class="w-full font-semibold text-gray-800 focus:outline-none bg-transparent placeholder-gray-400 text-sm"/>
            </div>
            <ul v-if="showToDropdown && toSuggestions.length" class="absolute left-0 right-0 top-full mt-2 bg-white border border-gray-200 shadow-xl rounded-xl z-50 overflow-hidden">
              <li v-for="loc in toSuggestions" :key="loc" @click="selectToLocation(loc)" class="px-4 py-3 hover:bg-gray-100 cursor-pointer text-sm text-gray-700 flex items-center gap-2">
                <span class="material-symbols-outlined text-gray-400 text-sm">history</span>
                {{ loc.replace('@@WARNING@@', '') }}
              </li>
            </ul>
          </div>

          <div class="flex-1 p-3 flex items-center gap-3 hover:bg-gray-50 transition-colors">
            <span class="material-symbols-outlined text-gray-400">directions_bus</span>
            <div class="flex-1">
              <label class="block text-[11px] text-gray-500 mb-0.5">Nhà xe yêu thích</label>
              <select v-model="busCompanyQuery" class="w-full font-semibold text-gray-800 focus:outline-none bg-transparent text-sm appearance-none cursor-pointer">
                <option value="all">Tất cả</option>
                <option v-for="company in uniqueCompanies" :key="company.name" :value="company.name">
                  {{ company.name }}
                </option>
              </select>
            </div>
            <span class="material-symbols-outlined text-gray-400 text-sm">expand_more</span>
          </div>

          <div class="flex-1 p-3 flex items-center gap-3 hover:bg-gray-50 transition-colors md:rounded-r-xl rounded-b-xl md:rounded-bl-none">
            <span class="material-symbols-outlined text-gray-400">calendar_month</span>
            <div class="flex-1">
              <label class="block text-[11px] text-gray-500 mb-0.5">Ngày đi</label>
              <input type="date" v-model="dateQuery" class="w-full font-semibold text-gray-800 focus:outline-none bg-transparent text-sm uppercase"/>
            </div>
          </div>
        </div>

        <button @click="handleSearch" class="bg-[#f03a17] hover:bg-[#d63314] text-white rounded-xl font-bold text-sm px-10 py-3 md:py-0 transition-colors">
          TÌM
        </button>
      </div>
    </div>

    <div id="scheduleSection" class="max-w-6xl mx-auto px-4 mb-8">
      <div class="flex flex-col md:flex-row items-center justify-between border-b border-gray-300 pb-2">
        <div class="flex items-center gap-3">
          <h3 class="text-lg font-bold text-gray-800">Lịch xuất bến hàng ngày</h3>
          <span v-if="popularRoutes.length > 0" class="bg-[#075955]/10 text-[#075955] text-[11px] font-bold px-2 py-0.5 rounded-full border border-[#075955]/20 flex items-center gap-1">
             <span class="material-symbols-outlined text-[12px]">directions_bus</span>
             Hiện có {{ popularRoutes.length }} chuyến xe
          </span>
        </div>
        <div class="flex flex-wrap gap-4 mt-2 md:mt-0">
          <div class="flex items-center bg-white border border-gray-300 rounded-md px-2 py-1 text-sm shadow-sm">
            <span class="bg-[#075955] text-white px-2 py-0.5 rounded text-[10px] mr-2 shrink-0">Ngày đi</span>
            <input type="date" v-model="filterDate" class="bg-transparent border-none outline-none font-semibold text-gray-700 cursor-pointer text-xs" />
          </div>
          <div class="flex items-center bg-white border border-gray-300 rounded-md px-2 py-1 text-sm shadow-sm">
            <span class="bg-[#075955] text-white px-2 py-0.5 rounded text-[10px] mr-2 shrink-0">Nơi đi</span>
            <select v-model="filterFrom" class="bg-transparent border-none outline-none font-semibold text-gray-700 cursor-pointer pr-2">
              <option value="">Tất cả</option>
              <option v-for="loc in allDeparturePoints" :key="loc" :value="loc">{{ simplifyLocation(loc) }}</option>
            </select>
          </div>
          <div class="flex items-center bg-white border border-gray-300 rounded-md px-2 py-1 text-sm shadow-sm">
            <span class="bg-[#075955] text-white px-2 py-0.5 rounded text-[10px] mr-2 shrink-0">Nơi đến</span>
            <select v-model="filterTo" class="bg-transparent border-none outline-none font-semibold text-gray-700 cursor-pointer pr-2">
              <option value="">Tất cả</option>
              <option v-for="loc in allArrivalPoints" :key="loc" :value="loc">{{ simplifyLocation(loc) }}</option>
            </select>
          </div>
          <div class="flex items-center bg-white border border-gray-300 rounded-md px-2 py-1 text-sm shadow-sm">
            <span class="bg-[#075955] text-white px-2 py-0.5 rounded text-[10px] mr-2 shrink-0">Hãng xe</span>
            <select v-model="filterCompany" class="bg-transparent border-none outline-none font-semibold text-gray-700 cursor-pointer pr-2">
              <option value="">Tất cả</option>
              <option v-for="company in uniqueCompanies" :key="company.name" :value="company.name">{{ company.name }}</option>
            </select>
          </div>
        </div>
      </div>
      <p class="text-center italic text-gray-600 mt-6 font-medium text-lg">
        Nền tảng đặt xe uy tín - giá gốc 100% từ nhà xe
      </p>
    </div>

    <main v-if="popularRoutes.length > 0" class="max-w-6xl mx-auto px-4 py-8">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="route in popularRoutes" :key="route.id" @click="quickSearch(route.from, route.to, route.date)" class="group bg-white rounded-xl overflow-hidden hover:shadow-xl hover:-translate-y-1 transition-all duration-300 cursor-pointer border border-gray-200 flex flex-col">
          <div class="relative h-48 overflow-hidden">
            <img 
              :src="route.image" 
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ease-out"
              @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?q=80&w=2069&auto=format&fit=crop'"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
            <div class="absolute top-3 left-3">
              <span class="bg-white/90 backdrop-blur-sm text-[#075955] text-[10px] font-black px-2 py-1 rounded-md shadow-sm uppercase tracking-tighter border border-[#075955]/20">
                {{ route.busType }}
              </span>
            </div>
            <div class="absolute bottom-3 left-4 text-white">
              <h3 class="font-bold text-xl">{{ route.shortFrom }} ➝ {{ route.shortTo }}</h3>
            </div>
          </div>
          <div class="p-4 flex justify-between items-center bg-white">
            <div>
              <p class="text-xs text-gray-500 mb-1">Giá vé từ</p>
              <p class="text-[#075955] font-bold text-lg">{{ route.price?.toLocaleString() }}đ</p>
            </div>
            <button class="bg-[#075955] text-white p-2 rounded-full hover:bg-[#05403d] transition-colors">
               <span class="material-symbols-outlined text-sm">arrow_forward</span>
            </button>
          </div>
        </div>
      </div>
    </main>

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
                  Bấm nút bên dưới để lập tức lọc danh sách <strong>Lịch xuất bến hàng ngày</strong> tại trang chủ theo bến xe bạn chọn!
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
import { useApi } from '../composables/useApi';
import { useLocationSearch } from '../composables/useLocationSearch';
import { useAuthStore } from '@/stores/auth';

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
    trips.value = response.data;
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

const popularRoutes = computed(() => {
  const images = [
    'https://images.unsplash.com/photo-1583417319070-4a69db38a482?auto=format&fit=crop&q=80&w=600',
    'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&q=80&w=600',
    'https://images.unsplash.com/photo-1590393275627-0c484ceac518?auto=format&fit=crop&q=80&w=600',
    'https://images.unsplash.com/photo-1543306917-ce6bb04791e8?auto=format&fit=crop&q=80&w=600'
  ];
  if (trips.value.length === 0) return [];
  
  // 🔍 Lọc dữ liệu dựa trên Select
  let filtered = [...trips.value];
  if (filterFrom.value) {
    filtered = filtered.filter(t => t.departurePoint === filterFrom.value);
  }
  if (filterTo.value) {
    filtered = filtered.filter(t => t.arrivalPoint === filterTo.value);
  }
  if (filterCompany.value) {
    filtered = filtered.filter(t => t.companyName === filterCompany.value);
  }
  if (filterDate.value) {
    filtered = filtered.filter(t => t.departureDate?.split('T')[0] === filterDate.value);
  }

  const routes = [];
  const seen = new Set();
  filtered.sort((a, b) => b.id - a.id).forEach(t => {
    const key = `${t.departurePoint}-${t.arrivalPoint}`;
    if (!seen.has(key)) {
      seen.add(key);
      
      // Kiểm tra tính hợp lệ của link ảnh
      const isValidImage = t.imageUrl && t.imageUrl.startsWith('http');
      
      routes.push({ 
        id: t.id, 
        from: t.departurePoint, 
        to: t.arrivalPoint, 
        shortFrom: simplifyLocation(t.departurePoint),
        shortTo: simplifyLocation(t.arrivalPoint),
        date: t.departureDate,
        price: t.price,
        busType: t.busType,
        image: isValidImage ? t.imageUrl : images[routes.length % images.length] 
      });
    }
  });
  return routes;
});

const currentUser2 = ref(null); // không cần nữa — dùng authStore.currentUser ở trên
onMounted(() => {
  window.scrollTo(0, 0);
  fetchTrips();
  window.addEventListener('click', handleClickOutside);
});
onUnmounted(() => {
  window.removeEventListener('click', handleClickOutside);
});
</script>