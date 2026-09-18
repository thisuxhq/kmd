package com.thisux.kmd

import com.thisux.kmd.internal.IdGenerator
import com.thisux.kmd.internal.JetBrainsMarkdownParser
import com.thisux.kmd.internal.ParsedMarkdown
import com.thisux.kmd.internal.rematchBlocks

class KmdEngine(
    private val parser: KmdParser = JetBrainsMarkdownParser(),
) {
    private val buffer = StringBuilder()
    private val ids = IdGenerator()
    private var current = KmdSnapshot(KmdDocument(emptyList()), activeBlock = null, revision = 0)
    private var starts: IntArray = IntArray(0)

    internal var lastParsedLength: Int = 0
        private set

    fun parse(markdown: String): KmdDocument = replace(markdown).document

    fun append(input: String): KmdSnapshot {
        if (input.isEmpty()) return current
        val previousLength = buffer.length
        buffer.append(input)
        return publishAppend(previousLength)
    }

    fun reset() {
        buffer.setLength(0)
        starts = IntArray(0)
        lastParsedLength = 0
        current = KmdSnapshot(KmdDocument(emptyList()), activeBlock = null, revision = current.revision + 1)
    }

    fun replace(markdown: String): KmdSnapshot {
        buffer.setLength(0)
        buffer.append(markdown)
        return publishFull(fromAppend = false)
    }

    fun snapshot(): KmdSnapshot = current

    private fun publishAppend(previousLength: Int): KmdSnapshot {
        val cut = incrementalCut(previousLength)
        if (cut <= 0) return publishFull(fromAppend = true)
        val tail = buffer.substring(cut)
        if (LinkDefinitionLine.containsMatchIn(tail)) {
            return publishFull(fromAppend = true)
        }
        val located = parseSlice(tail)
        val prefixCount = prefixCountForCut(cut)
        val prefix = current.document.blocks.take(prefixCount)
        val incoming = ArrayList<KmdBlock>(prefixCount + located.document.blocks.size)
        incoming.addAll(prefix)
        incoming.addAll(located.document.blocks)
        val incomingStarts = IntArray(incoming.size)
        if (prefixCount > 0) {
            starts.copyInto(incomingStarts, endIndex = prefixCount)
        }
        for (index in located.starts.indices) {
            incomingStarts[prefixCount + index] = located.starts[index] + cut
        }
        val (blocks, candidate) = rematchBlocks(current.document.blocks, incoming, ids)
        starts = incomingStarts
        current = KmdSnapshot(KmdDocument(blocks), candidate, current.revision + 1)
        return current
    }

    private fun publishFull(fromAppend: Boolean): KmdSnapshot {
        val located = parseSlice(buffer.toString())
        val (blocks, candidate) = rematchBlocks(current.document.blocks, located.document.blocks, ids)
        val active = if (fromAppend) candidate else null
        starts = located.starts
        current = KmdSnapshot(KmdDocument(blocks), active, current.revision + 1)
        return current
    }

    private fun incrementalCut(previousLength: Int): Int {
        if (starts.isEmpty() || current.document.blocks.isEmpty()) return 0
        val lastStart = starts.last()
        if (current.document.blocks.last() is CodeBlock) return lastStart
        if (endedWithBlankLine(previousLength)) return previousLength
        return lastStart
    }

    private fun prefixCountForCut(cut: Int): Int {
        var count = 0
        while (count < starts.size && starts[count] < cut) {
            count++
        }
        return count
    }

    private fun endedWithBlankLine(length: Int): Boolean {
        if (length < 2) return false
        return buffer[length - 1] == '\n' && buffer[length - 2] == '\n'
    }

    private fun parseSlice(source: String): ParsedMarkdown {
        lastParsedLength = source.length
        val located = parser as? JetBrainsMarkdownParser
        if (located != null) return located.parseLocated(source)
        val document = parser.parse(source)
        return ParsedMarkdown(document, IntArray(document.blocks.size))
    }

    private companion object {
        val LinkDefinitionLine = Regex("""(?m)^\s*\[[^]]+]:\s*\S""")
    }
}
