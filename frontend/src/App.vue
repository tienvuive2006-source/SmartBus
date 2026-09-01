<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import RouteAccessToast from '@/components/common/RouteAccessToast.vue'

onMounted(() => {
  // Chờ app render xong, thêm một chút delay để hiệu ứng mượt mà
  setTimeout(() => {
    const splash = document.getElementById('global-splash');
    if (splash) {
      // Kích hoạt hiệu ứng mờ dần và phóng to nhẹ
      splash.classList.add('fade-out');
      
      // Mở khóa thanh cuộn cho body
      document.body.style.overflow = 'auto';
      document.body.style.overflowX = 'hidden';
      
      // Xóa hẳn khỏi DOM sau khi hiệu ứng mờ dần (0.5s) kết thúc
      setTimeout(() => {
        splash.remove();
      }, 500);
    }
  }, 800); // Đã giảm thời gian chờ xuống 0.8s để vào trang nhanh hơn (thay vì 2.5s)
})
</script>

<template>
  <RouteAccessToast />
  <RouterView />
</template>
