<template>
  <main class="login-page -mx-container-margin -mt-stack-space">
    <section class="login-shell" aria-labelledby="login-title">
      <aside class="brand-panel">
        <img
          class="brand-panel__image"
          :src="loginCoachImage"
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
            <p>Hành trình an tâm</p>
            <h1 id="login-title">Chào mừng trở lại</h1>
            <span>Đăng nhập để tiếp tục hành trình cùng Trung Nam.</span>
          </div>
        </div>
      </aside>

      <div class="form-panel">
        <div class="form-panel__inner">
          <header class="form-heading">
            <p>Đăng nhập tài khoản</p>
            <h2>Tiếp tục chuyến đi của bạn</h2>
            <span>Quản lý vé, lịch trình và thanh toán tại một nơi.</span>
          </header>

          <form class="login-form" @submit.prevent="handleLogin">
            <div class="field-group">
              <label for="login-phone">Số điện thoại</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">call</span>
                <input
                  id="login-phone"
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
              <label for="login-password">Mật khẩu</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                <input
                  id="login-password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="current-password"
                  placeholder="Nhập mật khẩu"
                  required
                />
                <button
                  type="button"
                  class="password-toggle"
                  :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                  :aria-pressed="showPassword"
                  @click="showPassword = !showPassword"
                >
                  <span class="material-symbols-outlined" aria-hidden="true">
                    {{ showPassword ? 'visibility_off' : 'visibility' }}
                  </span>
                </button>
              </div>
            </div>

            <div v-if="errorMsg" class="error-message" role="alert">
              <span class="material-symbols-outlined" aria-hidden="true">error</span>
              <span>{{ errorMsg }}</span>
            </div>

            <button class="submit-button" type="submit" :disabled="loading">
              <span v-if="loading" class="loading-ring" aria-hidden="true"></span>
              <span>{{ loading ? 'Đang xác minh...' : 'Đăng nhập' }}</span>
              <span v-if="!loading" class="material-symbols-outlined" aria-hidden="true">arrow_forward</span>
            </button>
          </form>

          <div class="divider" aria-hidden="true"><span>hoặc</span></div>

          <div class="google-login-wrap">
            <GoogleLogin
              :callback="handleGoogleLogin"
              :button-config="googleButtonConfig"
            />
          </div>

          <div class="register-prompt">
            <span>Chưa có tài khoản?</span>
            <button type="button" @click="router.push('/auth/register')">Đăng ký ngay</button>
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
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import loginCoachImage from '@/assets/login-coach-night.png';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const phone = ref('');
const password = ref('');
const showPassword = ref(false);
const loading = ref(false);
const errorMsg = ref('');

const googleButtonConfig = {
  type: 'standard',
  theme: 'outline',
  size: 'large',
  text: 'continue_with',
  shape: 'rectangular',
  logo_alignment: 'left',
  width: 320,
};

const getRedirectPath = () => route.query.redirect || (
  authStore.isAdmin
    ? '/admin'
    : authStore.isInspector
      ? '/inspector'
      : authStore.isDriver
        ? '/driver'
        : '/'
);

const handleGoogleLogin = async (response) => {
  if (!response?.credential) return;

  errorMsg.value = '';
  loading.value = true;
  try {
    await authStore.googleLogin(response.credential);
    const redirect = getRedirectPath();
    router.push(authStore.needsPhoneCompletion
      ? { path: '/auth/complete-phone', query: { redirect } }
      : redirect);
  } catch (error) {
    console.error('Đăng nhập Google thất bại:', error);
    errorMsg.value = typeof error.response?.data === 'string'
      ? error.response.data
      : 'Không thể đăng nhập bằng Google. Vui lòng thử lại.';
  } finally {
    loading.value = false;
  }
};

const handleLogin = async () => {
  errorMsg.value = '';
  loading.value = true;

  try {
    await authStore.login(phone.value, password.value);
    router.push(getRedirectPath());
  } catch (error) {
    console.error('Đăng nhập thất bại:', error);
    if (error.response?.data) {
      errorMsg.value = typeof error.response.data === 'string'
        ? error.response.data
        : 'Số điện thoại hoặc mật khẩu không chính xác.';
    } else {
      errorMsg.value = 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
@import '@/assets/auth.css';
</style>
