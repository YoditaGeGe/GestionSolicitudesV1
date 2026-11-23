package com.example.gestionsolicitudes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Title
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.gestionsolicitudes.model.Categoria
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Prioridad
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.Usuario
import com.example.gestionsolicitudes.navigation.salir
import com.example.gestionsolicitudes.ui.components.BotonAncho
import com.example.gestionsolicitudes.ui.components.ChipsEstadoPrioridad
import com.example.gestionsolicitudes.ui.components.FichaSolicitud
import com.example.gestionsolicitudes.ui.components.HistorialSolicitud
import com.example.gestionsolicitudes.ui.components.MenuDesplegable
import com.example.gestionsolicitudes.ui.components.MetaLine
import com.example.gestionsolicitudes.ui.components.PrettyCard
import com.example.gestionsolicitudes.ui.components.ScreenHeader
import com.example.gestionsolicitudes.ui.components.SectionLabel
import com.example.gestionsolicitudes.ui.components.SoftChip
import com.example.gestionsolicitudes.ui.components.SoftField
import com.example.gestionsolicitudes.ui.components.SoftScreen
import com.example.gestionsolicitudes.ui.components.colorEstado
import com.example.gestionsolicitudes.ui.components.colorPrioridad
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import com.example.gestionsolicitudes.viewmodel.SolicitudViewModel

@Composable
fun InicioAdminScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    solicitudViewModel: SolicitudViewModel
) {
    val loginEstado by loginViewModel.estado.collectAsState()
    val estado by solicitudViewModel.estado.collectAsState()
    val usuario = loginEstado.usuarioActual
    val lista = estado.solicitudesFiltradas()

    SoftScreen {
        ScreenHeader(
            title = "Panel admin",
            subtitle = "Hola ${usuario?.nombre ?: ""}, revisa y gestiona solicitudes",
            icono = Icons.Outlined.AdminPanelSettings
        )

        PrettyCard {
            SectionLabel("Panel resumen", icono = Icons.Outlined.Dashboard)
            EstadoSolicitud.entries.forEach { item ->
                MetaLine(item.etiqueta, estado.solicitudes.count { it.estado == item }.toString())
            }
        }

        BotonAncho("Nueva solicitud", icono = Icons.Outlined.Add) { navController.navigate("nueva") }
        BotonAncho("Escanear código QR", contorno = true, icono = Icons.Outlined.QrCodeScanner) {
            navController.navigate("escanear_qr")
        }
        BotonAncho(
            "Posts en Firebase (GET/POST/PUT/DELETE)",
            secundario = true,
            icono = Icons.Outlined.Cloud
        ) { navController.navigate("posts") }

        Text(
            "Los posts se leen de tu Firebase. Las solicitudes siguen en Room.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrettyCard {
            SoftField(
                value = estado.busqueda,
                onValueChange = solicitudViewModel::onBusquedaChange,
                label = "Buscar por código o título",
                icono = Icons.Outlined.Search
            )

            MenuDesplegable(
                etiqueta = "Estado",
                valor = estado.filtroEstado?.etiqueta ?: "Todos",
                opciones = listOf("Todos") + EstadoSolicitud.entries.map { it.etiqueta },
                icono = Icons.Outlined.FilterList,
                alElegir = { elegida ->
                    solicitudViewModel.onFiltroEstado(
                        EstadoSolicitud.entries.find { it.etiqueta == elegida }
                    )
                }
            )
            MenuDesplegable(
                etiqueta = "Prioridad",
                valor = estado.filtroPrioridad?.etiqueta ?: "Todas",
                opciones = listOf("Todas") + Prioridad.entries.map { it.etiqueta },
                icono = Icons.Outlined.Flag,
                alElegir = { elegida ->
                    solicitudViewModel.onFiltroPrioridad(
                        Prioridad.entries.find { it.etiqueta == elegida }
                    )
                }
            )
            MenuDesplegable(
                etiqueta = "Cliente",
                valor = estado.filtroCliente ?: "Todos",
                opciones = listOf("Todos") + estado.solicitudes.map { it.clienteNombre }.distinct(),
                icono = Icons.Outlined.Person,
                alElegir = { elegida ->
                    solicitudViewModel.onFiltroCliente(elegida.takeUnless { it == "Todos" })
                }
            )
            MenuDesplegable(
                etiqueta = "Responsable",
                valor = estado.filtroResponsable ?: "Todos",
                opciones = listOf("Todos") + estado.solicitudes.mapNotNull { it.responsable }.distinct(),
                icono = Icons.Outlined.Engineering,
                alElegir = { elegida ->
                    solicitudViewModel.onFiltroResponsable(elegida.takeUnless { it == "Todos" })
                }
            )

            BotonAncho("Limpiar filtros", contorno = true, icono = Icons.Outlined.FilterAltOff) {
                solicitudViewModel.limpiarFiltros()
            }
        }

        SectionLabel("Solicitudes (${lista.size})", icono = Icons.Outlined.ListAlt)

        if (lista.isEmpty()) {
            PrettyCard { Text("No hay solicitudes que coincidan") }
        }

        lista.forEach { solicitud ->
            PrettyCard(
                onClick = {
                    solicitudViewModel.seleccionarSolicitud(solicitud.codigo)
                    navController.navigate("admin_detalle")
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(solicitud.codigo, style = MaterialTheme.typography.titleMedium)
                    SoftChip(solicitud.estado.etiqueta, colorEstado(solicitud.estado))
                }
                Text(solicitud.titulo, fontWeight = FontWeight.Medium)
                MetaLine("Cliente", "${solicitud.clienteNombre} (${solicitud.empresa})")
                SoftChip(
                    solicitud.prioridad?.etiqueta ?: "Sin prioridad",
                    colorPrioridad(solicitud.prioridad)
                )
                MetaLine("Responsable", solicitud.responsable ?: "Sin asignar")
                MetaLine("Actualizada", solicitud.fechaActualizacion)
            }
        }

        BotonAncho("Cerrar sesión", contorno = true, icono = Icons.Outlined.Logout) {
            navController.salir(loginViewModel)
        }
    }
}

