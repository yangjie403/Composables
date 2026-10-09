package com.mjieg.composables.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

data class ListItemData(val id: Int, val title: String)

@Composable
fun ScrollableMenuDemo() {
    val itemList = remember { List(30) { ListItemData(it, "列表项 #$it") } }
    // 提升状态：保存当前打开菜单的项ID
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = itemList,
            key = { it.id }
        ) { item ->
            val isMenuExpanded = expandedItemId == item.id

            // 1. 设置 zIndex：展开时提高层级，使其浮在下方其它项的上方
            // 2. 使用 OverlayAnchorBox 确保列表项尺寸不受菜单影响
            OverlayAnchorBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isMenuExpanded) 1f else 0f),
                // 主内容：项自身的大小
                content = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { expandedItemId = item.id }
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                // 浮动层：无论多大都不会撑开该项空间
                overlay = {
                    if (isMenuExpanded) {
                        ItemDropDownMenu(
                            onCloseClick = { expandedItemId = null }
                        )
                    }
                }
            )
        }
    }
}

/**
 * 核心组件：浮动锚点布局
 * 仅以 content 的尺寸作为整个布局的最终尺寸，overlay 绝不影响父级大小
 */
@Composable
fun OverlayAnchorBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    overlay: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = {
            content()
            overlay()
        }
    ) { measurables, constraints ->
        // 1. 测量主内容
        val contentPlaceable = measurables[0].measure(constraints)

        // 2. 测量浮层（使用宽松约束，允许其超出或使用固有尺寸）
        val overlayPlaceable = measurables.getOrNull(1)?.measure(
            constraints.copy(minWidth = 0, minHeight = 0)
        )

        // 3. 关键点：整个布局的宽高严格等于 content 的宽高！
        // 浮层占用空间被忽略，下面的 item 绝不会被往下推
        layout(contentPlaceable.width, contentPlaceable.height) {
            // 放置主内容
            contentPlaceable.placeRelative(0, 0)

            // 放置浮动菜单：紧贴在 content 的底部右侧（可根据需要微调坐标）
            overlayPlaceable?.let { overlayItem ->
                // x 坐标：靠右放置，右边留出 24dp 边距
                val x = contentPlaceable.width - overlayItem.width - 24.dp.roundToPx()
                // y 坐标：直接放在主内容下方，与主内容顶部对齐
                val y = 0

                overlayItem.placeRelative(x, y)
            }
        }
    }
}

/**
 * 菜单组件
 */
@Composable
fun ItemDropDownMenu(
    onCloseClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 仅此按钮可以关闭菜单
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "快捷菜单",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "操作一",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* 业务操作 */ }
                    .padding(vertical = 8.dp)
            )
            Text(
                text = "操作二",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* 业务操作 */ }
                    .padding(vertical = 8.dp)
            )
        }
    }
}