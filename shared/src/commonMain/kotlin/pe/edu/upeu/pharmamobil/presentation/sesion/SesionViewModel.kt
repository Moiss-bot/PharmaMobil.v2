package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null
)

class SesionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(SesionUiState())
    val estado: StateFlow<SesionUiState> = _estado.asStateFlow()

    init {
        verificarSesionGuardada()
    }

    fun onUsuarioChange(valor: String) {
        _estado.update { it.copy(usuario = valor, error = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, error = null) }
    }

    fun iniciarSesion() {
        val usuarioActual = _estado.value.usuario
        val claveActual = _estado.value.clave

        if (usuarioActual.isBlank() || claveActual.isBlank()) {
            _estado.update { it.copy(error = "Usuario y contraseña requeridos") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val sesion = authRepository.iniciarSesion(Credenciales(usuarioActual, claveActual))
                _estado.update { it.copy(cargando = false, sesion = sesion) }
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = "Credenciales inválidas o error de red") }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            _estado.update { SesionUiState() }
        }
    }

    private fun verificarSesionGuardada() {
        viewModelScope.launch {
            val sesion = authRepository.sesionGuardada()
            if (sesion != null) {
                _estado.update { it.copy(sesion = sesion) }
            }
        }
    }
}