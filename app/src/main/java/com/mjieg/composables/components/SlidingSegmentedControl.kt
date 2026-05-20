package com.mjieg.composables.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SlidingSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 外部容器的背景色（浅灰色/白色）
    val backgroundColor = Color(0xFFF5F5F5)
    // 选中滑块的背景色（灰色）
    val indicatorColor = Color(0xFFD8D8D8)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape) // 胶囊形状
            .background(backgroundColor)
            .padding(4.dp) // 内部留白，让滑块不紧贴边缘
    ) {
        val maxWidth = maxWidth
        val itemCount = options.size
        // 计算每个选项的宽度
        val itemWidth = maxWidth / itemCount

        // 计算滑块的偏移量
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "indicatorOffset"
        )

        // 1. 背景滑动块
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(x = indicatorOffset.toPx().toInt(), y = 0)
                }
                .width(itemWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(indicatorColor)
        )

        // 2. 顶层选项文字
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            // 移除默认的水波纹效果，让滑动感更纯粹
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectionChange(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedIndex == index) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EqualRemainingSpaceRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val childCount = measurables.size
        if (childCount == 0) {
            return@Layout layout(constraints.minWidth, constraints.minHeight) {}
        }

        // 1. 获取每个子组件自身内容需要的宽度 (Intrinsic Width)
        val intrinsicWidths = measurables.map { it.maxIntrinsicWidth(constraints.maxHeight) }
        val totalIntrinsicWidth = intrinsicWidths.sum()

        // 2. 计算剩余空间
        // 确保如果有外层约束限制了最大宽度才进行计算，否则没有剩余空间
        val maxWidth =
            if (constraints.hasBoundedWidth) constraints.maxWidth else totalIntrinsicWidth
        val remainingWidth = (maxWidth - totalIntrinsicWidth).coerceAtLeast(0)

        // 3. 将剩余空间均分给每个组件
        val extraWidthPerChild = remainingWidth / childCount

        // 4. 使用 (自身所需宽度 + 均分的剩余空间) 作为固定宽度，去测量每个子组件
        val placeables = measurables.mapIndexed { index, measurable ->
            val exactWidth = intrinsicWidths[index] + extraWidthPerChild
            measurable.measure(
                // 强制子组件的宽度必须是 exactWidth
                constraints.copy(
                    minWidth = exactWidth,
                    maxWidth = exactWidth
                )
            )
        }

        // 5. 依次横向排列这些组件
        val maxHeight = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
        layout(maxWidth, maxHeight) {
            var xPosition = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = xPosition, y = 0)
                xPosition += placeable.width // 下一个组件紧挨着上一个
            }
        }
    }
}

