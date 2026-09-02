<template>
  <div class="history-page min-h-screen bg-[#f2f5f8] font-sans text-slate-800">


    <div class="history-content pb-12 px-4 animate-fade-in bg-[#f2f5f8]">
    <main class="history-main max-w-3xl mx-auto space-y-6 py-6">
      <div class="history-heading bg-gradient-to-r from-[#075955] to-[#05403d] p-6 rounded-3xl shadow-md border border-[#05403d] relative overflow-hidden text-white mb-4">
        <div class="absolute -right-6 -bottom-6 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
        <h1 class="text-headline-md font-black flex items-center gap-2">
          <span class="material-symbols-outlined text-yellow-300 text-[32px]">confirmation_number</span>
          Lịch sử đặt vé
        </h1>
        <p class="text-label-md opacity-80 mt-1 tracking-widest font-bold">QUẢN LÝ TOÀN BỘ HÀNH TRÌNH CỦA BẠN</p>
      </div>

      <div class="history-tabs flex bg-white p-1.5 rounded-2xl border border-gray-200 mb-6 shadow-sm">
        <button 
          @click="activeTab = 'upcoming'"
          :class="['history-tab flex-1 py-3 rounded-xl text-body-md font-black transition-all duration-200 flex items-center justify-center gap-1.5', activeTab === 'upcoming' ? 'bg-[#075955] text-white shadow-sm' : 'text-gray-500 hover:bg-gray-100']"
        >
          <span class="material-symbols-outlined text-[18px]">upcoming</span>
          <span>Đang hoạt động</span><b>{{ upcomingTickets.length }}</b>
        </button>
        <button 
          @click="activeTab = 'completed'"
          :class="['history-tab flex-1 py-3 rounded-xl text-body-md font-black transition-all duration-200 flex items-center justify-center gap-1.5', activeTab === 'completed' ? 'bg-[#075955] text-white shadow-sm' : 'text-gray-500 hover:bg-gray-100']"
        >
          <span class="material-symbols-outlined text-[18px]">done_all</span>
          <span>Đã hủy / Đã qua</span><b>{{ completedTickets.length }}</b>
        </button>
      </div>

      <div class="ticket-list flex flex-col gap-6" v-if="activeTab === 'upcoming'">
        <div v-if="upcomingTickets.length === 0" class="history-empty flex flex-col items-center justify-center py-16 bg-white border border-dashed border-gray-300 rounded-3xl shadow-inner text-center px-6">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-gray-400">
            <span class="material-symbols-outlined text-5xl">search_off</span>
          </div>
          <h3 class="text-headline-sm font-black text-gray-800">Chưa có vé nào ở đây!</h3>
          <p class="text-body-md text-gray-500 mt-1 max-w-sm">Bạn chưa mua vé nào, hoặc lịch sử của trình duyệt đã bị xóa sạch.</p>
          <button @click="$router.push('/')" class="mt-6 bg-[#f03a17] text-white px-6 py-2.5 rounded-xl font-black text-body-md shadow-md hover:bg-[#d63314] transition-all active:scale-95">
            MUA VÉ NGAY
          </button>
        </div>

        <div 
          v-for="(ticket, index) in upcomingTickets" 
          :key="ticket.id || index"
          class="ticket-card bg-white rounded-3xl shadow-[0px_8px_30px_rgba(0,0,0,0.03)] overflow-hidden border border-gray-200 relative group animate-slide-up hover:shadow-md transition-all duration-300"
          :style="`animation-delay: ${index * 0.1}s`"
        >
          <div class="h-1.5 w-full bg-emerald-500"></div>

          <div class="ticket-card-body p-6 relative">
            <div class="ticket-card-header flex justify-between items-start mb-5">
              <div class="flex flex-col">
                <div class="flex items-center gap-2 mb-3">
                  <span class="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-emerald-100/50 shadow-sm" v-if="ticket.status === 'PAID'">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    ĐÃ THANH TOÁN
                  </span>
                  <span class="inline-flex items-center gap-1 bg-amber-50 text-amber-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-amber-100/50 shadow-sm" v-else-if="ticket.status === 'PENDING'">
                    <span class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                    CHỜ THANH TOÁN
                  </span>
                  <span class="inline-flex items-center gap-1 bg-rose-50 text-rose-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-rose-100/50 shadow-sm" v-else-if="ticket.status === 'CANCELLED'">
                    <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                    ĐÃ HỦY
                  </span>
                  <span class="inline-flex items-center gap-1 bg-blue-50 text-blue-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-blue-100/50 shadow-sm" v-else-if="ticket.status === 'CHECKED_IN'">
                    <span class="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
                    ĐÃ LÊN XE
                  </span>
                  <span class="text-[11px] font-bold text-gray-400">{{ ticketReference(ticket) }}</span>
                </div>
                <h2 class="text-headline-sm font-black text-gray-800 flex items-center gap-2 leading-tight">
                  {{ ticket.from }} 
                  <span class="material-symbols-outlined text-[#075955] text-[20px] animate-pulse">east</span> 
                  {{ ticket.to }}
                </h2>
                <p class="text-body-sm font-bold text-gray-600 mt-1 bg-gray-50 px-2 py-1 rounded-lg w-fit border border-gray-100">
                  🚀 Trung - Nam Premium • {{ ticket.busType }}
                </p>
                <div v-if="ticket.stopSelection" class="mt-2 flex flex-wrap gap-2 text-[10px] font-bold">
                  <span class="rounded-lg bg-emerald-50 px-2 py-1 text-emerald-700">Đón: {{ ticket.stopSelection.pickupName }}</span>
                  <span class="rounded-lg bg-blue-50 px-2 py-1 text-blue-700">Trả: {{ ticket.stopSelection.dropoffName }}</span>
                </div>
              </div>

              <div class="ticket-actions flex gap-2">
                <button
                  v-if="ticket.status === 'PAID' && !ticket.exchange"
                  @click="$router.push(`/booking/exchange/${ticket.id}`)"
                  class="p-3 bg-amber-50 border border-amber-100 rounded-2xl text-amber-600 hover:bg-amber-500 hover:text-white transition-all duration-300 shadow-sm active:scale-95 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black">swap_horiz</span>
                  <span class="text-[9px] font-black tracking-widest">ĐỔI VÉ</span>
                </button>
                <div
                  v-else-if="ticket.exchange"
                  class="p-3 bg-blue-50 border border-blue-100 rounded-2xl text-blue-600 flex flex-col items-center gap-0.5 shadow-sm"
                  title="Vé này đã sử dụng quyền đổi vé"
                >
                  <span class="material-symbols-outlined text-[28px] font-black">published_with_changes</span>
                  <span class="text-[9px] font-black tracking-widest">ĐÃ ĐỔI VÉ</span>
                </div>
                <button 
                  v-if="ticket.status === 'PAID' || ticket.status === 'PENDING'"
                  @click="openCancelModal(ticket)"
                  class="p-3 bg-rose-50 border border-rose-100 rounded-2xl text-rose-500 hover:bg-rose-500 hover:text-white transition-all duration-300 shadow-sm active:scale-95 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black">free_cancellation</span>
                  <span class="text-[9px] font-black tracking-widest">HỦY VÉ</span>
                </button>
                <button 
                  v-if="ticket.status !== 'CANCELLED'"
                  @click="openQrModal(ticket)"
                  class="p-3 bg-[#075955]/5 border border-[#075955]/10 rounded-2xl text-[#075955] hover:bg-[#075955] hover:text-white transition-all duration-300 shadow-sm active:scale-95 group-hover:scale-105 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black">qr_code_2</span>
                  <span class="text-[9px] font-black tracking-widest">QUÉT MÃ</span>
                </button>
              </div>
            </div>

            <div class="border-t border-dashed border-gray-300 my-4 relative">
              <div class="absolute -left-[34px] top-1/2 -translate-y-1/2 w-4 h-4 bg-slate-50 rounded-full border border-gray-200 shadow-inner"></div>
              <div class="absolute -right-[34px] top-1/2 -translate-y-1/2 w-4 h-4 bg-slate-50 rounded-full border border-gray-200 shadow-inner"></div>
            </div>

            <div class="ticket-summary flex justify-between items-end">
              <div class="ticket-details grid grid-cols-2 gap-x-8 gap-y-2">
                <div>
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Giờ khởi hành</p>
                  <p class="text-body-md font-black text-gray-800 flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm text-[#075955]">schedule</span>
                    {{ ticket.time }} ({{ ticket.date }})
                  </p>
                </div>
                <div>
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Vị trí Ghế</p>
                  <p class="text-body-md font-black text-[#f03a17] uppercase flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chair</span>
                    {{ ticket.seats }}
                  </p>
                </div>
              </div>
              <div class="ticket-price text-right border-l border-gray-100 pl-4">
                <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Đã trả qua {{ ticket.method || 'Ví MoMo' }}</p>
                <p class="text-headline-sm font-black text-[#075955] tracking-tight">{{ parseFloat(ticket.total).toLocaleString('vi-VN') }}đ</p>
              </div>
            </div>

            <div v-if="ticket.exchange" class="mt-5 overflow-hidden rounded-2xl border border-blue-100 bg-blue-50/60">
              <div class="flex items-center justify-between border-b border-blue-100 px-4 py-3">
                <div class="flex items-center gap-2 text-blue-700">
                  <span class="material-symbols-outlined text-lg">history</span>
                  <span class="text-[10px] font-black uppercase tracking-widest">Thông tin vé đã đổi</span>
                </div>
                <span class="text-[10px] font-bold text-blue-500">{{ formatExchangeDate(ticket.exchange.exchangedAt) }}</span>
              </div>
              <div class="grid gap-0 md:grid-cols-[1fr_auto_1fr] md:items-stretch">
                <div class="p-4">
                  <p class="mb-2 text-[10px] font-black uppercase tracking-widest text-slate-400">Vé cũ</p>
                  <p class="text-xs font-black text-slate-700">{{ exchangeRoute(ticket.exchange.oldTicket) }}</p>
                  <p class="mt-2 flex items-center gap-1.5 text-xs font-bold text-slate-600">
                    <span class="material-symbols-outlined text-[15px] text-slate-400">directions_bus</span>
                    {{ exchangeBusLine(ticket.exchange.oldTicket) }}
                  </p>
                  <p class="mt-2 text-xs font-semibold text-slate-500">{{ exchangeSchedule(ticket.exchange.oldTicket) }}</p>
                  <p class="mt-1 text-xs font-bold text-slate-500">Ghế {{ ticket.exchange.oldTicket.seatNumbers.join(', ') }}</p>
                  <p class="mt-2 font-black text-slate-600">{{ Number(ticket.exchange.oldTicket.price).toLocaleString('vi-VN') }}đ</p>
                </div>
                <div class="flex items-center justify-center border-y border-blue-100 px-3 py-2 text-blue-500 md:border-x md:border-y-0">
                  <span class="material-symbols-outlined">arrow_forward</span>
                </div>
                <div class="bg-white/60 p-4">
                  <p class="mb-2 text-[10px] font-black uppercase tracking-widest text-blue-500">Vé mới</p>
                  <p class="text-xs font-black text-slate-800">{{ exchangeRoute(ticket.exchange.newTicket) }}</p>
                  <p class="mt-2 flex items-center gap-1.5 text-xs font-bold text-[#075955]">
                    <span class="material-symbols-outlined text-[15px]">directions_bus</span>
                    {{ exchangeBusLine(ticket.exchange.newTicket) }}
                  </p>
                  <p class="mt-2 text-xs font-semibold text-slate-600">{{ exchangeSchedule(ticket.exchange.newTicket) }}</p>
                  <p class="mt-1 text-xs font-bold text-[#f03a17]">Ghế {{ ticket.exchange.newTicket.seatNumbers.join(', ') }}</p>
                  <p class="mt-2 font-black text-[#075955]">{{ Number(ticket.exchange.newTicket.price).toLocaleString('vi-VN') }}đ</p>
                </div>
              </div>
              <div v-if="ticket.exchange.priceDifference !== 0" class="border-t border-blue-100 px-4 py-2 text-right text-xs font-black" :class="ticket.exchange.priceDifference < 0 ? 'text-emerald-600' : 'text-rose-600'">
                {{ ticket.exchange.priceDifference < 0 ? 'Đã hoàn vào ví +' : 'Đã thanh toán thêm +' }}{{ Math.abs(ticket.exchange.priceDifference).toLocaleString('vi-VN') }}đ
              </div>
            </div>
            
            <!-- Review Section (if already reviewed) -->
            <div v-if="ticket.userReview" class="mt-4 p-4 bg-amber-50/50 rounded-2xl border border-amber-100/50 flex flex-col gap-2">
              <div class="flex items-center gap-1.5 mb-1">
                <span class="text-[10px] font-black uppercase tracking-widest text-amber-600/70">Đánh giá của bạn</span>
                <div class="flex">
                  <span v-for="star in ticket.userReview.rating" :key="star" class="material-symbols-outlined text-[14px] text-amber-400" style="font-variation-settings: 'FILL' 1;">star</span>
                  <span v-for="star in (5 - ticket.userReview.rating)" :key="'empty'+star" class="material-symbols-outlined text-[14px] text-gray-300" style="font-variation-settings: 'FILL' 1;">star</span>
                </div>
              </div>
              <p v-if="ticket.userReview.comment" class="text-sm font-semibold text-gray-700 italic">"{{ ticket.userReview.comment }}"</p>
            </div>
          </div>
        </div>
      </div>

      <div class="ticket-list flex flex-col gap-6" v-else-if="activeTab === 'completed'">
        <div v-if="completedTickets.length === 0" class="history-empty flex flex-col items-center justify-center py-20 bg-white border border-gray-200 rounded-3xl shadow-sm text-center px-6 animate-fade-in">
          <span class="material-symbols-outlined text-6xl text-gray-300 mb-4 animate-pulse">history_toggle_off</span>
          <h3 class="text-headline-sm font-black text-gray-800">Không có dữ liệu quá khứ</h3>
          <p class="text-body-md text-gray-500 mt-1">Các chuyến đi sau khi hoàn thành lộ trình sẽ tự động lưu tại đây.</p>
        </div>
        <div 
          v-for="(ticket, index) in completedTickets" 
          :key="ticket.id || index"
          class="ticket-card bg-white/80 rounded-3xl shadow-sm overflow-hidden border border-gray-200 relative group animate-slide-up transition-all duration-300 opacity-90 hover:opacity-100"
          :style="`animation-delay: ${index * 0.1}s`"
        >
          <div class="h-1.5 w-full bg-gray-400"></div>

          <div class="ticket-card-body p-6 relative">
            <div class="ticket-card-header flex justify-between items-start mb-5">
              <div class="flex flex-col">
                <div class="flex items-center gap-2 mb-3">
                  <span class="inline-flex items-center gap-1 bg-rose-50 text-rose-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-rose-100/50 shadow-sm" v-if="ticket.status === 'CANCELLED'">
                    <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                    ĐÃ HỦY
                  </span>
                  <span class="inline-flex items-center gap-1 bg-blue-50 text-blue-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-blue-100/50 shadow-sm" v-else-if="ticket.status === 'CHECKED_IN'">
                    <span class="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
                    ĐÃ LÊN XE
                  </span>
                  <span class="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 text-[10px] font-black uppercase tracking-wider px-3 py-1 rounded-full border border-emerald-100/50 shadow-sm" v-else>
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                    HOÀN THÀNH
                  </span>
                  <span class="text-[11px] font-bold text-gray-400">{{ ticketReference(ticket) }}</span>
                  <span v-if="ticket.status === 'CANCELLED' && ticket.refundRequest" class="refund-status" :class="refundStatusClass(ticket.refundRequest.status)">
                    {{ ticket.refundRequest.refundMethod === 'BANK_TRANSFER' ? 'Ngân hàng' : 'Ví' }} · {{ refundStatusText(ticket.refundRequest.status) }}
                  </span>
                </div>
                <h2 class="text-headline-sm font-black text-gray-800 flex items-center gap-2 leading-tight">
                  {{ ticket.from }} 
                  <span class="material-symbols-outlined text-gray-400 text-[20px]">east</span> 
                  {{ ticket.to }}
                </h2>
              </div>

              <div class="ticket-actions flex gap-2">
                <button 
                  v-if="!ticket.isReviewed && (ticket.status === 'COMPLETED' || ticket.status === 'CHECKED_IN')"
                  @click="openReviewModal(ticket)"
                  class="p-3 bg-amber-50 border border-amber-100 rounded-2xl text-amber-500 hover:bg-amber-500 hover:text-white transition-all duration-300 shadow-sm active:scale-95 flex flex-col items-center gap-0.5"
                >
                  <span class="material-symbols-outlined text-[28px] font-black" style="font-variation-settings: 'FILL' 1;">star</span>
                  <span class="text-[9px] font-black tracking-widest">ĐÁNH GIÁ</span>
                </button>
                <div 
                  v-else-if="ticket.isReviewed && (ticket.status === 'COMPLETED' || ticket.status === 'CHECKED_IN')"
                  class="p-3 bg-gray-50 border border-gray-100 rounded-2xl text-gray-400 flex flex-col items-center gap-0.5 cursor-not-allowed opacity-70"
                >
                  <span class="material-symbols-outlined text-[28px] font-black" style="font-variation-settings: 'FILL' 1;">check_circle</span>
                  <span class="text-[9px] font-black tracking-widest">ĐÃ ĐÁNH GIÁ</span>
                </div>
              </div>
            </div>

            <div class="border-t border-dashed border-gray-300 my-4"></div>

            <div class="ticket-summary flex justify-between items-end">
              <div class="ticket-details grid grid-cols-2 gap-x-8 gap-y-2">
                <div>
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Giờ khởi hành</p>
                  <p class="text-body-md font-black text-gray-800 flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm text-gray-500">schedule</span>
                    {{ ticket.time }} ({{ ticket.date }})
                  </p>
                </div>
                <div>
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Vị trí Ghế</p>
                  <p class="text-body-md font-black text-gray-600 uppercase flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chair</span>
                    {{ ticket.seats }}
                  </p>
                </div>
              </div>
              <div class="ticket-price text-right border-l border-gray-100 pl-4">
                <p class="text-[10px] font-black text-gray-400 uppercase tracking-wider mb-0.5">Đã trả qua {{ ticket.method || 'Ví MoMo' }}</p>
                <p class="text-headline-sm font-black text-gray-600 tracking-tight" :class="{'line-through text-gray-400': ticket.status === 'CANCELLED'}">{{ parseFloat(ticket.total).toLocaleString('vi-VN') }}đ</p>
              </div>
            </div>

            <div v-if="ticket.status === 'CANCELLED' && ticket.refundRequest" class="mt-4 flex flex-col gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 sm:flex-row sm:items-center sm:justify-between">
              <div class="flex items-start gap-3">
                <span class="material-symbols-outlined mt-0.5 text-[#075955]">payments</span>
                <div>
                  <p class="text-xs font-black text-slate-700">
                    {{ ticket.refundRequest.refundMethod === 'BANK_TRANSFER' ? 'Hoàn tiền qua ngân hàng' : 'Hoàn tiền vào Ví Trung Nam' }}
                    · {{ Number(ticket.refundRequest.refundAmount).toLocaleString('vi-VN') }}đ
                  </p>
                  <p class="mt-1 text-[11px] font-semibold text-slate-500">
                    {{ ticket.refundRequest.refundMethod === 'WALLET' ? 'Ví Trung Nam' : `${ticket.refundRequest.bankName} • ${maskBankAccount(ticket.refundRequest.bankAccountNumber)}` }}
                  </p>
                  <p v-if="['NEEDS_INFO', 'REJECTED'].includes(ticket.refundRequest.status)" class="mt-2 text-[11px] font-bold text-rose-600">Cần bổ sung: {{ ticket.refundRequest.adminNote || 'Vui lòng kiểm tra lại thông tin nhận tiền.' }}</p>
                </div>
              </div>
              <div class="flex flex-col items-stretch gap-2 sm:items-end">
                <span class="refund-status text-center" :class="refundStatusClass(ticket.refundRequest.status)">{{ refundStatusText(ticket.refundRequest.status) }}</span>
                <button v-if="['NEEDS_INFO', 'REJECTED'].includes(ticket.refundRequest.status)" type="button" class="rounded-lg bg-[#075955] px-3 py-2 text-[10px] font-black uppercase tracking-wider text-white hover:bg-[#064b48]" @click="openRefundUpdate(ticket)">Cập nhật tài khoản</button>
              </div>
            </div>
            
            <div v-if="ticket.userReview" class="mt-4 p-4 bg-amber-50/50 rounded-2xl border border-amber-100/50 flex flex-col gap-2">
              <div class="flex items-center gap-1.5 mb-1">
                <span class="text-[10px] font-black uppercase tracking-widest text-amber-600/70">Đánh giá của bạn</span>
                <div class="flex">
                  <span v-for="star in ticket.userReview.rating" :key="star" class="material-symbols-outlined text-[14px] text-amber-400" style="font-variation-settings: 'FILL' 1;">star</span>
                </div>
              </div>
              <p v-if="ticket.userReview.comment" class="text-sm font-semibold text-gray-700 italic">"{{ ticket.userReview.comment }}"</p>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="isModalOpen && selectedTicket" class="fixed inset-0 bg-slate-900/85 backdrop-blur-sm z-[999] flex items-center justify-center p-6 animate-fade-in" @click.self="closeModal">
      <div class="bg-white w-full max-w-sm rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.3)] overflow-hidden border border-white/20 animate-scale-up flex flex-col items-center relative">
        <button @click="closeModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-900 w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center transition-all active:scale-90">
          <span class="material-symbols-outlined">close</span>
        </button>

        <div class="p-6 w-full text-center">
          <h3 class="text-headline-sm font-black text-gray-900 mb-1">Mã Lên Xe Chi Nhánh</h3>
          <p class="text-[11px] font-bold text-[#075955] uppercase tracking-widest mb-6">{{ ticketReference(selectedTicket) }}</p>
          
          <div class="bg-gray-50 p-5 rounded-3xl border border-gray-200 mb-4 inline-block shadow-inner relative group">
            <img 
              :src="`https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=${encodeURIComponent('Mã đặt vé: ' + ticketReference(selectedTicket) + '\nKhách: ' + (authStore.currentUser?.fullName || 'Quý khách') + '\nGhế: ' + selectedTicket.seats + '\nTrạng thái: ' + (selectedTicket.method === 'CASH' ? 'CHƯA THANH TOÁN (THU TIỀN MẶT)' : 'ĐÃ THANH TOÁN'))}&color=075955&bgcolor=f8fafc`"
              alt="Modal QR" 
              class="w-44 h-44 group-hover:scale-105 transition-transform duration-300 mix-blend-multiply"
            />
          </div>
          <p class="text-[11px] font-black text-gray-500 uppercase tracking-widest leading-relaxed px-4">
            VUI LÒNG ĐƯA MÃ NÀY CHO TÀI XẾ KHI BƯỚC LÊN XE
          </p>
        </div>
        
        <div class="w-full bg-gray-50 px-6 py-4 border-t border-gray-200 text-center">
          <p class="text-body-md font-black text-gray-800">
            {{ selectedTicket.from }} <span class="text-[#075955]">→</span> {{ selectedTicket.to }}
          </p>
          <p class="text-[10px] font-bold text-gray-500 uppercase tracking-wider mt-0.5">
            Ghế {{ selectedTicket.seats }} • Khởi hành {{ selectedTicket.time }}
          </p>
        </div>
      </div>
    </div>

    
    <!-- Review Modal -->
    <div v-if="isReviewModalOpen && selectedTicket" class="fixed inset-0 bg-slate-900/85 backdrop-blur-sm z-[999] flex items-center justify-center p-6 animate-fade-in" @click.self="closeReviewModal">
      <div class="bg-white w-full max-w-lg max-h-[90dvh] overflow-y-auto rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.3)] border border-white/20 animate-scale-up p-6 relative">
        <button @click="closeReviewModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-900 w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center transition-all active:scale-90">
          <span class="material-symbols-outlined">close</span>
        </button>

        <h3 class="text-headline-sm font-black text-amber-500 mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">stars</span> Đánh giá Nhà xe
        </h3>
        <p class="text-xs text-gray-500 font-medium mb-4 leading-relaxed">
          Chuyến: <b class="text-gray-800">{{ selectedTicket.from }} ➝ {{ selectedTicket.to }}</b>.<br/>
          Dòng xe: <b class="text-amber-600">{{ selectedTicket.busType }}</b>
        </p>

        <div class="flex items-center gap-3 bg-gray-50 p-3 rounded-xl mb-6 border border-gray-100">
           <img :src="selectedTicket.imageUrl || 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=400'" 
                class="w-16 h-16 object-cover rounded-lg shadow-sm" alt="Bus Image" />
           <div class="flex flex-col">
              <span class="text-xs text-gray-500 uppercase font-bold tracking-widest">Tài xế</span>
              <span class="text-sm font-black text-gray-800">{{ selectedTicket.driverName }}</span>
              <span class="text-[11px] font-semibold text-gray-500 mt-0.5 flex items-center gap-1">
                 <span class="material-symbols-outlined text-[12px]">directions_bus</span> BKS: {{ selectedTicket.licensePlate || 'Chưa xếp xe' }}
              </span>
           </div>
        </div>

        <div class="flex justify-center gap-2 mb-6">
          <span 
            v-for="star in 5" 
            :key="star"
            @click="reviewForm.rating = star"
            class="material-symbols-outlined text-5xl cursor-pointer transition-colors"
            :class="star <= reviewForm.rating ? 'text-amber-400' : 'text-gray-200'"
            style="font-variation-settings: 'FILL' 1;"
          >star</span>
        </div>

        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">Bình luận (Tùy chọn)</label>
        <textarea v-model="reviewForm.comment" rows="3" placeholder="Nhà xe phục vụ như thế nào?..." class="w-full border border-gray-200 rounded-xl px-4 py-3 bg-gray-50 text-sm font-semibold text-gray-700 outline-none focus:border-amber-500 mb-6 resize-none"></textarea>

        <button 
          @click="submitReview" 
          :disabled="reviewForm.rating === 0 || isSubmittingReview"
          class="w-full bg-amber-500 text-white font-black py-3.5 rounded-xl text-sm uppercase tracking-widest shadow-md hover:bg-amber-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all active:scale-95"
        >
          <span v-if="isSubmittingReview" class="w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin"></span>
          {{ isSubmittingReview ? 'Đang gửi...' : 'Gửi Đánh Giá' }}
        </button>
      </div>
    </div>

    <!-- Hủy Vé Modal -->
    <Teleport to="body">
    <div v-if="isCancelModalOpen && selectedTicket" class="fixed inset-0 bg-slate-900/85 backdrop-blur-sm z-[9999] flex items-center justify-center overflow-y-auto p-4 sm:p-6 animate-fade-in" @click.self="closeCancelModal">
      <div class="cancel-modal-panel bg-white w-full max-w-xl max-h-[calc(100dvh-2rem)] sm:max-h-[calc(100dvh-3rem)] overflow-y-auto overscroll-contain rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.3)] border border-white/20 animate-scale-up p-6 sm:p-8 relative">
        <button @click="closeCancelModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-900 w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center transition-all active:scale-90">
          <span class="material-symbols-outlined">close</span>
        </button>

        <h3 class="text-headline-sm font-black text-rose-600 mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined">warning</span> Hủy vé xe
        </h3>
        
        <div v-if="hoursToDeparture < 12" class="bg-rose-50 p-4 rounded-xl border border-rose-200 mb-6">
          <p class="text-sm font-bold text-rose-700 mb-1">Không hỗ trợ hủy vé trực tuyến</p>
          <p class="text-xs text-rose-600 leading-relaxed">
            Chuyến xe của bạn sẽ khởi hành trong vòng chưa tới 12 tiếng nữa. 
            Theo quy định, hệ thống không hỗ trợ hoàn tiền khi hủy vé sát giờ khởi hành.
            <br/><br/>Mã vé của bạn: <b>{{ ticketReference(selectedTicket) }}</b>. Vui lòng liên hệ tổng đài 1900 1234 nếu cần hỗ trợ khẩn cấp.
          </p>
        </div>
        
        <template v-else>
          <p class="text-xs text-gray-500 font-medium mb-6 leading-relaxed">
            Mã vé: <b>{{ ticketReference(selectedTicket) }}</b>.<br/>
            <span v-if="selectedTicket.status === 'PAID' && selectedTicket.method !== 'CASH'">
              <span v-if="hoursToDeparture >= 12 && hoursToDeparture < 24">
                Bạn hủy vé trước giờ khởi hành 12-24 tiếng. Phí hủy là 30%, số tiền còn lại được hoàn theo phương thức bạn chọn.
              </span>
              <span v-else-if="hoursToDeparture >= 24">
                Bạn hủy vé trước giờ khởi hành 24 tiếng. Phí hủy là 5%, số tiền còn lại được hoàn theo phương thức bạn chọn.
              </span>
            </span>
            <span v-else>Thao tác này không thể hoàn tác.</span>
          </p>

          <label v-if="!authStore.isLoggedIn" class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">Số điện thoại đã đặt vé</label>
          <input
            v-if="!authStore.isLoggedIn"
            v-model.trim="guestCancellationPhone"
            type="tel"
            inputmode="numeric"
            maxlength="11"
            placeholder="Nhập số điện thoại để xác minh vé"
            class="w-full border border-gray-200 rounded-xl px-4 py-3 bg-gray-50 text-sm font-semibold text-gray-700 outline-none focus:border-[#075955] mb-4"
          />

          <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">Lý do hủy vé (Bắt buộc)</label>
          <select v-model="cancelReason" class="w-full border border-gray-200 rounded-xl px-4 py-3 bg-gray-50 text-sm font-semibold text-gray-700 outline-none focus:border-[#075955] mb-4">
            <option value="" disabled>-- Chọn lý do --</option>
            <option value="Thay đổi lịch trình">Thay đổi lịch trình</option>
            <option value="Tìm được xe khác phù hợp hơn">Tìm được xe khác phù hợp hơn</option>
            <option value="Đặt nhầm ngày/giờ">Đặt nhầm ngày/giờ</option>
            <option value="Lý do cá nhân">Lý do cá nhân</option>
          </select>

          <div v-if="selectedTicket.status === 'PAID' && selectedTicket.method !== 'CASH'" class="bg-amber-50 p-3 rounded-xl border border-amber-100 mb-6 flex items-start gap-2">
             <span class="material-symbols-outlined text-amber-500 text-lg">account_balance_wallet</span>
             <div>
               <p class="text-xs font-bold text-amber-700">
                 Số tiền hoàn lại 
                 ({{ hoursToDeparture >= 24 ? '95%' : '70%' }})
               </p>
               <p class="text-base font-black text-amber-600">
                 +{{ (selectedTicket.total * (hoursToDeparture >= 24 ? 0.95 : 0.7)).toLocaleString('vi-VN') }}đ
               </p>
             </div>
          </div>

          <div v-if="refundEligible" class="mb-6">
            <p class="mb-2 text-[10px] font-bold uppercase tracking-widest text-gray-400">Nhận tiền hoàn bằng</p>
            <div class="grid grid-cols-2 gap-3">
              <button v-if="authStore.isLoggedIn" type="button" class="refund-method" :class="{ active: refundMethod === 'WALLET' }" @click="refundMethod = 'WALLET'">
                <span class="material-symbols-outlined">account_balance_wallet</span>
                <span><b>Ví Trung Nam</b><small>Nhận tiền ngay</small></span>
              </button>
              <button type="button" class="refund-method" :class="{ active: refundMethod === 'BANK_TRANSFER' }" @click="refundMethod = 'BANK_TRANSFER'">
                <span class="material-symbols-outlined">account_balance</span>
                <span><b>Chuyển khoản</b><small>Admin xử lý</small></span>
              </button>
            </div>

            <div v-if="refundMethod === 'BANK_TRANSFER'" class="mt-3 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 sm:grid-cols-2">
              <label class="refund-field"><span>Ngân hàng</span><select v-model="bankInfo.bankName"><option value="" disabled>Chọn ngân hàng</option><option v-for="bank in vietnameseBanks" :key="bank.code" :value="bank.name">{{ bank.code }} · {{ bank.name }}</option></select></label>
              <label class="refund-field"><span>Số tài khoản</span><input v-model.trim="bankInfo.accountNumber" inputmode="numeric" maxlength="30" placeholder="Nhập số tài khoản nhận tiền" /></label>
              <label class="refund-field sm:col-span-2"><span>Tên chủ tài khoản</span><input v-model.trim="bankInfo.accountName" maxlength="150" placeholder="NGUYEN VAN A" /></label>
              <div class="flex gap-3 rounded-xl border border-amber-200 bg-amber-50 p-3 text-[11px] leading-5 text-amber-800 sm:col-span-2">
                <span class="material-symbols-outlined mt-0.5 text-lg text-amber-600">schedule</span>
                <div>
                  <p class="font-black">Vui lòng chờ ít nhất 24 giờ</p>
                  <p class="mt-0.5 font-semibold text-amber-700">Nhà xe cần thời gian kiểm tra và thực hiện chuyển khoản. Bạn sẽ nhận được thông báo ngay khi tiền đã được chuyển.</p>
                </div>
              </div>
            </div>
          </div>

          <p v-if="cancelError" class="mb-4 flex items-start gap-2 rounded-xl bg-rose-50 px-3 py-2.5 text-xs font-semibold text-rose-700">
            <span class="material-symbols-outlined text-base">error</span>{{ cancelError }}
          </p>
        </template>

        <button 
          v-if="hoursToDeparture >= 12"
          @click="confirmCancel" 
          :disabled="!canSubmitCancellation || isCancelling"
          class="w-full bg-rose-500 text-white font-black py-3.5 rounded-xl text-sm uppercase tracking-widest shadow-md hover:bg-rose-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all active:scale-95"
        >
          <span v-if="isCancelling" class="w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin"></span>
          {{ isCancelling ? 'Đang xử lý...' : 'Xác nhận Hủy Vé' }}
        </button>
      </div>
    </div>

    <div v-if="cancellationSuccess" class="fixed inset-0 z-[10000] flex items-center justify-center bg-slate-950/70 p-4 backdrop-blur-sm animate-fade-in" @click.self="closeCancellationSuccess">
      <section class="w-full max-w-md overflow-hidden rounded-3xl border border-white/20 bg-white shadow-[0_24px_80px_rgba(0,35,32,0.35)] animate-scale-up" role="dialog" aria-modal="true" aria-labelledby="cancel-success-title">
        <div class="relative bg-[#075955] px-7 pb-7 pt-8 text-white">
          <button type="button" class="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full bg-white/10 text-white/80 transition hover:bg-white/20 hover:text-white" aria-label="Đóng" @click="closeCancellationSuccess">
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
          <div class="grid h-12 w-12 place-items-center rounded-2xl bg-emerald-300 text-[#075955] shadow-lg shadow-black/10">
            <span class="material-symbols-outlined text-3xl">check_circle</span>
          </div>
          <p class="mt-5 text-[10px] font-black uppercase tracking-[0.18em] text-emerald-200">Hủy vé thành công</p>
          <h3 id="cancel-success-title" class="mt-1 text-2xl font-black tracking-tight">Yêu cầu đã được ghi nhận</h3>
        </div>

        <div class="p-7">
          <div class="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <div>
              <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">Số tiền hoàn</p>
              <strong class="mt-1 block text-2xl font-black text-[#075955]">{{ money(cancellationSuccess.amount) }}</strong>
            </div>
            <span class="rounded-full px-3 py-1.5 text-[11px] font-black" :class="cancellationSuccess.method === 'BANK_TRANSFER' ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'">
              {{ cancellationSuccess.method === 'BANK_TRANSFER' ? 'Chờ chuyển khoản' : 'Đã hoàn vào ví' }}
            </span>
          </div>

          <div class="mt-5 flex items-start gap-3 text-sm leading-6 text-slate-600">
            <span class="material-symbols-outlined mt-0.5 text-xl text-[#087269]">{{ cancellationSuccess.method === 'BANK_TRANSFER' ? 'account_balance' : 'account_balance_wallet' }}</span>
            <p v-if="cancellationSuccess.method === 'BANK_TRANSFER'">Thời gian xử lý tối thiểu 24 giờ. Admin sẽ kiểm tra và chuyển tiền đến tài khoản ngân hàng bạn đã cung cấp, sau đó hệ thống sẽ gửi thông báo.</p>
            <p v-else>Tiền hoàn đã được cộng vào Ví Trung Nam và có thể sử dụng cho lần đặt vé tiếp theo.</p>
          </div>

          <button type="button" class="mt-6 w-full rounded-xl bg-[#075955] py-3.5 text-sm font-black uppercase tracking-wider text-white shadow-lg shadow-emerald-950/10 transition hover:bg-[#064b48] active:translate-y-px" @click="closeCancellationSuccess">Đã hiểu</button>
        </div>
      </section>
    </div>

    <div v-if="refundUpdateTicket" class="fixed inset-0 z-[10000] flex items-center justify-center bg-slate-950/70 p-4 backdrop-blur-sm animate-fade-in" @click.self="closeRefundUpdate">
      <form class="w-full max-w-lg overflow-hidden rounded-3xl bg-white shadow-[0_24px_80px_rgba(0,35,32,0.35)] animate-scale-up" @submit.prevent="submitRefundUpdate">
        <div class="relative bg-[#075955] px-7 py-6 text-white">
          <button type="button" class="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full bg-white/10 text-white/80 hover:bg-white/20" aria-label="Đóng" @click="closeRefundUpdate"><span class="material-symbols-outlined">close</span></button>
          <p class="text-[10px] font-black uppercase tracking-[0.16em] text-emerald-200">Yêu cầu hoàn tiền #RF{{ refundUpdateTicket.refundRequest.id }}</p>
          <h3 class="mt-1 text-xl font-black">Cập nhật tài khoản nhận tiền</h3>
          <p class="mt-2 pr-8 text-xs leading-5 text-emerald-100">Kiểm tra kỹ thông tin trước khi gửi lại cho nhà xe.</p>
        </div>
        <div class="grid gap-4 p-7 sm:grid-cols-2">
          <div class="rounded-xl border border-rose-100 bg-rose-50 p-3 text-xs font-semibold leading-5 text-rose-700 sm:col-span-2">
            <b>Nội dung cần bổ sung:</b> {{ refundUpdateTicket.refundRequest.adminNote || 'Vui lòng kiểm tra lại thông tin nhận tiền.' }}
          </div>
          <label class="refund-field"><span>Ngân hàng</span><select v-model="refundUpdateForm.bankName"><option value="" disabled>Chọn ngân hàng</option><option v-for="bank in vietnameseBanks" :key="bank.code" :value="bank.name">{{ bank.code }} · {{ bank.name }}</option></select></label>
          <label class="refund-field"><span>Số tài khoản</span><input v-model.trim="refundUpdateForm.bankAccountNumber" inputmode="numeric" maxlength="30" placeholder="Nhập số tài khoản" /></label>
          <label class="refund-field sm:col-span-2"><span>Tên chủ tài khoản</span><input v-model.trim="refundUpdateForm.bankAccountName" maxlength="150" placeholder="NGUYEN VAN A" /></label>
          <p v-if="refundUpdateError" class="rounded-xl bg-rose-50 px-3 py-2.5 text-xs font-bold text-rose-700 sm:col-span-2">{{ refundUpdateError }}</p>
          <div class="flex justify-end gap-3 sm:col-span-2">
            <button type="button" class="rounded-xl bg-slate-100 px-4 py-3 text-xs font-black text-slate-600" @click="closeRefundUpdate">Đóng</button>
            <button type="submit" :disabled="refundUpdateSubmitting" class="rounded-xl bg-[#075955] px-5 py-3 text-xs font-black text-white disabled:opacity-60">{{ refundUpdateSubmitting ? 'Đang gửi lại...' : 'Gửi lại yêu cầu' }}</button>
          </div>
        </div>
      </form>
    </div>
    </Teleport>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

