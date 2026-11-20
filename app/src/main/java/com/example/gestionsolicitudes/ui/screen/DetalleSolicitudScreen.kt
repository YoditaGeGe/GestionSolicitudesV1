package com.example.gestionsolicitudes.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Update
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.Usuario
import com.example.gestionsolicitudes.ui.components.BotonAncho
import com.example.gestionsolicitudes.ui.components.ChipsEstadoPrioridad
import com.example.gestionsolicitudes.ui.components.FichaSolicitud
import com.example.gestionsolicitudes.ui.components.HistorialSolicitud
import com.example.gestionsolicitudes.ui.components.MenuDesplegable
import com.example.gestionsolicitudes.ui.components.PrettyCard
import com.example.gestionsolicitudes.ui.components.ScreenHeader
import com.example.gestionsolicitudes.ui.components.SectionLabel
import com.example.gestionsolicitudes.ui.components.SoftField
import com.example.gestionsolicitudes.ui.components.SoftScreen
import com.example.gestionsolicitudes.utils.codigoQr
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import com.example.gestionsolicitudes.viewmodel.SolicitudViewModel

@Composable
fun DetalleSolicitudScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    solicitudViewModel: SolicitudViewModel
) {
    val loginEstado by loginViewModel.estado.collectAsState()
    val estado by solicitudViewModel.estado.collectAsState()
    val usuario = loginEstado.usuarioActual
    val solicitud = estado.solicitudes.find { it.codigo == estado.codigoSeleccionado }
    var mostrarWhatsapp by rememberSaveable { mutableStateOf(false) }

    SoftScreen {
        if (solicitud == null) {
            PrettyCard { Text("No se encontró la solicitud") }
        } else {
            ScreenHeader(
                title = solicitud.codigo,
                subtitle = solicitud.titulo,
                icono = Icons.Outlined.Assignment
            )
            ChipsEstadoPrioridad(solicitud.estado, solicitud.prioridad)

            if (usuario?.perfil == Perfil.TECNICO && solicitud.responsable == usuario.nombre) {
                AccionesTecnico(solicitud, usuario, estado.nota, estado.errorNota, solicitudViewModel)
            }

            SectionLabel("Antecedentes", icono = Icons.Outlined.Description)
            FichaSolicitud(solicitud)

            if (usuario?.perfil == Perfil.CLIENTE && solicitud.clienteId == usuario.id) {
                BotonAncho("Compartir por WhatsApp (simulado)", icono = Icons.Outlined.Share) {
                    mostrarWhatsapp = true
                }
                if (mostrarWhatsapp) {
                    PrettyCard {
                        SectionLabel("WhatsApp · simulado", icono = Icons.Outlined.Chat)
                        Text(
                            "Hola, te comparto la solicitud ${solicitud.codigo} (${solicitud.titulo}). Estado: ${solicitud.estado.etiqueta}."
                        )
                    }
                }
                AccionesCliente(solicitud, usuario, estado.nota, estado.errorNota, solicitudViewModel)
            }

            HistorialSolicitud(solicitud.historial)

            PrettyCard {
                SectionLabel("Código QR", icono = Icons.Outlined.QrCode2)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val qr = remember(solicitud.codigo) { codigoQr(solicitud.codigo) }
                    Image(
                        bitmap = qr,
                        contentDescription = "Código QR de ${solicitud.codigo}",
                        modifier = Modifier.size(220.dp)
                    )
                }
                Text(
                    "Escanéalo en la app para abrir esta solicitud",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        BotonAncho("Volver", contorno = true, icono = Icons.Outlined.ArrowBack) {
            navController.popBackStack()
        }
    }
}

@Composable
private fun AccionesCliente(
    solicitud: Solicitud,
    cliente: Usuario,
    nota: String,
    errorNota: String?,
    viewModel: SolicitudViewModel
) {
    val puedeComentar = solicitud.estado != EstadoSolicitud.CERRADA &&
        solicitud.estado != EstadoSolicitud.CANCELADA
    val puedeCerrarCiclo = solicitud.estado == EstadoSolicitud.RESUELTA &&
        !solicitud.clienteConfirmoResolucion()

    if (!puedeComentar && !puedeCerrarCiclo) return

    PrettyCard {
        SectionLabel("Seguimiento", icono = Icons.Outlined.Chat)
        if (solicitud.estado == EstadoSolicitud.RESUELTA && solicitud.clienteConfirmoResolucion()) {
            Text("Confirmaste la resolución. El administrador puede cerrarla.")
        }
        if (puedeComentar) {
            SoftField(
                value = nota,
                onValueChange = viewModel::onNotaChange,
                label = if (solicitud.estado == EstadoSolicitud.PENDIENTE_INFO) {
                    "Responde la información pedida"
                } else {
                    "Comentario"
                },
                error = errorNota,
                minLines = 2,
                icono = Icons.Outlined.Chat
            )
            BotonAncho(
                texto = if (solicitud.estado == EstadoSolicitud.PENDIENTE_INFO) {
                    "Enviar información"
                } else {
                    "Agregar comentario"
                },
                icono = Icons.Outlined.Chat
            ) {
                viewModel.agregarComentario(solicitud.codigo, cliente)
            }
        }
        if (puedeCerrarCiclo) {
            BotonAncho("Confirmar resolución", icono = Icons.Outlined.CheckCircle) {
                viewModel.confirmarResolucion(solicitud.codigo, cliente)
            }
            BotonAncho("Pedir reapertura", contorno = true, icono = Icons.Outlined.Replay) {
                viewModel.pedirReapertura(solicitud.codigo, cliente)
            }
        }
    }
}

@Composable
private fun AccionesTecnico(
    solicitud: Solicitud,
    tecnico: Usuario,
    nota: String,
    errorNota: String?,
    viewModel: SolicitudViewModel
) {
    var evidencia by remember { mutableStateOf<String?>(null) }

    if (solicitud.estado == EstadoSolicitud.RESUELTA) {
        PrettyCard {
            Text("Propusiste la resolución. El cliente debe confirmarla o pedir reapertura.")
        }
        return
    }

    if (!solicitud.estaAbierta) {
        PrettyCard {
            Text("Esta solicitud está ${solicitud.estado.etiqueta.lowercase()} y ya no admite cambios.")
        }
        return
    }

    PrettyCard {
        SectionLabel("Actualizar estado", icono = Icons.Outlined.Update)
        Text("Estado actual: ${solicitud.estado.etiqueta}")
        if (solicitud.estado != EstadoSolicitud.EN_PROCESO) {
            BotonAncho("Marcar en proceso", icono = Icons.Outlined.Update) {
                viewModel.marcarEnProceso(solicitud.codigo, tecnico)
            }
        }
    }

    PrettyCard {
        SectionLabel("Registrar avance y evidencia", icono = Icons.Outlined.TrendingUp)
        SoftField(
            value = nota,
            onValueChange = viewModel::onNotaChange,
            label = "Qué avanzaste o cómo quedó resuelto",
            error = errorNota,
            minLines = 2,
            icono = Icons.Outlined.TrendingUp
        )
        MenuDesplegable(
            etiqueta = "Evidencia",
            valor = evidencia ?: "Sin evidencia nueva",
            opciones = listOf("Sin evidencia nueva") + viewModel.evidenciasFicticias,
            icono = Icons.Outlined.AttachFile,
            alElegir = { elegida ->
                evidencia = elegida.takeUnless { it == "Sin evidencia nueva" }
            }
        )
        BotonAncho("Registrar avance", icono = Icons.Outlined.TrendingUp) {
            viewModel.registrarAvance(solicitud.codigo, tecnico, evidencia)
        }
        BotonAncho("Proponer resolución", contorno = true, icono = Icons.Outlined.TaskAlt) {
            viewModel.proponerResolucion(solicitud.codigo, tecnico, evidencia)
        }
    }
}
