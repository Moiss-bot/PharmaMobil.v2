package pe.edu.upeu.pharmamobil.presentation.productos

import pe.edu.upeu.pharmamobil.domain.model.Producto

data class ProductosUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioState = FormularioState(),
    val sincronizacion: SincronizacionUiState = SincronizacionUiState(),
    val avisoRed: String? = null,
    val mensajeExito: String? = null,
    val ultimaSincronizacionMillis: Long? = null
) {
    sealed interface Fase {
        data object Cargando : Fase
        data object SinProductos : Fase
        data class ConProductos(val lista: List<Producto>) : Fase
    }
}

data class FormularioState(
    val id: Long? = null,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val guardando: Boolean = false
)