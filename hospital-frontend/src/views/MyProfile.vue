<template>
    <div class="profile-page">
      <header class="main-header">
        <div class="header-inner">
          <div class="logo-group" @click="router.push('/user')">
            <span class="logo-icon">🏥</span>
            <div class="logo-text"><h1>浙江省人民医院</h1><small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small></div>
          </div>
          <div class="back-home" @click="router.push('/user')"><Icon icon="mdi:home" /> 返回首页</div>
        </div>
      </header>
  
      <div class="top-banner-section">
        <div class="banner-bg"><div class="banner-text"><h1>我的信息</h1></div></div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')">网站首页</span><Icon icon="mdi:chevron-right" /><span class="current">个人中心</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          <div class="profile-layout">
            <div class="profile-sidebar">
              <div class="avatar-box">
                <div class="avatar-circle"><img :src="userInfo.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'" alt="avatar"></div>
                <h3 class="user-name">{{ userInfo.realName || '未实名用户' }}</h3>
                <span class="user-role">普通用户</span>
              </div>
              <div class="sidebar-menu">
                <div class="menu-item" :class="{ active: currentTab === 'info' }" @click="currentTab = 'info'"><Icon icon="mdi:account-details" /> 基本资料</div>
                <div class="menu-item" :class="{ active: currentTab === 'reviews' }" @click="currentTab = 'reviews'"><Icon icon="mdi:comment-quote" /> 我的评价</div>
                <div class="menu-item" @click="logout"><Icon icon="mdi:logout" /> 退出登录</div>
              </div>
            </div>
  
            <div class="profile-main">
              <div v-if="currentTab === 'info'">
                <div class="panel-header">
                  <h2>基本资料</h2>
                  <button class="btn-edit" @click="toggleEdit"><Icon :icon="isEditing ? 'mdi:content-save' : 'mdi:pencil'" /> {{ isEditing ? '保存修改' : '编辑资料' }}</button>
                </div>
                <div class="form-container">
                  <div class="form-group"><label>用户名</label><input type="text" v-model="userInfo.username" disabled class="input-disabled"></div>
                  <div class="form-group"><label>真实姓名</label><input type="text" v-model="userInfo.realName" :disabled="!isEditing" :class="{ 'input-edit': isEditing }"></div>
                  <div class="form-group"><label>手机号码</label><input type="text" v-model="userInfo.phone" :disabled="!isEditing" :class="{ 'input-edit': isEditing }"></div>
                </div>
              </div>
  
              <div v-if="currentTab === 'reviews'">
                <div class="panel-header"><h2>我的评价记录</h2></div>
                <div v-if="myReviews.length === 0" class="empty-reviews">
                  <Icon icon="mdi:comment-remove-outline" class="empty-icon" /><p>您还没有发表过任何评价</p>
                </div>
                <div v-else class="reviews-list">
                  <div v-for="rev in myReviews" :key="rev.id" class="my-review-card">
                    <div class="rev-header">
                      <span class="rev-doc">评价对象：{{ rev.doctorName }} 医生</span>
                      <span class="rev-date">{{ rev.time }}</span>
                    </div>
                    <div class="rev-rating">
                      <Icon v-for="n in rev.rating" :key="n" icon="mdi:star" class="star-yellow" />
                    </div>
                    <p class="rev-text">{{ rev.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
  
      <footer class="app-footer">
        <div class="footer-bottom-bar">Copyright © 2025 浙江省人民医院网站版权所有</div>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  
  const router = useRouter();
  const currentTab = ref('info');
  const isEditing = ref(false);
  const myReviews = ref([]);
  
  const userInfo = ref({
    username: 'user_18760',
    realName: '史良帅',
    phone: '187******60',
    email: 'shiliangshuai@example.com',
    avatar: '' 
  });
  
  onMounted(() => {
    const saved = localStorage.getItem('hospital_reviews');
    if (saved) {
      myReviews.value = JSON.parse(saved);
    }
  });
  
  const toggleEdit = () => {
    if (isEditing.value) { alert('信息保存成功！'); isEditing.value = false; } else { isEditing.value = true; }
  };
  
  const logout = () => {
    if(confirm('确定要退出登录吗？')) router.push('/login');
  };
  </script>
  
  <style scoped>
  /* 基础样式复用 */
  .profile-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; display: flex; flex-direction: column; }
  .main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
  .header-inner { width: 100%; max-width: 1200px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  .top-banner-section { background: white; }
  .banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.7), rgba(0,0,0,0.7)), url('https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
  .banner-text h1 { color: white; font-size: 2.2rem; }
  .breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; padding-left: 10%; color: white; }
  .breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; }
  .main-content { flex: 1; padding: 40px 0; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; }
  .profile-layout { display: flex; gap: 30px; }
  .profile-sidebar { width: 280px; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); overflow: hidden; height: fit-content; }
  .avatar-box { background: #2c3e50; padding: 40px 20px; display: flex; flex-direction: column; align-items: center; color: white; }
  .avatar-circle { width: 100px; height: 100px; border-radius: 50%; border: 4px solid rgba(255,255,255,0.2); overflow: hidden; margin-bottom: 15px; background: white; }
  .avatar-circle img { width: 100%; height: 100%; object-fit: cover; }
  .user-name { margin: 0; font-size: 1.4rem; font-weight: bold; }
  .user-role { font-size: 0.8rem; opacity: 0.8; margin-top: 5px; background: rgba(255,255,255,0.2); padding: 2px 10px; border-radius: 10px; }
  .sidebar-menu { padding: 10px 0; }
  .menu-item { padding: 15px 30px; display: flex; align-items: center; gap: 10px; color: #666; cursor: pointer; transition: 0.2s; font-weight: 500; }
  .menu-item:hover { background: #f5f7fa; color: #004ea2; }
  .menu-item.active { border-left: 4px solid #004ea2; background: #eef3fb; color: #004ea2; }
  .profile-main { flex: 1; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); padding: 40px; }
  .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
  .panel-header h2 { margin: 0; font-size: 1.5rem; color: #333; border-left: 5px solid #2c3e50; padding-left: 15px; }
  .btn-edit { background: #004ea2; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
  .form-container { max-width: 600px; }
  .form-group { margin-bottom: 25px; }
  .form-group label { display: block; margin-bottom: 8px; color: #666; font-weight: bold; }
  .form-group input { width: 100%; padding: 12px 15px; border: 1px solid #ddd; border-radius: 6px; outline: none; font-size: 1rem; transition: 0.3s; background: #fff; }
  .input-disabled { background: #f5f5f5 !important; color: #999; cursor: not-allowed; }
  .input-edit { border-color: #004ea2; background: #fbfdff; }
  
  /* 我的评价列表样式 */
  .empty-reviews { text-align: center; color: #999; padding: 40px; }
  .empty-icon { font-size: 3rem; margin-bottom: 10px; }
  .my-review-card { border: 1px solid #eee; border-radius: 8px; padding: 20px; margin-bottom: 20px; background: #fafafa; }
  .rev-header { display: flex; justify-content: space-between; margin-bottom: 10px; font-weight: bold; color: #333; }
  .rev-date { font-weight: normal; color: #999; font-size: 0.85rem; }
  .star-yellow { color: #ffca28; }
  .rev-text { color: #666; line-height: 1.5; margin-top: 10px; }
  .app-footer { background: #1a3a6e; color: rgba(255,255,255,0.6); text-align: center; padding: 20px; margin-top: 50px; }
  </style>