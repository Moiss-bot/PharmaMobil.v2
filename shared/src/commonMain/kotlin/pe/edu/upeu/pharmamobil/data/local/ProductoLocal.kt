package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.IO)
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

    fun pendientesPorEnviar(maxIntentos: Long): List<Producto> =
        consultas.pendientesPorEnviar(maxIntentos).executeAsList().map { fila ->
            Producto(
                id = fila.id,
                nombre = fila.nombre,
                precio = fila.precio,
                stock = fila.stock.toInt()
            )
        }

    fun contarPendientes(maxIntentos: Long): Long =
        consultas.contarPendientes(maxIntentos).executeAsOne()

    fun contarFallidas(maxIntentos: Long): Long =
        consultas.contarFallidas(maxIntentos).executeAsOne()

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val localId = minOf(menor, 0L) - 1L
        val local = producto.copy(id = localId)

        // Argumentos posicionales exactos para evitar errores de nombres generados por SQLDelight
        consultas.insertar(
            local.id,
            local.nombre,
            local.precio,
            local.stock.toLong(),
            1L,
            0L,
            null
        )
        return local
    }

    fun actualizarLocal(producto: Producto) {
        consultas.actualizar(
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            id = producto.id
        )
    }

    fun sumarIntento(id: Long, motivo: String) {
        consultas.sumarIntento(motivo = motivo, id = id)
    }

    fun marcarFallida(id: Long, motivo: String) {
        consultas.marcarFallida(motivo = motivo, id = id)
    }

    fun confirmarSincronizacion(idLocal: Long, idServidor: Long) {
        consultas.transaction {
            consultas.eliminarPorId(idLocal)
        }
    }

    fun eliminarLocal(id: Long) {
        consultas.eliminarPorId(id)
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { p ->
                consultas.insertar(
                    p.id,
                    p.nombre,
                    p.precio,
                    p.stock.toLong(),
                    0L,
                    0L,
                    null
                )
            }
        }
    }
}