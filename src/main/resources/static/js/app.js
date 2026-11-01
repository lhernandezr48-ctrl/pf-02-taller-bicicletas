'use strict';

/* ================== Utilidades ================== */
const API = '';   // misma origen: el backend sirve esta página
const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];

const moneda = new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'GTQ' });
const dinero = (n) => moneda.format(Number(n ?? 0));

const NOMBRES_ESTADO = { PENDIENTE: 'Pendiente', EN_PROCESO: 'En proceso', TERMINADA: 'Terminada' };

function esc(texto) {
  return String(texto ?? '').replace(/[&<>"']/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function hoy() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

function toast(mensaje, tipo = 'ok') {
  const el = document.createElement('div');
  el.className = `toast toast--${tipo}`;
  el.textContent = mensaje;
  $('#toasts').appendChild(el);
  setTimeout(() => el.remove(), tipo === 'ok' ? 3500 : 6000);
}

/* ================== Cliente de la API ================== */
class ErrorApi extends Error {
  constructor(estado, mensaje, errores) {
    super(mensaje);
    this.estado = estado;
    this.errores = errores || {};
  }
}

async function api(metodo, ruta, cuerpo) {
  const opciones = { method: metodo, headers: {} };
  if (cuerpo !== undefined) {
    opciones.headers['Content-Type'] = 'application/json';
    opciones.body = JSON.stringify(cuerpo);
  }
  let respuesta;
  try {
    respuesta = await fetch(API + ruta, opciones);
  } catch (e) {
    throw new ErrorApi(0, 'No se pudo conectar con el servidor. Verifique que el backend esté en ejecución.');
  }
  if (respuesta.status === 204) return null;

  let datos = null;
  try { datos = await respuesta.json(); } catch (e) { /* sin cuerpo JSON */ }

  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status,
      (datos && datos.mensaje) || `Error ${respuesta.status}`,
      datos && datos.errores);
  }
  return datos;
}

/* ================== Estado de la pantalla ================== */
const estado = {
  servicios: [],
  filtro: { tipo: 'todas', valor: '' },
  ordenEditando: null,
  servicioEditando: null
};

/* ================== Pestañas ================== */
$$('.pestana').forEach((btn) => btn.addEventListener('click', () => {
  $$('.pestana').forEach((b) => b.classList.toggle('pestana--activa', b === btn));
  $('#tab-ordenes').hidden = btn.dataset.tab !== 'ordenes';
  $('#tab-servicios').hidden = btn.dataset.tab !== 'servicios';
}));

/* ================== Formularios: errores ================== */
function limpiarErrores(form, cajaGeneral) {
  $$('[data-error]', form).forEach((s) => (s.textContent = ''));
  $$('.campo-invalido', form).forEach((c) => c.classList.remove('campo-invalido'));
  cajaGeneral.hidden = true;
  cajaGeneral.textContent = '';
}

function mostrarErrores(form, cajaGeneral, error) {
  const campos = error.errores || {};
  let hayCampos = false;
  Object.entries(campos).forEach(([campo, mensaje]) => {
    const span = $(`[data-error="${campo}"]`, form);
    if (span) {
      span.textContent = mensaje;
      const input = form.elements[campo];
      if (input) input.classList.add('campo-invalido');
      hayCampos = true;
    }
  });
  cajaGeneral.textContent = hayCampos ? 'Revise los campos marcados.' : error.message;
  cajaGeneral.hidden = false;
}

$$('[data-cerrar]').forEach((b) => b.addEventListener('click', () => b.closest('dialog').close()));

/* ================== Servicios ================== */
async function cargarServicios() {
  try {
    estado.servicios = await api('GET', '/api/servicios');
    pintarServicios();
    pintarOpcionesServicio();
  } catch (e) {
    toast(e.message, 'error');
  }
}

function pintarServicios() {
  const tbody = $('#tbody-servicios');
  if (!estado.servicios.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="vacio">No hay servicios registrados.</td></tr>';
    return;
  }
  tbody.innerHTML = estado.servicios.map((s) => `
    <tr>
      <td>${s.id}</td>
      <td><strong>${esc(s.nombre)}</strong></td>
      <td>${esc(s.descripcion)}</td>
      <td class="num">${dinero(s.costoBase)}</td>
      <td><span class="insignia insignia--${s.activo ? 'ACTIVO' : 'INACTIVO'}">${s.activo ? 'Activo' : 'Desactivado'}</span></td>
      <td><div class="acciones">
        <button class="btn btn--secundario btn--chico" data-accion="editar-servicio" data-id="${s.id}">Editar</button>
        <button class="btn btn--peligro btn--chico" data-accion="desactivar-servicio" data-id="${s.id}" ${s.activo ? '' : 'disabled'}>Desactivar</button>
      </div></td>
    </tr>`).join('');
}

function abrirFormServicio(servicio) {
  const form = $('#form-servicio');
  estado.servicioEditando = servicio || null;
  form.reset();
  limpiarErrores(form, $('#servicio-error'));
  $('#dlg-servicio-titulo').textContent = servicio ? `Editar servicio #${servicio.id}` : 'Nuevo servicio';
  if (servicio) {
    form.nombre.value = servicio.nombre;
    form.descripcion.value = servicio.descripcion ?? '';
    form.costoBase.value = servicio.costoBase;
  }
  $('#dlg-servicio').showModal();
}

$('#btn-nuevo-servicio').addEventListener('click', () => abrirFormServicio(null));

$('#form-servicio').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const form = ev.target;
  limpiarErrores(form, $('#servicio-error'));
  const cuerpo = {
    nombre: form.nombre.value,
    descripcion: form.descripcion.value,
    costoBase: form.costoBase.value === '' ? null : Number(form.costoBase.value)
  };
  try {
    const editando = estado.servicioEditando;
    if (editando) {
      await api('PUT', `/api/servicios/${editando.id}`, cuerpo);
      toast('Servicio actualizado correctamente.');
    } else {
      await api('POST', '/api/servicios', cuerpo);
      toast('Servicio registrado correctamente.');
    }
    $('#dlg-servicio').close();
    await cargarServicios();
  } catch (e) {
    mostrarErrores(form, $('#servicio-error'), e);
  }
});

