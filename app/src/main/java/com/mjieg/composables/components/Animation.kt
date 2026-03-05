package com.mjieg.composables.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    cornerRadius: Dp = 12.dp,
    paddingValues: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    // 动画目标值
    val offsetY = remember { Animatable(0f) }
    // 修改1: 初始阴影高度增加到 10dp，使阴影更明显
    val elevation = remember { Animatable(10f) }

    // 监听按压状态
    LaunchedEffect(isPressed.value) {
        if (isPressed.value) {
            // 按下时：向下偏移，减少阴影
            launch {
                offsetY.animateTo(
                    targetValue = 4f,
                    animationSpec = tween(durationMillis = 100)
                )
            }
            launch {
                // 修改2: 按下时阴影降低到 3dp，保留一点阴影，避免完全消失
                elevation.animateTo(
                    targetValue = 3f,
                    animationSpec = tween(durationMillis = 100)
                )
            }
        } else {
            // 松开时：回到原位置，恢复阴影
            launch {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
            launch {
                // 恢复到 10dp
                elevation.animateTo(
                    targetValue = 10f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .shadow(
                elevation = elevation.value.dp,
                shape = RoundedCornerShape(cornerRadius),
                // 修改3: 降低透明度到 0.5f，使大范围阴影看起来更柔和自然
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 禁用默认指示器，使用自定义动画
                onClick = onClick
            )
            .padding(paddingValues)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Preview
@Composable
fun AnimatedButtonPreview() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        AnimatedButton(
            text = "点击我",
            onClick = { }
        )
    }
}