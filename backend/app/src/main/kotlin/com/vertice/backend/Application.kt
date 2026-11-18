package com.vertice.backend

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val port = System.getenv("PORT")
        ?.toIntOrNull()
        ?.takeIf { it in 1..65_535 }
        ?: 8080

    val host = System.getenv("HOST")?.takeIf { it.isNotBlank() } ?: "0.0.0.0"

    embeddedServer(
        factory = Netty,
        host = host,
        port = port,
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    routing {
        get("/health/live") {
            call.respondText(
                text = """{"status":"ok","service":"vertice-api"}""",
                contentType = ContentType.Application.Json,
            )
        }
    }
}
