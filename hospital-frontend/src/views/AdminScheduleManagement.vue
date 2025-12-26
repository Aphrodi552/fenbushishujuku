<!-- hospital-frontend/src/views/AdminScheduleManagement.vue -->
<template>
  <div class="schedule-page">
    <h2 class="page-title">排班管理</h2>

    <!-- 查询区：卡片风格 -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="field">
          <label>院区</label>
          <select class="select" v-model="query.hospitalId" @change="onHospitalChange">
            <option value="">请选择</option>
            <option v-for="h in hospitals" :key="h.hospitalId" :value="h.hospitalId">
              {{ h.hospitalName || h.hospitalId }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>科室</label>
          <select class="select" v-model="query.departmentId" @change="onDepartmentChange" :disabled="!query.hospitalId">
            <option value="">全部</option>
            <option v-for="d in departments" :key="d.departmentId" :value="d.departmentId">
              {{ d.departmentName }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>医生</label>
          <select class="select" v-model="query.doctorId" :disabled="!query.hospitalId">
            <option value="">全部</option>
            <option v-for="doc in doctors" :key="doc.doctorId" :value="doc.doctorId">
              {{ doc.doctorName }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>日期起</label>
          <input class="input" type="date" v-model="query.workDateFrom" :disabled="!query.hospitalId" />
        </div>

        <div class="field">
          <label>日期止</label>
          <input class="input" type="date" v-model="query.workDateTo" :disabled="!query.hospitalId" />
        </div>

        <div class="field">
          <label>时段</label>
          <select class="select" v-model="query.timeSlot" :disabled="!query.hospitalId">
            <option value="">全部</option>
            <option v-for="t in timeSlots" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
      </div>

      <div class="toolbar-right">
        <button class="btn btn-primary" @click="fetchPage" :disabled="!query.hospitalId">查询</button>
        <button class="btn btn-ghost" @click="reset">重置</button>
        <button class="btn btn-primary" @click="openCreate" :disabled="!query.hospitalId">新增排班</button>
      </div>
    </div>

    <div v-if="!query.hospitalId" class="foot-hint">
      提示：排班表按院区（hospitalId）分片，查询/新增/修改请先选择院区。
    </div>

    <!-- 列表区：卡片风格 -->
    <div class="table-card">
      <div class="section-header">
        <div>
          <h3>排班列表</h3>
          <div class="hint">
            当前院区：<span class="mono">{{ currentHospitalName }}</span>
            <span class="sep">|</span>
            共 <span class="mono">{{ page.total }}</span> 条
          </div>
        </div>
      </div>

      <table class="admin-table">
        <thead>
        <tr>
          <th>日期</th>
          <th>时段</th>
          <th>院区</th>
          <th>科室</th>
          <th>医生</th>
          <th>总号源</th>
          <th>已预约</th>
          <th>剩余</th>
          <th style="width: 170px;">操作</th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="row in page.records" :key="row.scheduleId">
          <td>{{ row.workDate }}</td>
          <td>{{ row.timeSlot }}</td>
          <td>{{ row.hospitalName || row.hospitalId }}</td>
          <td>{{ row.departmentName || '-' }}</td>
          <td>{{ row.doctorName || row.doctorId }}</td>
          <td>{{ row.totalQuota }}</td>
          <td>{{ row.bookedCount }}</td>
          <td>
              <span
                  class="badge"
                  :class="row.remainingQuota > 0 ? 'badge-green' : 'badge-red'"
              >
                {{ row.remainingQuota }}
              </span>
          </td>
          <td>
            <div class="actions">
              <button class="btn-mini" @click="openEdit(row)">编辑</button>
              <button class="btn-mini danger" @click="onDelete(row)">删除</button>
            </div>
          </td>
        </tr>

        <tr v-if="page.records.length === 0">
          <td colspan="9" class="empty-td">
            <div class="empty-state">
              <div class="empty-icon">□</div>
              <div class="empty-title">暂无数据</div>
              <div class="empty-sub">请先选择院区并点击查询</div>
            </div>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div style="margin-top: 12px; display:flex; align-items:center; gap:10px;">
        <button class="btn btn-ghost" @click="prevPage" :disabled="query.page <= 1">上一页</button>
        <span class="muted">第 <span class="mono">{{ query.page }}</span> 页 / 共 <span class="mono">{{ totalPages }}</span> 页</span>
        <button class="btn btn-ghost" @click="nextPage" :disabled="query.page >= totalPages">下一页</button>

        <span class="muted" style="margin-left: 12px;">每页</span>
        <select class="select" style="width: 90px;" v-model.number="query.size" @change="onSizeChange">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <span class="muted">条</span>
      </div>
    </div>

    <!-- 新增/编辑：同样用卡片，不用弹窗 -->
    <div v-if="formVisible" class="table-card">
      <div class="section-header">
        <div>
          <h3>{{ formMode === 'create' ? '新增排班' : '编辑排班' }}</h3>
          <div class="hint">
            院区：<span class="mono">{{ currentHospitalName }}</span>
            <span class="sep">|</span>
            <span class="warn">编辑/新增均不允许改院区（分片键不可更新）</span>
          </div>
        </div>
      </div>

      <div class="toolbar-left">
        <div class="field">
          <label>医生</label>
          <select class="select" v-model="form.doctorId">
            <option value="">请选择</option>
            <option v-for="doc in doctors" :key="doc.doctorId" :value="doc.doctorId">
              {{ doc.doctorName }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>日期</label>
          <input class="input" type="date" v-model="form.workDate" />
        </div>

        <div class="field">
          <label>时段</label>
          <select class="select" v-model="form.timeSlot">
            <option value="">请选择</option>
            <option v-for="t in timeSlots" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>

        <div class="field">
          <label>总号源</label>
          <input class="input" type="number" v-model.number="form.totalQuota" min="0" />
        </div>

        <div class="field">
          <label>&nbsp;</label>
          <div style="display:flex; gap:10px;">
            <button class="btn btn-primary" @click="submitForm">
              {{ formMode === 'create' ? '创建' : '保存' }}
            </button>
            <button class="btn btn-ghost" @click="closeForm">取消</button>
          </div>
        </div>
      </div>

      <div v-if="formMode === 'edit'" class="foot-hint">
        提示：若该排班已有预约，后端会校验“总号源不得小于已预约数”。
      </div>
    </div>
    <!-- 弹窗：新增/编辑排班 -->
    <div v-if="formVisible" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <h3 class="modal-title">{{ formMode === 'create' ? '新增排班' : '编辑排班' }}</h3>
            <div class="hint">
              院区：<span class="mono">{{ currentHospitalName }}</span>
              <span class="sep">|</span>
              <span class="warn">院区不可修改（分片键不可更新）</span>
            </div>
          </div>
          <button class="modal-close" @click="closeForm">×</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <div class="field">
              <label>医生</label>
              <select class="select" v-model="form.doctorId">
                <option value="">请选择</option>
                <option v-for="doc in doctors" :key="doc.doctorId" :value="doc.doctorId">
                  {{ doc.doctorName }}
                </option>
              </select>
            </div>

            <div class="field">
              <label>日期</label>
              <input class="input" type="date" v-model="form.workDate" />
            </div>

            <div class="field">
              <label>时段</label>
              <select class="select" v-model="form.timeSlot">
                <option value="">请选择</option>
                <option v-for="t in timeSlots" :key="t" :value="t">{{ t }}</option>
              </select>
            </div>

            <div class="field">
              <label>总号源</label>
              <input class="input" type="number" v-model.number="form.totalQuota" min="0" />
            </div>
          </div>

          <div v-if="formMode === 'edit'" class="foot-hint" style="margin-top: 10px;">
            提示：若该排班已有预约，后端会校验“总号源不得小于已预约数”。
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitForm">
            {{ formMode === 'create' ? '创建' : '保存' }}
          </button>
          <button class="btn btn-ghost" @click="closeForm">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import {
  listHospitals,
  listDepartments,
  listDoctors,
  pageSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule
} from '../api/adminSchedule'

const hospitals = ref([])
const departments = ref([])
const doctors = ref([])

const timeSlots = ref([
  '08:00-10:00',
  '13:30-17:00',
  '19:00-21:00'
])


const query = reactive({
  hospitalId: '',
  departmentId: '',
  doctorId: '',
  workDateFrom: '',
  workDateTo: '',
  timeSlot: '',
  page: 1,
  size: 10
})

const page = reactive({
  records: [],
  total: 0
})

const totalPages = computed(() => {
  const t = Number(page.total || 0)
  const s = Number(query.size || 10)
  return Math.max(1, Math.ceil(t / s))
})

const currentHospitalName = computed(() => {
  const h = hospitals.value.find(x => String(x.hospitalId) === String(query.hospitalId))
  return h ? (h.hospitalName || h.hospitalId) : query.hospitalId
})

const formVisible = ref(false)
const formMode = ref('create') // create | edit
const editingScheduleId = ref('')

const form = reactive({
  doctorId: '',
  workDate: '',
  timeSlot: '',
  totalQuota: 0
})

onMounted(async () => {
  const res = await listHospitals()
  hospitals.value = res.data || []
})

async function onHospitalChange() {
  query.departmentId = ''
  query.doctorId = ''
  departments.value = []
  doctors.value = []
  page.records = []
  page.total = 0
  query.page = 1

  if (!query.hospitalId) return

  await loadDepartments()
  await loadDoctors()
  await fetchPage()
}

async function onDepartmentChange() {
  query.doctorId = ''
  await loadDoctors()
  query.page = 1
  await fetchPage()
}

async function loadDepartments() {
  const res = await listDepartments({ hospitalId: query.hospitalId })
  departments.value = res.data || []
}

async function loadDoctors() {
  const res = await listDoctors({
    hospitalId: query.hospitalId,
    departmentId: query.departmentId || ''
  })
  doctors.value = res.data || []
}

async function fetchPage() {
  if (!query.hospitalId) return

  const res = await pageSchedules({
    ...query
  })
  const p = res.data || {}
  page.records = p.records || []
  page.total = p.total || 0
}




function reset() {
  query.departmentId = ''
  query.doctorId = ''
  query.workDateFrom = ''
  query.workDateTo = ''
  query.timeSlot = ''
  query.page = 1
  query.size = 10
  if (query.hospitalId) {
    fetchPage()
  } else {
    page.records = []
    page.total = 0
  }
}

function onSizeChange() {
  query.page = 1
  if (query.hospitalId) fetchPage()
}

function prevPage() {
  if (query.page <= 1) return
  query.page--
  fetchPage()
}

function nextPage() {
  if (query.page >= totalPages.value) return
  query.page++
  fetchPage()
}

function openCreate() {
  formMode.value = 'create'
  editingScheduleId.value = ''

  form.doctorId = ''
  form.workDate = ''
  form.timeSlot = ''
  form.totalQuota = 0

  formVisible.value = true
  document.body.style.overflow = 'hidden'
}


function openEdit(row) {
  formMode.value = 'edit'
  editingScheduleId.value = row.scheduleId

  form.doctorId = row.doctorId
  form.workDate = row.workDate
  form.timeSlot = row.timeSlot
  form.totalQuota = row.totalQuota

  formVisible.value = true
  document.body.style.overflow = 'hidden'
}

function closeForm() {
  formVisible.value = false
  document.body.style.overflow = ''
}


async function submitForm() {
  if (!query.hospitalId) return alert('请先选择院区')
  if (!form.doctorId) return alert('请选择医生')
  if (!form.workDate) return alert('请选择日期')
  if (!form.timeSlot) return alert('请选择时段')
  if (form.totalQuota == null || form.totalQuota < 0) return alert('总号源必须为非负数')

  if (formMode.value === 'create') {
    await createSchedule({
      hospitalId: query.hospitalId,
      doctorId: form.doctorId,
      workDate: form.workDate,
      timeSlot: form.timeSlot,
      totalQuota: form.totalQuota
    })
    alert('创建成功')
  } else {
    await updateSchedule(editingScheduleId.value, {
      doctorId: form.doctorId,
      workDate: form.workDate,
      timeSlot: form.timeSlot,
      totalQuota: form.totalQuota
    })
    alert('保存成功')
  }

  formVisible.value = false
  await fetchPage()
}

async function onDelete(row) {
  const ok = window.confirm('确认删除该排班吗？')
  if (!ok) return
  await deleteSchedule(row.scheduleId)
  alert('删除成功')
  await fetchPage()
}
 </script>


 <style scoped>
.schedule-page { display: flex; flex-direction: column; gap: 14px; }

/* 卡片 */
.toolbar-card,
.table-card{
  background: white;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

/* 工具条 */
.toolbar-card{
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
}
.toolbar-left{
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: flex-end;
}
.toolbar-right{
  display: flex;
  gap: 10px;
}

.field{ display:flex; flex-direction:column; gap:6px; min-width: 220px; }
.field label{ font-size:0.8rem; color:#64748b; }

.input,.select{
  height:36px;
  border:1px solid #e2e8f0;
  border-radius:8px;
  padding:0 12px;
  outline:none;
  color:#0f172a;
  background:#fff;
}
.input:focus,.select:focus{
  border-color:#3b82f6;
  box-shadow:0 0 0 3px rgba(59,130,246,0.15);
}

.btn{
  height:36px;
  border-radius:8px;
  border:none;
  cursor:pointer;
  padding:0 14px;
  display:inline-flex;
  align-items:center;
  gap:6px;
  font-weight:700;
  font-size:0.9rem;
}
.btn-primary{ background:#3b82f6; color:white; }
.btn-ghost{ background:#f1f5f9; color:#334155; }
.btn:disabled{ opacity:.6; cursor:not-allowed; }

.btn-mini{
  height:30px;
  padding:0 10px;
  border-radius:6px;
  background:#f1f5f9;
  color:#334155;
  font-weight:700;
  font-size:0.82rem;
  border:none;
  cursor:pointer;
  display:inline-flex;
  align-items:center;
  gap:6px;
}
.btn-mini.danger{ background:#fee2e2; color:#b91c1c; }

/* 批量条 */
.batch-bar{
  background:#0f172a;
  color:white;
  border-radius:12px;
  padding:12px 14px;
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap:12px;
  box-shadow:0 6px 16px rgba(15,23,42,0.20);
}
.batch-left{ display:flex; align-items:center; gap:10px; }
.batch-chip{
  background:rgba(255,255,255,0.12);
  padding:6px 10px;
  border-radius:999px;
  font-size:0.9rem;
}
.btn-sm{ height:32px; padding:0 12px; font-size:0.86rem; }
.btn-danger{ background:#ef4444; color:white; }

/* 表头 */
.section-header{
  display:flex;
  justify-content:space-between;
  align-items:flex-start;
  margin-bottom:14px;
}
.section-header h3{ margin:0; font-size:1.05rem; color:#1e293b; }
.hint{ font-size:0.85rem; color:#64748b; }
.sub{ margin-top:6px; color:#64748b; font-size:0.88rem; }
.sep{ margin:0 8px; color:#cbd5e1; }
.muted{ color:#64748b; }

/* 表格 */
.admin-table{ width:100%; border-collapse:collapse; }
.admin-table th{ text-align:left; padding:14px; background:#f8fafc; color:#64748b; font-size:0.85rem; font-weight:800; }
.admin-table td{ padding:14px; border-bottom:1px solid #f1f5f9; color:#334155; font-size:0.9rem; vertical-align:middle; }

.actions{ display:flex; gap:8px; flex-wrap:wrap; }
.mono{
  font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,"Liberation Mono","Courier New",monospace;
  font-size:0.85rem;
  color:#0f172a;
}

/* 徽标 */
.badge{
  display:inline-flex;
  align-items:center;
  padding:4px 10px;
  border-radius:999px;
  font-size:0.78rem;
  font-weight:900;
}
.badge-green{ background:#dcfce7; color:#166534; }
.badge-red{ background:#fee2e2; color:#b91c1c; }
.warn{ color:#b45309; font-weight:900; }

/* 空态 */
.empty-td{ padding:30px !important; }
.empty-state{ text-align:center; color:#94a3b8; }
.empty-icon{ font-size:3rem; color:#cbd5e1; }
.empty-title{ font-weight:900; margin-top:8px; color:#64748b; }
.empty-sub{ margin-top:4px; font-size:0.9rem; }

.foot-hint{ font-size:0.85rem; color:#64748b; padding:10px 2px 0; }

/* 错误条 */
.error-bar{
  display:flex;
  align-items:center;
  gap:8px;
  background:#fff1f2;
  color:#9f1239;
  border:1px solid #fecdd3;
  padding:10px 12px;
  border-radius:10px;
  margin:10px 0;
}

/* 弹窗 */
.modal-mask{
  position:fixed;
  inset:0;
  background:rgba(15,23,42,0.45);
  display:flex;
  align-items:center;
  justify-content:center;
  padding:16px;
  z-index:99;
}
.modal{
  width:760px;
  max-width:100%;
  background:white;
  border-radius:14px;
  box-shadow:0 10px 30px rgba(0,0,0,0.20);
  overflow:hidden;
}
.modal-header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding:14px 16px;
  border-bottom:1px solid #e2e8f0;
}
.modal-title{
  display:inline-flex;
  align-items:center;
  gap:8px;
  font-weight:900;
  color:#0f172a;
}
.icon-btn{
  border:none;
  background:transparent;
  cursor:pointer;
  font-size:1.1rem;
  color:#64748b;
}
.modal-body{ padding:16px; }
.modal-footer{
  padding:14px 16px;
  border-top:1px solid #e2e8f0;
  display:flex;
  justify-content:flex-end;
  gap:10px;
}
.form-grid{ display:grid; grid-template-columns:1fr 1fr; gap:14px; }
.field.full{ grid-column:1 / -1; }
.textarea{
  border:1px solid #e2e8f0;
  border-radius:10px;
  padding:10px 12px;
  outline:none;
  color:#0f172a;
  background:#fff;
  resize:vertical;
}
.textarea:focus{ border-color:#3b82f6; box-shadow:0 0 0 3px rgba(59,130,246,0.15); }

.info-box{
  background:#f8fafc;
  border:1px solid #e2e8f0;
  border-radius:12px;
  padding:12px 12px;
  margin-bottom:12px;
  color:#334155;
}
.small{ font-size:0.85rem; margin-top:6px; }

.chips{ display:flex; flex-wrap:wrap; gap:10px; }
.chip{
  display:inline-flex;
  align-items:center;
  gap:8px;
  background:#f1f5f9;
  border:1px solid #e2e8f0;
  border-radius:999px;
  padding:8px 12px;
  color:#334155;
  font-weight:700;
  font-size:0.9rem;
}
/* ===== Modal（弹窗） ===== */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
  z-index: 9999;
}

.modal-card {
  width: min(760px, 96vw);
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eee;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.modal-close {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 22px;
  cursor: pointer;
  padding: 2px 6px;
  color: #666;
}

.modal-close:hover {
  color: #111;
}

.modal-body {
  padding: 14px 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 14px;
}

/* 小屏幕自动变单列 */
@media (max-width: 680px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

</style>
