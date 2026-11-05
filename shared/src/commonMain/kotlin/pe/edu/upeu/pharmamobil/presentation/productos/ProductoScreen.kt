package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.util.FormateadorTiempo

@Composable
fun ProductoScreen(
    viewModel: ProductosViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    val estadoSync = estado.sincronizacion
    val formulario = estado.formulario

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Antigüedad de la Sincronización
        Text(
            text = FormateadorTiempo.formatearAntiguedad(estadoSync.ultimaSyncMillis),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 2. Banner de Estado de Sincronización (Paso 4 - Guía 13)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (estadoSync.fallidas > 0) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (estadoSync.pendientes > 0) "Sin conexión: ${estadoSync.pendientes} cambios por enviar"
                        else "Conectado y sincronizado con PharmaSoft",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (estadoSync.fallidas > 0) {
                        Text(
                            text = "Atención: ${estadoSync.fallidas} registros fallaron permanentemente",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                if (estadoSync.enCurso) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        }

        // 3. Alerta general de red
        estado.avisoRed?.let { aviso ->
            Text(
                text = aviso,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 4. Formulario de Registro / Edición
        OutlinedTextField(
            value = formulario.nombre,
            onValueChange = { viewModel.onNombreChange(it) },
            label = { Text("Nombre del Producto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formulario.precio,
                onValueChange = { viewModel.onPrecioChange(it) },
                label = { Text("Precio") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = formulario.stock,
                onValueChange = { viewModel.onStockChange(it) },
                label = { Text("Stock") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.guardar() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formulario.guardando
        ) {
            Text(if (formulario.id == null || formulario.id == 0L) "Registrar" else "Actualizar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Lista de Productos según la Fase
        when (val fase = estado.fase) {
            is ProductosUiState.Fase.Cargando -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProductosUiState.Fase.SinProductos -> {
                Text(
                    text = "No hay productos disponibles.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is ProductosUiState.Fase.ConProductos -> {
                LazyColumn {
                    items(fase.lista) { producto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { viewModel.seleccionarParaEditar(producto) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = producto.nombre,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "S/ ${producto.precio} | Stock: ${producto.stock}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (producto.id < 0) {
                                        Text(
                                            text = "Pendiente de sincronizar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.eliminar(producto.id) }) {
                                    Text("X")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}