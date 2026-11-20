package com.example.gestionsolicitudes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.EventoHistorial
import com.example.gestionsolicitudes.model.Prioridad
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.ui.theme.ChipAsignada
import com.example.gestionsolicitudes.ui.theme.ChipCancelada
import com.example.gestionsolicitudes.ui.theme.ChipCerrada
import com.example.gestionsolicitudes.ui.theme.ChipPendiente
import com.example.gestionsolicitudes.ui.theme.ChipProceso
import com.example.gestionsolicitudes.ui.theme.ChipRecibida
import com.example.gestionsolicitudes.ui.theme.ChipResuelta
import com.example.gestionsolicitudes.ui.theme.ChipRevision
import com.example.gestionsolicitudes.ui.theme.AppBackground
import com.example.gestionsolicitudes.ui.theme.Nebula
import com.example.gestionsolicitudes.ui.theme.NebulaContainer
import com.example.gestionsolicitudes.ui.theme.Pulse
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History

@Composable
fun SoftScreen(
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val scroll = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NebulaContainer.copy(alpha = 0.7f), AppBackground, AppBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (scrollable) Modifier.verticalScroll(scroll) else Modifier)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    icono: ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icono != null) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PrettyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val cuerpo: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }

    if (onClick == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = cuerpo
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = cuerpo
        )
    }
}

@Composable
fun SoftChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun colorEstado(estado: EstadoSolicitud): Color = when (estado) {
    EstadoSolicitud.RECIBIDA -> ChipRecibida
    EstadoSolicitud.EN_REVISION -> ChipRevision
    EstadoSolicitud.PENDIENTE_INFO -> ChipPendiente
    EstadoSolicitud.ASIGNADA -> ChipAsignada
    EstadoSolicitud.EN_PROCESO -> ChipProceso
    EstadoSolicitud.RESUELTA -> ChipResuelta
    EstadoSolicitud.CERRADA -> ChipCerrada
    EstadoSolicitud.CANCELADA -> ChipCancelada
}

fun colorPrioridad(prioridad: Prioridad?): Color = when (prioridad) {
    Prioridad.BAJA -> ChipCerrada
    Prioridad.MEDIA -> ChipPendiente
    Prioridad.ALTA -> ChipRevision
    Prioridad.URGENTE -> Pulse
    null -> Nebula
}

@Composable
fun MetaLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionLabel(text: String, icono: ImageVector? = null) {
    Spacer(Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icono != null) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SoftField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    minLines: Int = 1,
    icono: ImageVector? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        minLines = minLines,
        visualTransformation = visualTransformation,
        leadingIcon = icono?.let { imagen ->
            { Icon(imagen, contentDescription = null) }
        },
        isError = error != null,
        supportingText = {
            error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDesplegable(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    icono: ImageVector? = null,
    alElegir: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = abierto,
        onExpandedChange = { abierto = it }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = icono?.let { imagen ->
                { Icon(imagen, contentDescription = null) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        alElegir(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}

@Composable
fun BotonAncho(
    texto: String,
    enabled: Boolean = true,
    contorno: Boolean = false,
    secundario: Boolean = false,
    icono: ImageVector? = null,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    val modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    if (contorno) {
        OutlinedButton(onClick = onClick, enabled = enabled, shape = forma, modifier = modifier) {
            ContenidoBoton(texto, icono, contorno)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = forma,
            colors = if (secundario) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                ButtonDefaults.buttonColors()
            },
            modifier = modifier
        ) {
            ContenidoBoton(texto, icono, contorno)
        }
    }
}

@Composable
private fun ContenidoBoton(texto: String, icono: ImageVector?, contorno: Boolean) {
    if (icono != null) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
    }
    Text(texto, fontWeight = if (contorno) FontWeight.Medium else FontWeight.SemiBold)
}

@Composable
fun ChipsEstadoPrioridad(estado: EstadoSolicitud, prioridad: Prioridad?) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SoftChip(estado.etiqueta, colorEstado(estado))
        SoftChip(prioridad?.etiqueta ?: "Sin prioridad", colorPrioridad(prioridad))
    }
}

@Composable
fun FichaSolicitud(solicitud: Solicitud, mostrarCliente: Boolean = false) {
    PrettyCard {
        if (mostrarCliente) {
            MetaLine("Cliente", "${solicitud.clienteNombre} (${solicitud.empresa})")
        }
        MetaLine("Categoría", solicitud.categoria.etiqueta)
        MetaLine("Responsable", solicitud.responsable ?: "Sin asignar")
        MetaLine("Servicio", solicitud.servicio.ifBlank { "No indicado" })
        MetaLine("Evidencia", solicitud.evidencia ?: "Sin evidencia")
        MetaLine("Creada", solicitud.fechaCreacion)
        MetaLine("Actualizada", solicitud.fechaActualizacion)
        Text(solicitud.descripcion, style = MaterialTheme.typography.bodyLarge)
        solicitud.motivoCancelacion?.let { MetaLine("Motivo cancelación", it) }
    }
}

@Composable
fun HistorialSolicitud(eventos: List<EventoHistorial>) {
    SectionLabel("Historial", icono = Icons.Outlined.History)
    eventos.forEach { evento ->
        PrettyCard {
            Text(
                "${evento.fechaHora} · ${evento.accion}",
                fontWeight = FontWeight.SemiBold
            )
            Text("${evento.estadoAnterior?.etiqueta ?: "—"} → ${evento.estadoNuevo.etiqueta}")
            MetaLine("Por", "${evento.usuario} (${evento.perfil.name})")
            Text(evento.comentario, color = MaterialTheme.colorScheme.onSurfaceVariant)
            evento.evidencia?.let { MetaLine("Evidencia", it) }
        }
    }
}
