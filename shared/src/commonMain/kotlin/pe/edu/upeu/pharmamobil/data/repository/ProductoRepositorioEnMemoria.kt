package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {

    private val productos = MutableStateFlow<List<Producto>>(emptyList())

    override fun observar(): Flow<List<Producto>> = productos.asStateFlow()

    override suspend fun sincronizar() {
        // No requiere sincronización síncrona en memoria
    }

    override suspend fun registrar(producto: Producto): Producto {
        val nuevoId = if (producto.id <= 0) (productos.value.maxOfOrNull { it.id } ?: 0L) + 1L else producto.id
        val productoConId = producto.copy(id = nuevoId)
        productos.value = productos.value + productoConId
        return productoConId
    }

    override suspend fun actualizar(producto: Producto): Producto {
        productos.value = productos.value.map { if (it.id == producto.id) producto else it }
        return producto
    }

    override suspend fun eliminar(id: Long) {
        productos.value = productos.value.filterNot { it.id == id }
    }
}