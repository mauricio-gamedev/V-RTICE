package com.vertice.backend

import com.vertice.backend.config.AppConfig
import com.vertice.backend.database.DatabaseFactory
import com.vertice.backend.database.JdbcIdentityRepository
import com.vertice.backend.identity.ApiError
import com.vertice.backend.identity.Argon2idPasswordHasher
import com.vertice.backend.identity.DuplicateAccountException
import com.vertice.backend.identity.IdentityRepository
import com.vertice.backend.identity.IdentityService
import com.vertice.backend.identity.InvalidCredentialsException
import com.vertice.backend.identity.InvalidInvitationCodeException
import com.vertice.backend.identity.InvalidRequestException
import com.vertice.backend.identity.InvalidSessionException
import com.vertice.backend.identity.LoginRequest
import com.vertice.backend.identity.PasswordHasher
import com.vertice.backend.identity.RefreshRequest
import com.vertice.backend.identity.RegisterRequest
import com.vertice.backend.identity.TokenService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.UUID

data class AppDependencies(
    val identityRepository: IdentityRepository,
    val passwordHasher: PasswordHasher,
    val tokenService: TokenService,
    val registrationInviteCode: String,
)

fun main() {
    val config = AppConfig.fromEnvironment()
    val dataSource = DatabaseFactory.connect(
        jdbcUrl = config.databaseUrl,
        username = config.databaseUser,
        password = config.databasePassword,
        maximumPoolSize = config.databasePoolSize,
    )
    val repository = JdbcIdentityRepository(dataSource)
    val tokenService = TokenService(
        secret = config.jwtSecret,
        issuer = config.jwtIssuer,
        audience = config.jwtAudience,
    )

    embeddedServer(
        factory = Netty,
        host = config.host,
        port = config.port,
    ) {
        module(
            AppDependencies(
                identityRepository = repository,
                passwordHasher = Argon2idPasswordHasher(),
                tokenService = tokenService,
                registrationInviteCode = config.registrationInviteCode,
            ),
        )
    }.start(wait = true)
}

fun Application.module(dependencies: AppDependencies) {
    val identityService = IdentityService(
        repository = dependencies.identityRepository,
        passwordHasher = dependencies.passwordHasher,
        tokenService = dependencies.tokenService,
    )

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = false
                explicitNulls = false
                encodeDefaults = true
            },
        )
    }

    install(StatusPages) {
        exception<InvalidRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("invalid_request", cause.message ?: "Confira os dados enviados."),
            )
        }
        exception<DuplicateAccountException> { call, _ ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiError("account_conflict", "E-mail ou nome de usuário já está em uso."),
            )
        }
        exception<InvalidCredentialsException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiError("invalid_credentials", "E-mail/nome de usuário ou senha inválidos."),
            )
        }
        exception<InvalidInvitationCodeException> { call, _ ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiError("invite_required", "O código de convite não é válido para este ambiente."),
            )
        }
        exception<InvalidSessionException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiError("invalid_session", "A sessão é inválida ou expirou. Entre novamente."),
            )
        }
        exception<ContentTransformationException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("invalid_body", "O corpo da requisição está ausente ou malformado."),
            )
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError("internal_error", "Ocorreu um erro interno."),
            )
        }
    }

    install(Authentication) {
        jwt("access-token") {
            verifier(dependencies.tokenService.verifier)
            validate { credential ->
                val userId = credential.payload.subject.toUuidOrNull()
                val sessionId = credential.payload.getClaim("sid").asString().toUuidOrNull()
                if (userId != null && sessionId != null &&
                    identityService.isSessionActive(userId, sessionId)
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiError("authentication_required", "Entre na sua conta para continuar."),
                )
            }
        }
    }

    routing {
        get("/health/live") {
            call.respondText(
                text = """{"status":"ok","service":"vertice-api"}""",
                contentType = ContentType.Application.Json,
            )
        }

        post("/v1/auth/register") {
            val request = call.receive<RegisterRequest>()
            if (!isValidInvitationCode(request.invitationCode, dependencies.registrationInviteCode)) {
                throw InvalidInvitationCodeException()
            }
            call.respond(HttpStatusCode.Created, identityService.register(request))
        }

        post("/v1/auth/login") {
            val request = call.receive<LoginRequest>()
            call.respond(HttpStatusCode.OK, identityService.login(request))
        }

        post("/v1/auth/refresh") {
            val request = call.receive<RefreshRequest>()
            call.respond(HttpStatusCode.OK, identityService.refresh(request.refreshToken))
        }

        authenticate("access-token") {
            get("/v1/me") {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw InvalidSessionException()
                val userId = principal.payload.subject.toUuidOrNull()
                    ?: throw InvalidSessionException()
                call.respond(identityService.profile(userId))
            }

            post("/v1/auth/logout") {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw InvalidSessionException()
                val userId = principal.payload.subject.toUuidOrNull()
                    ?: throw InvalidSessionException()
                val sessionId = principal.payload.getClaim("sid").asString().toUuidOrNull()
                    ?: throw InvalidSessionException()
                identityService.logout(userId, sessionId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun isValidInvitationCode(candidate: String, expected: String): Boolean {
    if (candidate.length !in 12..128) return false
    return MessageDigest.isEqual(
        candidate.toByteArray(UTF_8),
        expected.toByteArray(UTF_8),
    )
}

private fun String?.toUuidOrNull(): UUID? =
    this?.let { runCatching { UUID.fromString(it) }.getOrNull() }
