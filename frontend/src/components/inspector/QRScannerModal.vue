<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[60] flex items-center justify-center bg-slate-900/90 backdrop-blur-sm animate-fade-in">
      <div class="relative w-full max-w-md bg-white sm:rounded-3xl shadow-2xl overflow-hidden flex flex-col h-full sm:h-auto">
        
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-white shrink-0">
          <h3 class="text-title-md font-black text-slate-800">Quét mã QR Vé Xe</h3>
          <button @click="closeModal" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <div class="p-6 flex-1 flex flex-col items-center justify-center bg-slate-50">
          <div v-if="feedback" class="mb-4 text-center px-4 py-3 rounded-2xl w-full font-bold text-sm shadow-sm"
               :class="feedback.type === 'success' ? 'bg-emerald-100 text-emerald-700 border border-emerald-200' : 'bg-rose-100 text-rose-700 border border-rose-200'">
            {{ feedback.message }}
          </div>
          
          <div class="w-full max-w-[300px] bg-black rounded-3xl overflow-hidden relative shadow-lg aspect-square border-4 border-white flex items-center justify-center">
            <div id="qr-reader" class="w-full h-full"></div>
          </div>
          <p class="text-body-sm text-slate-500 mt-6 font-medium text-center">Hướng Camera vào mã QR trên vé của khách hàng để tự động xác nhận.</p>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { watch, nextTick, onBeforeUnmount } from 'vue';
import { Html5Qrcode, Html5QrcodeSupportedFormats } from 'html5-qrcode';

const props = defineProps({
  isOpen: Boolean,
  feedback: Object
});

const emit = defineEmits(['close', 'scan']);

let html5QrCode = null;

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    nextTick(() => {
      html5QrCode = new Html5Qrcode("qr-reader");
      html5QrCode.start(
        { facingMode: "environment" }, // Bắt buộc dùng camera sau
        {
          fps: 10,    // Tối ưu hóa FPS (10 là chuẩn nhất để không gây lag máy)
          // Bỏ giới hạn qrbox để quét TOÀN MÀN HÌNH Camera, bắt siêu nhạy không cần căn chuẩn!
          aspectRatio: 1.0,
          disableFlip: false, // Quét được cả mã bị ngược
          formatsToSupport: [ Html5QrcodeSupportedFormats.QR_CODE ] // CHỈ ƯU TIÊN QUÉT QR CODE -> NHANH GẤP 10 LẦN
        },
        onScanSuccess,
        onScanFailure
      ).catch(err => {
        console.error("Lỗi khởi động Camera:", err);
      });
    });
  } else {
    stopScanner();
  }
});

const stopScanner = () => {
  if (html5QrCode) {
    html5QrCode.stop().then(() => {
      html5QrCode.clear();
      html5QrCode = null;
    }).catch(console.error);
  }
};

const closeModal = () => {
  emit('close');
};

const onScanSuccess = (decodedText) => {
  emit('scan', decodedText);
};

const onScanFailure = () => {};

onBeforeUnmount(() => {
  stopScanner();
});
</script>

<style scoped>
:deep(#qr-reader) {
  border: none !important;
  border-radius: 1.5rem;
  overflow: hidden;
}
:deep(#qr-reader__scan_region) {
  background-color: black;
}
:deep(#qr-reader__dashboard) {
  background-color: white;
  padding: 10px;
}
:deep(#qr-reader button) {
  background-color: #0f172a;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 10px;
}
:deep(#qr-reader button:hover) {
  background-color: #334155;
}
:deep(#qr-reader select) {
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  width: 100%;
  margin-top: 10px;
  outline: none;
}
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
</style>
