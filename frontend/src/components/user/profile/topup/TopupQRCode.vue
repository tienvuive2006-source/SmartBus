<template>
  <div class="lg:col-span-6 flex flex-col h-full border-y py-8 lg:py-0 lg:border-y-0 lg:border-x border-slate-100 px-0 lg:px-8">
    <h4 class="text-sm font-black text-slate-800 mb-4 uppercase tracking-widest flex items-center gap-2">
      <span class="w-5 h-5 rounded-full bg-slate-800 text-white flex items-center justify-center text-[10px]">3</span>
      Quét mã QR để thanh toán
    </h4>
    
    <div class="bg-white border-2 border-dashed border-emerald-200 rounded-3xl p-6 flex flex-col items-center relative overflow-hidden shadow-sm">
       <!-- Countdown -->
       <div class="w-full flex justify-between items-center mb-6">
         <span class="text-xs font-bold text-slate-500">Giao dịch sẽ hết hạn sau</span>
         <div class="bg-emerald-100 text-emerald-700 px-3 py-1 rounded-full font-mono font-black text-sm tracking-widest flex items-center gap-1.5">
           <span class="material-symbols-outlined text-[14px]">timer</span>
           {{ formattedTime }}
         </div>
       </div>

       <img 
         :src="`https://img.vietqr.io/image/mb-0367093771-compact2.png?amount=${topupAmount}&addInfo=NAP${user.id}%20${topupAmount}&accountName=HUYNH%20DUC%20TIEN`" 
         alt="QR Code" 
         class="w-[320px] h-[320px] object-contain rounded-2xl bg-white border border-slate-100 shadow-sm"
       />
       
       <div class="flex items-center gap-2 mt-6">
         <div class="w-5 h-5 border-2 border-emerald-100 border-t-emerald-600 rounded-full animate-spin"></div>
         <span class="text-[11px] font-black text-emerald-600 uppercase tracking-widest">Đang chờ nhận tiền...</span>
       </div>
       
       <div class="w-full h-[1px] bg-slate-100 my-6"></div>
       
       <!-- Payment Info Text -->
       <div class="text-center w-full space-y-5">
         <div>
           <p class="text-[13px] font-bold text-slate-500 mb-1">Số tiền thanh toán</p>
           <p class="text-4xl font-black text-emerald-600 tracking-tight">{{ topupAmount.toLocaleString('vi-VN') }} <span class="underline underline-offset-4 decoration-4">đ</span></p>
         </div>
         
         <div class="bg-slate-50/80 p-4 rounded-2xl flex flex-col items-center">
           <p class="text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Nội dung chuyển khoản</p>
           <div class="flex items-center gap-3">
             <p class="text-2xl font-black text-slate-800 tracking-wider">NAP{{ user.id }} {{ topupAmount }}</p>
             <button @click="copyToClipboard(`NAP${user.id} ${topupAmount}`)" class="text-emerald-600 hover:text-emerald-700 font-bold text-sm flex items-center gap-1 transition-colors px-2 py-1 rounded hover:bg-emerald-50 whitespace-nowrap">
               <span class="material-symbols-outlined text-[16px]">content_copy</span> Sao chép
             </button>
           </div>
         </div>
       </div>
    </div>

    <!-- Steps Instructions -->
    <div class="mt-8 flex justify-between items-start gap-4">
      <div class="flex flex-col items-center text-center flex-1">
        <div class="w-8 h-8 rounded-full bg-slate-100 text-slate-500 font-black flex items-center justify-center text-sm mb-2">1</div>
        <p class="text-[10px] font-bold text-slate-500 uppercase">Mở ứng dụng Ngân hàng</p>
      </div>
      <div class="flex-1 flex justify-center mt-3"><span class="material-symbols-outlined text-slate-200">arrow_forward</span></div>
      <div class="flex flex-col items-center text-center flex-1">
        <div class="w-8 h-8 rounded-full bg-slate-100 text-slate-500 font-black flex items-center justify-center text-sm mb-2">2</div>
        <p class="text-[10px] font-bold text-slate-500 uppercase">Chọn "Quét mã QR"</p>
      </div>
      <div class="flex-1 flex justify-center mt-3"><span class="material-symbols-outlined text-slate-200">arrow_forward</span></div>
      <div class="flex flex-col items-center text-center flex-1">
        <div class="w-8 h-8 rounded-full bg-slate-100 text-slate-500 font-black flex items-center justify-center text-sm mb-2">3</div>
        <p class="text-[10px] font-bold text-slate-500 uppercase">Xác nhận thanh toán</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  topupAmount: {
    type: Number,
    required: true
  },
  user: {
    type: Object,
    required: true
  },
  formattedTime: {
    type: String,
    required: true
  }
});

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    alert('Đã sao chép: ' + text);
  } catch (err) {
    console.error('Không thể sao chép', err);
  }
};
</script>
