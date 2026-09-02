<template>
  <table class="customer-table">
    <colgroup>
      <col class="id-width" /><col class="name-width" /><col class="contact-width" />
      <col class="ticket-width" /><col class="role-width" /><col class="source-width" />
      <col class="status-width" /><col class="wallet-width" /><col class="action-width" />
    </colgroup>
    <thead>
      <tr>
        <th>ID</th>
        <th>Họ và tên</th>
        <th>Liên hệ</th>
        <th class="center">Số vé</th>
        <th>Quyền hạn</th>
        <th>Nguồn</th>
        <th>Trạng thái</th>
        <th class="money">Số dư ví</th>
        <th class="center">Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="user in users"
        :key="user.id"
        :class="{ selected: selectedUserId === user.id }"
        @click="$emit('select', user)"
      >
        <td><strong class="user-id">#{{ user.id }}</strong></td>
        <td>
          <div class="customer-cell">
            <img
              :src="user.avatarUrl || createAvatarFallback(user.fullName, '#64748b')"
              :alt="`Ảnh đại diện ${user.fullName}`"
              referrerpolicy="no-referrer"
              @error="handleAvatarError($event, user.fullName, '#64748b')"
            />
            <strong :title="user.fullName">{{ user.fullName }}</strong>
          </div>
        </td>
        <td>
          <div class="contact-cell">
            <span><i class="material-symbols-outlined">call</i><b :class="{ 'google-identifier': isGoogleIdentifier(user.phone) }">{{ displayPhone(user.phone) }}</b></span>
            <span><i class="material-symbols-outlined">mail</i><b :title="user.email">{{ user.email || 'Chưa có email' }}</b></span>
          </div>
        </td>
        <td class="center"><strong class="ticket-count">{{ user.ticketCount || 0 }}</strong></td>
        <td><span class="role-badge">{{ roleLabel(user.role) }}</span></td>
        <td>
          <span class="source-badge" :class="user.authProvider === 'GOOGLE' ? 'google' : 'local'">
              <svg v-if="user.authProvider === 'GOOGLE'" viewBox="0 0 24 24" aria-hidden="true">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.76h3.57c2.08-1.92 3.27-4.74 3.27-8.09Z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.29-2.66l-3.57-2.76c-.99.66-2.24 1.05-3.72 1.05-2.86 0-5.29-1.93-6.16-4.52H2.18v2.84A11 11 0 0 0 12 23Z"/>
                <path fill="#FBBC05" d="M5.84 14.11A6.6 6.6 0 0 1 5.49 12c0-.73.13-1.45.35-2.11V7.05H2.18A11 11 0 0 0 1 12c0 1.78.43 3.45 1.18 4.95l3.66-2.84Z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15A10.57 10.57 0 0 0 12 1 11 11 0 0 0 2.18 7.05l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38Z"/>
              </svg>
              <i v-else class="material-symbols-outlined">password</i>
              {{ user.authProvider === 'GOOGLE' ? 'Google' : 'Nội bộ' }}
          </span>
        </td>
        <td>
          <div class="status-cell">
            <span class="status-badge" :class="user.isLocked ? 'locked' : 'active'"><i></i>{{ user.isLocked ? 'Đã khóa' : 'Hoạt động' }}</span>
          </div>
        </td>
        <td class="money wallet">{{ formatMoney(user.walletBalance) }}</td>
        <td class="center">
          <div class="actions" @click.stop>
            <button type="button" title="Xem lịch sử đặt vé" @click="$emit('history', user)"><span class="material-symbols-outlined">visibility</span></button>
            <button type="button" :title="user.role === 'USER' ? 'Điều chỉnh số dư ví' : 'Chỉnh sửa thông tin'" @click="$emit('edit', user)"><span class="material-symbols-outlined">{{ user.role === 'USER' ? 'account_balance_wallet' : 'edit' }}</span></button>
            <details v-if="user.role !== 'ADMIN' || canDeleteUser(user)" class="action-menu">
              <summary title="Thao tác khác"><span class="material-symbols-outlined">more_vert</span></summary>
              <div class="action-popover">
                <button v-if="user.role !== 'ADMIN'" type="button" @click="$emit('toggle-lock', user)"><span class="material-symbols-outlined">{{ user.isLocked ? 'lock_open' : 'lock' }}</span>{{ user.isLocked ? 'Mở khóa' : 'Khóa tài khoản' }}</button>
                <button v-if="canDeleteUser(user)" type="button" class="danger" @click="$emit('delete', user.id)"><span class="material-symbols-outlined">delete</span>Xóa tài khoản</button>
              </div>
            </details>
            <span v-else class="action-placeholder" aria-hidden="true"></span>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
import { createAvatarFallback, handleAvatarError } from '@/utils/avatar'

const props = defineProps({
  users: { type: Array, required: true },
  selectedUserId: { type: Number, default: null },
  currentUserId: { type: [Number, String], default: null }
})
defineEmits(['select', 'history', 'edit', 'toggle-lock', 'delete'])

const formatMoney = value => `${Number(value || 0).toLocaleString('vi-VN')}đ`
const displayPhone = phone => phone || 'Chưa cập nhật'
const isGoogleIdentifier = phone => phone?.startsWith('GG_')
const canDeleteUser = user => !user.ticketCount && Number(user.id) !== Number(props.currentUserId)
const roleLabel = role => ({ USER: 'USER', ADMIN: 'ADMIN', DRIVER: 'TÀI XẾ', INSPECTOR: 'LƠ XE' }[role] || role)
</script>

