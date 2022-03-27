![](./isq9l-2271z.gif)

```kotlin
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
                Text(text = tip)
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
```


`implementation "io.github.chalilayang:pullrefresh:1.0.0"`