
package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(base: PharmaDb) {

    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> {
        return consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas ->
                filas.map { fila ->
                    Producto(
                        id = fila.id,
                        nombre = fila.nombre,
                        precio = fila.precio,
                        stock = fila.stock.toInt(),
                        estado = fila.estado == 1L
                    )
                }
            }
    }

    fun pendientesPorEnviar(maxIntentos: Long): List<Producto> {
        return consultas.pendientesPorEnviar(maxIntentos)
            .executeAsList()
            .map { fila ->
                Producto(
                    id = fila.id,
                    nombre = fila.nombre,
                    precio = fila.precio,
                    stock = fila.stock.toInt(),
                    estado = fila.estado == 1L
                )
            }
    }

    fun contarPendientes(maxIntentos: Long): Long {
        return consultas.contarPendientes(maxIntentos).executeAsOne()
    }

    fun contarFallidas(maxIntentos: Long): Long {
        return consultas.contarFallidas(maxIntentos).executeAsOne()
    }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val localId = minOf(menor, 0L) - 1L

        val productoLocal = producto.copy(
            id = localId,
            estado = true
        )

        consultas.insertar(
            id = productoLocal.id,
            nombre = productoLocal.nombre,
            precio = productoLocal.precio,
            stock = productoLocal.stock.toLong(),
            estado = if (productoLocal.estado) 1L else 0L,
            pendiente = 1L,
            intentos = 0L,
            ultimoError = null
        )

        return productoLocal
    }

    fun actualizarLocal(producto: Producto) {
        consultas.actualizar(
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            estado = if (producto.estado) 1L else 0L,
            id = producto.id
        )
    }

    fun desactivarLocal(id: Long) {
        consultas.desactivarProducto(id)
    }

    fun activarLocal(id: Long) {
        consultas.activarProducto(id)
    }

    fun sumarIntento(id: Long, motivo: String) {
        consultas.sumarIntento(
            id = id,
            motivo = motivo
        )
    }

    fun marcarFallida(id: Long, motivo: String) {
        consultas.marcarFallida(
            id = id,
            motivo = motivo
        )
    }

    fun confirmarSincronizacion(
        idLocal: Long,
        idServidor: Long
    ) {
        consultas.transaction {

            if (idLocal != idServidor) {
                consultas.eliminarPorId(idServidor)
            }

            consultas.confirmarSincronizacion(
                idLocal = idLocal,
                idServidor = idServidor
            )
        }
    }

    fun eliminarLocal(id: Long) {
        consultas.eliminarPorId(id)
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {

            // Se eliminan únicamente productos sincronizados.
            // Los pendientes deben permanecer en la base local.
            consultas.borrarSincronizados()

            productos.forEach { producto ->
                consultas.insertar(
                    id = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio,
                    stock = producto.stock.toLong(),
                    estado = if (producto.estado) 1L else 0L,
                    pendiente = 0L,
                    intentos = 0L,
                    ultimoError = null
                )
            }
        }
    }
}
