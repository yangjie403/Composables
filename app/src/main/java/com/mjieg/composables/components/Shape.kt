package com.mjieg.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
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
