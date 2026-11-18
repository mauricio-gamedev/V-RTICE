package com.vertice.backend

import com.vertice.backend.identity.AuthResponse
import com.vertice.backend.identity.DuplicateAccountException
import com.vertice.backend.identity.IdentityRepository
import com.vertice.backend.identity.PasswordHasher
import com.vertice.backend.identity.SessionRecord
import com.vertice.backend.identity.TokenService
import com.vertice.backend.identity.UserRecord
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun registrationCreatesProfileAndSessionRefreshRotatesToken() = testApplication {
        val dependencies = testDependencies()
        application { module(dependencies) }

        val registration = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"email":"Friend@Example.com","username":"Friend_01","displayName":"Friend One","password":"strong-password-01"}""",
            )
        }

        assertEquals(HttpStatusCode.Created, registration.status)
        val auth = Json.decodeFromString<AuthResponse>(registration.bodyAsText())
        assertEquals("friend@example.com", auth.user.email)
        assertEquals("friend_01", auth.user.username)
        assertEquals("Friend One", auth.user.displayName)
        assertEquals("Bearer", auth.tokenType)
        assertTrue(auth.refreshToken.length >= 40)

        val profile = client.get("/v1/me") {
            header(HttpHeaders.Authorization, "Bearer ${auth.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, profile.status)
        assertTrue(profile.bodyAsText().contains("friend@example.com"))

        val refresh = client.post("/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"${auth.refreshToken}"}""")
        }
        assertEquals(HttpStatusCode.OK, refresh.status)
        val rotated = Json.decodeFromString<AuthResponse>(refresh.bodyAsText())
        assertNotEquals(auth.refreshToken, rotated.refreshToken)

        val replay = client.post("/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"${auth.refreshToken}"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, replay.status)

        val logout = client.post("/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer ${rotated.accessToken}")
        }
        assertEquals(HttpStatusCode.NoContent, logout.status)

        val revokedProfile = client.get("/v1/me") {
            header(HttpHeaders.Authorization, "Bearer ${rotated.accessToken}")
        }
        assertEquals(HttpStatusCode.Unauthorized, revokedProfile.status)
    }

    @Test
    fun loginAcceptsUsernameAndRejectsWrongPassword() = testApplication {
        application { module(testDependencies()) }

        client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"email":"login@example.com","username":"login_user","displayName":"Login User","password":"correct-password"}""",
            )
        }

        val login = client.post("/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"identifier":"LOGIN_USER","password":"correct-password"}""")
        }
        assertEquals(HttpStatusCode.OK, login.status)

        val rejected = client.post("/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"identifier":"login_user","password":"wrong-password"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, rejected.status)
    }

    @Test
    fun registrationRejectsInvalidInputAndDuplicateIdentity() = testApplication {
        application { module(testDependencies()) }

        val invalid = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"email":"not-an-email","username":"x","displayName":"A","password":"short"}""",
            )
        }
        assertEquals(HttpStatusCode.BadRequest, invalid.status)

        val request = """{"email":"same@example.com","username":"same_user","displayName":"Same User","password":"strong-password"}"""
        val first = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        assertEquals(HttpStatusCode.Created, first.status)

        val duplicate = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        assertEquals(HttpStatusCode.Conflict, duplicate.status)
    }
}

private fun testDependencies(): AppDependencies = AppDependencies(
    identityRepository = FakeIdentityRepository(),
    passwordHasher = TestPasswordHasher(),
    tokenService = TokenService(
        secret = "test-only-jwt-secret-should-never-be-used-in-production",
        issuer = "vertice-test",
        audience = "vertice-test-client",
    ),
)

private class TestPasswordHasher : PasswordHasher {
    override fun hash(password: CharArray): String = "test-only:" + String(password)

    override fun verify(encodedHash: String, password: CharArray): Boolean =
        encodedHash == "test-only:" + String(password)
}

private class FakeIdentityRepository : IdentityRepository {
    private val users = mutableMapOf<UUID, UserRecord>()
    private val sessions = mutableMapOf<UUID, SessionRecord>()

    override suspend fun createAccount(user: UserRecord, session: SessionRecord) {
        if (users.values.any { it.email == user.email || it.username == user.username }) {
            throw DuplicateAccountException()
        }
        users[user.id] = user
        sessions[session.id] = session
    }

    override suspend fun findUserByLogin(identifier: String): UserRecord? =
        users.values.firstOrNull { it.email == identifier || it.username == identifier }

    override suspend fun findUserById(userId: UUID): UserRecord? = users[userId]

    override suspend fun createSession(session: SessionRecord) {
        sessions[session.id] = session
    }

    override suspend fun findActiveSessionByRefreshHash(
        refreshTokenHash: String,
        now: Instant,
    ): SessionRecord? = sessions.values.firstOrNull {
        it.refreshTokenHash == refreshTokenHash && it.revokedAt == null && it.expiresAt.isAfter(now)
    }

    override suspend fun rotateRefreshToken(
        sessionId: UUID,
        currentHash: String,
        nextHash: String,
        nextExpiry: Instant,
        now: Instant,
    ): Boolean {
        val current = sessions[sessionId] ?: return false
        if (current.refreshTokenHash != currentHash ||
            current.revokedAt != null ||
            !current.expiresAt.isAfter(now)
        ) return false

        sessions[sessionId] = current.copy(
            refreshTokenHash = nextHash,
            expiresAt = nextExpiry,
        )
        return true
    }

    override suspend fun revokeSession(
        sessionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean {
        val current = sessions[sessionId] ?: return false
        if (current.userId != userId || current.revokedAt != null) return false
        sessions[sessionId] = current.copy(revokedAt = now)
        return true
    }

    override suspend fun isSessionActive(
        sessionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean = sessions[sessionId]?.let {
        it.userId == userId && it.revokedAt == null && it.expiresAt.isAfter(now)
    } ?: false
}
