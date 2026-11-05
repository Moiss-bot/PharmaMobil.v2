package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.mapper.aResultadoEnvio
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequest
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync

class SincronizarProductosUseCase(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val categoriaPorDefecto: Long = 1L
) {
    suspend operator fun invoke(): ResumenSync {
        var enviadas = 0
        var fallidas = 0

        val pendientes = local.pendientesPorEnviar(MAX_INTENTOS)

        for (fila in pendientes) {
            val resultado = try {
                if (fila.id < 0) {
                    val respuesta = remoto.crear(fila.toRequest(categoriaPorDefecto))
                    ResultadoEnvio.Exito(respuesta.id)
                } else {
                    val respuesta = remoto.actualizar(fila.id, fila.toRequest(categoriaPorDefecto))
                    ResultadoEnvio.Exito(respuesta.id)
                }
            } catch (e: Exception) {
                e.aResultadoEnvio()
            }

            when (resultado) {
                is ResultadoEnvio.Exito -> {
                    local.confirmarSincronizacion(fila.id, resultado.idServidor)
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.id, resultado.motivo)
                    fallidas++
                }
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(fila.id, resultado.motivo)
                    delay(calcularEspera(1)) // Retardo básico
                }
            }
        }

        try {
            val remotosDto = remoto.listar().contenido
            local.reemplazarSincronizados(remotosDto.map { it.toDomain() })
        } catch (_: Exception) {
        }

        return ResumenSync(enviadas, fallidas, local.contarPendientes(MAX_INTENTOS))
    }

    private fun calcularEspera(intentos: Int): Long {
        return (1000L shl intentos.coerceAtMost(4))
    }

    companion object {
        const val MAX_INTENTOS = 5L
    }
}