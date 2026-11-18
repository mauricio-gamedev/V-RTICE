package com.vertice.backend.identity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.util.UUID

class IdentityService(
    private val repository: IdentityRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val dummyPasswordHash: String by lazy {
        val dummyPassword = "vertice-unknown-account-timing-pad".toCharArray()
        try {
            passwordHasher.hash(dummyPassword)
        } finally {
            dummyPassword.fill('\u0000')
        }
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        val email = normalizeEmail(request.email)
        val username = normalizeUsername(request.username)
        val displayName = request.displayName.trim()
        validateEmail(email)
        validateUsername(username)
        validateDisplayName(displayName)
        validatePassword(request.password)

        val now = clock.instant()
        val userId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val password = request.password.toCharArray()
        val passwordHash = try {
            withContext(Dispatchers.Default) { passwordHasher.hash(password) }
        } finally {
            password.fill('\u0000')
        }

        val refreshToken = tokenService.createRefreshToken()
        val user = UserRecord(
            id = userId,
            email = email,
            username = username,
            displayName = displayName,
            passwordHash = passwordHash,
            createdAt = now,
        )
        val session = SessionRecord(
            id = sessionId,
            userId = userId,
            refreshTokenHash = tokenService.hashRefreshToken(refreshToken),
            createdAt = now,
            expiresAt = now.plusSeconds(tokenService.refreshTokenLifetimeSeconds),
        )

        repository.createAccount(user, session)
        return createAuthResponse(user, sessionId, refreshToken, now)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val identifier = request.identifier.trim().lowercase()
        if (identifier.isBlank() || identifier.length > 254 ||
            request.password.isEmpty() || request.password.length > 128
        ) {
            throw InvalidCredentialsException()
        }

        val user = repository.findUserByLogin(identifier)
        val hashToVerify = user?.passwordHash ?: withContext(Dispatchers.Default) {
            dummyPasswordHash
        }
        val password = request.password.toCharArray()
        val valid = try {
            withContext(Dispatchers.Default) {
                passwordHasher.verify(hashToVerify, password)
            }
        } finally {
            password.fill('\u0000')
        }
        if (user == null || !valid) throw InvalidCredentialsException()

        val now = clock.instant()
        val sessionId = UUID.randomUUID()
        val refreshToken = tokenService.createRefreshToken()
        repository.createSession(
            SessionRecord(
                id = sessionId,
                userId = user.id,
                refreshTokenHash = tokenService.hashRefreshToken(refreshToken),
                createdAt = now,
                expiresAt = now.plusSeconds(tokenService.refreshTokenLifetimeSeconds),
            ),
        )
        return createAuthResponse(user, sessionId, refreshToken, now)
    }

    suspend fun refresh(rawRefreshToken: String): AuthResponse {
        if (rawRefreshToken.length !in 32..128) throw InvalidSessionException()
        val now = clock.instant()
        val currentHash = tokenService.hashRefreshToken(rawRefreshToken)
        val session = repository.findActiveSessionByRefreshHash(currentHash, now)
            ?: throw InvalidSessionException()
        val user = repository.findUserById(session.userId)
            ?: throw InvalidSessionException()

        val nextRefreshToken = tokenService.createRefreshToken()
        val nextExpiry = now.plusSeconds(tokenService.refreshTokenLifetimeSeconds)
        val rotated = repository.rotateRefreshToken(
            sessionId = session.id,
            currentHash = currentHash,
            nextHash = tokenService.hashRefreshToken(nextRefreshToken),
            nextExpiry = nextExpiry,
            now = now,
        )
        if (!rotated) throw InvalidSessionException()

        return createAuthResponse(user, session.id, nextRefreshToken, now)
    }

    suspend fun profile(userId: UUID): UserProfile {
        val user = repository.findUserById(userId) ?: throw InvalidSessionException()
        return user.toProfile()
    }

    suspend fun logout(userId: UUID, sessionId: UUID) {
        repository.revokeSession(sessionId, userId, clock.instant())
    }

    suspend fun isSessionActive(userId: UUID, sessionId: UUID): Boolean =
        repository.isSessionActive(sessionId, userId, clock.instant())

    private fun createAuthResponse(
        user: UserRecord,
        sessionId: UUID,
        refreshToken: String,
        now: Instant,
    ): AuthResponse = AuthResponse(
        accessToken = tokenService.createAccessToken(user.id, sessionId, now),
        refreshToken = refreshToken,
        expiresIn = tokenService.accessTokenLifetimeSeconds,
        user = user.toProfile(),
    )

    private fun UserRecord.toProfile() = UserProfile(
        id = id.toString(),
        email = email,
        username = username,
        displayName = displayName,
        createdAt = createdAt.toString(),
    )

    private fun normalizeEmail(value: String): String = value.trim().lowercase()
    private fun normalizeUsername(value: String): String = value.trim().lowercase()

    private fun validateEmail(value: String) {
        if (value.length !in 5..254 || !EMAIL_PATTERN.matches(value)) {
            throw InvalidRequestException("Informe um e-mail válido.")
        }
    }

    private fun validateUsername(value: String) {
        if (!USERNAME_PATTERN.matches(value)) {
            throw InvalidRequestException("O nome de usuário deve ter 3 a 24 caracteres: letras, números ou _.")
        }
    }

    private fun validateDisplayName(value: String) {
        if (value.length !in 2..40) {
            throw InvalidRequestException("O nome de exibição deve ter de 2 a 40 caracteres.")
        }
    }

    private fun validatePassword(value: String) {
        if (value.length !in 10..128) {
            throw InvalidRequestException("A senha deve ter de 10 a 128 caracteres.")
        }
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        val USERNAME_PATTERN = Regex("^[a-z0-9_]{3,24}$")
    }
}
