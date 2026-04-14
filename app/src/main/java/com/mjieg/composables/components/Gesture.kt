package com.mjieg.composables.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val TAG = "Gesture"

@Composable
fun DetectTapGesturesExample() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        Log.d(TAG, "onTap: $offset")
                    },
                    onDoubleTap = { offset ->
                        Log.d(TAG, "onDoubleTap: $offset")
                    },
                    onLongPress = { offset ->
                        Log.d(TAG, "onLongPress: $offset")
                    },
                    onPress = { offset ->
                        Log.d(TAG, "onPress: $offset")
                    }
                )
            }
    )
}

@Composable
fun DetectDragGesturesExample() {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(offsetX.toInt(), offsetY.toInt())
                }
                .size(200.dp)
                .background(Color.Green)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        )
    }
}

@Composable
fun DetectTransformGesturesExample() {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    translationX = offset.x
                    translationY = offset.y
                }
                .size(150.dp)
                .background(Color.Green)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotate ->
                        scale *= zoom
                        rotation += rotate
                        offset += pan
                    }
                }

        )
    }
}

enum class DragValue {
    Closed,
    Open
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableItem1() {
    val density = LocalDensity.current
    val menuWidth = 160.dp
    val menuWidthPx = with(density) { menuWidth.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Closed
        )
    }

    SideEffect {
        state.updateAnchors(
            DraggableAnchors {
                DragValue.Closed at 0f
                DragValue.Open at -menuWidthPx
            }
        )
    }
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        positionalThreshold = { distance -> distance * 0.5f },
        animationSpec = tween()
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    ) {
        // 底层：菜单层 (Buttons)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                // 平移效果
                // .offset {
                //     IntOffset(state.requireOffset().roundToInt() + menuWidth.toPx().roundToInt(), 0)
                // }
                .align(Alignment.CenterEnd) // 靠右对齐
                .width(menuWidth)
                // 缩放效果
                .graphicsLayer {
                    val currentOffset = if (state.offset.isNaN()) 0f else state.requireOffset()
                    // 计算缩放比例：范围 0f ~ 1f
                    val scaleFraction = (-currentOffset / menuWidth.toPx()).coerceIn(0f, 1f)
                    scaleX = scaleFraction
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                }
        ) {
            // 菜单按钮：收藏
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { Log.d(TAG, "SwipeableItem: 收藏") }) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                }
            }
            // 菜单按钮：删除
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { Log.d(TAG, "SwipeableItem: 删除") }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                }
            }
        }

        // 上层：内容层 (Item Content)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    // 应用滑动的偏移量
                    IntOffset(state.requireOffset().roundToInt(), 0)
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    flingBehavior = flingBehavior
                )
                .background(Color.LightGray)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = "向左滑动查看菜单 ->", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SwipeableListContainer() {
    val listState = rememberLazyListState()

    // 核心状态：记录当前哪一项被展开了，null表示全关
    var openItemId by remember { mutableStateOf<Int?>(null) }

    // 【要求2】监听列表滚动：在滑动时自动收起已展开的项
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            openItemId = null
        }
    }

    // 模拟列表数据
    val items = remember { List(20) { it } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = items, key = { it }) { id ->
            SwipeableItem(
                id = id,
                // 是否应该展开由父组件决定
                isOpen = openItemId == id,
                onOpen = {
                    // 【要求1 & 3】某个项侧滑打开时，更新ID，其他的项会自动收起
                    openItemId = id
                },
                onClose = {
                    // 当前项收起时重置状态
                    if (openItemId == id) openItemId = null
                },
                onClick = {
                    // 【要求2】点击任何列表项的内容层时，收起展开项并触发点击
                    openItemId = null
                    Log.d("SwipeableList", "点击了列表项: $id")
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableItem(
    id: Int,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val menuWidth = 160.dp
    val menuWidthPx = with(density) { menuWidth.toPx() }

    val state = remember {
        AnchoredDraggableState(initialValue = if (isOpen) DragValue.Open else DragValue.Closed)
    }

    SideEffect {
        state.updateAnchors(
            DraggableAnchors {
                DragValue.Closed at 0f
                DragValue.Open at -menuWidthPx
            }
        )
    }

    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        positionalThreshold = { distance -> distance * 0.5f },
        animationSpec = tween()
    )

    // 【重要】同步 外部状态 -> 内部动画
    // 当外层改变 isOpen 状态时 (例如被别的项顶掉，或触发了滑动/点击)，执行动画收起/展开
    LaunchedEffect(isOpen) {
        if (isOpen && state.currentValue != DragValue.Open) {
            state.animateTo(DragValue.Open)
        } else if (!isOpen && state.currentValue != DragValue.Closed) {
            state.animateTo(DragValue.Closed)
        }
    }

    // 【重要】同步 内部手势 -> 外部状态
    // 当用户真实用手指滑这个 item 时，更新父级的记录
    LaunchedEffect(state) {
        snapshotFlow { state.targetValue }.collect { targetValue ->
            if (targetValue == DragValue.Open && !isOpen) {
                onOpen()
            } else if (targetValue == DragValue.Closed && isOpen) {
                onClose()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    ) {
        // 底层：菜单层 (Buttons)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .width(menuWidth)
                .graphicsLayer {
                    val currentOffset = if (state.offset.isNaN()) 0f else state.requireOffset()
                    val scaleFraction = (-currentOffset / menuWidth.toPx()).coerceIn(0f, 1f)
                    scaleX = scaleFraction
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight().weight(1f).background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { Log.d("SwipeableItem", "收藏 $id") }) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight().weight(1f).background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { Log.d("SwipeableItem", "删除 $id") }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                }
            }
        }

        // 上层：内容层 (Item Content)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    // 加入 isNaN 防御，避免第一次重组时崩溃
                    val currentOffset = if (state.offset.isNaN()) 0f else state.requireOffset()
                    IntOffset(currentOffset.roundToInt(), 0)
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    flingBehavior = flingBehavior
                )
                .background(Color.LightGray)
                .clickable { onClick() } // 绑定点击事件，此处会触发父容器收起其他菜单的逻辑
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = "Item $id 向左滑动查看菜单 ->", style = MaterialTheme.typography.bodyLarge)
        }
    }
}