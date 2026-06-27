package org.ahugenb.movieswiper.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.ahugenb.movieswiper.core.models.*

class MovieBackendApi(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun discoverMovies(userId: String, page: Int = 1): List<Movie> {
        return httpClient.get("$baseUrl/matcher/discover") {
            parameter("userId", userId)
            parameter("page", page)
        }.body()
    }

    suspend fun syncProfile(request: InteractionRequest): TasteProfile {
        return httpClient.post("$baseUrl/profile/sync") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getNextQuestion(request: FinderQuestionRequest): FinderResponse {
        return httpClient.post("$baseUrl/finder/next-question") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}