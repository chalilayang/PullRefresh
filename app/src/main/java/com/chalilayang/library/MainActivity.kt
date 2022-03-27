package com.chalilayang.library

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalilayang.library.ui.theme.PullRefreshTheme
import com.chalilayang.pullrefresh.PullRefresh
import com.chalilayang.pullrefresh.rememberPullRefreshState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PullRefreshTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DefaultPreview()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PullRefreshTheme {
        PullRefresh(
            state = rememberPullRefreshState(isRefreshing = false),
            onRefresh = {},
            refreshingIndicator = { offsetPx, triggerOffsetPx, isRefreshing, isPulling ->
                val tip = if (isRefreshing) {
                    "正在刷新"
                } else if (isPulling && offsetPx >= triggerOffsetPx) {
                    "松手刷新"
                } else if (isPulling && offsetPx < triggerOffsetPx) {
                    "下拉刷新，松手恢复常态"
                } else {
                    "正在恢复常态"
                }
                Text(text = tip, modifier = Modifier.padding(0.dp, 40.dp))
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                repeat(80) {
                    item {
                        Greeting("Android $it")
                    }
                }
            }
        }
    }
}