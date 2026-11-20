package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.data.local.SolicitudDao
import com.example.gestionsolicitudes.data.local.UsuarioDao
import com.example.gestionsolicitudes.data.local.toEntity
import com.example.gestionsolicitudes.data.local.toModel
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SolicitudRepository(
    private val solicitudDao: SolicitudDao,
    private val usuarioDao: UsuarioDao
) {
    val solicitudes: Flow<List<Solicitud>> =
        solicitudDao.observarTodas().map { lista -> lista.map { it.toModel() } }

    suspend fun siguienteCodigo(): String {
        val mayor = solicitudDao.codigos()
            .mapNotNull { it.removePrefix("SOL-").toIntOrNull() }
            .maxOrNull() ?: 0
        return "SOL-" + (mayor + 1).toString().padStart(4, '0')
    }

    suspend fun eliminar(codigo: String) {
        solicitudDao.eliminar(codigo)
    }

    // Solo inserto los eventos que todavía no están guardados
    suspend fun guardar(solicitud: Solicitud) {
        val yaGuardados = solicitudDao.contarEventos(solicitud.codigo)
        val nuevos = solicitud.historial.drop(yaGuardados).map { it.toEntity(solicitud.codigo) }
        solicitudDao.guardarConEventos(solicitud.toEntity(), nuevos)
    }

    suspend fun autenticar(correo: String, clave: String): Usuario? =
        usuarioDao.autenticar(correo.trim(), clave)?.toModel()

    suspend fun tecnicos(): List<Usuario> = usuarioDao.tecnicos().map { it.toModel() }

    suspend fun clientes(): List<Usuario> = usuarioDao.clientes().map { it.toModel() }
}
