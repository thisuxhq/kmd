package com.thisux.kmd.internal

import com.thisux.kmd.KmdDocument
import com.thisux.kmd.KmdParser
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

internal class JetBrainsMarkdownParser : KmdParser {
    private val flavour = GFMFlavourDescriptor()
    private val parser = MarkdownParser(flavour, assertionsEnabled = false)

    override fun parse(markdown: String): KmdDocument = parseLocated(markdown).document

    fun parseLocated(markdown: String): ParsedMarkdown {
        val tree = parser.buildMarkdownTreeFromString(markdown)
        return AstConverter(markdown).convertLocated(tree)
    }
}
