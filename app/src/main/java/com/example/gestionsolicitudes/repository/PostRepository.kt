package com.example.gestionsolicitudes.repository

import com.example.gestionsolicitudes.data.model.Post
import com.example.gestionsolicitudes.data.remote.ApiService
import com.example.gestionsolicitudes.model.ServicioCatalogo
import com.example.gestionsolicitudes.data.remote.FirestoreDocumento
import com.example.gestionsolicitudes.data.remote.FirestoreLista
import com.example.gestionsolicitudes.data.remote.FirestoreNuevoDocumento
import com.example.gestionsolicitudes.data.remote.FirestoreValor

class PostRepository(
    private val api: ApiService? = null,
    private val projectId: String = "",
    private val apiKey: String = ""
) {

    suspend fun getPosts(): List<Post> = leer()

    suspend fun getServicios(): List<ServicioCatalogo> {
        return ServicioRepository.aCatalogo(servicio().listarServicios(projectId, apiKey))
    }

    suspend fun getServicioPorId(id: Int): List<ServicioCatalogo> {
        return getServicios().filter { it.id == id }
    }

    suspend fun getPostsDeUsuario(userId: Int): List<Post> {
        return leer().filter { it.userId == userId }
    }

    suspend fun getPostPorId(id: Int): List<Post> {
        return leer().filter { it.id == id }
    }

    suspend fun createPost(post: Post): Post {
        val nuevoId = if (post.id == 0) siguienteId() else post.id
        val nuevo = post.copy(id = nuevoId)
        servicio().crearPost(
            projectId,
            apiKey,
            FirestoreNuevoDocumento(
                fields = mapOf(
                    "userId" to FirestoreValor(integerValue = nuevo.userId.toString()),
                    "id" to FirestoreValor(integerValue = nuevo.id.toString()),
                    "title" to FirestoreValor(stringValue = nuevo.title),
                    "body" to FirestoreValor(stringValue = nuevo.body)
                )
            )
        )
        return nuevo
    }

    suspend fun updatePost(id: Int, post: Post): Post {
        val actualizado = post.copy(id = id)
        servicio().actualizarDocumento(
            urlDe(nombreDe(id)),
            apiKey,
            listOf("userId", "id", "title", "body"),
            actualizado.aDocumento()
        )
        return actualizado
    }

    suspend fun deletePost(id: Int) {
        servicio().borrarDocumento(urlDe(nombreDe(id)), apiKey)
    }

    private suspend fun leer(): List<Post> {
        return aPosts(servicio().listarPosts(projectId, apiKey))
    }

    private suspend fun siguienteId(): Int {
        return (leer().maxOfOrNull { it.id } ?: 0) + 1
    }

    private suspend fun nombreDe(id: Int): String {
        val documento = servicio().listarPosts(projectId, apiKey).documents.orEmpty()
            .firstOrNull { it.aPost()?.id == id }
        return documento?.name ?: error("No hay un post con id $id en Firebase")
    }

    private fun urlDe(nombre: String) = "https://firestore.googleapis.com/v1/$nombre"

    private fun servicio(): ApiService = api ?: error("Retrofit no está listo")

    companion object {
        fun aPosts(lista: FirestoreLista): List<Post> {
            return lista.documents.orEmpty().mapNotNull { it.aPost() }.sortedBy { it.id }
        }
    }
}

private fun FirestoreDocumento.aPost(): Post? {
    val campos = fields ?: return null
    val title = campos["title"]?.stringValue ?: return null
    val body = campos["body"]?.stringValue ?: return null
    return Post(
        userId = campos["userId"]?.integerValue?.toIntOrNull() ?: 0,
        id = campos["id"]?.integerValue?.toIntOrNull() ?: 0,
        title = title,
        body = body
    )
}

private fun Post.aDocumento() = FirestoreNuevoDocumento(
    fields = mapOf(
        "userId" to FirestoreValor(integerValue = userId.toString()),
        "id" to FirestoreValor(integerValue = id.toString()),
        "title" to FirestoreValor(stringValue = title),
        "body" to FirestoreValor(stringValue = body)
    )
)
