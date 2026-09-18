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
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thisux.kmd.BlockQuote
import com.thisux.kmd.BulletList
import com.thisux.kmd.CodeBlock
import com.thisux.kmd.Heading
import com.thisux.kmd.HorizontalRule
import androidx.compose.ui.text.TextStyle
import com.thisux.kmd.Image
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdInline
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
    val renderers = LocalKmdRenderers.current
    val streaming = LocalKmdOptions.current.streaming
    val reduced = LocalKmdReducedMotion.current
    val caretId = LocalKmdActiveBlock.current?.caretLeafId()
    val showCaret = streaming.caret && !reduced && block.id == caretId
    val animatedModifier = modifier.streamingEnter(block.id.value)
    when (block) {
        is Heading ->
            RenderOrOverride(renderers.heading, block, animatedModifier) {
                RenderHeading(block, it, showCaret)
            }
        is Paragraph ->
            RenderOrOverride(renderers.paragraph, block, animatedModifier) {
                RenderParagraph(block, it, showCaret)
            }
        is CodeBlock ->
            RenderOrOverride(renderers.codeBlock, block, animatedModifier) {
                RenderCodeBlock(block, it, showCaret)
            }
        is BlockQuote ->
            RenderOrOverride(renderers.quote, block, animatedModifier) { RenderQuote(block, it) }
        is BulletList ->
            RenderOrOverride(renderers.bulletList, block, animatedModifier) { RenderBulletList(block, it) }
        is OrderedList ->
            RenderOrOverride(renderers.orderedList, block, animatedModifier) { RenderOrderedList(block, it) }
        is HorizontalRule ->
            RenderOrOverride(renderers.horizontalRule, block, animatedModifier) { RenderHorizontalRule(it) }
        is Table ->
            RenderOrOverride(renderers.table, block, animatedModifier) { RenderTable(block, it) }
    }
}

@Composable
private fun <T> RenderOrOverride(
    override: (@Composable (T, Modifier) -> Unit)?,
    block: T,
    modifier: Modifier,
    default: @Composable (Modifier) -> Unit,
) {
    if (override != null) {
        override(block, modifier)
    } else {
        default(modifier)
    }
}

@Composable
private fun RenderHeading(
    block: Heading,
    modifier: Modifier,
    showCaret: Boolean = false,
) {
    val style = LocalKmdStyle.current
    val textStyle =
        when (block.level) {
            1 -> style.typography.h1
            2 -> style.typography.h2
            3 -> style.typography.h3
            4 -> style.typography.h4
            5 -> style.typography.h5
            else -> style.typography.h6
        }
    RenderInlines(
        content = block.content,
        textStyle = textStyle.copy(color = style.colors.text),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = style.spacing.headingBottom)
                .semantics(mergeDescendants = true) {
                    heading()
                    kmdHeadingLevel = block.level
                },
        showCaret = showCaret,
    )
}

@Composable
private fun RenderInlines(
    content: List<KmdInline>,
    textStyle: TextStyle,
    modifier: Modifier,
    showCaret: Boolean = false,
) {
    val style = LocalKmdStyle.current
    val options = LocalKmdOptions.current
    val onLinkClick = LocalKmdOnLinkClick.current
    val renderers = LocalKmdRenderers.current
    val layout = content.toInlineLayout(style, options, onLinkClick, renderers)
    KmdText(
        text = layout.text,
        style = textStyle,
        modifier = modifier,
        showCaret = showCaret,
        inlineContent = layout.inlineContent,
    )
}

@Composable
private fun RenderParagraph(
    block: Paragraph,
    modifier: Modifier,
    showCaret: Boolean = false,
) {
    val image = block.content.singleOrNull() as? Image
    if (image != null) {
        RenderImage(image, modifier)
        return
    }
    val style = LocalKmdStyle.current
    RenderInlines(
        content = block.content,
        textStyle = style.typography.paragraph.copy(color = style.colors.text),
        modifier = modifier.fillMaxWidth(),
        showCaret = showCaret,
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
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics {
                    collectionInfo = CollectionInfo(rowCount = items.size, columnCount = 1)
                },
    ) {
        items.forEachIndexed { index, item ->
            Row(
                Modifier
                    .semantics {
                        collectionItemInfo =
                            CollectionItemInfo(
                                rowIndex = index,
                                rowSpan = 1,
                                columnIndex = 0,
                                columnSpan = 1,
                            )
                    }
                    .padding(
                        start = style.list.indent,
                        bottom = if (index == items.lastIndex) 0.dp else style.list.itemSpacing,
                    ),
                verticalAlignment = Alignment.Top,
            ) {
                val checked = item.checked
                if (checked != null) {
                    val checkboxModifier = Modifier.width(style.list.markerWidth).padding(top = 4.dp)
                    val checkbox = LocalKmdRenderers.current.checkbox
                    if (checkbox != null) {
                        checkbox(checked, checkboxModifier)
                    } else {
                        TaskMarker(checked = checked, modifier = checkboxModifier)
                    }
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
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
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
    val rows = buildList {
        add(block.header)
        addAll(block.rows)
    }
    val columnCount = block.header.cells.size
    if (columnCount == 0) return
    val shape = RoundedCornerShape(6.dp)
    val dividerColor = style.colors.divider

    DisableSelection {
        Box(
            modifier
                .fillMaxWidth()
                .clip(shape)
                .border(1.dp, dividerColor, shape)
                .horizontalScroll(rememberScrollState())
                .semantics {
                    collectionInfo = CollectionInfo(rowCount = rows.size, columnCount = columnCount)
                },
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
                            .semantics {
                                collectionItemInfo =
                                    CollectionItemInfo(
                                        rowIndex = rowIndex,
                                        rowSpan = 1,
                                        columnIndex = column,
                                        columnSpan = 1,
                                    )
                            }
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
                            RenderInlines(
                                content = cell.content,
                                textStyle =
                                    style.typography.paragraph.copy(
                                        color = style.colors.text,
                                        fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                modifier = Modifier,
                            )
                        }
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
    val fromRegistry = LocalKmdRenderers.current.image
    if (fromRegistry != null) {
        fromRegistry(image, modifier.fillMaxWidth())
        return
    }
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
        modifier =
            modifier.semantics {
                role = Role.Image
            },
    )
}
