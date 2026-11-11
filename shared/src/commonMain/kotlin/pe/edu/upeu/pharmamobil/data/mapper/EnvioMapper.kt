package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio

fun Throwable.aResultadoEnvio(): ResultadoEnvio {
    val mensaje = this.message ?: "Error desconocido"
    return when {
        mensaje.contains("400") || mensaje.contains("409") || mensaje.contains("404") || mensaje.contains("403") -> {
            // Error permanente: Validaciones, duplicados o permisos no deben reintentarse
            ResultadoEnvio.Permanente(mensaje)
        }
        mensaje.contains("401") -> {
            ResultadoEnvio.Permanente("Sesión vencida: Vuelva a iniciar sesión")
        }
        else -> {
            // Error transitorio: Sin red, timeout o 500 del servidor
            ResultadoEnvio.Transitorio(mensaje)
        }
    }
}