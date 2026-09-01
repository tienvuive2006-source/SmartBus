<template>
  <section class="driver-section">
    <header class="driver-section__header">
      <span class="material-symbols-outlined">badge</span>
      <strong>Thông tin giấy phép</strong>
    </header>

    <div class="driver-fields driver-fields--two driver-fields--license">
      <label class="driver-field">
        <span>Hạng giấy phép lái xe <b>*</b></span>
        <input v-model.trim="form.driverLicenseClass" required type="text" maxlength="20" placeholder="Ví dụ: D" />
      </label>
      <label class="driver-field">
        <span>Số GPLX <b>*</b></span>
        <input v-model.trim="form.driverLicenseNumber" required type="text" maxlength="50" placeholder="Nhập số giấy phép" />
      </label>
      <label class="driver-field">
        <span>Ngày cấp GPLX</span>
        <input v-model="form.driverLicenseIssueDate" :max="today" type="date" />
      </label>
      <label class="driver-field">
        <span>Ngày hết hạn GPLX <b>*</b></span>
        <input v-model="form.driverLicenseExpiryDate" required type="date" />
      </label>
      <label class="driver-field driver-field--wide">
        <span>Kinh nghiệm lái xe</span>
        <span class="driver-input-with-suffix">
          <input v-model.number="form.drivingExperienceYears" type="number" min="0" max="60" placeholder="0" />
          <i>năm</i>
        </span>
      </label>
    </div>
  </section>
</template>

<script setup>
defineProps({ form: { type: Object, required: true } })
const today = new Date().toISOString().slice(0, 10)
</script>