const activeTab = ref('upcoming');
const allTickets = ref([]);

const upcomingTickets = computed(() => {
  return allTickets.value.filter(t => t.status === 'PAID' || t.status === 'PENDING').sort((a,b) => Number(b.id) - Number(a.id));
});

const completedTickets = computed(() => {
  return allTickets.value.filter(t => t.status === 'CHECKED_IN' || t.status === 'COMPLETED' || t.status === 'CANCELLED').sort((a,b) => Number(b.id) - Number(a.id));
});
const isModalOpen = ref(false);
const selectedTicket = ref(null);
const allBuses = ref([]);

const authStore = useAuthStore();
const api = useApi();
const ticketReference = ticket => ticket?.ticketCode || ticket?.id || '';
const vietnameseBanks = [
  { code: 'VCB', name: 'Vietcombank' },
  { code: 'BIDV', name: 'BIDV' },
  { code: 'CTG', name: 'VietinBank' },
  { code: 'AGR', name: 'Agribank' },
  { code: 'TCB', name: 'Techcombank' },
  { code: 'MB', name: 'MB Bank' },
  { code: 'ACB', name: 'ACB' },
  { code: 'VPB', name: 'VPBank' },
  { code: 'TPB', name: 'TPBank' },
  { code: 'STB', name: 'Sacombank' },
  { code: 'HDB', name: 'HDBank' },
  { code: 'VIB', name: 'VIB' },
  { code: 'SHB', name: 'SHB' },
  { code: 'OCB', name: 'OCB' },
  { code: 'MSB', name: 'MSB' },
  { code: 'SEAB', name: 'SeABank' },
  { code: 'EIB', name: 'Eximbank' },
  { code: 'NAB', name: 'Nam A Bank' },
  { code: 'VAB', name: 'VietABank' },
  { code: 'NCB', name: 'NCB' }
];

