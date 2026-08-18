<template>
  <main class="phone-page">
    <section class="completion-card" aria-labelledby="completion-title">
      <aside class="brand-side">
        <div class="brand-mark"><span>TN</span><div><strong>TRUNG NAM</strong><small>VIP LIMOUSINE</small></div></div>
        <div class="brand-copy">
          <span class="step-label">BƯỚC CUỐI CÙNG</span>
          <h1 id="completion-title">Thêm số điện thoại để đặt vé</h1>
          <p>Nhà xe sử dụng số này để xác nhận chuyến đi và liên hệ khi lịch trình thay đổi.</p>
        </div>
        <ul>
          <li><span class="material-symbols-outlined">verified</span>Xác nhận đúng hành khách</li>
          <li><span class="material-symbols-outlined">notifications_active</span>Nhận thông báo chuyến đi</li>
          <li><span class="material-symbols-outlined">support_agent</span>Hỗ trợ nhanh khi cần</li>
        </ul>
      </aside>

      <div class="form-side">
        <header>
          <span class="google-badge"><b>G</b> Tài khoản Google</span>
          <h2>Hoàn tất thông tin</h2>
          <p>Chỉ cần cập nhật một lần. Số điện thoại phải chưa được sử dụng cho tài khoản khác.</p>
        </header>

        <dl class="account-preview">
          <div><dt>Họ và tên</dt><dd>{{ authStore.currentUser?.fullName }}</dd></div>
          <div><dt>Email</dt><dd>{{ authStore.currentUser?.email }}</dd></div>
        </dl>

        <form @submit.prevent="submitPhone">
          <label for="required-phone">Số điện thoại <em>*</em></label>
          <div class="phone-control" :class="{ invalid: errorMessage }">
            <span class="country-code">+84</span>
            <input
              id="required-phone"
              v-model="phone"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              maxlength="10"
              placeholder="Ví dụ: 0901234567"
              autofocus
              @input="normalizePhone"
            />
            <span v-if="phone.length === 10 && !clientError" class="material-symbols-outlined valid-icon">check_circle</span>
          </div>
          <p v-if="errorMessage" class="form-error" role="alert"><span class="material-symbols-outlined">error</span>{{ errorMessage }}</p>
          <p v-else class="field-help">Nhập số di động Việt Nam gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09.</p>

          <button class="continue-button" type="submit" :disabled="submitting">
            <span v-if="submitting" class="spinner"></span>
            {{ submitting ? 'Đang cập nhật...' : 'Cập nhật và tiếp tục' }}
            <span v-if="!submitting" class="material-symbols-outlined">arrow_forward</span>
          </button>
        </form>

        <button class="logout-button" type="button" @click="logout">Đăng xuất tài khoản này</button>
        <p class="privacy-note"><span class="material-symbols-outlined">lock</span>Thông tin được bảo mật và chỉ dùng cho dịch vụ đặt vé.</p>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const phone = ref('')
const submitting = ref(false)
const serverError = ref('')

const clientError = computed(() => {
  if (!phone.value) return 'Vui lòng nhập số điện thoại.'
  if (!/^0[35789][0-9]{8}$/.test(phone.value)) return 'Số điện thoại chưa đúng định dạng.'
  return ''
})
const errorMessage = computed(() => serverError.value || (phone.value && clientError.value ? clientError.value : ''))

const targetPath = () => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
  return redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/'
}

const normalizePhone = () => {
  phone.value = phone.value.replace(/\D/g, '').slice(0, 10)
  serverError.value = ''
}

const submitPhone = async () => {
  serverError.value = ''
  if (clientError.value) {
    serverError.value = clientError.value
    return
  }
  submitting.value = true
  try {
    await authStore.completeGooglePhone(phone.value)
    await router.replace(targetPath())
  } catch (error) {
    serverError.value = error.response?.data?.message || 'Không thể cập nhật số điện thoại. Vui lòng thử lại.'
  } finally {
    submitting.value = false
  }
}

const logout = () => {
  authStore.logout()
  router.replace('/auth/login')
}

onMounted(() => {
  if (!authStore.isLoggedIn) router.replace('/auth/login')
  else if (!authStore.needsPhoneCompletion) router.replace(targetPath())
})
</script>

