import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useMenuStore } from '../stores/menu'

/**
 * 路由配置
 *
 * <p>定义管理员端应用的路由结构和导航守卫。
 *
 * <p>Phase 4 新增静态业务路由（管理员端只读/管理视角，对应医生端同名页面）：
 * - 药房工作台：/pharmacy/dispense、/pharmacy/refund、/pharmacy/drugs
 * - 药库管理：/inventory/stock、/inventory/stocktaking、/inventory/transfer
 * - 线下窗口：/window/registration、/window/charging、/window/payments
 * - 健康档案：/health-record/query、/health-record/trend
 * - 数据查看：/patients（患者管理）、/prescriptions（处方查询）
 * - 系统管理：/system/users、/system/roles、/system/menus
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 数据查看 ----
      {
        path: '/patients',
        name: 'PatientList',
        component: () => import('../views/PatientList.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/prescriptions',
        name: 'PrescriptionList',
        component: () => import('../views/PrescriptionList.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 药房工作台 ----
      {
        path: '/pharmacy/dispense',
        name: 'PharmacyDispense',
        component: () => import('../views/pharmacy/Dispense.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/pharmacy/refund',
        name: 'PharmacyRefund',
        component: () => import('../views/pharmacy/Refund.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/pharmacy/drugs',
        name: 'PharmacyDrugCatalog',
        component: () => import('../views/pharmacy/DrugCatalog.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 药库管理 ----
      {
        path: '/inventory/stock',
        name: 'InventoryStock',
        component: () => import('../views/inventory/StockList.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/inventory/stocktaking',
        name: 'InventoryStocktaking',
        component: () => import('../views/inventory/Stocktaking.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/inventory/transfer',
        name: 'InventoryTransfer',
        component: () => import('../views/inventory/Transfer.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 线下窗口 ----
      {
        path: '/window/registration',
        name: 'WindowRegistration',
        component: () => import('../views/window/Registration.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/window/charging',
        name: 'WindowCharging',
        component: () => import('../views/window/Charging.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/window/payments',
        name: 'WindowPayments',
        component: () => import('../views/window/PaymentRecords.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 健康档案 ----
      {
        path: '/health-record/query',
        name: 'HealthRecordQuery',
        component: () => import('../views/health-record/HealthRecordQuery.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/health-record/trend',
        name: 'HealthRecordTrend',
        component: () => import('../views/health-record/HealthTrend.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 系统管理 ----
      {
        path: '/system/users',
        name: 'UserManagement',
        component: () => import('../views/system/UserManagement.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/system/roles',
        name: 'RoleManagement',
        component: () => import('../views/system/RoleManagement.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/system/menus',
        name: 'MenuManagement',
        component: () => import('../views/system/MenuManagement.vue'),
        meta: { requiresAuth: true },
      },
      // ---- 硬件接入 (Device) ----
      // 静态路径须排在动态参数路径之前
      {
        path: '/devices',
        name: 'DeviceList',
        component: () => import('../views/device/DeviceList.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/devices/:id',
        name: 'DeviceDetail',
        component: () => import('../views/device/DeviceDetail.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: '/devices/:id/messages',
        name: 'DeviceMessageList',
        component: () => import('../views/device/DeviceMessageList.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 路由守卫
 *
 * 检查用户认证状态，未认证用户重定向到登录页。
 * 菜单获取失败时也重定向到登录页。
 * 进入登录页时清理动态路由，避免路由表膨胀。
 */
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  const menuStore = useMenuStore()

  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // 未认证跳转登录前，清理可能残留的菜单和动态路由
      menuStore.clearMenus()
      return next('/login')
    }

    // 加载菜单数据
    if (!menuStore.hasMenus) {
      const success = await menuStore.fetchMenus()
      if (!success) {
        // 菜单获取失败（网络错误/Token过期），清除认证状态并重定向到登录页
        await authStore.logout()
        menuStore.clearMenus()
        return next('/login')
      }
    }
  } else if (to.path === '/login') {
    // 进入登录页时，清理动态路由和菜单状态（登出场景）
    menuStore.clearMenus()
    if (authStore.isAuthenticated) {
      return next('/')
    }
  }

  next()
})

export default router
