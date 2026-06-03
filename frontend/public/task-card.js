// 任务卡片共享工具函数

/**
 * 状态映射
 * 注：状态 5（已打回）在功能上等价于状态 2（未开始），但保持独立的颜色和文字
 */
const STATUS_MAP = {
    1: '未分配',
    2: '未开始',
    3: '进行中',
    4: '验收中',
    5: '已打回',
    6: '已完成'
};

/**
 * 获取状态文本
 */
function getStatusText(status) {
    return STATUS_MAP[status] || '未知';
}

/**
 * 格式化日期
 */
function formatDate(dateStr) {
    if (!dateStr) return '--';
    try {
        const date = new Date(dateStr);
        if (Number.isNaN(date.getTime())) return '--';
        return date.toLocaleDateString('zh-CN');
    } catch {
        return '--';
    }
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateStr) {
    if (!dateStr) return '--';
    try {
        const date = new Date(dateStr);
        if (Number.isNaN(date.getTime())) return '--';
        return date.toLocaleString('zh-CN');
    } catch {
        return '--';
    }
}

/**
 * 检查是否超期
 */
function isOverdue(endTime) {
    if (!endTime) return false;
    try {
        const date = new Date(endTime);
        if (Number.isNaN(date.getTime())) return false;
        return date.getTime() < Date.now();
    } catch {
        return false;
    }
}

/**
 * 格式化剩余时间
 */
function formatRemaining(endTime) {
    if (!endTime) return '--';
    try {
        const date = new Date(endTime);
        if (Number.isNaN(date.getTime())) return '--';
        const diff = date.getTime() - Date.now();
        if (diff <= 0) return '已超期';

        const day = 24 * 60 * 60 * 1000;
        const hour = 60 * 60 * 1000;
        const minute = 60 * 1000;

        if (diff >= day) {
            const days = Math.floor(diff / day);
            return `${days} 天`;
        }

        const hours = Math.floor(diff / hour);
        const minutes = Math.floor((diff - hours * hour) / minute);
        if (hours === 0 && minutes === 0) return '不足 1 分钟';
        if (hours === 0) return `${minutes} 分钟`;
        return `${hours} 小时 ${minutes} 分钟`;
    } catch {
        return '--';
    }
}

/**
 * API 调用：获取任务列表
 */
async function fetchTasks() {
    try {
        const res = await fetch('/api/tesk');
        if (!res.ok) throw new Error('加载失败');
        const data = await res.json();
        return Array.isArray(data) ? data : [];
    } catch (err) {
        console.error('加载任务失败:', err);
        return [];
    }
}

/**
 * API 调用：获取单个任务
 */
async function fetchTask(taskId) {
    try {
        const res = await fetch(`/api/tesk/${taskId}`);
        if (!res.ok) throw new Error('加载失败');
        return await res.json();
    } catch (err) {
        console.error('加载任务失败:', err);
        return null;
    }
}

/**
 * API 调用：创建任务
 */
async function createTask(taskData) {
    try {
        const res = await fetch('/api/tesk', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '创建失败');
        }
        return await res.json();
    } catch (err) {
        console.error('创建任务失败:', err);
        throw err;
    }
}

/**
 * API 调用：更新任务
 */
