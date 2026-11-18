package com.vertice.backend.identity

import java.time.Instant
import java.util.UUID

interface IdentityRepository {
    suspend fun createAccount(user: UserRecord, session: SessionRecord)
    suspend fun findUserByLogin(identifier: String): UserRecord?
    suspend fun findUserById(userId: UUID): UserRecord?
    suspend fun createSession(session: SessionRecord)
    suspend fun findActiveSessionByRefreshHash(
        refreshTokenHash: String,
        now: Instant,
    ): SessionRecord?
    suspend fun rotateRefreshToken(
        sessionId: UUID,
        currentHash: String,
        nextHash: String,
        nextExpiry: Instant,
        now: Instant,
    ): Boolean
    suspend fun revokeSession(
        sessionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean
    suspend fun isSessionActive(
        sessionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean
}
