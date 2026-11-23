package com.example.gestionsolicitudes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.model.ServicioCatalogo
import com.example.gestionsolicitudes.ui.components.PrettyCard
import com.example.gestionsolicitudes.ui.components.SoftChip
import com.example.gestionsolicitudes.ui.theme.Abyss
import com.example.gestionsolicitudes.ui.theme.AppBackground
import com.example.gestionsolicitudes.ui.theme.NebulaContainer
import com.example.gestionsolicitudes.ui.theme.Pulse
import com.example.gestionsolicitudes.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: PostViewModel,
    onIrAGestion: (() -> Unit)? = null
) {
    val posts = viewModel.postList.collectAsState().value
    val servicios = viewModel.servicios.collectAsState().value
    val mensaje = viewModel.mensaje.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Posts en Firebase", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Retrofit · tu Firestore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (onIrAGestion != null) {
                        TextButton(onClick = onIrAGestion) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                contentDescription = null,
                                tint = Abyss,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Volver", color = Abyss, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NebulaContainer.copy(alpha = 0.75f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.fetchPosts() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("GET todos")
                }
                Button(
                    onClick = { viewModel.crearPostDemo() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Pulse
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("POST")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.fetchPostsDeUsuario(1) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("GET usuario 1")
                }
                OutlinedButton(
                    onClick = { viewModel.fetchPostPorId(1) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("GET id 1")
                }
            }
            OutlinedButton(
                onClick = { viewModel.crearServicioDemo() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("POST servicio")
            }

            if (posts.isEmpty() && servicios.isEmpty()) {
                PrettyCard {
                    Text("Firebase no devolvió documentos", fontWeight = FontWeight.Medium)
                    Text(
                        "Si borraste todo, pulsa GET todos y Firebase vuelve a crear los tres servicios.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(servicios, key = { "servicio-${it.id}" }) { servicio ->
                    TarjetaServicio(
                        servicio = servicio,
                        alEditar = { viewModel.actualizarServicio(servicio) },
                        alEliminar = { viewModel.eliminarServicio(servicio) }
                    )
                }
                items(posts, key = { "post-${it.id}" }) { post ->
                    PrettyCard {
                        SoftChip("Usuario ${post.userId}", Abyss)
                        Text(
                            text = "Título: ${post.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = post.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.actualizarPost(post) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("PUT")
                            }
                            TextButton(onClick = { viewModel.eliminarPost(post) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Pulse,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("DELETE", color = Pulse)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaServicio(
    servicio: ServicioCatalogo,
    alEditar: () -> Unit,
    alEliminar: () -> Unit
) {
    PrettyCard {
        SoftChip("Servicio ${servicio.id}", Abyss)
        Text(
            text = servicio.servicio,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${servicio.categoria}: ${servicio.descripcion}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = alEditar,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("PUT")
            }
            TextButton(onClick = alEliminar) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Pulse,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("DELETE", color = Pulse)
            }
        }
    }
}
