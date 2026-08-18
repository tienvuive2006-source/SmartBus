<template>
  <section class="location-page">
    <header class="page-header">
      <div>
        <span>Danh mục nội bộ</span>
        <h1>Vị trí đã lưu</h1>
        <p>Lưu tọa độ chính xác một lần để sử dụng lại khi tạo tuyến đường.</p>
      </div>
      <router-link to="/admin/route-manager">
        <span class="material-symbols-outlined">arrow_back</span>Quay lại tuyến đường
      </router-link>
    </header>

    <div class="location-layout">
      <form class="location-form" @submit.prevent="saveLocation">
        <div class="form-heading">
          <span class="material-symbols-outlined">add_location_alt</span>
          <div><h2>{{ editingId ? 'Chỉnh sửa vị trí' : 'Thêm vị trí mới' }}</h2><p>Nhập tọa độ lấy từ nguồn bạn đã xác minh.</p></div>
        </div>

        <label>
          <span>Tên vị trí <b>*</b></span>
          <input v-model.trim="form.name" maxlength="150" placeholder="Ví dụ: Bến xe Trung tâm Đà Nẵng" />
        </label>
        <label>
          <span>Tọa độ <b>*</b></span>
          <input v-model.trim="form.coordinates" inputmode="decimal" placeholder="Ví dụ: 16.0678, 108.1884" />
          <small class="coordinate-hint">Dán theo thứ tự: vĩ độ, kinh độ</small>
        </label>

        <p v-if="formError" class="form-message form-message--error"><span class="material-symbols-outlined">error</span>{{ formError }}</p>
        <p v-if="successMessage" class="form-message form-message--success"><span class="material-symbols-outlined">check_circle</span>{{ successMessage }}</p>

        <div class="form-actions">
          <button v-if="editingId" type="button" class="button-secondary" @click="resetForm">Hủy chỉnh sửa</button>
          <button type="submit" class="button-primary" :disabled="saving">
            <span class="material-symbols-outlined">{{ saving ? 'progress_activity' : 'save' }}</span>
            {{ saving ? 'Đang lưu' : editingId ? 'Cập nhật vị trí' : 'Lưu vị trí' }}
          </button>
        </div>
      </form>

      <div class="location-list-panel">
        <div class="list-toolbar">
          <div>
            <h2>Danh sách vị trí</h2>
            <p>{{ locations.length }} vị trí đang được lưu trong database</p>
          </div>
          <label class="search-box">
            <span class="material-symbols-outlined">search</span>
            <input v-model.trim="searchQuery" type="search" placeholder="Tìm tên hoặc tọa độ" />
          </label>
        </div>

        <div v-if="loading" class="location-skeletons">
          <div v-for="item in 5" :key="item"><span></span><p><i></i><i></i></p><b></b></div>
        </div>

        <div v-else-if="loadError" class="list-state">
          <span class="material-symbols-outlined">cloud_off</span>
          <h3>Không tải được danh mục</h3>
          <p>{{ loadError }}</p>
          <button type="button" @click="loadLocations">Thử lại</button>
        </div>

        <div v-else-if="!filteredLocations.length" class="list-state">
          <span class="material-symbols-outlined">location_off</span>
          <h3>{{ locations.length ? 'Không tìm thấy vị trí' : 'Chưa có vị trí nào' }}</h3>
          <p>{{ locations.length ? 'Thử một từ khóa khác.' : 'Thêm vị trí đầu tiên bằng biểu mẫu bên trái.' }}</p>
        </div>

        <div v-else class="location-table-wrap">
          <table>
            <thead><tr><th>Vị trí</th><th>Tọa độ</th><th aria-label="Thao tác"></th></tr></thead>
            <tbody>
              <tr v-for="location in filteredLocations" :key="location.id">
                <td>
                  <div class="location-name"><span class="material-symbols-outlined">location_on</span><p><strong>{{ location.name }}</strong></p></div>
                </td>
                <td><code>{{ formatCoordinate(location.latitude) }}, {{ formatCoordinate(location.longitude) }}</code></td>
                <td>
                  <div class="row-actions">
                    <button type="button" aria-label="Chỉnh sửa vị trí" @click="editLocation(location)"><span class="material-symbols-outlined">edit</span></button>
                    <button type="button" class="delete-button" aria-label="Xóa vị trí" @click="deleteLocation(location)"><span class="material-symbols-outlined">delete</span></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useApi } from '@/composables/useApi'