@Composable
fun AdminDetalleScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    solicitudViewModel: SolicitudViewModel
) {
    val loginEstado by loginViewModel.estado.collectAsState()
    val estado by solicitudViewModel.estado.collectAsState()
    val admin = loginEstado.usuarioActual
    val solicitud = estado.solicitudes.find { it.codigo == estado.codigoSeleccionado }

    SoftScreen {
        if (solicitud == null || admin == null) {
            PrettyCard { Text("No se encontró la solicitud") }
        } else {
            ScreenHeader(
                title = solicitud.codigo,
                subtitle = solicitud.titulo,
                icono = Icons.Outlined.Assignment
            )
            ChipsEstadoPrioridad(solicitud.estado, solicitud.prioridad)
            FichaSolicitud(solicitud, mostrarCliente = true)
            EditarSolicitud(solicitud, admin, solicitudViewModel)
            EliminarSolicitud(solicitud.codigo, admin, solicitudViewModel) {
                navController.popBackStack()
            }

            if (solicitud.estaAbierta) {
                PrettyCard {
                    SectionLabel("Revisar y clasificar", icono = Icons.Outlined.FactCheck)

                    if (solicitud.estado == EstadoSolicitud.RECIBIDA) {
                        BotonAncho("Pasar a revisión", icono = Icons.Outlined.FactCheck) {
                            solicitudViewModel.pasarARevision(solicitud.codigo, admin)
                        }
                    }
                    MenuDesplegable(
                        etiqueta = "Categoría",
                        valor = solicitud.categoria.etiqueta,
                        opciones = Categoria.entries.map { it.etiqueta },
                        icono = Icons.Outlined.Category,
                        alElegir = { elegida ->
                            Categoria.entries.find { it.etiqueta == elegida }?.let {
                                solicitudViewModel.clasificar(solicitud.codigo, it, admin)
                            }
                        }
                    )
                    MenuDesplegable(
                        etiqueta = "Prioridad",
                        valor = solicitud.prioridad?.etiqueta ?: "Sin definir",
                        opciones = Prioridad.entries.map { it.etiqueta },
                        icono = Icons.Outlined.Flag,
                        alElegir = { elegida ->
                            Prioridad.entries.find { it.etiqueta == elegida }?.let {
                                solicitudViewModel.definirPrioridad(solicitud.codigo, it, admin)
                            }
                        }
                    )
                    MenuDesplegable(
                        etiqueta = "Responsable",
                        valor = solicitud.responsable ?: "Sin asignar",
                        opciones = loginEstado.tecnicos.map { it.nombre },
                        icono = Icons.Outlined.Engineering,
                        alElegir = { nombre ->
                            loginEstado.tecnicos.find { it.nombre == nombre }?.let {
                                solicitudViewModel.asignarResponsable(solicitud.codigo, it, admin)
                            }
                        }
                    )

                    SoftField(
                        value = estado.comentarioAdmin,
                        onValueChange = solicitudViewModel::onComentarioAdminChange,
                        label = "Comentario (pedir info o cancelar)",
                        error = estado.errorComentarioAdmin,
                        icono = Icons.Outlined.Info
                    )
                    BotonAncho("Solicitar información", icono = Icons.Outlined.Info) {
                        solicitudViewModel.solicitarInformacion(solicitud.codigo, admin)
                    }
                    BotonAncho("Cancelar solicitud", contorno = true, icono = Icons.Outlined.Cancel) {
                        solicitudViewModel.cancelarSolicitud(solicitud.codigo, admin)
                    }
                }
            } else if (solicitud.estado == EstadoSolicitud.RESUELTA) {
                PrettyCard {
                    if (solicitud.clienteConfirmoResolucion()) {
                        Text("El cliente confirmó la resolución.")
                        BotonAncho("Cerrar solicitud", icono = Icons.Outlined.TaskAlt) {
                            solicitudViewModel.cerrarSolicitud(solicitud.codigo, admin)
                        }
                    } else {
                        Text("El técnico propuso la resolución. Falta que el cliente la confirme o pida reapertura.")
                    }
                }
            } else {
                PrettyCard {
                    Text("Esta solicitud está ${solicitud.estado.etiqueta.lowercase()} y ya no admite cambios.")
                }
            }

            HistorialSolicitud(solicitud.historial)
        }

        BotonAncho("Volver", contorno = true, icono = Icons.Outlined.ArrowBack) {
            navController.popBackStack()
        }
    }
}

