
package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequest
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val categoriaPorDefecto: Long = 1L
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> =
        local.observar()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)

        intentar {
            sincronizar()
        }

        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id != 0L) {
            local.actualizarLocal(producto)
        } else {
            local.guardarPendiente(producto)
        }

        intentar {
            sincronizar()
        }

        return producto
    }

    override suspend fun eliminar(id: Long) {
        local.eliminarLocal(id)

        if (id > 0) {
            intentar {
                remoto.eliminar(id)
            }
        }
    }

    override suspend fun sincronizar() {
        subirPendientes()

        val remotosDto = remoto.listar().contenido

        local.reemplazarSincronizados(
            remotosDto.map { it.toDomain() }
        )
    }

    private suspend fun subirPendientes() {
        val pendientes = local.pendientesPorEnviar(5L)

        pendientes.forEach { pendiente ->

            try {
                val request = pendiente.toRequest(
                    categoriaPorDefecto
                )

                val respuesta = if (pendiente.id < 0) {

                    // Crear producto en el backend
                    remoto.crear(request)

                } else {

                    // Actualizar producto existente
                    remoto.actualizar(
                        pendiente.id,
                        request
                    )
                }

                // Confirmar sincronización local
                local.confirmarSincronizacion(
                    idLocal = pendiente.id,
                    idServidor = respuesta.id
                )

            } catch (e: Exception) {

                // Registrar el intento fallido
                local.sumarIntento(
                    id = pendiente.id,
                    motivo = e.message
                        ?: "Error al sincronizar producto"
                )
            }
        }
    }

    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            println("🔄 PharmMobile: Iniciando sincronización...")
            bloque()
            println("✅ PharmMobile: Sincronización ejecutada.")
        } catch (e: Exception) {
            println("❌ PharmMobile: Error al sincronizar: ${e.message}")
            e.printStackTrace()
        }
    }
}
