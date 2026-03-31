package com.mjieg.composables.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun XiaohongshuLikeExplosionButton() {
    var isLiked by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    // 1. 爱心本体的缩放动画
    val heartScale = remember { Animatable(1f) }
    // 2. 烟花爆裂动画进度 (从 0f 到 1f)
    val explosionProgress = remember { Animatable(0f) }

    val tintColor by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFFE2C55) else Color(0xFF9E9E9E),
        animationSpec = tween(durationMillis = 200)
    )
    val icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder

    Box(
        modifier = Modifier
            // Box 必须比爱心大，留出空间给外围的散落圆点和圆环
            .size(72.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null // 禁用默认涟漪
            ) {
                isLiked = !isLiked
                if (isLiked) {
                    coroutineScope.launch {
                        // 【并发执行】 启动爆炸特效
                        launch {
                            explosionProgress.snapTo(0f) // 重置爆炸进度
                            explosionProgress.animateTo(
                                targetValue = 1f,
                                // 爆炸特效通常一开始极快，然后慢下来散开
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        }

                        // 【并发执行】 爱心Q弹动画
                        heartScale.animateTo(0.7f, tween(100))
                        heartScale.animateTo(
                            1.3f,
                            spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium)
                        )
                        heartScale.animateTo(
                            1f,
                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                        )
                    }
                } else {
                    // 取消点赞时不需要爆炸特效，只有爱心回缩
                    coroutineScope.launch {
                        launch {
                            explosionProgress.snapTo(0f)
                        }
                        heartScale.animateTo(0.8f, tween(100))
                        heartScale.animateTo(1f, spring(Spring.DampingRatioNoBouncy))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 绘制爆炸特效的画布 (处于爱心底层)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val p = explosionProgress.value
            if (p > 0f && p < 1f) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width / 2

                // --- 绘制向外扩张的圆环 ---
                // 圆环从 30% 大小扩张到 70% 大小
                val ringRadius = maxRadius * 0.3f + (maxRadius * 0.4f) * p
                // 圆环快速变透明消失 (进度到 0.8 时就完全透明了)
                val ringAlpha = (1f - p * 1.25f).coerceIn(0f, 1f)
                if (ringAlpha > 0f) {
                    drawCircle(
                        color = Color(0xFFFE2C55),
                        radius = ringRadius,
                        center = center,
                        // 圆环在扩大的同时线条变细
                        style = Stroke(width = 3.dp.toPx() * ringAlpha),
                        alpha = ringAlpha
                    )
                }

                // --- 绘制呈放射状散开的 8 个小圆点 ---
                val dotCount = 8
                // 圆点从靠近中心的地方散开到边缘
                val startDotRadius = maxRadius * 0.3f
                val endDotRadius = maxRadius * 0.9f
                val currentDotRadius = startDotRadius + (endDotRadius - startDotRadius) * p

                // 圆点的透明度渐变
                val dotAlpha = (1f - p).coerceIn(0f, 1f)
                // 圆点在向外散落的过程中慢慢变小
                val dotBaseSize = 3.5.dp.toPx()
                val dotSize = dotBaseSize * (1f - p * 0.5f)

                for (i in 0 until dotCount) {
                    // 利用三角函数计算均匀分布的角度和坐标
                    val angle = i * (2 * PI / dotCount)
                    val x = center.x + currentDotRadius * cos(angle).toFloat()
                    val y = center.y + currentDotRadius * sin(angle).toFloat()

                    drawCircle(
                        color = Color(0xFFFE2C55),
                        radius = dotSize,
                        center = Offset(x, y),
                        alpha = dotAlpha
                    )
                }
            }
        }

        // 顶层：爱心图标本体
        Icon(
            imageVector = icon,
            contentDescription = "Like",
            tint = tintColor,
            modifier = Modifier
                .size(32.dp) // 爱心本体尺寸比 Box 小，给特效留足空间
                .graphicsLayer {
                    scaleX = heartScale.value
                    scaleY = heartScale.value
                }
        )
    }
}