<style scoped>
.phone-page{display:grid;min-height:100dvh;place-items:center;padding:2rem;background:#edf3f1;color:#182c34}.completion-card{display:grid;width:min(58rem,100%);grid-template-columns:minmax(0,.9fr) minmax(0,1.1fr);overflow:hidden;border:1px solid #d7e3e0;border-radius:1.5rem;background:#fff;box-shadow:0 1.8rem 4.5rem rgb(7 62 58/.14)}.brand-side{display:flex;min-height:36rem;flex-direction:column;padding:2.4rem;color:#fff;background:#075955}.brand-mark{display:flex;align-items:center;gap:.75rem}.brand-mark>span{display:grid;width:3rem;height:3rem;place-items:center;border:1px solid rgb(255 255 255/.42);border-radius:.85rem;color:#f4c653;font-size:1rem;font-weight:950}.brand-mark div{display:grid}.brand-mark strong{font-size:.9rem;letter-spacing:.12em}.brand-mark small{margin-top:.12rem;color:#e8c661;font-size:.48rem;font-weight:850;letter-spacing:.18em}.brand-copy{margin-top:auto}.step-label{display:inline-flex;border:1px solid rgb(255 255 255/.2);border-radius:.4rem;padding:.35rem .5rem;color:#f2d273;background:rgb(255 255 255/.06);font-size:.56rem;font-weight:900;letter-spacing:.12em}.brand-copy h1{max-width:18rem;margin-top:1rem;font-size:2.15rem;font-weight:950;letter-spacing:-.045em;line-height:1.08;text-wrap:balance}.brand-copy p{max-width:22rem;margin-top:1rem;color:#c8dfda;font-size:.78rem;font-weight:600;line-height:1.65}.brand-side ul{display:grid;gap:.7rem;margin-top:2rem;padding:0;list-style:none}.brand-side li{display:flex;align-items:center;gap:.55rem;color:#e5f1ef;font-size:.68rem;font-weight:750}.brand-side li span{color:#f2ca55;font-size:1rem}.form-side{display:flex;flex-direction:column;padding:3rem 3.2rem}.form-side header{max-width:25rem}.google-badge{display:inline-flex;align-items:center;gap:.4rem;border:1px solid #dce5e7;border-radius:.45rem;padding:.3rem .5rem;color:#53666f;background:#f8fafb;font-size:.55rem;font-weight:850}.google-badge b{color:#4285f4;font-family:Arial;font-size:.8rem}.form-side h2{margin-top:1rem;font-size:1.65rem;font-weight:950;letter-spacing:-.04em}.form-side header p{margin-top:.5rem;color:#718087;font-size:.7rem;font-weight:600;line-height:1.55}.account-preview{display:grid;gap:.65rem;margin:1.4rem 0;border:1px solid #e0e9e8;border-radius:.8rem;padding:.85rem 1rem;background:#f7faf9}.account-preview div{display:grid;grid-template-columns:5rem minmax(0,1fr);gap:.75rem}.account-preview dt{color:#819097;font-size:.58rem;font-weight:750}.account-preview dd{overflow:hidden;color:#334a53;font-size:.64rem;font-weight:850;text-overflow:ellipsis;white-space:nowrap}.form-side form{display:grid;margin-top:.2rem}.form-side label{font-size:.62rem;font-weight:850}.form-side label em{color:#d2475d;font-style:normal}.phone-control{display:flex;height:3.25rem;align-items:center;margin-top:.45rem;border:1px solid #cedcda;border-radius:.72rem;background:#fff;transition:.18s}.phone-control:focus-within{border-color:#08796d;box-shadow:0 0 0 3px rgb(8 121 109/.1)}.phone-control.invalid{border-color:#e19aa6}.country-code{display:grid;height:1.8rem;place-items:center;border-right:1px solid #dce6e4;padding:0 .85rem;color:#075955;font-size:.7rem;font-weight:900}.phone-control input{min-width:0;flex:1;border:0;padding:0 .85rem;outline:0;color:#20373f;font-size:.82rem;font-weight:800}.phone-control input::placeholder{color:#9caaaf;font-weight:600}.valid-icon{margin-right:.75rem;color:#0aa276;font-size:1.15rem}.field-help,.form-error{display:flex;align-items:flex-start;gap:.3rem;min-height:2.2rem;padding-top:.45rem;font-size:.56rem;font-weight:650;line-height:1.45}.field-help{color:#829097}.form-error{color:#c64155}.form-error span{font-size:.8rem}.continue-button{display:flex;height:3rem;align-items:center;justify-content:center;gap:.5rem;border-radius:.7rem;color:#fff;background:#087066;font-size:.68rem;font-weight:900;box-shadow:0 .55rem 1.2rem rgb(8 112 102/.18);transition:.2s}.continue-button:hover:not(:disabled){background:#075d56;transform:translateY(-1px)}.continue-button:active:not(:disabled){transform:translateY(1px)}.continue-button:focus-visible,.logout-button:focus-visible{outline:3px solid rgb(8 112 102/.2);outline-offset:2px}.continue-button:disabled{cursor:wait;opacity:.7}.continue-button>span{font-size:1rem}.spinner{width:1rem;height:1rem;border:2px solid rgb(255 255 255/.35);border-top-color:#fff;border-radius:50%;animation:spin .7s linear infinite}.logout-button{align-self:center;margin-top:1rem;color:#6d7e85;font-size:.6rem;font-weight:800;text-decoration:underline;text-underline-offset:.2rem}.logout-button:hover{color:#b13c50}.privacy-note{display:flex;align-items:center;justify-content:center;gap:.3rem;margin-top:auto;padding-top:1.5rem;color:#8a989e;font-size:.53rem;font-weight:650}.privacy-note span{font-size:.75rem}@keyframes spin{to{transform:rotate(360deg)}}@media(max-width:760px){.phone-page{padding:1rem}.completion-card{grid-template-columns:1fr}.brand-side{min-height:auto;padding:1.4rem}.brand-copy{margin-top:2.5rem}.brand-copy h1{font-size:1.7rem}.brand-side ul{display:none}.form-side{padding:1.8rem 1.4rem}.privacy-note{margin-top:1rem}}
</style>
