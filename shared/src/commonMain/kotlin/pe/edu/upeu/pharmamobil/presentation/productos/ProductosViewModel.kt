package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductosViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<ProductosUiState>(ProductosUiState.Loading)
    val estado: StateFlow<ProductosUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() = viewModelScope.launch {
        _estado.value = ProductosUiState.Loading
        runCatching {
            repository.listar()
        }.onSuccess { productos: List<Producto> ->
            _estado.value = ProductosUiState.Success(productos)
        }.onFailure { error: Throwable ->
            _estado.value = ProductosUiState.Error(error.message ?: "Error al conectar con el servidor")
        }
    }
}