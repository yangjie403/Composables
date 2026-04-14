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

            /**
             * 预滚动回调：在子组件（如 LazyColumn）处理滚动之前调用
             * 用于优先处理 BottomSheet 的展开/收起手势
             *
             * @param available 可用的滚动手势偏移量，y 轴负值表示向上滑动，正值表示向下滑动
             * @param source 滚动来源（用户拖动或惯性滚动）
             * @return 实际消耗的偏移量
             */
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 提取垂直方向的滚动增量
                val delta = available.y
                // 获取当前面板的偏移量，如果为 NaN 则使用隐藏高度作为默认值
                val offset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()

                // 判断条件：向上滑动(delta < 0) 且 面板未完全展开(offset > 0)
                // 此时应该优先让面板展开，而不是让内部列表滚动
                return if (delta < 0 && offset > 0f) {
                    // 将滚动增量分发给 anchoredDraggableState，驱动面板向上展开
                    val consumed = state.dispatchRawDelta(delta)
                    // 返回实际消耗的偏移量（x 轴不消耗，y 轴返回实际消耗值）
                    Offset(0f, consumed)
                } else {
                    // 其他情况不消耗滚动事件，交给子组件处理
                    Offset.Zero
                }
            }

            /**
             * 后滚动回调：在子组件（如 LazyColumn）处理滚动之后调用
             * 用于处理列表已滚动到顶部时，继续向下拉动以收起面板
             *
             * @param consumed 子组件已消耗的偏移量
             * @param available 剩余的可用偏移量（子组件未消耗的部分）
             * @param source 滚动来源
             * @return 实际消耗的偏移量
             */
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 提取剩余的垂直滚动增量
                val delta = available.y
                // 判断条件：向下滑动(delta > 0)，即用户试图向下拉
                // 此时应该让面板跟随手指向下收起
                return if (delta > 0) {
                    // 将向下的滚动增量分发给 anchoredDraggableState，驱动面板向下收起
                    val consumedDelta = state.dispatchRawDelta(delta)
                    // 返回实际消耗的偏移量
                    Offset(0f, consumedDelta)
                } else {
                    // 向上滑动时不在此处处理（已在 onPreScroll 中处理）
                    Offset.Zero
                }
            }

            // 【关键修复点】：完美处理松手后的吸附
            override suspend fun onPreFling(available: Velocity): Velocity {
                val offset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()
                val velocityY = available.y

                // 判断：只要 offset 不在顶点 (0f) 也不在底点 (hiddenHeightPx)，说明面板悬停在半空
                val isIntermediate = offset > 0f && offset < hiddenHeightPx

                if (isIntermediate) {
                    // 1. 根据速度或当前位置决定去向
                    val targetState = when {
                        velocityY > velocityThresholdPx -> BottomSheetState.Collapsed // 快速向下划
                        velocityY < -velocityThresholdPx -> BottomSheetState.Expanded // 快速向上划
                        offset > hiddenHeightPx / 2f -> BottomSheetState.Collapsed    // 缓慢松手，且偏下
                        else -> BottomSheetState.Expanded                             // 缓慢松手，且偏上
                    }
                    // 2. 强制执行吸附动画
                    state.animateTo(targetState)
                    // 3. 消耗掉手势，不要传给列表
                    return available
                }

                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // 如果列表已经滚动到顶部，且还有向下的残余冲击力，收起面板
                if (available.y > 0) {
                    state.animateTo(BottomSheetState.Collapsed)
                    return available
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
                    val currentOffset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()
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