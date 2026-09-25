package com.thisux.kmd

import com.thisux.kmd.internal.IdGenerator
import com.thisux.kmd.internal.JetBrainsMarkdownParser
import com.thisux.kmd.internal.ParsedMarkdown
import com.thisux.kmd.internal.rematchBlocks

class KmdEngine(
    private val parser: KmdParser = JetBrainsMarkdownParser(),
    private val extensions: List<KmdExtension> = emptyList(),
) {
    private val buffer = StringBuilder()
    private val ids = IdGenerator()
    private var current = KmdSnapshot(KmdDocument(emptyList()), activeBlock = null, revision = 0)
    // Source offset of each top-level block. Grown in place; only the first startCount entries are live.
    private var starts: IntArray = IntArray(0)
    private var startCount = 0

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
        startCount = 0
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
        val extended = applyExtensions(located)
        val prefixCount = prefixCountForCut(cut)
        val previous = current.document.blocks
        // The prefix is untouched by construction, so only the tail is rematched.
        val (rematched, candidate) =
            rematchBlocks(previous.subList(prefixCount, previous.size), extended.document.blocks, ids)
        val blocks = ArrayList<KmdBlock>(prefixCount + rematched.size)
        for (index in 0 until prefixCount) {
            blocks.add(previous[index])
        }
        blocks.addAll(rematched)
        val count = prefixCount + extended.starts.size
        if (starts.size < count) {
            starts = starts.copyOf(maxOf(count, starts.size * 2))
        }
        for (index in extended.starts.indices) {
            starts[prefixCount + index] = extended.starts[index] + cut
        }
        startCount = count
        current = KmdSnapshot(KmdDocument(blocks), candidate, current.revision + 1)
        return current
    }

    private fun publishFull(fromAppend: Boolean): KmdSnapshot {
        val located = applyExtensions(parseSlice(buffer.toString()))
        val (blocks, candidate) = rematchBlocks(current.document.blocks, located.document.blocks, ids)
        val active = if (fromAppend) candidate else null
        starts = located.starts
        startCount = located.starts.size
        current = KmdSnapshot(KmdDocument(blocks), active, current.revision + 1)
        return current
    }

    private fun incrementalCut(previousLength: Int): Int {
        if (startCount == 0 || current.document.blocks.isEmpty()) return 0
        val lastStart = starts[startCount - 1]
        if (current.document.blocks.last() is CodeBlock) return lastStart
        if (endedWithBlankLine(previousLength)) return previousLength
        return lastStart
    }

    private fun prefixCountForCut(cut: Int): Int {
        // Starts never decrease, so find the first block at or after the cut.
        var low = 0
        var high = startCount
        while (low < high) {
            val mid = (low + high) ushr 1
            if (starts[mid] < cut) low = mid + 1 else high = mid
        }
        return low
    }

    private fun endedWithBlankLine(length: Int): Boolean {
        if (length < 2) return false
        return buffer[length - 1] == '\n' && buffer[length - 2] == '\n'
    }

    private fun applyExtensions(parsed: ParsedMarkdown): ParsedMarkdown {
        if (extensions.isEmpty()) return parsed
        val document = extensions.fold(parsed.document) { current, extension -> extension.process(current) }
        val starts =
            if (document.blocks.size == parsed.starts.size) {
                parsed.starts
            } else {
                IntArray(document.blocks.size)
            }
        return ParsedMarkdown(document, starts)
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
