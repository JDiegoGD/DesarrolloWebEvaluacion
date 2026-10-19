// Cliente común para /api/**: envía el token CSRF y gestiona 401 / 403.
// Spring Security deja el token en la cookie XSRF-TOKEN (legible por JS) y lo espera en el header X-XSRF-TOKEN.
(function () {
    const METODOS_SEGUROS = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];

    function leerCookie(nombre) {
        const par = document.cookie.split('; ').find(c => c.startsWith(nombre + '='));
        return par ? decodeURIComponent(par.substring(nombre.length + 1)) : null;
    }

    function mostrarAviso(mensaje) {
        let aviso = document.getElementById('aviso-seguridad');
        if (!aviso) {
            aviso = document.createElement('div');
            aviso.id = 'aviso-seguridad';
            aviso.style.cssText = 'position:fixed;top:16px;right:16px;z-index:9999;max-width:380px;padding:12px 16px;'
                + 'border-radius:8px;background:#fce8e6;color:#c5221f;font:14px Arial,sans-serif;'
                + 'box-shadow:0 4px 12px rgba(0,0,0,0.15)';
            document.body.appendChild(aviso);
        }
        aviso.textContent = mensaje;
        clearTimeout(aviso._timer);
        aviso._timer = setTimeout(() => aviso.remove(), 6000);
    }

    async function apiFetch(url, opciones = {}) {
        const metodo = (opciones.method || 'GET').toUpperCase();
        const headers = new Headers(opciones.headers || {});
        if (!METODOS_SEGUROS.includes(metodo)) {
            const token = leerCookie('XSRF-TOKEN');
            if (token) headers.set('X-XSRF-TOKEN', token);
        }
        const res = await fetch(url, { credentials: 'same-origin', ...opciones, headers });

        if (res.status === 401) {
            // Sesión vencida o usuario desactivado: volver al login
            window.location.href = '/login?expirado';
        } else if (res.status === 403) {
            let mensaje = 'No tienes permiso para realizar esta operación.';
            try { mensaje = (await res.clone().json()).mensaje || mensaje; } catch (e) { /* cuerpo no JSON */ }
            mostrarAviso(mensaje);
        }
        return res;
    }

    window.apiFetch = apiFetch;
})();
