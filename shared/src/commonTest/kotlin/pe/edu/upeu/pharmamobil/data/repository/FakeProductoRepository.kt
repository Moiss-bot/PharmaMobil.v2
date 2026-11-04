package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class FakeProductoRepository(
    var productos: List<Producto> = emptyList(),
    var debeLanzarErrorValidacion: Boolean = false,
    var debeLanzarErrorConexion: Boolean = false
) : ProductoRepository {

    override suspend fun listar(): List<Producto> {
        if (debeLanzarErrorConexion) {
            throw ErrorApiException(ErrorApi.SinConexion)
        }
        return productos
    }

    override suspend fun obtener(id: Long): Producto {
        return productos.find { it.id == id }
            ?: throw ErrorApiException(ErrorApi.NoEncontrado)
    }

    override suspend fun registrar(producto: Producto): Producto {
        if (debeLanzarErrorValidacion) {
            throw ErrorApiException(
                ErrorApi.Validacion(mapOf("nombre" to "El nombre debe tener al menos 3 caracteres"))
            )
        }
        val nuevo = producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L)
        productos = productos + nuevo
        return nuevo
    }

    override suspend fun actualizar(producto: Producto): Producto {
        val indice = productos.indexOfFirst { it.id == producto.id }
        if (indice == -1) throw ErrorApiException(ErrorApi.NoEncontrado)
        val listaMutada = productos.toMutableList()
        listaMutada[indice] = producto
        productos = listaMutada
        return producto
    }

    override suspend fun eliminar(id: Long) {
        val existe = productos.any { it.id == id }
        if (!existe) throw ErrorApiException(ErrorApi.NoEncontrado)
        productos = productos.filterNot { it.id == id }
    }
}