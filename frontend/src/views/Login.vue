<template>
  <div class="login-wrapper">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-icon">
          <el-icon :size="64"><Ship /></el-icon>
        </div>
        <h1 class="brand-title">VesselEMS</h1>
        <p class="brand-subtitle">企业级管理系统</p>
      </div>

      <!-- 右侧表单区 -->
      <div class="login-form-area">
        <h2 class="form-title">欢迎登录</h2>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
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

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <p class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Ship } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  email: '',
  password: ''
})

const formRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  // 模拟登录：延迟 800ms 模拟网络请求
  await new Promise(resolve => setTimeout(resolve, 800))

  // 演示模式：密码为 "123456" 模拟登录失败，其他密码模拟登录成功
  if (formData.password === '123456') {
    ElMessage.error('登录失败：账号或密码错误')
    loading.value = false
    return
  }

  ElMessage.success('登录成功（演示模式）')
  loading.value = false
  router.push('/main/dashboard')
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
}

.login-card {
  display: flex;
  width: 780px;
  max-width: 95vw;
  min-height: 460px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 左侧品牌区 */
.login-brand {
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
.login-form-area {
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
  margin-bottom: 32px;
  align-self: flex-start;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
}

.register-link {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
}

.register-link a {
  color: #2563eb;
  text-decoration: none;
  margin-left: 4px;
}

.register-link a:hover {
  text-decoration: underline;
}

/* 响应式：小屏幕上下排列 */
@media (max-width: 640px) {
  .login-card {
    flex-direction: column;
    width: 360px;
  }

  .login-brand {
    flex: 0 0 auto;
    padding: 32px 24px;
  }

  .brand-icon {
    margin-bottom: 12px;
  }

  .login-form-area {
    padding: 32px 24px;
  }
}
</style>