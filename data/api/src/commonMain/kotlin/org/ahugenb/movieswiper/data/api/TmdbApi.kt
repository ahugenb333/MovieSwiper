package org.ahugenb.movieswiper.data.api

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.ahugenb.movieswiper.core.models.Movie
import org.ahugenb.movieswiper.core.models.Genre

class TmdbApi(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3"
    }

    suspend fun discoverMovies(page: Int = 1, withGenres: String? = null): List<Movie> {
        return discoverMovies(
            page = page,
            withGenres = withGenres?.split(",")?.mapNotNull { it.trim().toIntOrNull() },
        )
    }

    suspend fun discoverMovies(
        page: Int = 1,
        withGenres: List<Int>? = null,
        withoutGenres: List<Int>? = null,
        primaryReleaseDateGte: String? = null,
        primaryReleaseDateLte: String? = null,
        sortBy: String = "popularity.desc",
        voteAverageGte: Double? = null,
        voteCountGte: Int? = null,
    ): List<Movie> {
        val response: MovieResponse = httpClient.get("$BASE_URL/discover/movie") {
            parameter("page", page)
            parameter("sort_by", sortBy)
            withGenres?.takeIf { it.isNotEmpty() }?.let {
                parameter("with_genres", it.joinToString(","))
            }
            withoutGenres?.takeIf { it.isNotEmpty() }?.let {
                parameter("without_genres", it.joinToString(","))
            }
            primaryReleaseDateGte?.let { parameter("primary_release_date.gte", it) }
            primaryReleaseDateLte?.let { parameter("primary_release_date.lte", it) }
            voteAverageGte?.let { parameter("vote_average.gte", it) }
            voteCountGte?.let { parameter("vote_count.gte", it) }
        }.body()
        return response.results
    }

    suspend fun getRecommendations(movieId: Int, page: Int = 1): List<Movie> {
        val response: MovieResponse = httpClient.get("$BASE_URL/movie/$movieId/recommendations") {
            parameter("page", page)
        }.body()
        return response.results
    }

    suspend fun getGenres(): List<Genre> {
        val response: GenreResponse = httpClient.get("$BASE_URL/genre/movie/list") {
        }.body()
        return response.genres
    }
}

@kotlinx.serialization.Serializable
private data class MovieResponse(
    val results: List<Movie>,
    val page: Int,
    @SerialName("total_pages")
    val totalPages: Int
)

@kotlinx.serialization.Serializable
private data class GenreResponse(
    val genres: List<Genre>
)

/**
 * Creates an HttpClient configured for either TMDB (with JWT) or our Backend (no JWT).
 */
class BackendHttpException(val statusCode: Int, bodyPreview: String) :
    Exception("Backend returned HTTP $statusCode: $bodyPreview")

fun createHttpClient(withTmdbAuth: Boolean = false) = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Napier.d(tag = "HttpClient", message = message)
            }
        }
        level = LogLevel.INFO
    }
    install(HttpCallValidator) {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val body = response.bodyAsText().take(300)
                throw BackendHttpException(response.status.value, body)
            }
        }
    }
    if (withTmdbAuth) {
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer ${BuildConfig.TMDB_API_KEY}")
        }
    }
}