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
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
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

object MyCustomIndication : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return CustomIndicationNode(interactionSource)
    }

    override fun hashCode(): Int = System.identityHashCode(this)

    override fun equals(other: Any?): Boolean = other === this

    // 定义内部 Node 处理绘制逻辑
    private class CustomIndicationNode(
        private val interactionSource: InteractionSource
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
            val scaleFactor = if (isPressed) 0.92f else 1f

            // 效果：按下时整体缩放
            scale(scaleFactor) {
                this@draw.drawContent()
            }

            // 效果：按下时叠加一层半透明前景
            if (isPressed) {
                drawRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    size = size
                )
            }
        }
    }
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
                indication = MyCustomIndication,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("绘图层反馈")
    }
}