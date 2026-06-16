package com.mjieg.composables.components

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


private const val TAG = "Animation"

@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    cornerRadius: Dp = 12.dp,
    paddingValues: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    // 动画目标值
    val offsetY = remember { Animatable(0f) }
    val elevation = remember { Animatable(20f) }

    // 监听按压状态
    LaunchedEffect(isPressed.value) {
        if (isPressed.value) {
            // 按下时：向下偏移，减少阴影
            launch {
                offsetY.animateTo(
                    targetValue = 4f,
                    animationSpec = tween(durationMillis = 100)
                )
            }
            launch {
                // 修改2: 按下时阴影降低到 3dp，保留一点阴影，避免完全消失
                elevation.animateTo(
                    targetValue = 3f,
                    animationSpec = tween(durationMillis = 100)
                )
            }
        } else {
            // 松开时：回到原位置，恢复阴影
            launch {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
            launch {
                // 恢复到 10dp
                elevation.animateTo(
                    targetValue = 20f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.value.dp.toPx()
                shape = RoundedCornerShape(cornerRadius)
                shadowElevation = elevation.value.dp.toPx()
                ambientShadowColor = Color.Red
                spotShadowColor = Color.Blue
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(paddingValues)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Preview
@Composable
fun AnimatedButtonPreview() {
    Column(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedButton(
            text = "点击我",
            onClick = { }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "你好",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun AnimatedVisibilityExample() {
    var isVisible by remember { mutableStateOf(true) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isVisible)
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.tertiary,
        animationSpec = tween(durationMillis = 300)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "我是上面的 Box",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable {
                    isVisible = !isVisible
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isVisible) "点击隐藏" else "点击显示",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AnimateContentSizeExample() {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // animateContentSize修饰符必须放在任何大小修饰符之前
        ExpandableText(
            text = "Jetpack Compose 提供了 animateContentSize 修饰符来实现高度与宽度的平滑尺寸变化动画。这是一个多行长文本的展开/收起示例。我们没有使用传统的覆盖渐变背景那种取巧且容易遮挡文字的方法，而是使用了 TextMeasurer 先行测量并精确计算第二行的末尾索引位置，确保“...展开”这几个字无缝地衔接在文本的最尾部。点击高亮文字后，它将平滑过渡，这极大地提升了用户界面的交互体验！",
            maxLines = 3
        )
        Spacer(Modifier.height(20.dp))
        Text("动态展开文本演示", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2
) {
    var isExpanded by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)

    val expandText = "...展开"
    val collapseText = " 收起"

    BoxWithConstraints(modifier = modifier) {
        val originalMeasure = remember(text, constraints.maxWidth, textStyle) {
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = textStyle,
                maxLines = maxLines,
                constraints = constraints
            )
        }

        if (!originalMeasure.hasVisualOverflow) {
            Text(
                text = text,
                style = textStyle,
                modifier = Modifier.animateContentSize()
            )
        } else {
            val collapsedAnnotatedStr = remember(text, constraints.maxWidth, textStyle) {
                val lineEndIndex = originalMeasure.getLineEnd(maxLines - 1)
                var bestIndex = lineEndIndex

                while (bestIndex > 0) {
                    if (Character.isHighSurrogate(text[bestIndex - 1])) {
                        bestIndex--
                    }

                    val testString = text.substring(0, bestIndex) + expandText
                    val testMeasure = textMeasurer.measure(
                        text = AnnotatedString(testString),
                        style = textStyle,
                        maxLines = maxLines,
                        constraints = constraints
                    )

                    if (!testMeasure.hasVisualOverflow) {
                        break
                    }
                    bestIndex--
                }

                buildAnnotatedString {
                    append(text.substring(0, bestIndex))
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "EXPAND",
                            styles = TextLinkStyles(style = SpanStyle(color = Color.Blue)),
                            linkInteractionListener = { isExpanded = true }
                        )
                    ) {
                        append(expandText)
                    }
                }
            }

            val expandedAnnotatedStr = remember(text) {
                buildAnnotatedString {
                    append(text)
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "COLLAPSE",
                            styles = TextLinkStyles(style = SpanStyle(color = Color.Blue)),
                            linkInteractionListener = { isExpanded = false }
                        )
                    ) {
                        append(collapseText)
                    }
                }
            }

            val currentText = if (isExpanded) expandedAnnotatedStr else collapsedAnnotatedStr

            Text(
                text = currentText,
                style = textStyle,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

/**
 * layout和size顺序的区别
 * 情况一：size在layout之后（当前代码写法）
 * 执行逻辑（从内向外报告尺寸）：
 * 约束传递（向下）：父组件给出可用空间限制，传递给layout，layout原封不动传递给size。
 * 内层测量（向上）：size(100.dp)接收到约束后，将内部的 Box强制测量为100x100，并作为一个 Placeable返回给你的layout。
 * 外层测量（向上）：在你的layout闭包中，placeable.width就是精确的100dp的像素值。然后你执行了layout()。
 * 最终结果：这个组件向它的父布局报告的最终尺寸是 100 + offset。
 * 如果旁边有其他组件，它会把周围的组件推开。
 *
 * 情况二：size在layout之前
 * 执行逻辑（从内向外报告尺寸）：
 * 约束传递（向下）：size(100.dp)接收到父组件的约束后，生成一个固定约束（Exactly 100dp），传递给内层的layout。
 * 内层测量（向上）：layout让最内部的Box进行测量（因为有固定约束，Box变成100x100）。layout尝试计算自己的尺寸为100+offset，并将其返回给size。
 * 外层测量（向上）：size(100.dp) 作为一个强势的“外壳”，它无视了内部layout报告的变大尺寸，强行截断，向父容器报告：“我的尺寸就是严格的100x100”。
 * 最终结果：这个组件向它的父布局报告的最终尺寸永远是固定的100dp x 100dp。
 * 它不会推开周围的任何组件，而是会覆盖在周围组件的上方（或者被覆盖，取决于层级）
 *
 * 框架的自动居中补偿
 * 在Compose中，当一个组件报告的尺寸（200px）大于它最终被强制限制的尺寸（100px）时，
 * Compose默认的放置策略是：将超出约束范围的内容“居中放置”在受约束的边界内。
 */
@Composable
fun AnimateOffsetAsStateExample() {
    var moved by remember {
        mutableStateOf(false)
    }
    val pxToMove = with(LocalDensity.current) {
        100.dp.toPx().roundToInt()
    }
    val offset by animateIntOffsetAsState(
        targetValue = if (moved) {
            IntOffset(pxToMove, pxToMove)
        } else {
            IntOffset.Zero
        },
        label = "offset"
    )
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) {
                moved = !moved
            }
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    Log.d(
                        TAG,
                        "AnimateOffsetAsStateExample: ${placeable.width.toDp()} x ${placeable.height.toDp()}"
                    )
                    layout(placeable.width + offset.x, placeable.height + offset.y) {
                        placeable.placeRelative(offset)
                    }
                }
                .size(100.dp)
                .background(MaterialTheme.colorScheme.secondary)
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.tertiary)
        )
    }
}

@Preview
@Composable
fun AlternatingSlideList() {
    val items = List(50) { "Item #$it" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            // 使用 MutableTransitionState 来确保组件一挂载就触发动画
            val visibleState = remember {
                MutableTransitionState(false).apply { targetState = true }
            }

            // 根据奇偶行决定滑入方向
            val slideInEnter = if (index % 2 == 0) {
                // 偶数行：从左侧滑入
                slideInHorizontally(animationSpec = tween(500)) { fullWidth -> -fullWidth } + fadeIn()
            } else {
                // 奇数行：从右侧滑入
                slideInHorizontally(animationSpec = tween(500)) { fullWidth -> fullWidth } + fadeIn()
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInEnter
            ) {
                ListItemCard(text = item)
            }
        }
    }
}

@Composable
fun ListItemCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val LowOnStartEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

@Preview
@Composable
fun PreciseAlternatingList() {
    val items = List(50) { "Item #$it" }

    LazyColumn {
        itemsIndexed(items) { index, item ->
            // 每一个 Item 维护自己的动画偏量
            val animatable = remember { Animatable(if (index % 2 == 0) -1f else 1f) }
            val alpha = remember { Animatable(0f) }

            // 当 Item 进入 composition 时，启动动画
            LaunchedEffect(Unit) {
                launch {
                    animatable.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 600, easing = LowOnStartEasing)
                    )
                }
                launch {
                    alpha.animateTo(1f, tween(600))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // 将偏量映射到像素位移，0.5f 表示滑过半个屏幕宽度
                        translationX = animatable.value * size.width
                        this.alpha = alpha.value
                    }
                    .padding(8.dp)
            ) {
                ListItemCard(text = item)
            }
        }
    }
}

