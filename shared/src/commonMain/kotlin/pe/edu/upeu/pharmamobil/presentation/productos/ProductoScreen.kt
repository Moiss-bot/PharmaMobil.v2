package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobil.domain.model.Producto

@Composable
fun ProductoScreen(
    viewModel: ProductosViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CRUD Productos PharmaSoft",
            style = MaterialTheme.typography.titleLarge
        )

        // Formulario
        FormularioSection(
            formulario = uiState.formulario,
            operacion = uiState.operacion,
            onNombreChange = viewModel::onNombreChange,
            onPrecioChange = viewModel::onPrecioChange,
            onStockChange = viewModel::onStockChange,
            onGuardar = viewModel::guardar
        )

        uiState.mensajeExito?.let { msj ->
            Text(
                text = msj,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Evaluación exhaustiva de Fase (Punto de Control 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val fase = uiState.fase) {
                ProductoUiState.Fase.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                ProductoUiState.Fase.SinProductos -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null)
                        Text("Aún no hay productos registrados")
                    }
                }

                is ProductoUiState.Fase.ConProductos -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(fase.productos, key = { it.id }) { producto ->
                            ProductoItem(
                                producto = producto,
                                operacion = uiState.operacion,
                                onEdit = { viewModel.seleccionarParaEditar(producto) },
                                onDelete = { viewModel.eliminar(producto.id) }
                            )
                        }
                    }
                }

                is ProductoUiState.Fase.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(fase.mensaje, color = MaterialTheme.colorScheme.error)
                        FilledTonalButton(onClick = viewModel::cargarProductos) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioSection(
    formulario: FormularioProducto,
    operacion: ProductoUiState.Operacion,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (formulario.id == null) "Nuevo Producto" else "Editar Producto #${formulario.id}")

            OutlinedTextField(
                value = formulario.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre") },
                isError = formulario.nombreError != null,
                supportingText = { formulario.nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formulario.precio,
                    onValueChange = onPrecioChange,
                    label = { Text("Precio") },
                    isError = formulario.precioError != null,
                    supportingText = { formulario.precioError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = formulario.stock,
                    onValueChange = onStockChange,
                    label = { Text("Stock") },
                    isError = formulario.stockError != null,
                    supportingText = { formulario.stockError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.weight(1f)
                )
            }

            val enGuardado = operacion is ProductoUiState.Operacion.EnCurso &&
                    (operacion.tipo == ProductoUiState.Tipo.Crear || operacion.tipo == ProductoUiState.Tipo.Actualizar)

            Button(
                onClick = onGuardar,
                enabled = !enGuardado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (enGuardado) "Guardando…" else "Guardar")
            }
        }
    }
}

@Composable
private fun ProductoItem(
    producto: Producto,
    operacion: ProductoUiState.Operacion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text("S/ ${producto.precio}  ·  Stock: ${producto.stock} u.")
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }

            val enEliminacion = operacion is ProductoUiState.Operacion.EnCurso &&
                    operacion.tipo == ProductoUiState.Tipo.Eliminar

            IconButton(onClick = onDelete, enabled = !enEliminacion) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}