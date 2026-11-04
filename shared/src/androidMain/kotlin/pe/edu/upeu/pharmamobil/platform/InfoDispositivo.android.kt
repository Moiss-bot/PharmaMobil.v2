package pe.edu.upeu.pharmamobil.platform

import android.os.Build

actual class InfoDispositivo {
    actual val sistema: String = "Android"
    actual val version: String = Build.VERSION.RELEASE
}