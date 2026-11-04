package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Motor Darwin para iOS
    single<HttpClientEngine> { Darwin.create() }
    // URL Base para iOS (localhost del simulador apunta a la PC host)
    single(named("urlBase")) { "http://localhost:8080/api/v1/" }
}