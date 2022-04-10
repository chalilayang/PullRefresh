package com.chalilayang.pullrefresh

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.OverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

typealias IndicatorComposable =
        @Composable (
            pullOffsetPx: Float,
            triggerRefreshOffsetPx: Float,
            isRefreshing: Boolean,
            isPulling: Boolean
        ) -> Unit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PullRefresh(
    state: PullRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    pullEnabled: Boolean = true,
    swipeMode: Boolean = false,
    refreshTriggerOffset: Dp = 120.dp,
    refreshingOffset: Dp = 100.dp,
    refreshingMaxPullOffset: Dp = 240.dp,
    refreshingIndicator: IndicatorComposable = {
            pullOffsetPx, triggerRefreshOffsetPx, isRefreshing, isPulling ->
        PullRefreshIndicator(
            modifier = Modifier.padding(0.dp, 20.dp),
            pullOffsetPx = pullOffsetPx,
            isRefreshing = isRefreshing,
            triggerOffsetPx = triggerRefreshOffsetPx,
            isPulling = isPulling,

        )
    },
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = rememberUpdatedState(onRefresh)
    val triggerOffsetPx = with(LocalDensity.current) { refreshTriggerOffset.toPx() }
    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }
    val maxOffsetPx = with(LocalDensity.current) { refreshingMaxPullOffset.toPx() }
    LaunchedEffect(state.isPulling, state.isRefreshing) {
        if (!state.isPulling) {
            val target = if (state.isRefreshing) {
                refreshingOffsetPx
            } else {
                0f
            }
            state.animateOffsetTo(target)
        }
    }
    val slingshot = rememberUpdatedIndicatorOffset(
        pullOffsetY = state.pullOffsetPx,
        maxOffsetY = triggerOffsetPx,
    )
    val nestedScrollConnection = remember(state, coroutineScope) {
        RefreshNestedScrollConnection(state, maxOffsetPx, triggerOffsetPx, coroutineScope) {
            updatedOnRefresh.value.invoke()
        }
    }.apply {
        this.enabled = pullEnabled
    }

    val overScrollConfig = if (pullEnabled) null else OverScrollConfiguration()
    CompositionLocalProvider(LocalOverScrollConfiguration provides overScrollConfig) {
        Box(
            modifier
                .fillMaxWidth()
                .clipToBounds()
                .nestedScroll(connection = nestedScrollConnection)) {
            val offset = IntOffset(0, if (swipeMode) 0 else state.pullOffsetPx.toInt())
            Box(modifier = Modifier.offset { offset }) {
                content()
            }
            var size by remember { mutableStateOf(IntSize(0, 0)) }
            Box(modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .onSizeChanged { size = it }
                .offset { IntOffset(0, slingshot.offset.toInt() - size.height) }
            ) {
                refreshingIndicator(
                    pullOffsetPx = state.pullOffsetPx,
                    isRefreshing = state.isRefreshing,
                    triggerRefreshOffsetPx = triggerOffsetPx,
                    isPulling = state.isPulling
                )
            }
        }
    }
}