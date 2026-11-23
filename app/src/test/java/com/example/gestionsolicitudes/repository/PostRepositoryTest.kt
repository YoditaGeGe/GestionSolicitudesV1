package com.example.gestionsolicitudes.repository

import com.example.gestionsolicitudes.data.model.Post
import com.example.gestionsolicitudes.data.remote.FirestoreDocumento
import com.example.gestionsolicitudes.data.remote.FirestoreLista
import com.example.gestionsolicitudes.data.remote.FirestoreValor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly

class PostRepositoryTest : StringSpec({
    "aPosts convierte el GET de Firestore" {
        val respuesta = FirestoreLista(
            documents = listOf(
                FirestoreDocumento(
                    fields = mapOf(
                        "userId" to FirestoreValor(integerValue = "1"),
                        "id" to FirestoreValor(integerValue = "1"),
                        "title" to FirestoreValor(stringValue = "Landing de la pyme"),
                        "body" to FirestoreValor(stringValue = "Ajustar el formulario")
                    )
                )
            )
        )

        PostRepository.aPosts(respuesta) shouldContainExactly listOf(
            Post(userId = 1, id = 1, title = "Landing de la pyme", body = "Ajustar el formulario")
        )
    }
})
