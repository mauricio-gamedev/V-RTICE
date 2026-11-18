package com.vertice.backend.identity

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

data class UserRecord(
    val id: UUID,
    val email: String,
    val username: String,
    val displayName: String,
    val passwordHash: String,
    val createdAt: Instant,
)

data class SessionRecord(
    val id: UUID,
    val userId: UUID,
    val refreshTokenHash: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val revokedAt: Instant? = null,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val displayName: String,
    val invitationCode: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserProfile,
)

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val createdAt: String,
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
)

class DuplicateAccountException : RuntimeException()
class InvalidRequestException(message: String) : RuntimeException(message)
class InvalidCredentialsException : RuntimeException()
class InvalidSessionException : RuntimeException()