$('#tbody-servicios').addEventListener('click', async (ev) => {
  const btn = ev.target.closest('button[data-accion]');
  if (!btn) return;
  const servicio = estado.servicios.find((s) => s.id === Number(btn.dataset.id));
  if (btn.dataset.accion === 'editar-servicio') {
    abrirFormServicio(servicio);
  } else if (btn.dataset.accion === 'desactivar-servicio') {
    if (!confirm(`¿Desactivar el servicio "${servicio.nombre}"? Ya no podrá usarse en nuevas órdenes.`)) return;
    try {
      await api('PATCH', `/api/servicios/${servicio.id}/desactivar`);
      toast('Servicio desactivado.');
      await cargarServicios();
    } catch (e) {
      toast(e.message, 'error');
    }
  }
});

/* ================== Órdenes ================== */
function pintarOpcionesServicio(seleccionado) {
  const select = $('#form-orden').servicioId;
  const activos = estado.servicios.filter((s) => s.activo);
  select.innerHTML = '<option value="">Seleccione un servicio…</option>' + activos.map((s) =>
    `<option value="${s.id}">${esc(s.nombre)} · ${dinero(s.costoBase)}</option>`).join('');
  if (seleccionado) select.value = String(seleccionado);
}

async function cargarIndicador() {
  try {
    const r = await api('GET', '/api/ordenes/pendientes/cantidad');
    $('#indicador-pendientes').textContent = r.cantidadPendientes;
  } catch (e) {
    $('#indicador-pendientes').textContent = '–';
  }
}

