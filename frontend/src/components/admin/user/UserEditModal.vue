<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] flex items-center justify-center p-3 sm:p-5">
      <div class="absolute inset-0 bg-slate-950/60 backdrop-blur-sm" @click="$emit('close')"></div>
      <section class="relative flex max-h-[94vh] w-full flex-col overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-2xl" :class="isInspector ? 'max-w-4xl' : 'max-w-xl'" role="dialog" aria-modal="true" aria-labelledby="user-form-title">
        <header class="flex items-start justify-between border-b border-slate-100 px-6 py-5 sm:px-8">
          <div class="flex items-center gap-4">
            <span class="grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-teal-50 text-teal-700"><span class="material-symbols-outlined text-[28px]">{{ isInspector ? 'badge' : 'manage_accounts' }}</span></span>
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-teal-700">{{ isInspector ? 'Hồ sơ nhân viên vận hành' : 'Quản lý tài khoản' }}</p>
              <h3 id="user-form-title" class="mt-1 text-xl font-black text-slate-900 sm:text-2xl">{{ isCreateMode ? (isInspector ? 'Thêm lơ xe mới' : 'Thêm tài khoản mới') : (isInspector ? 'Thông tin lơ xe' : 'Thông tin tài khoản') }}</h3>
              <p v-if="form.id" class="mt-1 text-xs font-bold text-slate-400">Mã nhân viên: {{ employeeCode }} · ID #{{ form.id }}</p>
            </div>
          </div>
          <button type="button" class="grid h-10 w-10 place-items-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" @click="$emit('close')"><span class="material-symbols-outlined">close</span></button>
        </header>

        <form class="flex-1 overflow-y-auto px-6 py-5 sm:px-8" autocomplete="off" @submit.prevent="handleSubmit">
          <div v-if="isInspector" class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_180px]">
            <div class="space-y-6">
              <FormSection icon="account_circle" title="Tài khoản & liên hệ" description="Thông tin dùng để đăng nhập và liên lạc với nhân viên.">
                <div class="field-grid">
                  <FormField label="Họ và tên" required class="sm:col-span-2"><input v-model.trim="form.fullName" type="text" required placeholder="Nhập họ tên nhân viên" class="form-control" /></FormField>
                  <FormField label="Tên đăng nhập" required><input v-model.trim="form.username" type="text" required minlength="4" maxlength="30" placeholder="Ví dụ: nhanvien01" class="form-control" /></FormField>
                  <FormField label="Số điện thoại" required><input v-model.trim="form.phone" type="tel" required placeholder="09xxxxxxxx" class="form-control font-mono" /></FormField>
                  <FormField label="Email"><input v-model.trim="form.email" type="email" placeholder="nhanvien@nhaxe.vn" class="form-control" /></FormField>
                  <FormField label="Mật khẩu"><input v-model="form.password" type="password" autocomplete="new-password" :placeholder="isCreateMode ? 'Mặc định 123456 nếu để trống' : 'Để trống nếu không đổi'" class="form-control" /></FormField>
                </div>
              </FormSection>

              <FormSection icon="id_card" title="Thông tin cá nhân" description="Thông tin nhận diện phục vụ hồ sơ lao động.">
                <div class="field-grid">
                  <FormField label="Giới tính"><select v-model="form.gender" class="form-control"><option value="">Chưa cập nhật</option><option value="MALE">Nam</option><option value="FEMALE">Nữ</option><option value="OTHER">Khác</option></select></FormField>
                  <FormField label="Ngày sinh"><input v-model="form.dateOfBirth" type="date" class="form-control" /></FormField>
                  <FormField label="Số CCCD/CMND"><input v-model.trim="form.citizenId" type="text" inputmode="numeric" maxlength="12" placeholder="Nhập số giấy tờ" class="form-control font-mono" /></FormField>
                  <FormField label="Ngày cấp"><input v-model="form.citizenIdIssueDate" type="date" class="form-control" /></FormField>
                  <FormField label="Địa chỉ thường trú" class="sm:col-span-2"><textarea v-model.trim="form.address" rows="2" placeholder="Số nhà, phường/xã, quận/huyện, tỉnh/thành" class="form-control resize-none"></textarea></FormField>
                </div>
              </FormSection>

              <FormSection icon="contact_emergency" title="Liên hệ khẩn cấp" description="Người cần liên hệ khi phát sinh sự cố trong chuyến.">
                <div class="field-grid">
                  <FormField label="Họ tên người liên hệ"><input v-model.trim="form.emergencyContactName" type="text" placeholder="Nhập họ tên" class="form-control" /></FormField>
                  <FormField label="Số điện thoại"><input v-model.trim="form.emergencyContactPhone" type="tel" placeholder="09xxxxxxxx" class="form-control font-mono" /></FormField>
                </div>
              </FormSection>

              <FormSection icon="work_history" title="Thông tin công việc" description="Thông tin nghiệp vụ dùng khi điều phối lơ xe.">
                <div class="field-grid">
                  <FormField label="Phân quyền" class="sm:col-span-2"><select v-model="form.role" class="form-control" disabled><option value="INSPECTOR">Lơ xe / Soát vé</option></select></FormField>
                  <FormField label="Ghi chú nghiệp vụ" class="sm:col-span-2"><textarea v-model.trim="form.driverNotes" rows="3" maxlength="500" placeholder="Kinh nghiệm, khu vực quen thuộc hoặc lưu ý khi phân công..." class="form-control resize-none"></textarea></FormField>
                </div>
              </FormSection>

              <InspectorRouteSection :form="form" :routes="routes" :loading="loadingRoutes" />
            </div>

            <aside class="lg:sticky lg:top-0 lg:self-start">
              <p class="mb-2 text-[10px] font-black uppercase tracking-[0.15em] text-slate-500">Ảnh hồ sơ 3×4</p>
              <button type="button" class="group relative aspect-[3/4] w-full overflow-hidden rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 transition hover:border-teal-500" @click="fileInput?.click()">
                <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="Ảnh hồ sơ lơ xe" class="h-full w-full object-cover" />
                <span v-else class="grid h-full place-items-center px-4 text-center"><span><span class="material-symbols-outlined text-4xl text-slate-300">add_photo_alternate</span><span class="mt-2 block text-xs font-bold text-slate-500">Chọn ảnh nhân viên</span></span></span>
                <span v-if="uploading" class="absolute inset-0 grid place-items-center bg-white/90 text-xs font-black text-teal-700">ĐANG TẢI...</span>
                <span v-else-if="form.avatarUrl" class="absolute inset-x-0 bottom-0 bg-slate-950/70 py-2 text-center text-xs font-bold text-white opacity-0 transition group-hover:opacity-100">Đổi ảnh</span>
              </button>
              <input ref="fileInput" type="file" class="hidden" accept="image/jpeg,image/png,image/webp" @change="handleFileUpload" />
              <p class="mt-3 text-[11px] leading-5 text-slate-400">Ảnh rõ khuôn mặt, nền sáng. Tỷ lệ dọc 3×4 giúp nhận diện nhanh khi phân công.</p>
              <div class="mt-4">
                <label for="inspector-avatar-url" class="mb-2 block text-[10px] font-black uppercase tracking-[0.12em] text-slate-500">Hoặc dán link ảnh</label>
                <div class="flex">
                  <input
                    id="inspector-avatar-url"
                    v-model.trim="imageUrlInput"
                    type="url"
                    inputmode="url"
                    placeholder="https://..."
                    :disabled="uploading"
                    class="min-w-0 flex-1 rounded-l-xl border border-r-0 border-slate-200 bg-slate-50 px-3 py-2.5 text-xs font-bold text-slate-700 outline-none transition focus:border-teal-600 focus:bg-white"
                    @keydown.enter.prevent="handleUrlUpload"
                  />
                  <button type="button" :disabled="uploading || !imageUrlInput" class="grid w-11 place-items-center rounded-r-xl border border-teal-200 bg-teal-50 text-teal-700 transition hover:bg-teal-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-50" title="Tải ảnh từ liên kết" @click="handleUrlUpload">
                    <span class="material-symbols-outlined text-lg">link</span>
                  </button>
                </div>
                <p class="mt-2 text-[10px] leading-4 text-slate-400">Dán địa chỉ ảnh công khai. Hệ thống sẽ tải ảnh lên và lưu link vào hồ sơ.</p>
              </div>
              <p v-if="imageUploadError" class="mt-3 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[11px] font-bold leading-4 text-rose-700">{{ imageUploadError }}</p>
              <button v-if="form.avatarUrl" type="button" class="mt-3 w-full rounded-xl border border-rose-200 px-3 py-2 text-xs font-bold text-rose-600 hover:bg-rose-50" @click="form.avatarUrl = ''">Xóa ảnh</button>
            </aside>
          </div>

          <div v-else class="space-y-4">
            <FormField :label="`Họ và tên ${form.role === 'USER' ? 'khách hàng' : 'nhân viên'}`" required><input v-model.trim="form.fullName" type="text" required class="form-control" /></FormField>
            <FormField label="Tên đăng nhập" :required="form.role !== 'USER'"><input v-model.trim="form.username" type="text" :required="form.role !== 'USER'" class="form-control" /></FormField>
            <FormField label="Số điện thoại" required><input v-model.trim="form.phone" type="tel" required class="form-control font-mono" /></FormField>
            <FormField label="Email"><input v-model.trim="form.email" type="email" class="form-control" /></FormField>
            <FormField label="Mật khẩu"><input v-model="form.password" type="password" autocomplete="new-password" :placeholder="isCreateMode ? 'Mặc định 123456 nếu để trống' : 'Để trống nếu không đổi'" class="form-control" /></FormField>
            <div class="grid grid-cols-2 gap-4">
              <FormField label="Phân quyền"><select v-model="form.role" class="form-control"><option value="USER">Khách hàng</option><option value="ADMIN">Quản trị viên</option><option value="INSPECTOR">Lơ xe / Soát vé</option><option value="DRIVER">Tài xế</option></select></FormField>
              <FormField v-if="form.role === 'USER'" label="Số dư ví (đ)"><input v-model.number="form.walletBalance" type="number" min="0" class="form-control" /></FormField>
            </div>
          </div>

          <p v-if="serverError" class="mt-5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-bold text-rose-700">{{ serverError }}</p>
          <footer class="sticky bottom-0 -mx-6 mt-6 flex justify-end gap-3 border-t border-slate-100 bg-white/95 px-6 py-4 backdrop-blur sm:-mx-8 sm:px-8">
            <button type="button" class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-black text-slate-600 transition hover:bg-slate-50" @click="$emit('close')">Hủy bỏ</button>
            <button type="submit" :disabled="submitting || uploading" class="rounded-xl bg-teal-700 px-6 py-3 text-sm font-black text-white shadow-lg shadow-teal-900/10 transition hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-50">{{ submitting ? 'Đang lưu...' : (isCreateMode ? 'Tạo hồ sơ' : 'Lưu thay đổi') }}</button>
          </footer>
        </form>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import axios from 'axios'
