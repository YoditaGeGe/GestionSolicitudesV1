package com.example.gestionsolicitudes.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionsolicitudes.ui.screen.AdminDetalleScreen
import com.example.gestionsolicitudes.ui.screen.DetalleSolicitudScreen
import com.example.gestionsolicitudes.ui.screen.InicioAdminScreen
import com.example.gestionsolicitudes.ui.screen.InicioClienteScreen
import com.example.gestionsolicitudes.ui.screen.InicioTecnicoScreen
import com.example.gestionsolicitudes.ui.screen.LoginScreen
import com.example.gestionsolicitudes.ui.screen.NuevaSolicitudScreen
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.ui.screen.QrScannerRoute
import com.example.gestionsolicitudes.ui.screen.PostScreen
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import com.example.gestionsolicitudes.viewmodel.PostViewModel
import com.example.gestionsolicitudes.viewmodel.SolicitudViewModel

fun NavController.salir(login: LoginViewModel) {
    login.cerrarSesion()
    navigate("login") { popUpTo(0) }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val solicitudViewModel: SolicitudViewModel = viewModel()
    val aplicacion = LocalContext.current.applicationContext as Application
    val postViewModel: PostViewModel = viewModel(factory = PostViewModel.fabrica(aplicacion))

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController, loginViewModel) }
        composable("posts") {
            PostScreen(
                viewModel = postViewModel,
                onIrAGestion = { navController.popBackStack() }
            )
        }
        composable("cliente") { InicioClienteScreen(navController, loginViewModel, solicitudViewModel) }
        composable("nueva") { NuevaSolicitudScreen(navController, loginViewModel, solicitudViewModel) }
        composable("detalle") {
            DetalleSolicitudScreen(navController, loginViewModel, solicitudViewModel)
        }
        composable("admin") { InicioAdminScreen(navController, loginViewModel, solicitudViewModel) }
        composable("admin_detalle") { AdminDetalleScreen(navController, loginViewModel, solicitudViewModel) }
        composable("tecnico") {
            InicioTecnicoScreen(navController, loginViewModel, solicitudViewModel)
        }
        composable("escanear_qr") {
            QrScannerRoute(
                onVolver = { navController.popBackStack() },
                onCodigoLeido = { texto ->
                    val abrio = solicitudViewModel.seleccionarSiExiste(texto)
                    if (abrio) {
                        val perfil = loginViewModel.estado.value.usuarioActual?.perfil
                        val ruta = if (perfil == Perfil.ADMINISTRADOR) "admin_detalle" else "detalle"
                        navController.navigate(ruta) {
                            popUpTo("escanear_qr") { inclusive = true }
                        }
                    }
                    abrio
                }
            )
        }
    }
}