const loadHistory = async () => {
  if (authStore.isLoggedIn) {
    try {
      const [response, busesRes, exchangesRes, stopSelectionsRes, refundsRes] = await Promise.all([
        // Thêm timestamp để chống cache trình duyệt giống hệt ProfileView
        api.get(`/auth/me/bookings?t=${new Date().getTime()}`),
        api.get('/buses').catch(() => ({ data: [] })),
        api.get(`/ticket-exchanges/me?t=${new Date().getTime()}`, {
          headers: { 'Cache-Control': 'no-cache' }
        }).catch(error => {
          console.error('Lỗi lấy lịch sử đổi vé:', error.response?.data || error.message);
          return { data: [] };
        }),
        api.get(`/route-stops/me/bookings?t=${new Date().getTime()}`).catch(() => ({ data: [] })),
        api.get(`/refund-requests/me?t=${new Date().getTime()}`).catch(() => ({ data: [] }))
      ]);
      allBuses.value = busesRes.data;
      if (response.data && Array.isArray(response.data)) {
        const exchangeMap = new Map((Array.isArray(exchangesRes.data) ? exchangesRes.data : []).map(item => [Number(item.bookingId), item]));
        const stopSelectionMap = new Map((Array.isArray(stopSelectionsRes.data) ? stopSelectionsRes.data : []).map(item => [Number(item.bookingId), item]));
        const refundMap = new Map((Array.isArray(refundsRes.data) ? refundsRes.data : []).map(item => [Number(item.booking?.id), item]));
        (Array.isArray(refundsRes.data) ? refundsRes.data : [])
          .filter(item => ['NEEDS_INFO', 'REJECTED'].includes(item.status))
          .forEach(item => {
            const alreadyNotified = authStore.notifications.some(notification =>
              ['REFUND_NEEDS_INFO', 'REFUND_REJECTED'].includes(notification.type) && Number(notification.refundId) === Number(item.id)
            );
            if (!alreadyNotified) {
              authStore.addNotification({
                type: 'REFUND_NEEDS_INFO',
                title: 'Cần bổ sung thông tin hoàn tiền',
                message: `Yêu cầu hoàn tiền vé #${item.booking?.id} cần bổ sung thông tin. Nội dung: ${item.adminNote || 'Vui lòng kiểm tra lại thông tin nhận tiền.'}`,
                amount: Number(item.refundAmount || 0),
                status: item.status,
                refundId: item.id,
                bookingId: item.booking?.id,
                date: item.updatedAt || new Date().toISOString()
              });
            }
          });
        const mappedServerTickets = response.data.map(b => {
          const bus = allBuses.value.find(bus => bus.licensePlate === b.trip.assignedLicensePlate);
          return {
            id: b.id,
            ticketCode: b.ticketCode,
            from: b.trip.departurePoint,
            to: b.trip.arrivalPoint,
            busType: b.trip.busType,
            time: b.trip.departureTime,
            date: b.trip.departureDate,
            seats: Array.isArray(b.seatNumbers) ? b.seatNumbers.join(', ') : b.seatNumbers,
            method: b.paymentMethod,
            total: b.totalPrice,
            status: b.status,
            imageUrl: b.trip.imageUrl,
            licensePlate: b.trip.assignedLicensePlate,
            driverName: b.trip.assignedDriverFullName ? b.trip.assignedDriverFullName : (bus && bus.driverName ? bus.driverName : 'Đang cập nhật'),
            isReviewed: b.reviewed || b.isReviewed || false,
            userReview: b.userReview || null,
            exchange: exchangeMap.get(Number(b.id)) || null,
            stopSelection: stopSelectionMap.get(Number(b.id)) || null,
            refundRequest: refundMap.get(Number(b.id)) || null
          };
        });
        
        // NẾU ĐÃ ĐĂNG NHẬP -> CHỈ DÙNG DỮ LIỆU TỪ SERVER (để đảm bảo đủ trạng thái, hình ảnh, v.v.)
        allTickets.value = mappedServerTickets;
        return; // Dừng tại đây, không cần merge local
      }
    } catch (err) {
      console.error("Lỗi lấy lịch sử từ server:", err);
    }
  }

  // CHỈ DÙNG LOCAL STORAGE NẾU LÀ KHÁCH VÃNG LAI (CHƯA ĐĂNG NHẬP)
  let localTickets = [];
  const stored = localStorage.getItem('trungnam_history') || localStorage.getItem('saomaifly_history') || localStorage.getItem('skybus_history');
  if (stored) {
    try {
      const parsed = JSON.parse(stored);
      localTickets = parsed.map(t => ({
        ...t,
        status: t.status || (t.method === 'CASH' ? 'PENDING' : 'PAID')
      }));
    } catch (err) {
      console.error("Lỗi nạp lịch sử local:", err);
    }
  }
  
  allTickets.value = localTickets;
};

