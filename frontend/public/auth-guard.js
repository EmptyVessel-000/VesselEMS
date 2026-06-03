async function checkAccess(requiredRoles) {
    try {
        const res = await fetch('/api/auth/me');
        if (!res.ok) {
            window.location.href = '/login';
            return false;
        }

        const user = await res.json();
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');

        if (user.role === 'manager') return true;

        if (!id || parseInt(id, 10) <= 0 || parseInt(id, 10) !== parseInt(user.id, 10)) {
            window.location.href = '/403.html';
            return false;
        }

        if (requiredRoles && !requiredRoles.includes(user.role)) {
            window.location.href = '/403.html';
            return false;
        }

        return true;
    } catch (error) {
        console.error('Access check failed:', error);
        window.location.href = '/login';
        return false;
    }
}
