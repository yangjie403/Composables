package com.mjieg.markdown

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Image as MarkdownImageNode
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text as MarkdownText
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import ru.noties.jlatexmath.JLatexMathAndroid
import ru.noties.jlatexmath.awt.AndroidGraphics2D
import java.util.regex.Pattern

/**
 * Renders CommonMark/GFM content with native Jetpack Compose.
 *
 * The component owns vertical scrolling by default. Set [scrollable] to false when embedding it
 * inside a caller-owned LazyColumn or another scroll container.
 */
@Composable
public fun Markdown(
    content: String,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true
) {
    val parser = remember { markdownParser() }
    val document = remember(content) { parser.parse(content) }
    val contentModifier = if (scrollable) {
        modifier.verticalScroll(rememberScrollState())
    } else {
        modifier
    }

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var node = document.firstChild
        while (node != null) {
            MarkdownBlock(node)
            node = node.next
        }
    }
}

private fun markdownParser(): Parser = Parser.builder()
    .extensions(
        listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            AutolinkExtension.create()
        )
    )
    .build()

@Composable
private fun MarkdownBlock(node: Node) {
    when (node) {
        is Heading -> MarkdownHeading(node)
        is Paragraph -> MarkdownParagraph(node)
        is FencedCodeBlock -> MarkdownCodeBlock(node.info, node.literal)
        is IndentedCodeBlock -> MarkdownCodeBlock(null, node.literal)
        is BlockQuote -> MarkdownBlockQuote(node)
        is BulletList -> MarkdownBulletList(node)
        is OrderedList -> MarkdownOrderedList(node)
        is ThematicBreak -> HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        is TableBlock -> MarkdownTable(node)
    }
}

@Composable
private fun MarkdownParagraph(paragraph: Paragraph) {
    val firstChild = paragraph.firstChild
    if (firstChild is MarkdownImageNode) {
        MarkdownImage(firstChild.destination, firstChild.title)
        return
    }

    val rawText = (firstChild as? MarkdownText)?.literal?.trim().orEmpty()
    if (rawText.startsWith("$$") && rawText.endsWith("$$") && rawText.length >= 4) {
        MarkdownBlockMath(rawText.removePrefix("$$").removeSuffix("$$").trim())
        return
    }

    Text(
        text = buildInlineMarkdown(paragraph),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 24.sp
    )
}

@Composable
private fun MarkdownBlockMath(latex: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val textSizePx = with(density) { 22.sp.toPx() }
    val formulaBitmap = remember(context, latex, textSizePx) {
        LatexRenderer.renderToBitmap(context, latex, textSizePx)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        if (formulaBitmap != null) {
            Image(
                bitmap = formulaBitmap,
                contentDescription = latex,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        } else {
            Text(text = "$$ $latex $$", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MarkdownImage(imageUrl: String, altText: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = altText ?: "image",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentScale = ContentScale.FillWidth
        )
        if (!altText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = altText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MarkdownCodeBlock(language: String?, code: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    val highlightedText = remember(code, language) {
        SyntaxHighlighter.highlight(code.trimEnd())
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF282C34),
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF21252B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language?.takeIf { it.isNotBlank() } ?: "code",
                    fontSize = 12.sp,
                    color = Color(0xFFABB2BF),
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("code", code)))
                            isCopied = true
                            Toast.makeText(context, "代码已复制", Toast.LENGTH_SHORT).show()
                            delay(2_000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                        contentDescription = "复制代码",
                        tint = if (isCopied) Color(0xFF98C379) else Color(0xFFABB2BF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = highlightedText,
                    color = Color(0xFFABB2BF),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MarkdownHeading(heading: Heading) {
    val (style, topPadding) = when (heading.level) {
        1 -> MaterialTheme.typography.headlineLarge to 14.dp
        2 -> MaterialTheme.typography.headlineMedium to 12.dp
        3 -> MaterialTheme.typography.headlineSmall to 10.dp
        4 -> MaterialTheme.typography.titleLarge to 8.dp
        else -> MaterialTheme.typography.titleMedium to 6.dp
    }
    Text(
        text = buildInlineMarkdown(heading),
        style = style.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = topPadding, bottom = 4.dp)
    )
}

@Composable
private fun MarkdownBlockQuote(blockQuote: BlockQuote) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            var child = blockQuote.firstChild
            while (child != null) {
                MarkdownBlock(child)
                child = child.next
            }
        }
    }
}

@Composable
private fun MarkdownBulletList(bulletList: BulletList) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var item = bulletList.firstChild
        while (item != null) {
            if (item is ListItem) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "• ", fontWeight = FontWeight.Bold)
                    Column {
                        var child = item.firstChild
                        while (child != null) {
                            MarkdownBlock(child)
                            child = child.next
                        }
                    }
                }
            }
            item = item.next
        }
    }
}

@Composable
private fun MarkdownOrderedList(orderedList: OrderedList) {
    var index = orderedList.markerStartNumber
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var item = orderedList.firstChild
        while (item != null) {
            if (item is ListItem) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "${index++}. ", fontWeight = FontWeight.SemiBold)
                    Column {
                        var child = item.firstChild
                        while (child != null) {
                            MarkdownBlock(child)
                            child = child.next
                        }
                    }
                }
            }
            item = item.next
        }
    }
}

