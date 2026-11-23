package com.example.gestionsolicitudes.repository

import com.example.gestionsolicitudes.data.remote.ApiService
import com.example.gestionsolicitudes.data.remote.FirestoreDocumento
import com.example.gestionsolicitudes.data.remote.FirestoreLista
import com.example.gestionsolicitudes.data.remote.FirestoreNuevoDocumento
import com.example.gestionsolicitudes.data.remote.FirestoreValor
import com.example.gestionsolicitudes.model.ServicioCatalogo

class ServicioRepository(
    private val projectId: String,
    private val apiKey: String,
    private val api: ApiService
) {

    suspend fun obtenerPorId(id: Int): ServicioCatalogo? {
        return leer().find { it.id == id }
    }

    suspend fun obtenerCatalogo(): List<ServicioCatalogo> {
        val actuales = leer()
        if (actuales.isNotEmpty()) return actuales
        SEMILLA.forEach { crear(it) }
        return leer()
    }

    suspend fun agregarDemo(): ServicioCatalogo {
        val id = (leer().maxOfOrNull { it.id } ?: 0) + 1
        val nuevo = ServicioCatalogo(id, "Servicio nuevo", "Otro", "Creado desde la app")
        crear(nuevo)
        return nuevo
    }

    suspend fun actualizar(servicio: ServicioCatalogo): ServicioCatalogo {
        api.actualizarDocumento(
            urlDe(nombreDe(servicio.id)),
            apiKey,
            listOf("id", "servicio", "categoria", "descripcion"),
            servicio.aDocumento()
        )
        return servicio
    }

    suspend fun eliminar(id: Int) {
        api.borrarDocumento(urlDe(nombreDe(id)), apiKey)
    }

    private suspend fun nombreDe(id: Int): String {
        val documento = api.listarServicios(projectId, apiKey).documents.orEmpty()
            .firstOrNull { it.aServicio()?.id == id }
        return documento?.name ?: error("No hay un servicio con id $id en Firebase")
    }

    private fun urlDe(nombre: String) = "https://firestore.googleapis.com/v1/$nombre"

    private suspend fun leer(): List<ServicioCatalogo> {
        return aCatalogo(api.listarServicios(projectId, apiKey))
    }

    private suspend fun crear(servicio: ServicioCatalogo) {
        api.crearServicio(projectId, apiKey, servicio.aDocumento())
    }

    companion object {
        val SEMILLA = listOf(
            ServicioCatalogo(1, "Mantenimiento web", "Web", "Actualización y soporte de sitios web"),
            ServicioCatalogo(2, "Soporte de correo", "Soporte", "Configuración y solución de problemas"),
            ServicioCatalogo(3, "Hosting", "Infraestructura", "Alojamiento y gestión de sitios")
        )

        fun aCatalogo(lista: FirestoreLista): List<ServicioCatalogo> {
            return lista.documents.orEmpty().mapNotNull { it.aServicio() }.sortedBy { it.id }
        }

    }
}

private fun ServicioCatalogo.aDocumento() = FirestoreNuevoDocumento(
    fields = mapOf(
        "id" to FirestoreValor(integerValue = id.toString()),
        "servicio" to FirestoreValor(stringValue = servicio),
        "categoria" to FirestoreValor(stringValue = categoria),
        "descripcion" to FirestoreValor(stringValue = descripcion)
    )
)

private fun FirestoreDocumento.aServicio(): ServicioCatalogo? {
    val campos = fields ?: return null
    val servicio = campos["servicio"]?.stringValue ?: return null
    val categoria = campos["categoria"]?.stringValue ?: return null
    val descripcion = campos["descripcion"]?.stringValue ?: return null
    val id = campos["id"]?.integerValue?.toIntOrNull() ?: return null
    return ServicioCatalogo(id, servicio, categoria, descripcion)
}
