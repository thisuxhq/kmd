package com.thisux.kmd

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thisux.kmd.internal.ProvideKmdLocals
import com.thisux.kmd.internal.RenderBlock

@Composable
fun LazyKmd(
    markdown: String,
    modifier: Modifier = Modifier,
    style: KmdStyle = KmdDefaults.style(),
    options: KmdOptions = KmdOptions.Default,
    onLinkClick: ((String) -> Unit)? = null,
    imageRenderer: KmdImageRenderer? = null,
    syntaxHighlighter: KmdSyntaxHighlighter? = null,
    renderers: KmdRenderers = KmdRenderers.Default,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val document =
        remember(markdown) {
            KmdEngine().parse(markdown)
        }
    LazyKmdDocument(
        document = document,
        modifier = modifier,
        style = style,
        options = options,
        onLinkClick = onLinkClick,
        imageRenderer = imageRenderer,
        syntaxHighlighter = syntaxHighlighter,
        renderers = renderers,
        state = state,
        contentPadding = contentPadding,
    )
}

@Composable
fun LazyKmd(
    state: KmdState,
    modifier: Modifier = Modifier,
    style: KmdStyle = KmdDefaults.style(),
    options: KmdOptions = KmdOptions.Default,
    onLinkClick: ((String) -> Unit)? = null,
    imageRenderer: KmdImageRenderer? = null,
    syntaxHighlighter: KmdSyntaxHighlighter? = null,
    renderers: KmdRenderers = KmdRenderers.Default,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyKmdDocument(
        document = state.document,
        modifier = modifier,
        style = style,
        options = options,
        onLinkClick = onLinkClick,
        imageRenderer = imageRenderer,
        syntaxHighlighter = syntaxHighlighter,
        renderers = renderers,
        state = listState,
        contentPadding = contentPadding,
    )
}

@Composable
private fun LazyKmdDocument(
    document: KmdDocument,
    modifier: Modifier,
    style: KmdStyle,
    options: KmdOptions,
    onLinkClick: ((String) -> Unit)?,
    imageRenderer: KmdImageRenderer?,
    syntaxHighlighter: KmdSyntaxHighlighter?,
    renderers: KmdRenderers,
    state: LazyListState,
    contentPadding: PaddingValues,
) {
    ProvideKmdLocals(
        style = style,
        options = options,
        onLinkClick = onLinkClick,
        imageRenderer = imageRenderer,
        syntaxHighlighter = syntaxHighlighter,
        renderers = renderers,
    ) {
        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
        ) {
            itemsIndexed(
                items = document.blocks,
                key = { _, block -> block.id.value },
            ) { index, block ->
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
