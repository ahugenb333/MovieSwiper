package org.ahugenb.movieswiper.feature.matcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.ahugenb.movieswiper.core.models.GenreConstants
import org.ahugenb.movieswiper.ui.SwipeableCard

@Composable
fun MatcherContent(component: MatcherComponent) {
    val state by component.state.subscribeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        state.error?.let { error ->
            Text(
                text = error, 
                color = MaterialTheme.colorScheme.error, 
                modifier = Modifier.align(Alignment.Center)
            )
        }

        state.movies.asReversed().forEachIndexed { index, movie ->
            val isFirstCard = index == state.movies.size - 1
            key(movie.id) {
                SwipeableCard(
                    onSwipeLeft = component::onSwipeLeft,
                    onSwipeRight = component::onSwipeRight,
                    isFirstCard = isFirstCard,
                    cardIndex = state.swipedCount
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Poster - Top 2/3
                        Box(
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxWidth()
                        ) {
                            movie.fullPosterUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = movie.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Info Section - Bottom 1/3
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Dynamic Genre Tags - Themed and High Contrast
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                movie.genreIds.take(3).forEach { id ->
                                    val genreName = GenreConstants.getGenreName(id)
                                    val colorHex = GenreConstants.getGenreColorHex(id)
                                    val tagColor = Color(parseColor(colorHex))
                                    
                                    Surface(
                                        color = tagColor.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = genreName,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = Color.White, // Always white on dark tags for contrast
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = movie.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }

        if (!state.isLoading && state.movies.isEmpty()) {
            Text(
                text = "No more movies!", 
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun parseColor(hex: String): Int {
    return if (hex.startsWith("#")) {
        hex.substring(1).toLong(16).toInt() or -0x1000000
    } else {
        0xFF808080.toInt()
    }
}