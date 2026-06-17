<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon :size="48"><Ship /></el-icon>
          </div>
          <h1 class="brand-title">VesselEMS</h1>
          <p class="brand-subtitle">企业级管理系统</p>
        </div>
        <div class="brand-decoration">
          <div class="deco-circle deco-circle-1"></div>
          <div class="deco-circle deco-circle-2"></div>
          <div class="deco-circle deco-circle-3"></div>
        </div>
      </div>
      <div class="login-form-panel">
        <div class="form-header">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请登录您的账号以继续</p>
        </div>
        <el-form ref="formRef" :model="formData" :rules="formRules" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="formData.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>
        <p class="register-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Ship } from '@element-plus/icons-vue'
import { loginUser } from '../stores/user.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const formData = reactive({ email: '', password: '' })
const formRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码不少于6位', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    await loginUser(formData.email, formData.password)
    ElMessage.success('登录成功')
    router.push('/workspace')
  } catch (e) {
    ElMessage.error(e?.message || '邮箱或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f4;
  padding: 20px;
}

.login-container {
  display: flex;
  width: 800px;
  max-width: 100%;
  min-height: 520px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 品牌区 - 经典蓝 */
.login-brand {
  flex: 0 0 340px;
  background: #2563eb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.brand-icon {
  margin-bottom: 20px;
  color: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 8px;
  color: #ffffff;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4px;
}

/* 装饰圆 */
.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.deco-circle-1 {
  width: 240px;
  height: 240px;
  top: -80px;
  right: -60px;
}

.deco-circle-2 {
  width: 160px;
  height: 160px;
  bottom: -40px;
  left: -40px;
}

.deco-circle-3 {
  width: 80px;
  height: 80px;
  bottom: 60px;
  right: 40px;
  background: rgba(255, 255, 255, 0.08);
}

/* 表单区 */
.login-form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 52px;
}

.form-header {
  text-align: center;
  margin-bottom: 36px;
  width: 100%;
}

.form-title {
  font-size: 22px;
  font-weight: 700;
  color: #1c1917;
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 14px;
  color: #a8a29e;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 1px;
}

.register-link {
  font-size: 13px;
  color: #a8a29e;
  margin-top: 16px;
  text-align: center;
}

.register-link a {
  color: #2563eb;
  text-decoration: none;
  margin-left: 4px;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}

@media (max-width: 640px) {
  .login-container {
    flex-direction: column;
    min-height: auto;
  }
  .login-brand {
    flex: 0 0 auto;
    padding: 40px 24px;
    min-height: 200px;
  }
  .brand-icon {
    margin-bottom: 12px;
  }
  .login-form-panel {
    padding: 32px 24px;
  }
}
</style>