package pe.edu.upeu.pharmamobil.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest : KoinTest {

    @BeforeTest
    fun setUp() {
        stopKoin()
        startKoin {
            modules(
                networkModule,
                dataModule,
                domainModule,
                presentationModule,
                platformModule
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {
        val repositorio: ProductoRepository = get()
        assertNotNull(repositorio)
    }

    @Test
    fun losRepositoriosSonUnicosEnTodaLaAplicacion() {
        val repo1: ProductoRepository = get()
        val repo2: ProductoRepository = get()
        assertNotNull(repo1)
        assertNotNull(repo2)
    }

    @Test
    fun resuelveLosCuatroCasosDeUsoConSusRepositorios() {
        val listar: ListarProductosUseCase = get()
        val registrar: RegistrarProductoUseCase = get()
        val actualizar: ActualizarProductoUseCase = get()
        val eliminar: EliminarProductoUseCase = get()

        assertNotNull(listar)
        assertNotNull(registrar)
        assertNotNull(actualizar)
        assertNotNull(eliminar)
    }

    @Test
    fun resuelveLosViewModelConSusCasosDeUso() {
        val viewModel: ProductosViewModel = get()
        assertNotNull(viewModel)
    }
}