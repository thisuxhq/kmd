package com.thisux.kmd.internal

import com.thisux.kmd.BlockQuote
import com.thisux.kmd.BulletList
import com.thisux.kmd.CodeBlock
import com.thisux.kmd.Emphasis
import com.thisux.kmd.HardBreak
import com.thisux.kmd.Heading
import com.thisux.kmd.HorizontalRule
import com.thisux.kmd.Image
import com.thisux.kmd.InlineCode
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdBlockId
import com.thisux.kmd.KmdDocument
import com.thisux.kmd.KmdInline
import com.thisux.kmd.KmdListItem
import com.thisux.kmd.Link
import com.thisux.kmd.OrderedList
import com.thisux.kmd.Paragraph
import com.thisux.kmd.SoftBreak
import com.thisux.kmd.Strike
import com.thisux.kmd.Strong
import com.thisux.kmd.Table
import com.thisux.kmd.TableCell
import com.thisux.kmd.TableRow
import com.thisux.kmd.Text
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

internal class AstConverter(
    private val source: String,
) {
    private val unassigned = KmdBlockId(0)
    private val references = mutableMapOf<String, String>()

    fun convert(root: ASTNode): KmdDocument = convertLocated(root).document

    fun convertLocated(root: ASTNode): ParsedMarkdown {
        collectReferences(root)
        val blocks = ArrayList<KmdBlock>()
        val starts = ArrayList<Int>()
        appendLocated(root.children, blocks, starts)
        return ParsedMarkdown(KmdDocument(blocks), starts.toIntArray())
    }

    private fun appendLocated(
        nodes: List<ASTNode>,
        blocks: MutableList<KmdBlock>,
        starts: MutableList<Int>,
    ) {
        for (node in nodes) {
            if (node.type == MarkdownElementTypes.MARKDOWN_FILE) {
                appendLocated(node.children, blocks, starts)
                continue
            }
            val converted = convertBlocks(listOf(node))
            for (block in converted) {
                blocks += block
                starts += node.startOffset
            }
        }
    }

    private fun collectReferences(node: ASTNode) {
        if (node.type == MarkdownElementTypes.LINK_DEFINITION) {
            val label = node.child(MarkdownElementTypes.LINK_LABEL)?.let { normalizeLabel(it.text()) }
            val destination = node.child(MarkdownElementTypes.LINK_DESTINATION)?.let { parseDestination(it) }
            if (label != null && destination != null) {
                references.putIfAbsent(label, destination)
            }
        }
        node.children.forEach(::collectReferences)
    }

    private fun convertBlocks(nodes: List<ASTNode>): List<KmdBlock> {
        val blocks = ArrayList<KmdBlock>()
        for (node in nodes) {
            when (node.type) {
                MarkdownElementTypes.ATX_1 -> blocks += heading(node, 1)
                MarkdownElementTypes.ATX_2 -> blocks += heading(node, 2)
                MarkdownElementTypes.ATX_3 -> blocks += heading(node, 3)
                MarkdownElementTypes.ATX_4 -> blocks += heading(node, 4)
                MarkdownElementTypes.ATX_5 -> blocks += heading(node, 5)
                MarkdownElementTypes.ATX_6 -> blocks += heading(node, 6)
                MarkdownElementTypes.SETEXT_1 -> blocks += heading(node, 1)
                MarkdownElementTypes.SETEXT_2 -> blocks += heading(node, 2)
                MarkdownElementTypes.PARAGRAPH -> {
                    val content = convertInlines(node)
                    if (content.isNotEmpty()) {
                        blocks += Paragraph(unassigned, content)
                    }
                }
                MarkdownElementTypes.CODE_FENCE -> blocks += fencedCode(node)
                MarkdownElementTypes.CODE_BLOCK -> blocks += indentedCode(node)
                MarkdownElementTypes.BLOCK_QUOTE -> {
                    val children = convertBlocks(node.children)
                    if (children.isNotEmpty()) {
                        blocks += BlockQuote(unassigned, children)
                    }
                }
                MarkdownElementTypes.UNORDERED_LIST ->
                    blocks += BulletList(unassigned, listItems(node))
                MarkdownElementTypes.ORDERED_LIST ->
                    blocks += OrderedList(unassigned, orderedStart(node), listItems(node))
                MarkdownTokenTypes.HORIZONTAL_RULE ->
                    blocks += HorizontalRule(unassigned)
                GFMElementTypes.TABLE -> table(node)?.let { blocks += it }
                GFMElementTypes.ALERT -> alertQuote(node)?.let { blocks += it }
                MarkdownElementTypes.MARKDOWN_FILE ->
                    blocks += convertBlocks(node.children)
                MarkdownElementTypes.LIST_ITEM -> {
                    val children = convertBlocks(node.children)
                    if (children.isNotEmpty()) {
                        blocks += children
                    }
                }
            }
        }
        return blocks
    }

    private fun alertQuote(node: ASTNode): BlockQuote? {
        val title = node.child(GFMTokenTypes.ALERT_TITLE)?.text()?.trim()
        val children = convertBlocks(node.children)
        val withTitle =
            if (title.isNullOrEmpty()) {
                children
            } else {
                listOf(Paragraph(unassigned, listOf(Text(title)))) + children
            }
        if (withTitle.isEmpty()) return null
        return BlockQuote(unassigned, withTitle)
    }

    private fun heading(node: ASTNode, level: Int): Heading {
        val contentNode =
            node.child(MarkdownTokenTypes.ATX_CONTENT)
                ?: node.child(MarkdownTokenTypes.SETEXT_CONTENT)
                ?: node
        return Heading(unassigned, level, trimInlines(convertInlines(contentNode)))
    }

    private fun fencedCode(node: ASTNode): CodeBlock {
        val language =
            node.child(MarkdownTokenTypes.FENCE_LANG)
                ?.text()
                ?.trim()
                ?.ifEmpty { null }
        val code =
            node.children
                .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                .joinToString("\n") { it.text() }
                .trimEnd('\n')
        return CodeBlock(unassigned, language, code)
    }

    private fun indentedCode(node: ASTNode): CodeBlock {
        val code =
            node.children
                .filter { it.type == MarkdownTokenTypes.CODE_LINE }
                .joinToString("") { line ->
                    val text = line.text()
                    val withoutIndent = text.dropWhileIndexed { index, char -> index < 4 && char == ' ' }
                    withoutIndent
                }
                .trimEnd('\n')
        return CodeBlock(unassigned, language = null, code = code)
    }

    private fun listItems(node: ASTNode): List<KmdListItem> {
        return node.children.mapNotNull { child ->
            if (child.type != MarkdownElementTypes.LIST_ITEM) return@mapNotNull null
            val checkbox = child.child(GFMTokenTypes.CHECK_BOX)
            val checked =
                checkbox?.text()?.let { marker ->
                    marker.contains('x', ignoreCase = true)
                }
            KmdListItem(
                id = unassigned,
                children = convertBlocks(child.children),
                checked = checked,
            )
        }
    }

    private fun table(node: ASTNode): Table? {
        val headerNode = node.child(GFMElementTypes.HEADER) ?: return null
        val header = tableRow(headerNode) ?: return null
        val rows =
            node.children.mapNotNull { child ->
                if (child.type != GFMElementTypes.ROW) return@mapNotNull null
                tableRow(child)
            }
        return Table(unassigned, header, rows)
    }

    private fun tableRow(node: ASTNode): TableRow? {
        val cells =
            node.children.mapNotNull { child ->
                if (child.type != GFMTokenTypes.CELL) return@mapNotNull null
                TableCell(trimInlines(convertInlines(child)))
            }
        if (cells.isEmpty()) return null
        return TableRow(unassigned, cells)
    }

    private fun orderedStart(node: ASTNode): Int {
        val marker =
            node.children
                .firstOrNull { it.type == MarkdownElementTypes.LIST_ITEM }
                ?.child(MarkdownTokenTypes.LIST_NUMBER)
                ?.text()
                .orEmpty()
        val digits = marker.takeWhile { it.isDigit() }
        return digits.toIntOrNull() ?: 1
    }

    private fun convertInlines(node: ASTNode): List<KmdInline> {
        val result = ArrayList<KmdInline>()
        for (child in node.children) {
            appendInline(result, child)
        }
        return mergeText(result)
    }

    private fun appendInline(result: MutableList<KmdInline>, node: ASTNode) {
        when (node.type) {
            MarkdownTokenTypes.TEXT,
            MarkdownTokenTypes.WHITE_SPACE,
            MarkdownTokenTypes.COLON,
            MarkdownTokenTypes.SINGLE_QUOTE,
            MarkdownTokenTypes.DOUBLE_QUOTE,
            MarkdownTokenTypes.LPAREN,
            MarkdownTokenTypes.RPAREN,
            MarkdownTokenTypes.LT,
            MarkdownTokenTypes.GT,
            MarkdownTokenTypes.EXCLAMATION_MARK,
            MarkdownTokenTypes.BAD_CHARACTER,
            -> result += Text(node.text())
            MarkdownTokenTypes.LBRACKET,
            MarkdownTokenTypes.RBRACKET,
            -> Unit
            MarkdownTokenTypes.EOL -> result += SoftBreak
            MarkdownTokenTypes.HARD_LINE_BREAK -> result += HardBreak
            MarkdownElementTypes.EMPH -> result += Emphasis(convertInlines(node))
            MarkdownElementTypes.STRONG -> result += Strong(convertInlines(node))
            GFMElementTypes.STRIKETHROUGH -> result += Strike(convertInlines(node))
            MarkdownElementTypes.CODE_SPAN -> result += InlineCode(codeSpanText(node))
            MarkdownElementTypes.INLINE_LINK -> result += inlineLink(node)
            MarkdownElementTypes.FULL_REFERENCE_LINK,
            MarkdownElementTypes.SHORT_REFERENCE_LINK,
            -> result += referenceLink(node)
            MarkdownElementTypes.IMAGE -> result += image(node)
            MarkdownElementTypes.AUTOLINK,
            MarkdownTokenTypes.AUTOLINK,
            MarkdownTokenTypes.EMAIL_AUTOLINK,
            GFMTokenTypes.GFM_AUTOLINK,
            -> {
                val raw = node.text().trim()
                val destination = raw.trim('<', '>')
                result += Link(destination, listOf(Text(destination)))
            }
            MarkdownTokenTypes.ATX_CONTENT,
            MarkdownTokenTypes.SETEXT_CONTENT,
            MarkdownElementTypes.LINK_TEXT,
            -> result += convertInlines(node)
            MarkdownTokenTypes.EMPH,
            MarkdownTokenTypes.BACKTICK,
            MarkdownTokenTypes.ESCAPED_BACKTICKS,
            MarkdownTokenTypes.ATX_HEADER,
            MarkdownTokenTypes.SETEXT_1,
            MarkdownTokenTypes.SETEXT_2,
            MarkdownTokenTypes.CODE_FENCE_START,
            MarkdownTokenTypes.CODE_FENCE_END,
            MarkdownTokenTypes.FENCE_LANG,
            MarkdownTokenTypes.LIST_BULLET,
            MarkdownTokenTypes.LIST_NUMBER,
            MarkdownTokenTypes.BLOCK_QUOTE,
            MarkdownTokenTypes.HTML_TAG,
            MarkdownElementTypes.LINK_DESTINATION,
            MarkdownElementTypes.LINK_TITLE,
            MarkdownElementTypes.LINK_LABEL,
            MarkdownElementTypes.LINK_DEFINITION,
            GFMTokenTypes.TILDE,
            GFMTokenTypes.TABLE_SEPARATOR,
            GFMTokenTypes.CHECK_BOX,
            GFMTokenTypes.DOLLAR,
            GFMTokenTypes.ALERT_TITLE,
            -> Unit
            else -> {
                if (node.children.isNotEmpty()) {
                    node.children.forEach { appendInline(result, it) }
                }
            }
        }
    }

    private fun inlineLink(node: ASTNode): Link {
        val textNode = node.child(MarkdownElementTypes.LINK_TEXT)
        val destination =
            node.child(MarkdownElementTypes.LINK_DESTINATION)
                ?.let(::parseDestination)
                .orEmpty()
        val children = textNode?.let(::convertInlines).orEmpty()
        return Link(destination, children)
    }

    private fun referenceLink(node: ASTNode): KmdInline {
        val textNode = node.child(MarkdownElementTypes.LINK_TEXT)
        val labelNode = node.child(MarkdownElementTypes.LINK_LABEL) ?: textNode
        val rawLabel = labelNode?.text()?.trim()?.trim('[', ']')
        val label = rawLabel?.let(::normalizeLabel)
        val destination = label?.let { references[it] }.orEmpty()
        val children =
            textNode?.let(::convertInlines).orEmpty().ifEmpty {
                rawLabel?.let { listOf(Text(it)) }.orEmpty()
            }
        return Link(destination, children)
    }

    private fun image(node: ASTNode): Image {
        val link = node.child(MarkdownElementTypes.INLINE_LINK)
        val textNode = link?.child(MarkdownElementTypes.LINK_TEXT) ?: node.child(MarkdownElementTypes.LINK_TEXT)
        val destinationNode =
            link?.child(MarkdownElementTypes.LINK_DESTINATION)
                ?: node.child(MarkdownElementTypes.LINK_DESTINATION)
        val alt = textNode?.let { plainText(convertInlines(it)) }
        val source = destinationNode?.let(::parseDestination).orEmpty()
        return Image(source, alt)
    }

    private fun codeSpanText(node: ASTNode): String {
        return node.children
            .filter { it.type != MarkdownTokenTypes.BACKTICK && it.type != MarkdownTokenTypes.ESCAPED_BACKTICKS }
            .joinToString("") { it.text() }
            .replace('\n', ' ')
    }

    private fun parseDestination(node: ASTNode): String {
        return node.text().trim().trim('<', '>').trim()
    }

    private fun normalizeLabel(raw: String): String {
        return raw.trim().trim('[', ']').lowercase().replace(Whitespace, " ")
    }

    private fun ASTNode.text(): String = source.substring(startOffset, endOffset)

    private fun ASTNode.child(type: IElementType): ASTNode? = children.firstOrNull { it.type == type }

    private fun trimInlines(inlines: List<KmdInline>): List<KmdInline> {
        if (inlines.isEmpty()) return inlines
        val copy = inlines.toMutableList()
        val first = copy.first()
        if (first is Text) {
            val trimmed = first.value.trimStart()
            if (trimmed.isEmpty()) copy.removeAt(0) else copy[0] = Text(trimmed)
        }
        if (copy.isNotEmpty()) {
            val last = copy.last()
            if (last is Text) {
                val trimmed = last.value.trimEnd()
                if (trimmed.isEmpty()) copy.removeAt(copy.lastIndex) else copy[copy.lastIndex] = Text(trimmed)
            }
        }
        return copy
    }

    private fun mergeText(inlines: List<KmdInline>): List<KmdInline> {
        if (inlines.isEmpty()) return emptyList()
        val merged = ArrayList<KmdInline>(inlines.size)
        for (inline in inlines) {
            val last = merged.lastOrNull()
            if (inline is Text && last is Text) {
                merged[merged.lastIndex] = Text(last.value + inline.value)
            } else {
                merged += inline
            }
        }
        return merged
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

    private companion object {
        val Whitespace = Regex("\\s+")
    }
}

private fun String.dropWhileIndexed(predicate: (Int, Char) -> Boolean): String {
    var index = 0
    while (index < length && predicate(index, this[index])) {
        index++
    }
    return substring(index)
}