const openQrModal = (ticket) => {
  selectedTicket.value = ticket;
  isModalOpen.value = true;
};

const formatExchangeDate = value => value ? new Date(value).toLocaleString('vi-VN') : '';
const exchangeRoute = ticket => {
  if (!ticket) return '';
  if (!ticket.departurePoint && !ticket.arrivalPoint) return `Chuyến #${ticket.tripId}`;
  return `${ticket.departurePoint} → ${ticket.arrivalPoint}`;
};
const exchangeSchedule = ticket => {
  if (!ticket) return '';
  const date = ticket.departureDate ? ticket.departureDate.split('-').reverse().join('/') : 'Chưa cập nhật';
  return `${ticket.departureTime || '--:--'} · ${date}`;
};
const exchangeBusLine = ticket => {
  if (!ticket) return 'Chưa cập nhật dòng xe';
  const company = String(ticket.companyName || '').trim();
  const busType = String(ticket.busType || '').trim();
  const licensePlate = String(ticket.licensePlate || '').trim();
  const name = [company, busType].filter(Boolean).join(' · ');
  return [name || 'Chưa cập nhật dòng xe', licensePlate].filter(Boolean).join(' · ');
};

const closeModal = () => {
  isModalOpen.value = false;
};


// Đánh giá nhà xe
const isReviewModalOpen = ref(false);
const isSubmittingReview = ref(false);
const reviewForm = ref({ rating: 0, comment: '' });

