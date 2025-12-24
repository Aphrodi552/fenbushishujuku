<template>
    <div class="schedule-page">
      
      <header class="main-header">
        <div class="header-inner">
          <div class="logo-group" @click="router.push('/user')">
            <span class="logo-icon">🏥</span>
            <div class="logo-text">
              <h1>浙江省人民医院</h1>
              <small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small>
            </div>
          </div>
          <div class="back-home" @click="router.push('/user')">
            <Icon icon="mdi:home" /> 返回首页
          </div>
        </div>
      </header>
  
      <div class="breadcrumb-bar">
        <div class="bar-content">
          <span @click="router.push('/user')">网站首页</span> 
          <Icon icon="mdi:chevron-right" class="sep" />
          <span>就诊指南</span>
          <Icon icon="mdi:chevron-right" class="sep" />
          <span class="current">门诊排班</span>
        </div>
        <div class="page-title-overlay">门诊排班</div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <div class="filter-toolbar">
            <div class="campus-tabs">
              <div 
                v-for="c in ['朝晖院区', '屏峰院区']" 
                :key="c"
                class="campus-tab"
                :class="{ active: activeCampus === c }"
                @click="activeCampus = c"
              >
                <span class="marker" v-if="activeCampus === c"></span>
                {{ c }}
              </div>
            </div>
  
            <div class="date-navigator">
              <button class="btn-nav"><Icon icon="mdi:chevron-left" /></button>
              <span class="date-range">2025.12.23 ~ 2025.12.29</span>
              <button class="btn-nav"><Icon icon="mdi:chevron-right" /></button>
            </div>
          </div>
  
          <div class="type-search-bar">
            <div class="type-tabs">
              <div 
                v-for="t in ['名医门诊', '精英门诊', '专家门诊', '普通门诊']" 
                :key="t" 
                class="type-btn"
                :class="{ active: activeType === t }"
                @click="activeType = t"
              >
                {{ t }}
              </div>
            </div>
            <div class="search-box">
              <Icon icon="mdi:magnify" class="search-icon"/>
              <input type="text" placeholder="请输入疾病/科室名称" />
              <button class="btn-search">搜索</button>
            </div>
          </div>
  
          <div class="schedule-table-wrapper">
            <table class="schedule-table">
              <thead>
                <tr>
                  <th style="width: 150px">科室</th>
                  <th style="width: 150px">副科室</th>
                  <th style="width: 80px">时段</th>
                  <th v-for="(day, idx) in weekDays" :key="idx">
                    <div class="th-date">{{ day.date }}</div>
                    <div class="th-week">{{ day.week }}</div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <template v-for="dept in scheduleData" :key="dept.id">
                  <tr>
                    <td rowspan="2" class="col-dept">{{ dept.name }}</td>
                    <td rowspan="2" class="col-sub-dept">{{ dept.subName }}</td>
                    <td class="col-period">上午</td>
                    <td v-for="(day, dIdx) in weekDays" :key="'am'+dIdx" class="col-doc">
                      <span v-if="getDoctor(dept, day.date, 'am')" class="doc-name">
                        {{ getDoctor(dept, day.date, 'am') }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td class="col-period">下午</td>
                    <td v-for="(day, dIdx) in weekDays" :key="'pm'+dIdx" class="col-doc">
                      <span v-if="getDoctor(dept, day.date, 'pm')" class="doc-name">
                        {{ getDoctor(dept, day.date, 'pm') }}
                      </span>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
  
        </div>
      </main>
  
      <footer class="simple-footer">
        <div class="footer-inner">
          <p>Copyright © 2025 浙江省人民医院 | 浙ICP备06015436号</p>
          <p>技术支持：杭州梦智能科技有限公司</p>
        </div>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  
  const router = useRouter();
  const activeCampus = ref('朝晖院区');
  const activeType = ref('名医门诊');
  
  // 模拟一周日期
  const weekDays = [
    { date: '12.23', week: '周二' },
    { date: '12.24', week: '周三' },
    { date: '12.25', week: '周四' },
    { date: '12.26', week: '周五' },
    { date: '12.27', week: '周六' },
    { date: '12.28', week: '周日' },
    { date: '12.29', week: '周一' },
  ];
  
  // 模拟排班数据
  const scheduleData = [
    {
      id: 1,
      name: '心血管内科',
      subName: '心血管内科',
      schedule: {
        '12.25': { am: '王长华' },
        '12.24': { pm: '沈乃吉' },
        '12.26': { pm: '沈乃吉' }
      }
    },
    {
      id: 2,
      name: '呼吸内科',
      subName: '呼吸内科',
      schedule: {
        '12.26': { am: '叶飘' },
        '12.23': { pm: '许武林' },
        '12.24': { pm: '陈淳' }
      }
    },
    {
      id: 3,
      name: '内分泌科',
      subName: '内分泌科',
      schedule: {
        '12.23': { pm: '邢玉波' },
        '12.24': { pm: '宋迎香' },
        '12.25': { pm: '宋迎香' },
        '12.27': { am: '马江波', pm: '吴晖' },
        '12.28': { pm: '王丽君' }
      }
    },
    {
      id: 4,
      name: '肿瘤内科',
      subName: '肿瘤内科',
      schedule: {
        '12.23': { pm: '杨柳' }
      }
    },
    {
      id: 5,
      name: '老年医学科',
      subName: '高血压门诊',
      schedule: {
        '12.26': { pm: '朱霞' },
        '12.27': { pm: '朱霞' }
      }
    }
  ];
  
  // 辅助函数：获取医生名字
  const getDoctor = (dept, dateStr, period) => {
    if (dept.schedule[dateStr] && dept.schedule[dateStr][period]) {
      return dept.schedule[dateStr][period];
    }
    return '';
  };
  </script>
  
  <style scoped>
  .schedule-page { min-height: 100vh; background: #fff; font-family: 'Helvetica Neue', Arial, sans-serif; }
  
  /* Header 复用 */
  .main-header { height: 80px; background: white; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: center; }
  .header-inner { width: 100%; max-width: 1200px; padding: 0 20px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  
  /* 面包屑 (橙色背景) */
  .breadcrumb-bar { background: #f0ad4e; height: 100px; position: relative; display: flex; align-items: flex-end; padding-bottom: 20px; padding-left: calc(50vw - 600px + 20px); overflow: hidden; }
  .bar-content { color: white; font-size: 0.9rem; display: flex; align-items: center; gap: 8px; z-index: 2; position: relative; }
  .bar-content span { cursor: pointer; opacity: 0.9; }
  .bar-content .sep { font-size: 1.2rem; opacity: 0.6; }
  .page-title-overlay { position: absolute; left: calc(50vw - 600px + 20px); top: 15px; font-size: 2.5rem; color: rgba(255,255,255,0.9); font-weight: bold; }
  
  /* 内容容器 */
  .main-content { padding: 40px 0; background: #fff; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
  
  /* 筛选工具栏 */
  .filter-toolbar { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 30px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
  .campus-tabs { display: flex; gap: 40px; font-size: 1.2rem; font-weight: bold; color: #666; }
  .campus-tab { cursor: pointer; padding-bottom: 10px; position: relative; display: flex; align-items: center; gap: 8px; }
  .campus-tab.active { color: #004ea2; }
  .marker { width: 10px; height: 10px; background: #004ea2; display: inline-block; border-radius: 2px; }
  
  .date-navigator { display: flex; align-items: center; gap: 15px; color: #333; font-weight: bold; font-size: 1.1rem; }
  .btn-nav { width: 30px; height: 30px; border-radius: 50%; border: 1px solid #ccc; background: white; color: #666; cursor: pointer; display: flex; align-items: center; justify-content: center; }
  .btn-nav:hover { background: #004ea2; color: white; border-color: #004ea2; }
  
  /* 类型与搜索 */
  .type-search-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
  .type-tabs { display: flex; gap: 0; }
  .type-btn { padding: 8px 25px; background: #f0f2f5; color: #666; cursor: pointer; border-right: 1px solid white; font-size: 0.9rem; transition: 0.3s; }
  .type-btn:first-child { border-radius: 4px 0 0 4px; }
  .type-btn:last-child { border-radius: 0 4px 4px 0; border-right: none; }
  .type-btn.active { background: #2f80ed; color: white; }
  
  .search-box { position: relative; display: flex; align-items: center; }
  .search-box input { width: 300px; padding: 10px 10px 10px 35px; border: 1px solid #ddd; border-radius: 30px; outline: none; }
  .search-icon { position: absolute; left: 12px; color: #999; }
  .btn-search { margin-left: 10px; background: #2f80ed; color: white; border: none; padding: 10px 25px; border-radius: 30px; cursor: pointer; }
  
  /* 表格样式 */
  .schedule-table-wrapper { border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; }
  .schedule-table { width: 100%; border-collapse: collapse; text-align: center; }
  .schedule-table thead { background: #2f80ed; color: white; }
  .schedule-table th { padding: 15px 5px; font-weight: normal; border-right: 1px solid rgba(255,255,255,0.2); }
  .th-date { font-weight: bold; font-size: 1.1rem; }
  .th-week { font-size: 0.8rem; opacity: 0.9; }
  
  .schedule-table td { border: 1px solid #eee; padding: 15px 5px; color: #333; font-size: 0.95rem; }
  .col-dept, .col-sub-dept { font-weight: bold; background: #fbfbfb; color: #333; }
  .col-period { color: #666; }
  .doc-name { display: block; cursor: pointer; font-weight: bold; }
  .doc-name:hover { color: #2f80ed; }
  
  /* Footer */
  .simple-footer { background: #1a3a6e; padding: 40px 0; color: rgba(255,255,255,0.6); font-size: 0.85rem; text-align: center; margin-top: 50px; }
  </style>