import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import { queueRouteAccessNotice } from '@/utils/routeAccessNotice'

// Tối ưu hóa hiệu năng bằng Lazy Loading (Code Splitting)
// Việc này giúp Vite tách các file js ra, người dùng vào trang nào tải js trang đó, tăng tốc độ truy cập trang chủ x10 lần.
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const SearchResultsView = () => import('../views/booking/SearchResultsView.vue')
const SeatSelectionView = () => import('../views/booking/SeatSelectionView.vue')
const PaymentView = () => import('../views/booking/PaymentView.vue')
const PaymentSuccessView = () => import('../views/booking/PaymentSuccessView.vue')
const TicketExchangeView = () => import('../views/booking/TicketExchangeView.vue')
const ProfileView = () => import('../views/user/ProfileView.vue')
const HistoryView = () => import('../views/user/HistoryView.vue')
const NotificationsView = () => import('../views/user/NotificationsView.vue')
const AdminDashboardView = () => import('../views/admin/AdminDashboardView.vue')
const AdminFundManagerView = () => import('../views/admin/AdminFundManagerView.vue')
const AdminTripManagerView = () => import('../views/admin/AdminTripManagerView.vue')
const AdminFleetStatusView = () => import('../views/admin/AdminFleetStatusView.vue')

const LoginView = () => import('../views/auth/LoginView.vue')
const RegisterView = () => import('../views/auth/RegisterView.vue')
const ForgotPasswordView = () => import('../views/auth/ForgotPasswordView.vue')
const CompletePhoneView = () => import('../views/auth/CompletePhoneView.vue')
const AdminUserManagerView = () => import('../views/admin/AdminUserManagerView.vue')
const AdminBookingManagerView = () => import('../views/admin/AdminBookingManagerView.vue')
const AdminReviewManagerView = () => import('../views/admin/AdminReviewManagerView.vue')
const AdminRouteManagerView = () => import('../views/admin/AdminRouteManagerView.vue')
const InspectorLayout = () => import('../layouts/InspectorLayout.vue')
const InspectorDashboardView = () => import('../views/inspector/InspectorDashboardView.vue')
const InspectorTripDetailView = () => import('../views/inspector/InspectorTripDetailView.vue')
const DriverLayout = () => import('../layouts/DriverLayout.vue')
const DriverDashboardView = () => import('../views/driver/DriverDashboardView.vue')
const DriverTripDetailView = () => import('../views/driver/DriverTripDetailView.vue')
const AdminAuditLogView = () => import('../views/admin/AdminAuditLogView.vue')
const AdminBannerManagerView = () => import('../views/admin/AdminBannerManagerView.vue')
const AdminIncidentManagerView = () => import('../views/admin/AdminIncidentManagerView.vue')
const AdminVoucherManagerView = () => import('../views/admin/AdminVoucherManagerView.vue')
const AdminDriverWorkspaceView = () => import('../views/admin/AdminDriverWorkspaceView.vue')
const AdminLeaveManagerView = () => import('../views/admin/AdminLeaveManagerView.vue')
const AdminDriverWrapperView = () => import('../views/admin/AdminDriverWrapperView.vue')
const AdminDriverLocationsView = () => import('../views/admin/AdminDriverLocationsView.vue')
const AdminArticleManagerView = () => import('../views/admin/AdminArticleManagerView.vue')
const AdminSavedLocationView = () => import('../views/admin/AdminSavedLocationView.vue')