@Composable
fun AnimateAsStateExample() {
    var isExpanded by remember { mutableStateOf(false) }

    val size by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        label = "size_anim"
    )
    val color by animateColorAsState(
        targetValue = if (isExpanded) Color.Red else Color.Cyan,
        label = "color_anim"
    )
    Box(
        modifier = Modifier
            .size(size)
            .background(color)
            .clickable { isExpanded = !isExpanded }
    )
}

@Composable
fun AnimatedContentExample() {
    var count by remember { mutableIntStateOf(0) }

    Column {
        Button(onClick = { count++ }) {
            Text("加 1")
        }
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            },
            label = "counter"
        ) { targetCount ->
            // 注意：这里必须使用传递进来的 targetCount 渲染 UI
            Text(text = "当前数字: $targetCount", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

enum class BoxState { Collapsed, Expanded }

@Composable
fun TransitionExample() {
    var currentState by remember { mutableStateOf(BoxState.Collapsed) }
    val transition = updateTransition(
        targetState = currentState,
        label = "transition"
    )
    val size by transition.animateDp(label = "size") { state ->
        if (state == BoxState.Collapsed) 100.dp else 200.dp
    }
    val cornerRadius by transition.animateDp(label = "radius") { state ->
        if (state == BoxState.Collapsed) 20.dp else 10.dp
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Blue)
            .clickable {
                currentState =
                    if (currentState == BoxState.Collapsed) BoxState.Expanded else BoxState.Collapsed
            }
    )
}

@Composable
fun AnimatableExample() {
    val offsetX = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Column {
        Button(onClick = {
            scope.launch {
                offsetX.animateTo(100f, tween(500))
                alpha.animateTo(0f, tween(500))
                offsetX.snapTo(0f)
                alpha.snapTo(1f)
            }
        }) {
            Text("开始串行动画")
        }
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = offsetX.value.dp)
                .background(Color.Magenta.copy(alpha = alpha.value))
        )
    }
}

