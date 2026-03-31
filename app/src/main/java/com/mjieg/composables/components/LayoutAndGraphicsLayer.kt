package com.mjieg.composables.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun JellyButton() {
    var pressed by remember { mutableStateOf(false) }
    // 动画缩放比例
    val scale by animateFloatAsState(if (pressed) 0.8f else 1f)

    Button(
        onClick = { /* ... */ },
        modifier = Modifier
            // 1. 监听触摸，改变状态
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) {
                            pressed = true
                        } else {
                            pressed = false
                        }
                    }
                }
            }
            .graphicsLayer {
                shape = CircleShape
                clip = true
            }
            // 2. 核心：使用 graphicsLayer 进行缩放
            // 这样按钮视觉上变小了，但它在父容器中占据的 Layout 空间依然是原始大小
            // 周围的组件不会因为这个动画而重新排版。
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = CircleShape
                clip = true
                // 可以加阴影变化
                shadowElevation = if (pressed) 2.dp.toPx() else 8.dp.toPx()
            }
    ) {
        Text("按住我")
    }
}

fun Modifier.ignoreParentPadding(
    horizontalPadding: Dp = 0.dp,
    verticalPadding: Dp = 0.dp
): Modifier =
    layout { measurable, constraints ->
        // 将 dp 转为 px
        val paddingPxH = horizontalPadding.roundToPx()
        val paddingPxV = verticalPadding.roundToPx()

        // 修改约束：允许子组件比父组件给的宽度更宽（加上两侧 padding）
        val newWidth = constraints.maxWidth + paddingPxH * 2
        val newHeight = constraints.maxHeight + paddingPxV * 2
        val newConstraints = constraints.copy(
            minWidth = newWidth,
            maxWidth = newWidth,
            minHeight = newHeight,
            maxHeight = newHeight
        )

        val placeable = measurable.measure(newConstraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

@Composable
fun FullWidthBanner() {
    Box(Modifier.width(200.dp)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Gray)
        ) {
            Text("Normal Text")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .ignoreParentPadding(16.dp)
                    .background(Color.Red)
            )

            Text("Normal Text")
        }
    }

}

@Composable
fun FlipCard(
    isFlipped: Boolean,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    // 动画状态
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(1000)
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                // 1. 设置 Y 轴旋转
                rotationY = rotation
                // 2. 设置相机距离：值越小，透视效果越强（类似于广角镜头）。
                // 通常设置为 density * 8f 左右
                cameraDistance = 8 * density
            }
    ) {
        if (rotation <= 90f) {
            // 正面
            Box(Modifier.fillMaxSize()) { front() }
        } else {
            // 背面
            // 关键点：当翻转超过 90 度显示背面时，
            // 背面原本是镜像的，需要再次水平翻转回来，或者在 graphicsLayer 里处理
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 修正背面的镜像问题，让文字显示正常
                        rotationY = 180f
                    }
            ) { back() }
        }
    }
}

