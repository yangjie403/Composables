package com.mjieg.composables.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mjieg.composables.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CircularThickWavyProgressIndicatorSample() {
    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    val thickStrokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            // Thick size is slightly larger than the
            // WavyProgressIndicatorDefaults.CircularContainerSize default
            modifier = Modifier.size(80.dp), // Thick size is slightly larger than the default
            stroke = thickStroke,
            trackStroke = thickStroke,
        )
        Spacer(Modifier.requiredHeight(30.dp))
        Text("Set progress:")
        Slider(
            modifier = Modifier.width(300.dp),
            value = progress,
            valueRange = 0f..1f,
            onValueChange = { progress = it },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CircularWavyProgressIndicatorSample() {
    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularWavyProgressIndicator(progress = { animatedProgress })
        Spacer(Modifier.requiredHeight(30.dp))
        Text("Set progress:")
        Slider(
            modifier = Modifier.width(300.dp),
            value = progress,
            valueRange = 0f..1f,
            onValueChange = { progress = it },
        )
    }
}

@Composable
@Preview
fun ProgressPreviewLayout() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularWavyProgressIndicatorSample()
        Spacer(Modifier.requiredHeight(30.dp))
        CircularThickWavyProgressIndicatorSample()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun DeterminateContainedLoadingIndicatorSample() {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by
    animateFloatAsState(
        targetValue = progress,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow,
                visibilityThreshold = 1 / 1000f,
            ),
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ContainedLoadingIndicator(progress = { animatedProgress })
        Spacer(Modifier.requiredHeight(30.dp))
        Text("Set loading progress:")
        Slider(
            modifier = Modifier.width(300.dp),
            value = progress,
            valueRange = 0f..1f,
            onValueChange = { progress = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun LoadingIndicatorPullToRefreshSample() {
    var itemCount by remember { mutableIntStateOf(15) }
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            // fetch something
            delay(5000)
            itemCount += 5
            isRefreshing = false
        }
    }
    val scaleFraction = {
        if (isRefreshing) 1f
        else LinearOutSlowInEasing.transform(state.distanceFraction).coerceIn(0f, 1f)
    }
    Scaffold(
        modifier =
            Modifier.pullToRefresh(
                state = state,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
            ),
        topBar = {
            TopAppBar(
                title = { Text("TopAppBar") },
                // Provide an accessible alternative to trigger refresh.
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, "Trigger Refresh")
                    }
                },
            )
        },
    ) {
        Box(Modifier.padding(it)) {
            LazyColumn(Modifier.fillMaxSize()) {
                if (!isRefreshing) {
                    items(itemCount) { ListItem({ Text(text = "Item ${itemCount - it}") }) }
                }
            }
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        scaleX = scaleFraction()
                        scaleY = scaleFraction()
                    }
            ) {
                PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = isRefreshing)
            }
        }
    }
}

@Composable
fun ImageRotationLoading(
    modifier: Modifier = Modifier,
    durationMillis: Int = 3000 // 旋转一圈所需的时间（毫秒）
) {
    // 1. 创建无限循环的动画状态
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    // 2. 定义从 0 到 360 度的动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            // 使用 LinearEasing 确保旋转过程匀速，不会有顿挫感
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box {
        // 3. 显示图片并应用旋转
        Image(
            painter = painterResource(id = R.drawable.ic_loading), // 你的图片资源
            contentDescription = "Loading",
            modifier = modifier
                .size(48.dp) // 设置大小
                .graphicsLayer {
                    // 使用 graphicsLayer 实现旋转，性能比 Modifier.rotate 更优
                    // 因为它只在绘制阶段处理，不会触发布局重测
                    rotationZ = rotation
                    transformOrigin = TransformOrigin.Center
                }
        )
    }
}