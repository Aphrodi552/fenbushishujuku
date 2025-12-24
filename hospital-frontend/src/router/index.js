import { createRouter, createWebHistory } from 'vue-router'

// 引入刚才创建的页面组件
import Login from '../views/Login.vue'
import UserHome from '../views/UserHome.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import DoctorDashboard from '../views/DoctorDashboard.vue'
import HospitalIntro from '../views/HospitalIntro.vue'
import CampusDetail from '../views/CampusDetail.vue'
import OutpatientSchedule from '../views/OutpatientSchedule.vue'
import SpecialistList from '../views/SpecialistList.vue'
import DepartmentNavigation from '../views/DepartmentNavigation.vue'
import DepartmentDetail from '../views/DepartmentDetail.vue'
import AppointmentRegister from '../views/AppointmentRegister.vue'
import ContactUs from '../views/ContactUs.vue'
import MyPatients from '../views/MyPatients.vue'
import MyProfile from '../views/MyProfile.vue'
import MyReports from '../views/MyReports.vue'
import VisitRecords from '../views/VisitRecords.vue'

const routes = [
  {
    path: '/',
    redirect: '/login' // 默认一进来就跳到登录页
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/user',
    name: 'UserHome',
    component: UserHome
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard
  },
  {
    path: '/doctor',
    name: 'DoctorDashboard',
    component: DoctorDashboard
  },
  // 2. 添加这条新路由
  {
    path: '/intro',         // 浏览器地址栏会显示这个
    name: 'HospitalIntro',
    component: HospitalIntro // 对应显示这个组件
  },
  // 新增的动态路由
  {
    path: '/campus/:id', 
    name: 'CampusDetail',
    component: CampusDetail // 这里使用了 CampusDetail，所以上面必须 import
  },
  // 2. 添加排班路由
  {
    path: '/schedule',
    name: 'OutpatientSchedule',
    component: OutpatientSchedule
  },
  {
    path: '/specialist',
    name: 'SpecialistList',
    component: SpecialistList
  },
  {
    path: '/department',
    name: 'DepartmentNavigation',
    component: DepartmentNavigation
  },
  {
    path: '/department/:id',
    name: 'DepartmentDetail',
    component: DepartmentDetail
  },
  {
    path: '/appointment',
    name: 'AppointmentRegister',
    component: AppointmentRegister
  },
  {
    path: '/contact',
    name: 'ContactUs',
    component: ContactUs
  },
  {
    path: '/patients',
    name: 'MyPatients',
    component: MyPatients
  },
  {
    path: '/profile',
    name: 'MyProfile',
    component: MyProfile
  },
  {
    path: '/reports',
    name: 'MyReports',
    component: MyReports
  },
  {
    path: '/visit-records',
    name: 'VisitRecords',
    component: VisitRecords
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router