<template>
    <div class="login-container">
      <div class="login-box">
        <div class="login-left">
          <div class="logo-area">
            <span class="logo-icon">🏥</span>
            <span class="logo-text">浙江工业大学健行医院</span>
          </div>
          <div class="illustration">
            <h3>智慧医疗 · 守护健康</h3>
            <p>Distributed Medical Information System</p>
            <div class="big-icon">👨‍⚕️</div>
          </div>
        </div>
  
        <div class="login-right">
          <h2>欢迎登录</h2>
          <p class="sub-title">请选择您的身份进行登录</p>
  
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label>登录身份</label>
              <div class="role-selector">
                <div 
                  class="role-item" 
                  :class="{ active: form.role === 'user' }"
                  @click="form.role = 'user'"
                >
                  患者/用户
                </div>
                <div 
                  class="role-item" 
                  :class="{ active: form.role === 'doctor' }"
                  @click="form.role = 'doctor'"
                >
                  医生
                </div>
                <div 
                  class="role-item" 
                  :class="{ active: form.role === 'admin' }"
                  @click="form.role = 'admin'"
                >
                  管理员
                </div>
              </div>
            </div>
  
            <div class="form-group">
              <label>账号</label>
              <input type="text" v-model="form.username" placeholder="请输入账号/手机号" />
            </div>
  
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="form.password" placeholder="请输入密码" />
            </div>
  
            <button type="submit" class="btn-login">登 录</button>
          </form>
          
          <div class="login-footer">
            <span>忘记密码?</span>
            <span>注册新账号</span>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { loginApi } from '../api/auth';       // Login.vue 与 api 同级在 src 下
// 如果 Login.vue 嵌套更深，按实际层级写  ../../api/auth  ...
  
  const router = useRouter();
  const form = ref({
    role: 'user', // 默认选中用户
    username: '',
    password: ''
  });
  

  const handleLogin = async () => {
    if (!form.value.username || !form.value.password) {
      alert("请输入账号和密码");
      return;
    }

    try {
      const res = await loginApi({
        userPhone: form.value.username,
        userPassword: form.value.password,
        role: form.value.role
      });

      // 登录成功，后端返回 { code: 200, data: { token: '...' } }
      const { token } = res.data;
      // 将Token保存到localStorage，以便后续请求使用
      localStorage.setItem('hospital_token', token);

      // 如果是医生登录，获取医生信息并存储
      if (form.value.role === 'doctor') {
        try {
          const doctorRes = await fetch('http://localhost:8080/api/doctor/profile', {
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`
            }
          });
          if (doctorRes.ok) {
            const doctorData = await doctorRes.json();
            if (doctorData.code === 200) {
              const doctor = doctorData.data;
              localStorage.setItem('doctorName', doctor.doctorName || '医生');
              localStorage.setItem('doctorDept', doctor.departmentName || '未设置科室');
            }
          }
        } catch (error) {
          console.error('获取医生信息失败:', error);
          // 设置默认值
          localStorage.setItem('doctorName', '医生');
          localStorage.setItem('doctorDept', '未设置科室');
        }
      }

      // 根据角色跳转到不同页面
      if (form.value.role === 'user') {
        router.push('/user');
      } else if (form.value.role === 'doctor') {
        router.push('/doctor');
      } else if (form.value.role === 'admin') {
        router.push('/admin');
      }
    } catch (error) {
      // 登录失败，错误信息已由 src/utils/request.js 中的响应拦截器统一处理（alert）
      console.error('登录失败:', error);
    }
  };
  </script>
  
  <style scoped>
  .login-container {
    height: 100vh;
    width: 100vw;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .login-box {
    width: 900px;
    height: 550px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 20px 50px rgba(0,0,0,0.1);
    display: flex;
    overflow: hidden;
  }
  
  /* 左侧样式 */
  .login-left {
    flex: 1;
    background: linear-gradient(135deg, #0056b3, #004ea2);
    padding: 40px;
    display: flex;
    flex-direction: column;
    color: white;
    position: relative;
  }
  .logo-area { display: flex; align-items: center; gap: 10px; font-size: 1.2rem; font-weight: bold; }
  .illustration { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; }
  .illustration h3 { font-size: 2rem; margin-bottom: 10px; }
  .illustration p { opacity: 0.8; }
  .big-icon { font-size: 8rem; margin-top: 30px; opacity: 0.9; }
  
  /* 右侧样式 */
  .login-right {
    flex: 1;
    padding: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  .login-right h2 { color: #333; margin-bottom: 10px; font-size: 1.8rem; }
  .sub-title { color: #999; margin-bottom: 30px; font-size: 0.9rem; }
  
  .form-group { margin-bottom: 20px; }
  .form-group label { display: block; margin-bottom: 8px; color: #666; font-size: 0.9rem; }
  .form-group input {
    width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; outline: none; transition: 0.3s;
  }
  .form-group input:focus { border-color: #0056b3; }
  
  /* 角色选择器 */
  .role-selector {
    display: flex; background: #f0f2f5; border-radius: 8px; padding: 4px;
  }
  .role-item {
    flex: 1; text-align: center; padding: 8px; font-size: 0.9rem; color: #666; cursor: pointer; border-radius: 6px; transition: 0.3s;
  }
  .role-item.active { background: white; color: #0056b3; font-weight: bold; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
  
  .btn-login {
    width: 100%; background: #0056b3; color: white; padding: 12px; border: none; border-radius: 8px; font-size: 1rem; cursor: pointer; margin-top: 10px; transition: 0.3s;
  }
  .btn-login:hover { background: #004494; }
  
  .login-footer { margin-top: 20px; display: flex; justify-content: space-between; font-size: 0.85rem; color: #0056b3; cursor: pointer; }
  </style>