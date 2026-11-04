package pe.edu.upeu.pharmamobil.presentation.productos

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.platform.formatearSoles

data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String, // Formateado en soles
    val stock: Int
)

fun Producto.toUi(): ProductoUi = ProductoUi(
    id = id,
    nombre = nombre,
    precio = formatearSoles(precio),
    stock = stock
)