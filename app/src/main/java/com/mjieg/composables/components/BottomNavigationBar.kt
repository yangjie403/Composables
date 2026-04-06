package com.mjieg.composables.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class BottomNavCurveShape(
    private val curveRadius: Dp, // 凹槽的宽度半径
    private val curveDepth: Dp   // 凹槽的深度
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { curveRadius.toPx() }
        val depthPx = with(density) { curveDepth.toPx() }

        val path = Path().apply {
            val center = size.width / 2f
            moveTo(0f, 0f)

            // 画左边的直线
            lineTo(center - radiusPx, 0f)

            // 左半边平滑下凹曲线 (使用调整后的控制点，形成宽碗状 U 型)
            // 0.6 和 0.4 的比例是为了让曲线在中间区域足够平坦，完美包住圆形按钮
            cubicTo(
                x1 = center - radiusPx * 0.4f, y1 = 0f,
                x2 = center - radiusPx * 0.4f, y2 = depthPx,
                x3 = center, y3 = depthPx
            )

            // 右半边平滑上凸曲线
            cubicTo(
                x1 = center + radiusPx * 0.4f, y1 = depthPx,
                x2 = center + radiusPx * 0.4f, y2 = 0f,
                x3 = center + radiusPx, y3 = 0f
            )

            // 画右边的直线
            lineTo(size.width, 0f)

            // 闭合底部和两侧
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CustomBottomNavigationBar() {
    // 提取图片中的近似颜色
    val bottomBarColor = Color(0xFFFFEBEA) // 粉色背景
    val fabColor = Color(0xFFA1AFBE)       // 灰蓝色FAB
    val iconColor = Color(0xFFA1AFBE)      // 未选中图标颜色
    val activeIconColor = Color(0xFF4A5668) // 选中图标颜色 (首页)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // 整体容器高度，留出空间给FAB突出
    ) {
        // 底部带有凹槽的背景栏
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp), // 实际底部栏的高度
            color = bottomBarColor,
            shape = BottomNavCurveShape(curveRadius = 60.dp, curveDepth = 26.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧图标组
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = "Home",
                            tint = activeIconColor
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Outlined.DateRange,
                            contentDescription = "Calendar",
                            tint = iconColor
                        )
                    }
                }

                // 中间占位，把空间留给凹槽和FAB
                Spacer(modifier = Modifier.width(80.dp))

                // 右侧图标组
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Compose默认没完全一致的钱包图标，这里用购物车/其他代替示意
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = "Wallet",
                            tint = iconColor
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = iconColor
                        )
                    }
                }
            }
        }

        // 中心的悬浮按钮 (FAB)
        FloatingActionButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .align(Alignment.TopCenter)
                //.offset(y = 10.dp) // 调整Y轴偏移量，使其正好落入凹槽中
                .size(50.dp),
            shape = CircleShape,
            containerColor = fabColor,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp) // 图片中似乎没有明显阴影
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun AnimatedBottomNavigationBar() {
    // 状态：记录当前选中的索引
    var selectedIndex by remember { mutableIntStateOf(0) }

    // 导航栏项目列表 (这里用系统自带图标示意，你可以替换成自己的)
    val navItems = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.DateRange,
        Icons.Outlined.Person,
        Icons.Outlined.ShoppingCart
    )

    // 提取配色
    val bottomBarColor = Color(0xFFFFEBEA) // 粉色背景
    val unselectedIconColor = Color(0xFFA1AFBE) // 灰蓝未选中图标
    val selectedIconColor = Color(0xFF4A5668) // 深灰选中图标

    // 1. 背景凹槽 X 轴平移的动画状态
    // 使用 spring (弹簧动画) 让滑动带有轻微的弹性，看起来更灵动
    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // 容器总高度设为100dp，留出上方的空间给图标"弹"上去
            // 2. 实时绘制动态跟随的凹槽背景
            .drawBehind {
                val itemWidth = size.width / navItems.size
                // 计算当前凹槽的中心 X 坐标 (根据动画偏移量实时改变)
                val centerX = (animatedOffset + 0.5f) * itemWidth

                // 定义凹槽的尺寸参数
                val radius = 55.dp.toPx() // 凹槽宽度半径
                val depth = 35.dp.toPx()  // 凹槽陷下去的深度
                val barHeight = 70.dp.toPx() // 底部栏的主体高度
                val startY = size.height - barHeight // 背景的顶部 Y 坐标

                // 用 Path 绘制带有贝塞尔曲线的形状
                val path = Path().apply {
                    moveTo(0f, startY)
                    lineTo(centerX - radius, startY)

                    // 左半边平滑下凹
                    cubicTo(
                        centerX - radius / 2f, startY,
                        centerX - radius / 2f, startY + depth,
                        centerX, startY + depth
                    )

                    // 右半边平滑上凸
                    cubicTo(
                        centerX + radius / 2f, startY + depth,
                        centerX + radius / 2f, startY,
                        centerX + radius, startY
                    )

                    lineTo(size.width, startY)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = bottomBarColor)
            }
    ) {
        // 3. 绘制图标内容
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // 贴着底部
                .height(70.dp), // Row 的高度等于粉色主体的高度
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, icon ->
                val isSelected = selectedIndex == index

                // 图标 Y 轴上移的动画 (选中时往上偏移 35dp)
                val yOffset by animateDpAsState(
                    targetValue = if (isSelected) (-35).dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "yOffset"
                )

                // 图标颜色的过渡动画
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) selectedIconColor else unselectedIconColor,
                    label = "iconColor"
                )

                // 选中时的圆形粉色背景的透明度动画
                val circleAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "alpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // 移除点击时的水波纹效果，避免破坏干净的 UI
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selectedIndex = index }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 这个 Box 负责 Y 轴的上下移动和圆形背景
                    Box(
                        modifier = Modifier
                            .offset(y = yOffset) // 核心：上下移动
                            .size(55.dp) // 圆形背景的尺寸
                            .background(
                                color = bottomBarColor.copy(alpha = circleAlpha),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizedAnimatedBottomNavigationBar() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.DateRange,
        Icons.Outlined.Person,
        Icons.Outlined.ShoppingCart
    )

    val bottomBarColor = Color(0xFFFFEBEA)
    val unselectedIconColor = Color(0xFFA1AFBE)
    val selectedIconColor = Color(0xFF4A5668)

    // 凹槽平移状态 (完全在 drawBehind 中读取，无重组)
    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .drawBehind { // 【优化点】在 Draw 阶段读取 animatedOffset，避免组合阶段重组
                val itemWidth = size.width / navItems.size
                val centerX = (animatedOffset + 0.5f) * itemWidth

                val radius = 55.dp.toPx()
                val depth = 35.dp.toPx()
                val barHeight = 70.dp.toPx()
                val startY = size.height - barHeight

                val path = Path().apply {
                    moveTo(0f, startY)
                    lineTo(centerX - radius, startY)
                    cubicTo(
                        centerX - radius / 2f, startY,
                        centerX - radius / 2f, startY + depth,
                        centerX, startY + depth
                    )
                    cubicTo(
                        centerX + radius / 2f, startY + depth,
                        centerX + radius / 2f, startY,
                        centerX + radius, startY
                    )
                    lineTo(size.width, startY)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = bottomBarColor)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, icon ->
                val isSelected = selectedIndex == index

                // 位移动画状态
                val yOffset by animateDpAsState(
                    targetValue = if (isSelected) (-35).dp else 0.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "yOffset"
                )

                // 综合透明度状态 (替代颜色动画)
                val transitionProgress by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "progress"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selectedIndex = index }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            // 【极致优化 1】使用 Lambda 版本的 offset
                            // 这会将状态的读取推迟到 Layout(布局) 阶段，跳过 Recomposition
                            .offset { IntOffset(0, yOffset.roundToPx()) },
                        contentAlignment = Alignment.Center
                    ) {
                        // 背后那个粉色圆圈
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                // 【极致优化 2】使用 graphicsLayer 控制透明度
                                // 这会将状态推迟到 Draw(绘制) 阶段，避免修改 background 颜色引发重组
                                .graphicsLayer { alpha = transitionProgress }
                                .background(color = bottomBarColor, shape = CircleShape)
                        )

                        // 【极致优化 3】使用两层 Icon 交替透明度，替代 animateColorAsState
                        // Icon 的 tint 一旦改变必引发自身重组。通过改变叠加层的 alpha 可以将改变推迟到绘制阶段
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = unselectedIconColor,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer { alpha = 1f - transitionProgress } // 未选中时的图标
                        )

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = selectedIconColor,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer { alpha = transitionProgress } // 选中时的深色图标
                        )
                    }
                }
            }
        }
    }
}