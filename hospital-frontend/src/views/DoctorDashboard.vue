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
            v-for="(item, index) in menuItems" 
            :key="index"
            class="nav-item"
            :class="{ active: activeMenu === item.id }"
            @click="activeMenu = item.id"
          >
            <Icon :icon="item.icon" class="nav-icon" />
            <span>{{ item.name }}</span>
          </div>
        </nav>
  
        <div class="sidebar-footer">
          <div class="sys-status">
            <span class="dot green"></span> 数据节点: Node-01
          </div>
          <button class="btn-logout" @click="handleLogout">
            <Icon icon="mdi:logout" /> 退出
          </button>
        </div>
      </aside>
  
      <main class="main-content">
        
        <header class="top-header">
          <div class="breadcrumb">
            <span>门诊中心</span> / <span class="current">今日接诊</span>
          </div>
          <div class="user-profile">
            <div class="avatar">👨‍⚕️</div>
            <div class="info">
              <span class="name">李华 主任医师</span>
              <span class="dept">心血管内科</span>
            </div>
            <Icon icon="mdi:bell-outline" class="bell-icon" />
          </div>
        </header>
  
        <div class="work-area">
          
          <div class="stats-row">
            <div class="stat-card blue-gradient">
              <div class="stat-info">
                <h3>待接诊</h3>
                <span class="num">12</span>
              </div>
              <Icon icon="mdi:account-clock" class="bg-icon" />
            </div>
            <div class="stat-card green-gradient">
              <div class="stat-info">
                <h3>已完成</h3>
                <span class="num">28</span>
              </div>
              <Icon icon="mdi:account-check" class="bg-icon" />
            </div>
            <div class="stat-card orange-gradient">
              <div class="stat-info">
                <h3>今日挂号</h3>
                <span class="num">45</span>
              </div>
              <Icon icon="mdi:calendar-today" class="bg-icon" />
            </div>
            <div class="stat-card purple-gradient">
              <div class="stat-info">
                <h3>好评率</h3>
                <span class="num">98%</span>
              </div>
              <Icon icon="mdi:star-face" class="bg-icon" />
            </div>
          </div>
  
          <div class="panel-container">
            <div class="panel-header">
              <h3>候诊队列 <small>Patient Queue</small></h3>
              <div class="actions">
                <button class="btn-refresh"><Icon icon="mdi:refresh" /> 刷新</button>
                <button class="btn-call-next">
                  <Icon icon="mdi:bullhorn-outline" /> 叫号下一位
                </button>
              </div>
            </div>
            
            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>就诊号</th>
                    <th>姓名</th>
                    <th>性别</th>
                    <th>年龄</th>
                    <th>挂号类型</th>
                    <th>等待时间</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(patient, index) in patientQueue" :key="patient.id">
                    <td class="id-col">#{{ patient.id }}</td>
                    <td class="name-col">{{ patient.name }}</td>
                    <td>{{ patient.gender }}</td>
                    <td>{{ patient.age }}</td>
                    <td>
                      <span class="type-badge" :class="patient.typeClass">{{ patient.type }}</span>
                    </td>
                    <td>{{ patient.waitTime }} mins</td>
                    <td>
                      <span class="status-dot" :class="patient.statusColor"></span>
                      {{ patient.status }}
                    </td>
                    <td>
                      <button class="btn-action primary" @click="handleDiagnose(patient)">接诊</button>
                      <button class="btn-action text">查看档案</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
  
        </div>
      </main>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  
  const router = useRouter();
  const activeMenu = ref('dashboard');
  
  // 侧边栏菜单
  const menuItems = [
    { id: 'dashboard', name: '今日接诊', icon: 'mdi:monitor-dashboard' },
    { id: 'schedule', name: '排班管理', icon: 'mdi:calendar-clock' },
    { id: 'records', name: '病历档案', icon: 'mdi:folder-account' },
    { id: 'history', name: '历史记录', icon: 'mdi:history' },
    { id: 'settings', name: '个人设置', icon: 'mdi:cog' },
  ];
  
  // 模拟患者队列数据
  const patientQueue = ref([
    { id: '202501', name: '张伟', gender: '男', age: 35, type: '普通号', typeClass: 'normal', waitTime: 5, status: '候诊中', statusColor: 'blue' },
    { id: '202502', name: '李秀英', gender: '女', age: 62, type: '专家号', typeClass: 'expert', waitTime: 12, status: '候诊中', statusColor: 'blue' },
    { id: '202503', name: '王强', gender: '男', age: 28, type: '急诊', typeClass: 'emergency', waitTime: 1, status: '准备中', statusColor: 'orange' },
    { id: '202504', name: '陈静', gender: '女', age: 45, type: '普通号', typeClass: 'normal', waitTime: 25, status: '过号', statusColor: 'gray' },
    { id: '202505', name: '刘洋', gender: '男', age: 19, type: '普通号', typeClass: 'normal', waitTime: 30, status: '候诊中', statusColor: 'blue' },
  ]);
  
  const handleLogout = () => {
    if(confirm('确定要退出登录吗？')) {
      router.push('/login');
    }
  };
  
  const handleDiagnose = (p) => {
    alert(`开始接诊患者：${p.name} (ID: ${p.id})`);
  };
  </script>
  
  <style scoped>
  /* 布局容器 */
  .doctor-layout {
    display: flex;
    height: 100vh;
    width: 100vw;
    background-color: #f0f2f5;
    font-family: 'Helvetica Neue', Arial, sans-serif;
    overflow: hidden;
  }
  
  /* --- 1. 侧边栏样式 --- */
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
  
  
  /* --- 2. 主体内容样式 --- */
  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  
  /* 顶部 Header */
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
  .info .dept { font-size: 0.75rem; color: #999; }
  .bell-icon { font-size: 1.5rem; color: #666; cursor: pointer; margin-left: 10px; }
  
  /* 核心工作区 */
  .work-area {
    flex: 1;
    padding: 30px;
    overflow-y: auto;
  }
  
  /* 统计卡片 */
  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 25px;
  }
  .stat-card {
    padding: 20px 25px;
    border-radius: 12px;
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 5px 15px rgba(0,0,0,0.1);
    position: relative;
    overflow: hidden;
    transition: transform 0.2s;
  }
  .stat-card:hover { transform: translateY(-3px); }
  .stat-info h3 { margin: 0; font-size: 0.9rem; opacity: 0.9; font-weight: normal; }
  .stat-info .num { font-size: 2rem; font-weight: bold; display: block; margin-top: 5px; }
  .bg-icon { font-size: 4rem; opacity: 0.2; position: absolute; right: -10px; bottom: -10px; transform: rotate(-15deg); }
  
  /* 渐变色 */
  .blue-gradient { background: linear-gradient(135deg, #3b82f6, #0056b3); }
  .green-gradient { background: linear-gradient(135deg, #42e695, #3bb2b8); }
  .orange-gradient { background: linear-gradient(135deg, #f093fb, #f5576c); }
  .purple-gradient { background: linear-gradient(135deg, #5ee7df, #b490ca); }
  
  /* 列表面板 */
  .panel-container {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 5px 20px rgba(0,0,0,0.03);
    height: calc(100% - 150px); /* 撑满剩余高度 */
    display: flex;
    flex-direction: column;
  }
  .panel-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  }
  .panel-header h3 { margin: 0; font-size: 1.2rem; color: #333; }
  .panel-header small { color: #999; font-weight: normal; margin-left: 8px; }
  
  .actions { display: flex; gap: 10px; }
  .btn-refresh { border: 1px solid #ddd; background: white; padding: 8px 15px; border-radius: 6px; cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  .btn-call-next { background: #004ea2; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 5px; font-weight: bold; transition: 0.2s; }
  .btn-call-next:hover { background: #003d80; box-shadow: 0 4px 10px rgba(0,78,162,0.3); }
  
  /* 表格样式 */
  .table-wrapper { flex: 1; overflow-y: auto; }
  table { width: 100%; border-collapse: collapse; }
  th { text-align: left; padding: 15px; color: #999; font-size: 0.85rem; border-bottom: 1px solid #eee; font-weight: 500; }
  td { padding: 15px; border-bottom: 1px solid #f9f9f9; color: #333; font-size: 0.95rem; }
  tr:hover { background: #f0f7ff; }
  
  .id-col { font-family: monospace; color: #004ea2; font-weight: bold; }
  .name-col { font-weight: 600; }
  
  .type-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; }
  .type-badge.normal { background: #e6f7ff; color: #004ea2; }
  .type-badge.expert { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }
  .type-badge.emergency { background: #fff1f0; color: #f5222d; font-weight: bold; }
  
  .status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 5px; vertical-align: middle; }
  .status-dot.blue { background: #1890ff; }
  .status-dot.orange { background: #fa8c16; }
  .status-dot.gray { background: #d9d9d9; }
  
  .btn-action { border: none; background: none; cursor: pointer; font-size: 0.9rem; margin-right: 10px; }
  .btn-action.primary { color: #004ea2; font-weight: bold; }
  .btn-action.text { color: #999; }
  .btn-action:hover { text-decoration: underline; }
  
  </style>
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