@Composable
private fun MarkdownTable(tableBlock: TableBlock) {
    val rows = remember(tableBlock) { tableBlockRows(tableBlock) }
    val columnCount = rows.maxOfOrNull { it.cells.size } ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        if (columnCount > 0) {
            Layout(
                content = {
                    rows.forEach { row ->
                        repeat(columnCount) { columnIndex ->
                            MarkdownTableCell(row.cells.getOrNull(columnIndex))
                        }
                    }
                },
                modifier = Modifier
            ) { measurables, constraints ->
                val columnWidths = IntArray(columnCount)
                measurables.forEachIndexed { index, measurable ->
                    columnWidths[index % columnCount] = maxOf(
                        columnWidths[index % columnCount],
                        measurable.maxIntrinsicWidth(Constraints.Infinity)
                    )
                }
                val placeables = measurables.mapIndexed { index, measurable ->
                    measurable.measure(
                        Constraints(
                            minWidth = columnWidths[index % columnCount],
                            maxWidth = columnWidths[index % columnCount],
                            minHeight = 0,
                            maxHeight = constraints.maxHeight
                        )
                    )
                }
                val rowHeights = IntArray(rows.size)
                placeables.forEachIndexed { index, placeable ->
                    val rowIndex = index / columnCount
                    rowHeights[rowIndex] = maxOf(rowHeights[rowIndex], placeable.height)
                }
                layout(columnWidths.sum(), rowHeights.sum()) {
                    var y = 0
                    var placeableIndex = 0
                    rowHeights.forEach { rowHeight ->
                        var x = 0
                        repeat(columnCount) { columnIndex ->
                            placeables[placeableIndex++].placeRelative(x, y)
                            x += columnWidths[columnIndex]
                        }
                        y += rowHeight
                    }
                }
            }
        }
    }
}

private data class MarkdownTableRow(val cells: List<TableCell>)

private fun tableBlockRows(tableBlock: TableBlock): List<MarkdownTableRow> {
    val rows = mutableListOf<MarkdownTableRow>()
    var section = tableBlock.firstChild
    while (section != null) {
        if (section is TableHead || section is TableBody) {
            var row = section.firstChild
            while (row != null) {
                if (row is TableRow) {
                    val cells = mutableListOf<TableCell>()
                    var cell = row.firstChild
                    while (cell != null) {
                        if (cell is TableCell) cells += cell
                        cell = cell.next
                    }
                    rows += MarkdownTableRow(cells)
                }
                row = row.next
            }
        }
        section = section.next
    }
    return rows
}

@Composable
private fun MarkdownTableCell(cell: TableCell?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            .background(
                if (cell?.isHeader == true) MaterialTheme.colorScheme.surfaceContainerHighest
                else Color.Transparent
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        if (cell != null) {
            Text(
                text = buildInlineMarkdown(cell),
                fontWeight = if (cell.isHeader) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
@OptIn(ExperimentalTextApi::class)
private fun buildInlineMarkdown(node: Node): AnnotatedString {
    return AnnotatedString.Builder().apply {
        var child = node.firstChild
        while (child != null) {
            when (child) {
                is MarkdownText -> append(child.literal)
                is StrongEmphasis -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(buildInlineMarkdown(child))
                    pop()
                }
                is Emphasis -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(buildInlineMarkdown(child))
                    pop()
                }
                is Strikethrough -> {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(buildInlineMarkdown(child))
                    pop()
                }
                is Code -> {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = MaterialTheme.colorScheme.surfaceContainerHighest,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                    append(" ${child.literal} ")
                    pop()
                }
                is Link -> {
                    pushLink(
                        LinkAnnotation.Url(
                            url = child.destination,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        )
                    )
                    append(buildInlineMarkdown(child))
                    pop()
                }
                is SoftLineBreak, is HardLineBreak -> append("\n")
                else -> append(buildInlineMarkdown(child))
            }
            child = child.next
        }
    }.toAnnotatedString()
}

private object SyntaxHighlighter {
    private val keywords = listOf(
        "val", "var", "fun", "class", "interface", "package", "import", "return", "if", "else",
        "for", "while", "type", "func", "struct", "go", "public", "private", "const", "def"
    )
    private val keywordPattern = Pattern.compile("\\b(${keywords.joinToString("|")})\\b")
    private val stringPattern = Pattern.compile("(\".*?\"|'.*?')")
    private val numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
    private val commentPattern = Pattern.compile("(//.*|/\\*[\\s\\S]*?\\*/|#.*)")
    private val functionPattern = Pattern.compile("\\b([a-zA-Z_]\\w*)\\s*(?=\\()")

    fun highlight(code: String): AnnotatedString = buildAnnotatedString {
        append(code)
        fun applyRegex(pattern: Pattern, color: Color, isBold: Boolean = false) {
            val matcher = pattern.matcher(code)
            while (matcher.find()) {
                addStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                    ),
                    matcher.start(),
                    matcher.end()
                )
            }
        }
        applyRegex(functionPattern, Color(0xFF61AFEF))
        applyRegex(numberPattern, Color(0xFFD19A66))
        applyRegex(keywordPattern, Color(0xFFC678DD), isBold = true)
        applyRegex(stringPattern, Color(0xFF98C379))
        applyRegex(commentPattern, Color(0xFF7F848E))
    }
}

private object LatexRenderer {
    fun renderToBitmap(context: Context, latex: String, textSizePx: Float): ImageBitmap? {
        return try {
            JLatexMathAndroid.init(context)
            val icon = TeXFormula(latex).createTeXIcon(TeXConstants.STYLE_DISPLAY, textSizePx)
            val bitmap = Bitmap.createBitmap(
                icon.iconWidth.coerceAtLeast(1),
                icon.iconHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val graphics = AndroidGraphics2D().apply { setCanvas(Canvas(bitmap)) }
            icon.paintIcon(null, graphics, 0, 0)
            bitmap.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}