const ArticleListView = () => import('../views/user/ArticleListView.vue')
const ArticleDetailView = () => import('../views/user/ArticleDetailView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    // Luôn luôn cuộn lên đầu trang khi chuyển route
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/LandingPageView.vue'),
          // Thêm hideHeader: true vào đây
          meta: { title: 'Trung Nam', showBack: false, hideHeader: true, hideFooter: true }
        },
        {
          path: 'booking/search',
          name: 'search',
          component: SearchResultsView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'booking/seat',
          name: 'seat',
          component: SeatSelectionView,
          meta: { hideFooter: true, requiresCompletePhone: true }
        },
        {
          path: 'booking/payment',
          name: 'payment',
          component: PaymentView,
          meta: { hideFooter: true, requiresCompletePhone: true }
        },
        {
          path: 'booking/payment-success',
          name: 'payment-success',
          component: PaymentSuccessView,
          meta: { hideFooter: true }
        },
        {
          path: 'booking/exchange/:bookingId',
          name: 'ticket-exchange',
          component: TicketExchangeView,
          meta: { title: 'Đổi vé', hideFooter: true, requiresCompletePhone: true }
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileView,
          meta: { title: 'Hồ sơ cá nhân', showBack: false }
        },
        {
          path: 'history',
          name: 'history',
          component: HistoryView,
          meta: { title: 'Lịch sử đặt vé', showBack: false }
        },
        {
          path: 'notifications',
          name: 'notifications',
          component: NotificationsView,
          meta: { title: 'Thông báo', showBack: true }
        },
        {
          path: 'tin-tuc',
          name: 'articles',
          component: ArticleListView,
          meta: { title: 'Tin tức & Khuyến mãi', showBack: true }
        },
        {
          path: 'tin-tuc/:slug',
          name: 'article-detail',
          component: ArticleDetailView,
          meta: { title: 'Đọc Tin Tức', showBack: true }
        },
        {
          path: 'auth/login',
          name: 'login',
          component: LoginView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'auth/register',
          name: 'register',
          component: RegisterView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'auth/forgot-password',
          name: 'forgot-password',
          component: ForgotPasswordView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'auth/complete-phone',
          name: 'complete-phone',
          component: CompletePhoneView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'landing-demo',
          name: 'landing-demo',
          component: () => import('../views/LandingPageView.vue'),
          meta: { title: 'Landing Page Demo', hideHeader: true, hideFooter: true, showBack: false }
        }
      ]
    },
    {
      path: '/admin/route',
      name: 'admin-route-locations',
      component: AdminSavedLocationView,
      meta: { title: 'Danh mục vị trí' }
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: AdminDashboardView
        },
        {
          path: 'funds',
          name: 'admin-funds',
          component: AdminFundManagerView
        },
          {
            path: 'refunds',
            name: 'admin-refunds',
            redirect: { path: '/admin/funds', query: { tab: 'refunds' } },
            meta: { title: 'Quản lý hoàn tiền' }
        },
        {
          path: 'trip-manager',
          name: 'admin-trip-manager',
          component: AdminTripManagerView
        },
        {
          path: 'booking-manager',
          name: 'admin-booking-manager',
          component: AdminBookingManagerView
        },
        {
          path: 'route-manager',
          name: 'admin-route-manager',
          component: AdminRouteManagerView
        },
        {
          path: 'fleet-status',
          name: 'admin-fleet-status',
          component: AdminFleetStatusView
        },
        {
          path: 'drivers',
          name: 'admin-drivers',
          component: AdminDriverWrapperView,
          redirect: '/admin/drivers/workspace',
          meta: { fullScreen: true },
          children: [
            {
              path: 'workspace',
              name: 'admin-drivers-workspace',
              component: AdminDriverWorkspaceView
            },
            {
              path: 'leave-requests',
              name: 'admin-leave-requests',
              component: AdminLeaveManagerView
            },
            {
              path: 'locations',
              name: 'admin-driver-locations',
              component: AdminDriverLocationsView
            }
          ]
        },
        {
          path: 'banners',
          name: 'admin-banners',
          component: AdminBannerManagerView,
          meta: { title: 'Quản lý Banner', showBack: true }
        },
        {
          path: 'articles',
          name: 'admin-articles',
          component: AdminArticleManagerView,
          meta: { title: 'Quản lý Tin tức', showBack: true }
        },
        {
          path: 'users',
          name: 'admin-user-manager',
          component: AdminUserManagerView,
          meta: { compactContent: true }
        },

        {
          path: 'reviews',
          name: 'admin-review-manager',
          component: AdminReviewManagerView
        },
        {
          path: 'audit-logs',
          name: 'admin-audit-logs',
          component: AdminAuditLogView
        },
        {
          path: 'incidents',
          name: 'admin-incidents',
          component: AdminIncidentManagerView,
          meta: { title: 'Quản lý Sự cố', showBack: true }
        },
        {
          path: 'vouchers',
          name: 'admin-vouchers',
          component: AdminVoucherManagerView,
          meta: { title: 'Quản lý Voucher', showBack: true, compactContent: true, hideAdminHeader: true }
        }
      ]
    },
    {
      path: '/inspector',
      component: InspectorLayout,
      children: [
        {
          path: '',
          name: 'inspector-dashboard',
          component: InspectorDashboardView
        },
        {
          path: 'trip/:id',
          name: 'inspector-trip-detail',
          component: InspectorTripDetailView
        }
      ]
    },
    {
      path: '/driver',
      component: DriverLayout,
      children: [
        {
          path: '',
          name: 'driver-dashboard',
          component: DriverDashboardView
        },
        {
          path: 'trip/:id',
          name: 'driver-trip-detail',
          component: DriverTripDetailView
        }
      ]
    }
  ]
})

