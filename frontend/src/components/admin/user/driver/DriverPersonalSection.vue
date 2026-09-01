<template>
  <section class="driver-section driver-personal-section">
    <header class="driver-section__header">
      <span class="material-symbols-outlined">person</span>
      <strong>Thông tin cá nhân</strong>
    </header>

    <div class="driver-fields driver-fields--two">
      <label class="driver-field">
        <span>Họ và tên tài xế <b>*</b></span>
        <input v-model.trim="form.fullName" required type="text" placeholder="Nhập họ và tên" />
      </label>
      <label class="driver-field">
        <span>Mã tài xế</span>
        <input :value="driverCode" type="text" disabled />
      </label>
      <label class="driver-field">
        <span>Tên đăng nhập <b>*</b></span>
        <input v-model.trim="form.username" required type="text" autocomplete="off" placeholder="Ví dụ: taixe1" />
      </label>
      <label class="driver-field">
        <span>Số điện thoại <b>*</b></span>
        <input v-model.trim="form.phone" required type="tel" inputmode="tel" placeholder="0901 234 567" />
      </label>
      <label class="driver-field">
        <span>Email</span>
        <input v-model.trim="form.email" type="email" placeholder="taixe@trungnam.vn" />
      </label>
      <label class="driver-field">
        <span>Giới tính</span>
        <select v-model="form.gender">
          <option value="">Chưa cập nhật</option>
          <option value="MALE">Nam</option>
          <option value="FEMALE">Nữ</option>
          <option value="OTHER">Khác</option>
        </select>
      </label>
      <label class="driver-field">
        <span>Ngày sinh</span>
        <input v-model="form.dateOfBirth" :max="today" type="date" />
      </label>
      <label class="driver-field">
        <span>CCCD/CMND <b>*</b></span>
        <input v-model.trim="form.citizenId" required type="text" inputmode="numeric" maxlength="12" placeholder="Nhập 9 hoặc 12 chữ số" />
      </label>
      <label class="driver-field">
        <span>Ngày cấp</span>
        <input v-model="form.citizenIdIssueDate" :max="today" type="date" />
      </label>
      <label class="driver-field driver-field--wide">
        <span>Địa chỉ</span>
        <span class="driver-input-with-icon">
          <i class="material-symbols-outlined">location_on</i>
          <textarea v-model.trim="form.address" class="driver-address-input" rows="4" placeholder="Số nhà, đường, phường/xã, tỉnh/thành"></textarea>
        </span>
      </label>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  form: { type: Object, required: true },
  isCreateMode: Boolean
})
const today = new Date().toISOString().slice(0, 10)
const driverCode = computed(() => props.form.id ? `TX${String(props.form.id).padStart(3, '0')}` : 'Tự động khi tạo')
</script>
