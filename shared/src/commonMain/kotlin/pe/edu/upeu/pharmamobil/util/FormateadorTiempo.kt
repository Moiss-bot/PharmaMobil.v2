package pe.edu.upeu.pharmamobil.util

import kotlin.time.Clock

object FormateadorTiempo {

    fun formatearAntiguedad(timestampMillis: Long?): String {
        if (timestampMillis == null || timestampMillis <= 0L) {
            return "Nunca sincronizado"
        }

        // Obtener tiempo actual usando el Clock nativo de Kotlin
        val ahora = Clock.System.now().toEpochMilliseconds()
        val diferenciaSegundos = (ahora - timestampMillis) / 1000

        if (diferenciaSegundos < 60) {
            return "Actualizado hace un momento"
        }
        if (diferenciaSegundos < 3600) {
            val minutos = diferenciaSegundos / 60
            return "Actualizado hace $minutos min"
        }
        if (diferenciaSegundos < 86400) {
            val horas = diferenciaSegundos / 3600
            return "Actualizado hace $horas h"
        }

        val dias = diferenciaSegundos / 86400
        return "Actualizado hace $dias d"
    }
}