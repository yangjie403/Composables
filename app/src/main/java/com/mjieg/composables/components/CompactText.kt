package com.mjieg.composables.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.requiredWidthIn
import kotlin.math.ceil

/**
 * 自动收缩宽度的 Text 组件。
 * 解决多行文本换行后右侧残留大片空白、导致父容器无法紧贴文字边缘的问题。
 */
@Composable
fun CompactText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val density = LocalDensity.current
    
    // 当 text、fontSize 或 style 发生改变时，重置 maxLineWidth 确保重新计算
    var maxLineWidth by remember(text, fontSize, style) { mutableStateOf(Int.MAX_VALUE) }

    Text(
        text = text,
        modifier = modifier.requiredWidthIn(
            max = if (maxLineWidth == Int.MAX_VALUE) {
                Dp.Unspecified // 首次测量时不限制最大宽度
            } else {
                with(density) { maxLineWidth.toDp() }
            }
        ),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = { textLayoutResult ->
            // 保留用户传入的 onTextLayout 回调触发
            onTextLayout(textLayoutResult)

            // 如果发生换行，计算最宽的一行并收缩宽度
            if (textLayoutResult.lineCount > 1) {
                var longestLine = 0f
                for (i in 0 until textLayoutResult.lineCount) {
                    val lineWidth = textLayoutResult.getLineRight(i) - textLayoutResult.getLineLeft(i)
                    longestLine = maxOf(longestLine, lineWidth)
                }
                val newMaxWidth = ceil(longestLine).toInt()
                if (maxLineWidth != newMaxWidth) {
                    maxLineWidth = newMaxWidth
                }
            }
        },
        style = style.merge(
            TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false // 默认关闭字体额外边距，获得更精准的对齐
                )
            )
        )
    )
}