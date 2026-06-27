package org.ahugenb.movieswiper.server

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import org.slf4j.event.Level
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.ahugenb.movieswiper.core.models.*
import org.ahugenb.movieswiper.data.api.TmdbApi
import org.ahugenb.movieswiper.data.api.createHttpClient

private const val DEFAULT_PORT = 8081

fun main() {
    Napier.base(DebugAntilog())
    val port = System.getenv("MOVIESWIPER_PORT")?.toIntOrNull() ?: DEFAULT_PORT
    try {
        println("Starting MovieSwiper Backend on http://0.0.0.0:$port (Android emulator: http://10.0.2.2:$port)")
        embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
            .start(wait = true)
    } catch (e: Exception) {
        if (e.cause is java.net.BindException || e is java.net.BindException) {
            System.err.println("Port $port is already in use. Stop the other server first:")
            System.err.println("  kill \$(lsof -ti :$port)")
            System.err.println("Or reuse the running instance — the app only needs one server on 8081.")
        } else {
            System.err.println("Failed to start server: ${e.message}")
            e.printStackTrace()
        }
    }
}

fun Application.module() {
    val tasteService = TasteService()
    val tmdbApi = TmdbApi(createHttpClient(withTmdbAuth = true))
    val recommendationService = RecommendationService(tmdbApi, tasteService)
    val finderService = FinderService(recommendationService, tasteService)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val path = call.request.uri
            "$method $path -> $status"
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            Napier.e(tag = "Server", message = "Unhandled exception: ${cause.message}", throwable = cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Internal Server Error")
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            Napier.w(tag = "Server", message = "404 ${call.request.httpMethod.value} ${call.request.uri}")
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))
        }
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    routing {
        get("/") {
            call.respondText("MovieSwiper Backend is running!")
        }

        post("/profile/sync") {
            val request = call.receive<InteractionRequest>()
            val updatedProfile = tasteService.syncInteractions(request.userId, request.interactions)
            call.respond(updatedProfile)
        }

        get("/matcher/discover") {
            val userId = call.request.queryParameters["userId"] ?: "anonymous"
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1

            Napier.d(tag = "Server", message = "Discover request: userId=$userId, page=$page")

            try {
                val movies = recommendationService.discoverForMatcher(userId, page)
                call.respond(movies)
            } catch (e: Exception) {
                Napier.e(tag = "Server", message = "Discover failed: ${e.message}")
                throw e
            }
        }

        post("/finder/next-question") {
            val request = call.receive<FinderQuestionRequest>()
            val response = finderService.getNextQuestion(request)
            call.respond(response)
        }
    }
}