@Composable
private fun EditarSolicitud(
    solicitud: Solicitud,
    admin: Usuario,
    viewModel: SolicitudViewModel
) {
    var titulo by remember(solicitud.codigo) { mutableStateOf(solicitud.titulo) }
    var descripcion by remember(solicitud.codigo) { mutableStateOf(solicitud.descripcion) }
    var servicio by remember(solicitud.codigo) { mutableStateOf(solicitud.servicio) }
    var error by remember(solicitud.codigo) { mutableStateOf<String?>(null) }

    PrettyCard {
        SectionLabel("Editar datos", icono = Icons.Outlined.Edit)
        val mensaje = error
        val errorTitulo = mensaje?.takeIf { it.contains("título") }
        val errorDescripcion = mensaje?.takeIf { it.contains("descripción") }
        SoftField(
            value = titulo,
            onValueChange = { titulo = it },
            label = "Título",
            error = errorTitulo,
            icono = Icons.Outlined.Title
        )
        SoftField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = "Descripción",
            error = errorDescripcion,
            minLines = 3,
            icono = Icons.Outlined.Notes
        )
        SoftField(
            value = servicio,
            onValueChange = { servicio = it },
            label = "Servicio o sitio web",
            icono = Icons.Outlined.Language
        )
        if (mensaje != null && errorTitulo == null && errorDescripcion == null) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }
        BotonAncho("Guardar cambios", icono = Icons.Outlined.Save) {
            error = viewModel.actualizarDatos(solicitud.codigo, admin, titulo, descripcion, servicio)
        }
    }
}

@Composable
private fun EliminarSolicitud(
    codigo: String,
    admin: Usuario,
    viewModel: SolicitudViewModel,
    alVolver: () -> Unit
) {
    var confirmar by remember(codigo) { mutableStateOf(false) }

    PrettyCard {
        SectionLabel("Eliminar", icono = Icons.Outlined.Delete)
        if (!confirmar) {
            Text("Borra la solicitud y su historial. Esta acción no se puede deshacer.")
            BotonAncho("Eliminar solicitud", contorno = true, icono = Icons.Outlined.Delete) {
                confirmar = true
            }
        } else {
            Text("¿Eliminar $codigo?")
            BotonAncho("Confirmar eliminación", icono = Icons.Outlined.DeleteForever) {
                viewModel.eliminarSolicitud(codigo, admin, alVolver)
            }
            BotonAncho("Cancelar", contorno = true, icono = Icons.Outlined.Cancel) { confirmar = false }
        }
    }
}
