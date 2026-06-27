package org.ahugenb.movieswiper.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Composable
fun SwipeableCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    isFirstCard: Boolean = false,
    cardIndex: Int = 0,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val nudgeOffset = remember { Animatable(0f) }
    var hasInteracted by remember { mutableStateOf(false) }

    LaunchedEffect(isFirstCard, cardIndex, hasInteracted) {
        if (isFirstCard && !hasInteracted) {
            val initialDelay = if (cardIndex == 0) 3.seconds else 15.seconds
            delay(initialDelay)
            
            if (hasInteracted) return@LaunchedEffect

            val nudgeDist = 60f
            val spec = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy)

            // Nudge Left, Left
            nudgeOffset.animateTo(-nudgeDist, spec)
            nudgeOffset.animateTo(0f, spec)
            nudgeOffset.animateTo(-nudgeDist, spec)
            nudgeOffset.animateTo(0f, spec)
            
            delay(500)

            // Nudge Right, Right
            nudgeOffset.animateTo(nudgeDist, spec)
            nudgeOffset.animateTo(0f, spec)
            nudgeOffset.animateTo(nudgeDist, spec)
            nudgeOffset.animateTo(0f, spec)
        }
    }

    val draggableState = rememberDraggableState { delta ->
        hasInteracted = true
        offsetX += delta
    }

    val totalOffsetX = offsetX + nudgeOffset.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Removed padding to hit screen edges
            .offset { IntOffset(totalOffsetX.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = totalOffsetX / 80f
                alpha = 1f - (kotlin.math.abs(totalOffsetX) / 5000f).coerceIn(0f, 1f)
            }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (offsetX > 400) {
                        onSwipeRight()
                    } else if (offsetX < -400) {
                        onSwipeLeft()
                    }
                    offsetX = 0f
                }
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            content()
        }
    }
}