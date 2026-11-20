package com.example.gestionsolicitudes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Title
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionsolicitudes.model.Categoria
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.ui.components.BotonAncho
import com.example.gestionsolicitudes.ui.components.MenuDesplegable
import com.example.gestionsolicitudes.ui.components.PrettyCard
import com.example.gestionsolicitudes.ui.components.ScreenHeader
import com.example.gestionsolicitudes.ui.components.SoftField
import com.example.gestionsolicitudes.ui.components.SoftScreen
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import com.example.gestionsolicitudes.viewmodel.SolicitudViewModel

@Composable
fun NuevaSolicitudScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    solicitudViewModel: SolicitudViewModel
) {
    val loginEstado by loginViewModel.estado.collectAsState()
    val estado by solicitudViewModel.estado.collectAsState()
    val form = estado.formulario
    val usuario = loginEstado.usuarioActual

    LaunchedEffect(estado.esperandoDetalle, estado.solicitudes) {
        val codigo = estado.codigoSeleccionado
        if (estado.esperandoDetalle && codigo != null && estado.solicitudes.any { it.codigo == codigo }) {
            solicitudViewModel.detalleAbierto()
            val esAdmin = usuario?.perfil == Perfil.ADMINISTRADOR
            navController.navigate(if (esAdmin) "admin_detalle" else "detalle") {
                popUpTo(if (esAdmin) "admin" else "cliente")
            }
        }
    }

    SoftScreen {
        ScreenHeader(
            title = "Nueva solicitud",
            subtitle = "Cuéntanos qué necesitas, con calma",
            icono = Icons.Outlined.NoteAdd
        )

        PrettyCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (usuario?.perfil == Perfil.ADMINISTRADOR) {
                    MenuDesplegable(
                        etiqueta = "Cliente",
                        valor = estado.clientes.find { it.id == form.clienteId }?.nombre
                            ?: "Selecciona un cliente",
                        opciones = estado.clientes.map { it.nombre },
                        icono = Icons.Outlined.Person,
                        alElegir = { nombre ->
                            estado.clientes.find { it.nombre == nombre }?.let {
                                solicitudViewModel.onClienteChange(it.id)
                            }
                        }
                    )
                    form.errores.cliente?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
                SoftField(
                    value = form.titulo,
                    onValueChange = solicitudViewModel::onTituloChange,
                    label = "Título",
                    error = form.errores.titulo,
                    icono = Icons.Outlined.Title
                )
                SoftField(
                    value = form.descripcion,
                    onValueChange = solicitudViewModel::onDescripcionChange,
                    label = "Descripción de la necesidad",
                    error = form.errores.descripcion,
                    minLines = 3,
                    icono = Icons.Outlined.Notes
                )
                SoftField(
                    value = form.servicio,
                    onValueChange = solicitudViewModel::onServicioChange,
                    label = "Servicio o sitio web (opcional)",
                    icono = Icons.Outlined.Language
                )
            }
        }

        PrettyCard {
            MenuDesplegable(
                etiqueta = "Categoría",
                valor = form.categoria?.etiqueta ?: "Selecciona una categoría",
                opciones = Categoria.entries.map { it.etiqueta },
                icono = Icons.Outlined.Category,
                alElegir = { elegida ->
                    Categoria.entries.find { it.etiqueta == elegida }?.let {
                        solicitudViewModel.onCategoriaChange(it)
                    }
                }
            )
            form.errores.categoria?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            MenuDesplegable(
                etiqueta = "Evidencia",
                valor = form.evidencia ?: "Sin evidencia",
                opciones = listOf("Sin evidencia") + solicitudViewModel.evidenciasFicticias,
                icono = Icons.Outlined.AttachFile,
                alElegir = { elegida ->
                    solicitudViewModel.onEvidenciaChange(elegida.takeUnless { it == "Sin evidencia" })
                }
            )
        }

        BotonAncho(
            texto = if (estado.guardando) "Guardando..." else "Enviar solicitud",
            onClick = { if (usuario != null) solicitudViewModel.enviarSolicitud(usuario) },
            enabled = usuario != null && !estado.guardando,
            icono = Icons.Outlined.Send
        )
        BotonAncho("Cancelar", contorno = true, icono = Icons.Outlined.Close) {
            navController.popBackStack()
        }
    }
}
