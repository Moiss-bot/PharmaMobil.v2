package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequest
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long = 1L
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = flow {
        val listaDto = api.listar().contenido
        emit(listaDto.map { it.toDomain() })
    }

    override suspend fun sincronizar() {
        // En repositorios puramente REST directos no aplica sincronización de SQLite
    }

    override suspend fun registrar(producto: Producto): Producto {
        val dto = api.crear(producto.toRequest(categoriaPorDefecto))
        return dto.toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto {
        val dto = api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto))
        return dto.toDomain()
    }

    override suspend fun eliminar(id: Long) {
        api.eliminar(id)
    }
}