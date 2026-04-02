package com.mjieg.composables.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjieg.composables.R
import androidx.core.graphics.scale

@Composable
fun LinearGradientExample() {
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFF5576C), Color(0xFFF093FB)),
        // 可选：指定起点和终点
        // start = Offset(0f, 0f),
        // end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(brush)
    )
}

// 垂直和水平渐变
@Composable
fun SimpleDirectionalGradients() {
    Column {
        // 垂直渐变
        Box(
            modifier = Modifier
                .size(80.dp, 80.dp)
                .background(Brush.verticalGradient(listOf(Color.Blue, Color.Cyan)))
        )
        Spacer(Modifier.size(10.dp))
        // 水平渐变
        Box(
            modifier = Modifier
                .size(80.dp, 80.dp)
                .background(Brush.horizontalGradient(listOf(Color.Red, Color.Yellow)))
        )
    }
}

// 径向渐变
@Composable
fun RadialGradientExample() {
    val brush = Brush.radialGradient(
        colors = listOf(Color.Yellow, Color.Transparent),
        center = Offset.Unspecified, // 默认居中
        radius = with(LocalDensity.current) { 40.dp.toPx() }
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(brush, shape = CircleShape)
    )
}

// 扫描渐变
@Composable
fun SweepGradientExample() {
    val brush = Brush.sweepGradient(
        colors = listOf(
            Color.Cyan,
            Color.Magenta,
            Color.Yellow,
            Color.Cyan // 建议首尾颜色一致，避免明显的分割线
        )
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(brush, shape = CircleShape)
    )
}

// 自定义色标位置
@Composable
fun CustomStopsGradient() {
    val brush = Brush.horizontalGradient(
        0.0f to Color.Red,
        0.2f to Color.Red,   // 前20% 纯红
        0.5f to Color.White, // 到50% 处变成白色
        1.0f to Color.Blue   // 最后是蓝色
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(brush)
    )
}

// 渐变文字
@Composable
fun GradientText() {
    Text(
        text = "Hello Compose Gradient",
        style = TextStyle(
            brush = Brush.linearGradient(
                colors = listOf(Color.Cyan, Color.Blue, Color.Magenta)
            ),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

// 不同 TileMode 的对比
@Composable
fun TileModeComparison() {
    val colors = listOf(Color(0xFF4285F4), Color(0xFFEA4335))
    // 定义一个很小的渐变区域，以便观察平铺效果
    val gradientSize = 100f

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Clamp (默认值)：拉伸边缘颜色
        Text("TileMode.Clamp (边缘拉伸)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, 0f),
                        end = Offset(gradientSize, gradientSize),
                        tileMode = TileMode.Clamp
                    )
                )
        )

        // 2. Repeated：重复渐变图案
        Text("TileMode.Repeated (重复)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, 0f),
                        end = Offset(gradientSize, gradientSize),
                        tileMode = TileMode.Repeated
                    )
                )
        )

        // 3. Mirror：镜像翻转重复
        Text("TileMode.Mirror (镜像)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, 0f),
                        end = Offset(gradientSize, gradientSize),
                        tileMode = TileMode.Mirror
                    )
                )
        )

        // 4. Decal：仅渲染定义的区域，其余部分透明（常用于贴图）
        // 注意：Decal 在某些低版本 Android 上可能表现为 Clamp
        Text("TileMode.Decal (贴纸/透明边缘)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.LightGray) // 加个底色以便看清 Decal
                .background(
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, 0f),
                        end = Offset(gradientSize, gradientSize),
                        tileMode = TileMode.Decal
                    )
                )
        )
    }
}

@Composable
fun StripedBackground() {
    val stripeWidth = 40f
    val brush = Brush.linearGradient(
        0.0f to Color.White,
        0.5f to Color.White,
        0.5f to Color(0xFFEEEEEE), // 0.5 位置颜色突变，形成硬边
        1.0f to Color(0xFFEEEEEE),
        start = Offset(0f, 0f),
        end = Offset(stripeWidth, stripeWidth),
        tileMode = TileMode.Repeated
    )

    Box(modifier = Modifier
        .size(100.dp)
        .background(brush))
}

@Composable
fun PulsatingRadialGradient() {
    val brush = Brush.radialGradient(
        colors = listOf(Color.Yellow, Color.Red),
        center = Offset.Unspecified,
        radius = 50f, // 半径很小
        tileMode = TileMode.Mirror
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(brush)
    )
}

@Composable
fun NonFixedBrushSize() {
    val listColors = listOf(Color.Yellow, Color.Red, Color.Blue)
    val customBrush = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    colors = listColors,
                    from = Offset.Zero,
                    to = Offset(size.width / 3, 0f),
                    tileMode = TileMode.Mirror
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .requiredSize(100.dp)
            .background(brush = customBrush)
    )
}

@Composable
fun ImageBrushExample() {
    val imageBrush =
        ShaderBrush(ImageShader(ImageBitmap.imageResource(R.drawable.xx)))

    val imageBitmap = ImageBitmap.imageResource(R.drawable.xx)

    val scaledImageBrush = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val scale = size.width / imageBitmap.width
                val scaledBitmap = imageBitmap.asAndroidBitmap().scale(
                    (imageBitmap.width * scale).toInt(),
                    (imageBitmap.height * scale).toInt()
                )
                return ImageShader(
                    image = scaledBitmap.asImageBitmap(),
                    tileModeX = TileMode.Clamp,
                    tileModeY = TileMode.Clamp,
                )
            }
        }
    }
    Column {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(imageBrush)
        )
        Text(
            text = "ImageBrush",
            style = TextStyle(
                brush = imageBrush,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp
            )
        )
        Canvas(
            modifier = Modifier.size(100.dp)
        ) {
            drawCircle(scaledImageBrush)
        }
    }
}

@Preview
@Composable
fun GradientExample() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearGradientExample()
        SimpleDirectionalGradients()
        RadialGradientExample()
        SweepGradientExample()
        CustomStopsGradient()
        GradientText()
        TileModeComparison()
        StripedBackground()
        PulsatingRadialGradient()
        NonFixedBrushSize()
        ImageBrushExample()
    }
}