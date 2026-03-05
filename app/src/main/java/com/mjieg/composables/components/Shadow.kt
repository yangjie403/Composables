package com.mjieg.composables.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@Composable
fun SimpleDropShadow() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(200.dp, 200.dp)
                // dropShadow必须在background前面设置
                .dropShadow(
                    shape = RoundedCornerShape(20.dp),
                    shadow = Shadow(
                        color = Color(0x40000000),
                        radius = 10.dp,
                        spread = 6.dp
                    )
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Drop Shadow",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SimpleInnerShadow() {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .width(300.dp)
                .height(200.dp)
                .align(Alignment.Center)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                // innerShadow必须在background后面设置
                .innerShadow(
                    shape = RoundedCornerShape(20.dp),
                    shadow = Shadow(
                        radius = 10.dp,
                        spread = 2.dp,
                        color = Color(0x40000000),
                        offset = DpOffset(x = 6.dp, 7.dp)
                    )
                )

        ) {
            Text(
                "Inner Shadow",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 32.sp
            )
        }
    }
}

@Composable
fun AnimatedColoredShadow() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val isPressed = interactionSource.collectIsPressedAsState()
        val transition = updateTransition(
            targetState = isPressed.value,
            label = "button_press_transition"
        )
        val blueDropShadowColor = remember {
            Color.Blue.copy(alpha = 0.6f)
        }

        fun <T> buttonPressAnimation() = tween<T>(
            durationMillis = 400,
            easing = EaseOut
        )

        val shadowAlpha by transition.animateFloat(
            label = "shadow_alpha",
            transitionSpec = { buttonPressAnimation() }
        ) { pressed ->
            if (pressed) 0f else 1f
        }
        val innerShadowAlpha by transition.animateFloat(
            label = "inner_shadow_alpha",
            transitionSpec = { buttonPressAnimation() }
        ) { pressed ->
            if (pressed) 1f else 0f
        }
        val blueDropShadow by transition.animateColor(
            label = "shadow_color",
            transitionSpec = { buttonPressAnimation() }
        ) { pressed ->
            if (pressed) Color.Transparent else blueDropShadowColor
        }
        Box(
            Modifier
                .width(300.dp)
                .height(200.dp)
                .align(Alignment.Center)
                .dropShadow(
                    shape = RoundedCornerShape(50.dp),
                    shadow = Shadow(
                        radius = 10.dp,
                        spread = 0.dp,
                        color = Color.Blue.copy(alpha = 0.4f),
                        offset = DpOffset(2.dp, 4.dp),
                        alpha = shadowAlpha
                    )
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(50.dp)
                )
                .innerShadow(
                    shape = RoundedCornerShape(50.dp),
                    shadow = Shadow(
                        radius = 10.dp,
                        spread = 0.dp,
                        color = Color.Blue.copy(alpha = 0.4f),
                        offset = DpOffset(2.dp, 4.dp),
                        alpha = innerShadowAlpha
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {}
        ) {
            Text(
                "Animated Shadow",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}