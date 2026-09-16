package com.thisux.kmd.internal

import com.thisux.kmd.KmdDocument
import com.thisux.kmd.KmdParser
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

internal class JetBrainsMarkdownParser : KmdParser {
    private val flavour = CommonMarkFlavourDescriptor()
    private val parser = MarkdownParser(flavour, assertionsEnabled = false)

    override fun parse(markdown: String): KmdDocument {
        val tree = parser.buildMarkdownTreeFromString(markdown)
        return AstConverter(markdown).convert(tree)
    }
}
