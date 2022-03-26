package com.chalilayang.pullrefresh

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun rememberPullRefreshState(
    isRefreshing: Boolean
): PullRefreshState {
    return remember {
        PullRefreshState(
            isRefreshing = isRefreshing
        )
    }.apply {
        this.isRefreshing = isRefreshing
    }
}

@Stable
class PullRefreshState(
    isRefreshing: Boolean
) {
    private val _pullOffsetPx = Animatable(0f)
    private val mutatorMutex = MutatorMutex()

    var isRefreshing: Boolean by mutableStateOf(isRefreshing)

    var isPulling: Boolean by mutableStateOf(false)
        internal set

    val pullOffsetPx: Float get() = _pullOffsetPx.value

    internal suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate {
            _pullOffsetPx.animateTo(offset)
        }
    }

    internal suspend fun dispatchScrollDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            _pullOffsetPx.snapTo(_pullOffsetPx.value + delta)
        }
    }

    fun updateRefreshStateByOffset(refreshTriggerPx: Float, onRefresh: () -> Unit) {
        if (!isRefreshing && pullOffsetPx >= refreshTriggerPx) {
            isRefreshing = true
            onRefresh.invoke()
        } else if (isRefreshing && pullOffsetPx < refreshTriggerPx * 0.6f) {
            isRefreshing = false
        }
    }
}

class PullRefreshNestedScrollConnection(
    private val state: PullRefreshState,
    private val maxOffsetPx: Float,
    private var triggerOffsetPx: Float,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {
    var enabled: Boolean = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.Drag -> onPreScroll(available)
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.Drag -> onPostScroll(available)
        else -> Offset.Zero
    }

    private fun onPreScroll(available: Offset): Offset {
        return if (available.y < 0) {
            preScroll(available = available)
        } else {
            Offset.Zero
        }
    }

    private fun onPostScroll(available: Offset): Offset {
        return if (available.y > 0) {
            postScroll(available = available)
        } else {
            Offset.Zero
        }
    }

    private fun preScroll(available: Offset): Offset {
        val offsetY = (available.y + state.pullOffsetPx).coerceIn(0f, maxOffsetPx)
        val dragConsumed = offsetY - state.pullOffsetPx
        return if (dragConsumed.absoluteValue > 0f) {
            state.isPulling = true
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            Offset(x = 0f, y = dragConsumed)
        } else {
            Offset.Zero
        }
    }

    private fun postScroll(available: Offset): Offset {
        val dragConsumed = computeOffsetConsume(available.y, state.pullOffsetPx, maxOffsetPx)
        return if (dragConsumed.absoluteValue > 0f) {
            state.isPulling = true
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            Offset(x = 0f, y = dragConsumed)
        } else {
            Offset.Zero
        }
    }

    private fun computeOffsetConsume(
        available: Float, curOffsetY: Float, maxOffsetY: Float): Float {
        val offsetPercent = min(1f, curOffsetY.absoluteValue / maxOffsetY)
        val rate = cos(offsetPercent * Math.PI / 2).toFloat()
        return available * rate
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val isPulling = state.isPulling
        state.isPulling = false
        state.updateRefreshStateByOffset(triggerOffsetPx) {
            onRefresh.invoke()
        }
        return if (isPulling) available else Velocity.Zero
    }
}

@Composable
fun rememberUpdatedIndicatorOffset(
    pullOffsetY: Float,
    maxOffsetY: Float,
): IndicatorOffset {
    val offsetPercent = min(1f, pullOffsetY / maxOffsetY)
    val extraOffset = abs(pullOffsetY) - maxOffsetY
    val tensionSlingshotPercent = max(0f, min(extraOffset, maxOffsetY * 2) / maxOffsetY)
    val tensionPercent = ((tensionSlingshotPercent / 4) - (tensionSlingshotPercent / 4).pow(2)) * 2
    val extraMove = maxOffsetY * tensionPercent
    val offset = (maxOffsetY * offsetPercent) + extraMove
    return remember { IndicatorOffset() }.apply {
        this.offset = offset
    }
}

@Stable
class IndicatorOffset {
    var offset: Float by mutableStateOf(0f)
}
