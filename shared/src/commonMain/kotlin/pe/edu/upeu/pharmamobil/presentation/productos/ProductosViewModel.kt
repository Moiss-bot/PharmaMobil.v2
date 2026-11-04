package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

class ProductosViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(ProductoUiState())
    val estado: StateFlow<ProductoUiState> = _estado.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() = viewModelScope.launch {
        _estado.update { it.copy(fase = ProductoUiState.Fase.Cargando) }
        listarProductosUseCase()
            .onSuccess { lista ->
                _estado.update {
                    it.copy(
                        fase = if (lista.isEmpty()) ProductoUiState.Fase.SinProductos
                        else ProductoUiState.Fase.ConProductos(lista)
                    )
                }
            }
            .onFailure { fallo ->
                val msj = mensajeDe(fallo)
                _estado.update { it.copy(fase = ProductoUiState.Fase.Error(msj)) }
            }
    }

    fun onNombreChange(nombre: String) {
        _estado.update { it.copy(formulario = it.formulario.copy(nombre = nombre, nombreError = null)) }
    }

    fun onPrecioChange(precio: String) {
        _estado.update { it.copy(formulario = it.formulario.copy(precio = precio, precioError = null)) }
    }

    fun onStockChange(stock: String) {
        _estado.update { it.copy(formulario = it.formulario.copy(stock = stock, stockError = null)) }
    }

    fun guardar() = viewModelScope.launch {
        val form = _estado.value.formulario
        val precioDouble = form.precio.toDoubleOrNull() ?: 0.0
        val stockInt = form.stock.toIntOrNull() ?: 0

        val esEdicion = form.id != null
        val tipoOp = if (esEdicion) ProductoUiState.Tipo.Actualizar else ProductoUiState.Tipo.Crear

        _estado.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(tipoOp)) }

        val producto = Producto(
            id = form.id ?: 0L,
            nombre = form.nombre,
            precio = precioDouble,
            stock = stockInt
        )

        val resultado = if (esEdicion) {
            actualizarProductoUseCase(producto)
        } else {
            registrarProductoUseCase(producto)
        }

        resultado
            .onSuccess {
                _estado.update {
                    it.copy(
                        formulario = FormularioProducto(),
                        operacion = ProductoUiState.Operacion.Inactiva,
                        mensajeExito = if (esEdicion) "Producto actualizado" else "Producto registrado"
                    )
                }
                cargarProductos()
            }
            .onFailure { fallo ->
                manejarFallo(fallo)
            }
    }

    fun seleccionarParaEditar(producto: Producto) {
        _estado.update {
            it.copy(
                formulario = FormularioProducto(
                    id = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio.toString(),
                    stock = producto.stock.toString()
                )
            )
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _estado.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Tipo.Eliminar)) }
        eliminarProductoUseCase(id)
            .onSuccess {
                _estado.update {
                    it.copy(
                        operacion = ProductoUiState.Operacion.Inactiva,
                        mensajeExito = "Producto eliminado"
                    )
                }
                cargarProductos()
            }
            .onFailure { fallo ->
                manejarFallo(fallo)
            }
    }

    private fun manejarFallo(fallo: Throwable) {
        val error = (fallo as? ErrorApiException)?.error
        when (error) {
            is ErrorApi.Validacion -> {
                _estado.update {
                    it.copy(
                        operacion = ProductoUiState.Operacion.Inactiva,
                        formulario = it.formulario.copy(
                            nombreError = error.porCampo["nombre"],
                            precioError = error.porCampo["precio"],
                            stockError = error.porCampo["stock"]
                        )
                    )
                }
            }
            else -> {
                _estado.update {
                    it.copy(operacion = ProductoUiState.Operacion.Fallida(mensajeDe(fallo)))
                }
            }
        }
    }

    private fun mensajeDe(fallo: Throwable): String {
        val error = (fallo as? ErrorApiException)?.error
        return when (error) {
            is ErrorApi.SinConexion -> "Sin conexión a internet"
            is ErrorApi.TiempoAgotado -> "El servidor tardó demasiado en responder"
            is ErrorApi.NoEncontrado -> "Recurso no encontrado"
            is ErrorApi.Conflicto -> error.mensaje
            is ErrorApi.Servidor -> "Error interno del servidor"
            else -> fallo.message ?: "Error desconocido"
        }
    }
}