async function updateTask(taskId, taskData) {
    try {
        const res = await fetch(`/api/tesk/${taskId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '更新失败');
        }
        return await res.json();
    } catch (err) {
        console.error('更新任务失败:', err);
        throw err;
    }
}

/**
 * API 调用：撤回任务
 */
async function withdrawTask(taskId) {
    try {
        const res = await fetch(`/api/tesk/${taskId}`, {
            method: 'DELETE'
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '撤回失败');
        }
    } catch (err) {
        console.error('撤回任务失败:', err);
        throw err;
    }
}

/**
 * API 调用：分配任务
 */
async function assignTask(taskId, assigneeId) {
    try {
        const res = await fetch(`/api/tesk/${taskId}/assign`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ assigneeId })
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '分配失败');
        }
        return await res.json();
    } catch (err) {
        console.error('分配任务失败:', err);
        throw err;
    }
}

/**
 * API 调用：审核任务
 */
async function reviewTask(taskId, decision, comment) {
    try {
        const res = await fetch(`/api/tesk/${taskId}/review`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ decision, comment })
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '审核失败');
        }
        return await res.json();
    } catch (err) {
        console.error('审核任务失败:', err);
        throw err;
    }
}

/**
 * API 调用：接受打回的任务
 */
async function acceptRejectedTask(taskId) {
    try {
        const res = await fetch(`/api/tesk/${taskId}/accept-reject`, {
            method: 'POST'
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '接受失败');
        }
        return await res.json();
    } catch (err) {
        console.error('接受打回任务失败:', err);
        throw err;
    }
}

/**
 * API 调用：提交任务验收
 */
async function submitTask(taskId, comment) {
    try {
        const res = await fetch(`/api/tesk/${taskId}/submit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ comment })
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || '提交失败');
        }
        return await res.json();
    } catch (err) {
        console.error('提交任务失败:', err);
        throw err;
    }
}

/**
 * 获取用户名（从用户 ID 获取）
 */
async function getUserName(userId) {
    if (!userId) return '--';
    try {
        const res = await fetch(`/api/users/${userId}`);
        if (!res.ok) return `用户 ${userId}`;
        const user = await res.json();
        return user.username || `用户 ${userId}`;
    } catch {
        return `用户 ${userId}`;
    }
}

/**
 * 获取任务可执行的操作
 */
function getAvailableActions(task, userRole) {
    const actions = {
        primary: [],
        secondary: []
    };

    const status = task.status;

    if (userRole === 'GUEST') {
        // Guest 操作
        if (status === 1) {
            actions.secondary.push({ label: '撤回', action: 'withdraw', danger: true });
        }
        if (status === 4) {
            actions.primary.push({ label: '通过', action: 'approve' });
            actions.primary.push({ label: '打回', action: 'reject', danger: true });
        }
        actions.secondary.push({ label: '修改', action: 'edit' });
    } else if (userRole === 'MEMBER') {
        // Member 操作
        if (status === 2 || status === 3) {
            actions.primary.push({ label: '提交验收', action: 'submit' });
            actions.secondary.push({ label: '修改进度', action: 'updateStatus' });
        }
        if (status === 5) {
            actions.primary.push({ label: '接受打回', action: 'acceptReject' });
        }
    } else if (userRole === 'MANAGER') {
        // Manager 操作
        if (status === 1) {
            actions.primary.push({ label: '分配', action: 'assign' });
        }
        actions.secondary.push({ label: '修改', action: 'edit' });
        actions.secondary.push({ label: '撤回', action: 'withdraw', danger: true });
    }

    return actions;
}

/**
 * 显示确认对话框
 */
function showConfirmDialog(title, message, onConfirm, onCancel) {
    const dialog = document.createElement('div');
    dialog.className = 'confirm-dialog-overlay';
    dialog.innerHTML = `
        <div class="confirm-dialog">
            <div class="confirm-dialog-header">
                <h3>${title}</h3>
            </div>
            <div class="confirm-dialog-body">
                <p>${message}</p>
            </div>
            <div class="confirm-dialog-footer">
                <button class="btn-cancel">取消</button>
                <button class="btn-confirm">确认</button>
            </div>
        </div>
    `;

    document.body.appendChild(dialog);

    const confirmBtn = dialog.querySelector('.btn-confirm');
    const cancelBtn = dialog.querySelector('.btn-cancel');

    confirmBtn.addEventListener('click', () => {
        dialog.remove();
        onConfirm();
    });

    cancelBtn.addEventListener('click', () => {
        dialog.remove();
        if (onCancel) onCancel();
    });

    dialog.addEventListener('click', (e) => {
        if (e.target === dialog) {
            dialog.remove();
            if (onCancel) onCancel();
        }
    });
}