@Composable
fun FlipCardExample() {
    var isFlipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Flip Card Example", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(48.dp))

        // 可点击的翻转卡片
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(280.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isFlipped = !isFlipped
                }
        ) {
            FlipCard(
                isFlipped = isFlipped,
                front = {
                    // 卡片正面：蓝色背景，显示 "Front"
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Blue
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Front",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                        }
                    }
                },
                back = {
                    // 卡片背面：绿色背景，显示 "Back"
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Green
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Tap the card to flip it!",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FoldingCardDemo() {
    // 状态控制：是否折叠
    var isFolded by remember { mutableStateOf(false) }

    // 动画插值：控制旋转角度
    // 目标是 -179f 而不是 -180f，是为了避免某些渲染引擎在完全重合时的闪烁，
    // 同时 -180f 代表向屏幕内部（后方）旋转。
    val animatedRotationY by animateFloatAsState(
        targetValue = if (isFolded) -179f else 0f,
        animationSpec = tween(durationMillis = 1000), // 动画时长 1秒
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 卡片容器
        Row(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(0.9f) // 占屏幕宽度的 90%
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isFolded = !isFolded } // 点击触发
        ) {
            // ============ 左半部分 (会动的) ============
            Box(
                modifier = Modifier
                    .weight(1f) // 占一半宽度
                    .graphicsLayer {
                        // 1. 设置透视距离，数值越大透视越弱，通常设为密度的 12-16倍
                        cameraDistance = 12f * density

                        // 2. 设置旋转轴心：X轴的最右侧 (1f)，Y轴的中心 (0.5f)
                        transformOrigin =
                            TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)

                        // 3. 应用旋转角度
                        rotationY = animatedRotationY
                    }
                    // 裁剪左边圆角，右边直角
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFF6200EE))
            ) {
                // 这里放置左侧内容
                CardContent(text = "Left Side", isLeft = true)

                // 可选：添加阴影层，随着折叠角度变黑，增加真实感
                val dimming = (animatedRotationY / -180f).coerceIn(0f, 0.6f)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimming))
                )
            }

            // ============ 右半部分 (不动的) ============
            Box(
                modifier = Modifier
                    .weight(1f) // 占另一半宽度
                    // 裁剪右边圆角，左边直角
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .background(Color(0xFF3700B3))
            ) {
                // 这里放置右侧内容
                CardContent(text = "Right Side", isLeft = false)
            }
        }
    }
}

