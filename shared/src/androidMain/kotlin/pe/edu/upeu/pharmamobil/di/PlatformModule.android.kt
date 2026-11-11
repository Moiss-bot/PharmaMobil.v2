
package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverAndroid
import pe.edu.upeu.pharmamobil.data.remote.NetworkMonitor
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid

actual val platformModule: Module = module {

    // Motor OkHttp para Android
    single<HttpClientEngine> {
        OkHttp.create()
    }

    // URL Base para Android
    single(named("urlBase")) {
        "http://10.0.2.2:8080/api/v1/"
    }

    // Compartidor
    single<Compartidor> {
        CompartidorAndroid(androidContext())
    }

    // Almacenamiento seguro
    single<AlmacenSeguro> {
        AlmacenSeguroAndroid(androidContext())
    }

    // Creador de Driver de SQLDelight
    single<CreadorDeDriver> {
        CreadorDeDriverAndroid(androidContext())
    }

    // Monitor de conexión a internet
    single<NetworkMonitor> {
        NetworkMonitor(androidContext())
    }
}
