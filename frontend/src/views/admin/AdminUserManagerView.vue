<template>
  <main class="user-manager">
    <header class="page-header">
      <div>
        <span class="eyebrow">Quản trị tài khoản</span>
        <h1>{{ pageTitle }}</h1>
        <p>{{ pageSubtitle }}</p>
      </div>
      <div class="header-actions">
        <label class="global-search">
          <span class="material-symbols-outlined">search</span>
          <input v-model="searchQuery" type="search" placeholder="Tìm theo tên, username hoặc SĐT..." />
        </label>
        <button type="button" class="primary-action" @click="openCreateModal">
          <span class="material-symbols-outlined">person_add</span>Thêm mới
        </button>
      </div>
    </header>

    <section v-if="activeTab === 'users'" class="summary-grid" aria-label="Tổng quan khách hàng">
      <article>
        <span class="summary-icon blue material-symbols-outlined">groups</span>
        <div><small>Tổng khách hàng</small><strong>{{ formatNumber(summary.totalUsers) }}</strong><p>Tài khoản khách hàng</p></div>
      </article>
      <article>
        <span class="summary-icon green material-symbols-outlined">person_check</span>
        <div><small>Đang hoạt động</small><strong>{{ formatNumber(summary.activeUsers) }}</strong><p>{{ activePercent }}% tổng khách hàng</p></div>
      </article>
      <article>
        <span class="summary-icon google" aria-hidden="true">
          <svg viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.76h3.57c2.08-1.92 3.27-4.74 3.27-8.09Z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.29-2.66l-3.57-2.76c-.99.66-2.24 1.05-3.72 1.05-2.86 0-5.29-1.93-6.16-4.52H2.18v2.84A11 11 0 0 0 12 23Z"/>
            <path fill="#FBBC05" d="M5.84 14.11A6.6 6.6 0 0 1 5.49 12c0-.73.13-1.45.35-2.11V7.05H2.18A11 11 0 0 0 1 12c0 1.78.43 3.45 1.18 4.95l3.66-2.84Z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15A10.57 10.57 0 0 0 12 1 11 11 0 0 0 2.18 7.05l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38Z"/>
          </svg>
        </span>
        <div><small>Đăng nhập Google</small><strong>{{ formatNumber(summary.googleUsers) }}</strong><p>{{ googlePercent }}% tổng khách hàng</p></div>
      </article>
      <article class="revenue-card">
        <span class="summary-icon teal material-symbols-outlined">payments</span>
        <div><small>Tổng tiền khách đã mua</small><strong>{{ formatMoney(summary.totalSpent) }}</strong><p>Không gồm vé chờ thanh toán hoặc đã hủy</p></div>
      </article>
    </section>

    <section v-if="activeTab === 'users'" class="filter-panel">
      <label><span>Nguồn đăng nhập</span><select v-model="providerFilter"><option value="ALL">Tất cả nguồn</option><option value="GOOGLE">Google</option><option value="LOCAL">Nội bộ</option></select></label>
      <label><span>Trạng thái</span><select v-model="statusFilter"><option value="ALL">Tất cả trạng thái</option><option value="ACTIVE">Hoạt động</option><option value="LOCKED">Đã khóa</option></select></label>
      <div class="wallet-summary"><span class="material-symbols-outlined">account_balance_wallet</span><div><small>Tổng số dư ví</small><strong>{{ formatMoney(summary.totalWalletBalance) }}</strong></div></div>
      <button type="button" class="reset-filter" @click="resetFilters"><span class="material-symbols-outlined">restart_alt</span>Đặt lại</button>
    </section>

    <section class="content-grid" :class="{ 'has-detail': activeTab === 'users' }">
      <div class="table-card">
        <header>
          <div><span class="material-symbols-outlined">table_rows</span><strong>Danh sách {{ activeTab === 'users' ? 'khách hàng' : 'tài khoản' }}</strong></div>
          <span class="result-count">{{ formatNumber(totalElements) }} tài khoản</span>
        </header>

        <div v-if="loading" class="skeleton-list"><i v-for="item in 6" :key="item"></i></div>
        <div v-else-if="filteredUsers.length" class="table-scroll">
          <UserTable
            v-if="activeTab === 'users' || activeTab === 'admin'"
            :users="filteredUsers"
            :selected-user-id="selectedUser?.id"
            @select="selectedUser = $event"
            @history="openHistoryModal"
            @edit="openEditModal"
            @toggle-lock="toggleLock"
            @delete="handleDelete"
          />
          <StaffTable
            v-else
            :users="filteredUsers"
            @edit="openEditModal"
            @toggle-lock="toggleLock"
            @delete="handleDelete"
          />
        </div>
        <div v-else class="empty-state">
          <span class="material-symbols-outlined">person_search</span>
          <strong>Không tìm thấy tài khoản phù hợp</strong>
          <p>Thử đổi từ khóa hoặc đặt lại bộ lọc.</p>
        </div>
        <AdminPagination
          v-if="!loading"
          :page="currentPage"
          :total-pages="totalPages"
          :total-elements="totalElements"
          :page-size="pageSize"
          :current-count="users.length"
          @update:page="changePage"
        />
      </div>

      <aside v-if="activeTab === 'users'" class="customer-detail">
        <template v-if="selectedUser">
          <header><strong>Thông tin nhanh</strong><button type="button" title="Đóng" @click="selectedUser = null"><span class="material-symbols-outlined">close</span></button></header>
          <div class="customer-identity">
            <img :src="selectedUser.avatarUrl || createAvatarFallback(selectedUser.fullName)" :alt="`Ảnh đại diện ${selectedUser.fullName}`" referrerpolicy="no-referrer" @error="handleAvatarError($event, selectedUser.fullName)" />
            <h2>{{ selectedUser.fullName }}</h2>
            <span :class="selectedUser.isLocked ? 'locked' : 'active'"><i></i>{{ selectedUser.isLocked ? 'Đã khóa' : 'Hoạt động' }}</span>
            <small>ID #{{ selectedUser.id }} · {{ selectedUser.authProvider === 'GOOGLE' ? 'Google' : 'Nội bộ' }}</small>
          </div>
          <dl class="contact-detail">
            <div><dt><span class="material-symbols-outlined">call</span>Điện thoại</dt><dd :class="{ 'google-identifier': isGoogleIdentifier(selectedUser.phone) }">{{ displayPhone(selectedUser.phone) }}</dd></div>
            <div><dt><span class="material-symbols-outlined">mail</span>Email</dt><dd>{{ selectedUser.email || 'Chưa cập nhật' }}</dd></div>
          </dl>
          <div class="purchase-highlight"><small>Tổng tiền đã mua</small><strong>{{ formatMoney(selectedUser.totalSpent) }}</strong><span>{{ selectedUser.ticketCount || 0 }} vé hợp lệ</span></div>
          <dl class="account-stats">
            <div><dt>Số dư ví</dt><dd>{{ formatMoney(selectedUser.walletBalance) }}</dd></div>
            <div><dt>Điểm thưởng</dt><dd>{{ formatNumber(selectedUser.loyaltyPoints) }} điểm</dd></div>
            <div><dt>Ngày tạo</dt><dd>{{ formatDate(selectedUser.createdAt) }}</dd></div>
            <div><dt>Đăng nhập cuối</dt><dd>{{ formatDateTime(selectedUser.lastLoginAt) }}</dd></div>
            <div><dt>Quyền hạn</dt><dd>{{ selectedUser.role }}</dd></div>
          </dl>
          <div class="detail-actions">
            <button type="button" @click="openHistoryModal(selectedUser)"><span class="material-symbols-outlined">receipt_long</span>Lịch sử vé</button>
            <button type="button" @click="openEditModal(selectedUser)"><span class="material-symbols-outlined">edit</span>Chỉnh sửa</button>
          </div>
        </template>
        <div v-else class="detail-placeholder"><span class="material-symbols-outlined">touch_app</span><strong>Chọn một khách hàng</strong><p>Thông tin mua vé và số dư sẽ hiện tại đây.</p></div>
      </aside>
    </section>

    <UserDeleteModal :isOpen="userToDelete !== null" :isDeleting="isDeleting" @close="cancelDelete" @confirm="confirmDelete" />
    <DriverEditModal
      v-if="editForm.role === 'DRIVER'"
      :isOpen="isModalOpen"
      :isCreateMode="isCreateMode"
      :form="editForm"
      :submitting="submitting"
      :serverError="modalError"
      @close="closeModal"
      @submit="submitEdit"
    />
    <UserEditModal v-else :isOpen="isModalOpen" :isCreateMode="isCreateMode" :form="editForm" :submitting="submitting" :serverError="modalError" @close="closeModal" @submit="submitEdit" />
    <UserHistoryModal :isOpen="isHistoryModalOpen" :user="historyUser" :bookings="userBookings" :loading="loadingHistory" @close="closeHistoryModal" />
  </main>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { createAvatarFallback, handleAvatarError } from '@/utils/avatar'
