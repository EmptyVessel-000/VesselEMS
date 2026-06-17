<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ===== 全局基础样式 ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  line-height: 1.6;
  color: #44403c;
  background: #f5f5f4;
}

/* ===== 全局字体层级 ===== */
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1c1917;
  line-height: 1.4;
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 14px;
  color: #a8a29e;
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1c1917;
  line-height: 1.5;
}

.text-body {
  font-size: 14px;
  font-weight: 400;
  color: #44403c;
  line-height: 1.6;
}

.text-secondary {
  font-size: 13px;
  font-weight: 400;
  color: #78716c;
  line-height: 1.5;
}

.text-muted {
  font-size: 12px;
  font-weight: 400;
  color: #a8a29e;
  line-height: 1.5;
}

/* ===== Element Plus 主题色 ===== */
:root {
  --el-color-primary: #2563eb;
  --el-color-primary-light-3: #60a5fa;
  --el-color-primary-light-5: #93c5fd;
  --el-color-primary-light-7: #bfdbfe;
  --el-color-primary-light-8: #dbeafe;
  --el-color-primary-light-9: #eff6ff;
  --el-color-success: #10b981;
  --el-color-warning: #f59e0b;
  --el-color-danger: #ef4444;
  --el-color-info: #78716c;
  --el-border-radius-base: 8px;
  --el-border-radius-small: 6px;
  --el-border-radius-round: 20px;
  --el-font-size-base: 14px;
  --el-font-size-small: 12px;
  --el-font-size-large: 16px;
  --el-border-color: #e7e5e4;
  --el-border-color-light: #f0efed;
  --el-bg-color: #ffffff;
  --el-bg-color-page: #f5f5f4;
  --el-text-color-primary: #1c1917;
  --el-text-color-regular: #44403c;
  --el-text-color-secondary: #78716c;
  --el-text-color-placeholder: #a8a29e;
  --el-fill-color: #f5f5f4;
  --el-fill-color-light: #fafaf9;
  --el-fill-color-lighter: #fcfcfb;
  --el-mask-color: rgba(0, 0, 0, 0.3);
  --el-box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --el-box-shadow-light: 0 1px 2px rgba(0, 0, 0, 0.04);
  --el-box-shadow-lighter: 0 1px 1px rgba(0, 0, 0, 0.02);
}

/* ===== 全局尺寸规范 ===== */

/* 页面容器：所有管理页面的外层 */
.page-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 页面头部：标题 + 可选的副标题 */
.page-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 0;
}

/* 卡片式搜索区 — 统一 padding: 20px 24px */
.search-section {
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
}
.search-section .el-form {
  margin-bottom: 0;
}
.search-section .el-form-item {
  margin-bottom: 0;
}
.search-section .el-form-item__label {
  font-size: 13px;
  color: #78716c;
  font-weight: 500;
  line-height: 32px;
}
.search-section .el-input {
  --el-input-height: 32px;
}

/* 卡片式表格区 — 统一 padding: 20px 24px */
.table-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
}

/* 工具栏 — 统一按钮组 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}
.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 选中提示 */
.selected-tip {
  font-size: 13px;
  color: #a8a29e;
}
.selected-tip strong {
  color: #2563eb;
}

/* 分页 — 统一 padding-top */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  flex-shrink: 0;
}

/* 对话框表单 */
.dialog-form {
  padding-right: 20px;
}
.dialog-form .el-form-item__label {
  font-size: 14px;
  font-weight: 500;
  color: #57534e;
}

/* 批量删除文本 */
.batch-delete-text {
  font-size: 15px;
  color: #57534e;
  text-align: center;
  padding: 16px 0;
}
.batch-delete-text strong {
  color: #ef4444;
  font-size: 18px;
}

/* ===== Element Plus 组件尺寸覆写 ===== */

