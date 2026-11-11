package pe.edu.upeu.pharmamobil.data.local
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(
    base: PharmaDb
) {

    private val consultas = base.productoQueries

    // Observar todos los productos locales
    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas ->
                filas.map { fila ->
                    Producto(
                        id = fila.id,
                        nombre = fila.nombre,
                        precio = fila.precio,
                        stock = fila.stock.toInt()
                    )
                }
            }

    // Obtener productos pendientes de sincronización
    fun pendientesPorEnviar(maxIntentos: Long): List<Producto> =
        consultas.pendientesPorEnviar(maxIntentos)
            .executeAsList()
            .map { fila ->
                Producto(
                    id = fila.id,
                    nombre = fila.nombre,
                    precio = fila.precio,
                    stock = fila.stock.toInt()
                )
            }

    // Contar productos pendientes
    fun contarPendientes(maxIntentos: Long): Long =
        consultas.contarPendientes(maxIntentos)
            .executeAsOne()

    // Contar productos con sincronización fallida
    fun contarFallidas(maxIntentos: Long): Long =
        consultas.contarFallidas(maxIntentos)
            .executeAsOne()

    // Guardar un producto nuevo como pendiente
    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal()
            .executeAsOne()

        val localId = minOf(menor, 0L) - 1L

        val local = producto.copy(id = localId)

        consultas.insertar(
            id = local.id,
            nombre = local.nombre,
            precio = local.precio,
            stock = local.stock.toLong(),
            pendiente = 1L,
            intentos = 0L,
            ultimoError = null
        )

        return local
    }

    // Actualizar un producto local y marcarlo como pendiente
    fun actualizarLocal(producto: Producto) {
        consultas.actualizar(
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            id = producto.id
        )
    }

    // Registrar un intento fallido de sincronización
    fun sumarIntento(id: Long, motivo: String) {
        consultas.sumarIntento(
            motivo = motivo,
            id = id
        )
    }

    // Marcar un producto como fallido permanentemente
    fun marcarFallida(id: Long, motivo: String) {
        consultas.marcarFallida(
            motivo = motivo,
            id = id
        )
    }

    // Confirmar que el producto fue sincronizado con el servidor
    fun confirmarSincronizacion(
        idLocal: Long,
        idServidor: Long
    ) {
        consultas.transaction {
            if (idLocal != idServidor) {
                consultas.eliminarPorId(idServidor)
            }

            consultas.actualizarIdSincronizado(
                idLocal = idLocal,
                idServidor = idServidor
            )
        }
    }

    // Eliminar un producto local
    fun eliminarLocal(id: Long) {
        consultas.eliminarPorId(id)
    }

    // Reemplazar los productos sincronizados por los recibidos del servidor
    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()

            productos.forEach { producto ->
                consultas.insertar(
                    id = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio,
                    stock = producto.stock.toLong(),
                    pendiente = 0L,
                    intentos = 0L,
                    ultimoError = null
                )
            }
        }
    }
}