import UserTable from '@/components/admin/user/UserTable.vue'
import StaffTable from '@/components/admin/user/StaffTable.vue'
import UserEditModal from '@/components/admin/user/UserEditModal.vue'
import DriverEditModal from '@/components/admin/user/DriverEditModal.vue'
import UserHistoryModal from '@/components/admin/user/UserHistoryModal.vue'
import UserDeleteModal from '@/components/admin/user/UserDeleteModal.vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import { useRouteDriverApi } from '@/services/routeDriverApi'
import { useRouteInspectorApi } from '@/services/routeInspectorApi'

const api = useApi()
const routeDriverApi = useRouteDriverApi()
const routeInspectorApi = useRouteInspectorApi()
const route = useRoute()
const router = useRouter()
const users = ref([])
const loading = ref(true)
const searchQuery = ref('')
const providerFilter = ref('ALL')
const statusFilter = ref('ALL')
const activeTab = ref(route.query.tab || 'users')
const currentPage = ref(0)
const pageSize = 20
const totalPages = ref(0)
const totalElements = ref(0)
const selectedUser = ref(null)
const summary = ref({ totalUsers: 0, activeUsers: 0, googleUsers: 0, totalWalletBalance: 0, totalSpent: 0 })
let searchTimer = null
let requestSequence = 0

