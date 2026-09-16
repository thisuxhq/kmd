package com.thisux.kmd

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thisux.kmd.internal.RenderBlock

class KmdRenderers internal constructor(
    internal val heading: (@Composable (Heading) -> Unit)? = null,
    internal val paragraph: (@Composable (Paragraph) -> Unit)? = null,
    internal val codeBlock: (@Composable (CodeBlock) -> Unit)? = null,
    internal val quote: (@Composable (BlockQuote) -> Unit)? = null,
    internal val bulletList: (@Composable (BulletList) -> Unit)? = null,
    internal val orderedList: (@Composable (OrderedList) -> Unit)? = null,
    internal val table: (@Composable (Table) -> Unit)? = null,
    internal val horizontalRule: (@Composable (HorizontalRule) -> Unit)? = null,
    internal val image: (@Composable (Image) -> Unit)? = null,
) {
    companion object {
        val Default: KmdRenderers = KmdRenderers()
    }
}

class KmdRenderersBuilder @PublishedApi internal constructor() {
    private var heading: (@Composable (Heading) -> Unit)? = null
    private var paragraph: (@Composable (Paragraph) -> Unit)? = null
    private var codeBlock: (@Composable (CodeBlock) -> Unit)? = null
    private var quote: (@Composable (BlockQuote) -> Unit)? = null
    private var bulletList: (@Composable (BulletList) -> Unit)? = null
    private var orderedList: (@Composable (OrderedList) -> Unit)? = null
    private var table: (@Composable (Table) -> Unit)? = null
    private var horizontalRule: (@Composable (HorizontalRule) -> Unit)? = null
    private var image: (@Composable (Image) -> Unit)? = null

    fun heading(renderer: @Composable (Heading) -> Unit) {
        heading = renderer
    }

    fun paragraph(renderer: @Composable (Paragraph) -> Unit) {
        paragraph = renderer
    }

    fun codeBlock(renderer: @Composable (CodeBlock) -> Unit) {
        codeBlock = renderer
    }

    fun quote(renderer: @Composable (BlockQuote) -> Unit) {
        quote = renderer
    }

    fun bulletList(renderer: @Composable (BulletList) -> Unit) {
        bulletList = renderer
    }

    fun orderedList(renderer: @Composable (OrderedList) -> Unit) {
        orderedList = renderer
    }

    fun table(renderer: @Composable (Table) -> Unit) {
        table = renderer
    }

    fun horizontalRule(renderer: @Composable (HorizontalRule) -> Unit) {
        horizontalRule = renderer
    }

    fun image(renderer: @Composable (Image) -> Unit) {
        image = renderer
    }

    internal fun build(): KmdRenderers {
        return KmdRenderers(
            heading = heading,
            paragraph = paragraph,
            codeBlock = codeBlock,
            quote = quote,
            bulletList = bulletList,
            orderedList = orderedList,
            table = table,
            horizontalRule = horizontalRule,
            image = image,
        )
    }
}

fun KmdRenderers(builder: KmdRenderersBuilder.() -> Unit): KmdRenderers {
    return KmdRenderersBuilder().apply(builder).build()
}

@Composable
fun KmdBlockContent(
    block: KmdBlock,
    modifier: Modifier = Modifier,
) {
    RenderBlock(block, modifier)
}
