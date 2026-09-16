package com.thisux.kmd.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thisux.kmd.BlockQuote
import com.thisux.kmd.BulletList
import com.thisux.kmd.CodeBlock
import com.thisux.kmd.Heading
import com.thisux.kmd.HorizontalRule
import com.thisux.kmd.Image
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdListItem
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdStyle
import com.thisux.kmd.OrderedList
import com.thisux.kmd.Paragraph
import com.thisux.kmd.Table
import com.thisux.kmd.TableRow

@Composable
internal fun RenderBlock(
    block: KmdBlock,
    modifier: Modifier = Modifier,
) {
    when (block) {
        is Heading -> RenderHeading(block, modifier)
        is Paragraph -> RenderParagraph(block, modifier)
        is CodeBlock -> RenderCodeBlock(block, modifier)
        is BlockQuote -> RenderQuote(block, modifier)
        is BulletList -> RenderBulletList(block, modifier)
        is OrderedList -> RenderOrderedList(block, modifier)
        is HorizontalRule -> RenderHorizontalRule(modifier)
        is Table -> RenderTable(block, modifier)
    }
}

@Composable
private fun RenderHeading(
    block: Heading,
    modifier: Modifier,
) {
    val style = LocalKmdStyle.current
    val options = LocalKmdOptions.current
    val onLinkClick = LocalKmdOnLinkClick.current
    val textStyle =
        when (block.level) {
            1 -> style.typography.h1
            2 -> style.typography.h2
            3 -> style.typography.h3
            4 -> style.typography.h4
            5 -> style.typography.h5
            else -> style.typography.h6
        }
    BasicText(
        text = block.content.toAnnotatedString(style, options, onLinkClick),
        style = textStyle.copy(color = style.colors.text),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = style.spacing.headingBottom)
                .semantics { heading() },
    )
}

@Composable
private fun RenderParagraph(
    block: Paragraph,
    modifier: Modifier,
) {
    val image = block.content.singleOrNull() as? Image
    if (image != null) {
        RenderImage(image, modifier)
        return
    }
    val style = LocalKmdStyle.current
    val options = LocalKmdOptions.current
    val onLinkClick = LocalKmdOnLinkClick.current
    BasicText(
        text = block.content.toAnnotatedString(style, options, onLinkClick),
        style = style.typography.paragraph.copy(color = style.colors.text),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun RenderQuote(
    block: BlockQuote,
    modifier: Modifier,
) {
    val style = LocalKmdStyle.current
    val quote = style.quote
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(quote.background),
    ) {
        Box(
            Modifier
                .width(quote.barWidth)
                .fillMaxHeight()
                .background(quote.barColor),
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = quote.contentPadding),
        ) {
            block.children.forEachIndexed { index, child ->
                RenderBlock(
                    block = child,
                    modifier =
                        Modifier.padding(
                            bottom = if (index == block.children.lastIndex) 0.dp else style.spacing.block,
                        ),
                )
            }
        }
    }
}

@Composable
private fun RenderBulletList(
    block: BulletList,
    modifier: Modifier,
) {
    val bullet = LocalKmdStyle.current.list.bullet
    RenderList(
        items = block.items,
        modifier = modifier,
        marker = { _ -> bullet },
    )
}

@Composable
private fun RenderOrderedList(
    block: OrderedList,
    modifier: Modifier,
) {
    RenderList(
        items = block.items,
        modifier = modifier,
        marker = { index -> "${block.start + index}." },
    )
}