const pageTitle = computed(() => ({ users: 'Quản lý khách hàng', driver: 'Quản lý tài xế', inspector: 'Quản lý lơ xe / phụ xe', admin: 'Quản trị viên' }[activeTab.value] || 'Quản lý người dùng'))
const pageSubtitle = computed(() => ({ users: 'Theo dõi tài khoản, lịch sử mua vé và giá trị khách hàng.', driver: 'Quản lý hồ sơ tài xế và lịch sử phân công.', inspector: 'Quản lý hồ sơ nhân viên soát vé và phụ xe.', admin: 'Quản lý các tài khoản có quyền quản trị.' }[activeTab.value] || 'Quản lý tài khoản trong hệ thống.'))
const filteredUsers = computed(() => users.value)
const activePercent = computed(() => summary.value.totalUsers ? Math.round(summary.value.activeUsers / summary.value.totalUsers * 100) : 0)
const googlePercent = computed(() => summary.value.totalUsers ? Math.round(summary.value.googleUsers / summary.value.totalUsers * 100) : 0)

const isModalOpen = ref(false)
const isCreateMode = ref(false)
const submitting = ref(false)
const modalError = ref('')
const isHistoryModalOpen = ref(false)
const historyUser = ref(null)
const userBookings = ref([])
const loadingHistory = ref(false)
const userToDelete = ref(null)
const isDeleting = ref(false)
const createEmptyForm = (role = 'USER') => ({
  id: null,
  fullName: '',
  username: '',
  phone: '',
  email: '',
  password: '',
  role,
  walletBalance: 0,
  avatarUrl: '',
  gender: '',
  dateOfBirth: '',
  citizenId: '',
  citizenIdIssueDate: '',
  address: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  driverLicenseClass: '',
  driverLicenseNumber: '',
  driverLicenseIssueDate: '',
  driverLicenseExpiryDate: '',
  drivingExperienceYears: null,
  driverStatus: 'FREE',
  driverShift: '',
  driverNotes: '',
  activeTripCount: 0,
  primaryRouteIds: [],
  backupRouteIds: []
})
const editForm = ref(createEmptyForm())

