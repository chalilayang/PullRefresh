
## 自定义Indicator1

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

## 自定义Indicator2

![](./s7aso-s2uj4.gif)

```kotlin
@Composable
fun DefaultPreview() {
    PullRefreshTheme {
        PullRefresh(
            state = rememberPullRefreshState(isRefreshing = false),
            onRefresh = {},
            refreshingIndicator = {
                    pullOffsetPx, triggerRefreshOffsetPx, isRefreshing, isPulling ->
                PullRefreshIndicator(
                    modifier = Modifier.padding(0.dp, 20.dp),
                    pullOffsetPx = pullOffsetPx,
                    isRefreshing = isRefreshing,
                    triggerOffsetPx = triggerRefreshOffsetPx,
                    isPulling = isPulling)
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

## build.gradle添加依赖

`implementation "io.github.chalilayang:pullrefresh:1.0.3"`

## Thanks

[accompanist-swiperefresh](https://google.github.io/accompanist/swiperefresh/)
