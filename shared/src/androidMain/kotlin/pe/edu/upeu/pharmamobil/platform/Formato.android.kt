package pe.edu.upeu.pharmamobil.platform

import java.text.NumberFormat
import java.util.Locale

actual fun formatearSoles(valor: Double): String {
    val localePeru = Locale.Builder().setLanguage("es").setRegion("PE").build()
    return NumberFormat.getCurrencyInstance(localePeru).format(valor)
}