async function cargarOrdenes() {
  const { tipo, valor } = estado.filtro;
  let ruta = '/api/ordenes';
  if (tipo === 'estado') ruta = `/api/ordenes/estado/${encodeURIComponent(valor)}`;
  if (tipo === 'cliente') ruta = `/api/ordenes/cliente/${encodeURIComponent(valor)}`;
  try {
    const ordenes = await api('GET', ruta);
    pintarOrdenes(ordenes);
    await actualizarTotalCliente();
  } catch (e) {
    pintarOrdenes([]);
    toast(e.message, 'error');
  }
}

async function actualizarTotalCliente() {
  const tarjeta = $('#tarjeta-total');
  if (estado.filtro.tipo !== 'cliente') { tarjeta.hidden = true; return; }
  try {
    const r = await api('GET', `/api/ordenes/cliente/${encodeURIComponent(estado.filtro.valor)}/total`);
    $('#total-titulo').textContent = `Total a pagar · "${r.cliente}"`;
    $('#total-valor').textContent = dinero(r.totalEstimado);
    $('#total-detalle').textContent = `${r.cantidadOrdenes} orden(es) encontradas`;
    tarjeta.hidden = false;
  } catch (e) {
    tarjeta.hidden = true;
  }
}

function botonesEstado(o) {
  const b = (nuevo, texto) =>
    `<button class="btn btn--secundario btn--chico" data-accion="estado" data-id="${o.id}" data-estado="${nuevo}">${texto}</button>`;
  if (o.estado === 'PENDIENTE') return b('EN_PROCESO', 'Iniciar') + b('TERMINADA', 'Terminar');
  if (o.estado === 'EN_PROCESO') return b('TERMINADA', 'Terminar') + b('PENDIENTE', 'A pendiente');
  return '';
}

function pintarOrdenes(ordenes) {
  const tbody = $('#tbody-ordenes');
  if (!ordenes.length) {
    tbody.innerHTML = '<tr><td colspan="9" class="vacio">No hay órdenes para mostrar.</td></tr>';
    tbody._ordenes = [];
    return;
  }
  tbody._ordenes = ordenes;
  tbody.innerHTML = ordenes.map((o) => `
    <tr>
      <td>${o.id}</td>
      <td><strong>${esc(o.cliente)}</strong></td>
      <td>${esc(o.bicicleta)}</td>
      <td class="nowrap">${esc(o.fecha)}</td>
      <td>${esc(o.servicioNombre)}</td>
      <td class="num">${dinero(o.recargo)}</td>
      <td class="num"><strong>${dinero(o.costoEstimado)}</strong></td>
      <td><span class="insignia insignia--${o.estado}">${NOMBRES_ESTADO[o.estado] ?? esc(o.estado)}</span></td>
      <td><div class="acciones">
        ${botonesEstado(o)}
        <button class="btn btn--texto btn--chico" data-accion="editar" data-id="${o.id}" ${o.estado === 'TERMINADA' ? 'disabled title="Una orden terminada no se modifica"' : ''}>Editar</button>
        <button class="btn btn--peligro btn--chico" data-accion="eliminar" data-id="${o.id}" ${o.estado === 'PENDIENTE' ? '' : 'disabled title="Solo se eliminan órdenes pendientes"'}>Eliminar</button>
      </div></td>
    </tr>`).join('');
}

async function refrescarOrdenes() {
  await Promise.all([cargarOrdenes(), cargarIndicador()]);
}

/* ---- Filtros ---- */
$('#filtro-estado').addEventListener('change', (ev) => {
  const v = ev.target.value;
  $('#filtro-cliente').value = '';
  estado.filtro = v ? { tipo: 'estado', valor: v } : { tipo: 'todas', valor: '' };
  cargarOrdenes();
});

function buscarCliente() {
  const v = $('#filtro-cliente').value.trim();
  if (!v) { toast('Escriba el nombre de un cliente para buscar.', 'error'); return; }
  $('#filtro-estado').value = '';
  estado.filtro = { tipo: 'cliente', valor: v };
  cargarOrdenes();
}
$('#btn-buscar-cliente').addEventListener('click', buscarCliente);
$('#filtro-cliente').addEventListener('keydown', (ev) => { if (ev.key === 'Enter') buscarCliente(); });