const openReviewModal = (ticket) => {
  if (!authStore.isLoggedIn) {
    alert("Vui lòng đăng nhập để đánh giá chuyến đi!");
    return;
  }
  selectedTicket.value = ticket;
  reviewForm.value = { rating: 5, comment: '' };
  isReviewModalOpen.value = true;
};

const closeReviewModal = () => {
  isReviewModalOpen.value = false;
};

const submitReview = async () => {
  if (reviewForm.value.rating === 0) return;
  isSubmittingReview.value = true;
  try {
    const user = authStore.currentUser;
    if (!user) throw new Error("Vui lòng đăng nhập!");
    await api.post(`/reviews/create/${user.id}`, {
      bookingId: selectedTicket.value.id,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment
    });
    alert("Cảm ơn bạn đã đánh giá chuyến đi!");
    if (selectedTicket.value) {
      selectedTicket.value.isReviewed = true;
      selectedTicket.value.userReview = {
        rating: reviewForm.value.rating,
        comment: reviewForm.value.comment
      };
    }
    closeReviewModal();
  } catch (err) {
    alert("Gửi đánh giá thất bại: " + (err.response?.data?.message || err.message));
  } finally {
    isSubmittingReview.value = false;
  }
};

// Hủy vé
const isCancelModalOpen = ref(false);
const cancellationSuccess = ref(null);
const refundUpdateTicket = ref(null);
const refundUpdateForm = ref({ bankName: '', bankAccountNumber: '', bankAccountName: '' });
const refundUpdateError = ref('');
const refundUpdateSubmitting = ref(false);
const cancelReason = ref('');
const guestCancellationPhone = ref('');
const isCancelling = ref(false);
const hoursToDeparture = ref(24);
const refundMethod = ref('WALLET');
const bankInfo = ref({ bankName: '', accountNumber: '', accountName: '' });
const cancelError = ref('');

