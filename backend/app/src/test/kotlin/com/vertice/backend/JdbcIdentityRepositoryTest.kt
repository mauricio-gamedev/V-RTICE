package com.vertice.backend

import com.vertice.backend.database.DatabaseFactory
import com.vertice.backend.database.JdbcIdentityRepository
import com.vertice.backend.identity.SessionRecord
import com.vertice.backend.identity.UserRecord
import kotlinx.coroutines.runBlocking
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdbcIdentityRepositoryTest {
    @Test
    fun postgresPersistsAccountAndEnforcesRefreshRotationAndRevocation() = runBlocking {
        val jdbcUrl = System.getenv("TEST_DATABASE_URL") ?: return
        val username = System.getenv("TEST_DATABASE_USER") ?: "vertice"
        val password = System.getenv("TEST_DATABASE_PASSWORD") ?: "vertice_ci_password"
        val dataSource = DatabaseFactory.connect(
            jdbcUrl = jdbcUrl,
            username = username,
            password = password,
            maximumPoolSize = 3,
        )
        val repository = JdbcIdentityRepository(dataSource)
        val now = Instant.now()
        val userId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val unique = UUID.randomUUID().toString().replace("-", "").take(12)
        val email = "ci-$unique@example.test"
        val handle = "ci_$unique"

        try {
            repository.createAccount(
                UserRecord(
                    id = userId,
                    email = email,
                    username = handle,
                    displayName = "CI Identity",
                    passwordHash = "test-only-hash",
                    createdAt = now,
                ),
                SessionRecord(
                    id = sessionId,
                    userId = userId,
                    refreshTokenHash = "a".repeat(64),
                    createdAt = now,
                    expiresAt = now.plusSeconds(3_600),
                ),
            )

            val saved = repository.findUserByLogin(handle)
            assertNotNull(saved)
            assertEquals(userId, saved.id)
            assertEquals(email, saved.email)

            val active = repository.findActiveSessionByRefreshHash("a".repeat(64), now)
            assertNotNull(active)
            assertEquals(sessionId, active.id)

            val nextHash = "b".repeat(64)
            assertTrue(
                repository.rotateRefreshToken(
                    sessionId = sessionId,
                    currentHash = "a".repeat(64),
                    nextHash = nextHash,
                    nextExpiry = now.plusSeconds(7_200),
                    now = now,
                ),
            )
            assertEquals(null, repository.findActiveSessionByRefreshHash("a".repeat(64), now))
            assertNotNull(repository.findActiveSessionByRefreshHash(nextHash, now))

            assertTrue(repository.revokeSession(sessionId, userId, now))
            assertFalse(repository.isSessionActive(sessionId, userId, now))
        } finally {
            dataSource.connection.use { connection ->
                connection.prepareStatement("DELETE FROM auth_sessions WHERE user_id = ?").use {
                    it.setObject(1, userId)
                    it.executeUpdate()
                }
                connection.prepareStatement("DELETE FROM users WHERE id = ?").use {
                    it.setObject(1, userId)
                    it.executeUpdate()
                }
            }
            dataSource.close()
        }
    }
}
