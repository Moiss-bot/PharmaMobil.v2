package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val status: Int? = null,
    val message: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)