const refundEligible = computed(() => selectedTicket.value?.status === 'PAID' && selectedTicket.value?.method !== 'CASH');
const canSubmitCancellation = computed(() => {
  if (!cancelReason.value) return false;
  if (!authStore.isLoggedIn && !/^\d{10,11}$/.test(guestCancellationPhone.value.replace(/\s+/g, ''))) return false;
  if (!refundEligible.value || refundMethod.value === 'WALLET') return true;
  const accountNumber = bankInfo.value.accountNumber.replace(/\s+/g, '');
  return Boolean(bankInfo.value.bankName && bankInfo.value.accountName && /^\d{6,30}$/.test(accountNumber));
});

const openCancelModal = (ticket) => {
  selectedTicket.value = ticket;
  cancelReason.value = '';
  guestCancellationPhone.value = ticket.customerPhone || '';
  refundMethod.value = authStore.isLoggedIn ? 'WALLET' : 'BANK_TRANSFER';
  bankInfo.value = { bankName: '', accountNumber: '', accountName: '' };
  cancelError.value = '';
  
  // Tính toán số giờ còn lại trước khi xe chạy
  try {
    const depDateTime = new Date(`${ticket.date}T${ticket.time}`);
    const now = new Date();
    const diffMs = depDateTime.getTime() - now.getTime();
    hoursToDeparture.value = Math.max(0, diffMs / (1000 * 60 * 60));
  } catch (e) {
    console.error("Lỗi tính giờ hủy:", e);
    hoursToDeparture.value = 24;
  }

  isCancelModalOpen.value = true;
};

