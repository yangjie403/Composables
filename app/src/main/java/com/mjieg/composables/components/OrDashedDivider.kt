package com.mjieg.composables.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrDashedDivider(
    modifier: Modifier = Modifier,
    text: String = "OR", // 中间显示的文字
    fontWeight: FontWeight = FontWeight.Normal,
    fontPadding: Dp = 8.dp,
    fontSize: TextUnit = 10.sp,
    textColor: Color = Color.White, // 文字颜色（默认为中灰色）
    lineColor: Color = Color.White, // 虚线颜色
    dashLength: Dp = 2.dp,  // 单个虚线段的长度
    dashGap: Dp = 2.dp,     // 虚线段之间的间隔
    lineWidth: Dp = 1.dp    // 虚线的粗细
) {
    val density = LocalDensity.current

    // 将 dp 单位转换为 px，供 Canvas 绘制时使用
    val dashLengthPx = remember(dashLength) { with(density) { dashLength.toPx() } }
    val dashGapPx = remember(dashGap) { with(density) { dashGap.toPx() } }
    val strokeWidthPx = remember(lineWidth) { with(density) { lineWidth.toPx() } }

    // 1. 判断是否为 Android 9 (API 28) 或以上系统
    val isAndroid9OrAbove = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.P }

    // 2. 在外部缓存 PathEffect，避免每次重绘时发生内存抖动
    val pathEffect = remember(dashLengthPx, dashGapPx) {
        PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashLengthPx, dashGapPx),
            phase = 0f
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically // 确保垂直方向上完美居中
    ) {
        // 左侧虚线 Canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(lineWidth * 4) // 给予 Canvas 略高于线宽的高度，防止边缘裁剪
        ) {
            drawDashedLineCompat(
                isAndroid9OrAbove = isAndroid9OrAbove,
                lineColor = lineColor,
                strokeWidthPx = strokeWidthPx,
                pathEffect = pathEffect,
                dashLengthPx = dashLengthPx,
                dashGapPx = dashGapPx
            )
        }

        // 中间文字（保留您自定义的 wsp/wdp 屏幕自适应单位）
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            modifier = Modifier.padding(horizontal = fontPadding)
        )

        // 右侧虚线 Canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(lineWidth * 4)
                .scale(scaleX = -1f, scaleY = 1f)
        ) {
            drawDashedLineCompat(
                isAndroid9OrAbove = isAndroid9OrAbove,
                lineColor = lineColor,
                strokeWidthPx = strokeWidthPx,
                pathEffect = pathEffect,
                dashLengthPx = dashLengthPx,
                dashGapPx = dashGapPx
            )
        }
    }
}

private fun DrawScope.drawDashedLineCompat(
    isAndroid9OrAbove: Boolean,
    lineColor: Color,
    strokeWidthPx: Float,
    pathEffect: PathEffect,
    dashLengthPx: Float,
    dashGapPx: Float
) {
    val y = size.height / 2f

    if (isAndroid9OrAbove) {
        // Android 9+ 直接采用原生高效的 PathEffect 渲染
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidthPx,
            pathEffect = pathEffect
        )
    } else {
        // Android 9 以下版本，采用循环小段线段渲染，以规避硬件加速 Bug
        var startX = 0f
        while (startX < size.width) {
            val endX = (startX + dashLengthPx).coerceAtMost(size.width)
            drawLine(
                color = lineColor,
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = strokeWidthPx
            )
            startX += dashLengthPx + dashGapPx
        }
    }
}