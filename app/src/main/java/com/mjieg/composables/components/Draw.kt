package com.mjieg.composables.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjieg.composables.R

@Composable
fun DrawContentExample() {
    var pointerOffset by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput("dragging") {
                detectDragGestures { change, dragAmount ->
                    pointerOffset += dragAmount
                }
            }
            .onSizeChanged {
                pointerOffset = Offset(it.width / 2f, it.height / 2f)
            }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.radialGradient(
                        center = pointerOffset,
                        radius = 100.dp.toPx(),
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1f to Color.Black
                        )
                    )
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Hello Compose!",
            modifier = Modifier
                .drawWithCache {
                    val brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF9E82F0),
                            Color(0xFF42A5F5)
                        )
                    )
                    onDrawBehind {
                        drawRoundRect(
                            brush,
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                    }
                }
                .padding(10.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            modifier = Modifier
                .padding(10.dp)
                .drawBehind {
                    drawRoundRect(
                        Color(0xFFBBAAEE),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                }
                .padding(10.dp),
            text = "除了 Canvas 可组合项之外，Compose 还有几个实用的图形 Modifiers，可帮助绘制自定义内容。这些修饰符可以应用于任何可组合项，因而非常实用。"
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RectangleShape)
                    .border(2.dp, Color.Black)
                    .graphicsLayer {
                        clip = true
                        shape = CircleShape
                        translationY = 20.dp.toPx()
                    }
                    .background(Color(0xFFF06292))
            ) {
                Text(
                    "Hello Compose",
                    style = TextStyle(color = Color.Black, fontSize = 10.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(500.dp))
                    .background(Color(0xFF4DB6AC))
            )
        }
    }
}

@Composable
fun DistinctCompositingExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        // --- 场景 1：默认行为 (Auto) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("默认 (Auto): 重叠处变深了", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("每个圆分别按 0.5 透明度绘制，交集处叠加了两次", fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .graphicsLayer(
                        alpha = 0.5f,
                        // ModulateAlpha，子元素独立渲染
                        compositingStrategy = CompositingStrategy.ModulateAlpha
                    )
            ) {
                OverlappingCircles()
            }
        }

        // --- 场景 2：强制离屏 (Offscreen) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("离屏 (Offscreen): 颜色完全统一", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("先在缓存区画好整体，再统一设置 0.5 透明度", fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .graphicsLayer(
                        alpha = 0.5f,
                        // 【关键】强制开启离屏缓存，实现 Group Alpha 效果
                        compositingStrategy = CompositingStrategy.Offscreen
                    )
            ) {
                OverlappingCircles()
            }
        }
        Image(
            painter = painterResource(id = R.drawable.xx),
            contentDescription = "Dog",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .aspectRatio(1f)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFC5E1A5),
                            Color(0xFF80DEEA)
                        )
                    )
                )
                .padding(8.dp)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithCache {
                    val path = Path()
                    path.addOval(
                        Rect(
                            topLeft = Offset.Zero,
                            bottomRight = Offset(size.width, size.height)
                        )
                    )
                    onDrawWithContent {
                        clipPath(path) {
                            // this draws the actual image - if you don't call drawContent, it wont
                            // render anything
                            this@onDrawWithContent.drawContent()
                        }
                        val dotSize = size.width / 8f
                        // Clip a white border for the content
                        drawCircle(
                            Color.Transparent,
                            radius = dotSize,
                            center = Offset(
                                x = size.width - dotSize,
                                y = size.height - dotSize
                            ),
                            blendMode = BlendMode.Clear
                        )
                        // draw the red circle indication
                        drawCircle(
                            Color(0xFFEF5350), radius = dotSize * 0.8f,
                            center = Offset(
                                x = size.width - dotSize,
                                y = size.height - dotSize
                            )
                        )
                    }
                }
        )
    }
}

@Composable
fun OverlappingCircles() {
    // 两个互相重叠的蓝色圆圈
    Row {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Blue, CircleShape)
        )
        // 向左偏移，制造重叠区域
        Box(
            modifier = Modifier
                .offset(x = (-40).dp)
                .size(100.dp)
                .background(Color.Red, CircleShape)
        )
    }
}

@Composable
fun HolePunchExample() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text("我是底部的文字，通过洞可以看到我")

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen) // 必须开启
        ) {
            // 1. 先画一层半透明遮罩
            drawRect(color = Color.Black.copy(alpha = 0.7f))

            // 2. 使用 Clear 模式画一个圆，实现镂空
            drawCircle(
                color = Color.Transparent,
                radius = 200f,
                blendMode = BlendMode.Clear // 清除缓冲区内容
            )
        }
    }
}

// 使用CompositingStrategy.Offscreen时，系统会创建绘制区域大小的屏幕外纹理，
// 并将其渲染回屏幕上。默认情况下，使用此策略完成的所有绘制命令都会被裁剪至此区域。
@Composable
fun CompositingStrategyExamples() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Canvas(
            modifier = Modifier
                .graphicsLayer()
                .size(100.dp)
                .border(2.dp, color = Color.Blue)
        ) {
            drawRect(color = Color.Magenta, size = Size(200.dp.toPx(), 200.dp.toPx()))
        }
        Spacer(modifier = Modifier
            .size(300.dp)
            .border(1.dp, color = Color.Red))
        Canvas(
            modifier = Modifier
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .size(100.dp)
                .border(2.dp, color = Color.Blue)
        ) {
            drawRect(color = Color.Red, size = Size(200.dp.toPx(), 200.dp.toPx()))
        }
    }
}

// 自定义绘制修饰符
class FlippedModifier : DrawModifier {
    override fun ContentDrawScope.draw() {
        scale(1f, -1f) {
            this@draw.drawContent()
        }
    }
}

fun Modifier.flipped() = this.then(FlippedModifier())

@Composable
fun FlippedModifierExample() {
    Box(
        modifier = Modifier
            .flipped()
    ) {
        Text("我翻转了")
    }
}