package com.vertice.backend.config

data class AppConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val registrationInviteCode: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val host: String,
    val port: Int,
    val databasePoolSize: Int,
) {
    companion object {
        fun fromEnvironment(environment: Map<String, String> = System.getenv()): AppConfig {
            fun required(name: String): String =
                environment[name]?.takeIf { it.isNotBlank() }
                    ?: error("Missing required environment variable: $name")

            val registrationInviteCode = required("REGISTRATION_INVITE_CODE")
            require(registrationInviteCode.length in 12..128) {
                "REGISTRATION_INVITE_CODE must contain between 12 and 128 characters."
            }

            val jwtSecret = required("JWT_SECRET")
            require(jwtSecret.toByteArray(Charsets.UTF_8).size >= 32) {
                "JWT_SECRET must contain at least 32 UTF-8 bytes."
            }

            val port = environment["PORT"]?.toIntOrNull() ?: 8080
            require(port in 1..65_535) { "PORT must be between 1 and 65535." }

            val poolSize = environment["DATABASE_POOL_SIZE"]?.toIntOrNull() ?: 8
            require(poolSize in 2..32) { "DATABASE_POOL_SIZE must be between 2 and 32." }

            return AppConfig(
                databaseUrl = required("DATABASE_URL"),
                databaseUser = required("DATABASE_USER"),
                databasePassword = required("DATABASE_PASSWORD"),
                registrationInviteCode = registrationInviteCode,
                jwtSecret = jwtSecret,
                jwtIssuer = environment["JWT_ISSUER"]?.ifBlank { null } ?: "vertice-api",
                jwtAudience = environment["JWT_AUDIENCE"]?.ifBlank { null } ?: "vertice-android",
                host = environment["HOST"]?.ifBlank { null } ?: "0.0.0.0",
                port = port,
                databasePoolSize = poolSize,
            )
        }
    }
}
