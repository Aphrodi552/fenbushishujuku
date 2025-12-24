<template>
  <div class="appointment-page">
    
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

    <div class="top-banner-section">
      <div class="banner-bg">
        <div class="banner-text"><h1>预约挂号</h1></div>
      </div>
      <div class="breadcrumb-strip">
        <div class="container">
          <span @click="router.push('/user')">网站首页</span> 
          <Icon icon="mdi:chevron-right" />
          <span>就诊指南</span>
          <Icon icon="mdi:chevron-right" />
          <span class="current">预约挂号</span>
        </div>
        <div class="strip-shape"></div>
      </div>
    </div>

    <main class="main-content">
      <div class="content-container">
        
        <div class="steps-bar">
          <div class="step-item" :class="{ active: currentStep >= 1, finished: currentStep > 1 }">
            <div class="step-num">1</div><span class="step-text">选择院区</span>
          </div>
          <div class="step-line" :class="{ active: currentStep > 1 }"></div>
          
          <div class="step-item" :class="{ active: currentStep >= 2, finished: currentStep > 2 }">
            <div class="step-num">2</div><span class="step-text">选择科室</span>
          </div>
          <div class="step-line" :class="{ active: currentStep > 2 }"></div>
          
          <div class="step-item" :class="{ active: currentStep >= 3, finished: currentStep > 3 }">
            <div class="step-num">3</div><span class="step-text">选择医生</span>
          </div>
          <div class="step-line" :class="{ active: currentStep > 3 }"></div>

          <div class="step-item" :class="{ active: currentStep >= 4, finished: currentStep > 4 }">
            <div class="step-num">4</div><span class="step-text">排班详情</span>
          </div>
          <div class="step-line" :class="{ active: currentStep > 4 }"></div>

          <div class="step-item" :class="{ active: currentStep >= 5, finished: currentStep > 5 }">
            <div class="step-num">5</div><span class="step-text">选择就诊人</span>
          </div>
          <div class="step-line" :class="{ active: currentStep > 5 }"></div>
          
          <div class="step-item" :class="{ active: currentStep >= 6 }">
            <div class="step-num">6</div><span class="step-text">确认预约</span>
          </div>
        </div>

        <div v-if="currentStep === 1" class="step-content fade-in">
          <h2 class="step-title">请选择就诊院区</h2>
          <div class="campus-grid">
            <div v-for="campus in campuses" :key="campus.id" class="campus-card" @click="selectCampus(campus)">
              <img :src="campus.img" alt="campus" class="campus-img">
              <div class="campus-info">
                <h3>{{ campus.name }}</h3>
                <p><Icon icon="mdi:map-marker" /> {{ campus.addr }}</p>
                <button class="btn-select">去挂号</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 2" class="step-content fade-in">
          <div class="step-header-row">
            <h2 class="step-title">当前选择：<span class="highlight">{{ bookingData.campusName }}</span></h2>
            <button class="btn-back" @click="currentStep = 1">重新选择院区</button>
          </div>
          <div class="dept-selector">
            <div class="dept-sidebar">
              <div 
                v-for="(cat, index) in deptCategories" :key="index"
                class="sidebar-item" :class="{ active: activeCategory === cat.name }"
                @click="activeCategory = cat.name"
              >
                {{ cat.name }}
              </div>
            </div>
            <div class="dept-main-list">
              <div class="grid-wrapper">
                <div v-for="dept in currentDepts" :key="dept" class="dept-item" @click="selectDept(dept)">
                  {{ dept }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 3" class="step-content fade-in">
          <div class="step-header-row">
            <div class="info-crumb">
              <span class="tag">{{ bookingData.campusName }}</span>
              <Icon icon="mdi:chevron-right" />
              <span class="tag">{{ bookingData.deptName }}</span>
            </div>
            <button class="btn-back" @click="currentStep = 2">返回上一步</button>
          </div>

          <div class="booking-tabs">
            <div class="b-tab" :class="{ active: bookingMode === 'doctor' }" @click="bookingMode = 'doctor'">按医生挂号</div>
            <div class="b-tab" :class="{ active: bookingMode === 'date' }" @click="bookingMode = 'date'">按日期挂号</div>
          </div>

          <div v-if="bookingMode === 'date'" class="date-calendar-strip">
            <div 
              v-for="(day, idx) in weekData" :key="idx" 
              class="day-box" :class="{ active: selectedDateIndex === idx }"
              @click="selectedDateIndex = idx"
            >
              <div class="week-day">{{ day.week }}</div>
              <div class="date-num">{{ day.date }}</div>
              <div class="status" :class="day.status === '有号' ? 'available' : 'full'">{{ day.status }}</div>
            </div>
          </div>

          <div class="doctor-list-wrapper">
            <div v-if="filteredDoctors.length === 0" class="empty-tip">当前日期暂无排班医生</div>
            <div v-for="doc in filteredDoctors" :key="doc.id" class="doctor-row">
              <div class="doc-left">
                <img :src="doc.photo" class="avatar" />
                <div class="doc-basic">
                  <div class="name-line">
                    <span class="name">{{ doc.name }}</span>
                    <span class="title">{{ doc.title }}</span>
                    <span class="badge" v-if="doc.isExpert">名医</span>
                  </div>
                  <div class="skill">擅长：{{ doc.skill }}</div>
                </div>
              </div>
              <div class="doc-right">
                <button class="btn-book" @click="goToDoctorDetail(doc)">预约挂号</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 4" class="step-content fade-in">
          <div class="step-header-row">
             <h2 class="step-title">医生排班详情</h2>
             <button class="btn-back" @click="currentStep = 3">返回列表</button>
          </div>

          <div class="doctor-profile-card">
            <div class="profile-left">
              <img :src="selectedDoctor.photo" class="profile-avatar">
              <div class="profile-info">
                <div class="profile-name">
                  {{ selectedDoctor.name }} <span class="profile-title">{{ selectedDoctor.title }}</span>
                </div>
                <div class="profile-dept">{{ bookingData.campusName }} | {{ bookingData.deptName }}</div>
                <div class="profile-tags">
                  <span class="tag-item">从业20年</span>
                  <span class="tag-item">好评率 99%</span>
                  <span class="tag-item">接诊量 5000+</span>
                </div>
                <div class="profile-desc">擅长：{{ selectedDoctor.skill }}</div>
              </div>
            </div>
            <div class="profile-right">
              <button class="btn-fav"><Icon icon="mdi:heart-outline" /> 关注医生</button>
            </div>
          </div>

          <div class="schedule-grid-container">
            <div class="grid-header-title"><Icon icon="mdi:calendar-clock" /> 选择就诊时间</div>
            <table class="schedule-table">
              <thead>
                <tr>
                  <th width="100">时段</th>
                  <th v-for="(day, i) in weekData" :key="i">
                    <div class="th-week">{{ day.week }}</div>
                    <div class="th-date">{{ day.date }}</div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="td-period">上午</td>
                  <td v-for="(day, i) in weekData" :key="'am-'+i" class="td-cell">
                    <div v-if="hasSlot(selectedDoctor, day.date, 'am')">
                      <button class="btn-slot available" @click="selectSlot(day, '上午')">
                        挂号 <span class="price">￥{{ selectedDoctor.price }}</span>
                      </button>
                    </div>
                    <div v-else class="empty-slot"></div>
                  </td>
                </tr>
                <tr>
                  <td class="td-period">下午</td>
                  <td v-for="(day, i) in weekData" :key="'pm-'+i" class="td-cell">
                    <div v-if="hasSlot(selectedDoctor, day.date, 'pm')">
                      <button class="btn-slot available" @click="selectSlot(day, '下午')">
                        挂号 <span class="price">￥{{ selectedDoctor.price }}</span>
                      </button>
                    </div>
                    <div v-else class="empty-slot"></div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="currentStep === 5" class="step-content fade-in">
          <div class="step-header-row">
             <h2 class="step-title">选择就诊人</h2>
             <button class="btn-back" @click="currentStep = 4">返回时间选择</button>
          </div>
          
          <div class="patient-selection-container">
            <div class="saved-patient-list">
              <div 
                v-for="p in patientList" 
                :key="p.id" 
                class="patient-card"
                :class="{ active: selectedPatientId === p.id }"
                @click="selectPatient(p.id)"
              >
                <div class="p-header">
                  <span class="p-name">{{ p.name }}</span>
                  <span class="p-tag">{{ p.relation }}</span>
                  <Icon icon="mdi:check-circle" class="check-icon" v-if="selectedPatientId === p.id" />
                </div>
                <div class="p-info">身份证：{{ maskIdCard(p.idCard) }}</div>
                <div class="p-info">手机号：{{ maskPhone(p.phone) }}</div>
              </div>

              <div class="add-patient-btn" @click="showAddForm = true" v-if="!showAddForm">
                <Icon icon="mdi:plus-circle-outline" /> 添加就诊人
              </div>
            </div>

            <div class="add-patient-form" v-if="showAddForm">
              <h3 class="form-title">添加新就诊人</h3>
              <div class="form-grid">
                <div class="form-group">
                  <label>姓名</label>
                  <input type="text" v-model="newPatient.name" placeholder="请输入真实姓名">
                </div>
                <div class="form-group">
                  <label>身份证号</label>
                  <input type="text" v-model="newPatient.idCard" placeholder="请输入身份证号">
                </div>
                <div class="form-group">
                  <label>手机号码</label>
                  <input type="text" v-model="newPatient.phone" placeholder="请输入手机号码">
                </div>
                <div class="form-group">
                  <label>出生日期</label>
                  <input type="date" v-model="newPatient.dob">
                </div>
                <div class="form-group">
                  <label>性别</label>
                  <select v-model="newPatient.gender">
                    <option value="男">男</option>
                    <option value="女">女</option>
                  </select>
                </div>
              </div>
              <div class="form-actions">
                <button class="btn-cancel-add" @click="showAddForm = false">取消</button>
                <button class="btn-save-add" @click="addNewPatient">保存并使用</button>
              </div>
            </div>

            <div class="action-footer" v-if="!showAddForm">
              <button class="btn-next-step" :disabled="!selectedPatientId" @click="goToConfirm">
                下一步：确认预约
              </button>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 6" class="step-content fade-in">
          <div class="confirm-card">
            <div class="card-header">确认挂号信息</div>
            <div class="card-body">
              <div class="confirm-row">
                <span class="label">预约医院：</span>
                <span class="val">{{ bookingData.campusName }}</span>
              </div>
              <div class="confirm-row">
                <span class="label">预约科室：</span>
                <span class="val">{{ bookingData.deptName }}</span>
              </div>
              <div class="confirm-row">
                <span class="label">预约医生：</span>
                <span class="val bold">{{ bookingData.doctorName }} ({{ bookingData.doctorTitle }})</span>
              </div>
              <div class="confirm-row">
                <span class="label">就诊时间：</span>
                <span class="val highlight">{{ bookingData.date }} ({{ bookingData.week }}) {{ bookingData.period }}</span>
              </div>
              <div class="confirm-row">
                <span class="label">挂号费用：</span>
                <span class="val price">￥{{ bookingData.price }}.00</span>
              </div>
              <div class="divider"></div>
              <div class="confirm-row">
                <span class="label">就诊人：</span>
                <span class="val">{{ currentPatient?.name }} ({{ maskPhone(currentPatient?.phone) }})</span>
              </div>
              <div class="confirm-row">
                <span class="label">身份证：</span>
                <span class="val">{{ maskIdCard(currentPatient?.idCard) }}</span>
              </div>
            </div>
            <div class="card-footer">
              <div class="agreement">
                <input type="checkbox" id="agree" checked>
                <label for="agree">我已阅读并同意《预约挂号须知》</label>
              </div>
              <div class="btn-group">
                <button class="btn-cancel" @click="currentStep = 5">返回修改</button>
                <button class="btn-confirm" @click="submitBooking">确定预约</button>
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';

const router = useRouter();
const currentStep = ref(1);
const bookingMode = ref('doctor'); 
const activeCategory = ref('内科');
const selectedDateIndex = ref(0);
const selectedDoctor = ref({}); 

// 暂存预约数据
const bookingData = ref({
  campusName: '',
  deptName: '',
  doctorName: '',
  doctorTitle: '',
  price: 0,
  date: '',
  week: '',
  period: ''
});

// --- 就诊人管理逻辑 ---
const selectedPatientId = ref(1); // 默认选中第一个
const showAddForm = ref(false);
const newPatient = ref({ name: '', idCard: '', phone: '', dob: '', gender: '男' });

// 模拟已保存的就诊人
const patientList = ref([
  { id: 1, name: '陆露露', relation: '本人', idCard: '330106199508201234', phone: '18866668888', gender: '女' },
  { id: 2, name: '张大爷', relation: '父亲', idCard: '330106195501015678', phone: '13900001111', gender: '男' }
]);

const currentPatient = computed(() => {
  return patientList.value.find(p => p.id === selectedPatientId.value);
});

// 掩码处理函数
const maskPhone = (str) => str ? str.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '';
const maskIdCard = (str) => str ? str.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2') : '';

// 选择就诊人
const selectPatient = (id) => {
  selectedPatientId.value = id;
};

// 添加新就诊人
const addNewPatient = () => {
  if(!newPatient.value.name || !newPatient.value.idCard || !newPatient.value.phone) {
    alert('请填写完整信息');
    return;
  }
  const newId = patientList.value.length + 1;
  const p = {
    id: newId,
    ...newPatient.value,
    relation: '其他'
  };
  patientList.value.push(p);
  selectedPatientId.value = newId; // 自动选中新建的
  showAddForm.value = false; // 关闭表单
  // 清空表单
  newPatient.value = { name: '', idCard: '', phone: '', dob: '', gender: '男' };
};

const goToConfirm = () => {
  currentStep.value = 6;
  window.scrollTo(0, 0);
};

// --- 原有逻辑 ---
const campuses = [
  { id: 'zhaohui', name: '浙江省人民医院朝晖院区', addr: '杭州市上塘路158号', img: 'https://images.unsplash.com/photo-1516549655169-df83a09295ba?q=80&w=600&auto=format&fit=crop' },
  { id: 'yuecheng', name: '浙江省人民医院越城院区', addr: '绍兴市越城区敬宾路299号', img: 'https://images.unsplash.com/photo-1586773860418-d37222d8fce3?q=80&w=400' },
];

const deptCategories = [
  { name: '内科', list: ['心血管内科', '呼吸内科', '消化内科', '神经内科', '肾脏病科'] },
  { name: '外科', list: ['普通外科', '肝胆胰外科', '骨科', '神经外科', '泌尿外科'] },
  { name: '妇产科', list: ['妇科', '产科'] }
];

const weekData = [
  { date: '12-24', week: '周三', status: '有号' },
  { date: '12-25', week: '周四', status: '满号' },
  { date: '12-26', week: '周五', status: '有号' },
  { date: '12-27', week: '周六', status: '有号' },
  { date: '12-28', week: '周日', status: '有号' },
  { date: '12-29', week: '周一', status: '有号' },
  { date: '12-30', week: '周二', status: '有号' },
];

const doctorList = [
  { id: 1, name: '孙学锐', title: '主任医师', isExpert: true, price: 50, skill: '发育迟缓、小儿咳嗽、小儿哮喘、小儿厌食症等。', photo: 'https://randomuser.me/api/portraits/women/44.jpg', schedule: { '12-24': ['am', 'pm'], '12-26': ['am'], '12-28': ['pm'] } },
  { id: 2, name: '潘文胜', title: '主任医师', isExpert: false, price: 30, skill: '食管、胃、肠、肝、胆、胰等消化病的诊治。', photo: 'https://randomuser.me/api/portraits/men/32.jpg', schedule: { '12-24': ['am'], '12-25': [], '12-26': ['pm'] } },
  { id: 3, name: '汪望月', title: '主任医师', isExpert: true, price: 30, skill: '消化科疾病的规范诊疗，如反流性食管炎。', photo: 'https://randomuser.me/api/portraits/men/85.jpg', schedule: { '12-25': ['am', 'pm'], '12-29': ['am'] } },
  { id: 4, name: '吴伟权', title: '副主任医师', isExpert: false, price: 30, skill: '擅长消化内镜下的各种诊断和治疗技术。', photo: 'https://randomuser.me/api/portraits/men/11.jpg', schedule: { '12-24': ['pm'], '12-27': ['am', 'pm'] } },
];

const currentDepts = computed(() => {
  const cat = deptCategories.find(c => c.name === activeCategory.value);
  return cat ? cat.list : [];
});

const filteredDoctors = computed(() => {
  if (bookingMode.value === 'doctor') {
    return doctorList;
  } else {
    const targetDate = weekData[selectedDateIndex.value].date;
    return doctorList.filter(doc => doc.schedule && doc.schedule[targetDate] && doc.schedule[targetDate].length > 0);
  }
});

const selectCampus = (campus) => { bookingData.value.campusName = campus.name; currentStep.value = 2; window.scrollTo(0, 0); };
const selectDept = (dept) => { bookingData.value.deptName = dept; currentStep.value = 3; window.scrollTo(0, 0); };
const goToDoctorDetail = (doc) => { selectedDoctor.value = doc; currentStep.value = 4; window.scrollTo(0, 0); };
const hasSlot = (doc, dateStr, period) => doc.schedule && doc.schedule[dateStr] && doc.schedule[dateStr].includes(period);

const selectSlot = (dayInfo, periodStr) => {
  bookingData.value.doctorName = selectedDoctor.value.name;
  bookingData.value.doctorTitle = selectedDoctor.value.title;
  bookingData.value.price = selectedDoctor.value.price;
  bookingData.value.date = dayInfo.date;
  bookingData.value.week = dayInfo.week;
  bookingData.value.period = periodStr;
  currentStep.value = 5; // 进入就诊人选择
  window.scrollTo(0, 0);
};

const submitBooking = () => {
  if (confirm('确定要提交预约吗？')) {
    alert('预约成功！请按时就诊。');
    router.push('/user');
  }
};
</script>

<style scoped>
/* 基础设置 */
.appointment-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; }
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* Header & Banner 复用之前的样式 */
.main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
.header-inner { width: 100%; max-width: 1200px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
.logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.logo-icon { font-size: 2.2rem; }
.logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
.logo-text small { font-size: 0.6rem; color: #666; }
.back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }

.top-banner-section { background: white; }
.banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url('https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
.banner-text h1 { color: white; font-size: 2.2rem; }
.breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; position: relative; padding-left: 10%; color: white; }
.breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; z-index: 2; }
.strip-shape { position: absolute; right: 0; top: 0; border-top: 50px solid #f0ad4e; border-left: 50px solid transparent; }

/* 主体容器 */
.main-content { padding: 40px 0; }
.content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; }

