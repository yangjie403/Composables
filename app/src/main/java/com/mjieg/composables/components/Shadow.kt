package com.mjieg.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun ElevationBasedShadow() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(200.dp, 200.dp)
                .dropShadow(
                    shape = RectangleShape,
                    shadow = Shadow(
                        color = Color.Blue,
                        radius = 0.dp, // 模糊半径
                        spread = 0.dp, // 扩展半径
                        offset = DpOffset(6.dp, 6.dp), // 阴影偏移量
                        blendMode = DefaultBlendMode,
                    )
                )
                .border(6.dp, Color.Green, RectangleShape)
                .background(Color.Red)
        )
    }
}