package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {

    private fun baseEnMemoria(): PharmaDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        return PharmaDb(driver)
    }

    private fun crearClienteMock(respuestaJson: String, codigoStatus: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { _ ->
                    respond(
                        content = respuestaJson,
                        status = codigoStatus,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
    }

    // PRUEBA 1: Sincronización con red
    @Test
    fun prueba1_sincronizacionConRed() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)
        val jsonMock = """{ "contenido": [ {"id": 1, "nombre": "Paracetamol", "precio": 5.0, "stock": 20} ] }"""
        val api = ProductoApi(crearClienteMock(jsonMock))
        val repo = ProductoRepositorioOffline(local, api)

        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(1, lista.size)
        assertEquals("Paracetamol", lista[0].nombre)
    }

    // PRUEBA 2: Registro sin red (Guarda en SQLite con id local negativo)
    @Test
    fun prueba2_registroSinRed() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)
        val api = ProductoApi(crearClienteMock("", HttpStatusCode.ServiceUnavailable))
        val repo = ProductoRepositorioOffline(local, api)

        val nuevo = Producto(0L, "Ibuprofeno", 8.5, 15)
        val guardado = repo.registrar(nuevo)

        assertTrue(guardado.id < 0)
        val pendientes = local.pendientes()
        assertEquals(1, pendientes.size)
        assertEquals("Ibuprofeno", pendientes[0].nombre)
    }

    // PRUEBA 3: Envío al reconectar
    @Test
    fun prueba3_envioAlReconectar() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        // 1. Crear producto sin red
        val apiError = ProductoApi(crearClienteMock("", HttpStatusCode.ServiceUnavailable))
        val repoOffline = ProductoRepositorioOffline(local, apiError)
        repoOffline.registrar(Producto(0L, "Amoxicilina", 12.0, 10))
        assertEquals(1, local.pendientes().size)

        // 2. Reconectar con red disponible
        val jsonMock = """{ "contenido": [ {"id": 100, "nombre": "Amoxicilina", "precio": 12.0, "stock": 10} ] }"""
        val apiOk = ProductoApi(crearClienteMock(jsonMock))
        val repoOnline = ProductoRepositorioOffline(local, apiOk)

        repoOnline.sincronizar()

        assertEquals(0, local.pendientes().size)
        val lista = repoOnline.observar().first()
        assertEquals(100L, lista[0].id)
    }

    // PRUEBA 4: La bajada no borra lo pendiente
    @Test
    fun prueba4_bajadaNoBorraPendientes() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        // Registrar producto pendiente offline
        local.guardarPendiente(Producto(0L, "Local Pendiente", 3.0, 5))

        val jsonMock = """{ "contenido": [ {"id": 10, "nombre": "Servidor", "precio": 15.0, "stock": 30} ] }"""
        val api = ProductoApi(crearClienteMock(jsonMock))
        val repo = ProductoRepositorioOffline(local, api)

        repo.sincronizar()

        val lista = repo.observar().first()
        // Deben convivir 2: el servidor + el local pendiente resguardado
        assertEquals(2, lista.size)
    }

    // PRUEBA 5: Identificadores sin colisión
    @Test
    fun prueba5_identificadoresSinColision() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        val prodLocal = local.guardarPendiente(Producto(0L, "Prod Local", 2.0, 5)) // ID: -1
        val jsonMock = """{ "contenido": [ {"id": 1, "nombre": "Prod Remoto", "precio": 10.0, "stock": 20} ] }"""
        val api = ProductoApi(crearClienteMock(jsonMock))
        val repo = ProductoRepositorioOffline(local, api)

        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(2, lista.size)
        assertTrue(prodLocal.id < 0)
        assertTrue(lista.any { it.id == 1L })
    }

    // PRUEBA 6: Sincronizar dos veces no duplica
    @Test
    fun prueba6_sincronizarDosVecesNoDuplica() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)
        val jsonMock = """{ "contenido": [ {"id": 1, "nombre": "Vitamina C", "precio": 4.0, "stock": 50} ] }"""
        val api = ProductoApi(crearClienteMock(jsonMock))
        val repo = ProductoRepositorioOffline(local, api)

        repo.sincronizar()
        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(1, lista.size)
    }
}