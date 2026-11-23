package com.example.gestionsolicitudes.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

// Catálogo externo del diagrama: GET a la colección servicios de tu Firestore.
interface ApiService {

    @GET("projects/{projectId}/databases/(default)/documents/posts")
    suspend fun listarPosts(
        @Path("projectId") projectId: String,
        @Query("key") key: String,
        @Query("pageSize") pageSize: Int = 50
    ): FirestoreLista

    @GET("projects/{projectId}/databases/(default)/documents/servicios")
    suspend fun listarServicios(
        @Path("projectId") projectId: String,
        @Query("key") key: String,
        @Query("pageSize") pageSize: Int = 50
    ): FirestoreLista

    @POST("projects/{projectId}/databases/(default)/documents/posts")
    suspend fun crearPost(
        @Path("projectId") projectId: String,
        @Query("key") key: String,
        @Body documento: FirestoreNuevoDocumento
    ): FirestoreDocumento

    @PATCH
    suspend fun actualizarDocumento(
        @Url url: String,
        @Query("key") key: String,
        @Query("updateMask.fieldPaths") campos: List<String>,
        @Body documento: FirestoreNuevoDocumento
    ): FirestoreDocumento

    @DELETE
    suspend fun borrarDocumento(
        @Url nombre: String,
        @Query("key") key: String
    )

    @POST("projects/{projectId}/databases/(default)/documents/servicios")
    suspend fun crearServicio(
        @Path("projectId") projectId: String,
        @Query("key") key: String,
        @Body documento: FirestoreNuevoDocumento
    ): FirestoreDocumento
}
