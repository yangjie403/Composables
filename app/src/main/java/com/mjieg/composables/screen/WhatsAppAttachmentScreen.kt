package com.mjieg.composables.screen

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max

@Composable
fun WhatsAppAttachmentScreen() {
    var isMenuExpanded by remember { mutableStateOf(false) }

    // 控制动画进度的状态 (0f 到 1f)
    val revealProgress by animateFloatAsState(
        targetValue = if (isMenuExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "RevealProgress"
    )

    // 根布局
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECE5DD)) // 类似 WhatsApp 的背景色
    ) {
        // 如果菜单打开，添加一个全屏的透明遮罩，点击外部收起菜单
        if (isMenuExpanded || revealProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isMenuExpanded = false }
            )
        }

        // 附件菜单卡片
        // 只有当 progress > 0 时才将其放入组合中，避免隐藏时阻挡触摸事件
        // if (revealProgress > 0f)
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp) // 悬浮在底部操作栏上方
                .width(320.dp)
                // 使用 graphicsLayer 进行图形裁剪动画，不触发重组，点击区域也会被裁剪，但会频繁创建Shape对象
                .graphicsLayer {
                    clip = true
                    shape = CircularRevealShape(
                        progress = revealProgress,
                        // 设置圆心为卡片的右下角偏左位置（对准下方的附件按钮）
                        buttonOffsetRatioX = 0.7f
                    )
                },
                // 使用 drawWithCache 进行图形裁剪动画，不触发重组，
                // 但也不会裁剪点击区域，即使隐藏也会触发点击事件，progress为0时需从组合树中移除
                // .drawWithCache {
                //     // 1. 这里的代码只在 View 大小改变时执行，用于缓存对象 (只创建一次 Path!)
                //     val path = Path()
                //     val buttonOffsetRatioX = 0.85f
                //
                //     // 2. onDrawWithContent 内部的代码会在每帧绘制时执行
                //     onDrawWithContent {
                //         val cx = size.width * buttonOffsetRatioX
                //         val cy = size.height
                //
                //         val maxRadius = hypot(
                //             max(cx, size.width - cx).toDouble(),
                //             max(cy, size.height - cy).toDouble()
                //         ).toFloat()
                //
                //         // 重点：在这里读取动画进度，这只会触发 Draw 阶段的重绘，不触发 Recomposition
                //         val currentRadius = maxRadius * revealProgress
                //
                //         // 3. 复用同一个 Path 对象，先重置，再添加新的圆
                //         path.reset()
                //         path.addOval(
                //             Rect(
                //                 left = cx - currentRadius,
                //                 top = cy - currentRadius,
                //                 right = cx + currentRadius,
                //                 bottom = cy + currentRadius
                //             )
                //         )
                //
                //         // 4. 使用 Path 进行裁剪，然后绘制卡片内容
                //         clipPath(path) {
                //             this@onDrawWithContent.drawContent()
                //         }
                //     }
                // },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            // 菜单内部的网格视图
            AttachmentMenuGrid()
        }

        // 底部操作栏
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 触发动画的附件按钮
            IconButton(
                onClick = { isMenuExpanded = !isMenuExpanded },
                modifier = Modifier
                    .background(Color(0xFF00897B), RoundedCornerShape(24.dp))
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Attachment",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 自定义圆形揭示裁剪形状
 */
class CircularRevealShape(
    private val progress: Float,
    private val buttonOffsetRatioX: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // 1. 计算扩散的中心点 (cx, cy)
        val cx = size.width * buttonOffsetRatioX
        val cy = size.height

        // 2. 计算最大半径（使用勾股定理计算圆心到最远顶点-左上角的距离）
        val maxRadius = hypot(
            max(cx, size.width - cx).toDouble(),
            max(cy, size.height - cy).toDouble()
        ).toFloat()

        // 3. 当前半径 = 最大半径 * 动画进度
        val currentRadius = maxRadius * progress

        // 4. 返回一个圆形的 Path 作为裁剪轮廓
        val path = Path().apply {
            addOval(
                Rect(
                    left = cx - currentRadius,
                    top = cy - currentRadius,
                    right = cx + currentRadius,
                    bottom = cy + currentRadius
                )
            )
        }
        return Outline.Generic(path)
    }
}

/**
 * 附件菜单网格内容 (两行三列)
 */
@Composable
fun AttachmentMenuGrid() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MenuItem(Icons.Default.Description, "文档", Color(0xFF5C6BC0))
            MenuItem(Icons.Default.CameraAlt, "相机", Color(0xFFE91E63))
            MenuItem(Icons.Default.Image, "图库", Color(0xFF9C27B0))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MenuItem(Icons.Default.Headphones, "音频", Color(0xFFF44336))
            MenuItem(Icons.Default.LocationOn, "位置", Color(0xFF4CAF50))
            MenuItem(Icons.Default.Person, "联系人", Color(0xFF2196F3))
        }
    }
}

@Composable
fun MenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color, RoundedCornerShape(27.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}