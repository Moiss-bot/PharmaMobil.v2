
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
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline

import pe.edu.upeu.pharmamobil.db.PharmaDb

import pe.edu.upeu.pharmamobil.domain.port.Telemetria
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase

import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase

import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel

private const val CATEGORIA_POR_DEFECTO = 1L

// --------------------------------------------------
// NETWORK MODULE
// --------------------------------------------------

val networkModule = module {

    single(named("urlBase")) {
        "http://10.0.2.2:8080/api/v1/"
    }

    single {
        crearHttpClient(
            get(),
            get(named("urlBase")),
            get()
        )
    }
}

// --------------------------------------------------
// DATA MODULE
// --------------------------------------------------

val dataModule = module {

    // Repositorio de autenticación
    single<AuthRepository> {
        AuthRepositorioRest(get(), get())
    }

    // Repositorio de clientes
    single<ClienteRepository> {
        ClienteRepositorioEnMemoria()
    }

    // API de productos
    single {
        ProductoApi(get())
    }

    // Base de datos
    single {
        PharmaDb(get<CreadorDeDriver>().crear())
    }

    // Productos locales
    single {
        ProductoLocal(get())
    }

    // Telemetría Firebase
    single<Telemetria> {
        TelemetriaFirebase()
    }

    // Repositorio Offline-First
    single<ProductoRepository> {
        ProductoRepositorioOffline(
            local = get(),
            remoto = get(),
            categoriaPorDefecto = CATEGORIA_POR_DEFECTO
        )
    }
}

// --------------------------------------------------
// DOMAIN MODULE
// --------------------------------------------------

val domainModule = module {

    // Casos de uso de productos
    factory {
        ListarProductosUseCase(get())
    }

    factory {
        RegistrarProductoUseCase(get())
    }

    factory {
        ActualizarProductoUseCase(get())
    }

    factory {
        EliminarProductoUseCase(get())
    }

    factory {
        SincronizarProductosUseCase(
            local = get(),
            remoto = get(),
            categoriaPorDefecto = CATEGORIA_POR_DEFECTO
        )
    }

    // Casos de uso de clientes
    factory {
        RegistrarClienteUseCase(get())
    }

    factory {
        ListarClientesUseCase(get())
    }
}

// --------------------------------------------------
// PRESENTATION MODULE
// --------------------------------------------------

val presentationModule = module {

    // Sesión
    factory {
        SesionViewModel(get())
    }

    // Productos
    factory {
        ProductosViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // Clientes
    factory {
        ClienteViewModel(
            registrarCliente = get(),
            listarClientes = get()
        )
    }
}

// --------------------------------------------------
// INICIALIZACIÓN DE KOIN
// --------------------------------------------------

fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {

    appDeclaration()

    modules(
        networkModule,
        dataModule,
        domainModule,
        presentationModule,
        platformModule
    )
}