import FormField from './FormField.vue'
import FormSection from './FormSection.vue'
import InspectorRouteSection from './InspectorRouteSection.vue'
import { useApi } from '@/composables/useApi'
import { useRouteInspectorApi } from '@/services/routeInspectorApi'

const props = defineProps({ isOpen: Boolean, isCreateMode: Boolean, form: { type: Object, required: true }, submitting: Boolean, serverError: { type: String, default: '' } })
const emit = defineEmits(['close', 'submit'])
const api = useApi()
const routeInspectorApi = useRouteInspectorApi()
const fileInput = ref(null)
const uploading = ref(false)
const imageUrlInput = ref('')
const imageUploadError = ref('')
const routes = ref([])
const loadingRoutes = ref(false)
const isInspector = computed(() => props.form.role === 'INSPECTOR')
const employeeCode = computed(() => props.form.id ? `NV${String(props.form.id).padStart(3, '0')}` : 'Tự động sau khi tạo')
const handleSubmit = () => emit('submit')

const loadRouteAssignments = async () => {
  if (!isInspector.value) return
  loadingRoutes.value = true
  props.form.primaryRouteIds = []
  props.form.backupRouteIds = []
  try {
    const routesResponse = await api.get('/routes')
    routes.value = (Array.isArray(routesResponse.data) ? routesResponse.data : [])
      .sort((a, b) => String(a.name || '').localeCompare(String(b.name || ''), 'vi'))
    if (props.form.id) {
      const assignmentsResponse = await routeInspectorApi.getInspectorRoutes(props.form.id)
      const assignments = Array.isArray(assignmentsResponse.data) ? assignmentsResponse.data : []
      props.form.primaryRouteIds = assignments.filter(item => item.role === 'PRIMARY').map(item => item.routeId)
      props.form.backupRouteIds = assignments.filter(item => item.role === 'BACKUP').map(item => item.routeId)
    }
  } catch (error) {
    console.error('Không tải được tuyến phụ trách của lơ xe:', error)
  } finally {
    loadingRoutes.value = false
  }
}

