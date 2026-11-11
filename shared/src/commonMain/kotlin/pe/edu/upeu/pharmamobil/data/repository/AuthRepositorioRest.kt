package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val respuesta: LoginResponseDto = cliente.post("auth/login") {
            setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
        }.body()

        almacen.guardarToken(respuesta.token)
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
        return respuesta.aSesion()
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken() ?: return null
        return Sesion(
            token = token,
            usuario = "usuario_guardado",
            nombreCompleto = "Usuario PharmaMobil",
            permisos = setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR", "CLIENTES_LEER")
        )
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}