<style scoped>
.customer-table{width:100%;table-layout:fixed;border-collapse:collapse;color:#24353f;text-align:left}.id-width{width:5%}.name-width{width:15%}.contact-width{width:20%}.ticket-width{width:7%}.role-width{width:10%}.source-width{width:10%}.status-width{width:11%}.wallet-width{width:11%}.action-width{width:11%}
.customer-table th{height:3.15rem;padding:0 .5rem;border-bottom:1px solid #dfe7e9;color:#6f8089;background:#f7f9fa;font-size:.56rem;font-weight:900;letter-spacing:.045em;text-transform:uppercase;white-space:nowrap}.customer-table td{height:4.35rem;padding:.5rem;border-bottom:1px solid #e9eff0;font-size:.62rem;vertical-align:middle}.customer-table tbody tr{cursor:pointer;transition:background-color .18s ease,box-shadow .18s ease}.customer-table tbody tr:hover{background:#f8fbfa}.customer-table tbody tr.selected{background:#edf8f5;box-shadow:inset 3px 0 #087b6c}.center{text-align:center}.money{text-align:right;font-variant-numeric:tabular-nums;white-space:nowrap}.user-id{color:#51656e;font-size:.62rem;font-weight:900}
.customer-cell{display:flex;min-width:0;align-items:center;gap:.45rem}.customer-cell img{width:2.1rem;height:2.1rem;flex:none;border:1px solid #dce6e7;border-radius:.68rem;object-fit:cover;background:#edf3f2}.customer-cell strong{overflow:hidden;color:#1f3039;font-size:.64rem;font-weight:900;text-overflow:ellipsis;white-space:nowrap}.contact-cell{display:grid;min-width:0;gap:.26rem;color:#687a84}.contact-cell span{display:flex;min-width:0;align-items:center;gap:.28rem}.contact-cell i{flex:none;color:#91a1a8;font-size:.72rem}.contact-cell b{overflow:hidden;font-weight:650;text-overflow:ellipsis;white-space:nowrap}.contact-cell .google-identifier{border:1px solid #c9dcf8;border-radius:.32rem;padding:.13rem .3rem;color:#2d63a9;background:#eef5ff;font-family:ui-monospace,SFMono-Regular,Consolas,monospace;font-size:.52rem;font-weight:850;letter-spacing:.015em}.ticket-count{font-size:.68rem;font-weight:900}
.role-badge,.source-badge,.status-badge{display:inline-flex;align-items:center;gap:.22rem;border:1px solid #dce5e7;border-radius:.38rem;padding:.2rem .34rem;font-size:.48rem;font-weight:850;white-space:nowrap}.role-badge{color:#3463b2;background:#eef4ff}.source-badge svg{width:.67rem;height:.67rem}.source-badge>i{font-size:.65rem}.source-badge.google{color:#42556a;background:#fff}.source-badge.local{color:#5c6b73;background:#f3f5f6}.status-cell{display:flex;align-items:center}.status-badge>i{width:.32rem;height:.32rem;border-radius:50%}.status-badge.active{border-color:#c8e9dc;color:#087b5e;background:#ebf8f3}.status-badge.active>i{background:#10a77d}.status-badge.locked{border-color:#efc8cf;color:#bc4053;background:#fff1f3}.status-badge.locked>i{background:#d84b61}.wallet{color:#108165;font-size:.65rem;font-weight:900}
.actions{position:relative;display:flex;align-items:center;justify-content:center;gap:.28rem}.actions>button,.action-menu>summary,.action-placeholder{display:grid;width:1.7rem;height:1.7rem;flex:0 0 1.7rem;place-items:center;border:1px solid #dfe7e9;border-radius:.45rem;color:#657780;background:#fff;transition:.18s}.actions>button:hover,.action-menu>summary:hover{border-color:#9fc9c1;color:#075955;background:#f1f9f7;transform:translateY(-1px)}.actions span{font-size:.82rem}.action-menu{position:relative;flex:0 0 1.7rem}.action-menu>summary{cursor:pointer;list-style:none}.action-menu>summary::-webkit-details-marker{display:none}.action-menu[open]>summary{border-color:#9fc9c1;color:#075955;background:#f1f9f7}.action-popover{position:absolute;z-index:5;top:calc(100% + .3rem);right:0;display:grid;width:8.4rem;overflow:hidden;border:1px solid #dce6e7;border-radius:.55rem;background:#fff;box-shadow:0 .65rem 1.5rem rgb(19 66 61/.14)}.action-popover button{display:flex;align-items:center;gap:.4rem;padding:.55rem .65rem;color:#4b6068;background:#fff;font-size:.55rem;font-weight:800;text-align:left}.action-popover button:hover{color:#075955;background:#f2f8f6}.action-popover button.danger{color:#bf4054}.action-popover button.danger:hover{background:#fff2f4}.action-placeholder{visibility:hidden}
.customer-table tbody tr:last-child .action-popover{top:auto;bottom:calc(100% + .3rem)}
@media(max-width:1100px){.customer-table th,.customer-table td{padding-left:.35rem;padding-right:.35rem}.customer-cell img{width:1.85rem;height:1.85rem}.role-badge,.source-badge,.status-badge{padding-left:.25rem;padding-right:.25rem}}
</style>
