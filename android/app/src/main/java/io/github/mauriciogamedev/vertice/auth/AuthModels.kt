package io.github.mauriciogamedev.vertice.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val username: String,
    val displayName: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String,
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String,
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: AuthUserDto,
)

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val createdAt: String,
)

@Serializable
data class ApiErrorDto(
    val code: String = "request_failed",
    val message: String = "Não foi possível concluir a solicitação.",
)

data class AuthSession(
    val accessToken: String,
    val user: AuthUserDto,
)

class VerticeApiException(message: String) : Exception(message)
