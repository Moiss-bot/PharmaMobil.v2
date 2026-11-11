package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import io.ktor.http.encodedPath

fun crearHttpClient(
    engine: HttpClientEngine,
    urlBase: String,
    almacen: AlmacenSeguro,
    esDepuracion: Boolean = true
): HttpClient = HttpClient(engine) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }

    // Plugin Auth: adjunta automáticamente Authorization: Bearer <token>
    install(Auth) {
        bearer {
            loadTokens {
                almacen.leerToken()?.let { token ->
                    BearerTokens(token, "")
                }
            }
            // Evita enviar el token en la petición de login
            sendWithoutRequest { peticion ->
                !peticion.url.encodedPath.endsWith("/auth/login")
            }
        }
    }

    install(Logging) {
        level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
        // Sanea la cabecera de autorización para mostrar "Authorization: Bearer ***" en logs
        sanitizeHeader { nombre -> nombre == HttpHeaders.Authorization }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 10000
    }

    defaultRequest {
        url(urlBase)
        contentType(ContentType.Application.Json)
    }
}