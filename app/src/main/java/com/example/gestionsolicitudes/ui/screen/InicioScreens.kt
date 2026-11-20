package com.example.gestionsolicitudes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.gestionsolicitudes.navigation.salir
import com.example.gestionsolicitudes.ui.components.BotonAncho
import com.example.gestionsolicitudes.ui.components.MetaLine
import com.example.gestionsolicitudes.ui.components.PrettyCard
import com.example.gestionsolicitudes.ui.components.ScreenHeader
import com.example.gestionsolicitudes.ui.components.SectionLabel
import com.example.gestionsolicitudes.ui.components.SoftChip
import com.example.gestionsolicitudes.ui.components.SoftScreen
import com.example.gestionsolicitudes.ui.components.colorEstado
import com.example.gestionsolicitudes.ui.components.colorPrioridad
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import com.example.gestionsolicitudes.viewmodel.SolicitudViewModel

@Composable
fun InicioClienteScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    solicitudViewModel: SolicitudViewModel
) {
    val loginEstado by loginViewModel.estado.collectAsState()
    val estado by solicitudViewModel.estado.collectAsState()
    val usuario = loginEstado.usuarioActual

    val misSolicitudes = estado.solicitudes
        .filter { it.clienteId == usuario?.id }
        .reversed()

    SoftScreen {
        ScreenHeader(
            title = "Tu panel",
            subtitle = "Hola ${usuario?.nombre ?: ""}, aquí están tus solicitudes",
            icono = Icons.Outlined.Person
        )

        PrettyCard {
            MetaLine("Empresa", usuario?.empresa ?: "—")
            Text(
                "Cuando necesites algo, crea una solicitud o escanea un código QR.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BotonAncho("Nueva solicitud", icono = Icons.Outlined.Add) { navController.navigate("nueva") }
        BotonAncho("Escanear código QR", contorno = true, icono = Icons.Outlined.QrCodeScanner) {
            navController.navigate("escanear_qr")
        }

        SectionLabel("Mis solicitudes (${misSolicitudes.size})", icono = Icons.Outlined.Assignment)

        if (misSolicitudes.isEmpty()) {
            PrettyCard {
                Text("Todavía no tienes solicitudes ✨")
                Text(
                    "Cuando envíes la primera, aparecerá aquí.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        misSolicitudes.forEach { solicitud ->
            PrettyCard(
                onClick = {
                    solicitudViewModel.seleccionarSolicitud(solicitud.codigo)
                    navController.navigate("detalle")
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
                MetaLine("Actualizada", solicitud.fechaActualizacion)
            }
        }

        BotonAncho("Cerrar sesión", contorno = true, icono = Icons.Outlined.Logout) {
            navController.salir(loginViewModel)
        }
    }
}

@Composable
fun InicioTecnicoScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    solicitudViewModel: SolicitudViewModel
) {
    val loginEstado by loginViewModel.estado.collectAsState()
    val estado by solicitudViewModel.estado.collectAsState()
    val usuario = loginEstado.usuarioActual

    val asignadas = estado.solicitudes
        .filter { it.responsable == usuario?.nombre }
        .reversed()

    SoftScreen {
        ScreenHeader(
            title = "Panel técnico",
            subtitle = "Hola ${usuario?.nombre ?: ""}, estas son tus solicitudes",
            icono = Icons.Outlined.Engineering
        )
        PrettyCard {
            MetaLine("Empresa", usuario?.empresa ?: "—")
            Text(
                "Toca una solicitud para cambiar el estado o registrar un avance.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BotonAncho("Escanear código QR", contorno = true, icono = Icons.Outlined.QrCodeScanner) {
            navController.navigate("escanear_qr")
        }

        SectionLabel("Mis solicitudes asignadas (${asignadas.size})", icono = Icons.Outlined.Assignment)

        if (asignadas.isEmpty()) {
            PrettyCard {
                Text("Todavía no tienes solicitudes asignadas")
                Text(
                    "Cuando el administrador te asigne una, aparecerá aquí.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        asignadas.forEach { solicitud ->
            PrettyCard(
                onClick = {
                    solicitudViewModel.seleccionarSolicitud(solicitud.codigo)
                    navController.navigate("detalle")
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
                MetaLine("Cliente", solicitud.clienteNombre)
                SoftChip(
                    solicitud.prioridad?.etiqueta ?: "Sin prioridad",
                    colorPrioridad(solicitud.prioridad)
                )
                MetaLine("Actualizada", solicitud.fechaActualizacion)
            }
        }

        BotonAncho("Cerrar sesión", contorno = true, icono = Icons.Outlined.Logout) {
            navController.salir(loginViewModel)
        }
    }
}
