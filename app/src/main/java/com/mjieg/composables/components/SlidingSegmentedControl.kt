package com.mjieg.composables.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else totalIntrinsicWidth
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
    }
}