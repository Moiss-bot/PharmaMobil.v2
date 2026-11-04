package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {

    private val candado = Mutex()
    private val productos = mutableListOf<Producto>()
    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(RETARDO_OPERACION_MS)
        return candado.withLock {
            val guardado = producto.copy(id = siguienteId++)
            productos.add(guardado)
            guardado
        }
    }

    override suspend fun listar(): List<Producto> {
        delay(RETARDO_OPERACION_MS)
        return candado.withLock {
            productos.toList()
        }
    }

    override suspend fun obtener(id: Long): Producto {
        delay(RETARDO_OPERACION_MS)
        return candado.withLock {
            productos.find { it.id == id }
                ?: throw NoSuchElementException("Producto con ID $id no encontrado")
        }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        delay(RETARDO_OPERACION_MS)
        return candado.withLock {
            val indice = productos.indexOfFirst { it.id == producto.id }
            if (indice != -1) {
                productos[indice] = producto
                producto
            } else {
                throw NoSuchElementException("No se pudo actualizar: ID ${producto.id} no existe")
            }
        }
    }

    override suspend fun eliminar(id: Long) {
        delay(RETARDO_OPERACION_MS)
        candado.withLock {
            productos.removeAll { it.id == id }
        }
    }

    private companion object {
        const val RETARDO_OPERACION_MS = 400L
    }
}