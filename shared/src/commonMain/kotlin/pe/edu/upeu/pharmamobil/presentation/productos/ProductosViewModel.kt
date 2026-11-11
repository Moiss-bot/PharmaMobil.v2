
package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase

class ProductosViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val sincronizarProductosUseCase: SincronizarProductosUseCase
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioState())
    private val _sincronizando = MutableStateFlow(false)
    private val _avisoRed = MutableStateFlow<String?>(null)

    // Evita sincronizaciones simultáneas
    private val mutexSincronizacion = Mutex()

    init {
        // Intenta sincronizar al iniciar el ViewModel
        sincronizar()
    }

    val uiState: StateFlow<ProductosUiState> = combine(
        listarProductosUseCase(),
        _formulario,
        _sincronizando,
        _avisoRed
    ) { productos: List<Producto>,
        form: FormularioState,
        sync: Boolean,
        aviso: String? ->

        val fase = if (productos.isEmpty()) {
            ProductosUiState.Fase.SinProductos
        } else {
            ProductosUiState.Fase.ConProductos(productos)
        }

        // Cuenta productos nuevos con ID local negativo
        val pendientesCount = productos.count { it.id < 0 }.toLong()

        ProductosUiState(
            fase = fase,
            formulario = form,
            sincronizacion = SincronizacionUiState(
                enCurso = sync,
                pendientes = pendientesCount
            ),
            avisoRed = aviso
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductosUiState()
    )

    fun onNombreChange(valor: String) {
        _formulario.update {
            it.copy(nombre = valor)
        }
    }

    fun onPrecioChange(valor: String) {
        _formulario.update {
            it.copy(precio = valor)
        }
    }

    fun onStockChange(valor: String) {
        _formulario.update {
            it.copy(stock = valor)
        }
    }

    fun seleccionarParaEditar(producto: Producto) {
        _formulario.update {
            it.copy(
                id = producto.id,
                nombre = producto.nombre,
                precio = producto.precio.toString(),
                stock = producto.stock.toString()
            )
        }
    }

    fun guardar() {
        val currentForm = _formulario.value

        val nombre = currentForm.nombre.trim()
        val precio = currentForm.precio.toDoubleOrNull()
        val stock = currentForm.stock.toIntOrNull()

        // Validación de datos
        if (nombre.isBlank()) {
            _avisoRed.value = "Ingrese el nombre del producto"
            return
        }

        if (precio == null || precio < 0) {
            _avisoRed.value = "Ingrese un precio válido"
            return
        }

        if (stock == null || stock < 0) {
            _avisoRed.value = "Ingrese un stock válido"
            return
        }

        if (currentForm.guardando) return

        viewModelScope.launch {
            _formulario.update {
                it.copy(guardando = true)
            }

            _avisoRed.value = null

            try {
                val producto = Producto(
                    id = currentForm.id ?: 0L,
                    nombre = nombre,
                    precio = precio,
                    stock = stock
                )

                if (currentForm.id == null || currentForm.id == 0L) {
                    registrarProductoUseCase(producto)
                } else {
                    actualizarProductoUseCase(producto)
                }

                // Limpia el formulario luego de guardar
                _formulario.update {
                    FormularioState()
                }

            } catch (e: Exception) {
                _avisoRed.value =
                    "Error al guardar producto: ${e.message}"

            } finally {
                _formulario.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            try {
                eliminarProductoUseCase(id)
                _avisoRed.value = null
            } catch (e: Exception) {
                _avisoRed.value =
                    "Error al eliminar producto: ${e.message}"
            }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            mutexSincronizacion.withLock {
                _sincronizando.value = true
                _avisoRed.value = null

                try {
                    println("🔄 PharmMobile: iniciando sincronización")

                    sincronizarProductosUseCase()

                    println("✅ PharmMobile: sincronización ejecutada")

                } catch (e: Exception) {
                    println("❌ PharmMobile: error de sincronización")
                    println(e.message)

                    _avisoRed.value =
                        "Error al sincronizar: ${e.message}"

                } finally {
                    _sincronizando.value = false
                }
            }
        }
    }
}
