<template>
  <div class="users-page">
    <!-- 顶部工具条（保持结构一致） -->
    <div class="toolbar-card">
      <div class="toolbar-left">
      </div>

      <div class="toolbar-right">
        <button class="btn btn-primary" @click="openCreate">
          <Icon icon="mdi:account-plus-outline" />
          新增医生
        </button>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="field">
          <label>院区ID（hospitalId）</label>
          <input
              v-model="filters.hospitalId"
              class="input"
              placeholder="如：1 / 2"
              @keyup.enter="loadDoctors(1)"
          />
        </div>

        <div class="field">
          <label>关键词</label>
          <input
              v-model="filters.keyword"
              class="input"
              placeholder="医生姓名 / doctorId / userId / 电话"
              @keyup.enter="loadDoctors(1)"
          />
        </div>

        <div class="field">
          <label>
            科室
            <span v-if="filterDeptLoading" style="color:#64748b;font-weight:600;">（加载中…）</span>
          </label>
          <select
              v-model="filters.departmentId"
              class="select"
              :disabled="!filters.hospitalId || filterDeptLoading"
          >
            <option value="">
              {{ filters.hospitalId ? '全部科室' : '请先填写院区ID' }}
            </option>
            <option
                v-for="dep in filterDepartments"
                :key="dep.departmentId"
                :value="dep.departmentId"
            >
              {{ dep.departmentName }}（{{ dep.departmentId }}）
            </option>
          </select>
        </div>

        <div class="actions">
          <button class="btn btn-outline" @click="loadDoctors(1)">
            <Icon icon="mdi:magnify" />
            查询
          </button>
          <button class="btn btn-ghost" @click="resetFilters">
            <Icon icon="mdi:refresh" />
            重置
          </button>
        </div>
      </div>

      <div v-if="error" class="error-banner">
        <Icon icon="mdi:alert-circle-outline" />
        <span>{{ error }}</span>
      </div>
    </div>

    <!-- 表格区 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          医生列表
          <span class="count">（共 {{ pager.total }} 条）</span>
        </div>
        <div class="table-right">
          <button class="btn btn-ghost" @click="loadDoctors(pager.page)">
            <Icon icon="mdi:reload" />
            刷新
          </button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="admin-table">
          <thead>
          <tr>
            <th>doctorId</th>
            <th>userId</th>
            <th>院区ID</th>
            <th>科室ID</th>
            <th>医生姓名</th>
            <th>性别</th>
            <th>职称</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th>用户手机号</th>
            <th>创建时间</th>
            <th style="width: 160px;">操作</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="loading">
            <td colspan="12" class="empty">加载中...</td>
          </tr>

          <tr v-else-if="rows.length === 0">
            <td colspan="12" class="empty">暂无数据</td>
          </tr>

          <tr v-else v-for="r in rows" :key="r.doctorId">
            <td class="mono">{{ r.doctorId }}</td>
            <td class="mono">{{ r.userId }}</td>
            <td class="mono">{{ r.hospitalId }}</td>
            <td class="mono">{{ r.departmentId }}</td>
            <td>{{ r.doctorName }}</td>
            <td>{{ r.doctorGender || '-' }}</td>
            <td>{{ r.title || '-' }}</td>
            <td class="mono">{{ r.doctorPhone || '-' }}</td>
            <td class="mono">{{ r.doctorEmail || '-' }}</td>
            <td class="mono">{{ r.userPhone || '-' }}</td>
            <td class="mono">{{ formatTime(r.createdAt) }}</td>
            <td>
              <div class="row-actions">
                <button class="btn btn-small" @click="openEdit(r)">
                  <Icon icon="mdi:pencil-outline" />
                  修改
                </button>
                <button class="btn btn-small danger" @click="confirmDelete(r)">
                  <Icon icon="mdi:trash-can-outline" />
                  删除
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pager">
        <div class="pager-left">
          <span class="muted">第</span>
          <span class="mono">{{ pager.page }}</span>
          <span class="muted">/</span>
          <span class="mono">{{ totalPages }}</span>
          <span class="muted">页</span>
        </div>

        <div class="pager-right">
          <button class="btn btn-small" :disabled="pager.page <= 1" @click="loadDoctors(pager.page - 1)">
            上一页
          </button>
          <button class="btn btn-small" :disabled="pager.page >= totalPages" @click="loadDoctors(pager.page + 1)">
            下一页
          </button>

          <select v-model.number="pager.pageSize" class="select small" @change="loadDoctors(1)">
            <option :value="10">10/页</option>
            <option :value="20">20/页</option>
            <option :value="50">50/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 弹窗：新增/编辑 -->
    <div v-if="modal.visible" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            {{ modal.mode === 'create' ? '新增医生' : '修改医生信息' }}
          </div>
          <button class="icon-btn" @click="closeModal">
            <Icon icon="mdi:close" />
          </button>
        </div>

        <div class="modal-body">
          <div v-if="modalError" class="error-banner">
            <Icon icon="mdi:alert-circle-outline" />
            <span>{{ modalError }}</span>
          </div>

          <div class="form-grid">
            <!-- doctor_id：编辑时只展示不可改 -->
            <div class="field">
              <label>医生ID（doctorId）</label>
              <input v-model="form.doctor_id" class="input" disabled placeholder="系统生成/不可修改" />
            </div>

            <!-- user_id：编辑时不可改；新增时后端会创建 user，前端只填 userPhone/userPassword -->
            <div class="field">
              <label>用户ID（userId）</label>
              <input v-model="form.user_id" class="input" disabled placeholder="系统生成/不可修改" />
            </div>

            <div class="field">
              <label>用户手机号（userPhone）</label>
              <input v-model="form.user_phone" class="input" placeholder="必填" />
            </div>

            <div class="field" v-if="modal.mode === 'create'">
              <label>用户密码（userPassword）</label>
              <input v-model="form.user_password" class="input" placeholder="可选，默认 123456" />
            </div>

            <!-- hospital_id：分片键，编辑禁止修改；新增必填 -->
            <div class="field">
              <label>院区ID（hospitalId）</label>
              <input
                  v-model="form.hospital_id"
                  class="input"
                  placeholder="hospitalId"
                  :disabled="modal.mode === 'edit'"
              />
            </div>

            <!-- ✅ 科室：下拉框（根据院区ID加载；编辑时禁止修改） -->
            <div class="field">
              <label>
                科室
                <span v-if="deptLoading" style="color:#64748b;font-weight:600;">（加载中…）</span>
              </label>
              <select
                  v-model="form.department_id"
                  class="select"
                  :disabled="modal.mode === 'edit' || !form.hospital_id || deptLoading"
              >
                <option value="">
                  {{ form.hospital_id ? '请选择科室' : '请先填写院区ID' }}
                </option>
                <option
                    v-for="dep in departments"
                    :key="dep.departmentId"
                    :value="dep.departmentId"
                >
                  {{ dep.departmentName }}（{{ dep.departmentId }}）
                </option>
              </select>
            </div>

            <div class="field">
              <label>医生姓名</label>
              <input v-model="form.doctor_name" class="input" placeholder="必填" />
            </div>

            <div class="field">
              <label>性别</label>
              <select v-model="form.doctor_gender" class="select">
                <option value="">-</option>
                <option value="男">男</option>
                <option value="女">女</option>
              </select>
            </div>

            <div class="field">
              <label>身份证</label>
              <input v-model="form.doctor_idcard" class="input" placeholder="必填" />
            </div>

            <div class="field">
              <label>职称</label>
              <input v-model="form.title" class="input" placeholder="如：医师/主治医师" />
            </div>

            <div class="field">
              <label>医生手机号</label>
              <input v-model="form.doctor_phone" class="input" placeholder="可选（默认等于 userPhone）" />
            </div>

            <div class="field">
              <label>医生邮箱</label>
              <input v-model="form.doctor_email" class="input" placeholder="可选" />
            </div>

            <div class="field full">
              <label>医生简介</label>
              <textarea v-model="form.doctor_intro" class="textarea" placeholder="可选"></textarea>
            </div>

            <div class="field full">
              <label>头像URL</label>
              <input v-model="form.avatar_url" class="input" placeholder="可选" />
            </div>

            <!-- 编辑提示：这些不能改 -->
            <div v-if="modal.mode === 'edit'" class="hint full">
              提示：编辑时禁止修改：院区ID（hospitalId/分片键）、科室、用户ID、医生ID。
              提交更新时会携带 hospitalId 用于分片路由，但不会更新 hospitalId 本身。
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeModal">取消</button>
          <button class="btn btn-primary" :disabled="modalSaving" @click="submitModal">
            {{ modalSaving ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="del.visible" class="modal-mask" @click.self="closeDelete">
      <div class="modal small">
        <div class="modal-header">
          <div class="modal-title">确认删除</div>
          <button class="icon-btn" @click="closeDelete">
            <Icon icon="mdi:close" />
          </button>
        </div>

        <div class="modal-body">
          <div class="muted">
            将删除医生：<span class="mono">{{ del.row?.doctorId }}</span>（{{ del.row?.doctorName }}）
          </div>
          <div class="hint" style="margin-top: 10px;">
            注意：删除需要 hospitalId（分片路由），且不会进行广播删除。
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeDelete">取消</button>
          <button class="btn btn-small danger" :disabled="del.saving" @click="doDelete">
            {{ del.saving ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'

/**
 * 对齐后端（建议）：
 * - GET    /api/admin/doctors?page=&size=&keyword=&hospitalId=&departmentId=
 * - POST   /api/admin/doctors                     body: AdminDoctorCreateRequest
 * - PUT    /api/admin/doctors/{doctorId}?hospitalId=1   body: AdminDoctorUpdateRequest（不含 hospitalId/departmentId）
 * - DELETE /api/admin/doctors/{doctorId}?hospitalId=1
 *
 * 下拉科室（复用科室分页接口）：
 * - GET /api/admin/departments?hospitalId=xxx&page=1&size=200
 */
const API_BASE = import.meta.env.VITE_API_BASE || ''

const filters = reactive({
  keyword: '',
  hospitalId: '',
  departmentId: ''
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const totalPages = computed(() => {
  const t = Math.ceil((pager.total || 0) / pager.pageSize)
  return Math.max(t, 1)
})

function formatTime(val) {
  if (!val) return '-'
  const s = String(val).replace('T', ' ').replace('Z', '')
  return s.length > 19 ? s.slice(0, 19) : s
}

function resetFilters() {
  filters.keyword = ''
  filters.hospitalId = ''
  filters.departmentId = ''
  filterDepartments.value = []
  loadDoctors(1)
}

const loading = ref(false)
const error = ref('')
const rows = ref([])

/** ======== 简易 request（对齐你其它页面风格） ======== */
async function request(url, options = {}) {
  const token = localStorage.getItem('token') || localStorage.getItem('Authorization') || ''
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  }
  if (token) {
    headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    headers['token'] = token
  }

  const res = await fetch(url, { ...options, headers })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `HTTP ${res.status}`)
  }
  const ct = res.headers.get('content-type') || ''
  if (ct.includes('application/json')) {
    return await res.json()
  }
  return await res.text()
}

function unwrapPage(resp) {
  // 兼容：Result.success(page) => { code, msg, data }
  // 或直接 Page => { records, total, ... }
  const data = resp?.data ?? resp
  return data || {}
}

/** ======== 科室下拉（新增弹窗用） ======== */
const departments = ref([]) // { departmentId, departmentName }[]
const deptLoading = ref(false)

// 查询筛选 - 科室下拉（随 filters.hospitalId 变化加载）
const filterDepartments = ref([]) // { departmentId, departmentName }[]
const filterDeptLoading = ref(false)

/** 兼容科室 VO/Entity：departmentId/departmentName 或 department_id/department_name */
function mapDepartmentItem(x) {
  return {
    departmentId: x.departmentId ?? x.department_id ?? '',
    departmentName: x.departmentName ?? x.department_name ?? x.name ?? ''
  }
}

/**
 * 复用 /api/admin/departments 分页接口
 * GET /api/admin/departments?hospitalId=xxx&page=1&size=200
 */
async function loadDepartmentsByHospital(hospitalId) {
  const hid = (hospitalId || '').trim()
  if (!hid) {
    departments.value = []
    return
  }

  deptLoading.value = true
  try {
    const qs = new URLSearchParams({
      hospitalId: hid,
      page: '1',
      size: '200'
    })
    const resp = await request(`${API_BASE}/api/admin/departments?${qs.toString()}`)
    const { records } = unwrapPage(resp)
    departments.value = (records || [])
        .map(mapDepartmentItem)
        .filter(d => d.departmentId && d.departmentName)
  } catch (e) {
    departments.value = []
    if (modal.visible) {
      modalError.value = `科室加载失败：${e.message || e}`
    }
  } finally {
    deptLoading.value = false
  }
}

async function loadFilterDepartmentsByHospital(hospitalId) {
  const hid = (hospitalId || '').trim()
  if (!hid) {
    filterDepartments.value = []
    return
  }

  filterDeptLoading.value = true
  try {
    const qs = new URLSearchParams({
      hospitalId: hid,
      page: '1',
      size: '200'
    })
    const resp = await request(`${API_BASE}/api/admin/departments?${qs.toString()}`)
    const { records } = unwrapPage(resp)
    filterDepartments.value = (records || [])
        .map(mapDepartmentItem)
        .filter(d => d.departmentId && d.departmentName)
  } catch (e) {
    filterDepartments.value = []
  } finally {
    filterDeptLoading.value = false
  }
}

/** ======== 列表 ======== */
async function loadDoctors(page = 1) {
  pager.page = Math.max(1, page)
  error.value = ''
  loading.value = true
  try {
    const qs = new URLSearchParams({
      page: String(pager.page),
      size: String(pager.pageSize),
      keyword: (filters.keyword || '').trim(),
      hospitalId: (filters.hospitalId || '').trim(),
      departmentId: (filters.departmentId || '').trim()
    })
    // 去掉空参数（避免后端误解析）
    for (const [k, v] of Array.from(qs.entries())) {
      if (!v) qs.delete(k)
    }

    const resp = await request(`${API_BASE}/api/admin/doctors?${qs.toString()}`)
    const pageObj = unwrapPage(resp)
    rows.value = pageObj.records || []
    pager.total = Number(pageObj.total || 0)
  } catch (e) {
    rows.value = []
    pager.total = 0
    error.value = `加载失败：${e.message || e}`
  } finally {
    loading.value = false
  }
}

/** ======== 弹窗：新增/编辑 ======== */
const modal = reactive({
  visible: false,
  mode: 'create' // create | edit
})
const modalSaving = ref(false)
const modalError = ref('')

const form = reactive({
  doctor_id: '',
  user_id: '',
  // 新增：创建 user 用
  user_phone: '',
  user_password: '',

  // 分片键（新增必填；编辑禁改；更新时作为 query 参数）
  hospital_id: '',

  // 新增必选；编辑禁改
  department_id: '',

  doctor_name: '',
  doctor_gender: '',
  doctor_idcard: '',
  title: '',
  doctor_phone: '',
  doctor_email: '',
  doctor_intro: '',
  avatar_url: ''
})

function resetForm() {
  form.doctor_id = ''
  form.user_id = ''

  form.user_phone = ''
  form.user_password = ''

  form.hospital_id = ''
  form.department_id = ''

  form.doctor_name = ''
  form.doctor_gender = ''
  form.doctor_idcard = ''
  form.title = ''
  form.doctor_phone = ''
  form.doctor_email = ''
  form.doctor_intro = ''
  form.avatar_url = ''
}

function openCreate() {
  resetForm()
  modalError.value = ''
  departments.value = []
  modal.mode = 'create'
  modal.visible = true
}

function openEdit(row) {
  resetForm()
  modalError.value = ''
  departments.value = []

  modal.mode = 'edit'
  modal.visible = true

  // 编辑：回填（不可改字段也展示）
  form.doctor_id = row.doctorId || ''
  form.user_id = row.userId || ''

  form.user_phone = row.userPhone || '' // 更新会改 userPhone（不改 userId）
  form.hospital_id = row.hospitalId || '' // 分片键：仅用于路由
  form.department_id = row.departmentId || '' // 禁改

  form.doctor_name = row.doctorName || ''
  form.doctor_gender = row.doctorGender || ''
  form.doctor_idcard = row.doctorIdcard || ''
  form.title = row.title || ''
  form.doctor_phone = row.doctorPhone || ''
  form.doctor_email = row.doctorEmail || ''
  form.doctor_intro = row.doctorIntro || ''
  form.avatar_url = row.avatarUrl || ''
}

function closeModal() {
  modal.visible = false
}

function validateCreate() {
  if (!form.user_phone?.trim()) return 'userPhone必填'
  if (!form.hospital_id?.trim()) return 'hospitalId必填（分片键）'
  if (!form.department_id?.trim()) return 'departmentId必选'
  if (!form.doctor_name?.trim()) return 'doctorName必填'
  if (!form.doctor_idcard?.trim()) return 'doctorIdcard必填'
  return ''
}

function validateEdit() {
  if (!form.doctor_id?.trim()) return 'doctorId缺失'
  if (!form.hospital_id?.trim()) return 'hospitalId必填（用于分片路由）'
  if (!form.user_phone?.trim()) return 'userPhone必填'
  if (!form.doctor_name?.trim()) return 'doctorName必填'
  if (!form.doctor_idcard?.trim()) return 'doctorIdcard必填'
  // 禁改字段：department_id / hospital_id / user_id / doctor_id 不允许提交变更
  return ''
}

async function submitModal() {
  modalError.value = ''
  const msg = modal.mode === 'create' ? validateCreate() : validateEdit()
  if (msg) {
    modalError.value = msg
    return
  }

  modalSaving.value = true
  try {
    if (modal.mode === 'create') {
      const payload = {
        userPhone: form.user_phone.trim(),
        userPassword: (form.user_password || '').trim(),
        hospitalId: form.hospital_id.trim(),
        departmentId: form.department_id.trim(),

        doctorName: form.doctor_name.trim(),
        doctorGender: form.doctor_gender || '',
        doctorIdcard: form.doctor_idcard.trim(),
        title: form.title || '',

        doctorPhone: form.doctor_phone || '',
        doctorEmail: form.doctor_email || '',
        doctorIntro: form.doctor_intro || '',
        avatarUrl: form.avatar_url || ''
      }

      await request(`${API_BASE}/api/admin/doctors`, {
        method: 'POST',
        body: JSON.stringify(payload)
      })
    } else {
      // 重点：更新不触碰分片键（hospitalId 不出现在 body 里），但必须作为 query 参数用于路由
      const hid = form.hospital_id.trim()
      const payload = {
        userPhone: form.user_phone.trim(),
        doctorName: form.doctor_name.trim(),
        doctorGender: form.doctor_gender || '',
        doctorIdcard: form.doctor_idcard.trim(),
        title: form.title || '',
        doctorPhone: form.doctor_phone || '',
        doctorEmail: form.doctor_email || '',
        doctorIntro: form.doctor_intro || '',
        avatarUrl: form.avatar_url || ''
      }

      const qs = new URLSearchParams({ hospitalId: hid })
      await request(`${API_BASE}/api/admin/doctors/${form.doctor_id}?${qs.toString()}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
    }

    closeModal()
    await loadDoctors(1)
  } catch (e) {
    modalError.value = `提交失败：${e.message || e}`
  } finally {
    modalSaving.value = false
  }
}

/** ======== 删除 ======== */
const del = reactive({
  visible: false,
  saving: false,
  row: null
})

function confirmDelete(row) {
  del.row = row
  del.visible = true
}

function closeDelete() {
  del.visible = false
  del.row = null
  del.saving = false
}

async function doDelete() {
  if (!del.row) return
  const doctorId = del.row.doctorId
  const hospitalId = del.row.hospitalId
  if (!hospitalId) {
    alert('删除失败：hospitalId 缺失（无法分片路由）')
    return
  }

  del.saving = true
  try {
    const qs = new URLSearchParams({ hospitalId })
    await request(`${API_BASE}/api/admin/doctors/${doctorId}?${qs.toString()}`, {
      method: 'DELETE'
    })
    closeDelete()
    await loadDoctors(1)
  } catch (e) {
    alert(`删除失败：${e.message || e}`)
  } finally {
    del.saving = false
  }
}

/**
 * ✅ 监听：筛选条件 hospitalId 变化时，刷新“科室下拉”
 * - hospitalId 为空时不加载科室列表
 * - hospitalId 变化时重置 departmentId（避免跨院区错选）
 */
watch(
    () => filters.hospitalId,
    async (newVal, oldVal) => {
      const nv = (newVal || '').trim()
      const ov = (oldVal || '').trim()
      if (nv === ov) return

      filters.departmentId = ''
      await loadFilterDepartmentsByHospital(nv)
    }
)

/**
 * ✅ 监听：院区ID变化时，自动刷新科室下拉
 * - 仅对“新增模式”生效（编辑模式禁止改院区/科室）
 */
watch(
    () => form.hospital_id,
    async (newVal, oldVal) => {
      if (!modal.visible) return
      if (modal.mode === 'edit') return

      const nv = (newVal || '').trim()
      const ov = (oldVal || '').trim()
      if (nv === ov) return

      form.department_id = ''
      modalError.value = ''
      await loadDepartmentsByHospital(nv)
    }
)

onMounted(() => {
  loadDoctors(1)
})
</script>


<style scoped>

/* 原样保留：严格保持原来的界面风格（从用户管理页面 1:1 拷贝） */

.users-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片 */
.toolbar-card,
.table-card {
  background: white;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.toolbar-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
}

.toolbar-left {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: flex-end;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 220px;
}

.field.full {
  grid-column: 1 / -1;
}

.field label {
  font-size: 0.8rem;
  color: #64748b;
}

.input,
.select {
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0 12px;
  outline: none;
  color: #0f172a;
  background: #fff;
}

.input:focus,
.select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.15);
}

/* 按钮 */
.btn {
  height: 36px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 0.9rem;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-ghost {
  background: #f1f5f9;
  color: #334155;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-mini {
  height: 30px;
  padding: 0 10px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #334155;
  font-weight: 600;
  font-size: 0.82rem;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-mini.danger {
  background: #fee2e2;
  color: #b91c1c;
}

/* 表头区 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-header h3 {
  margin: 0;
  font-size: 1.05rem;
  color: #1e293b;
}

.hint {
  font-size: 0.85rem;
  color: #64748b;
}

/* 表格 */
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { text-align: left; padding: 14px; background: #f8fafc; color: #64748b; font-size: 0.85rem; font-weight: 700; }
.admin-table td { padding: 14px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 0.9rem; vertical-align: middle; }

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.85rem;
  color: #0f172a;
}

/* 错误条 */
.error-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff1f2;
  color: #9f1239;
  border: 1px solid #fecdd3;
  padding: 10px 12px;
  border-radius: 10px;
  margin: 10px 0;
}

/* 空态 */
.empty-td {
  padding: 30px !important;
}

.empty-state {
  text-align: center;
  color: #94a3b8;
}

.empty-icon {
  font-size: 3rem;
  color: #cbd5e1;
}

.empty-title {
  font-weight: 800;
  margin-top: 8px;
  color: #64748b;
}

.empty-sub {
  margin-top: 4px;
  font-size: 0.9rem;
}

/* 分页 */
.pager {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
}

.pager-info {
  font-size: 0.9rem;
  color: #64748b;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 99;
}

.modal {
  width: 640px;
  max-width: 100%;
  background: white;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.20);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 900;
  color: #0f172a;
}

.icon-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 1.1rem;
  color: #64748b;
}

.modal-body {
  padding: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.modal-footer {
  padding: 14px 16px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
