package pe.edu.upeu.pharmamobil.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

class AlmacenSeguroIos : AlmacenSeguro {
    private var tokenEnMemoria: String? = null

    override fun guardarToken(token: String) {
        tokenEnMemoria = token
    }

    override fun leerToken(): String? {
        return tokenEnMemoria
    }

    override fun borrarToken() {
        tokenEnMemoria = null
    }
}