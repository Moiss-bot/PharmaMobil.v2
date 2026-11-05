package pe.edu.upeu.pharmamobil.di

import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.firebase.TelemetriaFirebase
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.port.Telemetria
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel

private const val CATEGORIA_POR_DEFECTO = 1L

val networkModule = module {
    single(named("urlBase")) { "http://10.0.2.2:8080/api/v1/" }
    single { crearHttpClient(get(), get(named("urlBase")), get()) }
}

val dataModule = module {
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
    single { ProductoApi(get()) }

    // --- SESIÓN 11 & 13: BASE DE DATOS Y TELEMETRÍA ---
    single { PharmaDb(get<CreadorDeDriver>().crear()) }
    single { ProductoLocal(get()) }

    // Paso 6 (Guía 13): Inyección de Telemetría Firebase detrás de la interfaz del dominio
    single<Telemetria> { TelemetriaFirebase() }

    // Repositorio Offline-First como fuente de verdad
    single<ProductoRepository> {
        ProductoRepositorioOffline(
            local = get(),
            remoto = get(),
            categoriaPorDefecto = CATEGORIA_POR_DEFECTO
        )
    }
}

val domainModule = module {
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    // Paso 3 (Guía 13): Caso de uso de Sincronización con backoff exponencial y conciliación
    factory { SincronizarProductosUseCase(local = get(), remoto = get(), categoriaPorDefecto = CATEGORIA_POR_DEFECTO) }
}

val presentationModule = module {
    factory { SesionViewModel(get()) }
    factory { ProductosViewModel(get(), get(), get(), get(), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(networkModule, dataModule, domainModule, presentationModule, platformModule)
}