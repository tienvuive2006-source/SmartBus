<template>
  <aside class="driver-section driver-avatar-section">
    <header class="driver-section__header">
      <span class="material-symbols-outlined">account_box</span>
      <strong>Ảnh đại diện</strong>
    </header>

    <button class="driver-avatar" type="button" :disabled="uploading" @click="fileInput?.click()">
      <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="Ảnh đại diện tài xế" />
      <span v-else class="driver-avatar__placeholder">
        <i class="material-symbols-outlined">add_a_photo</i>
        <small>Chưa có ảnh</small>
      </span>
      <span v-if="uploading" class="driver-avatar__loading"><i></i>Đang tải ảnh</span>
    </button>
    <input ref="fileInput" hidden type="file" accept="image/jpeg,image/png,image/webp" @change="uploadAvatar" />

    <button class="driver-upload-button" type="button" :disabled="uploading" @click="fileInput?.click()">
      <span class="material-symbols-outlined">upload</span>
      {{ form.avatarUrl ? 'Đổi ảnh' : 'Tải ảnh lên' }}
    </button>
    <p class="driver-upload-hint">JPG, PNG hoặc WEBP · tối đa 2 MB</p>

    <form class="driver-image-url" @submit.prevent="uploadAvatarFromUrl">
      <label for="driver-avatar-url">Hoặc dán link ảnh</label>
      <span>
        <input
          id="driver-avatar-url"
          v-model.trim="imageUrl"
          type="url"
          inputmode="url"
          placeholder="https://..."
          :disabled="uploading"
        />
        <button type="submit" :disabled="uploading || !imageUrl" title="Dùng ảnh từ liên kết">
          <span class="material-symbols-outlined">link</span>
        </button>
      </span>
      <small>Trên Google, chọn “Sao chép địa chỉ hình ảnh”.</small>
    </form>

    <div class="driver-emergency">
      <strong>Liên hệ khẩn cấp</strong>
      <label class="driver-field">
        <span>Họ tên người liên hệ</span>
        <input v-model.trim="form.emergencyContactName" type="text" placeholder="Nguyễn Thị Lan" />
      </label>
      <label class="driver-field">
        <span>Số điện thoại</span>
        <span class="driver-input-with-icon">
          <i class="material-symbols-outlined">call</i>
          <input v-model.trim="form.emergencyContactPhone" type="tel" inputmode="tel" placeholder="0903 456 789" />
        </span>
      </label>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['upload-error'])
const fileInput = ref(null)
const uploading = ref(false)
const imageUrl = ref('')

const uploadToCloudinary = async source => {
  const data = new FormData()
  data.append('file', source)
  data.append('upload_preset', 'skybus_preset')
  const response = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', data)
  props.form.avatarUrl = response.data.secure_url
}

const uploadAvatar = async event => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    emit('upload-error', 'Ảnh phải có định dạng JPG, PNG hoặc WEBP.')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    emit('upload-error', 'Dung lượng ảnh không được vượt quá 2 MB.')
    return
  }

  uploading.value = true
  emit('upload-error', '')
  try {
    await uploadToCloudinary(file)
  } catch (error) {
    console.error('Không tải được ảnh tài xế:', error)
    emit('upload-error', 'Không tải được ảnh. Vui lòng kiểm tra kết nối và thử lại.')
  } finally {
    uploading.value = false
  }
}

const uploadAvatarFromUrl = async () => {
  let parsedUrl
  try {
    parsedUrl = new URL(imageUrl.value)
  } catch {
    emit('upload-error', 'Link ảnh không hợp lệ.')
    return
  }
  if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
    emit('upload-error', 'Link ảnh phải bắt đầu bằng http:// hoặc https://.')
    return
  }

  uploading.value = true
  emit('upload-error', '')
  try {
    await uploadToCloudinary(parsedUrl.href)
    imageUrl.value = ''
  } catch (error) {
    console.error('Không tải được ảnh tài xế từ liên kết:', error)
    emit('upload-error', 'Không đọc được ảnh từ link này. Hãy sao chép địa chỉ hình ảnh công khai rồi thử lại.')
  } finally {
    uploading.value = false
  }
}
</script>