// 🛡️ HỆ THỐNG TƯỜNG LỬA CỬA NGÕ (VUE NAVIGATION GUARD - JWT VERSION)
router.beforeEach((to, from) => {
  // Đọc thẳng từ localStorage (cách này hoạt động trước khi Pinia khởi tạo)
  const token = localStorage.getItem('jwt_token')
  const userStr = localStorage.getItem('jwt_user')
  let userRole = null
  let storedUser = null

  if (userStr) {
    try { storedUser = JSON.parse(userStr); userRole = storedUser?.role } catch { /* bỏ qua */ }
  }

  const needsPhoneCompletion = storedUser?.authProvider === 'GOOGLE'
    && storedUser?.phone?.startsWith('GG_')
  if (to.meta.requiresCompletePhone && token && needsPhoneCompletion) {
    return { path: '/auth/complete-phone', query: { redirect: to.fullPath } }
  }

  // 🔒 Bảo vệ route ADMIN
  if (to.path.startsWith('/admin')) {
    if (!token) {
      queueRouteAccessNotice('Vui lòng đăng nhập bằng tài khoản quản trị viên để tiếp tục.')
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }
    if (userRole !== 'ADMIN') {
      queueRouteAccessNotice('Tài khoản hiện tại không có quyền truy cập trang quản trị.')
      return '/';
    }
  }

  // 🔒 Bảo vệ route INSPECTOR
  if (to.path.startsWith('/inspector')) {
    if (!token) {
      queueRouteAccessNotice('Vui lòng đăng nhập bằng tài khoản nhân viên soát vé để tiếp tục.')
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }
    if (userRole !== 'INSPECTOR' && userRole !== 'ADMIN') {
      queueRouteAccessNotice('Tài khoản hiện tại không có quyền truy cập khu vực soát vé.')
      return '/';
    }
  }

  // 🔒 Bảo vệ route DRIVER
  if (to.path.startsWith('/driver')) {
    if (!token) {
      queueRouteAccessNotice('Vui lòng đăng nhập bằng tài khoản lái xe để tiếp tục.')
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }
    if (userRole !== 'DRIVER' && userRole !== 'ADMIN') {
      queueRouteAccessNotice('Tài khoản hiện tại không có quyền truy cập khu vực lái xe.')
      return '/';
    }
  }

  // Đủ điều kiện hợp lệ -> Cho phép đi tiếp
  return true;
});

export default router;
