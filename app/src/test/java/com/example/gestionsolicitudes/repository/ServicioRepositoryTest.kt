package com.example.gestionsolicitudes.repository

import com.example.gestionsolicitudes.data.remote.FirestoreDocumento
import com.example.gestionsolicitudes.data.remote.FirestoreLista
import com.example.gestionsolicitudes.data.remote.FirestoreValor
import com.example.gestionsolicitudes.model.ServicioCatalogo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ServicioRepositoryTest : StringSpec({
    "aCatalogo convierte la respuesta REST de Firestore" {
        val respuesta = FirestoreLista(
            documents = listOf(
                FirestoreDocumento(
                    fields = mapOf(
                        "id" to FirestoreValor(integerValue = "2"),
                        "servicio" to FirestoreValor(stringValue = "Soporte de correo"),
                        "categoria" to FirestoreValor(stringValue = "Soporte"),
                        "descripcion" to FirestoreValor(stringValue = "Configuración y solución de problemas")
                    )
                ),
                FirestoreDocumento(
                    fields = mapOf(
                        "id" to FirestoreValor(integerValue = "1"),
                        "servicio" to FirestoreValor(stringValue = "Mantenimiento web"),
                        "categoria" to FirestoreValor(stringValue = "Web"),
                        "descripcion" to FirestoreValor(stringValue = "Actualización y soporte de sitios web")
                    )
                )
            )
        )

        ServicioRepository.SEMILLA.map { it.servicio } shouldBe listOf(
            "Mantenimiento web",
            "Soporte de correo",
            "Hosting"
        )

        ServicioRepository.aCatalogo(respuesta) shouldContainExactly listOf(
            ServicioCatalogo(1, "Mantenimiento web", "Web", "Actualización y soporte de sitios web"),
            ServicioCatalogo(2, "Soporte de correo", "Soporte", "Configuración y solución de problemas")
        )
    }
})
