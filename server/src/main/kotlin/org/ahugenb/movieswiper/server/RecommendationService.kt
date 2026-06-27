package org.ahugenb.movieswiper.server

import org.ahugenb.movieswiper.core.logic.DiscoverQuery
import org.ahugenb.movieswiper.core.logic.DiscoverQueryBuilder
import org.ahugenb.movieswiper.core.logic.MoviePool
import org.ahugenb.movieswiper.core.logic.TastePreferences
import org.ahugenb.movieswiper.core.models.FinderAnswer
import org.ahugenb.movieswiper.core.models.Movie
import org.ahugenb.movieswiper.core.models.TasteProfile
import org.ahugenb.movieswiper.data.api.TmdbApi
import org.ahugenb.movieswiper.server.smile.SmileRecommendationEngine

class RecommendationService(
    private val tmdbApi: TmdbApi,
    private val tasteService: TasteService,
    private val smileEngine: SmileRecommendationEngine = SmileRecommendationEngine(),
) {
    suspend fun discoverForMatcher(userId: String, page: Int): List<Movie> {
        val profile = tasteService.getProfile(userId)
        val query = DiscoverQueryBuilder.forMatcher(profile, page)
        val movies = fetchMovies(query)
        return rankMovies(userId, movies, TastePreferences(), profile)
    }

    suspend fun buildFinderPool(answers: List<FinderAnswer>): List<Movie> {
        val pages = (1..3).flatMap { page ->
            val query = DiscoverQueryBuilder.forFinder(answers, page)
            fetchMovies(query)
        }
        return MoviePool.filterByAnswers(pages, answers)
    }

    suspend fun recommendFromPool(
        pool: List<Movie>,
        userId: String,
        answers: List<FinderAnswer>,
        limit: Int = 10,
    ): List<Movie> {
        val profile = tasteService.getProfile(userId)
        val preferences = TastePreferences.fromAnswers(answers)
        return rankMovies(userId, pool, preferences, profile, limit)
    }

    private suspend fun fetchMovies(query: DiscoverQuery): List<Movie> {
        return tmdbApi.discoverMovies(
            page = query.page,
            withGenres = query.withGenres.takeIf { it.isNotEmpty() },
            withoutGenres = query.withoutGenres.takeIf { it.isNotEmpty() },
            primaryReleaseDateGte = query.primaryReleaseDateGte,
            primaryReleaseDateLte = query.primaryReleaseDateLte,
            sortBy = query.sortBy,
            voteAverageGte = query.voteAverageGte,
            voteCountGte = query.voteCountGte,
        )
    }

    private fun rankMovies(
        userId: String,
        movies: List<Movie>,
        preferences: TastePreferences,
        profile: TasteProfile,
        limit: Int = 20,
    ): List<Movie> {
        return smileEngine.rankMovies(
            userId = userId,
            movies = movies,
            interactions = tasteService.getInteractions(userId),
            preferences = preferences,
            profile = profile,
            limit = limit,
        )
    }
}
