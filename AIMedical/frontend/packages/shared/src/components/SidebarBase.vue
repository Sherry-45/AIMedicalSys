<template>
  <div class="sidebar-container">
    <div class="sidebar-header">
      <h2>{{ title }}</h2>
    </div>
    <nav class="sidebar-nav">
      <div v-if="menus && menus.length > 0" class="menu-section">
        <template v-for="menu in menus" :key="menu.id">
          <div class="menu-item" :class="{ 'has-children': hasChildren(menu) }">
            <router-link
              v-if="!hasChildren(menu)"
              :to="menu.path"
              class="nav-item"
              :class="{ active: isActive(menu.path) }"
              @click="emit('select', menu.path)"
            >
              <span v-if="iconEmoji(menu.icon)" class="menu-icon">{{ iconEmoji(menu.icon) }}</span>
              <span class="menu-name">{{ menu.name }}</span>
            </router-link>
            <div
              v-else
              class="nav-item parent-item"
              :class="{ active: isParentActive(menu) }"
              @click="toggleExpand(menu)"
            >
              <span v-if="iconEmoji(menu.icon)" class="menu-icon">{{ iconEmoji(menu.icon) }}</span>
              <span class="menu-name">{{ menu.name }}</span>
              <span class="expand-icon" :class="{ expanded: isExpanded(menu) }">▶</span>
            </div>
            <!-- 子菜单：根据展开状态显示 -->
            <div v-if="hasChildren(menu) && isExpanded(menu)" class="submenu">
              <router-link
                v-for="child in menu.children"
                :key="child.id"
                :to="child.path"
                class="nav-item submenu-item"
                :class="{ active: isActive(child.path) }"
                @click="emit('select', child.path)"
              >
                <span class="menu-name">{{ child.name }}</span>
              </router-link>
            </div>
          </div>
        </template>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
/**
 * 通用侧边栏组件（T10/T11 修复）
 *
 * <p>特性：
 * <ul>
 *   <li>纯 UI 组件，通过 props 接收菜单数据，通过 emit 触发选择事件，不依赖具体应用的 store</li>
 *   <li>支持子菜单展开/收起（T11）：父级菜单点击切换展开状态，子菜单根据展开状态显示/隐藏</li>
 *   <li>区分父菜单（有 children）和叶子菜单（无 children）：父菜单不跳转，仅展开/收起</li>
 * </ul>
 */
import { ref, watch, type Ref } from 'vue'
import { useRoute } from 'vue-router'
import type { MenuItem } from '../types'

const props = defineProps<{
  /** 侧边栏标题 */
  title: string
  /** 菜单树 */
  menus: MenuItem[]
  /** 当前激活菜单路径 */
  activeMenu: string
}>()

const emit = defineEmits<{
  (e: 'select', path: string): void
}>()

const route = useRoute()

// 已展开的父菜单 id 集合
const expandedKeys: Ref<Set<number>> = ref(new Set())

/**
 * 后端 icon 字段存的是英文图标名（如 'stethoscope'、'medicine'），
 * 前端无对应图标库，直接显示会变成英文文本。此处映射为 emoji，
 * 未匹配的图标名返回空字符串（不显示），避免出现英文文本。
 */
const ICON_EMOJI_MAP: Record<string, string> = {
  dashboard: '📊',
  stethoscope: '🩺',
  bell: '🔔',
  'user-friend': '👤',
  registration: '📋',
  'edit-square': '📝',
  calendar: '📅',
  robot: '🤖',
  brain: '🧠',
  search: '🔍',
  edit: '✏️',
  check: '✅',
  'file-text': '📄',
  medicine: '💊',
  rollback: '↩️',
  list: '📋',
  box: '📦',
  clipboard: '📋',
  swap: '🔄',
  shop: '🏪',
  'user-add': '👤',
  'money-collect': '💰',
  'file-done': '✅',
  heart: '❤️',
  'line-chart': '📈',
  setting: '⚙️',
  user: '👤',
  role: '👥',
  menu: '☰',
  peoples: '👥',
  'user-tag': '🏷️',
  idcard: '🪪',
  'id-card': '🪪',
  form: '📝',
  logininfor: '🔐',
  monitor: '📈',
}

function iconEmoji(icon?: string): string {
  if (!icon) return ''
  return ICON_EMOJI_MAP[icon] || ''
}

/**
 * 自动展开当前激活路径所属的父目录，避免用户登录后看到全部收起的目录、
 * 找不到当前页面所在的分组。
 */
function autoExpandActiveParent(): void {
  if (!props.menus || props.menus.length === 0) return
  const currentPath = props.activeMenu || route.path
  for (const menu of props.menus) {
    if (hasChildren(menu) && menu.children!.some((child) => isActive(child.path))) {
      if (!expandedKeys.value.has(menu.id)) {
        const next = new Set(expandedKeys.value)
        next.add(menu.id)
        expandedKeys.value = next
      }
    }
  }
}

watch(
  () => [props.menus, route.path, props.activeMenu] as const,
  () => {
    autoExpandActiveParent()
  },
  { immediate: true, deep: true },
)

function hasChildren(menu: MenuItem): boolean {
  return !!(menu.children && menu.children.length > 0)
}

function isExpanded(menu: MenuItem): boolean {
  return expandedKeys.value.has(menu.id)
}

function toggleExpand(menu: MenuItem): void {
  if (!hasChildren(menu)) {
    return
  }
  const next = new Set(expandedKeys.value)
  if (next.has(menu.id)) {
    next.delete(menu.id)
  } else {
    next.add(menu.id)
  }
  expandedKeys.value = next
}

function isActive(path: string): boolean {
  // 优先使用 props 中的 activeMenu，否则使用 route.path
  return props.activeMenu === path || route.path === path
}

function isParentActive(menu: MenuItem): boolean {
  // 父菜单高亮：任一子菜单激活则父菜单高亮
  if (!menu.children) return false
  return menu.children.some((child) => isActive(child.path))
}
</script>

<style scoped>
.sidebar-container {
  height: 100vh;
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  font-size: 18px;
  font-weight: 500;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.nav-item.active {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.menu-section {
  margin-top: 10px;
}

.menu-item {
  position: relative;
}

.menu-item .nav-item {
  padding-left: 30px;
}

.menu-item.has-children > .nav-item {
  padding-right: 30px;
}

.menu-icon {
  font-size: 14px;
}

.menu-name {
  flex: 1;
}

.expand-icon {
  font-size: 10px;
  transition: transform 0.3s;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.submenu {
  background: rgba(0, 0, 0, 0.1);
}

.submenu-item {
  padding-left: 50px !important;
  font-size: 14px;
}
</style>
