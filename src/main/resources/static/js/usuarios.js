// Gestión de usuarios (/usuarios, solo ADMINISTRADOR). Todas las llamadas pasan por apiFetch (api.js).
// Los datos de la API se insertan con textContent / new Option: nunca se concatenan en innerHTML.
(function () {
    const $ = id => document.getElementById(id);

    let rolesActivos = [];
    let usuarios = new Map();     // id -> UsuarioResponseDTO de la lista visible
    let yo = null;                // nombreUsuario autenticado (GET /api/cuenta/me)
    let editando = null;          // usuario abierto en el modal de edición
    let accionConfirmar = null;   // función que ejecuta el botón Confirmar
    let ultimaCarga = 0;          // descarta respuestas de listados antiguos
    let modalAbierto = null;
    let focoPrevio = null;

    const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // ---------- Utilidades ----------

    // La API responde 400/404/409 con texto plano (GlobalExceptionHandler) y 401/403 con JSON {mensaje}
    async function leerMensaje(res, porDefecto) {
        let texto = '';
        try { texto = await res.text(); } catch (e) { /* sin cuerpo */ }
        try {
            const json = JSON.parse(texto);
            if (json && json.mensaje) return json.mensaje;
        } catch (e) { /* no es JSON */ }
        return texto || porDefecto;
    }

    function mostrarMensaje(el, texto, ok) {
        el.textContent = texto;
        el.className = 'msg ' + (ok ? 'msg-ok' : 'msg-err');
    }

    function ocultarMensaje(el) {
        el.textContent = '';
        el.className = 'msg';
    }

    function marcarErrores(prefijo, campos, errores) {
        campos.forEach(campo => {
            const input = $(prefijo + campo);
            const span = $('err-' + prefijo + campo);
            const error = errores[campo] || '';
            if (span) span.textContent = error;
            if (input) {
                input.classList.toggle('invalido', !!error);
                input.setAttribute('aria-invalid', error ? 'true' : 'false');
            }
        });
        const primero = campos.find(c => errores[c]);
        if (primero) $(prefijo + primero).focus();
        return !primero;
    }

    function validarPassword(errores, password, confirmar, obligatoria) {
        if (!password) {
            if (obligatoria) errores.Password = 'La contraseña es obligatoria.';
            else if (confirmar) errores.Password = 'Escribe la nueva contraseña o deja ambos campos vacíos.';
        } else if (password.length < 8 || password.length > 72) {
            errores.Password = 'La contraseña debe tener entre 8 y 72 caracteres.';
        }
        if ((password || confirmar) && password !== confirmar) errores.Confirmar = 'Las contraseñas no coinciden.';
    }

    function validarComunes(errores, nombres, correo, rolId) {
        if (!nombres) errores.Nombres = 'Los nombres son obligatorios.';
        else if (nombres.length > 150) errores.Nombres = 'Máximo 150 caracteres.';
        if (correo && (!CORREO.test(correo) || correo.length > 150)) errores.Correo = 'El correo no tiene un formato válido.';
        if (!rolId) errores.Rol = 'Selecciona un rol.';
    }

    function llenarSelectRoles(select, roles, primera) {
        select.replaceChildren();
        if (primera) select.add(new Option(primera, ''));
        roles.forEach(r => select.add(new Option(r.activo === false ? `${r.nombre} (inactivo)` : r.nombre, r.id)));
    }

    function esYo(u) {
        return yo !== null && u.nombreUsuario.toLowerCase() === yo.toLowerCase();
    }

    // ---------- Carga inicial ----------

    async function cargarCuenta() {
        const res = await apiFetch('/api/cuenta/me');
        if (res.ok) yo = (await res.json()).nombreUsuario;
    }

    async function cargarRoles() {
        // Registro y edición: solo roles activos. Filtro: todos los roles
        const [resActivos, resTodos] = await Promise.all([
            apiFetch('/api/roles?activo=true'),
            apiFetch('/api/roles')
        ]);
        if (resActivos.ok) {
            rolesActivos = await resActivos.json();
            llenarSelectRoles($('regRol'), rolesActivos, '-- Seleccionar --');
        } else if (resActivos.status !== 401) {
            mostrarMensaje($('msgRegistro'), await leerMensaje(resActivos, 'No se pudieron cargar los roles.'), false);
        }
        if (resTodos.ok) llenarSelectRoles($('filtroRol'), await resTodos.json(), 'Todos');
    }

    // ---------- Listado ----------

    function filaMensaje(texto) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 7;
        td.className = 'vacio';
        td.textContent = texto;
        tr.appendChild(td);
        return tr;
    }

    function celda(texto) {
        const td = document.createElement('td');
        td.textContent = texto ?? '';
        return td;
    }

    function badge(texto, ...clases) {
        const span = document.createElement('span');
        span.className = ['badge', ...clases].join(' ');
        span.textContent = texto;
        return span;
    }

    function boton(texto, clase, onClick) {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'btn btn-fila ' + clase;
        b.textContent = texto;
        b.addEventListener('click', onClick);
        return b;
    }

    function crearFila(u) {
        const tr = document.createElement('tr');
        tr.dataset.id = u.id;
        const propio = esYo(u);
        if (propio) tr.className = 'fila-propia';

        tr.appendChild(celda(u.id));
        const tdUsuario = celda(u.nombreUsuario);
        if (propio) {
            const tu = document.createElement('span');
            tu.className = 'tu';
            tu.textContent = ' (tú)';
            tdUsuario.appendChild(tu);
        }
        tr.appendChild(tdUsuario);
        tr.appendChild(celda(u.nombres));
        tr.appendChild(celda(u.correo));

        const tdRol = document.createElement('td');
        const claseRol = 'badge-' + String(u.rolNombre || '').toLowerCase().replace(/[^a-z]/g, '');
        tdRol.appendChild(badge(u.rolNombre ?? '-', 'badge-rol', claseRol));
        tr.appendChild(tdRol);

        const tdEstado = document.createElement('td');
        tdEstado.appendChild(u.activo ? badge('Activo', 'badge-activo') : badge('Inactivo', 'badge-inactivo'));
        tr.appendChild(tdEstado);

        const tdAcciones = document.createElement('td');
        tdAcciones.className = 'celda-acciones';
        const btnEditar = boton('Editar', 'btn-editar', e => abrirEditar(u.id, e.currentTarget));
        btnEditar.setAttribute('aria-label', `Editar al usuario ${u.nombreUsuario}`);
        tdAcciones.appendChild(btnEditar);

        const btnEstado = boton(u.activo ? 'Desactivar' : 'Activar', u.activo ? 'btn-desactivar' : 'btn-activar',
            e => pedirCambioEstado(u.id, e.currentTarget));
        btnEstado.setAttribute('aria-label', `${u.activo ? 'Desactivar' : 'Activar'} al usuario ${u.nombreUsuario}`);
        if (propio && u.activo) {
            btnEstado.disabled = true;
            btnEstado.title = 'No puedes desactivar tu propia cuenta.';
        }
        tdAcciones.appendChild(btnEstado);
        tr.appendChild(tdAcciones);
        return tr;
    }

    function renderTabla() {
        const tbody = $('bodyUsuarios');
        if (!usuarios.size) {
            tbody.replaceChildren(filaMensaje('No hay usuarios que coincidan.'));
            return;
        }
        tbody.replaceChildren(...[...usuarios.values()].map(crearFila));
    }

    function parametrosFiltro() {
        const p = new URLSearchParams();
        const rolId = $('filtroRol').value;
        const activo = $('filtroEstado').value;
        const q = $('filtroTexto').value.trim();
        if (rolId) p.set('rolId', rolId);
        if (activo) p.set('activo', activo);
        if (q) p.set('q', q);
        return p.toString();
    }

    async function cargarUsuarios() {
        const id = ++ultimaCarga;
        $('bodyUsuarios').replaceChildren(filaMensaje('Cargando...'));
        const qs = parametrosFiltro();
        const res = await apiFetch('/api/usuarios' + (qs ? '?' + qs : ''));
        if (id !== ultimaCarga) return;
        if (!res.ok) {
            if (res.status === 401) return;
            const texto = await leerMensaje(res, 'No se pudo cargar la lista de usuarios.');
            $('bodyUsuarios').replaceChildren(filaMensaje(texto));
            return;
        }
        const lista = await res.json();
        usuarios = new Map(lista.map(u => [u.id, u]));
        renderTabla();
    }

    // ---------- Registro ----------

    async function registrar(e) {
        e.preventDefault();
        const msg = $('msgRegistro');
        ocultarMensaje(msg);
        const datos = {
            nombreUsuario: $('regNombreUsuario').value.trim(),
            nombres: $('regNombres').value.trim(),
            correo: $('regCorreo').value.trim(),
            password: $('regPassword').value,
            rolId: Number($('regRol').value) || null
        };
        const errores = {};
        if (datos.nombreUsuario.length < 3 || datos.nombreUsuario.length > 100)
            errores.NombreUsuario = 'Debe tener entre 3 y 100 caracteres.';
        else if (/^\d+$/.test(datos.nombreUsuario))
            errores.NombreUsuario = 'No puede estar formado solo por números.';
        validarComunes(errores, datos.nombres, datos.correo, datos.rolId);
        validarPassword(errores, datos.password, $('regConfirmar').value, true);
        if (!marcarErrores('reg', ['NombreUsuario', 'Nombres', 'Correo', 'Rol', 'Password', 'Confirmar'], errores)) return;

        const btn = $('btnRegistrar');
        btn.disabled = true;
        try {
            const res = await apiFetch('/api/usuarios', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...datos, correo: datos.correo || null })
            });
            $('regPassword').value = '';
            $('regConfirmar').value = '';
            if (res.status === 201) {
                const creado = await res.json();
                $('formRegistro').reset();
                mostrarMensaje(msg, `Usuario "${creado.nombreUsuario}" registrado correctamente con rol ${creado.rolNombre}.`, true);
                $('regNombreUsuario').focus();
                cargarUsuarios();
            } else if (res.status !== 401) {
                mostrarMensaje(msg, await leerMensaje(res, 'No se pudo registrar el usuario.'), false);
            }
        } finally {
            btn.disabled = false;
        }
    }

    // ---------- Modales ----------

    function abrirModal(id, foco, origen) {
        modalAbierto = $(id);
        focoPrevio = origen || document.activeElement;
        modalAbierto.classList.add('abierto');
        foco.focus();
    }

    function cerrarModal() {
        if (!modalAbierto) return;
        modalAbierto.classList.remove('abierto');
        if (modalAbierto.id === 'modalEditar') {
            $('edPassword').value = '';
            $('edConfirmar').value = '';
            editando = null;
        }
        accionConfirmar = null;
        modalAbierto = null;
        if (focoPrevio && document.contains(focoPrevio)) focoPrevio.focus();
    }

    function teclado(e) {
        if (!modalAbierto) return;
        if (e.key === 'Escape') {
            e.preventDefault();
            cerrarModal();
        } else if (e.key === 'Tab') {
            // Mantiene el foco dentro del modal
            const enfocables = [...modalAbierto.querySelectorAll('input:not([readonly]), select, button')]
                .filter(el => !el.disabled);
            if (!enfocables.length) return;
            const primero = enfocables[0], ultimo = enfocables[enfocables.length - 1];
            if (e.shiftKey && document.activeElement === primero) { e.preventDefault(); ultimo.focus(); }
            else if (!e.shiftKey && document.activeElement === ultimo) { e.preventDefault(); primero.focus(); }
        }
    }

    // ---------- Edición ----------

    async function abrirEditar(id, origen) {
        const res = await apiFetch(`/api/usuarios/${id}`);
        if (!res.ok) {
            if (res.status !== 401)
                mostrarMensaje($('msgLista'), await leerMensaje(res, 'No se pudo obtener el usuario.'), false);
            if (res.status === 404) cargarUsuarios();
            return;
        }
        editando = await res.json();
        ocultarMensaje($('msgEditar'));
        marcarErrores('ed', ['Nombres', 'Correo', 'Rol', 'Password', 'Confirmar'], {});

        $('tituloEditar').textContent = `Editar usuario: ${editando.nombreUsuario}`;
        $('edNombreUsuario').value = editando.nombreUsuario;
        $('edNombres').value = editando.nombres ?? '';
        $('edCorreo').value = editando.correo ?? '';
        $('edPassword').value = '';
        $('edConfirmar').value = '';

        // Si su rol actual está inactivo se muestra igual (el backend permite conservarlo, no asignarlo)
        const roles = [...rolesActivos];
        if (!roles.some(r => r.id === editando.rolId))
            roles.push({ id: editando.rolId, nombre: editando.rolNombre, activo: false });
        const selRol = $('edRol');
        llenarSelectRoles(selRol, roles);
        selRol.value = String(editando.rolId);
        const propio = esYo(editando);
        selRol.disabled = propio;
        selRol.title = propio ? 'No puedes cambiar tu propio rol desde esta pantalla.' : '';

        abrirModal('modalEditar', $('edNombres'), origen);
    }

    async function guardarEdicion(e) {
        e.preventDefault();
        if (!editando) return;
        const msg = $('msgEditar');
        ocultarMensaje(msg);
        const nombres = $('edNombres').value.trim();
        const correo = $('edCorreo').value.trim();
        const rolId = Number($('edRol').value) || null;
        const password = $('edPassword').value;
        const errores = {};
        validarComunes(errores, nombres, correo, rolId);
        validarPassword(errores, password, $('edConfirmar').value, false);
        if (!marcarErrores('ed', ['Nombres', 'Correo', 'Rol', 'Password', 'Confirmar'], errores)) return;

        const cambiaDatos = nombres !== (editando.nombres ?? '')
            || correo.toLowerCase() !== (editando.correo ?? '').toLowerCase()
            || password !== '';
        const cambiaRol = rolId !== editando.rolId;
        if (!cambiaDatos && !cambiaRol) {
            mostrarMensaje(msg, 'No hay cambios que guardar.', false);
            return;
        }

        // Solo cambia el rol: PATCH /rol (la auditoría registra el rol anterior y el nuevo).
        // Cualquier otro cambio: un único PUT con todos los campos (una transacción, una fila de auditoría)
        const opciones = cambiaDatos
            ? { url: `/api/usuarios/${editando.id}`, method: 'PUT',
                body: { nombres, correo: correo || null, rolId, ...(password ? { password } : {}) } }
            : { url: `/api/usuarios/${editando.id}/rol`, method: 'PATCH', body: { rolId } };

        const btn = $('btnGuardar');
        btn.disabled = true;
        try {
            const res = await apiFetch(opciones.url, {
                method: opciones.method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(opciones.body)
            });
            $('edPassword').value = '';
            $('edConfirmar').value = '';
            if (res.ok) {
                const actualizado = await res.json();
                cerrarModal();
                mostrarMensaje($('msgLista'), `Usuario "${actualizado.nombreUsuario}" actualizado correctamente.`, true);
                cargarUsuarios();
            } else if (res.status !== 401) {
                mostrarMensaje(msg, await leerMensaje(res, 'No se pudo actualizar el usuario.'), false);
            }
        } finally {
            btn.disabled = false;
        }
    }

    // ---------- Activar / desactivar ----------

    function pedirCambioEstado(id, origen) {
        const u = usuarios.get(id);
        if (!u) return;
        if (u.activo && esYo(u)) return;
        const activar = !u.activo;
        $('tituloConfirmar').textContent = activar ? 'Activar usuario' : 'Desactivar usuario';
        $('textoConfirmar').textContent = activar
            ? `¿Activar al usuario "${u.nombreUsuario}"? Podrá volver a iniciar sesión.`
            : `¿Desactivar al usuario "${u.nombreUsuario}"? No podrá iniciar sesión y su sesión abierta se cerrará.`;
        const btn = $('btnConfirmar');
        btn.textContent = activar ? 'Activar' : 'Desactivar';
        btn.className = 'btn ' + (activar ? 'btn-search' : 'btn-peligro');
        ocultarMensaje($('msgConfirmar'));
        accionConfirmar = () => cambiarEstado(u, activar);
        abrirModal('modalConfirmar', btn, origen);
    }

    async function cambiarEstado(u, activo) {
        const btn = $('btnConfirmar');
        btn.disabled = true;
        try {
            const res = await apiFetch(`/api/usuarios/${u.id}/estado`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ activo })
            });
            if (res.ok) {
                const actualizado = await res.json();
                cerrarModal();
                mostrarMensaje($('msgLista'),
                    `Usuario "${actualizado.nombreUsuario}" ${actualizado.activo ? 'activado' : 'desactivado'}.`, true);
                if ($('filtroEstado').value) {
                    cargarUsuarios();   // con filtro de estado la fila ya no coincide
                } else {
                    usuarios.set(actualizado.id, actualizado);
                    const fila = $('bodyUsuarios').querySelector(`tr[data-id="${actualizado.id}"]`);
                    const nueva = crearFila(actualizado);
                    if (fila) fila.replaceWith(nueva); else renderTabla();
                    nueva.querySelector('.btn-desactivar, .btn-activar').focus();
                }
            } else if (res.status !== 401) {
                mostrarMensaje($('msgConfirmar'), await leerMensaje(res, 'No se pudo cambiar el estado.'), false);
            }
        } finally {
            btn.disabled = false;
        }
    }

    // ---------- Eventos ----------

    let temporizador;
    $('formRegistro').addEventListener('submit', registrar);
    $('formRegistro').addEventListener('reset', () => {
        marcarErrores('reg', ['NombreUsuario', 'Nombres', 'Correo', 'Rol', 'Password', 'Confirmar'], {});
    });
    $('formFiltros').addEventListener('submit', e => { e.preventDefault(); cargarUsuarios(); });
    $('filtroRol').addEventListener('change', cargarUsuarios);
    $('filtroEstado').addEventListener('change', cargarUsuarios);
    $('filtroTexto').addEventListener('input', () => {
        clearTimeout(temporizador);
        temporizador = setTimeout(cargarUsuarios, 350);
    });
    $('btnLimpiarFiltros').addEventListener('click', () => {
        $('formFiltros').reset();
        cargarUsuarios();
    });
    $('formEditar').addEventListener('submit', guardarEdicion);
    $('btnConfirmar').addEventListener('click', () => { if (accionConfirmar) accionConfirmar(); });
    document.querySelectorAll('[data-cerrar]').forEach(b => b.addEventListener('click', cerrarModal));
    document.querySelectorAll('.modal-overlay').forEach(m =>
        m.addEventListener('click', e => { if (e.target === m) cerrarModal(); }));
    document.addEventListener('keydown', teclado);

    // La cuenta se carga antes del listado para marcar la fila propia
    (async () => {
        await Promise.all([cargarCuenta(), cargarRoles()]);
        cargarUsuarios();
    })();
})();
