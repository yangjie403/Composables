package com.mjieg.composables.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import com.mjieg.composables.R
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

            /**
             * 预滚动回调：在子组件（如 LazyColumn）处理滚动之前调用
             * 用于优先处理 BottomSheet 的展开/收起手势
             *
             * @param available 可用的滚动手势偏移量，y 轴负值表示向上滑动，正值表示向下滑动
             * @param source 滚动来源（用户拖动或惯性滚动）
             * @return 实际消耗的偏移量
             */
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
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

                if (source != NestedScrollSource.UserInput) return Offset.Zero
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

            /**
             * 预惯性滚动回调：在用户快速滑动松手后、惯性动画开始前调用
             * 用于处理 BottomSheet 面板的吸附逻辑，确保松手后面板能自动吸附到展开或收起状态
             *
             * @param available 可用的惯性速度，y 轴正值表示向下惯性，负值表示向上惯性
             * @return 实际消耗的速度（返回 available 表示完全消耗，Velocity.Zero 表示不消耗）
             */
            override suspend fun onPreFling(available: Velocity): Velocity {
                // 获取当前面板的垂直偏移量，如果为 NaN 则使用隐藏高度作为默认值
                val offset = if (state.offset.isNaN()) hiddenHeightPx else state.requireOffset()
                // 提取垂直方向的惯性速度
                val velocityY = available.y

                // 判断面板是否处于中间状态（既未完全展开也未完全收起）
                // offset > 0f：面板未完全展开（完全展开时 offset = 0）
                // offset < hiddenHeightPx：面板未完全收起（完全收起时 offset = hiddenHeightPx）
                val isIntermediate = offset > 0f && offset < hiddenHeightPx

                if (isIntermediate) {
                    // 面板悬停在半空，需要根据位置决定吸附目标状态
                    // 1. 根据当前位置决定去向（已注释掉速度判断逻辑）
                    val targetState = when {
                        // 原逻辑：根据惯性速度大小判断
                        // velocityY > velocityThresholdPx -> BottomSheetState.Collapsed // 快速向下划，吸附到收起状态
                        // velocityY < -velocityThresholdPx -> BottomSheetState.Expanded // 快速向上划，吸附到展开状态

                        // 现逻辑：仅根据面板当前位置判断
                        offset > hiddenHeightPx / 2f -> BottomSheetState.Collapsed    // 面板位置偏下（超过一半），吸附到收起状态
                        else -> BottomSheetState.Expanded                             // 面板位置偏上（未过半），吸附到展开状态
                    }

                    // 2. 强制执行吸附动画，将面板平滑过渡到目标状态
                    // animateTo 是 suspend 函数，会等待动画完成后再继续执行
                    state.animateTo(targetState)

                    // 3. 消耗掉所有惯性速度，阻止惯性传递给内部列表
                    // 这样可以避免面板吸附过程中列表还在惯性滚动
                    return available
                }

                // 面板已在顶点或底点，不处理惯性，交给子组件处理
                return Velocity.Zero
            }

            /**
             * 后惯性滚动回调：在子组件处理完惯性滚动之后调用
             * 可用于处理特殊情况下的面板收起逻辑（当前已注释）
             *
             * @param consumed 子组件已消耗的惯性速度
             * @param available 剩余的可用惯性速度（子组件未消耗的部分）
             * @return 实际消耗的速度
             */
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // 原逻辑：如果列表已滚动到顶部，且还有向下的残余惯性速度，则收起面板
                // 适用场景：用户在列表顶部快速向下滑动，希望直接收起 BottomSheet
                // if (available.y > 0) {
                //    state.animateTo(BottomSheetState.Collapsed)
                //    return available
                //}

                // 当前禁用此逻辑，不处理后惯性事件
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
                return Offset(0f, 0f)
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
            Text(
                "可以折叠的标题栏",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun ImageCropperDemo() {
    val context = LocalContext.current

    // 1. 加载 Bitmap
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    // 使用 remember 加载优化过的 Bitmap，避免重复加载
    val imageBitmap = remember {
        decodeSampledBitmapFromResource(context, R.drawable.steve, screenWidth, screenHeight)
    }

    // 2. 状态变量
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 裁剪框设定（假设为 250dp 的正方形）
    val cropSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 250.dp.toPx() }
    val cropRectSize = Size(cropSizePx, cropSizePx)

    // 3. 计算约束逻辑
    // 当容器大小确定后，计算初始最小缩放比例
    val minScale = remember(containerSize, imageBitmap) {
        if (containerSize == IntSize.Zero) 1f else {
            val widthScale = cropRectSize.width / imageBitmap.width
            val heightScale = cropRectSize.height / imageBitmap.height
            // 取最大值，确保宽和高都至少大于等于裁剪框
            maxOf(widthScale, heightScale)
        }
    }
    val maxScale by remember {
        mutableFloatStateOf(4f)
    }

    // 初始缩放设为 minScale
    LaunchedEffect(minScale) {
        scale = minScale
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .onGloballyPositioned { containerSize = it.size },
        contentAlignment = Alignment.Center
    ) {
        // 4. 下层：图片绘制（使用 Canvas 方便精确控制位移和缩放）
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(imageBitmap, containerSize) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // 更新缩放
                        val targetScale = (scale * zoom).coerceIn(minScale, maxScale)

                        // 更新位移并应用边界约束
                        val newScale = targetScale
                        val newOffset = offset + pan

                        // 计算当前缩放后的图片尺寸
                        val scaledWidth = imageBitmap.width * newScale
                        val scaledHeight = imageBitmap.height * newScale

                        // 边界计算：图片边缘不能进入裁剪框
                        // 图片相对于中心点的最大允许偏移量
                        val maxOffsetX = (scaledWidth - cropRectSize.width) / 2
                        val maxOffsetY = (scaledHeight - cropRectSize.height) / 2

                        scale = newScale
                        offset = Offset(
                            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                }
        ) {
            // 将绘图坐标系移至中心
            withTransform({
                translate(size.width / 2f + offset.x, size.height / 2f + offset.y)
                scale(scale, scale, pivot = Offset.Zero)
            }) {
                // 绘制图片，中心点对齐
                drawImage(
                    image = imageBitmap,
                    topLeft = Offset(-imageBitmap.width / 2f, -imageBitmap.height / 2f)
                )
            }
        }

        // 5. 上层：蒙层与裁剪框
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val left = (canvasWidth - cropRectSize.width) / 2
            val top = (canvasHeight - cropRectSize.height) / 2

            val outerPath = Path().apply {
                addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
            }
            val innerPath = Path().apply {
                addRect(Rect(left, top, left + cropRectSize.width, top + cropRectSize.height))
            }

            // 绘制半透明蒙层（差集填充）
            val combinedPath = Path.combine(PathOperation.Difference, outerPath, innerPath)
            drawPath(combinedPath, color = Color.Black.copy(alpha = 0.7f))

            // 绘制裁剪框白边
            drawRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = cropRectSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// --- 优化 1: 安全加载 Bitmap 的工具函数 ---
fun decodeSampledBitmapFromResource(context: Context, resId: Int, reqWidth: Int, reqHeight: Int): ImageBitmap {
    val options = BitmapFactory.Options().apply {
        // 只检查尺寸
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, resId, this)

        // 计算缩放倍数（inSampleSize 只能是 2 的幂）
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        // 正式加载
        inJustDecodeBounds = false
        // 建议使用 RGB_565 减少一半内存开销（如果不需要透明度）
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)

    // --- 优化 2: 如果图片依然超过 GPU 纹理限制 (通常是 4096)，进行强制压缩 ---
    val maxTextureSize = 4096
    return if (bitmap.width > maxTextureSize || bitmap.height > maxTextureSize) {
        val scale = maxTextureSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        val scaledBitmap =
            bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())
        scaledBitmap.asImageBitmap()
    } else {
        bitmap.asImageBitmap()
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}