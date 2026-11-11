package pe.edu.upeu.pharmamobil

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.mapper.aResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SincronizacionTest {

    @Test
    fun unFalloTransitorioSumaIntentoNoMarcaFallida() = runTest {
        val excepcion = Exception("503 Service Unavailable")
        val resultado = excepcion.aResultadoEnvio()

        assertTrue(resultado is ResultadoEnvio.Transitorio)
        assertEquals("503 Service Unavailable", (resultado as ResultadoEnvio.Transitorio).motivo)
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        val excepcion = Exception("409 Conflict: Registro duplicado")
        val resultado = excepcion.aResultadoEnvio()

        assertTrue(resultado is ResultadoEnvio.Permanente)
        assertEquals("409 Conflict: Registro duplicado", (resultado as ResultadoEnvio.Permanente).motivo)
    }
}