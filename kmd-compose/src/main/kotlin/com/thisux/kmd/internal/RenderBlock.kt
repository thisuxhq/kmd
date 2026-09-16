package com.thisux.kmd.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicText
import com.thisux.kmd.BlockQuote
import com.thisux.kmd.BulletList
import com.thisux.kmd.CodeBlock
import com.thisux.kmd.Heading
import com.thisux.kmd.HorizontalRule
import com.thisux.kmd.Image
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdListItem
import com.thisux.kmd.OrderedList
import com.thisux.kmd.Paragraph

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
            ) {
                BasicText(
                    text = marker(index),
                    style = style.typography.paragraph.copy(color = style.colors.text),
                    modifier = Modifier.width(style.list.markerWidth),
                )
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
