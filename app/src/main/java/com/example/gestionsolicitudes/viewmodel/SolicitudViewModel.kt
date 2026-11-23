package com.example.gestionsolicitudes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.R
import com.example.gestionsolicitudes.data.local.AppDatabase
import com.example.gestionsolicitudes.data.remote.RetrofitInstance
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.Categoria
import com.example.gestionsolicitudes.model.ServicioCatalogo
import com.example.gestionsolicitudes.repository.ServicioRepository
import com.example.gestionsolicitudes.model.ErroresSolicitud
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.EventoHistorial
import com.example.gestionsolicitudes.model.FormularioSolicitud
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Prioridad
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.SolicitudUiState
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val repositorio = SolicitudRepository(db.solicitudDao(), db.usuarioDao())
    private val catalogoRemoto = ServicioRepository(
        projectId = app.getString(R.string.project_id),
        apiKey = app.getString(R.string.google_api_key),
        api = RetrofitInstance.crear(app)
    )

    private val _estado = MutableStateFlow(SolicitudUiState())
    val estado: StateFlow<SolicitudUiState> = _estado.asStateFlow()

    val evidenciasFicticias = listOf("captura_error_demo.png", "documento_demo.pdf")

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    init {
        viewModelScope.launch {
            repositorio.solicitudes.collect { lista ->
                _estado.update { it.copy(solicitudes = lista) }
            }
        }
        viewModelScope.launch {
            _estado.update { it.copy(clientes = repositorio.clientes()) }
        }
        viewModelScope.launch {
            try {
                val lista = catalogoRemoto.obtenerCatalogo()
                _estado.update { it.copy(catalogo = lista, errorCatalogo = null) }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(errorCatalogo = "No se pudo leer el catálogo: ${e.localizedMessage}")
                }
            }
        }
    }

    fun onClienteChange(clienteId: String) {
        _estado.update {
            it.copy(
                formulario = it.formulario.copy(
                    clienteId = clienteId,
                    errores = it.formulario.errores.copy(cliente = null)
                )
            )
        }
    }

    fun onTituloChange(nuevoTitulo: String) {
        _estado.update {
            it.copy(
                formulario = it.formulario.copy(
                    titulo = nuevoTitulo,
                    errores = it.formulario.errores.copy(titulo = null)
                )
            )
        }
    }

    fun onDescripcionChange(nuevaDescripcion: String) {
        _estado.update {
            it.copy(
                formulario = it.formulario.copy(
                    descripcion = nuevaDescripcion,
                    errores = it.formulario.errores.copy(descripcion = null)
                )
            )
        }
    }

    fun onCategoriaChange(nuevaCategoria: Categoria) {
        _estado.update {
            it.copy(
                formulario = it.formulario.copy(
                    categoria = nuevaCategoria,
                    errores = it.formulario.errores.copy(categoria = null)
                )
            )
        }
    }

    fun onServicioChange(nuevoServicio: String) {
        _estado.update { it.copy(formulario = it.formulario.copy(servicio = nuevoServicio)) }
    }

    fun onServicioCatalogo(servicio: ServicioCatalogo) {
        val categoria = Categoria.entries.find {
            it.etiqueta.equals(servicio.servicio, ignoreCase = true)
        }
        _estado.update {
            it.copy(
                formulario = it.formulario.copy(
                    servicio = servicio.servicio,
                    categoria = categoria ?: it.formulario.categoria,
                    errores = it.formulario.errores.copy(
                        categoria = if (categoria != null) null else it.formulario.errores.categoria
                    )
                )
            )
        }
    }

    fun onEvidenciaChange(nuevaEvidencia: String?) {
        _estado.update { it.copy(formulario = it.formulario.copy(evidencia = nuevaEvidencia)) }
    }

    fun seleccionarSolicitud(codigo: String) {
        _estado.update { it.copy(codigoSeleccionado = codigo) }
    }

    fun seleccionarSiExiste(texto: String): Boolean {
        val codigo = texto.trim()
        val solicitud = _estado.value.solicitudes.find {
            it.codigo.equals(codigo, ignoreCase = true)
        } ?: return false
        _estado.update { it.copy(codigoSeleccionado = solicitud.codigo) }
        return true
    }

    fun detalleAbierto() {
        _estado.update { it.copy(esperandoDetalle = false) }
    }

    fun enviarSolicitud(usuario: Usuario) {
        if (_estado.value.guardando) return

        val form = _estado.value.formulario
        val categoria = form.categoria
        val cliente = if (usuario.perfil == Perfil.ADMINISTRADOR) {
            _estado.value.clientes.find { it.id == form.clienteId }
        } else {
            usuario
        }

        val errores = ErroresSolicitud(
            cliente = if (usuario.perfil == Perfil.ADMINISTRADOR && cliente == null) "Selecciona un cliente" else null,
            titulo = if (form.titulo.isBlank()) "Campo obligatorio" else null,
            descripcion = if (form.descripcion.trim().length < 10) "Describe la necesidad (mínimo 10 caracteres)" else null,
            categoria = if (categoria == null) "Selecciona una categoría" else null
        )

        if (cliente == null || categoria == null || errores.titulo != null || errores.descripcion != null) {
            _estado.update { it.copy(formulario = it.formulario.copy(errores = errores)) }
            return
        }

        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                val codigo = repositorio.siguienteCodigo()
                val ahora = LocalDateTime.now().format(formatoFecha)
                val laCreoElAdmin = usuario.perfil == Perfil.ADMINISTRADOR

                val primerEvento = EventoHistorial(
                    id = "$codigo-E1",
                    estadoAnterior = null,
                    estadoNuevo = EstadoSolicitud.RECIBIDA,
                    accion = "Solicitud creada",
                    comentario = if (laCreoElAdmin) {
                        "El administrador registró la solicitud"
                    } else {
                        "El cliente registró la solicitud"
                    },
                    perfil = usuario.perfil,
                    usuario = usuario.nombre,
                    fechaHora = ahora,
                    evidencia = form.evidencia
                )

                val nueva = Solicitud(
                    codigo = codigo,
                    clienteId = cliente.id,
                    clienteNombre = cliente.nombre,
                    empresa = cliente.empresa,
                    titulo = form.titulo.trim(),
                    descripcion = form.descripcion.trim(),
                    categoria = categoria,
                    servicio = form.servicio.trim(),
                    estado = EstadoSolicitud.RECIBIDA,
                    evidencia = form.evidencia,
                    fechaCreacion = ahora,
                    fechaActualizacion = ahora,
                    historial = listOf(primerEvento)
                )

                repositorio.guardar(nueva)
                _estado.update {
                    it.copy(
                        formulario = FormularioSolicitud(),
                        codigoSeleccionado = codigo,
                        esperandoDetalle = true
                    )
                }
            } finally {
                _estado.update { it.copy(guardando = false) }
            }
        }
    }

    fun onBusquedaChange(texto: String) {
        _estado.update { it.copy(busqueda = texto) }
    }

    fun onFiltroEstado(estado: EstadoSolicitud?) {
        _estado.update { it.copy(filtroEstado = estado) }
    }

    fun onFiltroPrioridad(prioridad: Prioridad?) {
        _estado.update { it.copy(filtroPrioridad = prioridad) }
    }

    fun onFiltroCliente(cliente: String?) {
        _estado.update { it.copy(filtroCliente = cliente) }
    }

    fun onFiltroResponsable(responsable: String?) {
        _estado.update { it.copy(filtroResponsable = responsable) }
    }

    fun limpiarFiltros() {
        _estado.update {
            it.copy(
                busqueda = "",
                filtroEstado = null,
                filtroPrioridad = null,
                filtroCliente = null,
                filtroResponsable = null
            )
        }
    }

    fun onComentarioAdminChange(texto: String) {
        _estado.update { it.copy(comentarioAdmin = texto, errorComentarioAdmin = null) }
    }

    private fun buscar(codigo: String): Solicitud? {
        return _estado.value.solicitudes.find { it.codigo == codigo }
    }

    private fun registrarCambio(
        codigo: String,
        usuario: Usuario,
        nuevoEstado: EstadoSolicitud,
        accion: String,
        comentario: String,
        prioridad: Prioridad? = null,
        responsable: String? = null,
        motivoCancelacion: String? = null,
        categoria: Categoria? = null,
        evidencia: String? = null,
        titulo: String? = null,
        descripcion: String? = null,
        servicio: String? = null
    ): Boolean {
        if (_estado.value.guardando) return false
        val solicitud = buscar(codigo) ?: return false

        val ahora = LocalDateTime.now().format(formatoFecha)
        val evento = EventoHistorial(
            id = "${solicitud.codigo}-E${solicitud.historial.size + 1}",
            estadoAnterior = solicitud.estado,
            estadoNuevo = nuevoEstado,
            accion = accion,
            comentario = comentario,
            perfil = usuario.perfil,
            usuario = usuario.nombre,
            fechaHora = ahora,
            evidencia = evidencia
        )

        val actualizada = solicitud.copy(
            estado = nuevoEstado,
            fechaActualizacion = ahora,
            historial = solicitud.historial + evento,
            prioridad = prioridad ?: solicitud.prioridad,
            responsable = responsable ?: solicitud.responsable,
            motivoCancelacion = motivoCancelacion ?: solicitud.motivoCancelacion,
            categoria = categoria ?: solicitud.categoria,
            titulo = titulo ?: solicitud.titulo,
            descripcion = descripcion ?: solicitud.descripcion,
            servicio = servicio ?: solicitud.servicio
        )

        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                repositorio.guardar(actualizada)
            } finally {
                _estado.update { it.copy(guardando = false) }
            }
        }
        return true
    }

    fun pasarARevision(codigo: String, admin: Usuario) {
        if (admin.perfil != Perfil.ADMINISTRADOR) return
        val solicitud = buscar(codigo) ?: return
        if (solicitud.estado != EstadoSolicitud.RECIBIDA) return
        registrarCambio(
            codigo, admin, EstadoSolicitud.EN_REVISION,
            "Pasó a revisión", "El administrador revisa los antecedentes"
        )
    }

    fun clasificar(codigo: String, categoria: Categoria, admin: Usuario) {
        if (admin.perfil != Perfil.ADMINISTRADOR) return
        val solicitud = buscar(codigo) ?: return
        if (!solicitud.estaAbierta || solicitud.categoria == categoria) return
        registrarCambio(
            codigo, admin, solicitud.estado,
            "Clasificada como ${categoria.etiqueta}",
            "Categoría anterior: ${solicitud.categoria.etiqueta}",
            categoria = categoria
        )
    }

    fun actualizarDatos(
        codigo: String,
        admin: Usuario,
        titulo: String,
        descripcion: String,
        servicio: String
    ): String? {
        if (admin.perfil != Perfil.ADMINISTRADOR) return "No tienes permiso"
        val solicitud = buscar(codigo) ?: return "No se encontró la solicitud"
        val tituloLimpio = titulo.trim()
        val descripcionLimpia = descripcion.trim()
        val servicioLimpio = servicio.trim()
        if (tituloLimpio.isEmpty()) return "El título es obligatorio"
        if (descripcionLimpia.length < 10) return "La descripción necesita al menos 10 caracteres"
        if (
            tituloLimpio == solicitud.titulo &&
            descripcionLimpia == solicitud.descripcion &&
            servicioLimpio == solicitud.servicio
        ) {
            return null
        }
        val guardado = registrarCambio(
            codigo, admin, solicitud.estado,
            "Datos actualizados",
            "El administrador editó título, descripción o servicio",
            titulo = tituloLimpio,
            descripcion = descripcionLimpia,
            servicio = servicioLimpio
        )
        return if (guardado) null else "No se pudo guardar"
    }

    fun eliminarSolicitud(codigo: String, admin: Usuario, alEliminar: () -> Unit) {
        if (admin.perfil != Perfil.ADMINISTRADOR) return
        if (buscar(codigo) == null || _estado.value.guardando) return
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                repositorio.eliminar(codigo)
                _estado.update { it.copy(codigoSeleccionado = null) }
                alEliminar()
            } finally {
                _estado.update { it.copy(guardando = false) }
            }
        }
    }

    fun cerrarSolicitud(codigo: String, admin: Usuario) {
        if (admin.perfil != Perfil.ADMINISTRADOR) return
        val solicitud = buscar(codigo) ?: return
        if (solicitud.estado != EstadoSolicitud.RESUELTA || !solicitud.clienteConfirmoResolucion()) return
        registrarCambio(
            codigo, admin, EstadoSolicitud.CERRADA,
            "Solicitud cerrada", "El cliente ya había confirmado la resolución"
        )
    }

    fun definirPrioridad(codigo: String, prioridad: Prioridad, admin: Usuario) {
        if (admin.perfil != Perfil.ADMINISTRADOR) return
        val solicitud = buscar(codigo) ?: return
        if (!solicitud.estaAbierta || solicitud.prioridad == prioridad) return
        registrarCambio(
            codigo, admin, solicitud.estado,
            "Prioridad definida: ${prioridad.etiqueta}",
            "Prioridad anterior: ${solicitud.prioridad?.etiqueta ?: "Sin definir"}",
            prioridad = prioridad
        )
    }

    fun asignarResponsable(codigo: String, tecnico: Usuario, admin: Usuario) {
        if (admin.perfil != Perfil.ADMINISTRADOR || tecnico.perfil != Perfil.TECNICO) return
        val solicitud = buscar(codigo) ?: return
        if (!solicitud.estaAbierta || solicitud.responsable == tecnico.nombre) return

        val esReasignacion = solicitud.responsable != null
        val nuevoEstado = if (
            solicitud.estado == EstadoSolicitud.RECIBIDA ||
            solicitud.estado == EstadoSolicitud.EN_REVISION ||
            solicitud.estado == EstadoSolicitud.PENDIENTE_INFO
        ) EstadoSolicitud.ASIGNADA else solicitud.estado

        val accion = if (esReasignacion) {
            "Reasignada a ${tecnico.nombre}"
        } else {
            "Asignada a ${tecnico.nombre}"
        }

        registrarCambio(
            codigo, admin, nuevoEstado,
            accion,
            "Responsable anterior: ${solicitud.responsable ?: "Sin asignar"}",
            responsable = tecnico.nombre
        )
    }

    fun solicitarInformacion(codigo: String, admin: Usuario): Boolean {
        if (admin.perfil != Perfil.ADMINISTRADOR) return false
        val solicitud = buscar(codigo) ?: return false
        if (!solicitud.estaAbierta) return false

        val comentario = _estado.value.comentarioAdmin.trim()
        if (comentario.isEmpty()) {
            _estado.update { it.copy(errorComentarioAdmin = "Escribe qué información necesitas") }
            return false
        }

        val aplico = registrarCambio(
            codigo, admin, EstadoSolicitud.PENDIENTE_INFO,
            "Información solicitada al cliente", comentario
        )
        if (!aplico) return false

        _estado.update { it.copy(comentarioAdmin = "", errorComentarioAdmin = null) }
        return true
    }

    fun cancelarSolicitud(codigo: String, admin: Usuario): Boolean {
        if (admin.perfil != Perfil.ADMINISTRADOR) return false
        val solicitud = buscar(codigo) ?: return false
        if (!solicitud.estaAbierta) return false

        val motivo = _estado.value.comentarioAdmin.trim()
        if (motivo.isEmpty()) {
            _estado.update { it.copy(errorComentarioAdmin = "Escribe el motivo de la cancelación") }
            return false
        }

        val aplico = registrarCambio(
            codigo, admin, EstadoSolicitud.CANCELADA,
            "Solicitud cancelada", motivo,
            motivoCancelacion = motivo
        )
        if (!aplico) return false

        _estado.update { it.copy(comentarioAdmin = "", errorComentarioAdmin = null) }
        return true
    }

    fun onNotaChange(texto: String) {
        _estado.update { it.copy(nota = texto, errorNota = null) }
    }

    private fun notaEscrita(): String? {
        val nota = _estado.value.nota.trim()
        if (nota.isEmpty()) {
            _estado.update { it.copy(errorNota = "Escribe un comentario") }
            return null
        }
        return nota
    }

    private fun limpiarNota() {
        _estado.update { it.copy(nota = "", errorNota = null) }
    }

    fun agregarComentario(codigo: String, cliente: Usuario): Boolean {
        if (cliente.perfil != Perfil.CLIENTE) return false
        val solicitud = buscar(codigo) ?: return false
        if (solicitud.clienteId != cliente.id) return false
        if (solicitud.estado == EstadoSolicitud.CERRADA || solicitud.estado == EstadoSolicitud.CANCELADA) return false
        val nota = notaEscrita() ?: return false
        val respondeInfo = solicitud.estado == EstadoSolicitud.PENDIENTE_INFO
        val ok = registrarCambio(
            codigo,
            cliente,
            if (respondeInfo) EstadoSolicitud.EN_REVISION else solicitud.estado,
            if (respondeInfo) "Cliente envió información" else "Comentario del cliente",
            nota
        )
        if (ok) limpiarNota()
        return ok
    }

    fun confirmarResolucion(codigo: String, cliente: Usuario) {
        if (cliente.perfil != Perfil.CLIENTE) return
        val solicitud = buscar(codigo) ?: return
        if (solicitud.clienteId != cliente.id || solicitud.estado != EstadoSolicitud.RESUELTA) return
        if (solicitud.clienteConfirmoResolucion()) return
        registrarCambio(
            codigo, cliente, EstadoSolicitud.RESUELTA,
            "Cliente confirmó la resolución", "El cliente acepta el resultado"
        )
    }

    fun pedirReapertura(codigo: String, cliente: Usuario): Boolean {
        if (cliente.perfil != Perfil.CLIENTE) return false
        val solicitud = buscar(codigo) ?: return false
        if (solicitud.clienteId != cliente.id || solicitud.estado != EstadoSolicitud.RESUELTA) return false
        if (solicitud.clienteConfirmoResolucion()) return false
        val nota = notaEscrita() ?: return false
        val ok = registrarCambio(
            codigo, cliente, EstadoSolicitud.EN_PROCESO,
            "Reapertura pedida", nota
        )
        if (ok) limpiarNota()
        return ok
    }

    private fun esElResponsable(codigo: String, tecnico: Usuario): Boolean {
        if (tecnico.perfil != Perfil.TECNICO) return false
        val solicitud = buscar(codigo) ?: return false
        return solicitud.responsable == tecnico.nombre
    }

    fun marcarEnProceso(codigo: String, tecnico: Usuario) {
        if (!esElResponsable(codigo, tecnico)) return
        val solicitud = buscar(codigo) ?: return
        if (!solicitud.estaAbierta || solicitud.estado == EstadoSolicitud.EN_PROCESO) return
        registrarCambio(
            codigo, tecnico, EstadoSolicitud.EN_PROCESO,
            "Estado actualizado: En proceso", "El técnico pasó la solicitud a en proceso"
        )
    }

    fun registrarAvance(codigo: String, tecnico: Usuario, evidencia: String?): Boolean {
        if (!esElResponsable(codigo, tecnico)) return false
        val solicitud = buscar(codigo) ?: return false
        if (!solicitud.estaAbierta) return false
        val nota = notaEscrita() ?: return false
        val ok = registrarCambio(
            codigo, tecnico, EstadoSolicitud.EN_PROCESO,
            "Avance registrado", nota, evidencia = evidencia
        )
        if (ok) limpiarNota()
        return ok
    }

    fun proponerResolucion(codigo: String, tecnico: Usuario, evidencia: String?): Boolean {
        if (!esElResponsable(codigo, tecnico)) return false
        val solicitud = buscar(codigo) ?: return false
        if (!solicitud.estaAbierta) return false
        val nota = notaEscrita() ?: return false
        val ok = registrarCambio(
            codigo, tecnico, EstadoSolicitud.RESUELTA,
            "Resolución propuesta", nota, evidencia = evidencia
        )
        if (ok) limpiarNota()
        return ok
    }
}
