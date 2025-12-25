<template>
  <div class="doctor-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">🏥</div>
        <div class="logo-text">
          <h2>医生工作台</h2>
          <small>Doctor Station</small>
        </div>
      </div>

      <nav class="side-nav">
        <div
            v-for="item in menuItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: isActive(item) }"
            @click="go(item)"
        >
          <Icon :icon="item.icon" class="nav-icon" />
          <span>{{ item.name }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button class="btn-logout" @click="handleLogout">
          <Icon icon="mdi:logout" /> 退出
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="breadcrumb">
          <span>医生端</span> / <span class="current">{{ currentTitle }}</span>
        </div>

        <div class="user-profile">
          <div class="avatar">👨‍⚕️</div>
          <div class="info">
            <span class="name">{{ doctorName }}</span>
            <span class="dept">{{ doctorDept }}</span>
          </div>
          <Icon icon="mdi:bell-outline" class="bell-icon" />
        </div>
      </header>

      <div class="work-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'

const router = useRouter()
const route = useRoute()

const doctorName = computed(() => localStorage.getItem('doctorName') || '医生')
const doctorDept = computed(() => localStorage.getItem('doctorDept') || '未设置科室')

const currentTitle = computed(() => route.meta?.title || '工作台')

const menuItems = [
  { id: 'dashboard', name: '今日接诊', icon: 'mdi:monitor-dashboard', routeName: 'DoctorOverview' },
  { id: 'schedule', name: '我的排班', icon: 'mdi:calendar-clock', routeName: 'DoctorSchedule' },
  { id: 'records', name: '病历档案', icon: 'mdi:file-document-outline', routeName: 'DoctorRecords' },
  { id: 'profile', name: '个人信息', icon: 'mdi:account-cog-outline', routeName: 'DoctorProfile' },
]

function isActive(item) {
  return route.name === item.routeName
}

function go(item) {
  if (route.name !== item.routeName) {
    router.push({ name: item.routeName })
  }
}

function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    // 清除所有登录相关数据
    localStorage.removeItem('hospital_token')
    localStorage.removeItem('doctorName')
    localStorage.removeItem('doctorTitle')
    localStorage.removeItem('doctorDept')
    router.push('/login')
  }
}
</script>

<style scoped>
.doctor-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f0f2f5;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0,0,0,0.05);
  z-index: 10;
}

.sidebar-header {
  height: 80px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  border-bottom: 1px solid #f0f0f0;
}
.logo-icon { font-size: 2rem; }
.logo-text h2 { font-size: 1.2rem; margin: 0; color: #004ea2; font-weight: 700; }
.logo-text small { font-size: 0.7rem; color: #999; display: block; }

.side-nav { flex: 1; padding: 20px 0; }
.nav-item {
  display: flex; align-items: center; gap: 15px;
  padding: 15px 30px;
  cursor: pointer;
  color: #666;
  font-weight: 500;
  transition: 0.3s;
  border-right: 4px solid transparent;
}
.nav-item:hover { background: #f6f8fa; color: #004ea2; }
.nav-item.active {
  background: #e6f7ff;
  color: #004ea2;
  border-right-color: #004ea2;
}
.nav-icon { font-size: 1.4rem; }

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}
.sys-status { font-size: 0.75rem; color: #999; margin-bottom: 10px; display: flex; align-items: center; gap: 5px; }
.dot.green { width: 8px; height: 8px; background: #52c41a; border-radius: 50%; }

.btn-logout {
  width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 10px; border: 1px solid #ffdcd6; background: #fff5f5; color: #ff4d4f;
  border-radius: 6px; cursor: pointer; transition: 0.2s;
}
.btn-logout:hover { background: #ff4d4f; color: white; border-color: #ff4d4f; }

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  height: 64px;
  background: white;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.02);
}
.breadcrumb { color: #999; font-size: 0.9rem; }
.breadcrumb .current { color: #333; font-weight: bold; }

.user-profile { display: flex; align-items: center; gap: 15px; }
.avatar { width: 40px; height: 40px; background: #e6f7ff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.info { display: flex; flex-direction: column; text-align: right; }
.info .name { font-weight: bold; font-size: 0.9rem; color: #333; }
.info .title { font-size: 0.75rem; color: #666; }
.info .dept { font-size: 0.75rem; color: #999; }
.bell-icon { font-size: 1.5rem; color: #666; cursor: pointer; margin-left: 10px; }

.work-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}
</style>
