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
                  :disabled="codeSent"
                />
              </div>
            </div>

            <div class="field-group">
              <label for="register-email">Email</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                <input
                  id="register-email"
                  v-model.trim="email"
                  type="email"
                  autocomplete="email"
                  placeholder="Ví dụ: example@gmail.com"
                  required
                  :disabled="codeSent"
                />
              </div>
            </div>

            <div v-if="codeSent" class="field-group">
              <label for="register-verification-code">Mã xác nhận email</label>
              <div class="field-control">
                <span class="material-symbols-outlined" aria-hidden="true">mark_email_read</span>
                <input
                  id="register-verification-code"
                  v-model.trim="verificationCode"
                  type="text"
                  inputmode="numeric"
                  autocomplete="one-time-code"
                  placeholder="Nhập mã 6 số"
                  maxlength="6"
                  pattern="[0-9]{6}"
                  required
                />
              </div>
              <div class="verification-actions">
                <span>Mã có hiệu lực trong 10 phút.</span>
                <button type="button" :disabled="loading || resendSeconds > 0" @click="resendCode">
                  {{ resendSeconds > 0 ? `Gửi lại sau ${resendSeconds}s` : 'Gửi lại mã' }}
                </button>
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

            <div v-if="successMsg" class="success-message" role="status">
              <span class="material-symbols-outlined" aria-hidden="true">mark_email_read</span>
              <span>{{ successMsg }}</span>
            </div>

            <button class="submit-button" type="submit" :disabled="loading">
              <span v-if="loading" class="loading-ring" aria-hidden="true"></span>
              <span>{{ submitButtonLabel }}</span>
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
import { computed, onBeforeUnmount, ref } from 'vue';
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
const verificationCode = ref('');
const codeSent = ref(false);
const resendSeconds = ref(0);
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');
let resendTimer = null;

const submitButtonLabel = computed(() => {
  if (loading.value) return codeSent.value ? 'Đang xác nhận...' : 'Đang gửi mã...';
  return codeSent.value ? 'Xác nhận & tạo tài khoản' : 'Gửi mã xác nhận';
});

const startResendCountdown = () => {
  resendSeconds.value = 60;
  if (resendTimer) clearInterval(resendTimer);
  resendTimer = setInterval(() => {
    resendSeconds.value -= 1;
    if (resendSeconds.value <= 0) {
      clearInterval(resendTimer);
      resendTimer = null;
    }
  }, 1000);
};

const errorMessageOf = error => {
  if (typeof error.response?.data === 'string') return error.response.data;
  return error.response?.data?.message || 'Máy chủ đang bận! Vui lòng thử lại sau.';
};

const sendCode = async () => {
  await authStore.sendRegistrationCode(email.value, phone.value);
  codeSent.value = true;
  verificationCode.value = '';
  successMsg.value = `Mã xác nhận đã được gửi đến ${email.value}.`;
  startResendCountdown();
};

const resendCode = async () => {
  if (resendSeconds.value > 0) return;
  errorMsg.value = '';
  successMsg.value = '';
  loading.value = true;
  try {
    await sendCode();
  } catch (error) {
    errorMsg.value = errorMessageOf(error);
  } finally {
    loading.value = false;
  }
};

const handleRegister = async () => {
  errorMsg.value = '';
  successMsg.value = '';

  if (password.value !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu nhập lại không khớp!';
    return;
  }
  
  loading.value = true;
  
  try {
    if (!codeSent.value) {
      await sendCode();
      return;
    }
    await authStore.register(fullName.value, phone.value, password.value, email.value, verificationCode.value);
    router.push('/');
  } catch (error) {
    console.error("Đăng ký thất bại:", error);
    errorMsg.value = errorMessageOf(error);
  } finally {
    loading.value = false;
  }
};

onBeforeUnmount(() => {
  if (resendTimer) clearInterval(resendTimer);
});
</script>

<style scoped>
@import '@/assets/auth.css';
.success-message { display:flex; align-items:flex-start; gap:9px; padding:12px 14px; border:1px solid #a7f3d0; border-radius:13px; color:#087467; background:#f0fdf8; font-size:13px; font-weight:650; line-height:1.45; }
.success-message .material-symbols-outlined { margin-top:1px; font-size:18px; }
.verification-actions { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-top:7px; color:#718096; font-size:11px; font-weight:600; }
.verification-actions button { border:0; padding:0; color:#075fcc; background:transparent; font-size:11px; font-weight:800; cursor:pointer; }
.verification-actions button:disabled { color:#94a3b8; cursor:not-allowed; }
.field-control input:disabled { cursor:not-allowed; color:#64748b; background:#f1f5f9; }
</style>
