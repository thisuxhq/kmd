package com.thisux.kmd.internal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdDocument
import com.thisux.kmd.KmdImageRenderer
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdRenderers
import com.thisux.kmd.KmdStyle
import com.thisux.kmd.KmdSyntaxHighlighter

@Composable
internal fun ProvideKmdLocals(
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
    imageRenderer: KmdImageRenderer?,
    syntaxHighlighter: KmdSyntaxHighlighter?,
    renderers: KmdRenderers,
    activeBlock: KmdBlock? = null,
    document: KmdDocument? = null,
    content: @Composable () -> Unit,
) {
    val tracker = remember { BlockEnterTracker() }
    val caret = remember { KmdCaret() }
    val caretId = activeBlock?.caretLeafId()
    // Written after composition so only blocks whose caret flips recompose.
    SideEffect { caret.blockId = caretId }
    if (document != null) {
        tracker.onFrame(document.blocks.map { it.id.value })
    }
    val reduced = rememberReducedMotion()
    CompositionLocalProvider(
        LocalKmdStyle provides style,
        LocalKmdOptions provides options,
        LocalKmdOnLinkClick provides onLinkClick,
        LocalKmdImageRenderer provides imageRenderer,
        LocalKmdSyntaxHighlighter provides syntaxHighlighter,
        LocalKmdRenderers provides renderers,
        LocalKmdCaret provides caret,
        LocalBlockEnterTracker provides tracker,
        LocalKmdReducedMotion provides reduced,
        content = content,
    )
}

@Composable
internal fun RenderDocument(
    document: KmdDocument,
    modifier: Modifier,
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
    imageRenderer: KmdImageRenderer?,
    syntaxHighlighter: KmdSyntaxHighlighter?,
    renderers: KmdRenderers,
    activeBlock: KmdBlock? = null,
) {
    ProvideKmdLocals(
        style = style,
        options = options,
        onLinkClick = onLinkClick,
        imageRenderer = imageRenderer,
        syntaxHighlighter = syntaxHighlighter,
        renderers = renderers,
        activeBlock = activeBlock,
        document = document,
    ) {
        Column(modifier = modifier) {
            document.blocks.forEachIndexed { index, block ->
                key(block.id.value) {
                    RenderBlock(
                        block = block,
                        modifier =
                            Modifier.padding(
                                top = if (index == 0) 0.dp else style.spacing.block,
                            ),
                    )
                }
            }
        }
    }
}