const closeCancelModal = () => {
  isCancelModalOpen.value = false;
};

const closeCancellationSuccess = () => {
  cancellationSuccess.value = null;
};

const openRefundUpdate = ticket => {
  refundUpdateTicket.value = ticket;
  refundUpdateForm.value = {
    bankName: normalizeBankName(ticket.refundRequest.bankName),
    bankAccountNumber: ticket.refundRequest.bankAccountNumber || '',
    bankAccountName: ticket.refundRequest.bankAccountName || ''
  };
  refundUpdateError.value = '';
};

const normalizeBankName = value => {
  const current = String(value || '').trim().toLocaleLowerCase('vi-VN');
  return vietnameseBanks.find(bank => bank.code.toLocaleLowerCase('vi-VN') === current || bank.name.toLocaleLowerCase('vi-VN') === current)?.name || '';
};

const closeRefundUpdate = () => {
  if (!refundUpdateSubmitting.value) refundUpdateTicket.value = null;
};

const submitRefundUpdate = async () => {
  const form = refundUpdateForm.value;
  if (!form.bankName || !/^\d{6,30}$/.test(form.bankAccountNumber.replace(/\s+/g, '')) || !form.bankAccountName) {
    refundUpdateError.value = 'Vui lòng nhập đầy đủ ngân hàng, số tài khoản hợp lệ và tên chủ tài khoản.';
    return;
  }
  refundUpdateSubmitting.value = true;
  refundUpdateError.value = '';
  try {
    const refundId = refundUpdateTicket.value.refundRequest.id;
    await api.put(`/refund-requests/me/${refundId}/resubmit`, form);
    authStore.addNotification({
      type: 'REFUND_APPROVED',
      title: 'Đã gửi lại yêu cầu hoàn tiền',
      message: `Thông tin nhận tiền cho vé #${refundUpdateTicket.value.id} đã được cập nhật và đang chờ admin xử lý.`,
      refundId,
      date: new Date().toISOString()
    });
    refundUpdateTicket.value = null;
    await loadHistory();
  } catch (error) {
    refundUpdateError.value = error.response?.data?.message || 'Không thể gửi lại yêu cầu hoàn tiền.';
  } finally {
    refundUpdateSubmitting.value = false;
  }
};

const confirmCancel = async () => {
  if (!canSubmitCancellation.value) return;
  isCancelling.value = true;
  cancelError.value = '';
  try {
    const cancellationReference = authStore.isLoggedIn
      ? selectedTicket.value.id
      : ticketReference(selectedTicket.value);
    const res = await api.post(`/auth/me/bookings/${encodeURIComponent(cancellationReference)}/cancel`, {
      customerPhone: authStore.isLoggedIn ? null : guestCancellationPhone.value,
      reason: cancelReason.value,
      refundMethod: refundEligible.value ? refundMethod.value : 'NONE',
      bankName: refundMethod.value === 'BANK_TRANSFER' ? bankInfo.value.bankName : null,
      bankAccountNumber: refundMethod.value === 'BANK_TRANSFER' ? bankInfo.value.accountNumber : null,
      bankAccountName: refundMethod.value === 'BANK_TRANSFER' ? bankInfo.value.accountName : null
    });
    
    // Cập nhật số dư ví trong store nếu có
    if (res.data.walletBalance !== undefined) {
      authStore.updateWalletBalance(res.data.walletBalance);
    }
    
    if (res.data.refundStatus === 'PENDING') {
      authStore.addNotification({
        type: 'REFUND',
        title: 'Đã gửi yêu cầu hoàn tiền',
        message: `Yêu cầu hoàn ${Number(res.data.refundAmount).toLocaleString('vi-VN')}đ qua ngân hàng đang chờ admin xử lý.`,
        date: new Date().toISOString()
      });
    } else {
      // Hoàn vào ví được xử lý ngay trên máy chủ.
    }
    isCancelModalOpen.value = false;
    activeTab.value = 'completed';
    cancellationSuccess.value = {
      amount: Number(res.data.refundAmount || 0),
      method: res.data.refundMethod
    };
    if (!authStore.isLoggedIn) {
      const historyKey = localStorage.getItem('trungnam_history') !== null
        ? 'trungnam_history'
        : (localStorage.getItem('saomaifly_history') !== null ? 'saomaifly_history' : 'skybus_history');
      const history = JSON.parse(localStorage.getItem(historyKey) || '[]');
      history.forEach(ticket => {
        if (String(ticket.id) === String(selectedTicket.value.id)) ticket.status = 'CANCELLED';
      });
      localStorage.setItem(historyKey, JSON.stringify(history));
    }
    loadHistory(); // Tải lại danh sách
  } catch (err) {
    cancelError.value = err.response?.data?.message || err.response?.data || err.message || 'Không thể hủy vé.';
  } finally {
    isCancelling.value = false;
  }
};

