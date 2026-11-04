package pe.edu.upeu.pharmamobil.presentation.productos

import pe.edu.upeu.pharmamobil.domain.model.Producto

sealed interface ProductosUiState {
    data object Loading : ProductosUiState
    data class Success(val productos: List<Producto>) : ProductosUiState
    data class Error(val mensaje: String) : ProductosUiState
}