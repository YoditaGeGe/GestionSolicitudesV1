package com.example.gestionsolicitudes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Login
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.ui.components.BotonAncho
import com.example.gestionsolicitudes.ui.components.PrettyCard
import com.example.gestionsolicitudes.ui.components.SoftField
import com.example.gestionsolicitudes.ui.components.SoftScreen
import com.example.gestionsolicitudes.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.usuarioActual) {
        val perfil = estado.usuarioActual?.perfil ?: return@LaunchedEffect
        val ruta = when (perfil) {
            Perfil.CLIENTE -> "cliente"
            Perfil.ADMINISTRADOR -> "admin"
            Perfil.TECNICO -> "tecnico"
        }
        navController.navigate(ruta) {
            popUpTo("login") { inclusive = true }
        }
    }

    SoftScreen {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Hola 👋",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Gestión de solicitudes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Entra para ver y seguir tus requerimientos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrettyCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftField(
                    value = estado.correo,
                    onValueChange = viewModel::onCorreoChange,
                    label = "Correo electrónico",
                    error = estado.errores.correo,
                    icono = Icons.Outlined.Email
                )
                SoftField(
                    value = estado.clave,
                    onValueChange = viewModel::onClaveChange,
                    label = "Contraseña",
                    error = estado.errores.clave,
                    icono = Icons.Outlined.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )
                estado.errores.general?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                BotonAncho(
                    "Ingresar",
                    secundario = true,
                    icono = Icons.Outlined.Login,
                    onClick = viewModel::iniciarSesion
                )
            }
        }

        PrettyCard {
            Text(
                "Cuentas de prueba",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Clave para todas: 123456",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("cliente@demo.cl")
            Text("admin@demo.cl")
            Text("tecnico@demo.cl")
        }
    }
}