@Composable
fun ZeroRecompositionLikeButton(onClick: (isLiked: Boolean) -> Unit = {}) {
    // 内部状态仅在 onClick 的 lambda 中读取和修改，不在组合(Composition)作用域内读取
    // 这意味着 isLiked 的改变不会触发当前组件的重组！
    var isLiked by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val lastClickTime = remember { mutableLongStateOf(0L) }
    // 动画状态：全部使用 Animatable，以便我们在 graphicsLayer 中精准提取 value
    val heartScale = remember { Animatable(1f) }
    val explosionProgress = remember { Animatable(0f) }
    val crossfadeAlpha = remember { Animatable(0f) } // 0f = 显示灰色空心, 1f = 显示红色实心

    Box(
        modifier = Modifier
            .size(72.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (System.currentTimeMillis() - lastClickTime.longValue < 500) {
                    return@clickable
                }
                lastClickTime.longValue = System.currentTimeMillis()
                isLiked = !isLiked // 状态修改
                onClick(isLiked)
                if (isLiked) {
                    coroutineScope.launch {
                        // 1. 颜色渐变 (修改透明度，不触发重组)
                        launch { crossfadeAlpha.animateTo(1f, tween(200)) }

                        // 2. 爆炸特效
                        launch {
                            explosionProgress.snapTo(0f)
                            explosionProgress.animateTo(
                                1f,
                                tween(400, easing = LinearOutSlowInEasing)
                            )
                        }

                        // 3. Q弹缩放
                        launch {
                            heartScale.animateTo(0.7f, tween(100))
                            heartScale.animateTo(
                                1.3f,
                                spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium)
                            )
                            heartScale.animateTo(
                                1f,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                            )
                        }
                    }
                } else {
                    coroutineScope.launch {
                        launch { crossfadeAlpha.animateTo(0f, tween(200)) }
                        launch {
                            heartScale.animateTo(0.8f, tween(100))
                            heartScale.animateTo(1f, spring(Spring.DampingRatioNoBouncy))
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // --- 第一层：爆炸特效 Canvas ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 【极其关键】在 Canvas (DrawScope) 内部读取 value，仅触发绘制阶段(Draw Phase)
            val p = explosionProgress.value
            if (p > 0f && p < 1f) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width / 2

                // 绘制圆环
                val ringRadius = maxRadius * 0.3f + (maxRadius * 0.4f) * p
                val ringAlpha = (1f - p * 1.25f).coerceIn(0f, 1f)
                if (ringAlpha > 0f) {
                    drawCircle(
                        color = Color(0xFFFE2C55),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx() * ringAlpha),
                        alpha = ringAlpha
                    )
                }

                // 绘制散开的圆点
                val dotCount = 8
                val startDotRadius = maxRadius * 0.3f
                val endDotRadius = maxRadius * 0.9f
                val currentDotRadius = startDotRadius + (endDotRadius - startDotRadius) * p
                val dotAlpha = (1f - p).coerceIn(0f, 1f)
                val dotSize = 3.5.dp.toPx() * (1f - p * 0.5f)

                for (i in 0 until dotCount) {
                    val angle = i * (2 * PI / dotCount)
                    val x = center.x + currentDotRadius * cos(angle).toFloat()
                    val y = center.y + currentDotRadius * sin(angle).toFloat()

                    drawCircle(
                        color = Color(0xFFFE2C55),
                        radius = dotSize,
                        center = Offset(x, y),
                        alpha = dotAlpha
                    )
                }
            }
        }

        // --- 第二层：爱心本体容器 ---
        Box(
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    // 【极其关键】在 graphicsLayer 内部读取 scale，不触发重组，直接交由 GPU 缩放
                    val scale = heartScale.value
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            // 底图：灰色空心爱心 (点赞时透明度逐渐变为0)
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 【极其关键】仅在绘制阶段改变透明度
                        alpha = 1f - crossfadeAlpha.value
                    }
            )

            // 顶图：红色实心爱心 (点赞时透明度逐渐变为1)
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFFE2C55),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 【极其关键】仅在绘制阶段改变透明度
                        alpha = crossfadeAlpha.value
                    }
            )
        }
    }
}