@Composable
fun DynamicSlidingSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    val density = LocalDensity.current

    // 用来记录每个选项的宽度和相对于父容器的 X 偏移
    // Key 是索引，Value 是 Pair(宽度, 偏移量)
    val itemLayouts = remember { mutableStateMapOf<Int, Pair<Dp, Dp>>() }

    // 获取当前选中项的布局信息，如果没有测量好则默认为 0
    val currentLayout = itemLayouts[selectedIndex] ?: Pair(0.dp, 0.dp)

    // 对滑块的宽度进行动画处理
    val animatedWidth by animateDpAsState(
        targetValue = currentLayout.first,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "width"
    )

    // 对滑块的偏移量进行动画处理
    val animatedOffset by animateDpAsState(
        targetValue = currentLayout.second,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFF2F2F2)) // 浅灰背景
            .padding(4.dp)
            // 使用绘制方式实现底层滑块
            .drawBehind {
                // 🚀 纯 Draw 阶段读取状态，0 重组，0 重新布局！
                val width = animatedWidth.toPx()
                val offset = animatedOffset.toPx()

                if (width > 0f) {
                    drawRoundRect(
                        color = Color(0xFFD8D8D8),
                        topLeft = Offset(x = offset, y = 0f),
                        size = Size(width = width, height = size.height),
                        // 圆角半径设置为高度的一半，就是胶囊形状 (CircleShape)
                        cornerRadius = CornerRadius(x = size.height / 2, y = size.height / 2)
                    )
                }
            }
    ) {
        // 1. 底层滑块 (Indicator)
        // 只有当宽度大于0（即测量完成后）才显示，避免初始位置跳变
        // if (animatedWidth > 0.dp) {
        //     Box(
        //         modifier = Modifier
        //             .offset {
        //                 IntOffset(x = animatedOffset.roundToPx(), y = 0)
        //             }
        //             .layout { measurable, constraints ->
        //                 // 在这里（Layout阶段）读取 animatedWidth，跳过重组
        //                 val widthPx = animatedWidth.roundToPx()
        //                 val placeable = measurable.measure(
        //                     constraints.copy(minWidth = widthPx, maxWidth = widthPx)
        //                 )
        //                 layout(placeable.width, placeable.height) {
        //                     placeable.placeRelative(0, 0)
        //                 }
        //             }
        //             .fillMaxHeight()
        //             .clip(CircleShape)
        //             .background(Color(0xFFD8D8D8)) // 选中态深灰背景
        //     )
        // }

        // 2. 表层选项
        EqualRemainingSpaceRow(
            modifier = Modifier.fillMaxSize()
        ) {
            options.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        // 核心：测量每个选项实际占用的位置
                        .onGloballyPositioned { coordinates ->
                            val width = with(density) { coordinates.size.width.toDp() }
                            val x = with(density) { coordinates.positionInParent().x.toDp() }
                            itemLayouts[index] = Pair(width, x)
                        }
                        .fillMaxHeight()
                        // 增加左右内边距，让点击区域和滑块看起来比文字宽一点
                        .padding(horizontal = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectionChange(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        color = if (selectedIndex == index) Color.Black else Color(0xFF666666),
                        fontWeight = if (selectedIndex == index) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableDynamicSlidingSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 记录每个选项的 宽度 和 X偏移（直接使用 Float / Px 以避免性能开销和精度丢失）
    val itemLayouts = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

    // 滑块的动画状态
    val indicatorOffset = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }

    // 拖拽相关状态
    var isDragging by remember { mutableStateOf(false) }
    var isInitialMeasure by remember { mutableStateOf(true) }

    // 监听 selectedIndex 的变化，执行动画 (仅在非拖拽状态下)
    LaunchedEffect(selectedIndex, itemLayouts.size) {
        if (!isDragging && itemLayouts.size == options.size) {
            val target = itemLayouts[selectedIndex]
            if (target != null) {
                if (isInitialMeasure) {
                    // 第一次测量完毕，瞬间对齐，防止动画从 0 飞过来
                    indicatorOffset.snapTo(target.second)
                    indicatorWidth.snapTo(target.first)
                    isInitialMeasure = false
                } else {
                    launch {
                        indicatorOffset.animateTo(
                            target.second,
                            spring(stiffness = Spring.StiffnessLow)
                        )
                    }
                    launch {
                        indicatorWidth.animateTo(
                            target.first,
                            spring(stiffness = Spring.StiffnessLow)
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFF2F2F2)) // 浅灰背景
            .padding(4.dp)
            // 手势监听
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val currentX = indicatorOffset.value
                        val currentW = indicatorWidth.value
                        // 判定手指是否按在当前滑块上（左右各扩展了 40px 作为容错区域）
                        if (offset.x in (currentX - 40f)..(currentX + currentW + 40f)) {
                            isDragging = true
                        }
                    },
                    onDragEnd = {
                        if (isDragging) {
                            isDragging = false
                            // 找到距离当前松手位置最近的选项
                            val targetIndex = findClosestIndex(indicatorOffset.value, itemLayouts)
                            onSelectionChange(targetIndex)

                            // 触发回弹动画（如果松手后 index 没变，外部不更新，需要内部手动吸附）
                            val target = itemLayouts[targetIndex]
                            if (target != null) {
                                coroutineScope.launch {
                                    launch {
                                        indicatorOffset.animateTo(
                                            target.second,
                                            spring(stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                    launch {
                                        indicatorWidth.animateTo(
                                            target.first,
                                            spring(stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        if (isDragging) {
                            isDragging = false
                            val target = itemLayouts[selectedIndex]
                            if (target != null) {
                                coroutineScope.launch {
                                    launch {
                                        indicatorOffset.animateTo(
                                            target.second,
                                            spring(stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                    launch {
                                        indicatorWidth.animateTo(
                                            target.first,
                                            spring(stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (isDragging) {
                            change.consume()
                            coroutineScope.launch {
                                val minOffset = itemLayouts[0]?.second ?: 0f
                                val maxOffset = itemLayouts[options.lastIndex]?.second ?: 0f

                                // 判断是否越界
                                val isOutOfBounds =
                                    indicatorOffset.value < minOffset || indicatorOffset.value > maxOffset

                                // 阻尼感核心：
                                // 正常拖动：轻微阻尼 (0.85f)，感觉滑块比较实沉
                                // 越界拖动：强阻尼 (0.25f)，呈现"橡皮筋"拉扯效果
                                val damping = if (isOutOfBounds) 0.25f else 0.85f
                                val newOffset = indicatorOffset.value + (dragAmount * damping)

                                indicatorOffset.snapTo(newOffset)

                                // 动态计算拖拽时的滑块宽度（当选项宽度不同时，平滑过渡宽度）
                                val newWidth =
                                    calculateInterpolatedWidth(newOffset, itemLayouts, options.size)
                                if (newWidth != null) {
                                    indicatorWidth.snapTo(newWidth)
                                }
                            }
                        }
                    }
                )
            }
            .drawBehind {
                val width = indicatorWidth.value
                val offset = indicatorOffset.value

                if (width > 0f) {
                    drawRoundRect(
                        color = Color(0xFFD8D8D8),
                        topLeft = Offset(x = offset, y = 0f),
                        size = Size(width = width, height = size.height),
                        cornerRadius = CornerRadius(x = size.height / 2, y = size.height / 2)
                    )
                }
            }
    ) {
        // 2. 表层选项
        EqualRemainingSpaceRow(
            modifier = Modifier.fillMaxSize()
        ) {
            options.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            // 直接保存 px 值
                            val width = coordinates.size.width.toFloat()
                            val x = coordinates.positionInParent().x
                            itemLayouts[index] = Pair(width, x)
                        }
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectionChange(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        color = if (selectedIndex == index) Color.Black else Color(0xFF666666),
                        fontWeight = if (selectedIndex == index) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ========= 辅助计算函数 =========

/**
 * 寻找当前偏移量离得最近的 Index
 */
private fun findClosestIndex(currentX: Float, itemLayouts: Map<Int, Pair<Float, Float>>): Int {
    var closestIndex = 0
    var minDistance = Float.MAX_VALUE
    for ((index, layout) in itemLayouts) {
        val distance = abs(layout.second - currentX)
        if (distance < minDistance) {
            minDistance = distance
            closestIndex = index
        }
    }
    return closestIndex
}

/**
 * 如果各个 Tab 的宽度不一样长，拖动时平滑过渡宽度
 */
private fun calculateInterpolatedWidth(
    currentX: Float,
    itemLayouts: Map<Int, Pair<Float, Float>>,
    size: Int
): Float? {
    if (itemLayouts.size < size) return null

    val minLayout = itemLayouts[0]!!
    val maxLayout = itemLayouts[size - 1]!!

    // 越界时，保持最小/最大宽度
    if (currentX <= minLayout.second) return minLayout.first
    if (currentX >= maxLayout.second) return maxLayout.first

    // 计算当前处于哪两个选项之间
    for (i in 0 until size - 1) {
        val left = itemLayouts[i]!!
        val right = itemLayouts[i + 1]!!
        if (currentX >= left.second && currentX <= right.second) {
            val fraction = (currentX - left.second) / (right.second - left.second)
            return left.first + (right.first - left.first) * fraction
        }
    }
    return null
}

@Composable
fun IOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 8.dp
    val thumbCornerRadius = 6.dp
    val backgroundColor = Color(0xFFE5E5EA)
    val thumbColor = Color.White
    val selectedTextColor = Color.Black
    val unselectedTextColor = Color(0xFF8E8E93)

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // 使用 rememberUpdatedState 保证在手势侦听器中始终能拿到最新的回调和状态，而不会导致侦听器重启
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnOptionSelected by rememberUpdatedState(onOptionSelected)

    BoxWithConstraints(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(2.dp)
    ) {
        val segmentWidthDp = maxWidth / options.size
        val segmentWidthPx = with(density) { segmentWidthDp.toPx() }
        val maxOffsetX = segmentWidthPx * (options.size - 1)

        // 维护滑块实际的像素偏移量 (使用 Animatable 实现动画和平滑接管)
        val thumbOffsetX = remember(segmentWidthPx) {
            Animatable(currentSelectedIndex * segmentWidthPx)
        }

        var isDragging by remember { mutableStateOf(false) }
        var dragGrabOffset by remember { mutableFloatStateOf(0f) }

        // 非拖拽状态下，滑块自动执行弹簧动画，吸附到选中的索引位置
        LaunchedEffect(currentSelectedIndex, isDragging, segmentWidthPx) {
            if (!isDragging) {
                thumbOffsetX.animateTo(
                    targetValue = currentSelectedIndex * segmentWidthPx,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }

        // 核心手势处理：合并点击和横向拖拽
        val gestureModifier = Modifier
            .pointerInput(options.size, segmentWidthPx) {
                detectTapGestures(
                    onPress = { offset ->
                        // 手指按下的瞬间 (onPress) 就立即触发选中事件
                        val tappedIndex =
                            (offset.x / segmentWidthPx).toInt().coerceIn(0, options.size - 1)
                        currentOnOptionSelected(tappedIndex)
                    }
                )
            }
            .pointerInput(options.size, segmentWidthPx) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        // 记录手指按下位置与滑块当前位置的差值，为了在动画途中也能完美无缝地接住滑块
                        dragGrabOffset = thumbOffsetX.value - offset.x
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        // 计算拖拽时滑块应该在的像素位置 (限制在最左和最右边界内)
                        val newTargetX =
                            (change.position.x + dragGrabOffset).coerceIn(0f, maxOffsetX)

                        coroutineScope.launch {
                            // 使用 snapTo 去掉动画，实现完全跟手的 0 延迟拖拽
                            thumbOffsetX.snapTo(newTargetX)
                        }

                        // 动态计算当前滑块中心点处于哪个选项的区域内，并更新选中状态
                        val newIndex = ((newTargetX + segmentWidthPx / 2f) / segmentWidthPx)
                            .toInt()
                            .coerceIn(0, options.size - 1)
                        if (newIndex != currentSelectedIndex) {
                            currentOnOptionSelected(newIndex)
                        }
                    }
                )
            }

        // UI 渲染层
        BoxWithConstraints(
            modifier = modifier
                .height(32.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .padding(2.dp)
                .then(gestureModifier)
        ) {
            // 1. 渲染滑动背景块 (Thumb)
            val segmentWidth = maxWidth / options.size
            val thumbOffsetState = animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "ThumbOffset"
            )

            // 2. 绘制滑块 (背景块)
            Box(
                modifier = Modifier
                    // 2. 【核心性能优化】：使用 Lambda 版本的 offset
                    // 这使得动画数值的读取发生在 Layout 阶段，彻底跳过 重组(Composition)
                    .offset {
                        IntOffset(x = thumbOffsetState.value.roundToPx(), y = 0)
                    }
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(thumbCornerRadius))
                    .background(color = thumbColor, shape = RoundedCornerShape(thumbCornerRadius))
            )

            // 2. 渲染文本选项
            Row(modifier = Modifier.fillMaxSize()) {
                options.forEachIndexed { index, text ->
                    val isSelected = index == currentSelectedIndex

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(), // 移除了 .clickable，因为手势由上层 Box 统一处理了
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            color = if (isSelected) selectedTextColor else unselectedTextColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IOSVariableWidthSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 8.dp
    val thumbCornerRadius = 6.dp
    val backgroundColor = Color(0xFFE5E5EA)
    val thumbColor = Color.White
    val selectedTextColor = Color.Black
    val unselectedTextColor = Color(0xFF8E8E93)

    val coroutineScope = rememberCoroutineScope()
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnOptionSelected by rememberUpdatedState(onOptionSelected)

    // 存储每个非等宽选项的实际边界 (X坐标和宽度)
    val boundsArray = remember(options) { Array(options.size) { Rect.Zero } }
    var segmentBounds by remember(options) { mutableStateOf<List<Rect>>(emptyList()) }
    var isThumbInitialized by remember(options) { mutableStateOf(false) }

    // 滑块的实时坐标和宽度（使用 Animatable 以支持跟手和弹簧动画）
    val thumbX = remember { Animatable(0f) }
    val thumbWidth = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // 【1. 宽度动画】：无论是否在拖拽，只要选中的 index 改变，宽度就平滑渐变到新 index 的宽度
    LaunchedEffect(segmentBounds, currentSelectedIndex) {
        if (segmentBounds.isNotEmpty()) {
            val targetWidth = segmentBounds[currentSelectedIndex].width
            if (!isThumbInitialized) {
                // 初始化时直接闪现，去除多余动画
                thumbWidth.snapTo(targetWidth)
            } else {
                launch {
                    thumbWidth.animateTo(
                        targetWidth,
                        spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    )
                }
            }
        }
    }

    // 【2. X坐标动画】：只在非拖拽（手指松开）时自动吸附归位
    LaunchedEffect(segmentBounds, currentSelectedIndex, isDragging) {
        if (segmentBounds.isNotEmpty() && !isDragging) {
            val targetX = segmentBounds[currentSelectedIndex].left
            if (!isThumbInitialized) {
                thumbX.snapTo(targetX)
                isThumbInitialized = true // 初始化完成
            } else {
                launch {
                    thumbX.animateTo(
                        targetX,
                        spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    )
                }
            }
        }
    }

    // 手势处理层：处理点击和动态跟手拖拽
    val gestureModifier = Modifier
        .pointerInput(segmentBounds) {
            if (segmentBounds.isEmpty()) return@pointerInput
            detectTapGestures(
                onPress = { offset ->
                    val newIndex = segmentBounds.indexOfFirst {
                        offset.x >= it.left && offset.x <= it.right
                    }.coerceIn(0, options.size - 1)
                    if (newIndex >= 0) currentOnOptionSelected(newIndex)
                }
            )
        }
        .pointerInput(segmentBounds) {
            if (segmentBounds.isEmpty()) return@pointerInput
            // dragGrabOffset 仅存在于指针事件内部，修改它不会导致 Compose 重组
            var dragGrabOffset = 0f

            detectHorizontalDragGestures(
                onDragStart = { offset ->
                    isDragging = true
                    dragGrabOffset = thumbX.value - offset.x
                },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onHorizontalDrag = { change, _ ->
                    val maxLimit = segmentBounds.last().left
                    val minLimit = segmentBounds.first().left
                    val newX = (change.position.x + dragGrabOffset).coerceIn(minLimit, maxLimit)

                    coroutineScope.launch {
                        thumbX.snapTo(newX) // 像素级跟手赋值
                    }

                    // 根据滑块目前的中心点位置，判断滑块处于哪个选项上
                    val thumbCenter = newX + thumbWidth.value / 2f
                    val newIndex = segmentBounds.indexOfFirst {
                        thumbCenter >= it.left && thumbCenter <= it.right
                    }.coerceIn(0, options.size - 1)

                    // 如果划到了新区域，更新选中状态 (这会触发上面的宽度跟随动画)
                    if (newIndex >= 0 && newIndex != currentSelectedIndex) {
                        currentOnOptionSelected(newIndex)
                    }
                }
            )
        }

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(2.dp)
    ) {
        // 【核心性能优化】：使用 Modifier.layout 读取 Animatable
        // 这样 thumbX 和 thumbWidth 在 60fps 刷新时，只会触发 Compose 的 Layout(布局)阶段，
        // 彻底跳过了耗时的 Composition(重组)阶段，极致流畅。
        Box(
            modifier = Modifier
                .alpha(if (isThumbInitialized) 1f else 0f) // 未初始化完成前隐藏，防止出现闪烁
                .layout { measurable, constraints ->
                    val w = thumbWidth.value.roundToInt()
                    val x = thumbX.value.roundToInt()
                    // 动态约束滑块宽度
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                    layout(placeable.width, placeable.height) {
                        // 动态设置偏移量
                        placeable.placeRelative(x, 0)
                    }
                }
                .fillMaxHeight()
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(thumbCornerRadius))
                .background(color = thumbColor, shape = RoundedCornerShape(thumbCornerRadius))
        )

        // 选项文本排版
        Row(modifier = gestureModifier.fillMaxHeight()) {
            options.forEachIndexed { index, text ->
                val isSelected = index == currentSelectedIndex

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .onGloballyPositioned { layoutCoordinates ->
                            boundsArray[index] = layoutCoordinates.boundsInParent()
                            // 当所有元素测量完毕后，统一更新一次 State
                            if (boundsArray.none { it.isEmpty }) {
                                segmentBounds = boundsArray.toList()
                            }
                        }
                        .padding(horizontal = 16.dp), // 控制左右内边距，文字越长宽度越大
                    contentAlignment = Alignment.Center
                ) {
                    // 【改动点】：去掉了 animateColorAsState，瞬间变色
                    Text(
                        text = text,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ExampleScreen() {
    val items = listOf("年", "月", "日", "我的照片")
    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DynamicSlidingSegmentedControl(
            options = items,
            selectedIndex = selectedIndex,
            onSelectionChange = { selectedIndex = it }
        )
        Spacer(modifier = Modifier.size(20.dp))
        DraggableDynamicSlidingSegmentedControl(
            options = items,
            selectedIndex = selectedIndex,
            onSelectionChange = { selectedIndex = it }
        )
        Spacer(modifier = Modifier.size(20.dp))
        IOSSegmentedControl(
            options = items,
            selectedIndex = selectedIndex,
            onOptionSelected = { selectedIndex = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(20.dp))
        IOSVariableWidthSegmentedControl(
            options = items,
            selectedIndex = selectedIndex,
            onOptionSelected = { selectedIndex = it },
            modifier = Modifier
        )
    }
}