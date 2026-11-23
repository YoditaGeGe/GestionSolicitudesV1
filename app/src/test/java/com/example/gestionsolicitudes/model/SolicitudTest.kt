package com.example.gestionsolicitudes.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class SolicitudTest : StringSpec({
    "una solicitud recibida sigue abierta y una cerrada no" {
        solicitud(EstadoSolicitud.RECIBIDA).estaAbierta.shouldBeTrue()
        solicitud(EstadoSolicitud.CERRADA).estaAbierta.shouldBeFalse()
        solicitud(EstadoSolicitud.CANCELADA).estaAbierta.shouldBeFalse()
    }

    "el cliente confirmo la resolucion solo despues de la propuesta" {
        val propuesta = evento(
            "Resolución propuesta",
            EstadoSolicitud.EN_PROCESO,
            EstadoSolicitud.RESUELTA
        )
        val confirmacion = evento(
            "Cliente confirmó la resolución",
            EstadoSolicitud.RESUELTA,
            EstadoSolicitud.RESUELTA
        )
        val resuelta = solicitud(EstadoSolicitud.RESUELTA).copy(
            historial = listOf(propuesta, confirmacion)
        )

        resuelta.clienteConfirmoResolucion().shouldBeTrue()
        solicitud(EstadoSolicitud.RESUELTA).clienteConfirmoResolucion().shouldBeFalse()
    }
})

private fun solicitud(estado: EstadoSolicitud) = Solicitud(
    codigo = "SOL-1",
    clienteId = "U1",
    clienteNombre = "Cliente Demo",
    empresa = "Servicios Digitales Demo",
    titulo = "Landing",
    descripcion = "Ajuste del formulario",
    categoria = Categoria.MANTENIMIENTO_WEB,
    servicio = "Mantenimiento web",
    estado = estado,
    fechaCreacion = "22/11/2026 21:00",
    fechaActualizacion = "22/11/2026 21:00"
)

private fun evento(
    accion: String,
    anterior: EstadoSolicitud,
    nuevo: EstadoSolicitud
) = EventoHistorial(
    id = accion,
    estadoAnterior = anterior,
    estadoNuevo = nuevo,
    accion = accion,
    comentario = accion,
    perfil = Perfil.CLIENTE,
    usuario = "Cliente Demo",
    fechaHora = "22/11/2026 21:00"
)
