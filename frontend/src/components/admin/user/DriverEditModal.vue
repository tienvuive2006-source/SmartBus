<template>
  <Teleport to="body">
    <div v-if="isOpen" class="driver-modal-layer" role="dialog" aria-modal="true" aria-labelledby="driver-modal-title">
      <button class="driver-modal-backdrop" type="button" aria-label="Đóng biểu mẫu" @click="$emit('close')"></button>
      <article class="driver-modal-card">
        <header class="driver-modal-header">
          <span class="driver-modal-header__icon material-symbols-outlined">person_edit</span>
          <div>
            <h2 id="driver-modal-title">{{ isCreateMode ? 'Thêm tài xế' : 'Thông tin tài xế' }}</h2>
            <p>{{ isCreateMode ? 'Tạo hồ sơ nhân sự vận hành mới' : 'Cập nhật hồ sơ và thông tin vận hành' }}</p>
          </div>
          <button class="driver-modal-close" type="button" title="Đóng" @click="$emit('close')">
            <span class="material-symbols-outlined">close</span>
          </button>
        </header>

        <form class="driver-modal-form" autocomplete="off" @submit.prevent="submitForm">
          <div class="driver-modal-body">
            <div v-if="displayError" class="driver-form-error" role="alert">
              <span class="material-symbols-outlined">error</span>
              {{ displayError }}
            </div>

            <div class="driver-profile-grid">
              <DriverPersonalSection :form="form" :is-create-mode="isCreateMode" />
              <DriverAvatarSection :form="form" @upload-error="uploadError = $event" />
            </div>

            <div class="driver-detail-grid">
              <DriverLicenseSection :form="form" />
              <DriverWorkSection :form="form" :routes="routes" :loading-routes="loadingRoutes" />
            </div>

            <div class="driver-detail-grid driver-detail-grid--bottom">
              <section class="driver-section">
                <header class="driver-section__header">
                  <span class="material-symbols-outlined">lock</span>
                  <strong>Mật khẩu</strong>
                </header>
                <label class="driver-field">
                  <span>{{ isCreateMode ? 'Mật khẩu đăng nhập' : 'Mật khẩu mới' }}</span>
                  <span class="driver-input-with-action">
                    <input
                      v-model="form.password"
                      :type="showPassword ? 'text' : 'password'"
                      autocomplete="new-password"
                      :placeholder="isCreateMode ? 'Để trống sẽ dùng 123456' : 'Để trống nếu không thay đổi'"
                    />
                    <button type="button" :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'" @click="showPassword = !showPassword">
                      <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
                    </button>
                  </span>
                </label>
              </section>

              <section class="driver-section">
                <header class="driver-section__header">
                  <span class="material-symbols-outlined">notes</span>
                  <strong>Ghi chú</strong>
                </header>
                <label class="driver-field driver-note-field">
                  <textarea v-model.trim="form.driverNotes" maxlength="1000" rows="3" placeholder="Kinh nghiệm, lưu ý sức khỏe hoặc thông tin cần bàn giao"></textarea>
                  <small>{{ (form.driverNotes || '').length }}/1000</small>
                </label>
              </section>
            </div>
          </div>

          <footer class="driver-modal-footer">
            <p><span class="material-symbols-outlined">info</span>Các trường có dấu * là bắt buộc</p>
            <div>
              <button class="driver-button driver-button--secondary" type="button" @click="$emit('close')">
                <span class="material-symbols-outlined">close</span>Hủy
              </button>
              <button class="driver-button driver-button--primary" type="submit" :disabled="submitting">
                <span class="material-symbols-outlined">save</span>{{ submitting ? 'Đang lưu…' : 'Lưu tài xế' }}
              </button>
            </div>
          </footer>
        </form>
      </article>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import DriverPersonalSection from './driver/DriverPersonalSection.vue'
import DriverAvatarSection from './driver/DriverAvatarSection.vue'
import DriverLicenseSection from './driver/DriverLicenseSection.vue'
import DriverWorkSection from './driver/DriverWorkSection.vue'
import { useApi } from '@/composables/useApi'
import { useRouteDriverApi } from '@/services/routeDriverApi'
import './driver/driver-form.css'

