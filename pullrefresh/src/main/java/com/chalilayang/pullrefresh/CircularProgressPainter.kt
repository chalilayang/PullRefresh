package com.chalilayang.pullrefresh

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

internal class CircularProgressPainter : Painter() {
    var color by mutableStateOf(Color.Unspecified)
    var alpha by mutableStateOf(1f)
    var arcRadius by mutableStateOf(0.dp)
    var strokeWidth by mutableStateOf(5.dp)
    var arrowEnabled by mutableStateOf(false)
    var arrowWidth by mutableStateOf(0.dp)
    var arrowHeight by mutableStateOf(0.dp)
    var arrowScale by mutableStateOf(1f)

    private val arrow: Path by lazy {
        Path().apply { fillType = PathFillType.EvenOdd }
    }

    var startTrim by mutableStateOf(0f)
    var endTrim by mutableStateOf(0f)
    var rotation by mutableStateOf(0f)

    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun DrawScope.onDraw() {
        rotate(degrees = rotation) {
            val arcRadius = arcRadius.toPx() + strokeWidth.toPx() / 2f
            val arcBounds = Rect(
                size.center.x - arcRadius,
                size.center.y - arcRadius,
                size.center.x + arcRadius,
                size.center.y + arcRadius
            )
            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle
            drawArc(
                color = color,
                alpha = alpha,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcBounds.topLeft,
                size = arcBounds.size,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Square
                )
            )
            if (arrowEnabled) {
                drawArrow(startAngle, sweepAngle, arcBounds)
            }
        }
    }

    private fun DrawScope.drawArrow(startAngle: Float, sweepAngle: Float, bounds: Rect) {
        arrow.reset()
        arrow.moveTo(0f, 0f)
        arrow.lineTo(
            x = arrowWidth.toPx() * arrowScale,
            y = 0f
        )
        arrow.lineTo(
            x = arrowWidth.toPx() * arrowScale / 2,
            y = arrowHeight.toPx() * arrowScale
        )
        val radius = min(bounds.width, bounds.height) / 2f
        val inset = arrowWidth.toPx() * arrowScale / 2f
        arrow.translate(
            Offset(
                x = radius + bounds.center.x - inset,
                y = bounds.center.y + strokeWidth.toPx() / 2f
            )
        )
        arrow.close()
        rotate(degrees = startAngle + sweepAngle) {
            drawPath(
                path = arrow,
                color = color,
                alpha = alpha
            )
        }
    }
}

@Composable
internal fun rememberUpdatedSlingshot(
    offsetY: Float,
    maxOffsetY: Float,
): Slingshot {
    val offsetPercent = min(1f, offsetY / maxOffsetY)
    val adjustedPercent = max(offsetPercent - 0.4f, 0f) * 5 / 3
    val extraOffset = abs(offsetY) - maxOffsetY
    val tensionSlingshotPercent = max(
        0f, min(extraOffset, maxOffsetY * 2) / maxOffsetY
    )
    val tensionPercent = (
            (tensionSlingshotPercent / 4) -
                    (tensionSlingshotPercent / 4).pow(2)
            ) * 2
    val strokeStart = adjustedPercent * 0.8f
    val startTrim = 0f
    val endTrim = strokeStart.coerceAtMost(MaxProgressArc)
    val rotation = (-0.25f + 0.4f * adjustedPercent + tensionPercent * 2) * 0.5f
    val arrowScale = min(1f, adjustedPercent)

    return remember { Slingshot() }.apply {
        this.startTrim = startTrim
        this.endTrim = endTrim
        this.rotation = rotation
        this.arrowScale = arrowScale
    }
}

@Stable
internal class Slingshot {
    var startTrim: Float by mutableStateOf(0f)
    var endTrim: Float by mutableStateOf(0f)
    var rotation: Float by mutableStateOf(0f)
    var arrowScale: Float by mutableStateOf(0f)
}

internal const val MaxProgressArc = 0.8f