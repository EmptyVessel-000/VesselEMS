<template>
  <div class="index-page">
    <!-- 顶部导航 -->
    <header class="index-header">
      <div class="header-inner">
        <div class="header-logo">
          <el-icon :size="28"><Ship /></el-icon>
          <span class="logo-text">VesselEMS</span>
        </div>
        <div class="header-nav">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link nav-link-primary">注册</router-link>
        </div>
      </div>
    </header>

    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-deco hero-deco-1"></div>
        <div class="hero-deco hero-deco-2"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">v2.0 企业级智能管理系统</div>
        <h1 class="hero-title">VesselEMS</h1>
        <p class="hero-subtitle">企业级智能管理系统 — 融合 NL2SQL 自然语言查询与 RAG 知识库检索，<br>让数据管理更高效、更智能</p>
        <div class="hero-actions">
          <router-link to="/login" class="hero-btn hero-btn-primary">
            立即使用
            <el-icon><ArrowRight /></el-icon>
          </router-link>
          <router-link to="/register" class="hero-btn hero-btn-secondary">
            创建账号
          </router-link>
        </div>
      </div>
    </section>

    <!-- 功能模块 -->
    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-desc">一站式企业数据管理平台</p>
      </div>
      <div class="features-grid">
        <div class="feature-card" v-for="f in features" :key="f.title">
          <div class="feature-icon" :style="{ background: f.bg }">
            <el-icon :size="28"><component :is="f.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="index-footer">
      <div class="footer-inner">
        <p>© 2026 VesselEMS. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { Ship, DataBoard, ChatDotRound, Setting, User, Folder, Key, Cpu } from '@element-plus/icons-vue'
import { ArrowRight } from '@element-plus/icons-vue'

const features = [
  {
    title: 'NL2SQL 智能查询',
    desc: '用自然语言描述需求，系统自动生成 SQL 语句并执行查询，无需编写复杂代码。',
    icon: ChatDotRound,
    bg: '#eff6ff'
  },
  {
    title: 'RAG 知识库',
    desc: '上传文档构建企业知识库，支持智能问答与语义检索，让知识触手可及。',
    icon: Folder,
    bg: '#ecfdf5'
  },
  {
    title: '用户与权限管理',
    desc: '完善的 RBAC 权限体系，支持用户、角色、菜单、权限的精细化管控。',
    icon: User,
    bg: '#fffbeb'
  },
  {
    title: '数据源管理',
    desc: '支持 MySQL、PostgreSQL 等多种数据库的连接与管理，统一数据入口。',
    icon: DataBoard,
    bg: '#f0fdfa'
  },
  {
    title: '系统配置',
    desc: '灵活的配置管理，支持字符串、数字、布尔、JSON 等多种配置类型。',
    icon: Setting,
    bg: '#faf5ff'
  },
  {
    title: '部门与组织架构',
    desc: '树形部门管理，支持多级组织架构，清晰展示企业结构。',
    icon: Cpu,
    bg: '#fef2f2'
  }
]
</script>

<style scoped>
.index-page {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航 ===== */
.index-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #f0efed;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #2563eb;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #1c1917;
  letter-spacing: 1px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-link {
  font-size: 14px;
  font-weight: 500;
  color: #57534e;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-link:hover {
  background: #f5f5f4;
  color: #1c1917;
}

.nav-link-primary {
  background: #2563eb;
  color: #ffffff;
}

.nav-link-primary:hover {
  background: #3b82f6;
  color: #ffffff;
}

/* ===== Hero 区域 ===== */
.hero-section {
  position: relative;
  overflow: hidden;
  padding: 80px 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 520px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
}

.hero-deco-1 {
  width: 400px;
  height: 400px;
  top: -120px;
  right: -80px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.06) 0%, transparent 70%);
}

.hero-deco-2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.04) 0%, transparent 70%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 720px;
}

.hero-badge {
  display: inline-block;
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
  background: #eff6ff;
  padding: 6px 16px;
  border-radius: 20px;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  color: #1c1917;
  letter-spacing: 2px;
  margin-bottom: 16px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 16px;
  color: #78716c;
  line-height: 1.7;
  margin-bottom: 36px;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  padding: 14px 32px;
  border-radius: 10px;
  text-decoration: none;
  transition: all 0.2s ease-out;
}

.hero-btn-primary {
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.25);
}

.hero-btn-primary:hover {
  background: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.3);
}

.hero-btn-secondary {
  background: #f5f5f4;
  color: #57534e;
}

.hero-btn-secondary:hover {
  background: #e7e5e4;
  transform: translateY(-2px);
}

/* ===== 功能模块 ===== */
.features-section {
  padding: 80px 24px;
  background: #fafaf9;
}

.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #1c1917;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 15px;
  color: #a8a29e;
}

.features-grid {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease-out;
}

.feature-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-4px);
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: #2563eb;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1c1917;
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 14px;
  color: #78716c;
  line-height: 1.6;
}

/* ===== 页脚 ===== */
.index-footer {
  border-top: 1px solid #f0efed;
  padding: 24px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  font-size: 13px;
  color: #a8a29e;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  .hero-actions {
    flex-direction: column;
  }
}
</style>