watch(() => props.isOpen, open => {
  if (open) {
    imageUrlInput.value = ''
    imageUploadError.value = ''
    loadRouteAssignments()
  }
}, { immediate: true })

const uploadAvatarToCloudinary = async source => {
  const data = new FormData()
  data.append('file', source)
  data.append('upload_preset', 'skybus_preset')
  const response = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', data)
  props.form.avatarUrl = response.data.secure_url
}

const handleFileUpload = async event => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    imageUploadError.value = 'Ảnh phải có định dạng JPG, PNG hoặc WEBP.'
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    imageUploadError.value = 'Dung lượng ảnh không được vượt quá 2 MB.'
    return
  }
  uploading.value = true
  imageUploadError.value = ''
  try {
    await uploadAvatarToCloudinary(file)
  } catch (error) {
    console.error(error)
    imageUploadError.value = 'Không thể tải ảnh. Vui lòng kiểm tra kết nối và thử lại.'
  } finally {
    uploading.value = false
  }
}

const handleUrlUpload = async () => {
  let parsedUrl
  try {
    parsedUrl = new URL(imageUrlInput.value)
  } catch {
    imageUploadError.value = 'Link ảnh không hợp lệ.'
    return
  }
  if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
    imageUploadError.value = 'Link ảnh phải bắt đầu bằng http:// hoặc https://.'
    return
  }

  uploading.value = true
  imageUploadError.value = ''
  try {
    await uploadAvatarToCloudinary(parsedUrl.href)
    imageUrlInput.value = ''
  } catch (error) {
    console.error('Không tải được ảnh lơ xe từ liên kết:', error)
    imageUploadError.value = 'Không đọc được ảnh từ link này. Hãy dán địa chỉ ảnh công khai rồi thử lại.'
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.field-grid { display: grid; gap: 1rem; }
.form-control { width: 100%; min-height: 2.8rem; border: 1px solid #dce5e7; border-radius: .8rem; background: #f8fafb; padding: .72rem .9rem; color: #24343d; font-size: .82rem; font-weight: 700; outline: none; transition: .18s ease; }
.form-control:focus { border-color: #168a7a; background: white; box-shadow: 0 0 0 3px rgb(13 148 136 / .09); }
.form-control:disabled { cursor: not-allowed; color: #687982; background: #eef3f3; }
@media (min-width: 640px) { .field-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
