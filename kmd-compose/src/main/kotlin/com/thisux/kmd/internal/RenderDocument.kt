package com.thisux.kmd.internal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thisux.kmd.KmdDocument
import com.thisux.kmd.KmdImageRenderer
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdStyle
import com.thisux.kmd.KmdSyntaxHighlighter

@Composable
internal fun RenderDocument(
    document: KmdDocument,
    modifier: Modifier,
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
    imageRenderer: KmdImageRenderer?,
    syntaxHighlighter: KmdSyntaxHighlighter?,
) {
    CompositionLocalProvider(
        LocalKmdStyle provides style,
        LocalKmdOptions provides options,
        LocalKmdOnLinkClick provides onLinkClick,
        LocalKmdImageRenderer provides imageRenderer,
        LocalKmdSyntaxHighlighter provides syntaxHighlighter,
    ) {
        Column(modifier = modifier) {
            document.blocks.forEachIndexed { index, block ->
                key(block.id.value) {
                    RenderBlock(
                        block = block,
                        modifier =
                            Modifier.padding(
                                bottom = if (index == document.blocks.lastIndex) 0.dp else style.spacing.block,
                            ),
                    )
                }
            }
        }
    }
}
