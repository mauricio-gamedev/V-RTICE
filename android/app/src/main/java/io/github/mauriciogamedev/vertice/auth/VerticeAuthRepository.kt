package io.github.mauriciogamedev.vertice.auth

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class VerticeAuthRepository(context: Context) {
    private val endpointStore = ApiEndpointStore(context)
    private val tokenStore = SecureTokenStore(context)

    private val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
    }

    fun currentServerUrl(): String? = endpointStore.getBaseUrl()

    fun configureServer(url: String) {
        endpointStore.saveBaseUrl(url)
    }

    suspend fun checkServerConnection() {
        val response = client.get(endpoint("/health/live"))
        if (!response.status.isSuccess()) {
            throw VerticeApiException("O servidor respondeu com HTTP ${response.status.value}.")
        }
    }

    suspend fun register(
        email: String,
        username: String,
        displayName: String,
        password: String,
    ): AuthSession {
        val response = client.post(endpoint("/v1/auth/register")) {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequestDto(
                    email = email,
                    username = username,
                    displayName = displayName,
                    password = password,
                ),
            )
        }
        return response.toAuthSession()
    }

    suspend fun login(identifier: String, password: String): AuthSession {
        val response = client.post(endpoint("/v1/auth/login")) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(identifier = identifier, password = password))
        }
        return response.toAuthSession()
    }

    suspend fun restoreSession(): AuthSession? {
        val storedToken = tokenStore.readRefreshToken() ?: return null
        return runCatching { refresh(storedToken) }
            .getOrElse {
                tokenStore.clear()
                null
            }
    }

    suspend fun logout(session: AuthSession) {
        val accessToken = tokenStore.readRefreshToken()?.let { storedRefresh ->
            try {
                refresh(storedRefresh).accessToken
            } catch (failure: Exception) {
                tokenStore.clear()
                throw failure
            }
        } ?: session.accessToken

        val response = client.post(endpoint("/v1/auth/logout")) {
            bearerAuth(accessToken)
        }
        if (!response.status.isSuccess()) {
            throw response.toApiException()
        }
        tokenStore.clear()
    }

    private suspend fun refresh(refreshToken: String): AuthSession {
        val response = client.post(endpoint("/v1/auth/refresh")) {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequestDto(refreshToken = refreshToken))
        }
        return response.toAuthSession()
    }

    private suspend fun HttpResponse.toAuthSession(): AuthSession {
        if (!status.isSuccess()) throw toApiException()
        val payload = body<AuthResponseDto>()
        tokenStore.saveRefreshToken(payload.refreshToken)
        return AuthSession(
            accessToken = payload.accessToken,
            user = payload.user,
        )
    }

    private suspend fun HttpResponse.toApiException(): VerticeApiException {
        val apiError = runCatching { body<ApiErrorDto>() }.getOrNull()
        return VerticeApiException(
            apiError?.message ?: "Falha na comunicação com o servidor (HTTP ${status.value}).",
        )
    }

    private fun endpoint(path: String): String {
        val baseUrl = endpointStore.getBaseUrl()
            ?: throw VerticeApiException("Configure primeiro o endereço HTTPS do servidor VÉRTICE.")
        return baseUrl.trimEnd('/') + path
    }
}
