package io.github.mauriciogamedev.vertice.auth

import android.content.Context
import java.net.URI

class ApiEndpointStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "vertice_api_settings",
        Context.MODE_PRIVATE,
    )

    fun getBaseUrl(): String? =
        preferences.getString(KEY_BASE_URL, null)

    fun saveBaseUrl(value: String): String {
        val normalized = value.trim().trimEnd('/')
        val uri = runCatching { URI(normalized) }.getOrNull()
            ?: throw VerticeApiException("Endereço do servidor inválido.")

        if (uri.scheme != "https" ||
            uri.host.isNullOrBlank() ||
            uri.userInfo != null ||
            uri.query != null ||
            uri.fragment != null
        ) {
            throw VerticeApiException("Use um endereço HTTPS válido, sem usuário, senha ou parâmetros.")
        }

        preferences.edit().putString(KEY_BASE_URL, normalized).apply()
        return normalized
    }

    private companion object {
        const val KEY_BASE_URL = "api_base_url"
    }
}
