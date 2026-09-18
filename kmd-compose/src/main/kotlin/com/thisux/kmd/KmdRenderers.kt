package com.thisux.kmd

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thisux.kmd.internal.RenderBlock

class KmdRenderers internal constructor(
    internal val heading: (@Composable (Heading, Modifier) -> Unit)? = null,
    internal val paragraph: (@Composable (Paragraph, Modifier) -> Unit)? = null,
    internal val codeBlock: (@Composable (CodeBlock, Modifier) -> Unit)? = null,
    internal val quote: (@Composable (BlockQuote, Modifier) -> Unit)? = null,
    internal val bulletList: (@Composable (BulletList, Modifier) -> Unit)? = null,
    internal val orderedList: (@Composable (OrderedList, Modifier) -> Unit)? = null,
    internal val table: (@Composable (Table, Modifier) -> Unit)? = null,
    internal val horizontalRule: (@Composable (HorizontalRule, Modifier) -> Unit)? = null,
    internal val image: (@Composable (Image, Modifier) -> Unit)? = null,
    internal val link: (@Composable (Link, Modifier) -> Unit)? = null,
    internal val inlineCode: (@Composable (InlineCode, Modifier) -> Unit)? = null,
    internal val checkbox: (@Composable (Boolean, Modifier) -> Unit)? = null,
    internal val custom: Map<String, @Composable (CustomBlock, Modifier) -> Unit> = emptyMap(),
) {
    companion object {
        val Default: KmdRenderers = KmdRenderers()
    }
}

class KmdRenderersBuilder @PublishedApi internal constructor() {
    private var heading: (@Composable (Heading, Modifier) -> Unit)? = null
    private var paragraph: (@Composable (Paragraph, Modifier) -> Unit)? = null
    private var codeBlock: (@Composable (CodeBlock, Modifier) -> Unit)? = null
    private var quote: (@Composable (BlockQuote, Modifier) -> Unit)? = null
    private var bulletList: (@Composable (BulletList, Modifier) -> Unit)? = null
    private var orderedList: (@Composable (OrderedList, Modifier) -> Unit)? = null
    private var table: (@Composable (Table, Modifier) -> Unit)? = null
    private var horizontalRule: (@Composable (HorizontalRule, Modifier) -> Unit)? = null
    private var image: (@Composable (Image, Modifier) -> Unit)? = null
    private var link: (@Composable (Link, Modifier) -> Unit)? = null
    private var inlineCode: (@Composable (InlineCode, Modifier) -> Unit)? = null
    private var checkbox: (@Composable (Boolean, Modifier) -> Unit)? = null
    private val custom = mutableMapOf<String, @Composable (CustomBlock, Modifier) -> Unit>()

    fun heading(renderer: @Composable (Heading, Modifier) -> Unit) {
        heading = renderer
    }

    fun paragraph(renderer: @Composable (Paragraph, Modifier) -> Unit) {
        paragraph = renderer
    }

    fun codeBlock(renderer: @Composable (CodeBlock, Modifier) -> Unit) {
        codeBlock = renderer
    }

    fun quote(renderer: @Composable (BlockQuote, Modifier) -> Unit) {
        quote = renderer
    }

    fun bulletList(renderer: @Composable (BulletList, Modifier) -> Unit) {
        bulletList = renderer
    }

    fun orderedList(renderer: @Composable (OrderedList, Modifier) -> Unit) {
        orderedList = renderer
    }

    fun table(renderer: @Composable (Table, Modifier) -> Unit) {
        table = renderer
    }

    fun horizontalRule(renderer: @Composable (HorizontalRule, Modifier) -> Unit) {
        horizontalRule = renderer
    }

    fun image(renderer: @Composable (Image, Modifier) -> Unit) {
        image = renderer
    }

    fun link(renderer: @Composable (Link, Modifier) -> Unit) {
        link = renderer
    }

    fun inlineCode(renderer: @Composable (InlineCode, Modifier) -> Unit) {
        inlineCode = renderer
    }

    fun checkbox(renderer: @Composable (Boolean, Modifier) -> Unit) {
        checkbox = renderer
    }

    fun custom(name: String, renderer: @Composable (CustomBlock, Modifier) -> Unit) {
        custom[name] = renderer
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
            link = link,
            inlineCode = inlineCode,
            checkbox = checkbox,
            custom = custom.toMap(),
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