const props = defineProps({
  isOpen: Boolean,
  isCreateMode: Boolean,
  form: { type: Object, required: true },
  submitting: Boolean,
  serverError: { type: String, default: '' }
})
const emit = defineEmits(['close', 'submit'])
const api = useApi()
const routeDriverApi = useRouteDriverApi()
const validationError = ref('')
const uploadError = ref('')
const showPassword = ref(false)
const routes = ref([])
const loadingRoutes = ref(false)
const routeLoadError = ref('')
const displayError = computed(() => validationError.value || uploadError.value || routeLoadError.value || props.serverError)

const loadRouteAssignments = async () => {
  loadingRoutes.value = true
  routeLoadError.value = ''
  props.form.primaryRouteIds = []
  props.form.backupRouteIds = []
  try {
    const routesResponse = await api.get('/routes')
    routes.value = Array.isArray(routesResponse.data) ? routesResponse.data : []
  } catch (error) {
    console.error('Không tải được danh sách tuyến đường:', error)
    routes.value = []
    routeLoadError.value = 'Không tải được danh sách tuyến đường.'
    loadingRoutes.value = false
    return
  }

  if (props.form.id) {
    try {
      const assignmentsResponse = await routeDriverApi.getDriverRoutes(props.form.id)
      const assignments = Array.isArray(assignmentsResponse.data) ? assignmentsResponse.data : []
      props.form.primaryRouteIds = assignments
        .filter(item => item.role === 'PRIMARY')
        .map(item => item.routeId)
      props.form.backupRouteIds = assignments
        .filter(item => item.role === 'BACKUP')
        .map(item => item.routeId)
    } catch (error) {
      console.error('Không tải được cấu hình tuyến của tài xế:', error)
      routeLoadError.value = 'Danh sách tuyến vẫn dùng được nhưng chưa tải được các tuyến đã gán. Hãy khởi động lại backend.'
    }
  }

  try {
    // Preserve a predictable order after both requests finish.
    routes.value = [...routes.value].sort((a, b) => String(a.name || '').localeCompare(String(b.name || ''), 'vi'))
  } finally {
    loadingRoutes.value = false
  }
}

const normalizePhone = value => String(value || '').replace(/[\s.-]/g, '')
const isPhoneValid = value => /^(?:\+84|0)\d{9}$/.test(normalizePhone(value))

const validate = () => {
  validationError.value = ''
  if (!props.form.fullName?.trim()) return 'Vui lòng nhập họ và tên tài xế.'
  if (!/^[a-zA-Z][a-zA-Z0-9._-]{3,29}$/.test(String(props.form.username || '').trim())) return 'Tên đăng nhập phải bắt đầu bằng chữ và có 4-30 ký tự không dấu.'
  if (!isPhoneValid(props.form.phone)) return 'Số điện thoại phải gồm 10 chữ số hoặc bắt đầu bằng +84.'
  if (!/^(?:\d{9}|\d{12})$/.test(String(props.form.citizenId || '').trim())) return 'CCCD/CMND phải gồm 9 hoặc 12 chữ số.'
  if (props.form.emergencyContactPhone && !isPhoneValid(props.form.emergencyContactPhone)) return 'Số điện thoại liên hệ khẩn cấp không hợp lệ.'
  if (!props.form.driverLicenseClass?.trim()) return 'Vui lòng nhập hạng giấy phép lái xe.'
  if (!props.form.driverLicenseNumber?.trim()) return 'Vui lòng nhập số giấy phép lái xe.'
  if (!props.form.driverLicenseExpiryDate) return 'Vui lòng chọn ngày hết hạn giấy phép lái xe.'
  if (props.form.driverLicenseIssueDate && props.form.driverLicenseExpiryDate < props.form.driverLicenseIssueDate) return 'Ngày hết hạn GPLX phải sau ngày cấp.'
  if (props.form.dateOfBirth && props.form.dateOfBirth >= new Date().toISOString().slice(0, 10)) return 'Ngày sinh phải nhỏ hơn ngày hiện tại.'
  return ''
}

const submitForm = () => {
  uploadError.value = ''
  const error = validate()
  if (error) {
    validationError.value = error
    return
  }
  emit('submit')
}

const handleEscape = event => { if (event.key === 'Escape' && props.isOpen && !props.submitting) emit('close') }
watch(() => props.isOpen, open => {
  if (open) {
    validationError.value = ''
    uploadError.value = ''
    showPassword.value = false
    loadRouteAssignments()
  }
}, { immediate: true })
onMounted(() => window.addEventListener('keydown', handleEscape))
onBeforeUnmount(() => window.removeEventListener('keydown', handleEscape))
</script>