@Composable
private fun RenderList(
    items: List<KmdListItem>,
    modifier: Modifier,
    marker: (Int) -> String,
) {
    val style = LocalKmdStyle.current
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            Row(
                Modifier.padding(
                    start = style.list.indent,
                    bottom = if (index == items.lastIndex) 0.dp else style.list.itemSpacing,
                ),
                verticalAlignment = Alignment.Top,
            ) {
                val checked = item.checked
                if (checked != null) {
                    TaskMarker(
                        checked = checked,
                        modifier = Modifier.width(style.list.markerWidth).padding(top = 4.dp),
                    )
                } else {
                    BasicText(
                        text = marker(index),
                        style = style.typography.paragraph.copy(color = style.colors.text),
                        modifier = Modifier.width(style.list.markerWidth),
                    )
                }
                Column(Modifier.weight(1f)) {
                    item.children.forEachIndexed { childIndex, child ->
                        RenderBlock(
                            block = child,
                            modifier =
                                Modifier.padding(
                                    bottom =
                                        if (childIndex == item.children.lastIndex) {
                                            0.dp
                                        } else {
                                            style.spacing.block / 2
                                        },
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskMarker(
    checked: Boolean,
    modifier: Modifier,
) {
    val style = LocalKmdStyle.current
    Box(
        modifier =
            modifier.semantics {
                role = Role.Checkbox
                selected = checked
                contentDescription = if (checked) "Completed" else "Not completed"
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(16.dp)
                .border(1.dp, style.colors.text, RoundedCornerShape(3.dp))
                .background(
                    if (checked) style.colors.text else style.colors.divider.copy(alpha = 0f),
                    RoundedCornerShape(3.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                BasicText(
                    text = "✓",
                    style = style.typography.inlineCode.copy(color = style.colors.codeBackground),
                )
            }
        }
    }
}

@Composable
private fun RenderTable(
    block: Table,
    modifier: Modifier,
) {
    val style = LocalKmdStyle.current
    val options = LocalKmdOptions.current
    val onLinkClick = LocalKmdOnLinkClick.current
    val rows = buildList {
        add(block.header)
        addAll(block.rows)
    }
    val columnCount = block.header.cells.size
    if (columnCount == 0) return
    val shape = RoundedCornerShape(6.dp)
    val dividerColor = style.colors.divider

    Box(
        modifier
            .horizontalScroll(rememberScrollState())
            .clip(shape)
            .border(1.dp, dividerColor, shape),
    ) {
        TableGrid(
            columnCount = columnCount,
            rowCount = rows.size,
        ) {
            rows.forEachIndexed { rowIndex, row ->
                val header = rowIndex == 0
                val background =
                    when {
                        header -> style.colors.codeBackground
                        (rowIndex - 1) % 2 == 1 -> dividerColor.copy(alpha = 0.25f)
                        else -> Color.Transparent
                    }
                val lastRow = rowIndex == rows.lastIndex
                repeat(columnCount) { column ->
                    val cell = row.cells.getOrNull(column)
                    Box(
                        Modifier
                            .background(background)
                            .drawBehind {
                                if (!lastRow) {
                                    val stroke = 1.dp.toPx()
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(0f, size.height - stroke / 2f),
                                        end = Offset(size.width, size.height - stroke / 2f),
                                        strokeWidth = stroke,
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (cell != null) {
                            BasicText(
                                text = cell.content.toAnnotatedString(style, options, onLinkClick),
                                style =
                                    style.typography.paragraph.copy(
                                        color = style.colors.text,
                                        fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableGrid(
    columnCount: Int,
    rowCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, _ ->
        val columnWidths = IntArray(columnCount)
        val rowHeights = IntArray(rowCount)
        measurables.forEachIndexed { index, measurable ->
            val column = index % columnCount
            columnWidths[column] =
                maxOf(columnWidths[column], measurable.maxIntrinsicWidth(Constraints.Infinity))
        }
        measurables.forEachIndexed { index, measurable ->
            val column = index % columnCount
            val row = index / columnCount
            rowHeights[row] =
                maxOf(rowHeights[row], measurable.minIntrinsicHeight(columnWidths[column]))
        }
        val placeables =
            measurables.mapIndexed { index, measurable ->
                val column = index % columnCount
                val row = index / columnCount
                measurable.measure(Constraints.fixed(columnWidths[column], rowHeights[row]))
            }
        layout(columnWidths.sum(), rowHeights.sum()) {
            var y = 0
            for (row in 0 until rowCount) {
                var x = 0
                for (column in 0 until columnCount) {
                    placeables[row * columnCount + column].place(x, y)
                    x += columnWidths[column]
                }
                y += rowHeights[row]
            }
        }
    }
}

@Composable
private fun RenderHorizontalRule(modifier: Modifier) {
    val style = LocalKmdStyle.current
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(style.colors.divider),
    )
}

@Composable
private fun RenderImage(
    image: Image,
    modifier: Modifier,
) {
    val renderer = LocalKmdImageRenderer.current
    if (renderer != null) {
        renderer.render(image, modifier.fillMaxWidth())
        return
    }
    val style = LocalKmdStyle.current
    val description = image.alt?.ifBlank { null } ?: image.source
    BasicText(
        text = description,
        style =
            style.typography.paragraph.copy(
                color = style.colors.link,
                fontStyle = FontStyle.Italic,
            ),
        modifier = modifier.semantics { contentDescription = description },
    )
}
