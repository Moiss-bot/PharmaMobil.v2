
package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.CancellationException
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

        sincronizarConRegistroDeErrores()

        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id != 0L) {
            local.actualizarLocal(producto)
        } else {
            local.guardarPendiente(producto)
        }

        sincronizarConRegistroDeErrores()

        return producto
    }

    override suspend fun eliminar(id: Long) {
        local.desactivarLocal(id)

        if (id > 0L) {
            try {
                remoto.eliminar(id)

                println(
                    "✅ PharmMobile: Producto $id desactivado en el servidor."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println(
                    "❌ PharmMobile: Error al desactivar producto $id: ${e.message}"
                )
            }
        }
    }

    override suspend fun sincronizar() {
        println("🔄 PharmMobile: Iniciando sincronización...")

        // Enviar productos locales pendientes
        val erroresEnvio = subirPendientes()

        // Descargar productos desde el servidor
        val respuestaRemota = remoto.listar()

        val productosRemotos = respuestaRemota.contenido.map {
            it.toDomain()
        }

        // Actualizar productos sincronizados localmente
        local.reemplazarSincronizados(productosRemotos)

        println(
            "📥 PharmMobile: Descarga de ${productosRemotos.size} productos completada."
        )

        // Si algún producto no se envió, informar el error al ViewModel
        if (erroresEnvio.isNotEmpty()) {
            val detalle = erroresEnvio.joinToString(separator = "; ")

            throw IllegalStateException(
                "Sincronización incompleta. Productos pendientes: $detalle"
            )
        }

        println("✅ PharmMobile: Sincronización completada correctamente.")
    }

    private suspend fun subirPendientes(): List<String> {
        val pendientes = local.pendientesPorEnviar(5L)

        val errores = mutableListOf<String>()

        println(
            "📦 PharmMobile: Productos pendientes encontrados: ${pendientes.size}"
        )

        pendientes.forEach { pendiente ->

            try {
                val request = pendiente.toRequest(categoriaPorDefecto)

                val respuesta = if (pendiente.id < 0L) {

                    println(
                        "⬆️ PharmMobile: Creando producto ${pendiente.nombre}..."
                    )

                    remoto.crear(request)

                } else {

                    println(
                        "⬆️ PharmMobile: Actualizando producto ${pendiente.nombre}, ID ${pendiente.id}..."
                    )

                    remoto.actualizar(pendiente.id, request)
                }

                // Marcar como sincronizado únicamente tras respuesta exitosa
                local.confirmarSincronizacion(
                    idLocal = pendiente.id,
                    idServidor = respuesta.id
                )

                println(
                    "✅ PharmMobile: Producto ${pendiente.nombre} sincronizado. " +
                            "ID local: ${pendiente.id}, ID servidor: ${respuesta.id}"
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                val mensaje = e.message ?: "Error desconocido al sincronizar"

                errores.add("${pendiente.nombre}: $mensaje")

                local.sumarIntento(
                    id = pendiente.id,
                    motivo = mensaje
                )

                println(
                    "❌ PharmMobile: Falló el envío de ${pendiente.nombre}. " +
                            "ID: ${pendiente.id}. Error: $mensaje"
                )

                e.printStackTrace()
            }
        }

        return errores
    }

    private suspend fun sincronizarConRegistroDeErrores() {
        try {
            sincronizar()

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            println(
                "❌ PharmMobile: Error al sincronizar: ${e.message}"
            )

            e.printStackTrace()
        }
    }
}