$('#btn-limpiar').addEventListener('click', () => {
  $('#filtro-estado').value = '';
  $('#filtro-cliente').value = '';
  estado.filtro = { tipo: 'todas', valor: '' };
  cargarOrdenes();
});

/* ---- Formulario de orden ---- */
function calcularCostoPrevio() {
  const form = $('#form-orden');
  const servicio = estado.servicios.find((s) => s.id === Number(form.servicioId.value));
  const base = servicio ? Number(servicio.costoBase) : 0;
  const recargo = Number(form.recargo.value) || 0;
  $('#costo-previo').textContent = dinero(base + recargo);
}
$('#form-orden').servicioId.addEventListener('change', calcularCostoPrevio);
$('#form-orden').recargo.addEventListener('input', calcularCostoPrevio);

function abrirFormOrden(orden) {
  const form = $('#form-orden');
  estado.ordenEditando = orden || null;
  form.reset();
  limpiarErrores(form, $('#orden-error'));
  $('#dlg-orden-titulo').textContent = orden ? `Editar orden #${orden.id}` : 'Nueva orden';
  pintarOpcionesServicio(orden ? orden.servicioId : null);
  if (orden) {
    form.cliente.value = orden.cliente;
    form.bicicleta.value = orden.bicicleta;
    form.fecha.value = orden.fecha;
    form.recargo.value = orden.recargo;
    form.observaciones.value = orden.observaciones ?? '';
  } else {
    form.fecha.value = hoy();
    form.recargo.value = 0;
  }
  calcularCostoPrevio();
  $('#dlg-orden').showModal();
}

$('#btn-nueva-orden').addEventListener('click', () => abrirFormOrden(null));

$('#form-orden').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const form = ev.target;
  limpiarErrores(form, $('#orden-error'));
  const cuerpo = {
    cliente: form.cliente.value,
    bicicleta: form.bicicleta.value,
    fecha: form.fecha.value || null,
    observaciones: form.observaciones.value,
    recargo: form.recargo.value === '' ? null : Number(form.recargo.value),
    servicioId: form.servicioId.value === '' ? null : Number(form.servicioId.value)
  };
  try {
    const editando = estado.ordenEditando;
    if (editando) {
      await api('PUT', `/api/ordenes/${editando.id}`, cuerpo);
      toast('Orden actualizada correctamente.');
    } else {
      await api('POST', '/api/ordenes', cuerpo);
      toast('Orden registrada correctamente.');
    }
    $('#dlg-orden').close();
    await refrescarOrdenes();
  } catch (e) {
    mostrarErrores(form, $('#orden-error'), e);
  }
});

/* ---- Acciones de la tabla ---- */
$('#tbody-ordenes').addEventListener('click', async (ev) => {
  const btn = ev.target.closest('button[data-accion]');
  if (!btn) return;
  const id = Number(btn.dataset.id);
  const orden = ($('#tbody-ordenes')._ordenes || []).find((o) => o.id === id);

  try {
    if (btn.dataset.accion === 'editar') {
      abrirFormOrden(orden);
    } else if (btn.dataset.accion === 'estado') {
      await api('PATCH', `/api/ordenes/${id}/estado`, { estado: btn.dataset.estado });
      toast(`Orden #${id} cambió a ${NOMBRES_ESTADO[btn.dataset.estado]}.`);
      await refrescarOrdenes();
    } else if (btn.dataset.accion === 'eliminar') {
      if (!confirm(`¿Eliminar la orden #${id} de ${orden ? orden.cliente : ''}?`)) return;
      await api('DELETE', `/api/ordenes/${id}`);
      toast(`Orden #${id} eliminada.`);
      await refrescarOrdenes();
    }
  } catch (e) {
    toast(e.message, 'error');
    await refrescarOrdenes();
  }
});

/* ================== Inicio ================== */
if (location.protocol === 'file:') $('#aviso-archivo').hidden = false;

(async function iniciar() {
  await cargarServicios();
  await refrescarOrdenes();
})();
