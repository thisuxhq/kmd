package com.thisux.kmd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.thisux.kmd.internal.RenderDocument

@Composable
fun Kmd(
    markdown: String,
    modifier: Modifier = Modifier,
    style: KmdStyle = KmdDefaults.style(),
    options: KmdOptions = KmdOptions.Default,
    onLinkClick: ((String) -> Unit)? = null,
    imageRenderer: KmdImageRenderer? = null,
    syntaxHighlighter: KmdSyntaxHighlighter? = null,
    renderers: KmdRenderers = KmdRenderers.Default,
    extensions: List<KmdExtension> = emptyList(),
) {
    val document =
        remember(markdown, extensions) {
            KmdEngine(extensions = extensions).parse(markdown)
        }
    RenderDocument(
        document = document,
        modifier = modifier,
        style = style,
        options = options,
        onLinkClick = onLinkClick,
        imageRenderer = imageRenderer,
        syntaxHighlighter = syntaxHighlighter,
        renderers = renderers,
    )
}

@Composable
fun Kmd(
    state: KmdState,
    modifier: Modifier = Modifier,
    style: KmdStyle = KmdDefaults.style(),
    options: KmdOptions = KmdOptions.Default,
    onLinkClick: ((String) -> Unit)? = null,
    imageRenderer: KmdImageRenderer? = null,
    syntaxHighlighter: KmdSyntaxHighlighter? = null,
    renderers: KmdRenderers = KmdRenderers.Default,
) {
    val snapshot = state.snapshot()
    RenderDocument(
        document = snapshot.document,
        modifier = modifier,
        style = style,
        options = options,
        onLinkClick = onLinkClick,
        imageRenderer = imageRenderer,
        syntaxHighlighter = syntaxHighlighter,
        renderers = renderers,
        activeBlock = snapshot.activeBlock,
    )
}
