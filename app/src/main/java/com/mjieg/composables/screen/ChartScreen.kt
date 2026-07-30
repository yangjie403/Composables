package com.mjieg.composables.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SmoothSlidingChart(
    data: List<Float>, // 推荐在实际项目中改用 FloatArray 以彻底杜绝装箱开销
    modifier: Modifier = Modifier,
    updateIntervalMillis: Int = 100,
    lineColor: Color = Color(0xFF00BCD4),
    fillColor: Color = Color(0xFF00BCD4).copy(alpha = 0.2f)
) {
    // 拷贝数据快照
    val currentSnapshot = remember(data) { data.toList() }

    var previousData by remember { mutableStateOf(currentSnapshot) }
    var currentData by remember { mutableStateOf(currentSnapshot) }

    val transitionProgress = remember { Animatable(1f) }

    // 1. 预先分配并在内存中复用 Path 对象（极大降低GC）
    val strokePath = remember { Path() }
    val fillPath = remember { Path() }

    // 2. 预先分配颜色列表，防止每帧都在 Brush 中创建 List
    val brushColors = remember(fillColor) { listOf(fillColor, Color.Transparent) }

    LaunchedEffect(currentSnapshot) {
        if (currentSnapshot != currentData) {
            previousData = currentData
            currentData = currentSnapshot

            transitionProgress.snapTo(0f)
            transitionProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = updateIntervalMillis, easing = LinearEasing)
            )
        }
    }

    Canvas(modifier = modifier) {
        if (currentData.size < 2) return@Canvas

        // Compose 优势：读取 State 会将该 Lambda 标记为只在 Draw 阶段重绘
        // 不会触发外层组件的 Recomposition (重组)
        val progress = transitionProgress.value

        val width = size.width
        val height = size.height
        val paddingY = 20.dp.toPx()
        val availableHeight = height - paddingY * 2

        val dx = width / (currentData.size - 1).coerceAtLeast(1).toFloat()

        val oldMax = previousData.maxOrNull()?.coerceAtLeast(0f) ?: 0f
        val newMax = currentData.maxOrNull()?.coerceAtLeast(0f) ?: 0f
        val dynamicMax = oldMax + (newMax - oldMax) * progress
        val renderMax = dynamicMax.coerceAtLeast(0.1f)

        // 3. 复用 Path，先清空之前帧的数据
        strokePath.reset()
        fillPath.reset()

        // 4. 不再使用 buildList 创建 Offset 对象，直接在循环中计算并绘制
        var prevX = 0f
        var prevY = 0f

        for (i in 0..currentData.size) {
            val value = if (i < previousData.size) previousData[i] else currentData.last()

            val x = (i - progress) * dx
            val y = height - paddingY - (value / renderMax) * availableHeight

            if (i == 0) {
                strokePath.moveTo(x, y)
            } else {
                val cpX = prevX + (x - prevX) / 2f
                // 贝塞尔曲线控制点直接计算并填入，0 对象分配
                strokePath.cubicTo(cpX, prevY, cpX, y, x, y)
            }

            prevX = x
            prevY = y
        }

        // 拼接填充路径
        fillPath.addPath(strokePath)
        fillPath.lineTo(prevX, height)
        // 回到原点的 X 坐标 (注意这里不能写死0，因为整体左移了 progress)
        fillPath.lineTo(-progress * dx, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = brushColors, // 使用复用的 List
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun ChartDemoScreen() {
    val dataSize = 60
    val updateInterval = 1000 // 刷新间隔

    // 1. 初始为 60 个 0 的列表
    var chartData by remember { mutableStateOf(List(dataSize) { 0f }) }
    var timePhase by remember { mutableStateOf(0f) }

    // 2. 模拟真实数据流入，不断把新数据添加到最右侧，并移除最左侧
    LaunchedEffect(Unit) {
        while (true) {
            delay(updateInterval.toLong())
            timePhase += 0.1f

            // 制造一个带有波动的数据源，并在有 3% 的概率产生一个异常峰值(测试 Y轴缩放)
            val wave = (sin(timePhase) + 1f) * 20f
            val spike = if (Random.nextFloat() > 0.97f) Random.nextFloat() * 100f else 0f
            val newElement = wave + spike + Random.nextFloat() * 5f // 加入一点噪音

            // 移除最左边(第一个)，在最右边追加新数据
            chartData = chartData.drop(1) + newElement
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "实时平滑折线图 (左移 + 动态Y轴)", color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        // 3. 调用组件
        SmoothSlidingChart(
            data = chartData,
            updateIntervalMillis = updateInterval, // 保持与外层数据刷新频率一致，确保无缝衔接
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

class ChartRingBuffer(val capacity: Int = 300) {
    // 纯基础类型数组，0 对象分配，初始全 0
    private val data = FloatArray(capacity) { 0f }
    private var head = 0

    // 用于触发 Compose Canvas 重绘的状态标记
    var updateCount by mutableLongStateOf(0L)
        private set

    /** 追加新数据：覆盖最旧的数据，后移指针 */
    fun add(value: Float) {
        data[head] = value
        head = (head + 1) % capacity
        updateCount++ // 数值改变，触发 UI 刷新
    }

    /** 逻辑取值：0 是最旧的数据（最左侧），capacity - 1 是最新数据（最右侧） */
    operator fun get(index: Int): Float {
        return data[(head + index) % capacity]
    }

    /** 获取当前这 300 个点中的最大值，用于动态调整 Y 轴 */
    fun getMax(): Float {
        var m = data[0]
        for (i in 1 until capacity) {
            val v = data[i]
            if (v > m) m = v
        }
        return m
    }
}

@Composable
fun SimpleSlidingChart(
    buffer: ChartRingBuffer,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00BCD4),
    fillColor: Color = Color(0xFF00BCD4).copy(alpha = 0.2f)
) {
    // 预分配内存，拒绝绘制时的 GC 卡顿
    val strokePath = remember { Path() }
    val fillPath = remember { Path() }
    val brushColors = remember(fillColor) { listOf(fillColor, Color.Transparent) }

    Canvas(modifier = modifier) {
        // 【核心】读取该状态，当外部调用 buffer.add() 时，这里会被标记为 Dirty 并自动重绘
        buffer.updateCount

        val width = size.width
        val height = size.height
        val paddingY = 20.dp.toPx()
        val availableHeight = height - paddingY * 2

        // 计算相邻点之间的固定间距 (比如宽度是900px, 那么 dx 就是 3px)
        val dx = width / (buffer.capacity - 1).coerceAtLeast(1).toFloat()

        // 获取 Y 轴最大值，保证下限不为 0
        val dynamicMax = buffer.getMax().coerceAtLeast(0.1f)

        // 清空上一帧的路径
        strokePath.reset()
        fillPath.reset()

        var prevX = 0f
        var prevY = 0f

        // 遍历数组，计算位置并构建三阶贝塞尔平滑曲线
        for (i in 0 until buffer.capacity) {
            val value = buffer[i]

            // X 轴直接按索引乘间距，不需要任何偏移补偿
            val x = i * dx
            // Y 轴根据当前最大值动态等比计算
            val y = height - paddingY - (value / dynamicMax) * availableHeight

            if (i == 0) {
                strokePath.moveTo(x, y)
            } else {
                val cpX = prevX + (x - prevX) / 2f
                strokePath.cubicTo(cpX, prevY, cpX, y, x, y)
            }
            prevX = x
            prevY = y
        }

        // 闭合填充路径
        fillPath.addPath(strokePath)
        fillPath.lineTo(prevX, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        // 绘制渐变填充
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = brushColors,
                startY = 0f,
                endY = height
            )
        )

        // 绘制贝塞尔线条本身
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun UltimateChartDemo() {
    // 1. 初始化我们写好的底层环形缓冲区，全 0
    val chartBuffer = remember { ChartRingBuffer(capacity = 100) }
    val interval = 200L // 生成一次数据时间间隔

    // 2. 模拟传感器高速推送数据
    LaunchedEffect(Unit) {
        var phase = 0f
        while (true) {
            delay(interval)
            phase += 0.15f

            val wave = (sin(phase) + 1f) * 50f
            // 偶尔来个异常极峰，验证最大值自适应变化
            val spike = if (Random.nextFloat() > 0.95f) 180f else 0f

            chartBuffer.add(wave + spike + Random.nextFloat() * 5f)
        }
    }

    SimpleSlidingChart(
        buffer = chartBuffer,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}