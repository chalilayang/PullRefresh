package com.chalilayang.pullrefresh

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
private data class PullRefreshIndicatorSizes(
    val size: Dp,
    val arcRadius: Dp,
    val strokeWidth: Dp,
    val arrowWidth: Dp,
    val arrowHeight: Dp,
)

private val DefaultSizes = PullRefreshIndicatorSizes(
    size = 40.dp,
    arcRadius = 7.5.dp,
    strokeWidth = 2.5.dp,
    arrowWidth = 10.dp,
    arrowHeight = 5.dp,
)

@Composable
fun PullRefreshIndicator(
    pullOffsetPx: Float,
    triggerOffsetPx: Float,
    isRefreshing: Boolean,
    isPulling: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Blue,
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    elevation: Dp = 6.dp,
) {
    val slingshot = rememberUpdatedSlingshot(
        offsetY = pullOffsetPx,
        maxOffsetY = triggerOffsetPx,
    )

    val adjustedElevation = when {
        isRefreshing -> elevation
        pullOffsetPx > 0.5f -> elevation
        else -> 0.dp
    }

    val sizes = DefaultSizes
    Surface(
        modifier = modifier.size(size = sizes.size),
        shape = shape,
        color = backgroundColor,
        elevation = adjustedElevation
    ) {
        val painter = remember { CircularProgressPainter() }
        painter.arcRadius = sizes.arcRadius
        painter.strokeWidth = sizes.strokeWidth
        painter.arrowWidth = sizes.arrowWidth
        painter.arrowHeight = sizes.arrowHeight
        painter.arrowEnabled = !isRefreshing
        painter.color = contentColor
        val alpha = (pullOffsetPx / triggerOffsetPx).coerceIn(0f, 1f)
        painter.alpha = alpha
        painter.startTrim = slingshot.startTrim
        painter.endTrim = slingshot.endTrim
        painter.rotation = slingshot.rotation
        painter.arrowScale = slingshot.arrowScale

        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = CrossFadeDurationMs)
        ) { refreshing ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (refreshing) {
                    val circleSize = (sizes.arcRadius + sizes.strokeWidth) * 2
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = sizes.strokeWidth,
                        modifier = Modifier.size(circleSize),
                    )
                } else {
                    Image(
                        painter = painter,
                        contentDescription = "Refreshing"
                    )
                }
            }
        }
    }
}

private const val CrossFadeDurationMs = 100