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

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView,
          meta: { title: 'TransLink Pro', showBack: false }
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
          path: 'fleet-status',
          name: 'admin-fleet-status',
          component: AdminFleetStatusView
        }
      ]
    }
  ]
})

export default router