@Composable
fun RadarScanExample() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val radarBrush = Brush.sweepGradient(
        colorStops = arrayOf(
            0.0f to Color.Transparent,
            0.8f to Color.Green.copy(0.2f),
            0.99f to Color.Green.copy(0.7f),
            1f to Color.Transparent
        ),
        center = Offset.Zero
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Green, CircleShape)
            .drawBehind {
                translate(left = size.width / 2, top = size.height / 2) {
                    rotate(degrees = rotation, pivot = Offset.Zero) {
                        drawCircle(brush = radarBrush,
                            center = Offset(0f, 0f),
                            radius = size.width / 2)
                    }
                }
            }
    )
}

@Composable
fun EnterExitAnimationExample() {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "隐藏组件" else "显示组件")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 使用 AnimatedVisibility
        AnimatedVisibility(
            visible = visible,

            // ================= 入场动画组合 =================
            enter = fadeIn(
                // 1. 淡入：初始透明度 0.3，耗时 500ms
                initialAlpha = 0.3f,
                animationSpec = tween(500)
            ) + slideInVertically(
                // 2. 垂直滑入：从下方滑入 (起始位置为自身高度的正值)
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = tween(500)
            ) + expandVertically(
                // 3. 垂直展开：从顶部开始向下展开
                expandFrom = Alignment.Top,
                animationSpec = tween(500)
            ),

            // ================= 退场动画组合 =================
            exit = fadeOut(
                // 1. 淡出
                targetAlpha = 0f,
                animationSpec = tween(300)
            ) + slideOutHorizontally(
                // 2. 向右滑出屏幕：目标位置是自身宽度
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + shrinkVertically(
                // 3. 收缩：向底部收缩消失
                shrinkTowards = Alignment.Bottom,
                animationSpec = tween(300)
            )
        ) {
            // 被动画包裹的 UI 组件
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Blue)
            ) {
                Text(
                    text = "我是一个带华丽动画的 Box",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // 下方的组件，用来演示 Expand/Shrink 会自动推挤布局
        Text("我是下方的文本，上面出现时我会被挤下去！", modifier = Modifier.padding(top = 16.dp))
    }
}

@Preview
@Composable
fun AnimateApiExamplePreview() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimateAsStateExample()
        AnimatedVisibilityExample()
        AnimatedContentExample()
        TransitionExample()
        RadarScanExample()
        EnterExitAnimationExample()
        AnimatableExample()
    }
}