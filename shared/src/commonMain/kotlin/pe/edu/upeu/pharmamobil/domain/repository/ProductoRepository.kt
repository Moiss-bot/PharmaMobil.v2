package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {
    /** La pantalla se suscribe una vez y se actualiza sola desde SQLite */
    fun observar(): Flow<List<Producto>>

    /** Sube los pendientes en modo avión y trae la lista del servidor */
    suspend fun sincronizar()

    /** Operaciones de modificación y registro local */
    suspend fun registrar(producto: Producto): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}