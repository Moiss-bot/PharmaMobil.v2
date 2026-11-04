package pe.edu.upeu.pharmamobil.platform

import platform.UIKit.UIDevice

actual class InfoDispositivo {
    actual val sistema: String = UIDevice.currentDevice.systemName
    actual val version: String = UIDevice.currentDevice.systemVersion
}