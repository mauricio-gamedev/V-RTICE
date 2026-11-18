package com.vertice.backend.identity

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.HexFormat
import java.util.UUID
import java.security.MessageDigest

class TokenService(
    secret: String,
    private val issuer: String,
    private val audience: String,
    val accessTokenLifetimeSeconds: Long = 900,
    val refreshTokenLifetimeSeconds: Long = 2_592_000,
) {
    private val algorithm = Algorithm.HMAC256(secret)
    private val secureRandom = SecureRandom()

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createAccessToken(userId: UUID, sessionId: UUID, now: Instant): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("sid", sessionId.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(accessTokenLifetimeSeconds)))
            .sign(algorithm)

    fun createRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashRefreshToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
        return HexFormat.of().formatHex(digest)
    }
}
