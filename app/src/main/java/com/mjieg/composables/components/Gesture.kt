package com.mjieg.composables.components

import android.util.Log
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
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
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { Log.d("SwipeableItem", "收藏 $id") }) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color.Red),
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

// 定义底部菜单的状态
enum class BottomSheetState {
    Collapsed, // 收起状态 (只露一点)
    Expanded   // 展开状态 (全露出)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NestedScrollBottomSheet() {
    val density = LocalDensity.current

    // 尺寸定义
    val sheetFullHeight = 400.dp
    val sheetPeekHeight = 150.dp // 初始露出的高度
    val velocityThresholdPx = with(density) { 125.dp.toPx() }

    // 计算隐藏部分的高度像素值，也是 Collapsed 状态下的偏移量
    val hiddenHeightPx = with(density) { (sheetFullHeight - sheetPeekHeight).toPx() }

    // 1. 初始化 AnchoredDraggableState
    val state = remember {
        AnchoredDraggableState(
            initialValue = BottomSheetState.Collapsed,
            anchors = DraggableAnchors {
                BottomSheetState.Collapsed at hiddenHeightPx
                BottomSheetState.Expanded at 0f
            },
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay()
        )
    }

    // 2. 构建嵌套滑动连接器 (核心冲突处理机制)
    val nestedScrollConnection = remember(state) {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 【关键修改 1】：只有用户真实手指拖动，才允许实时改变偏移量。拒绝惯性产生的假性滑动。
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val delta = available.y
                val offset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()

                // 向上滑，且面板未完全展开时，优先展开面板
                return if (delta < 0 && offset > 0f) {
                    val consumed = state.dispatchRawDelta(delta)
                    Offset(0f, consumed)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 【关键修改 2】：同上，拦截惯性的假性滑动
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val delta = available.y
                // 列表滑到顶后，向下的拉力用来拉动面板
                return if (delta > 0) {
                    val consumedDelta = state.dispatchRawDelta(delta)
                    Offset(0f, consumedDelta)
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val offset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()
                val velocityY = available.y

                // 场景A：手指向上飞划，且面板未完全展开 -> 直接展开面板
                if (velocityY < 0 && offset > 0f) {
                    state.animateTo(BottomSheetState.Expanded)
                    return available // 吞掉速度，不让列表发生滚动
                }

                // 场景B：手指拖拽面板到一半，缓慢松手 或 飞划松手
                val isIntermediate = offset > 0f && offset < hiddenHeightPx
                if (isIntermediate) {
                    // 根据速度或当前所处的位置，智能判断去向
                    val targetState = when {
                        velocityY > velocityThresholdPx -> BottomSheetState.Collapsed
                        velocityY < -velocityThresholdPx -> BottomSheetState.Expanded
                        offset > hiddenHeightPx / 2f -> BottomSheetState.Collapsed
                        else -> BottomSheetState.Expanded
                    }
                    state.animateTo(targetState)
                    return available
                }

                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val offset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()

                // 场景C：【你遇到的Bug的终极解法】
                // 列表猛烈向下滑动撞到了顶部，产生了巨大的向下残余冲击力
                if (available.y > 0) {
                    state.animateTo(BottomSheetState.Collapsed)
                    return available
                }

                // 终极兜底保障：不管发生什么，只要一切滑动结束，如果面板还没吸附，强制吸附
                if (offset > 0f && offset < hiddenHeightPx) {
                    val targetState = if (offset > hiddenHeightPx / 2f) BottomSheetState.Collapsed else BottomSheetState.Expanded
                    state.animateTo(targetState)
                }

                return Velocity.Zero
            }
        }
    }


    // ================= UI 布局结构 =================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0)) // 模拟背景页面
    ) {
        // 背景页面的内容
        Text(
            text = "我是底部的背景层内容\n向上滑动底部的控制条试试",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        // Bottom Sheet 容器
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetFullHeight)
                // 3. 应用滑动偏移量
                .offset {
                    val currentOffset =
                        if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()
                    IntOffset(x = 0, y = currentOffset.roundToInt())
                }
                // 4. 绑定嵌套滑动 (极其重要：绑定在容器最外层，必须在 draggable 之前)
                .nestedScroll(nestedScrollConnection)
                // 5. 绑定拖拽手势
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical
                )
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color.White)
        ) {
            // Sheet 的头部 (露出区域 / 拖拽把手)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color(0xFF6200EE)),
                contentAlignment = Alignment.Center
            ) {
                // 把手视觉指示器
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }

            // Sheet 内部可滑动的列表内容
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(30) { index ->
                    Text(
                        text = "内部可滑动列表项 #$index",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsingToolbarExample() {
    val toolbarHeight = 80.dp
    val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }

    // 状态：记录 Toolbar 的偏移量（范围是 -toolbarHeightPx 到 0）
    var toolbarOffsetPx by remember { mutableFloatStateOf(0f) }

    // 1. 创建 NestedScrollConnection
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

            // 阶段 1：拖拽前。优先处理向上滑动（隐藏Toolbar）
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // 假设 delta < 0 (向上滑动) 或 delta > 0 (向下滑动)
                // 我们在拖拽前拦截滑动事件，用来改变 Toolbar 的位置
                val newOffset = toolbarOffsetPx + delta

                // 限制偏移量在 [-80px, 0px] 之间
                val coercedOffset = newOffset.coerceIn(-toolbarHeightPx, 0f)

                // 计算我们实际消耗掉的像素值
                val consumed = coercedOffset - toolbarOffsetPx
                toolbarOffsetPx = coercedOffset

                // 告诉子组件：我们消耗了 consumed 这么多，剩下的你拿去滑
                return Offset(0f, consumed)
            }
        }
    }

    // 2. 将 Connection 绑定到父级容器上
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection) // 必须绑定在包裹列表的外层！
    ) {
        // 3. 子组件：列表
        LazyColumn(
            // 为了不让 Toolbar 挡住第一行数据，加上 paddingTop
            contentPadding = PaddingValues(top = toolbarHeight)
        ) {
            items(100) { index ->
                Text(
                    text = "我是列表内容 $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // 4. 父组件：折叠标题栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(toolbarHeight)
                // 根据滑动的偏移量移动 Toolbar
                .offset { IntOffset(x = 0, y = toolbarOffsetPx.roundToInt()) }
                .background(Color.Blue),
            contentAlignment = Alignment.Center
        ) {
            Text("可以折叠的标题栏", color = Color.White, style = MaterialTheme.typography.titleLarge)
        }
    }
}