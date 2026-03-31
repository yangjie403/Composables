package com.mjieg.composables.components

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ForegroundEffectButton() {
    val interactionSource = remember { MutableInteractionSource() }
    // 监听是否被按下
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(120.dp, 50.dp)
            .background(Color.Cyan, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 禁用默认水波纹，因为我们要自己写
                onClick = { }
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("自定义反馈")

        // 【前景遮罩层】
        // 当按下时，覆盖一层半透明黑影
        if (isPressed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .ignoreParentPadding(8.dp, 8.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun DrawEffectButton() {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(150.dp, 60.dp)
            .background(Color.Cyan, RoundedCornerShape(8.dp))
            .drawWithContent {
                drawContent() // 先画原本的内容（文字、背景等）

                // 如果被按下，就在内容之上画一层前景
                if (isPressed) {
                    drawRoundRect(color = Color(0x80BEBEBE), cornerRadius = CornerRadius(8.dp.toPx()))
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("绘图层反馈")
    }
}

class MyCustomIndication(
    private val shape: Shape = RoundedCornerShape(8.dp),
    private val pressedScale: Float = 0.92f,
    private val overlayColor: Color = Color.Black.copy(alpha = 0.2f)
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return CustomIndicationNode(interactionSource, shape, pressedScale, overlayColor)
    }

    override fun hashCode(): Int = System.identityHashCode(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MyCustomIndication) return false
        return shape == other.shape &&
                pressedScale == other.pressedScale &&
                overlayColor == other.overlayColor
    }

    // 定义内部 Node 处理绘制逻辑
    private class CustomIndicationNode(
        private val interactionSource: InteractionSource,
        private val shape: Shape,
        private val pressedScale: Float,
        private val overlayColor: Color
    ) : Modifier.Node(), DrawModifierNode {

        private var isPressed by mutableStateOf(false)

        // 当 Node 挂载到 UI 树时开始监听交互
        override fun onAttach() {
            coroutineScope.launch {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> isPressed = true
                        is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
                    }
                }
            }
        }

        // 核心绘制逻辑：直接操作 Canvas，不触发重组
        override fun ContentDrawScope.draw() {
            val scaleFactor = if (isPressed) pressedScale else 1f

            // 效果：按下时叠加一层半透明背景（使用指定形状）
            if (isPressed) {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = overlayColor
                )
            }

            // 效果：按下时整体缩放
            scale(scaleFactor) {
                this@draw.drawContent()
            }
        }
    }
}

// 最佳实践，在外部创建常用实例，避免重复创建
object CustomIndications {
    val Default = MyCustomIndication()

    val Round20 = MyCustomIndication(
        shape = RoundedCornerShape(20.dp),
        pressedScale = 0.92f,
        overlayColor = Color.Black.copy(alpha = 0.2f)
    )

    val Circle = MyCustomIndication(
        shape = RoundedCornerShape(50),
        pressedScale = 0.88f,
        overlayColor = Color.Black.copy(alpha = 0.2f)
    )
}

@Composable
fun CustomInteractionButton() {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(150.dp, 60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Cyan)
            .clickable(
                interactionSource = interactionSource,
                indication = CustomIndications.Round20,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("绘图层反馈")
    }
}

@Composable
fun CircularIndicationButton() {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(100.dp, 80.dp)
            .background(Color.Magenta)
            .clickable(
                interactionSource = interactionSource,
                indication = CustomIndications.Circle,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("圆形")
    }
}

@Composable
fun CustomShapeIndicationButton() {
    val interactionSource = remember { MutableInteractionSource() }
    val customShape = object : Shape {
        override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: androidx.compose.ui.unit.LayoutDirection, density: androidx.compose.ui.unit.Density): androidx.compose.ui.graphics.Outline {
            val path = android.graphics.Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(size.width * 0.3f, size.height * 0.3f)
                lineTo(0f, size.height * 0.3f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(0f, size.height)
                lineTo(size.width / 2, size.height * 0.7f)
                lineTo(size.width, size.height)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width, size.height * 0.3f)
                lineTo(size.width * 0.7f, size.height * 0.3f)
                close()
            }
            return Outline.Generic(path.asComposePath())
        }
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.Yellow)
            .clickable(
                interactionSource = interactionSource,
                indication = MyCustomIndication(
                    shape = customShape,
                    pressedScale = 0.9f,
                    overlayColor = Color.Blue.copy(alpha = 0.25f)
                ),
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("星形反馈")
    }
}