package com.thisux.kmd.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.thisux.kmd.Emphasis
import com.thisux.kmd.HardBreak
import com.thisux.kmd.Image
import com.thisux.kmd.InlineCode
import com.thisux.kmd.KmdInline
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdStyle
import com.thisux.kmd.Link
import com.thisux.kmd.SoftBreak
import com.thisux.kmd.Strike
import com.thisux.kmd.Strong
import com.thisux.kmd.Text

internal fun List<KmdInline>.toAnnotatedString(
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
): AnnotatedString {
    return buildAnnotatedString {
        appendInlines(this@toAnnotatedString, style, options, onLinkClick)
    }
}

private fun AnnotatedString.Builder.appendInlines(
    inlines: List<KmdInline>,
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
) {
    for (inline in inlines) {
        when (inline) {
            is Text -> append(inline.value)
            is Strong ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInlines(inline.children, style, options, onLinkClick)
                }
            is Emphasis ->
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInlines(inline.children, style, options, onLinkClick)
                }
            is Strike ->
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    appendInlines(inline.children, style, options, onLinkClick)
                }
            is InlineCode ->
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
            is Link -> {
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
                    appendInlines(inline.children, style, options, onLinkClick)
                }
            }
            is Image -> append(inline.alt ?: inline.source)
            SoftBreak -> append(if (options.softBreakAsNewline) "\n" else " ")
            HardBreak -> append('\n')
        }
    }
}
