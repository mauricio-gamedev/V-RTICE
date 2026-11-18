package com.vertice.backend.database

import com.vertice.backend.identity.DuplicateAccountException
import com.vertice.backend.identity.IdentityRepository
import com.vertice.backend.identity.SessionRecord
import com.vertice.backend.identity.UserRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

class JdbcIdentityRepository(
    private val dataSource: DataSource,
) : IdentityRepository {
    override suspend fun createAccount(user: UserRecord, session: SessionRecord) =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    connection.prepareStatement(
                        """
                        INSERT INTO users (id, email, username, display_name, password_hash, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setObject(1, user.id)
                        statement.setString(2, user.email)
                        statement.setString(3, user.username)
                        statement.setString(4, user.displayName)
                        statement.setString(5, user.passwordHash)
                        statement.setTimestamp(6, Timestamp.from(user.createdAt))
                        statement.executeUpdate()
                    }
                    connection.insertSession(session)
                    connection.commit()
                } catch (failure: SQLException) {
                    connection.rollback()
                    if (failure.sqlState == "23505") throw DuplicateAccountException()
                    throw failure
                } catch (failure: Throwable) {
                    connection.rollback()
                    throw failure
                }
            }
        }

    override suspend fun findUserByLogin(identifier: String): UserRecord? =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    SELECT id, email, username, display_name, password_hash, created_at
                    FROM users
                    WHERE email = ? OR username = ?
                    LIMIT 1
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, identifier)
                    statement.setString(2, identifier)
                    statement.executeQuery().use { result ->
                        if (result.next()) result.toUserRecord() else null
                    }
                }
            }
        }

    override suspend fun findUserById(userId: UUID): UserRecord? =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    SELECT id, email, username, display_name, password_hash, created_at
                    FROM users WHERE id = ?
                    """.trimIndent(),
                ).use { statement ->
                    statement.setObject(1, userId)
                    statement.executeQuery().use { result ->
                        if (result.next()) result.toUserRecord() else null
                    }
                }
            }
        }

    override suspend fun createSession(session: SessionRecord) =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.insertSession(session)
            }
        }

    override suspend fun findActiveSessionByRefreshHash(
        refreshTokenHash: String,
        now: Instant,
    ): SessionRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, refresh_token_hash, created_at, expires_at, revoked_at
                FROM auth_sessions
                WHERE refresh_token_hash = ? AND revoked_at IS NULL AND expires_at > ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, refreshTokenHash)
                statement.setTimestamp(2, Timestamp.from(now))
                statement.executeQuery().use { result ->
                    if (result.next()) result.toSessionRecord() else null
                }
            }
        }
    }

    override suspend fun rotateRefreshToken(
        sessionId: UUID,
        currentHash: String,
        nextHash: String,
        nextExpiry: Instant,
        now: Instant,
    ): Boolean = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE auth_sessions
                SET refresh_token_hash = ?, expires_at = ?, last_used_at = ?
                WHERE id = ? AND refresh_token_hash = ?
                  AND revoked_at IS NULL AND expires_at > ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, nextHash)
                statement.setTimestamp(2, Timestamp.from(nextExpiry))
                statement.setTimestamp(3, Timestamp.from(now))
                statement.setObject(4, sessionId)
                statement.setString(5, currentHash)
                statement.setTimestamp(6, Timestamp.from(now))
                statement.executeUpdate() == 1
            }
        }
    }

    override suspend fun revokeSession(
        sessionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE auth_sessions SET revoked_at = ?
                WHERE id = ? AND user_id = ? AND revoked_at IS NULL
                """.trimIndent(),
            ).use { statement ->
                statement.setTimestamp(1, Timestamp.from(now))
                statement.setObject(2, sessionId)
                statement.setObject(3, userId)
                statement.executeUpdate() == 1
            }
        }
    }

    override suspend fun isSessionActive(
        sessionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT 1 FROM auth_sessions
                WHERE id = ? AND user_id = ? AND revoked_at IS NULL AND expires_at > ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setObject(1, sessionId)
                statement.setObject(2, userId)
                statement.setTimestamp(3, Timestamp.from(now))
                statement.executeQuery().use { it.next() }
            }
        }
    }

    private fun Connection.insertSession(session: SessionRecord) {
        prepareStatement(
            """
            INSERT INTO auth_sessions
                (id, user_id, refresh_token_hash, created_at, expires_at, revoked_at, last_used_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setObject(1, session.id)
            statement.setObject(2, session.userId)
            statement.setString(3, session.refreshTokenHash)
            statement.setTimestamp(4, Timestamp.from(session.createdAt))
            statement.setTimestamp(5, Timestamp.from(session.expiresAt))
            statement.setTimestamp(6, session.revokedAt?.let(Timestamp::from))
            statement.setTimestamp(7, Timestamp.from(session.createdAt))
            statement.executeUpdate()
        }
    }

    private fun java.sql.ResultSet.toUserRecord() = UserRecord(
        id = getObject("id", UUID::class.java),
        email = getString("email"),
        username = getString("username"),
        displayName = getString("display_name"),
        passwordHash = getString("password_hash"),
        createdAt = getTimestamp("created_at").toInstant(),
    )

    private fun java.sql.ResultSet.toSessionRecord() = SessionRecord(
        id = getObject("id", UUID::class.java),
        userId = getObject("user_id", UUID::class.java),
        refreshTokenHash = getString("refresh_token_hash"),
        createdAt = getTimestamp("created_at").toInstant(),
        expiresAt = getTimestamp("expires_at").toInstant(),
        revokedAt = getTimestamp("revoked_at")?.toInstant(),
    )
}
