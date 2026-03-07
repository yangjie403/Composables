package com.mjieg.composables.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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