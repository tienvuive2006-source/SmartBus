import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import SearchResultsView from '../views/booking/SearchResultsView.vue'
import SeatSelectionView from '../views/booking/SeatSelectionView.vue'
import PaymentView from '../views/booking/PaymentView.vue'
import PaymentSuccessView from '../views/booking/PaymentSuccessView.vue'
import ProfileView from '../views/user/ProfileView.vue'
import HistoryView from '../views/user/HistoryView.vue'
import AiAssistantView from '../views/user/AiAssistantView.vue'
import NotificationsView from '../views/user/NotificationsView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminTripManagerView from '../views/admin/AdminTripManagerView.vue'
import AdminFleetStatusView from '../views/admin/AdminFleetStatusView.vue'

import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import AdminUserManagerView from '../views/admin/AdminUserManagerView.vue'
import AdminBookingManagerView from '../views/admin/AdminBookingManagerView.vue'
import AdminReviewManagerView from '../views/admin/AdminReviewManagerView.vue'
import AdminRouteManagerView from '../views/admin/AdminRouteManagerView.vue'
import InspectorLayout from '../layouts/InspectorLayout.vue'
import InspectorDashboardView from '../views/inspector/InspectorDashboardView.vue'
import InspectorTripDetailView from '../views/inspector/InspectorTripDetailView.vue'
import DriverLayout from '../layouts/DriverLayout.vue'
import DriverDashboardView from '../views/driver/DriverDashboardView.vue'
import DriverTripDetailView from '../views/driver/DriverTripDetailView.vue'
import AdminAuditLogView from '../views/admin/AdminAuditLogView.vue'
import AdminBannerManagerView from '../views/admin/AdminBannerManagerView.vue'
import AdminIncidentManagerView from '../views/admin/AdminIncidentManagerView.vue'
import AdminDriverWorkspaceView from '../views/admin/AdminDriverWorkspaceView.vue'
import AdminLeaveManagerView from '../views/admin/AdminLeaveManagerView.vue'
import AdminDriverWrapperView from '../views/admin/AdminDriverWrapperView.vue'
import AdminDriverLocationsView from '../views/admin/AdminDriverLocationsView.vue'

// (I will add routes inside the router array)

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
          meta: { hideFooter: true }
        },
        {
          path: 'booking/payment',
          name: 'payment',
          component: PaymentView,
          meta: { hideFooter: true }
        },
        {
          path: 'booking/payment-success',
          name: 'payment-success',
          component: PaymentSuccessView,
          meta: { hideFooter: true }
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
          path: 'ai-assistant',
          name: 'ai-assistant',
          component: AiAssistantView,
          meta: { title: 'Trợ lý AI', showBack: false }
        },
        {
          path: 'notifications',
          name: 'notifications',
          component: NotificationsView,
          meta: { title: 'Thông báo', showBack: true }
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
          path: 'landing-demo',
          name: 'landing-demo',
          component: () => import('../views/LandingPageView.vue'),
          meta: { title: 'Landing Page Demo', hideHeader: true, hideFooter: true, showBack: false }
        }
      ]
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
          path: 'users',
          name: 'admin-user-manager',
          component: AdminUserManagerView
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

  if (userStr) {
    try { userRole = JSON.parse(userStr)?.role } catch { /* bỏ qua */ }
  }

  // 🔒 Bảo vệ route ADMIN
  if (to.path.startsWith('/admin')) {
    if (!token) {
      alert("🔒 BẢO MẬT: Vui lòng đăng nhập tài khoản Quản Trị Viên!");
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }
    if (userRole !== 'ADMIN') {
      alert("⛔ CẢNH BÁO: Bạn không có đặc quyền truy cập Bảng Quản Trị!\nHệ thống sẽ trục xuất bạn về Trang Chủ.");
      return '/';
    }
  }

  // 🔒 Bảo vệ route INSPECTOR
  if (to.path.startsWith('/inspector')) {
    if (!token) {
      alert("🔒 BẢO MẬT: Vui lòng đăng nhập tài khoản Nhân viên Soát vé!");
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }
    if (userRole !== 'INSPECTOR' && userRole !== 'ADMIN') {
      alert("⛔ CẢNH BÁO: Bạn không có quyền hạn Soát vé!");
      return '/';
    }
  }

  // 🔒 Bảo vệ route DRIVER
  if (to.path.startsWith('/driver')) {
    if (!token) {
      alert("🔒 BẢO MẬT: Vui lòng đăng nhập tài khoản Lái xe!");
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }
    if (userRole !== 'DRIVER' && userRole !== 'ADMIN') {
      alert("⛔ CẢNH BÁO: Bạn không có quyền hạn Lái xe!");
      return '/';
    }
  }

  // Đủ điều kiện hợp lệ -> Cho phép đi tiếp
  return true;
});

export default router;
