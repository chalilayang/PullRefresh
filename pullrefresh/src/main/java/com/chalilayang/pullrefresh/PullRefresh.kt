package com.chalilayang.pullrefresh

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.OverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
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
    refreshTriggerOffset: Dp = 120.dp,
    refreshingOffset: Dp = 100.dp,
    refreshingMaxPullOffset: Dp = 240.dp,
    refreshingIndicator: IndicatorComposable = {
            pullOffsetPx, triggerRefreshOffsetPx, isRefreshing, isPulling ->
        PullRefreshIndicator(
            pullOffsetPx = pullOffsetPx,
            isRefreshing = isRefreshing,
            triggerOffsetPx = triggerRefreshOffsetPx,
            isPulling = isPulling
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
        PullRefreshNestedScrollConnection(state, maxOffsetPx, triggerOffsetPx, coroutineScope) {
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
//            Row(modifier = Modifier.background(Color.Red).fillMaxWidth().height(refreshingMaxPullOffset)) {}
//            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth().height(refreshTriggerOffset)) {}
//            Row(modifier = Modifier.background(Color.Blue).fillMaxWidth().height(refreshingOffset)) {}
            Box(modifier = Modifier.offset { IntOffset(0, state.pullOffsetPx.toInt()) }) {
                content()
            }
            var size by remember { mutableStateOf(IntSize(0, 0)) }
            Row(modifier = Modifier
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