package com.mjieg.composables.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
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