<template>
  <div id="app-container">
    <script src="/global-header.js"></script>
    <main class="wrap" id="app">
      <section class="hero">
        <div class="badge">Worklist v1.0</div>
        <h1>欢迎来到 Worklist</h1>
        <p>
          一个功能完整的任务管理系统，支持多角色权限分离、任务全生命周期管理、
          实时统计分析和高效的协作流程。
        </p>
        <div class="actions">
          <a class="btn primary" href="/login">登录系统</a>
          <a class="btn" href="/register">创建账户</a>
          <a class="btn" href="/demo.html">查看演示</a>
        </div>
      </section>

      <section class="intro">
        <h2>📋 项目简介</h2>
        <p>
          Worklist 是一个基于 Spring Boot + Vue.js 的现代化任务管理系统。
          它实现了完整的权限分离架构，支持三个不同角色（Guest、Member、Manager）的协作工作流，
          提供任务创建、分配、执行、验收的全流程管理，以及详细的统计分析功能。
        </p>
      </section>

      <section class="intro">
        <h2>✨ 核心功能</h2>
        <div class="features">
          <div class="feature-card">
            <div class="feature-icon">📝</div>
            <h3>任务管理</h3>
            <p>创建、编辑、分配、跟踪任务，支持详细的任务描述和批注</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">👥</div>
            <h3>权限分离</h3>
            <p>三角色权限模型，确保不同用户只能访问相应的功能和数据</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>统计分析</h3>
            <p>实时任务统计、打回率分析、成员绩效评估、七日趋势图表</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">✅</div>
            <h3>验收流程</h3>
            <p>完整的任务验收机制，支持通过、打回和重新分配</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">⚡</div>
            <h3>高效查询</h3>
            <p>使用 JOIN 查询一次性获取所有数据，消除 N+1 问题</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔒</div>
            <h3>安全认证</h3>
            <p>基于 Spring Security 的完整认证和授权机制</p>
          </div>
        </div>
      </section>

      <section class="intro">
        <h2>👤 角色说明</h2>
        <div class="roles">
          <div class="role-card">
            <h3>🎯 Guest（甲方）</h3>
            <ul>
              <li>创建任务</li>
              <li>编辑未分配的任务</li>
              <li>审核任务验收</li>
              <li>通过或打回任务</li>
              <li>查看创建的任务统计</li>
            </ul>
          </div>
          <div class="role-card">
            <h3>💼 Member（乙方执行）</h3>
            <ul>
              <li>接受分配的任务</li>
              <li>更新任务进度</li>
              <li>提交任务验收</li>
              <li>接受打回的任务</li>
              <li>查看个人绩效统计</li>
            </ul>
          </div>
          <div class="role-card">
            <h3>⚙️ Manager（乙方协调）</h3>
            <ul>
              <li>分配任务给 Member</li>
              <li>管理所有任务</li>
              <li>查看全局统计</li>
              <li>分析成员绩效</li>
              <li>协调项目进度</li>
            </ul>
          </div>
        </div>
      </section>

      <section class="intro">
        <h2>🛠️ 技术栈</h2>
        <div class="tech-stack">
          <div class="tech-grid">
            <div class="tech-item">Spring Boot 3.x</div>
            <div class="tech-item">Spring Security</div>
            <div class="tech-item">Spring Data JPA</div>
            <div class="tech-item">MySQL 8.0</div>
            <div class="tech-item">Vue.js 3</div>
            <div class="tech-item">Element Plus</div>
            <div class="tech-item">ECharts 5</div>
            <div class="tech-item">RESTful API</div>
          </div>
        </div>
      </section>

      <section class="intro">
        <h2>🚀 快速开始</h2>
        <p style="margin-bottom: 16px;">
          <strong>1. 创建账户</strong><br>
          点击"创建账户"按钮，选择角色（Guest、Member 或 Manager）并填写信息。
        </p>
        <p style="margin-bottom: 16px;">
          <strong>2. 登录系统</strong><br>
          使用创建的账户登录，进入对应角色的控制台。
        </p>
        <p style="margin-bottom: 16px;">
          <strong>3. 开始使用</strong><br>
          根据你的角色，创建任务、分配任务或执行任务。
        </p>
        <p>
          <strong>4. 查看统计</strong><br>
          在统计页面查看任务进度、成员绩效和各类数据分析。
        </p>
      </section>

      <section class="intro">
        <h2>📈 任务状态流转</h2>
        <p>
          <strong>1 → 未分配</strong> → <strong>2 → 未开始</strong> → <strong>3 → 进行中</strong> → 
          <strong>4 → 验收中</strong> → <strong>6 → 已完成</strong><br>
          <br>
          如果验收不通过，任务会进入 <strong>5 → 已打回</strong> 状态，
          然后重新分配给 Member 继续执行。
        </p>
      </section>
    </main>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {}
  }
}
</script>

<style scoped>
@import '/global-header.css';
@import '/style.css';
@import '/role-pages.css';
@import '/task-card.css';
@import '/dialog.css';
@import '/element-plus.css';
</style>