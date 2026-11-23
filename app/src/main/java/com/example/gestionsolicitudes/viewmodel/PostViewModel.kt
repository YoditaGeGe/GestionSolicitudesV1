package com.example.gestionsolicitudes.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlin.jvm.JvmOverloads
import com.example.gestionsolicitudes.R
import com.example.gestionsolicitudes.data.model.Post
import com.example.gestionsolicitudes.data.remote.RetrofitInstance
import com.example.gestionsolicitudes.model.ServicioCatalogo
import com.example.gestionsolicitudes.repository.PostRepository
import com.example.gestionsolicitudes.repository.ServicioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class PostViewModel @JvmOverloads constructor(
    private val repository: PostRepository = PostRepository(),
    private val serviciosRepo: ServicioRepository? = null
) : ViewModel() {

    protected val _postList = MutableStateFlow<List<Post>>(emptyList())
    open val postList: StateFlow<List<Post>> = _postList

    private val _servicios = MutableStateFlow<List<ServicioCatalogo>>(emptyList())
    val servicios: StateFlow<List<ServicioCatalogo>> = _servicios

    private val _mensaje = MutableStateFlow<String?>(null)
    open val mensaje: StateFlow<String?> = _mensaje

    init {
        fetchPosts()
    }

    open fun fetchPosts() = ejecutar("Error al obtener datos") {
        val servicios = leerServicios()
        val lista = repository.getPosts()
        _servicios.value = servicios
        _postList.value = lista
        _mensaje.value = "GET Firebase: ${servicios.size} servicios, ${lista.size} posts"
    }

    fun fetchPostsDeUsuario(userId: Int = 1) = ejecutar("Error al obtener datos") {
        val servicios = repository.getServicios()
        val lista = repository.getPostsDeUsuario(userId)
        _servicios.value = servicios
        _postList.value = lista
        _mensaje.value = "GET del usuario $userId: ${lista.size} posts y ${servicios.size} servicios"
    }

    fun fetchPostPorId(id: Int = 1) = ejecutar("Error al obtener datos") {
        val servicios = repository.getServicioPorId(id)
        val lista = repository.getPostPorId(id)
        _servicios.value = servicios
        _postList.value = lista
        val nombre = servicios.firstOrNull()?.servicio ?: lista.firstOrNull()?.title
        _mensaje.value = if (nombre == null) "GET id $id: no está" else "GET id $id: $nombre"
    }

    fun crearPostDemo() = ejecutar("Error al crear") {
        val creado = repository.createPost(
            Post(userId = 1, id = 0, title = "Post de prueba", body = "Creado desde la app")
        )
        _mensaje.value = "POST guardado en Firebase con id ${creado.id}"
        _postList.value = listOf(creado) + _postList.value
    }

    fun actualizarPost(post: Post) = ejecutar("Error al actualizar") {
        val actualizado = repository.updatePost(post.id, post.copy(title = "${post.title} (editado)"))
        _postList.value = _postList.value.map { if (it.id == post.id) actualizado else it }
        _mensaje.value = "PUT actualizó el post ${actualizado.id}"
    }

    fun eliminarPost(post: Post) = ejecutar("Error al eliminar") {
        repository.deletePost(post.id)
        _postList.value = _postList.value.filterNot { it.id == post.id }
        _mensaje.value = "DELETE eliminó el post ${post.id}"
    }

    fun crearServicioDemo() = ejecutar("Error al crear el servicio") {
        val creado = catalogo().agregarDemo()
        _servicios.value = listOf(creado) + _servicios.value
        _mensaje.value = "POST guardó el servicio ${creado.id}"
    }

    fun actualizarServicio(servicio: ServicioCatalogo) = ejecutar("Error al actualizar el servicio") {
        val actualizado = catalogo().actualizar(
            servicio.copy(servicio = "${servicio.servicio} (editado)")
        )
        _servicios.value = _servicios.value.map { if (it.id == servicio.id) actualizado else it }
        _mensaje.value = "PUT actualizó el servicio ${actualizado.id}"
    }

    fun eliminarServicio(servicio: ServicioCatalogo) = ejecutar("Error al eliminar el servicio") {
        catalogo().eliminar(servicio.id)
        _servicios.value = _servicios.value.filterNot { it.id == servicio.id }
        _mensaje.value = "DELETE eliminó el servicio ${servicio.id}. Si borras todos, GET todos los recupera"
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private suspend fun leerServicios(): List<ServicioCatalogo> {
        return serviciosRepo?.obtenerCatalogo() ?: repository.getServicios()
    }

    private fun catalogo(): ServicioRepository {
        return serviciosRepo ?: error("Retrofit no está listo")
    }

    private fun ejecutar(prefijoError: String, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                bloque()
            } catch (e: Exception) {
                _mensaje.value = "$prefijoError: ${e.localizedMessage}"
            }
        }
    }

    companion object {
        fun fabrica(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val api = RetrofitInstance.crear(app)
                    val projectId = app.getString(R.string.project_id)
                    val apiKey = app.getString(R.string.google_api_key)
                    val repo = PostRepository(api = api, projectId = projectId, apiKey = apiKey)
                    val servicios = ServicioRepository(projectId, apiKey, api)
                    @Suppress("UNCHECKED_CAST")
                    return PostViewModel(repo, servicios) as T
                }
            }
    }
}