/* 按钮 — 统一尺寸规范 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease-out;
}
.el-button--small {
  height: 28px;
  font-size: 12px;
  padding: 5px 12px;
}
.el-button--default {
  height: 32px;
  font-size: 13px;
  padding: 7px 16px;
}
.el-button--large {
  height: 40px;
  font-size: 14px;
  padding: 10px 20px;
}
.el-button--primary {
  --el-button-bg-color: #2563eb;
  --el-button-border-color: #2563eb;
  --el-button-hover-bg-color: #3b82f6;
  --el-button-hover-border-color: #3b82f6;
  --el-button-active-bg-color: #1d4ed8;
  --el-button-active-border-color: #1d4ed8;
}
.el-button--primary:hover {
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
  transform: translateY(-1px);
}
.el-button--primary:active {
  transform: translateY(0);
  box-shadow: none;
}
.el-button--danger:hover {
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.2);
  transform: translateY(-1px);
}
.el-button--danger:active {
  transform: translateY(0);
}
.el-button--success:hover {
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
  transform: translateY(-1px);
}
.el-button--success:active {
  transform: translateY(0);
}

/* 输入框 */
.el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e7e5e4 inset;
  transition: box-shadow 0.2s ease-out;
  background: #fafaf9;
}
.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #d6d3d1 inset;
}
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px #93c5fd inset;
}
.el-input--large .el-input__wrapper {
  border-radius: 10px;
}
.el-input__inner {
  font-size: 14px;
  color: #44403c;
}

/* 卡片 */
.el-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease-out, transform 0.2s ease-out;
}
.el-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 对话框 */
.el-dialog {
  border-radius: 16px;
  --el-dialog-border-radius: 16px;
}
.el-dialog__header {
  padding: 24px 24px 0;
  margin-right: 0;
}
.el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #1c1917;
}
.el-dialog__body {
  padding: 20px 24px;
}
.el-dialog__footer {
  padding: 0 24px 20px;
}
.el-dialog__footer .el-button {
  height: 36px;
  font-size: 14px;
  padding: 8px 20px;
}

/* 表格 */
.el-table {
  border-radius: 12px;
  overflow: hidden;
  font-size: 14px;
}
.el-table th.el-table__cell {
  background-color: #fafaf9;
  font-weight: 600;
  color: #57534e;
  font-size: 13px;
  padding: 10px 0;
}
.el-table td.el-table__cell {
  padding: 12px 0;
  color: #44403c;
}
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: #fcfcfb;
}
.el-table__body tr:hover > td {
  background-color: #f5f5f4 !important;
}

/* 分页 */
.el-pagination .el-pager li {
  border-radius: 8px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  font-weight: 500;
  font-size: 13px;
}
.el-pagination .el-pager li.is-active {
  background-color: #2563eb;
  color: #ffffff;
}
.el-pagination .btn-prev,
.el-pagination .btn-next {
  border-radius: 8px;
  min-width: 32px;
  height: 32px;
}
.el-pagination__total {
  font-size: 13px;
  color: #78716c;
}

/* 标签 */
.el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
  font-size: 12px;
  height: 24px;
  line-height: 24px;
  padding: 0 8px;
}

/* 开关 */
.el-switch__core {
  border-radius: 12px;
  height: 20px;
  width: 36px;
}

/* 选择器 */
.el-select .el-input__wrapper {
  border-radius: 8px;
}

/* 面包屑 */
.el-breadcrumb__inner {
  font-weight: 500;
  color: #78716c;
  font-size: 14px;
}
.el-breadcrumb__inner.is-link {
  color: #57534e;
}
.el-breadcrumb__inner.is-link:hover {
  color: #2563eb;
}

/* 菜单 */
.el-menu {
  border-right: none;
}
.el-menu-item {
  border-radius: 8px;
  margin: 2px 8px;
  transition: all 0.15s ease-out;
  font-size: 14px;
  height: 40px;
  line-height: 40px;
}
.el-menu-item:hover {
  background-color: #f5f5f4;
}
.el-menu-item.is-active {
  background-color: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}
.el-sub-menu__title {
  border-radius: 8px;
  margin: 2px 8px;
  font-size: 14px;
  height: 40px;
  line-height: 40px;
}
.el-sub-menu__title:hover {
  background-color: #f5f5f4;
}

/* 下拉菜单 */
.el-dropdown-menu__item {
  border-radius: 6px;
  margin: 2px 4px;
  padding: 6px 12px;
  font-size: 13px;
  height: 32px;
  line-height: 20px;
}
.el-dropdown-menu__item:hover {
  background-color: #eff6ff;
  color: #2563eb;
}

/* 表单 */
.el-form-item__label {
  font-size: 14px;
  font-weight: 500;
  color: #57534e;
  line-height: 40px;
}
.el-form-item {
  margin-bottom: 18px;
}

/* 其他 */
.el-alert {
  border-radius: 10px;
}
.el-upload-dragger {
  border-radius: 12px;
}
.el-tree {
  border-radius: 8px;
  font-size: 14px;
}
.el-select-dropdown__item {
  border-radius: 6px;
  font-size: 14px;
  height: 34px;
}
.el-empty__description p {
  font-size: 13px;
  color: #a8a29e;
}

/* 路由切换过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>