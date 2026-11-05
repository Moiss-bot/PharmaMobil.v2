package pe.edu.upeu.pharmamobil.presentation.productos

data class SincronizacionUiState(
    val enCurso: Boolean = false,
    val pendientes: Long = 0,
    val fallidas: Long = 0,
    val ultimaSyncMillis: Long? = null
)