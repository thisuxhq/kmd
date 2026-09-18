package com.thisux.kmd

object GfmAlerts : KmdExtension {
    override fun process(document: KmdDocument): KmdDocument {
        return document.copy(blocks = document.blocks.map(::transform))
    }

    private fun transform(block: KmdBlock): KmdBlock {
        return when (block) {
            is BlockQuote -> asAlert(block) ?: block.copy(children = block.children.map(::transform))
            is BulletList -> block.copy(items = block.items.map(::transformItem))
            is OrderedList -> block.copy(items = block.items.map(::transformItem))
            is CustomBlock -> block.copy(children = block.children.map(::transform))
            else -> block
        }
    }

    private fun transformItem(item: KmdListItem): KmdListItem {
        return item.copy(children = item.children.map(::transform))
    }

    private fun asAlert(quote: BlockQuote): CustomBlock? {
        val first = quote.children.firstOrNull() as? Paragraph ?: return null
        val extracted = extractKind(first) ?: return null
        val children =
            buildList {
                extracted.leftover?.let { add(it) }
                addAll(quote.children.drop(1).map(::transform))
            }
        return CustomBlock(
            id = quote.id,
            name = AlertName,
            children = children,
            data = mapOf(KindKey to extracted.kind),
        )
    }

    private fun extractKind(paragraph: Paragraph): ExtractedKind? {
        val content = paragraph.content
        if (content.isEmpty()) return null
        return when (val first = content[0]) {
            is Link -> {
                val label = plainText(first.children).trim()
                if (!label.startsWith("!")) return null
                val kind = label.drop(1).uppercase()
                if (kind !in KindNames) return null
                ExtractedKind(kind, paragraphOrNull(paragraph, trimInlines(dropLeadingBreaks(content.drop(1)))))
            }
            is Text -> {
                val match = Marker.find(first.value) ?: return null
                if (match.range.first != 0) return null
                val kind = match.groupValues[1].uppercase()
                val rest = first.value.substring(match.range.last + 1).trimStart()
                val remaining =
                    buildList {
                        if (rest.isNotEmpty()) add(Text(rest))
                        addAll(dropLeadingBreaks(content.drop(1)))
                    }
                ExtractedKind(kind, paragraphOrNull(paragraph, remaining))
            }
            else -> null
        }
    }

    private fun paragraphOrNull(
        original: Paragraph,
        content: List<KmdInline>,
    ): Paragraph? {
        if (content.isEmpty()) return null
        return original.copy(content = content)
    }

    private fun dropLeadingBreaks(content: List<KmdInline>): List<KmdInline> {
        return content.dropWhile { inline ->
            inline is SoftBreak ||
                inline is HardBreak ||
                (inline is Text && inline.value.isBlank())
        }
    }

    private fun trimInlines(content: List<KmdInline>): List<KmdInline> {
        if (content.isEmpty()) return content
        val copy = content.toMutableList()
        val first = copy.first()
        if (first is Text) {
            val trimmed = first.value.trimStart()
            if (trimmed.isEmpty()) copy.removeAt(0) else copy[0] = Text(trimmed)
        }
        return copy
    }

    private fun plainText(inlines: List<KmdInline>): String {
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
            walk(inlines)
        }
    }

    const val AlertName = "alert"
    const val KindKey = "kind"

    private val KindNames = setOf("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION")
    private val Marker =
        Regex(
            """^\s*\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\s*""",
            RegexOption.IGNORE_CASE,
        )

    private data class ExtractedKind(
        val kind: String,
        val leftover: Paragraph?,
    )
}
