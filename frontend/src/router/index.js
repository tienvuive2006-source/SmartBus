import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import HomeView from '../views/HomeView.vue'
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
import AdminBusTypeView from '../views/admin/AdminBusTypeView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import AdminUserManagerView from '../views/admin/AdminUserManagerView.vue'
import AdminBookingManagerView from '../views/admin/AdminBookingManagerView.vue'

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
          component: HomeView,
          // Thêm hideHeader: true vào đây
          meta: { title: 'SkyBus', showBack: false, hideHeader: true }
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
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'booking/payment',
          name: 'payment',
          component: PaymentView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'booking/payment-success',
          name: 'payment-success',
          component: PaymentSuccessView,
          meta: { hideHeader: true, hideFooter: true }
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileView,
          meta: { title: 'Hồ sơ cá nhân', showBack: false, hideHeader: true }
        },
        {
          path: 'history',
          name: 'history',
          component: HistoryView,
          meta: { title: 'Lịch sử đặt vé', showBack: false, hideHeader: true }
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
          path: 'bus-type',
          name: 'admin-bus-type',
          component: AdminBusTypeView
        },
        {
          path: 'fleet-status',
          name: 'admin-fleet-status',
          component: AdminFleetStatusView
        },
        {
          path: 'users',
          name: 'admin-user-manager',
          component: AdminUserManagerView
        }
      ]
    }
  ]
})

// 🛡️ HỆ THỐNG TƯỜNG LỬA CỬA NGÕ (VUE NAVIGATION GUARD)
// Chặn đứng 100% các hành vi truy cập trái phép vào khu vực ADMIN
router.beforeEach((to, from, next) => {
  // Kiểm tra xem đường đi có bắt đầu bằng tiền tố quản trị /admin hay không
  if (to.path.startsWith('/admin')) {
    const storedUser = localStorage.getItem('currentUser');

    // TRƯỜNG HỢP 1: Chưa hề đăng nhập
    if (!storedUser) {
      alert("🔒 BẢO MẬT: Vui lòng đăng nhập tài khoản Quản Trị Viên!");
      return next('/auth/login');
    }

    try {
      const user = JSON.parse(storedUser);

      // TRƯỜNG HỢP 2: Đã đăng nhập nhưng ROLE là USER thường (Xâm nhập trái phép)
      if (user.role !== 'ADMIN') {
        alert("⛔ CẢNH BÁO XÂM NHẬP: Bạn không có đặc quyền truy cập Bảng Quản Trị!\nHệ thống sẽ tự động trục xuất bạn về Trang Chủ.");
        return next('/'); // Đá bay về trang chủ ngay lập tức!
      }
    } catch (error) {
      // Lỗi format bộ nhớ -> Bắt login lại cho an toàn
      localStorage.removeItem('currentUser');
      return next('/auth/login');
    }
  }

  // Đủ điều kiện hợp lệ -> Cho phép đi tiếp
  next();
});

export default router;
