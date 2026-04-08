package com.mjieg.composables.components

import android.graphics.Matrix
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

class SmoothRoundedCornerShape(private val smoothing: Float = 0.8f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // 关键点：明确指定 centerX 和 centerY
        val roundedPolygon = RoundedPolygon.rectangle(
            width = size.width,
            height = size.height,
            rounding = CornerRounding(
                radius = size.minDimension / 4f,
                smoothing = smoothing
            ),
            centerX = size.width / 2f, // 将中心移到 Box 中心
            centerY = size.height / 2f  // 将中心移到 Box 中心
        )

        // 转换为 Compose 路径
        val path = roundedPolygon.toPath().asComposePath()
        return Outline.Generic(path)
    }
}

@Composable
fun SmoothRoundedCornerShapeExample() {
    // 使用示例
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(SmoothRoundedCornerShape(smoothing = 0.6f)) // 0.6f 接近 iOS 效果
            .background(Color.Blue),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Smooth Rounded Corner Shape", color = Color.White)
    }
}


@Composable
fun RoundedPolygonExample() {
    Box(
        modifier = Modifier
            .drawWithCache {
                val roundedPolygon = RoundedPolygon(
                    numVertices = 6,
                    radius = size.minDimension / 2,
                    centerX = size.width / 2,
                    centerY = size.height / 2
                )
                val roundedPolygonPath = roundedPolygon.toPath().asComposePath()
                onDrawBehind {
                    drawPath(roundedPolygonPath, color = Color.Blue)
                }
            }
            .fillMaxSize()
    )
}

@Composable
fun MorphingAnimation() {
    // 1. 定义起始和结束形状
    val startShape = remember {
        RoundedPolygon(
            numVertices = 3,
            rounding = CornerRounding(0.2f, smoothing = 1.0f)
        )
    } // 三角形
    val endShape = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 8,
            rounding = CornerRounding(0.2f, smoothing = 0.2f)
        )
    } // 8角星

    // 2. 创建 Morph 对象
    val morph = remember { Morph(startShape, endShape) }

    // 3. 设置动画进度 (0f 到 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "morph")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "progress"
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val matrix = Matrix()
        matrix.setScale(size.width / 2f, size.height / 2f)
        matrix.postTranslate(size.width / 2f, size.height / 2f)

        // 4. 获取当前进度的 Path
        val currentPath = morph.toPath(progress).asComposePath()
        currentPath.asAndroidPath().transform(matrix)

        drawPath(currentPath, color = Color.Cyan)
    }
}