/* 步骤条 */
.steps-bar { display: flex; align-items: center; justify-content: center; margin-bottom: 40px; }
.step-item { display: flex; flex-direction: column; align-items: center; gap: 5px; position: relative; z-index: 2; }
.step-num { width: 40px; height: 40px; background: #ddd; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 1.2rem; transition: 0.3s; }
.step-text { font-size: 0.9rem; color: #999; font-weight: bold; }
.step-item.active .step-num { background: #2f80ed; }
.step-item.active .step-text { color: #2f80ed; }
.step-item.finished .step-num { background: #28a745; }
.step-line { width: 60px; height: 4px; background: #ddd; margin: -20px 10px 0 10px; transition: 0.3s; }
.step-line.active { background: #28a745; }

/* 院区 & 科室 & 医生列表 & 排班详情 复用之前的CSS (略微调整) */
.step-title { color: #333; margin-bottom: 25px; border-left: 5px solid #2f80ed; padding-left: 15px; }
.campus-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 30px; }
.campus-card { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.05); cursor: pointer; transition: 0.3s; display: flex; }
.campus-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.1); border: 1px solid #2f80ed; }
.campus-img { width: 200px; height: 160px; object-fit: cover; }
.campus-info { padding: 20px; flex: 1; display: flex; flex-direction: column; justify-content: center; }
.btn-select { align-self: flex-start; background: #2f80ed; color: white; border: none; padding: 8px 25px; border-radius: 20px; cursor: pointer; }

.step-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.highlight { color: #2f80ed; }
.btn-back { background: none; border: 1px solid #999; color: #666; padding: 5px 15px; border-radius: 4px; cursor: pointer; }
.dept-selector { display: flex; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); min-height: 500px; }
.dept-sidebar { width: 200px; background: #f7f9fc; border-right: 1px solid #eee; }
.sidebar-item { padding: 15px 20px; cursor: pointer; font-weight: bold; color: #555; transition: 0.2s; border-left: 4px solid transparent; }
.sidebar-item.active { background: white; color: #2f80ed; border-left-color: #2f80ed; }
.dept-main-list { flex: 1; padding: 30px; }
.grid-wrapper { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.dept-item { background: white; border: 1px solid #eee; padding: 15px; text-align: center; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.dept-item:hover { color: #2f80ed; border-color: #2f80ed; box-shadow: 0 5px 10px rgba(47, 128, 237, 0.1); }

.booking-tabs { display: flex; gap: 0; margin-bottom: 20px; border-bottom: 2px solid #ddd; }
.b-tab { padding: 15px 30px; font-size: 1.1rem; cursor: pointer; font-weight: bold; color: #666; position: relative; top: 2px; }
.b-tab.active { color: #2f80ed; border-bottom: 3px solid #2f80ed; }
.date-calendar-strip { display: flex; gap: 10px; margin-bottom: 30px; background: white; padding: 15px; border-radius: 8px; justify-content: space-between; }
.day-box { flex: 1; text-align: center; padding: 10px; border: 1px solid #eee; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.day-box.active { background: #2f80ed; color: white; border-color: #2f80ed; }
.status.available { color: #28a745; }
.day-box.active .status.available { color: #aefbc0; }
.doctor-list-wrapper { background: white; border-radius: 8px; padding: 10px; min-height: 200px; }
.empty-tip { text-align: center; padding: 40px; color: #999; }
.doctor-row { display: flex; justify-content: space-between; border-bottom: 1px solid #f0f0f0; padding: 25px; transition: 0.2s; }
.doc-left { display: flex; gap: 20px; }
.avatar { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; }
.name { font-size: 1.3rem; font-weight: bold; color: #333; }
.title { color: #666; font-size: 0.9rem; margin-left: 10px; }
.badge { background: #f0ad4e; color: white; padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; margin-left: 10px; }
.skill { color: #888; font-size: 0.9rem; margin-top: 5px; max-width: 500px; }
.btn-book { background: #2f80ed; color: white; border: none; padding: 8px 25px; border-radius: 20px; cursor: pointer; }

.doctor-profile-card { background: white; padding: 30px; border-radius: 8px; display: flex; justify-content: space-between; box-shadow: 0 4px 15px rgba(0,0,0,0.05); margin-bottom: 30px; }
.profile-left { display: flex; gap: 25px; }
.profile-avatar { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; border: 3px solid #eee; }
.profile-name { font-size: 1.6rem; font-weight: bold; color: #333; margin-bottom: 5px; }
.profile-title { font-size: 1rem; color: #666; font-weight: normal; margin-left: 10px; }
.btn-fav { background: white; border: 1px solid #ddd; padding: 5px 15px; border-radius: 20px; cursor: pointer; display: flex; align-items: center; gap: 5px; color: #666; }
.schedule-grid-container { background: white; border-radius: 8px; padding: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.schedule-table { width: 100%; border-collapse: collapse; text-align: center; }
.schedule-table th { background: #f8f9fa; padding: 15px; border: 1px solid #eee; }
.schedule-table td { border: 1px solid #eee; height: 80px; vertical-align: middle; }
.btn-slot { display: block; width: 80%; margin: 0 auto; background: #28a745; color: white; border: none; padding: 8px 0; border-radius: 6px; cursor: pointer; font-size: 0.9rem; transition: 0.2s; }
.btn-slot:hover { background: #218838; transform: translateY(-2px); }

/* ★★★ Step 5: 就诊人选择 样式 ★★★ */
.patient-selection-container { background: white; border-radius: 8px; padding: 30px; min-height: 400px; }
.saved-patient-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 30px; }
.patient-card { 
  border: 2px solid #eee; border-radius: 8px; padding: 20px; cursor: pointer; transition: 0.2s; position: relative;
  background: #fafafa;
}
.patient-card:hover { border-color: #2f80ed; box-shadow: 0 5px 15px rgba(47, 128, 237, 0.1); }
.patient-card.active { border-color: #2f80ed; background: #f0f7ff; }
.p-header { display: flex; align-items: center; margin-bottom: 10px; }
.p-name { font-size: 1.2rem; font-weight: bold; color: #333; margin-right: 10px; }
.p-tag { background: #e0e0e0; color: #666; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
.check-icon { margin-left: auto; color: #2f80ed; font-size: 1.4rem; }
.p-info { color: #666; font-size: 0.9rem; margin-bottom: 5px; }

.add-patient-btn { 
  border: 2px dashed #ccc; border-radius: 8px; display: flex; align-items: center; justify-content: center; 
  cursor: pointer; color: #999; font-size: 1rem; gap: 8px; min-height: 120px;
}
.add-patient-btn:hover { border-color: #2f80ed; color: #2f80ed; background: #f9fcff; }

.add-patient-form { background: #f9f9f9; padding: 30px; border-radius: 8px; margin-top: 20px; border: 1px solid #eee; }
.form-title { margin-top: 0; margin-bottom: 20px; color: #333; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 0.9rem; color: #666; font-weight: bold; }
.form-group input, .form-group select { 
  padding: 10px; border: 1px solid #ddd; border-radius: 4px; outline: none; transition: 0.2s; 
}
.form-group input:focus { border-color: #2f80ed; }
.form-actions { margin-top: 20px; display: flex; gap: 15px; justify-content: flex-end; }
.btn-save-add { background: #2f80ed; color: white; border: none; padding: 10px 30px; border-radius: 4px; cursor: pointer; }
.btn-cancel-add { background: white; border: 1px solid #ccc; padding: 10px 20px; border-radius: 4px; cursor: pointer; }

.action-footer { margin-top: 40px; text-align: center; border-top: 1px solid #eee; padding-top: 30px; }
.btn-next-step { 
  background: #2f80ed; color: white; border: none; padding: 12px 60px; border-radius: 30px; 
  font-size: 1.1rem; font-weight: bold; cursor: pointer; box-shadow: 0 5px 15px rgba(47, 128, 237, 0.3);
}
.btn-next-step:disabled { background: #ccc; cursor: not-allowed; box-shadow: none; }

/* Step 6: 确认 */
.confirm-card { background: white; max-width: 600px; margin: 0 auto; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
.card-header { background: #2f80ed; color: white; text-align: center; padding: 15px; font-size: 1.2rem; font-weight: bold; }
.card-body { padding: 30px; }
.confirm-row { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 1rem; color: #555; }
.confirm-row .val { color: #333; font-weight: 500; }
.confirm-row .val.highlight { color: #2f80ed; font-weight: bold; }
.confirm-row .val.price { color: #ff4d4f; font-size: 1.3rem; }
.divider { border-bottom: 1px dashed #ddd; margin: 20px 0; }
.card-footer { background: #f9f9f9; padding: 20px; }
.agreement { margin-bottom: 20px; text-align: center; }
.btn-group { display: flex; gap: 20px; justify-content: center; }
.btn-cancel { background: white; border: 1px solid #ccc; padding: 10px 30px; border-radius: 4px; cursor: pointer; }
.btn-confirm { background: #2f80ed; color: white; border: none; padding: 10px 50px; border-radius: 4px; font-weight: bold; cursor: pointer; }

.app-footer { background: #1a3a6e; color: rgba(255,255,255,0.6); text-align: center; padding: 20px; margin-top: 50px; }
</style>