const formatNumber = value => Number(value || 0).toLocaleString('vi-VN')
const formatMoney = value => `${formatNumber(value)}đ`
const displayPhone = phone => phone || 'Chưa cập nhật'
const isGoogleIdentifier = phone => phone?.startsWith('GG_')
const formatDate = value => value ? new Date(value).toLocaleDateString('vi-VN') : 'Chưa ghi nhận'
const formatDateTime = value => value ? new Date(value).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'Chưa ghi nhận'

const fetchUsers = async () => {
  const sequence = ++requestSequence
  loading.value = true
  try {
    const roleMap = { users: 'USER', driver: 'DRIVER', inspector: 'INSPECTOR', admin: 'ADMIN' }
    const locked = statusFilter.value === 'ACTIVE' ? false : statusFilter.value === 'LOCKED' ? true : undefined
    const response = await api.get('/users/page', { params: {
      page: currentPage.value,
      size: pageSize,
      role: roleMap[activeTab.value],
      search: searchQuery.value || undefined,
      provider: activeTab.value === 'users' && providerFilter.value !== 'ALL' ? providerFilter.value : undefined,
      locked: activeTab.value === 'users' ? locked : undefined
    } })
    if (sequence !== requestSequence) return
    const payload = response.data || {}
    users.value = payload.content || []
    totalPages.value = Number(payload.totalPages || 0)
    totalElements.value = Number(payload.totalElements || 0)
    summary.value = { ...summary.value, ...(payload.summary || {}) }
    if (selectedUser.value) selectedUser.value = users.value.find(user => user.id === selectedUser.value.id) || null
    if (!selectedUser.value && users.value.length && activeTab.value === 'users') selectedUser.value = users.value[0]
  } catch (error) {
    console.error('Không tải được danh sách người dùng:', error)
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}

const scheduleFetch = () => {
  currentPage.value = 0
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(fetchUsers, 300)
}

watch(() => route.query.tab, newTab => { if (newTab && newTab !== activeTab.value) activeTab.value = newTab })
watch(activeTab, newTab => {
  if (route.query.tab !== newTab) router.replace({ query: { ...route.query, tab: newTab } })
  providerFilter.value = 'ALL'; statusFilter.value = 'ALL'; selectedUser.value = null; currentPage.value = 0; fetchUsers()
})
watch(searchQuery, scheduleFetch)
watch([providerFilter, statusFilter], scheduleFetch)

const resetFilters = () => { searchQuery.value = ''; providerFilter.value = 'ALL'; statusFilter.value = 'ALL' }
const changePage = page => { currentPage.value = page; selectedUser.value = null; fetchUsers() }
const openEditModal = user => {
  isCreateMode.value = false
  modalError.value = ''
  editForm.value = { ...createEmptyForm(user.role), ...user, username: user.username || '', phone: user.phone || '', password: '', email: user.email || '', avatarUrl: user.avatarUrl || '' }
  isModalOpen.value = true
}
const openCreateModal = () => {
  isCreateMode.value = true
  modalError.value = ''
  editForm.value = createEmptyForm(activeTab.value === 'users' ? 'USER' : activeTab.value.toUpperCase())
  isModalOpen.value = true
}
const closeModal = () => { isModalOpen.value = false; modalError.value = '' }

const openHistoryModal = async user => {
  historyUser.value = user; isHistoryModalOpen.value = true; loadingHistory.value = true; userBookings.value = []
  try { const response = await api.get(`/users/${user.id}/bookings`); userBookings.value = response.data.sort((a, b) => b.id - a.id) }
  catch (error) { console.error('Không tải được lịch sử vé:', error) }
  finally { loadingHistory.value = false }
}
const closeHistoryModal = () => { isHistoryModalOpen.value = false; historyUser.value = null; userBookings.value = [] }

const submitEdit = async () => {
  modalError.value = ''
  submitting.value = true
  try {
    const { primaryRouteIds = [], backupRouteIds = [], ...userPayload } = editForm.value
    if (editForm.value.role === 'INSPECTOR') userPayload.driverShift = ''
    let driverId = editForm.value.id
    if (isCreateMode.value) {
      const payloadEmail = editForm.value.role === 'DRIVER' ? '' : editForm.value.email
      const registration = await api.post('/auth/register', { fullName: editForm.value.fullName, username: editForm.value.username || null, phone: editForm.value.phone, email: payloadEmail, password: editForm.value.password || '123456' })
      driverId = registration.data?.id
      if (driverId) await api.put(`/users/${driverId}`, { ...userPayload, id: undefined, email: payloadEmail, password: '' })
    } else await api.put(`/users/${editForm.value.id}`, userPayload)
    if (editForm.value.role === 'DRIVER' && driverId) {
      await routeDriverApi.updateDriverRoutes(driverId, { primaryRouteIds, backupRouteIds })
    }
    if (editForm.value.role === 'INSPECTOR' && driverId) {
      await routeInspectorApi.updateInspectorRoutes(driverId, { primaryRouteIds, backupRouteIds })
    }
    await fetchUsers(); closeModal()
  } catch (error) {
    console.error('Không lưu được người dùng:', error)
    const message = error.response?.data?.message || error.response?.data || 'Không thể lưu thông tin.'
    modalError.value = typeof message === 'string' ? message : 'Không thể lưu hồ sơ nhân viên.'
  }
  finally { submitting.value = false }
}

const handleDelete = id => { userToDelete.value = id }
const cancelDelete = () => { userToDelete.value = null }
const confirmDelete = async () => {
  if (!userToDelete.value) return
  isDeleting.value = true
  try { await api.delete(`/users/${userToDelete.value}`); userToDelete.value = null; await fetchUsers() }
  catch (error) { console.error('Không xóa được người dùng:', error) }
  finally { isDeleting.value = false }
}
const toggleLock = async user => {
  const action = user.isLocked ? 'mở khóa' : 'khóa'
  if (!window.confirm(`Xác nhận ${action} tài khoản ${user.fullName}?`)) return
  try { await api.put(`/users/${user.id}/lock`); await fetchUsers() }
  catch (error) { console.error(`Không thể ${action} tài khoản:`, error) }
}

onMounted(fetchUsers)
onUnmounted(() => { if (searchTimer) clearTimeout(searchTimer) })
</script>

<style scoped>
.user-manager{display:grid;grid-auto-rows:max-content;align-content:start;gap:.85rem;padding:.8rem .9rem 1.75rem;color:#1c2c36;background:#f6f9f8;min-height:calc(100dvh - 4rem)}
.page-header{display:flex;align-items:flex-end;justify-content:space-between;gap:1rem}.eyebrow{color:#07806c;font-size:.62rem;font-weight:900;letter-spacing:.12em;text-transform:uppercase}.page-header h1{margin-top:.25rem;font-size:1.75rem;font-weight:950;letter-spacing:-.045em}.page-header p{margin-top:.35rem;color:#728089;font-size:.78rem;font-weight:600}.header-actions{display:flex;align-items:center;gap:.65rem}.global-search{position:relative;width:min(25rem,36vw)}.global-search span{position:absolute;left:.85rem;top:.72rem;color:#7b8b93;font-size:1.1rem}.global-search input{width:100%;height:2.65rem;border:1px solid #dce5e7;border-radius:.7rem;padding:0 .9rem 0 2.55rem;outline:none;color:#31444d;background:white;font-size:.7rem;font-weight:700;box-shadow:0 .25rem .8rem rgb(21 66 61/.03)}.global-search input:focus{border-color:#5aa99d;box-shadow:0 0 0 3px rgb(7 89 85/.08)}.primary-action{display:flex;height:2.65rem;align-items:center;gap:.4rem;border-radius:.7rem;padding:0 1rem;color:white;background:#075955;font-size:.7rem;font-weight:850;box-shadow:0 .45rem 1rem rgb(7 89 85/.17);transition:.18s}.primary-action:hover{background:#064c49;transform:translateY(-1px)}.primary-action span{font-size:1rem}
.summary-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(15rem,1.25fr);gap:.8rem}.summary-grid article{display:flex;min-height:7.6rem;align-items:center;gap:.85rem;border:1px solid #e0e8ea;border-radius:.95rem;padding:1rem;background:white;box-shadow:0 .55rem 1.7rem rgb(12 62 58/.04)}.summary-icon{display:grid;width:3rem;height:3rem;flex:none;place-items:center;border-radius:.8rem;font-size:1.35rem;font-weight:900}.summary-icon.blue{color:#2e68c8;background:#edf4ff}.summary-icon.green{color:#0b9a6f;background:#eaf9f3}.summary-icon.google{color:#4285f4;background:#f4f7fb;font-family:Arial}.summary-icon.teal{color:#087f72;background:#e9f7f4}.summary-grid small{color:#687982;font-size:.62rem;font-weight:800}.summary-grid strong{display:block;margin-top:.28rem;font-size:1.45rem;font-weight:950;letter-spacing:-.035em;font-variant-numeric:tabular-nums}.summary-grid p{margin-top:.26rem;color:#8a989f;font-size:.55rem;font-weight:650}.revenue-card{border-color:#cfe7df!important;background:linear-gradient(135deg,#fff 45%,#eff9f5)!important}.revenue-card strong{color:#08765f;font-size:1.25rem}
.filter-panel{display:grid;grid-template-columns:13rem 13rem 1fr auto;align-items:end;gap:.75rem;border:1px solid #e0e8ea;border-radius:.85rem;padding:.8rem;background:white}.filter-panel label{display:grid;gap:.3rem}.filter-panel label>span,.wallet-summary small{color:#6b7b84;font-size:.58rem;font-weight:800}.filter-panel select{height:2.35rem;border:1px solid #dce5e7;border-radius:.6rem;padding:0 .75rem;outline:none;color:#40535c;background:#fbfcfc;font-size:.67rem;font-weight:700}.wallet-summary{display:flex;align-items:center;gap:.55rem;padding:0 .4rem}.wallet-summary>span{color:#10846f}.wallet-summary div{display:grid}.wallet-summary strong{margin-top:.1rem;color:#075955;font-size:.85rem;font-weight:900;font-variant-numeric:tabular-nums}.reset-filter{display:flex;height:2.35rem;align-items:center;gap:.35rem;border:1px solid #dce5e7;border-radius:.6rem;padding:0 .75rem;color:#586a73;background:#fff;font-size:.62rem;font-weight:800}.reset-filter:hover{color:#075955;background:#f3f9f7}.reset-filter span{font-size:.95rem}
.content-grid{display:grid;gap:.6rem}.content-grid.has-detail{grid-template-columns:minmax(0,1fr) 15.25rem}.table-card,.customer-detail{overflow:hidden;border:1px solid #dfe8e9;border-radius:.95rem;background:white;box-shadow:0 .65rem 2rem rgb(13 61 57/.04)}.table-card>header,.customer-detail>header{display:flex;height:3.2rem;align-items:center;justify-content:space-between;border-bottom:1px solid #e7edef;padding:0 1rem}.table-card>header>div{display:flex;align-items:center;gap:.45rem}.table-card>header span{color:#0b7c6d;font-size:1rem}.table-card>header strong,.customer-detail>header strong{font-size:.72rem;font-weight:900}.result-count{border-radius:.4rem;padding:.25rem .5rem;color:#087761!important;background:#edf8f4;font-size:.55rem!important;font-weight:850}.table-scroll{overflow-x:auto}.empty-state,.detail-placeholder{display:grid;min-height:23rem;place-items:center;align-content:center;padding:2rem;text-align:center}.empty-state>span,.detail-placeholder>span{color:#91aaa5;font-size:2.6rem}.empty-state strong,.detail-placeholder strong{margin-top:.55rem;font-size:.78rem}.empty-state p,.detail-placeholder p{margin-top:.25rem;color:#829198;font-size:.62rem}.skeleton-list{display:grid}.skeleton-list i{height:4.65rem;border-bottom:1px solid #edf1f2;background:linear-gradient(90deg,#fafcfc 20%,#edf3f2 40%,#fafcfc 60%);background-size:220% 100%;animation:shimmer 1.3s linear infinite}@keyframes shimmer{to{background-position-x:-220%}}
.table-card{align-self:start}
.customer-detail{align-self:start}.customer-detail>header button{color:#819097}.customer-detail>header span{font-size:1rem}.customer-identity{display:grid;justify-items:center;padding:1.1rem 1rem .9rem}.customer-identity img{width:4rem;height:4rem;border:1px solid #dce7e5;border-radius:1.15rem;object-fit:cover}.customer-identity h2{margin-top:.65rem;font-size:.9rem;font-weight:950}.customer-identity>span{display:flex;align-items:center;gap:.3rem;margin-top:.4rem;border-radius:.4rem;padding:.25rem .45rem;font-size:.55rem;font-weight:850}.customer-identity>span i{width:.38rem;height:.38rem;border-radius:50%}.customer-identity>span.active{color:#087c5d;background:#ebf8f3}.customer-identity>span.active i{background:#12a97e}.customer-identity>span.locked{color:#bd4055;background:#fff0f3}.customer-identity>span.locked i{background:#d94860}.customer-identity small{margin-top:.35rem;color:#829198;font-size:.56rem;font-weight:700}.contact-detail,.account-stats{display:grid;border-top:1px solid #edf1f2;padding:.75rem 1rem}.contact-detail div{display:grid;gap:.2rem;padding:.38rem 0}.contact-detail dt{display:flex;align-items:center;gap:.35rem;color:#829097;font-size:.55rem;font-weight:750}.contact-detail dt span{font-size:.8rem}.contact-detail dd{overflow:hidden;color:#42565f;font-size:.65rem;font-weight:750;text-overflow:ellipsis;white-space:nowrap}.purchase-highlight{margin:0 .85rem;border-radius:.7rem;padding:.75rem;color:white;background:#075955}.purchase-highlight small,.purchase-highlight span{display:block;color:#bfe2d9;font-size:.55rem;font-weight:700}.purchase-highlight strong{display:block;margin:.2rem 0;font-size:1.15rem;font-weight:950;font-variant-numeric:tabular-nums}.account-stats{gap:.5rem;margin-top:.75rem}.account-stats div{display:flex;justify-content:space-between;gap:.5rem}.account-stats dt{color:#7a898f;font-size:.58rem;font-weight:700}.account-stats dd{color:#344850;font-size:.6rem;font-weight:850;text-align:right}.detail-actions{display:grid;grid-template-columns:1fr 1fr;gap:.45rem;border-top:1px solid #edf1f2;padding:.8rem}.detail-actions button{display:flex;align-items:center;justify-content:center;gap:.25rem;border:1px solid #dce6e7;border-radius:.55rem;padding:.5rem;color:#4e646b;background:white;font-size:.56rem;font-weight:850}.detail-actions button:hover{border-color:#9bc6bd;color:#075955;background:#f3f9f7}.detail-actions span{font-size:.8rem}
.table-scroll{overflow-x:hidden}.summary-icon.google{color:inherit;font-family:inherit}.summary-icon.google svg{width:1.45rem;height:1.45rem}
.contact-detail dd.google-identifier{width:max-content;max-width:100%;border:1px solid #c9dcf8;border-radius:.38rem;padding:.22rem .42rem;color:#2d63a9;background:#eef5ff;font-family:ui-monospace,SFMono-Regular,Consolas,monospace;font-size:.56rem;font-weight:850;letter-spacing:.015em}
@media(max-width:1250px){.summary-grid{grid-template-columns:repeat(2,1fr)}.content-grid.has-detail{grid-template-columns:1fr}.customer-detail{display:none}}
@media(max-width:800px){.user-manager{padding:1rem}.page-header{align-items:stretch;flex-direction:column}.header-actions{align-items:stretch}.global-search{width:100%}.summary-grid{grid-template-columns:1fr}.filter-panel{grid-template-columns:1fr 1fr}.wallet-summary{grid-column:1/-1}.reset-filter{width:max-content}.page-header h1{font-size:1.45rem}}
</style>
