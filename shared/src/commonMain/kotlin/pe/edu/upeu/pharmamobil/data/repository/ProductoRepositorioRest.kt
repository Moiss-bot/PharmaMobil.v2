package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val respaldoLocal: ProductoRepositorioEnMemoria
) : ProductoRepository {

    override suspend fun listar(): List<Producto> =
        api.listar().contenido.map { it.toDomain() }

    override suspend fun registrar(producto: Producto): Producto =
        respaldoLocal.registrar(producto)
}