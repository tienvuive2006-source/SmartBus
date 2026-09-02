<template>
  <main class="login-page -mx-container-margin -mt-stack-space">
    <section class="login-shell" aria-labelledby="forgot-password-title">
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
            <p>Bảo vệ tài khoản</p>
            <h1 id="forgot-password-title">Lấy lại quyền truy cập</h1>
            <span>Mã xác nhận sẽ được gửi đến email đã đăng ký với tài khoản của bạn.</span>
          </div>
        </div>
      </aside>

      <div class="form-panel">
        <div class="form-panel__inner">
          <template v-if="resetComplete">
            <div class="completion-state" role="status">
              <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
              <div>
                <p>Đặt lại mật khẩu</p>
                <h2>Đã cập nhật mật khẩu</h2>
                <span>Bạn có thể đăng nhập bằng mật khẩu mới ngay bây giờ.</span>
              </div>
            </div>
            <button class="submit-button" type="button" @click="router.push('/auth/login')">
              <span>Về trang đăng nhập</span>
              <span class="material-symbols-outlined" aria-hidden="true">arrow_forward</span>
            </button>
          </template>

          <template v-else>
            <header class="form-heading">
              <p>{{ codeSent ? 'Xác nhận email' : 'Quên mật khẩu' }}</p>
              <h2>{{ codeSent ? 'Nhập mã và mật khẩu mới' : 'Nhận mã xác nhận' }}</h2>
              <span>
                {{ codeSent
                  ? `Mã gồm 6 chữ số đã được gửi đến ${maskedEmail}.`
                  : 'Nhập email đã dùng khi đăng ký tài khoản.' }}
              </span>
            </header>

            <form class="login-form" @submit.prevent="codeSent ? handleReset() : handleSendCode()">
              <div class="field-group">
                <label for="reset-email">Email</label>
                <div class="field-control">
                  <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                  <input
                    id="reset-email"
                    v-model.trim="email"
                    type="email"
                    placeholder="ban@example.com"
                    autocomplete="email"
                    :readonly="codeSent"
                    required
                  />
                </div>
              </div>

              <template v-if="codeSent">
                <div class="field-group">
                  <label for="reset-code">Mã xác nhận</label>
                  <div class="field-control code-control">
                    <span class="material-symbols-outlined" aria-hidden="true">pin</span>
                    <input
                      id="reset-code"
                      v-model.trim="code"
                      type="text"
                      inputmode="numeric"
                      pattern="[0-9]{6}"
                      maxlength="6"
                      placeholder="000000"
                      autocomplete="one-time-code"
                      required
                    />
                  </div>
                </div>

                <div class="field-group">
                  <label for="reset-new-password">Mật khẩu mới</label>
                  <div class="field-control">
                    <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                    <input
                      id="reset-new-password"
                      v-model="newPassword"
                      :type="showPassword ? 'text' : 'password'"
                      minlength="6"
                      maxlength="100"
                      placeholder="Ít nhất 6 ký tự"
                      autocomplete="new-password"
                      required
                    />
                    <button
                      type="button"
                      class="password-toggle"
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
                  <label for="reset-confirm-password">Nhập lại mật khẩu</label>
                  <div class="field-control">
                    <span class="material-symbols-outlined" aria-hidden="true">lock_reset</span>
                    <input
                      id="reset-confirm-password"
                      v-model="confirmPassword"
                      :type="showPassword ? 'text' : 'password'"
                      minlength="6"
                      maxlength="100"
                      placeholder="Nhập lại mật khẩu mới"
                      autocomplete="new-password"
                      required
                    />
                  </div>
                </div>
              </template>

              <div v-if="errorMsg" class="error-message" role="alert">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span>{{ errorMsg }}</span>
              </div>

              <div v-if="successMsg && !errorMsg" class="success-message" role="status">
                <span class="material-symbols-outlined" aria-hidden="true">mark_email_read</span>
                <span>{{ successMsg }}</span>
              </div>

              <button class="submit-button" type="submit" :disabled="loading">
                <span v-if="loading" class="loading-ring" aria-hidden="true"></span>
                <span>{{ submitLabel }}</span>
                <span v-if="!loading" class="material-symbols-outlined" aria-hidden="true">arrow_forward</span>
              </button>

              <div v-if="codeSent" class="reset-actions">
                <button type="button" :disabled="loading || cooldown > 0" @click="handleSendCode">
                  {{ cooldown > 0 ? `Gửi lại sau ${cooldown}s` : 'Gửi lại mã' }}
                </button>
                <button type="button" @click="changeEmail">Đổi email</button>
              </div>
            </form>

            <div class="register-prompt">
              <span>Đã nhớ mật khẩu?</span>
              <button type="button" @click="router.push('/auth/login')">Đăng nhập</button>
            </div>
          </template>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import loginCoachImage from '@/assets/login-coach-night.png';

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const code = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const codeSent = ref(false);
const resetComplete = ref(false);
const showPassword = ref(false);
const loading = ref(false);
const cooldown = ref(0);
const errorMsg = ref('');
const successMsg = ref('');
let cooldownTimer = null;

