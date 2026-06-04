<template>
  <div class="register-wrapper">
    <div class="register-card">
      <!-- 左侧品牌区 -->
      <div class="register-brand">
        <div class="brand-icon">
          <el-icon :size="64"><Ship /></el-icon>
        </div>
        <h1 class="brand-title">VesselEMS</h1>
        <p class="brand-subtitle">企业级管理系统</p>
      </div>

      <!-- 右侧表单区 -->
      <div class="register-form-area">
        <h2 class="form-title">创建账号</h2>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="formData.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <p class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Ship, User } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  // 演示模式：模拟注册延迟
  await new Promise(resolve => setTimeout(resolve, 800))

  ElMessage.success('注册成功，请登录（演示模式）')
  loading.value = false
  router.push('/login')
}
</script>

<style scoped>
.register-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
}

.register-card {
  display: flex;
  width: 780px;
  max-width: 95vw;
  min-height: 520px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 左侧品牌区 */
.register-brand {
  flex: 0 0 320px;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  padding: 40px;
}

.brand-icon {
  margin-bottom: 20px;
  color: #3b82f6;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 14px;
  color: #94a3b8;
  letter-spacing: 4px;
}

/* 右侧表单区 */
.register-form-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 28px;
  align-self: flex-start;
}

.register-form {
  width: 100%;
}

.register-btn {
  width: 100%;
}

.login-link {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
}

.login-link a {
  color: #2563eb;
  text-decoration: none;
  margin-left: 4px;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 响应式：小屏幕上下排列 */
@media (max-width: 640px) {
  .register-card {
    flex-direction: column;
    width: 360px;
  }

  .register-brand {
    flex: 0 0 auto;
    padding: 32px 24px;
  }

  .brand-icon {
    margin-bottom: 12px;
  }

  .register-form-area {
    padding: 32px 24px;
  }
}
</style>