const api = useApi()
const locations = ref([])
const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const editingId = ref(null)
const searchQuery = ref('')
const formError = ref('')
const successMessage = ref('')
const form = ref({ name: '', coordinates: '' })

const filteredLocations = computed(() => {
  const query = searchQuery.value.toLocaleLowerCase('vi-VN')
  if (!query) return locations.value
  return locations.value.filter(location => [location.name, location.latitude, location.longitude]
    .filter(value => value !== null && value !== undefined)
    .some(value => String(value).toLocaleLowerCase('vi-VN').includes(query)))
})

const loadLocations = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const response = await api.get('/saved-locations')
    locations.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    loadError.value = error.response?.data?.message || 'Không thể kết nối đến máy chủ.'
  } finally {
    loading.value = false
  }
}

const validateForm = () => {
  if (!form.value.name) return 'Vui lòng nhập tên vị trí.'
  const [latitude, longitude] = parseCoordinatePair()
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return 'Tọa độ phải có dạng: vĩ độ, kinh độ. Ví dụ: 16.0678, 108.1884.'
  if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) return 'Vĩ độ phải nằm trong khoảng -90 đến 90.'
  if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) return 'Kinh độ phải nằm trong khoảng -180 đến 180.'
  return ''
}

const parseCoordinatePair = () => {
  const parts = form.value.coordinates.split(',').map(value => value.trim())
  if (parts.length !== 2 || parts.some(value => value === '')) return [NaN, NaN]
  return parts.map(Number)
}

const saveLocation = async () => {
  formError.value = validateForm()
  successMessage.value = ''
  if (formError.value) return
  saving.value = true
  const [latitude, longitude] = parseCoordinatePair()
  const payload = { name: form.value.name, address: null, latitude, longitude }
  try {
    if (editingId.value) {
      const response = await api.put(`/saved-locations/${editingId.value}`, payload)
      const index = locations.value.findIndex(location => location.id === editingId.value)
      if (index >= 0) locations.value[index] = response.data
      successMessage.value = 'Đã cập nhật vị trí.'
    } else {
      const response = await api.post('/saved-locations', payload)
      locations.value.push(response.data)
      successMessage.value = 'Đã lưu vị trí mới.'
    }
    resetForm(false)
  } catch (error) {
    formError.value = error.response?.data?.message || 'Không thể lưu vị trí.'
  } finally {
    saving.value = false
  }
}

