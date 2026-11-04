package pe.edu.upeu.pharmamobil.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel

// Módulo de datos según la Guía 07
val dataModule = module {
    single { crearHttpClient(get(), get(named("urlBase"))) }
    single { ProductoApi(get()) }
    single { ProductoRepositorioEnMemoria() }
    single<ProductoRepository> { ProductoRepositorioRest(get(), get()) }
    viewModel { ProductosViewModel(get()) }
}

// Función global que llama androidApp al arrancar
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(dataModule, platformModule)
}