@Composable
fun CardContent(text: String, isLeft: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = if (isLeft)
                            listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
                        else
                            listOf(Color(0xFF92FE9D), Color(0xFF00C9FF))
                    )
                )
        )
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SplitTextFoldingCardFixed() {
    var isFolded by remember { mutableStateOf(false) }

    val animatedRotationY by animateFloatAsState(
        targetValue = if (isFolded) -91f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(300.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isFolded = !isFolded }
        ) {
            // 获取精确的测量值
            val totalWidth = maxWidth
            val halfWidth = maxWidth / 2

            Row(modifier = Modifier.fillMaxSize()) {
                // ================= 左半部分 =================
                Box(
                    modifier = Modifier
                        .width(halfWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            cameraDistance = 12f * density
                            transformOrigin = TransformOrigin(1f, 0.5f)
                            rotationY = animatedRotationY
                        }
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                    // 关键点1：必须设置为 Start 对齐，确保内容从左边缘开始绘制
                    contentAlignment = Alignment.TopStart
                ) {
                    SharedCardContent(
                        width = totalWidth,
                        text = LONG_TEXT_CONTENT
                    )

                    // 阴影层
                    val dimming = (animatedRotationY / -180f).coerceIn(0f, 0.6f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = dimming))
                    )
                }

                // ================= 右半部分 =================
                Box(
                    modifier = Modifier
                        .width(halfWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
                    // 关键点2：同样设置为 Start 对齐，作为坐标参考系
                    contentAlignment = Alignment.TopStart
                ) {
                    // 关键点3：这里包裹一层 Box 进行偏移
                    Box(
                        modifier = Modifier.offset(x = -halfWidth)
                    ) {
                        SharedCardContent(
                            width = totalWidth,
                            text = LONG_TEXT_CONTENT
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SharedCardContent(width: Dp, text: String) {
    Box(
        modifier = Modifier
            // 包裹内容大小，忽略父容器的宽度指定，根据内容实际宽度来决定自身的宽度
            .wrapContentWidth(align = Alignment.Start, unbounded = true)
            .requiredWidth(width)
            .fillMaxHeight()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "折叠卡片 Title",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}

const val LONG_TEXT_CONTENT =
    "这是一个能够产生视差错觉的折叠卡片效果。所有的内容本质上是一个完整的布局，但在技术实现上，我们将它渲染了两次。\n" +
            "通过使用 requiredWidth，我们强制让右侧被压缩的容器渲染出完整宽度的内容，再通过 Offset 移动它，从而完美拼接。"

@Composable
fun PolyToPolyFoldingCard() {
    var folded by remember { mutableStateOf(false) }

    // 动画进度：0f (展开) -> 1f (完全折叠到后面)
    val animatedProgress by animateFloatAsState(
        targetValue = if (folded) 0.51f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "foldingProgress"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(300.dp, 200.dp)
                .clickable { folded = !folded }
                .drawWithContent {
                    val w = size.width
                    val h = size.height
                    val halfW = w / 2f

                    // --- 1. 计算点到点的映射矩阵 ---
                    val nativeMatrix = android.graphics.Matrix()

                    // 源点：左半部分的矩形 [左上, 右上, 右下, 左下]
                    val src = floatArrayOf(
                        0f, 0f,         // LT
                        halfW, 0f,      // RT (中轴)
                        halfW, h,       // RB (中轴)
                        0f, h           // LB
                    )

                    /**
                     * 目标点计算逻辑：
                     * 当 progress = 0.5 时，左边缘重合在中轴线（侧立状态）
                     * 当 progress = 1.0 时，左边缘运动到最右侧（折叠到后面）
                     */
                    val progress = animatedProgress
                    // 左边缘的 X 坐标从 0 运动到 W
                    val edgeX = progress * w
                    // 透视导致的缩放：在中间时(0.5)收缩最厉害，两头(0, 1)不收缩
                    val perspectiveFactor = (1f - abs(0.5f - progress) * 2f) * 0.15f
                    val topY = h * perspectiveFactor
                    val bottomY = h * (1f - perspectiveFactor)

                    val dst = floatArrayOf(
                        edgeX, topY,    // LT 映射后的位置
                        halfW, 0f,      // RT 始终固定在中轴线上边
                        halfW, h,       // RB 始终固定在中轴线下边
                        edgeX, bottomY  // LB 映射后的位置
                    )

                    // 将 4 个点的映射关系写入矩阵
                    nativeMatrix.setPolyToPoly(src, 0, dst, 0, 4)

                    // --- 2. 绘制过程 ---

                    // A. 绘制左半部分 (使用矩阵变换)
                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.concat(nativeMatrix)
                    // 只绘制左侧原始内容
                    clipRect(left = 0f, top = 0f, right = halfW, bottom = h) {
                        this@drawWithContent.drawContent()
                        // 叠加阴影：折叠过程中变暗
                        drawRect(Color.Black, alpha = progress * 0.4f)
                    }
                    drawContext.canvas.nativeCanvas.restore()

                    // B. 绘制右半部分 (静止，盖在最上面)
                    clipRect(left = halfW, top = 0f, right = w, bottom = h) {
                        this@drawWithContent.drawContent()
                    }
                }
                .background(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF6200EE), Color(0xFF03DAC5))),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 卡片内容
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "POLY-TO-POLY",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Folding Matrix", color = Color.White.copy(0.7f))
            }
        }
    }
}

@Composable
fun ComposeToBitmapExample() {
    // 1. 创建一个 graphicsLayer 用于捕获内容
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scale = 3f
    val sizePx = with(LocalDensity.current) { 100.dp.toPx() }
    val targetSize = IntSize((sizePx * scale).toInt(), (sizePx * scale).toInt())
    Column(
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2. 这是你想要转换成图片的内容区域
        Box(
            modifier = Modifier
                .size(100.dp)
                .drawWithContent {
                    // 绘制内容到屏幕上
                    drawContent()
                    // 【关键点】将内容记录到 graphicsLayer 中
                    // 缩放处理
                    graphicsLayer.record(size = targetSize) {
                        scale(scale, pivot = Offset.Zero) {
                            this@drawWithContent.drawContent()
                        }
                    }
                    // 将记录的内容（缩放后的）绘制到屏幕上
                    // drawLayer(graphicsLayer)
                }
                .background(Color.Blue)
        ) {
            Text(
                text = "Hello Bitmap!",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 触发转换的按钮
        Button(onClick = {
            coroutineScope.launch {
                // 将记录的layer（缩放后的）转换为ImageBitmap，再转为Android Bitmap
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                capturedBitmap = bitmap
            }
        }) {
            Text("点击生成 Bitmap")
        }

        // 4. 显示生成的预览图（仅用于测试）
        capturedBitmap?.let {
            Spacer(modifier = Modifier.height(20.dp))
            Text("预览生成的图片：")
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.LightGray)
            )
        }
    }
}