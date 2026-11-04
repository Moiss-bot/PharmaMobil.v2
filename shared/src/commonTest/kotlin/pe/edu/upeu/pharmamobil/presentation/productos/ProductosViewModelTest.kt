package pe.edu.upeu.pharmamobil.presentation.productos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun el_listado_vacio_termina_en_SinProductos() = runTest(testDispatcher) {
        val repositorio = FakeProductoRepository(productos = emptyList())
        val viewModel = ProductosViewModel(
            ListarProductosUseCase(repositorio),
            RegistrarProductoUseCase(repositorio),
            ActualizarProductoUseCase(repositorio),
            EliminarProductoUseCase(repositorio)
        )

        advanceUntilIdle()

        assertIs<ProductoUiState.Fase.SinProductos>(viewModel.estado.value.fase)
    }

    @Test
    fun la_carga_exitosa_pasa_a_ConProductos() = runTest(testDispatcher) {
        val listaInicial = listOf(Producto(1L, "Paracetamol", 5.0, 10))
        val repositorio = FakeProductoRepository(productos = listaInicial)
        val viewModel = ProductosViewModel(
            ListarProductosUseCase(repositorio),
            RegistrarProductoUseCase(repositorio),
            ActualizarProductoUseCase(repositorio),
            EliminarProductoUseCase(repositorio)
        )

        advanceUntilIdle()

        val faseActual = viewModel.estado.value.fase
        assertIs<ProductoUiState.Fase.ConProductos>(faseActual)
        assertEquals(1, faseActual.productos.size)
        assertEquals("Paracetamol", faseActual.productos.first().nombre)
    }

    @Test
    fun ante_un_error_de_validacion_se_asignan_errores_al_formulario_y_la_fase_no_cambia() = runTest(testDispatcher) {
        val repositorio = FakeProductoRepository(
            productos = listOf(Producto(1L, "Ibuprofeno", 8.0, 20)),
            debeLanzarErrorValidacion = true
        )
        val viewModel = ProductosViewModel(
            ListarProductosUseCase(repositorio),
            RegistrarProductoUseCase(repositorio),
            ActualizarProductoUseCase(repositorio),
            EliminarProductoUseCase(repositorio)
        )

        advanceUntilIdle() // Carga inicial

        viewModel.onNombreChange("Pa") // < 3 caracteres
        viewModel.onPrecioChange("5.0")
        viewModel.onStockChange("10")
        viewModel.guardar()

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals("El nombre debe tener al menos 3 caracteres", estado.formulario.nombreError)
        assertIs<ProductoUiState.Fase.ConProductos>(estado.fase)
    }

    @Test
    fun la_eliminacion_mantiene_inactiva_la_operacion_al_finalizar_y_actualiza_la_lista() = runTest(testDispatcher) {
        val repositorio = FakeProductoRepository(
            productos = listOf(Producto(1L, "Amoxicilina", 12.0, 15))
        )
        val viewModel = ProductosViewModel(
            ListarProductosUseCase(repositorio),
            RegistrarProductoUseCase(repositorio),
            ActualizarProductoUseCase(repositorio),
            EliminarProductoUseCase(repositorio)
        )

        advanceUntilIdle()

        viewModel.eliminar(1L)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(ProductoUiState.Operacion.Inactiva, estado.operacion)
        assertEquals("Producto eliminado", estado.mensajeExito)
        assertIs<ProductoUiState.Fase.SinProductos>(estado.fase)
    }
}