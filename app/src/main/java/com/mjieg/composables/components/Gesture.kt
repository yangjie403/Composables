package com.mjieg.composables.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
fun SwipeableItem() {
    val density = LocalDensity.current
    val menuWidth = 160.dp
    val menuWidthPx = with(density) { menuWidth.toPx() }

    // 1. 定义衰减动画（新版本必须提供）
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()

    // 2. 构造 AnchoredDraggableState
    // 注意：构造函数里现在只有这些参数，没有 anchors
    val state = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Closed,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    // 3. 使用 SideEffect 或 remember 更新锚点
    // 当 menuWidthPx 变化时，重新更新锚点映射
    SideEffect {
        state.updateAnchors(
            DraggableAnchors {
                DragValue.Closed at 0f
                DragValue.Open at -menuWidthPx
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    ) {
        // 3. 底层：菜单层 (Buttons)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd) // 靠右对齐
                .width(menuWidth)
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

        // 4. 上层：内容层 (Item Content)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    // 应用滑动的偏移量
                    IntOffset(state.requireOffset().roundToInt(), 0)
                }
                .anchoredDraggable(state, Orientation.Horizontal) // 绑定拖动手势
                .background(Color.LightGray)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = "向左滑动查看菜单 ->", style = MaterialTheme.typography.bodyLarge)
        }
    }
}