package pe.edu.upeu.pharmamobil.data.firebase

import pe.edu.upeu.pharmamobil.domain.port.Telemetria

class TelemetriaFirebase : Telemetria {
    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        // Log seguro sin exponer tokens ni contraseñas del usuario
        println("Firebase Crashlytics Log: ${e.message} | Contexto: $contexto")
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        println("Firebase Analytics Event: $nombre | Datos: $datos")
    }
}