const refundStatusText = status => ({ PENDING: 'Chờ admin xử lý', APPROVED: 'Đã duyệt', COMPLETED: 'Đã hoàn tiền', NEEDS_INFO: 'Cần bổ sung', REJECTED: 'Cần bổ sung' }[status] || status);
const refundStatusClass = status => ({ PENDING: 'pending', APPROVED: 'approved', COMPLETED: 'completed', NEEDS_INFO: 'rejected', REJECTED: 'rejected' }[status] || 'pending');
const maskBankAccount = value => {
  const account = String(value || '');
  return account.length > 4 ? `•••• ${account.slice(-4)}` : account;
};
const money = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`;

const handleRefundStatusUpdate = () => {
  loadHistory();
};

onMounted(() => {
  loadHistory();
  window.addEventListener('refund-status-updated', handleRefundStatusUpdate);
});

onUnmounted(() => {
  window.removeEventListener('refund-status-updated', handleRefundStatusUpdate);
});
</script>

<style scoped>
.refund-method { display: flex; min-height: 4rem; align-items: center; gap: .65rem; border: 1px solid #dce5e3; border-radius: .9rem; padding: .7rem; color: #596b68; background: #fff; text-align: left; transition: border-color .2s ease, background .2s ease, transform .2s ease; }
.refund-method:hover { border-color: #9fc8c2; background: #f5faf9; }
.refund-method:active { transform: translateY(1px); }
.refund-method.active { border-color: #087269; color: #075955; background: #eef8f6; box-shadow: 0 0 0 3px rgb(8 114 105 / .09); }
.refund-method > .material-symbols-outlined { font-size: 1.35rem; }
.refund-method span:last-child { display: flex; min-width: 0; flex-direction: column; }
.refund-method b { font-size: .75rem; font-weight: 850; }
.refund-method small { margin-top: .1rem; font-size: .62rem; font-weight: 650; opacity: .7; }
.refund-field { display: grid; gap: .35rem; }
.refund-field > span { color: #687a76; font-size: .62rem; font-weight: 800; }
.refund-field input, .refund-field select { width: 100%; border: 1px solid #d8e2e0; border-radius: .7rem; padding: .68rem .75rem; outline: none; color: #263b37; background: #fff; font-size: .75rem; font-weight: 700; }
.refund-field select { cursor: pointer; }
.refund-field input:focus, .refund-field select:focus { border-color: #087269; box-shadow: 0 0 0 3px rgb(8 114 105 / .09); }
.refund-status { border-radius: .55rem; padding: .38rem .55rem; font-size: .62rem; font-weight: 850; white-space: nowrap; }
.refund-status.pending { color: #9a6708; background: #fff4d6; }
.refund-status.approved { color: #1d5f91; background: #eaf5ff; }
.refund-status.completed { color: #08705e; background: #e8f7f1; }
.refund-status.rejected { color: #ad334b; background: #fff0f3; }
.cancel-modal-panel { scrollbar-width: thin; scrollbar-color: #a9bbb7 transparent; }
.cancel-modal-panel::-webkit-scrollbar { width: 6px; }
.cancel-modal-panel::-webkit-scrollbar-thumb { border-radius: 999px; background: #a9bbb7; }
.cancel-modal-panel::-webkit-scrollbar-track { margin: 1.25rem 0; background: transparent; }
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes scaleUp {
  from { transform: scale(0.9) translateY(10px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-slide-up {
  animation: slideUp 0.45s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@media (max-width: 767px) {
  .history-content {
    padding: 0 0.75rem 6rem !important;
  }

  .history-main {
    padding-block: 0.75rem !important;
    row-gap: 0.85rem !important;
  }

  .history-heading {
    margin-bottom: 0 !important;
    padding: 1rem !important;
    border-radius: 1rem !important;
    background: #075955 !important;
    box-shadow: 0 0.55rem 1.4rem rgb(7 89 85 / 0.16) !important;
  }

  .history-heading > div:first-child {
    display: none;
  }

  .history-heading h1 {
    gap: 0.45rem !important;
    font-size: 1.1rem !important;
    line-height: 1.25 !important;
  }

  .history-heading h1 .material-symbols-outlined {
    font-size: 1.4rem !important;
  }

  .history-heading p {
    margin-top: 0.25rem !important;
    font-size: 0.58rem !important;
    line-height: 1.35 !important;
    letter-spacing: 0.09em !important;
  }

  .history-tabs {
    position: sticky;
    top: 4.5rem;
    z-index: 30;
    gap: 0.3rem;
    margin-bottom: 0 !important;
    padding: 0.3rem !important;
    border-radius: 0.9rem !important;
    box-shadow: 0 0.35rem 1rem rgb(15 23 42 / 0.08) !important;
  }

  .history-tab {
    min-width: 0;
    min-height: 2.75rem;
    gap: 0.35rem !important;
    padding: 0.55rem 0.35rem !important;
    border-radius: 0.68rem !important;
    font-size: 0.64rem !important;
    line-height: 1.15 !important;
    white-space: nowrap;
  }

  .history-tab .material-symbols-outlined {
    display: none;
  }

  .history-tab b {
    display: grid;
    min-width: 1.15rem;
    height: 1.15rem;
    place-items: center;
    border-radius: 999px;
    background: rgb(255 255 255 / 0.18);
    font-size: 0.58rem;
  }

  .history-tab:not(.text-white) b {
    background: #f1f5f9;
  }

  .ticket-list {
    gap: 0.85rem !important;
  }

  .history-empty {
    min-height: clamp(18rem, 44dvh, 25rem);
    padding: 2.75rem 1.25rem !important;
    border-style: solid !important;
    border-radius: 1rem !important;
    box-shadow: 0 0.5rem 1.5rem rgb(15 23 42 / 0.04) !important;
  }

  .history-empty > div:first-child {
    width: 3.5rem !important;
    height: 3.5rem !important;
    margin-bottom: 0.85rem !important;
  }

  .history-empty > div:first-child .material-symbols-outlined,
  .history-empty > .material-symbols-outlined {
    margin-bottom: 0.85rem !important;
    font-size: 2.4rem !important;
  }

  .history-empty h3 {
    font-size: 1rem !important;
    line-height: 1.35 !important;
  }

  .history-empty p {
    max-width: 17rem;
    font-size: 0.72rem !important;
    line-height: 1.5 !important;
  }

  .history-empty button {
    width: 100%;
    max-width: 12rem;
    min-height: 2.75rem;
    margin-top: 1.25rem !important;
    border-radius: 0.75rem !important;
    font-size: 0.72rem !important;
  }

  .ticket-card {
    border-radius: 1rem !important;
  }

  .ticket-card-body {
    padding: 1rem !important;
  }

  .ticket-card-header {
    flex-direction: column;
    gap: 0.85rem;
    margin-bottom: 1rem !important;
  }

  .ticket-card-header > div:first-child {
    width: 100%;
  }

  .ticket-card-header h2 {
    font-size: 1rem !important;
  }

  .ticket-actions {
    display: grid !important;
    width: 100%;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0.4rem !important;
  }

  .ticket-actions > * {
    min-width: 0;
    min-height: 2.75rem;
    flex-direction: row !important;
    justify-content: center;
    gap: 0.3rem !important;
    padding: 0.45rem !important;
    border-radius: 0.7rem !important;
  }

  .ticket-actions .material-symbols-outlined {
    font-size: 1.15rem !important;
  }

  .ticket-actions span:last-child {
    font-size: 0.52rem !important;
    letter-spacing: 0.04em !important;
  }

  .ticket-summary {
    display: grid !important;
    gap: 0.85rem;
  }

  .ticket-details {
    width: 100%;
    gap: 0.75rem !important;
  }

  .ticket-price {
    width: 100%;
    padding: 0.75rem 0 0 !important;
    border-top: 1px solid #f1f5f9;
    border-left: 0 !important;
    text-align: left !important;
  }
}

@media (prefers-reduced-motion: reduce) {
  .animate-fade-in,
  .animate-scale-up,
  .animate-slide-up {
    animation: none !important;
  }
}
</style>
