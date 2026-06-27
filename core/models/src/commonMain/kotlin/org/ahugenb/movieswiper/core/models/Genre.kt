package org.ahugenb.movieswiper.core.models

object GenreConstants {
    val allGenreIds: List<Int> = listOf(
        28, 12, 16, 35, 80, 99, 18, 10751, 14, 36, 27, 10402, 9648, 10749, 878, 10770, 53, 10752, 37,
    )

    private val genreMap = mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Sci-Fi",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western"
    )

    fun getGenreName(id: Int): String = genreMap[id] ?: "Other"
    
    // Using hex strings for colors to avoid direct dependency on UI in models
    private val colorMap = mapOf(
        28 to "#E50914",    // Action: Red
        12 to "#FF8C00",    // Adventure: Orange
        16 to "#FFD700",    // Animation: Yellow
        35 to "#FF69B4",    // Comedy: Pink
        80 to "#4B0082",    // Crime: Indigo
        99 to "#2F4F4F",    // Documentary: Dark Slate Gray
        18 to "#8B4513",    // Drama: Saddle Brown
        10751 to "#ADFF2F", // Family: Green Yellow
        14 to "#9370DB",    // Fantasy: Medium Purple
        36 to "#A0522D",    // History: Sienna
        27 to "#000000",    // Horror: Black
        10402 to "#1E90FF", // Music: Dodger Blue
        9648 to "#708090",  // Mystery: Slate Gray
        10749 to "#DC143C", // Romance: Crimson
        878 to "#00FFFF",   // Sci-Fi: Cyan
        10770 to "#BC8F8F", // TV Movie: Rosy Brown
        53 to "#FF4500",    // Thriller: Orange Red
        10752 to "#556B2F", // War: Dark Olive Green
        37 to "#D2B48C"     // Western: Tan
    )

    fun getGenreColorHex(id: Int): String = colorMap[id] ?: "#808080"
}