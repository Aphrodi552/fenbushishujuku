<template>
  <div class="users-page">
    <!-- 顶部工具条 -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="field">
          <label>关键字</label>
          <input
              v-model="filters.keyword"
              class="input"
              placeholder="按手机号 / 用户ID 搜索"
              @keyup.enter="loadUsers(1)"
          />
        </div>

        <div class="field">
          <label>角色</label>
          <select v-model="filters.role" class="select" @change="loadUsers(1)">
            <option value="">全部</option>
            <option value="user">普通用户</option>
            <option value="doctor">医生</option>
            <option value="admin">管理员</option>
          </select>
        </div>

        <button class="btn btn-primary" @click="loadUsers(1)">
          <Icon icon="mdi:magnify" /> 查询
        </button>

        <button class="btn btn-ghost" @click="resetFilters">
          <Icon icon="mdi:refresh" /> 重置
        </button>
      </div>

      <div class="toolbar-right">
        <button class="btn btn-primary" @click="openCreate">
          <Icon icon="mdi:account-plus" /> 新增用户
        </button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <div class="section-header">
        <h3>用户列表</h3>
        <div class="hint">
          <span v-if="loading">加载中…</span>
          <span v-else>共 {{ pager.total }} 条</span>
        </div>
      </div>

      <div v-if="error" class="error-bar">
        <Icon icon="mdi:alert-circle-outline" />
        {{ error }}
      </div>

      <table class="admin-table">
        <thead>
        <tr>
          <th style="width: 210px;">用户ID</th>
          <th style="width: 160px;">手机号</th>
          <th style="width: 140px;">角色</th>
          <th style="width: 180px;">创建时间</th>
          <th style="width: 260px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="u in users" :key="u.user_id">
          <td class="mono">{{ u.user_id }}</td>
          <td>{{ u.user_phone }}</td>
          <td>
              <span class="badge" :class="roleBadgeClass(u.role)">
                {{ roleLabel(u.role) }}
              </span>
          </td>
          <td>{{ formatTime(u.created_at) }}</td>
          <td class="actions">
            <button class="btn btn-mini" @click="openEdit(u)">
              <Icon icon="mdi:pencil-outline" /> 编辑
            </button>

            <button class="btn btn-mini" @click="openRole(u)">
              <Icon icon="mdi:shield-account-outline" /> 身份
            </button>

            <button class="btn btn-mini danger" @click="confirmResetPwd(u)">
              <Icon icon="mdi:lock-reset" /> 重置密码
            </button>

            <button class="btn btn-mini danger" @click="confirmDelete(u)">
              <Icon icon="mdi:delete-outline" /> 删除
            </button>
          </td>
        </tr>

        <tr v-if="!loading && users.length === 0">
          <td colspan="5" class="empty-td">
            <div class="empty-state">
              <Icon icon="mdi:account-search-outline" class="empty-icon" />
              <div class="empty-title">暂无数据</div>
              <div class="empty-sub">请调整筛选条件后重试。</div>
            </div>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pager">
        <button class="btn btn-ghost" :disabled="pager.page <= 1" @click="loadUsers(pager.page - 1)">
          上一页
        </button>
        <div class="pager-info">
          第 {{ pager.page }} / {{ totalPages }} 页（每页 {{ pager.pageSize }} 条）
        </div>
        <button class="btn btn-ghost" :disabled="pager.page >= totalPages" @click="loadUsers(pager.page + 1)">
          下一页
        </button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="modal.visible" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            <Icon :icon="modal.mode === 'create' ? 'mdi:account-plus' : 'mdi:account-edit-outline'" />
            {{ modal.mode === 'create' ? '新增用户' : '编辑用户' }}
          </div>
          <button class="icon-btn" @click="closeModal"><Icon icon="mdi:close" /></button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <div class="field">
              <label>手机号</label>
              <input v-model.trim="form.user_phone" class="input" placeholder="例如：138xxxxxx" />
            </div>

            <div class="field">
              <label>角色</label>
              <select v-model="form.role" class="select">
                <option value="user">普通用户</option>
                <option value="doctor">医生</option>
                <option value="admin">管理员</option>
              </select>
            </div>

            <div class="field full">
              <label>
                密码
                <span v-if="modal.mode === 'edit'" style="color:#64748b;font-weight:600;">
                  （编辑不支持直接改密码，请用“重置密码”）
                </span>
              </label>
              <input
                  v-model="form.user_password"
                  class="input"
                  type="password"
                  :placeholder="modal.mode === 'create' ? '不少于 6 位' : '编辑模式下此项不生效'"
                  :disabled="modal.mode === 'edit'"
              />
            </div>

            <div v-if="modal.mode === 'edit'" class="field full">
              <label>用户ID</label>
              <input class="input" :value="form.user_id" disabled />
            </div>
          </div>

          <div v-if="modalError" class="error-bar">
            <Icon icon="mdi:alert-circle-outline" />
            {{ modalError }}
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeModal">取消</button>
          <button class="btn btn-primary" :disabled="modalSaving" @click="submitForm">
            {{ modalSaving ? '提交中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 身份管理弹窗（内部用 PUT /api/admin/users/{id} 更新 role） -->
    <div v-if="roleModal.visible" class="modal-mask" @click.self="closeRoleModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            <Icon icon="mdi:shield-account-outline" /> 身份管理
          </div>
          <button class="icon-btn" @click="closeRoleModal"><Icon icon="mdi:close" /></button>
        </div>

        <div class="modal-body">
          <div class="role-box">
            <div class="role-row">
              <div class="role-k">用户ID</div>
              <div class="role-v mono">{{ roleModal.user?.user_id }}</div>
            </div>
            <div class="role-row">
              <div class="role-k">手机号</div>
              <div class="role-v">{{ roleModal.user?.user_phone }}</div>
            </div>
            <div class="role-row">
              <div class="role-k">当前角色</div>
              <div class="role-v">
                <span class="badge" :class="roleBadgeClass(roleModal.user?.role)">
                  {{ roleLabel(roleModal.user?.role) }}
                </span>
              </div>
            </div>

            <div class="divider"></div>

            <div class="field">
              <label>调整为</label>
              <select v-model="roleModal.nextRole" class="select">
                <option value="user">普通用户</option>
                <option value="doctor">医生</option>
                <option value="admin">管理员</option>
              </select>
            </div>

            <div class="warn">
              提示：角色取值严格限定为 'user' / 'doctor' / 'admin'，请与后端一致。
            </div>

            <div v-if="roleModal.error" class="error-bar">
              <Icon icon="mdi:alert-circle-outline" />
              {{ roleModal.error }}
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeRoleModal">取消</button>
          <button class="btn btn-primary" :disabled="roleModal.saving" @click="submitRole">
            {{ roleModal.saving ? '提交中…' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'

/**
 * 对齐后端（建议）：
 * - GET    /api/admin/users?page=&size=&keyword=&role=
 * - POST   /api/admin/users                     body: { userPhone, role, userPassword? }
 * - PUT    /api/admin/users/{userId}            body: { userPhone, role }
 * - PATCH  /api/admin/users/{userId}/reset-password
 *
 * 后端返回（常见 Result 包装）：
 * - { code, message, data: { records, total, current, size } }
 * 或者直接 { records, total, ... }
 */
const API_BASE = import.meta.env.VITE_API_BASE || '' // 例如 http://localhost:8080

const loading = ref(false)
const error = ref('')
const users = ref([])

const filters = reactive({
  keyword: '',
  role: ''
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

function roleLabel(role) {
  if (role === 'admin') return '管理员'
  if (role === 'doctor') return '医生'
  return '普通用户'
}

function roleBadgeClass(role) {
  if (role === 'admin') return 'badge-red'
  if (role === 'doctor') return 'badge-purple'
  return 'badge-blue'
}

function formatTime(val) {
  if (!val) return '-'
  const s = String(val).replace('T', ' ').replace('Z', '')
  return s.length > 19 ? s.slice(0, 19) : s
}

function resetFilters() {
  filters.keyword = ''
  filters.role = ''
  loadUsers(1)
}

/** 统一请求：自动带 token（兼容常见后端） */
async function request(url, options = {}) {
  const token = localStorage.getItem('token') || localStorage.getItem('Authorization') || ''
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  }
  if (token) {
    // 兼容：Bearer 与纯 token 两种写法（后端取哪个就用哪个）
    headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    headers['token'] = token
  }

  const res = await fetch(url, { ...options, headers })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `HTTP ${res.status}`)
  }
  const ct = res.headers.get('content-type') || ''
  if (ct.includes('application/json')) return res.json()
  return res.text()
}

/** 将后端 VO（camelCase）映射为本页面既有字段（snake_case），保证样式与模板不动 */
function mapUserVOToRow(vo) {
  return {
    user_id: vo.userId ?? vo.user_id ?? '',
    user_phone: vo.userPhone ?? vo.user_phone ?? '',
    role: (vo.role ?? '').toLowerCase(),
    created_at: vo.createdAt ?? vo.created_at ?? ''
  }
}

/** 解包 Result / Page：兼容多种后端返回 */
function unwrapPage(resp) {
  // 1) 兼容 Result<T>：真实数据在 resp.data
  const data = resp?.data && typeof resp.data === 'object' ? resp.data : resp

  // 2) 分页结构：{ records, total, ... } 或 { list, total, ... }
  if (data && (Array.isArray(data.records) || Array.isArray(data.list))) {
    const records = data.records ?? data.list ?? []
    const total = Number(data.total ?? records.length ?? 0)
    return { records, total }
  }

  // 3) 有些后端把列表放在 data.data
  if (data && Array.isArray(data.data)) {
    const records = data.data
    const total = Number(data.total ?? records.length ?? 0)
    return { records, total }
  }

  // 4) 兼容“单个用户对象”：Result<User> 或直接 User
  const looksLikeUser =
      data && typeof data === 'object' &&
      (data.userId || data.user_id || data.userPhone || data.user_phone)

  if (looksLikeUser) {
    return { records: [data], total: 1 }
  }

  // 5) 兼容“直接数组”
  if (Array.isArray(data)) {
    return { records: data, total: data.length }
  }

  return { records: [], total: 0 }
}

async function loadUsers(page = pager.page) {
  loading.value = true
  error.value = ''
  try {
    pager.page = page
    const qs = new URLSearchParams({
      keyword: filters.keyword || '',
      role: filters.role || '',
      page: String(pager.page),
      size: String(pager.pageSize)
    })

    const resp = await request(`${API_BASE}/api/admin/users?${qs.toString()}`)
    const { records, total } = unwrapPage(resp)

    users.value = (records || []).map(mapUserVOToRow)
    pager.total = total
  } catch (e) {
    error.value = `加载失败：${e.message || e}`
  } finally {
    loading.value = false
  }
}

/* ---------- 新增/编辑 ---------- */
const modal = reactive({
  visible: false,
  mode: 'create' // create | edit
})
const modalSaving = ref(false)
const modalError = ref('')

const form = reactive({
  user_id: '',
  user_phone: '',
  user_password: '',
  role: 'user'
})

function openCreate() {
  modal.visible = true
  modal.mode = 'create'
  modalError.value = ''
  form.user_id = ''
  form.user_phone = ''
  form.user_password = ''
  form.role = 'user'
}

function openEdit(u) {
  modal.visible = true
  modal.mode = 'edit'
  modalError.value = ''
  form.user_id = u.user_id
  form.user_phone = u.user_phone
  form.user_password = '' // 编辑不支持直接改密码
  form.role = u.role || 'user'
}

function closeModal() {
  modal.visible = false
  modalError.value = ''
}

function validateForm() {
  if (!form.user_phone) return '手机号不能为空'
  if (!/^[0-9+\-]{6,20}$/.test(form.user_phone)) return '手机号格式不合法（建议仅数字，长度 6~20）'
  if (!['user', 'doctor', 'admin'].includes(form.role)) return '角色取值非法'
  if (modal.mode === 'create') {
    if (form.user_password && form.user_password.length < 6) return '密码长度不足（不少于 6 位）'
    // 允许不填：后端默认 123456（与你的实现一致）
  }
  return ''
}

async function submitForm() {
  modalError.value = ''
  const msg = validateForm()
  if (msg) {
    modalError.value = msg
    return
  }

  modalSaving.value = true
  try {
    if (modal.mode === 'create') {
      const payload = {
        userPhone: form.user_phone,
        role: form.role
      }
      if (form.user_password) payload.userPassword = form.user_password

      await request(`${API_BASE}/api/admin/users`, {
        method: 'POST',
        body: JSON.stringify(payload)
      })
    } else {
      // 编辑：只改手机号/角色（密码用重置）
      await request(`${API_BASE}/api/admin/users/${encodeURIComponent(form.user_id)}`, {
        method: 'PUT',
        body: JSON.stringify({
          userPhone: form.user_phone,
          role: form.role
        })
      })
    }

    closeModal()
    await loadUsers(1)
  } catch (e) {
    modalError.value = `提交失败：${e.message || e}`
  } finally {
    modalSaving.value = false
  }
}

/* ---------- 删除（后端未实现：保留按钮风格，避免报错） ---------- */
async function confirmDelete(u) {
  if (!confirm(`确定删除用户 ${u.user_phone} 吗？此操作不可恢复。`)) return
  try {
    await request(`${API_BASE}/api/admin/users/${encodeURIComponent(u.user_id)}`, {
      method: 'DELETE'
    })
    await loadUsers(pager.page)
  } catch (e) {
    alert(`删除失败：${e.message || e}`)
  }
}


/* ---------- 重置密码 ---------- */
async function confirmResetPwd(u) {
  if (!confirm(`确定将用户 ${u.user_phone} 的密码重置为 123456 吗？`)) return
  try {
    await request(`${API_BASE}/api/admin/users/${encodeURIComponent(u.user_id)}/reset-password`, {
      method: 'PATCH'
    })
    await loadUsers(pager.page)
  } catch (e) {
    alert(`重置失败：${e.message || e}`)
  }
}

/* ---------- 身份管理（改角色：复用 PUT /api/admin/users/{id}） ---------- */
const roleModal = reactive({
  visible: false,
  user: null,
  nextRole: 'user',
  saving: false,
  error: ''
})

function openRole(u) {
  roleModal.visible = true
  roleModal.user = u
  roleModal.nextRole = u.role || 'user'
  roleModal.saving = false
  roleModal.error = ''
}

function closeRoleModal() {
  roleModal.visible = false
  roleModal.user = null
  roleModal.error = ''
}

async function submitRole() {
  roleModal.error = ''
  if (!roleModal.user) return

  const uid = roleModal.user.user_id
  const nextRole = roleModal.nextRole

  if (!['user', 'doctor', 'admin'].includes(nextRole)) {
    roleModal.error = '角色取值非法'
    return
  }
  if (nextRole === roleModal.user.role) {
    roleModal.error = '未发生变更'
    return
  }

  roleModal.saving = true
  try {
    await request(`${API_BASE}/api/admin/users/${encodeURIComponent(uid)}`, {
      method: 'PUT',
      body: JSON.stringify({
        userPhone: roleModal.user.user_phone,
        role: nextRole
      })
    })

    closeRoleModal()
    await loadUsers(pager.page)
  } catch (e) {
    roleModal.error = `修改失败：${e.message || e}`
  } finally {
    roleModal.saving = false
  }
}

onMounted(() => {
  loadUsers(1)
})
</script>

<style scoped>
/* 原样保留：严格保持原来的界面风格 */

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

/* 角色徽标 */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 800;
}

.badge-blue { background: #dbeafe; color: #1d4ed8; }
.badge-purple { background: #ede9fe; color: #6d28d9; }
.badge-red { background: #fee2e2; color: #b91c1c; }

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

/* 身份管理 */
.role-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-row {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 10px;
  align-items: center;
}

.role-k { color: #64748b; font-weight: 700; font-size: 0.9rem; }
.role-v { color: #0f172a; font-size: 0.92rem; }

.divider {
  height: 1px;
  background: #e2e8f0;
  margin: 8px 0;
}

.warn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #475569;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 0.9rem;
}
</style>