const editLocation = location => {
  editingId.value = location.id
  form.value = { name: location.name, coordinates: `${formatCoordinate(location.latitude)}, ${formatCoordinate(location.longitude)}` }
  formError.value = ''
  successMessage.value = ''
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const resetForm = (clearMessage = true) => {
  editingId.value = null
  form.value = { name: '', coordinates: '' }
  formError.value = ''
  if (clearMessage) successMessage.value = ''
}

const deleteLocation = async location => {
  if (!window.confirm(`Xóa vị trí "${location.name}"?`)) return
  try {
    await api.delete(`/saved-locations/${location.id}`)
    locations.value = locations.value.filter(item => item.id !== location.id)
    if (editingId.value === location.id) resetForm()
  } catch (error) {
    formError.value = error.response?.data?.message || 'Không thể xóa vị trí.'
  }
}

const formatCoordinate = value => Number(value).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
onMounted(loadLocations)
</script>

<style scoped>
.location-page { color: #263633; }.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 1.5rem; margin-bottom: 1.5rem; }.page-header > div > span { color: #08746d; font-size: .65rem; font-weight: 850; letter-spacing: .09em; text-transform: uppercase; }.page-header h1 { margin-top: .15rem; color: #173c38; font-size: 1.65rem; font-weight: 900; letter-spacing: -.03em; }.page-header p { margin-top: .35rem; color: #70817d; font-size: .78rem; }.page-header > a { display: flex; align-items: center; gap: .4rem; border: 1px solid #d6e2df; border-radius: .7rem; padding: .65rem .8rem; color: #47615c; background: white; font-size: .7rem; font-weight: 800; transition: background .2s, transform .2s; }.page-header > a:hover { color: #075955; background: #edf6f3; }.page-header > a:active { transform: translateY(1px); }.page-header > a span { font-size: 1rem; }
.location-layout { display: grid; grid-template-columns: minmax(18rem, .72fr) minmax(0, 1.55fr); gap: 1.25rem; align-items: start; }.location-form, .location-list-panel { border: 1px solid #dce6e3; border-radius: .9rem; background: white; box-shadow: 0 .7rem 1.8rem rgb(17 68 63 / .055); }.location-form { position: sticky; top: 0; padding: 1.2rem; }.form-heading { display: flex; align-items: flex-start; gap: .7rem; margin-bottom: 1.2rem; }.form-heading > span { display: grid; width: 2.35rem; height: 2.35rem; flex: 0 0 auto; place-items: center; border-radius: .65rem; color: white; background: #075955; font-size: 1.15rem; }.form-heading h2, .list-toolbar h2 { color: #213a36; font-size: .9rem; font-weight: 900; }.form-heading p, .list-toolbar p { margin-top: .2rem; color: #83918e; font-size: .65rem; line-height: 1.45; }.location-form > label, .coordinate-grid label { display: grid; gap: .38rem; margin-top: .85rem; }.location-form label > span { color: #526762; font-size: .62rem; font-weight: 800; }.location-form label b { color: #c33d56; }.location-form input, .location-form textarea { width: 100%; border: 1px solid #d7e2df; border-radius: .65rem; padding: .68rem .75rem; outline: none; color: #253c38; background: #f9fbfa; font-size: .72rem; font-weight: 650; transition: border-color .2s, box-shadow .2s, background .2s; }.location-form textarea { resize: vertical; line-height: 1.5; }.location-form input:focus, .location-form textarea:focus { border-color: #08746d; background: white; box-shadow: 0 0 0 3px rgb(8 116 109 / .1); }.coordinate-grid { display: grid; grid-template-columns: 1fr 1fr; gap: .65rem; }.form-message { display: flex; align-items: flex-start; gap: .35rem; margin-top: .85rem; border-radius: .55rem; padding: .55rem .65rem; font-size: .65rem; font-weight: 750; }.form-message span { font-size: .9rem; }.form-message--error { color: #a62f48; background: #fff0f3; }.form-message--success { color: #08705e; background: #eaf8f2; }.form-actions { display: flex; justify-content: flex-end; gap: .55rem; margin-top: 1.15rem; }.form-actions button { border-radius: .65rem; padding: .65rem .8rem; font-size: .65rem; font-weight: 850; transition: background .2s, transform .2s; }.form-actions button:active { transform: translateY(1px); }.button-primary { display: flex; align-items: center; gap: .35rem; color: white; background: #075955; }.button-primary:hover { background: #064b48; }.button-primary:disabled { opacity: .65; cursor: wait; }.button-primary span { font-size: .95rem; }.button-secondary { color: #5f716d; background: #edf2f1; }.button-secondary:hover { background: #e2e9e7; }
.coordinate-hint { color: #879692; font-size: .6rem; }
.location-list-panel { min-width: 0; overflow: hidden; }.list-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border-bottom: 1px solid #e2e9e7; padding: 1rem 1.1rem; }.search-box { display: flex; width: min(17rem, 100%); align-items: center; gap: .4rem; border: 1px solid #d7e2df; border-radius: .65rem; padding: 0 .65rem; color: #84938f; background: #f9fbfa; }.search-box span { font-size: 1rem; }.search-box input { width: 100%; min-height: 2.35rem; outline: none; color: #304743; background: transparent; font-size: .68rem; font-weight: 650; }.search-box:focus-within { border-color: #08746d; box-shadow: 0 0 0 3px rgb(8 116 109 / .08); }.location-table-wrap { overflow-x: auto; }.location-table-wrap table { width: 100%; border-collapse: collapse; }.location-table-wrap th { padding: .7rem 1rem; color: #7d8d89; background: #f7faf9; font-size: .57rem; font-weight: 850; letter-spacing: .06em; text-align: left; text-transform: uppercase; }.location-table-wrap th:last-child { width: 5.5rem; }.location-table-wrap td { padding: .8rem 1rem; border-bottom: 1px solid #edf1f0; vertical-align: middle; }.location-table-wrap tr:last-child td { border-bottom: 0; }.location-table-wrap tbody tr { transition: background .2s; }.location-table-wrap tbody tr:hover { background: #fafcfb; }.location-name { display: flex; min-width: 15rem; align-items: flex-start; gap: .55rem; }.location-name > span { color: #08746d; font-size: 1.05rem; }.location-name p { display: flex; min-width: 0; flex-direction: column; }.location-name strong { color: #2b403c; font-size: .7rem; font-weight: 850; }.location-name small { overflow: hidden; max-width: 24rem; margin-top: .18rem; color: #879692; font-size: .6rem; text-overflow: ellipsis; white-space: nowrap; }.location-table-wrap code { border-radius: .4rem; padding: .35rem .45rem; color: #176159; background: #edf7f4; font-size: .62rem; font-weight: 750; white-space: nowrap; }.row-actions { display: flex; justify-content: flex-end; gap: .3rem; }.row-actions button { display: grid; width: 1.85rem; height: 1.85rem; place-items: center; border-radius: .5rem; color: #58706a; background: #f0f4f3; transition: color .2s, background .2s, transform .2s; }.row-actions button:hover { color: #075955; background: #e3f2ee; }.row-actions button:active { transform: scale(.94); }.row-actions button span { font-size: .95rem; }.row-actions .delete-button:hover { color: #b8324c; background: #fff0f3; }
.list-state { display: flex; min-height: 21rem; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; text-align: center; }.list-state > span { color: #8ba09b; font-size: 2.2rem; }.list-state h3 { margin-top: .65rem; color: #354a46; font-size: .8rem; font-weight: 900; }.list-state p { margin-top: .25rem; color: #879692; font-size: .65rem; }.list-state button { margin-top: .8rem; border-radius: .6rem; padding: .55rem .75rem; color: white; background: #075955; font-size: .65rem; font-weight: 850; }.location-skeletons { display: grid; gap: 0; }.location-skeletons > div { display: grid; grid-template-columns: 2rem 1fr 5rem; align-items: center; gap: .65rem; padding: .85rem 1rem; border-bottom: 1px solid #edf1f0; }.location-skeletons span, .location-skeletons i, .location-skeletons b { display: block; background: linear-gradient(90deg, #e6ecea 25%, #f5f7f6 45%, #e6ecea 65%); background-size: 220% 100%; animation: shimmer 1.3s infinite linear; }.location-skeletons span { width: 1.75rem; height: 1.75rem; border-radius: .5rem; }.location-skeletons p { display: grid; gap: .35rem; }.location-skeletons i { width: 45%; height: .5rem; border-radius: .2rem; }.location-skeletons i:last-child { width: 70%; }.location-skeletons b { height: 1.4rem; border-radius: .35rem; }
.page-header a:focus-visible, .location-form button:focus-visible, .row-actions button:focus-visible, .list-state button:focus-visible { outline: 3px solid #d8ad37; outline-offset: 2px; } @keyframes shimmer { to { background-position-x: -220%; } }
.location-page { min-height: 100vh; padding: 2rem; background: #f4f8f7; }
.page-header, .location-layout { width: 100%; max-width: 76rem; margin-right: auto; margin-left: auto; }
.page-header { margin-bottom: 1.5rem; }
@media (max-width: 900px) { .location-layout { grid-template-columns: 1fr; }.location-form { position: static; }.page-header { align-items: flex-start; } }
@media (max-width: 640px) { .page-header { flex-direction: column; }.location-page { padding: 1rem; }.coordinate-grid { grid-template-columns: 1fr; }.list-toolbar { align-items: stretch; flex-direction: column; }.search-box { width: 100%; }.location-table-wrap th:nth-child(2), .location-table-wrap td:nth-child(2) { display: none; } }
@media (prefers-reduced-motion: reduce) { *, *::before, *::after { scroll-behavior: auto !important; animation-duration: .01ms !important; transition-duration: .01ms !important; } }
</style>
