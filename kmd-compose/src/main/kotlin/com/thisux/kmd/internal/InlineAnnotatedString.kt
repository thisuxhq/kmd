package com.thisux.kmd.internal

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.em
import com.thisux.kmd.Emphasis
import com.thisux.kmd.HardBreak
import com.thisux.kmd.Image
import com.thisux.kmd.InlineCode
import com.thisux.kmd.KmdInline
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdRenderers
import com.thisux.kmd.KmdStyle
import com.thisux.kmd.Link
import com.thisux.kmd.SoftBreak
import com.thisux.kmd.Strike
import com.thisux.kmd.Strong
import com.thisux.kmd.Text

internal data class InlineLayout(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

internal fun List<KmdInline>.toInlineLayout(
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
    renderers: KmdRenderers = KmdRenderers.Default,
): InlineLayout {
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    var nextId = 0
    val text =
        buildAnnotatedString {
            appendInlines(
                inlines = this@toInlineLayout,
                style = style,
                options = options,
                onLinkClick = onLinkClick,
                renderers = renderers,
                inlineContent = inlineContent,
                nextId = { nextId++ },
            )
        }
    return InlineLayout(text, inlineContent)
}

internal fun List<KmdInline>.toAnnotatedString(
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
): AnnotatedString = toInlineLayout(style, options, onLinkClick).text

internal fun List<KmdInline>.plainText(): String {
    return buildString {
        fun walk(items: List<KmdInline>) {
            for (item in items) {
                when (item) {
                    is Text -> append(item.value)
                    is Strong -> walk(item.children)
                    is Emphasis -> walk(item.children)
                    is Strike -> walk(item.children)
                    is InlineCode -> append(item.code)
                    is Link -> walk(item.children)
                    is Image -> append(item.alt.orEmpty())
                    SoftBreak -> append(' ')
                    HardBreak -> append('\n')
                }
            }
        }
        walk(this@plainText)
    }
}

private fun AnnotatedString.Builder.appendInlines(
    inlines: List<KmdInline>,
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
    renderers: KmdRenderers,
    inlineContent: MutableMap<String, InlineTextContent>,
    nextId: () -> Int,
) {
    for (inline in inlines) {
        when (inline) {
            is Text -> append(inline.value)
            is Strong ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInlines(inline.children, style, options, onLinkClick, renderers, inlineContent, nextId)
                }
            is Emphasis ->
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInlines(inline.children, style, options, onLinkClick, renderers, inlineContent, nextId)
                }
            is Strike ->
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    appendInlines(inline.children, style, options, onLinkClick, renderers, inlineContent, nextId)
                }
            is InlineCode -> {
                val override = renderers.inlineCode
                if (override != null) {
                    appendPlaceholder(
                        id = "code-${nextId()}",
                        fallback = inline.code,
                        inlineContent = inlineContent,
                    ) { modifier ->
                        override(inline, modifier)
                    }
                } else {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = style.colors.codeBackground,
                            color =
                                style.typography.inlineCode.color.takeIf { it != Color.Unspecified }
                                    ?: style.colors.text,
                        ),
                    ) {
                        append(inline.code)
                    }
                }
            }
            is Link -> {
                val override = renderers.link
                if (override != null) {
                    val fallback = inline.children.plainText().ifEmpty { inline.destination }
                    appendPlaceholder(
                        id = "link-${nextId()}",
                        fallback = fallback,
                        inlineContent = inlineContent,
                    ) { modifier ->
                        override(inline, modifier)
                    }
                } else {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = inline.destination,
                            styles =
                                TextLinkStyles(
                                    style =
                                        SpanStyle(
                                            color = style.colors.link,
                                            textDecoration = TextDecoration.Underline,
                                        ),
                                ),
                            linkInteractionListener = { onLinkClick?.invoke(inline.destination) },
                        ),
                    ) {
                        appendInlines(
                            inline.children,
                            style,
                            options,
                            onLinkClick,
                            renderers,
                            inlineContent,
                            nextId,
                        )
                    }
                }
            }
            is Image -> append(inline.alt ?: inline.source)
            SoftBreak -> append(if (options.softBreakAsNewline) "\n" else " ")
            HardBreak -> append('\n')
        }
    }
}

private fun AnnotatedString.Builder.appendPlaceholder(
    id: String,
    fallback: String,
    inlineContent: MutableMap<String, InlineTextContent>,
    content: @Composable (Modifier) -> Unit,
) {
    val widthEm = fallback.length.coerceAtLeast(1).toFloat()
    inlineContent[id] =
        InlineTextContent(
            placeholder =
                Placeholder(
                    width = widthEm.em,
                    height = 1.2.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                ),
            children = { content(Modifier) },
        )
    appendInlineContent(id, fallback.ifEmpty { " " })
}
