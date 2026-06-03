(function () {
    async function fetchCurrentUser() {
        try {
            const res = await fetch('/api/auth/me');
            if (!res.ok) return null;
            return await res.json();
        } catch {
            return null;
        }
    }

    function buildHeader(user) {
        const homeUrl = document.body.dataset.homeUrl || '/index';
        const loginUrl = document.body.dataset.loginUrl || '/login';
        const accountLabel = user ? `用户 #${user.id}` : '未登录';

        const header = document.createElement('header');
        header.className = 'global-header';
        header.innerHTML = `
            <a class="brand" href="${homeUrl}">
                <span class="brand-badge">W</span>
                <span>Worklist</span>
            </a>
            <div class="header-actions">
                <a class="nav-link" href="${homeUrl}">返回首页</a>
                ${user ? `
                    <div class="account-box">
                        <span>账户</span>
                        <span class="account-name">${accountLabel}</span>
                    </div>
                    <button class="logout-btn" type="button" id="logout-btn">退出登录</button>
                ` : `
                    <a class="login-btn" href="${loginUrl}">登录</a>
                `}
            </div>
        `;
        return header;
    }

    async function mount() {
        const app = document.getElementById('app');
        if (!app) return;

        if (!document.getElementById('global-header-style')) {
            const link = document.createElement('link');
            link.id = 'global-header-style';
            link.rel = 'stylesheet';
            link.href = 'global-header.css';
            document.head.appendChild(link);
        }

        document.body.classList.add('has-global-header');
        if (document.body.dataset.pageType === 'auth') {
            document.body.classList.add('auth-page');
        }
        const user = await fetchCurrentUser();
        const header = buildHeader(user);
        document.body.insertBefore(header, app);

        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', async () => {
                await fetch('/logout', { method: 'POST' });
                window.location.href = '/login';
            });
        }
    }

    document.addEventListener('DOMContentLoaded', mount);
})();
