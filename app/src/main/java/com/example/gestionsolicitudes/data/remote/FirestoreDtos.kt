package com.example.gestionsolicitudes.data.remote

data class FirestoreLista(
    val documents: List<FirestoreDocumento>? = null
)

data class FirestoreDocumento(
    val name: String? = null,
    val fields: Map<String, FirestoreValor>? = null
)

data class FirestoreValor(
    val stringValue: String? = null,
    val integerValue: String? = null
)

data class FirestoreNuevoDocumento(
    val fields: Map<String, FirestoreValor>
)
