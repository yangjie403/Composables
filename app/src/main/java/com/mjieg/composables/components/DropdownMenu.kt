package com.mjieg.composables.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MenuDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun CustomDropdownMenuExample() {
    var expanded by remember { mutableStateOf(false) }

    // 整个屏幕居中，方便演示
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

        // 【关键对齐技巧】：
        // 使用 wrapContentSize(Alignment.TopEnd) 让 DropdownMenu 的锚点偏向右侧。
        // 这样弹出的菜单右边缘会和按钮的右边缘对齐。
        Box {

            // 锚点按钮
            Button(onClick = { expanded = true }) {
                Text("自定义 DropdownMenu")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // 【自定义位置】：向右偏移 0dp，向下偏移 10dp（与按钮留出一点空隙）
                // offset = DpOffset(x = 0.dp, y = 10.dp),
                // 【自定义样式】：圆角
                shape = RoundedCornerShape(16.dp),
                // 【自定义样式】：背景色
                containerColor = Color(0xFFF0F4FF), // 浅蓝色背景
                // 【自定义样式】：阴影大小
                // shadowElevation = 12.dp,
                // 【自定义样式】：边框
                border = BorderStroke(1.dp, Color.Blue.copy(alpha = 0.2f))
            ) {
                // 菜单项 1
                DropdownMenuItem(
                    text = { Text("收藏此项目") },
                    leadingIcon = {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                    },
                    onClick = { expanded = false },
                    // 自定义菜单项的颜色
                    colors = MenuDefaults.itemColors(
                        textColor = Color(0xFF1E88E5), // 文字变蓝
                        leadingIconColor = Color.Red   // 图标变红
                    )
                )

                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                // 菜单项 2
                DropdownMenuItem(
                    text = { Text("删除", style = MaterialTheme.typography.bodyLarge) },
                    onClick = { expanded = false },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.Red // 危险操作标红
                    ),
                    // 自定义内边距（比如让上下更宽）
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun CustomAlignedMenuExample() {
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val context = LocalContext.current
    val positionProvider = remember {
        WindowAlignmentProvider(PopupAnchor.TopLeft, with(density) { 16.dp.toPx().toInt() })
    }
    Box {
        Button(onClick = { expanded = true }) {
            Text("点击我")
        }

        if (expanded) {
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                // 自己画一个类似 DropdownMenu 的外观
                Column(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)
                        .width(150.dp) // 指定菜单宽度
                ) {
                    Text(
                        "选项一", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "点击了选项一", Toast.LENGTH_SHORT).show()
                                expanded = false
                            }
                            .padding(16.dp))
                    Text(
                        "选项二", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "点击了选项二", Toast.LENGTH_SHORT).show()
                                expanded = false
                            }
                            .padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun DropDownMenuExample() {
    val context = LocalContext.current
    val options = remember {
        listOf("Option 1", "Option 2", "Option 3")
    }
    var selectedOption by remember { mutableStateOf(options[0]) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DropdownMenuButton(
            modifier = Modifier
                .size(width = 90.dp, 30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            selectedOption = selectedOption,
            fontSize = 12.sp,
            options = options,
            onOptionSelected = {
                selectedOption = it
                Toast.makeText(context, "点击了 $selectedOption", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DropdownMenuButton(
    modifier: Modifier = Modifier,
    selectedOption: String = "",
    fontSize: TextUnit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val positionProvider = WindowAlignmentProvider(PopupAnchor.TopLeft)
    Box {
        Row(
            modifier = modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { expanded = !expanded }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedOption,
                fontSize = fontSize
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
        if (expanded) {
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFC7C7C7))
                        .width(90.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        if (index != 0) {
                            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                        }
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = {
                                        onOptionSelected(option)
                                        expanded = false
                                    }
                                )
                                .padding(vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

enum class PopupAnchor {
    TopLeft,    // 按钮的左上方
    BottomLeft  // 按钮的左下方
}

class WindowAlignmentProvider(
    private val anchor: PopupAnchor,
    private val marginPx: Int = 0 // 可选：留出一点间距
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,     // 按钮在屏幕的位置
        windowSize: IntSize,       // 屏幕大小
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize  // Popup 自身的大小
    ): IntOffset {

        // X 坐标固定为按钮的左侧
        val x = anchorBounds.left

        // 根据选择的方向计算 Y 坐标
        val y = when (anchor) {
            PopupAnchor.TopLeft -> {
                // 按钮顶部坐标 - Popup 高度 - 间距
                anchorBounds.top - popupContentSize.height - marginPx
            }

            PopupAnchor.BottomLeft -> {
                // 按钮底部坐标 + 间距
                anchorBounds.bottom + marginPx
            }
        }

        return IntOffset(x, y)
    }
}