<template>
  <div class="dashboard-panel">
    <div class="overview-grid">
      <div class="data-card card-blue">
        <div class="card-title">今日预约总量</div>
        <div class="card-num">3,421</div>
        <div class="card-trend up">↑ 12% 环比增长</div>
        <Icon icon="mdi:calendar-check" class="bg-icon" />
      </div>
      <div class="data-card card-purple">
        <div class="card-title">活跃医生数</div>
        <div class="card-num">186</div>
        <div class="card-trend">在线率 98%</div>
        <Icon icon="mdi:doctor" class="bg-icon" />
      </div>
      <div class="data-card card-orange">
        <div class="card-title">数据库QPS</div>
        <div class="card-num">8,900</div>
        <div class="card-trend down">负载均衡正常</div>
        <Icon icon="mdi:database-search" class="bg-icon" />
      </div>
      <div class="data-card card-red">
        <div class="card-title">异常审计日志</div>
        <div class="card-num">2</div>
        <div class="card-trend alert">需立即处理</div>
        <Icon icon="mdi:alert-decagram" class="bg-icon" />
      </div>
    </div>

    <div class="admin-table-section">
      <div class="section-header">
        <h3>最近系统操作审计 (Audit Log)</h3>
        <div class="filter-group">
          <input type="date" class="date-picker" />
          <button class="btn-export">导出报表</button>
        </div>
      </div>

      <table class="admin-table">
        <thead>
        <tr>
          <th>操作ID</th>
          <th>操作人</th>
          <th>IP地址</th>
          <th>操作模块</th>
          <th>执行动作</th>
          <th>耗时(ms)</th>
          <th>状态</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="i in 5" :key="i">
          <td>#LOG-202501{{ i }}</td>
          <td>Admin_001</td>
          <td>192.168.1.{{ 10 + i }}</td>
          <td>排班管理</td>
          <td>修改号源池 [UPDATE]</td>
          <td>{{ 15 + i * 2 }}ms</td>
          <td><span class="badge-success">成功</span></td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'
 </script>

 <style scoped>
/* 整体布局 */
.admin-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f3f4f6;
  font-family: 'Segoe UI', sans-serif;
}

/* --- 左侧侧边栏 (深色风格) --- */
.admin-sidebar {
  width: 250px;
  background: #1e293b; /* 深灰蓝 */
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-brand {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #334155;
  color: white;
}
.logo-box {
  width: 35px; height: 35px; background: #3b82f6; border-radius: 6px;
  display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 12px;
}
.brand-text h2 { margin: 0; font-size: 1.1rem; }
.brand-text small { font-size: 0.7rem; color: #64748b; }

.nav-menu { flex: 1; padding: 20px 0; overflow-y: auto; }
.menu-category {
  padding: 10px 25px;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #475569;
  margin-top: 10px;
}

.nav-link {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 25px;
  cursor: pointer;
  transition: 0.2s;
  color: #94a3b8;
}
.nav-link:hover { color: white; background: rgba(255,255,255,0.05); }
.nav-link.active {
  color: white;
  background: #3b82f6; /* 选中亮蓝 */
  border-right: 3px solid #60a5fa;
}
.nav-icon { font-size: 1.2rem; }

.sidebar-bottom {
  padding: 20px;
  border-top: 1px solid #334155;
  display: flex; justify-content: space-between; align-items: center;
}
.admin-user { display: flex; align-items: center; gap: 8px; color: white; font-size: 0.9rem; }
.btn-quit { background: none; border: none; color: #ef4444; font-size: 1.2rem; cursor: pointer; }

/* --- 右侧主内容 --- */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部栏 */
.admin-header {
  height: 64px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 30px;
}
.page-title { font-size: 1.25rem; font-weight: 600; color: #1e293b; }
.header-tools { display: flex; align-items: center; gap: 20px; }
.status-indicator { font-size: 0.85rem; color: #10b981; display: flex; align-items: center; gap: 6px; background: #ecfdf5; padding: 5px 12px; border-radius: 20px; }
.dot { width: 8px; height: 8px; background: #10b981; border-radius: 50%; }
.blinking { animation: blink 2s infinite; }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
.tool-btn { font-size: 1.2rem; color: #64748b; cursor: pointer; }

/* 内容区 */
.content-wrapper {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

/* 仪表盘卡片 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}
.data-card {
  background: white; border-radius: 12px; padding: 25px;
  position: relative; overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.card-title { color: #64748b; font-size: 0.9rem; margin-bottom: 10px; }
.card-num { font-size: 2rem; font-weight: bold; color: #0f172a; margin-bottom: 5px; }
.card-trend { font-size: 0.8rem; font-weight: 500; }
.card-trend.up { color: #10b981; }
.card-trend.down { color: #3b82f6; }
.card-trend.alert { color: #ef4444; }

.bg-icon { position: absolute; right: -10px; bottom: -10px; font-size: 5rem; opacity: 0.1; }

.card-blue .bg-icon { color: #3b82f6; }
.card-purple .bg-icon { color: #8b5cf6; }
.card-orange .bg-icon { color: #f59e0b; }
.card-red .bg-icon { color: #ef4444; }

/* 表格区域 */
.admin-table-section {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h3 { margin: 0; font-size: 1.1rem; color: #1e293b; }
.filter-group { display: flex; gap: 10px; }
.date-picker { padding: 5px 10px; border: 1px solid #e2e8f0; border-radius: 6px; }
.btn-export { background: #3b82f6; color: white; border: none; padding: 6px 15px; border-radius: 6px; cursor: pointer; }

.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { text-align: left; padding: 15px; background: #f8fafc; color: #64748b; font-size: 0.85rem; font-weight: 600; }
.admin-table td { padding: 15px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 0.9rem; }
.badge-success { background: #dcfce7; color: #166534; padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; }

/* 占位空状态 */
.placeholder-page {
  height: 100%; display: flex; justify-content: center; align-items: center;
}
.empty-state { text-align: center; color: #94a3b8; }
.empty-icon { font-size: 4rem; margin-bottom: 20px; color: #cbd5e1; }
.btn-primary { margin-top: 20px; background: #3b82f6; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
</style>