const maskedEmail = computed(() => {
  const [name = '', domain = ''] = email.value.split('@');
  if (!domain) return email.value;
  const visible = name.slice(0, Math.min(2, name.length));
  return `${visible}${'*'.repeat(Math.max(2, name.length - visible.length))}@${domain}`;
});

const submitLabel = computed(() => {
  if (loading.value) return codeSent.value ? 'Đang cập nhật...' : 'Đang gửi mã...';
  return codeSent.value ? 'Đặt lại mật khẩu' : 'Gửi mã xác nhận';
});

const readError = error => {
  const data = error.response?.data;
  if (typeof data === 'string') return data;
  return data?.message || data?.error || 'Không thể xử lý yêu cầu. Vui lòng thử lại.';
};

const startCooldown = seconds => {
  if (cooldownTimer) window.clearInterval(cooldownTimer);
  cooldown.value = Number(seconds) || 60;
  cooldownTimer = window.setInterval(() => {
    cooldown.value -= 1;
    if (cooldown.value <= 0) {
      window.clearInterval(cooldownTimer);
      cooldownTimer = null;
    }
  }, 1000);
};

const handleSendCode = async () => {
  errorMsg.value = '';
  successMsg.value = '';
  loading.value = true;
  try {
    const response = await authStore.requestPasswordReset(email.value);
    codeSent.value = true;
    successMsg.value = response.message;
    startCooldown(response.resendAfterSeconds);
  } catch (error) {
    errorMsg.value = readError(error);
  } finally {
    loading.value = false;
  }
};

const handleReset = async () => {
  errorMsg.value = '';
  successMsg.value = '';
  if (newPassword.value !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu nhập lại chưa khớp.';
    return;
  }
  loading.value = true;
  try {
    await authStore.resetPassword(email.value, code.value, newPassword.value);
    resetComplete.value = true;
  } catch (error) {
    errorMsg.value = readError(error);
  } finally {
    loading.value = false;
  }
};

const changeEmail = () => {
  codeSent.value = false;
  code.value = '';
  newPassword.value = '';
  confirmPassword.value = '';
  errorMsg.value = '';
  successMsg.value = '';
  cooldown.value = 0;
  if (cooldownTimer) window.clearInterval(cooldownTimer);
  cooldownTimer = null;
};

onBeforeUnmount(() => {
  if (cooldownTimer) window.clearInterval(cooldownTimer);
});
</script>

<style scoped>
@import '@/assets/auth.css';

.success-message {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 12px 14px;
  border: 1px solid #b8e5cf;
  border-radius: 13px;
  color: #0d6845;
  background: #f1fbf6;
  font-size: 13px;
  font-weight: 650;
  line-height: 1.45;
}

.success-message .material-symbols-outlined {
  font-size: 18px;
}

.code-control input {
  letter-spacing: 0.32em;
  font-size: 18px;
  font-weight: 800;
}

.reset-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.reset-actions button {
  border: 0;
  padding: 4px 0;
  color: #075fcc;
  background: transparent;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}

.reset-actions button:disabled {
  color: #7d8ba0;
  cursor: not-allowed;
}

.completion-state {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
  margin-bottom: 28px;
}

.completion-state > .material-symbols-outlined {
  color: #0d8256;
  font-size: 42px;
}

.completion-state p {
  margin: 0 0 8px;
  color: #0d8256;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.completion-state h2 {
  margin: 0;
  color: #071d43;
  font-size: 29px;
  letter-spacing: -0.035em;
}

.completion-state span:not(.material-symbols-outlined) {
  display: block;
  margin-top: 11px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.55;
}
</style>
