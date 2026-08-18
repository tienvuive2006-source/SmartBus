<template>
  <main class="login-page -mx-container-margin -mt-stack-space">
    <section class="login-shell" aria-labelledby="register-title">
      <aside class="brand-panel">
        <img
          class="brand-panel__image"
          :src="registerCoachImage"
          alt="Xe khách Trung Nam di chuyển trên tuyến đường về thành phố"
        />
        <div class="brand-panel__scrim"></div>

        <div class="brand-panel__content">
          <div class="brand-mark" aria-label="Trung Nam Smart Bus">
            <span class="brand-mark__symbol">TN</span>
            <span class="brand-mark__copy">
              <strong>TRUNG NAM</strong>
              <small>SMART BUS</small>
            </span>
          </div>

          <div class="brand-panel__heading">
            <p>Khởi tạo tài khoản</p>
            <h1 id="register-title">Gia nhập cùng chúng tôi</h1>
            <span>Đăng ký để dễ dàng đặt vé, theo dõi chuyến đi và nhận ưu đãi độc quyền.</span>
          </div>
        </div>
      </aside>

      <div class="form-panel">
        <div class="form-panel__inner">
          <header class="form-heading">
            <p>Tạo tài khoản mới</p>
            <h2>Bắt đầu hành trình</h2>
            <span>Chỉ vài bước đơn giản để tạo tài khoản của bạn.</span>
          </header>

          <form class="login-form" @submit.prevent="handleRegister">
            <div class="field-group">
              <label for="register-fullname">Họ và tên thật</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">badge</span>
                <input
                  id="register-fullname"
                  v-model.trim="fullName"
                  type="text"
                  autocomplete="name"
                  placeholder="Ví dụ: Nguyễn Văn A"
                  required
                />
              </div>
            </div>

            <div class="field-group">
              <label for="register-phone">Số điện thoại</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">call</span>
                <input
                  id="register-phone"
                  v-model.trim="phone"
                  type="tel"
                  inputmode="tel"
                  autocomplete="tel"
                  placeholder="Nhập số điện thoại"
                  required
                />
              </div>
            </div>

            <div class="field-group">
              <label for="register-email">Email (Tùy chọn)</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                <input
                  id="register-email"
                  v-model.trim="email"
                  type="email"
                  autocomplete="email"
                  placeholder="Ví dụ: example@gmail.com"
                />
              </div>
            </div>

            <div class="field-row">
              <div class="field-group">
                <label for="register-password">Mật khẩu</label>
                <div class="field-control">
                  <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                  <input
                    id="register-password"
                    v-model="password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    placeholder="Tối thiểu 6 ký tự"
                    minlength="6"
                    required
                  />
                  <button
                    type="button"
                    class="password-toggle"
                    tabindex="-1"
                    :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click="showPassword = !showPassword"
                  >
                    <span class="material-symbols-outlined" aria-hidden="true">
                      {{ showPassword ? 'visibility_off' : 'visibility' }}
                    </span>
                  </button>
                </div>
              </div>

              <div class="field-group">
                <label for="register-confirm">Xác nhận</label>
                <div class="field-control">
                  <span class="material-symbols-outlined" aria-hidden="true">lock_reset</span>
                  <input
                    id="register-confirm"
                    v-model="confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    placeholder="Nhập lại"
                    minlength="6"
                    required
                  />
                </div>
              </div>
            </div>

            <div v-if="errorMsg" class="error-message" role="alert">
              <span class="material-symbols-outlined" aria-hidden="true">error</span>
              <span>{{ errorMsg }}</span>
            </div>

            <button class="submit-button" type="submit" :disabled="loading">
              <span v-if="loading" class="loading-ring" aria-hidden="true"></span>
              <span>{{ loading ? 'Đang khởi tạo...' : 'Tạo tài khoản' }}</span>
              <span v-if="!loading" class="material-symbols-outlined" aria-hidden="true">arrow_forward</span>
            </button>
          </form>

          <div class="register-prompt">
            <span>Đã có tài khoản?</span>
            <button type="button" @click="router.push('/auth/login')">Đăng nhập ngay</button>
          </div>

          <div class="trust-note">
            <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
            <span>Đặt vé an toàn · Thanh toán bảo mật</span>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import registerCoachImage from '@/assets/login-coach-night.png';

const router = useRouter();
const authStore = useAuthStore();

const fullName = ref('');
const phone = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);
const errorMsg = ref('');

const handleRegister = async () => {
  errorMsg.value = '';

  if (password.value !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu nhập lại không khớp!';
    return;
  }
  
  loading.value = true;
  
  try {
    await authStore.register(fullName.value, phone.value, password.value, email.value);
    router.push('/');
  } catch (error) {
    console.error("Đăng ký thất bại:", error);
    if (error.response?.data) {
      errorMsg.value = typeof error.response.data === 'string'
        ? error.response.data
        : "Số điện thoại đã tồn tại!";
    } else {
      errorMsg.value = "Máy chủ đang bận! Vui lòng thử lại sau.";
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
@import '@/assets/auth.css';
</style>
