package com.thisux.kmd

import com.thisux.kmd.internal.IdGenerator
import com.thisux.kmd.internal.JetBrainsMarkdownParser
import com.thisux.kmd.internal.rematchBlocks

class KmdEngine(
    private val parser: KmdParser = JetBrainsMarkdownParser(),
) {
    private val buffer = StringBuilder()
    private val ids = IdGenerator()
    private var current = KmdSnapshot(KmdDocument(emptyList()), activeBlock = null, revision = 0)

    fun parse(markdown: String): KmdDocument = replace(markdown).document

    fun append(input: String): KmdSnapshot {
        if (input.isEmpty()) return current
        buffer.append(input)
        return publish(fromAppend = true)
    }

    fun reset() {
        buffer.setLength(0)
        current = KmdSnapshot(KmdDocument(emptyList()), activeBlock = null, revision = current.revision + 1)
    }

    fun replace(markdown: String): KmdSnapshot {
        buffer.setLength(0)
        buffer.append(markdown)
        return publish(fromAppend = false)
    }

    fun snapshot(): KmdSnapshot = current

    private fun publish(fromAppend: Boolean): KmdSnapshot {
        val parsed = parser.parse(buffer.toString())
        val (blocks, candidate) = rematchBlocks(current.document.blocks, parsed.blocks, ids)
        val active = if (fromAppend) candidate else null
        current = KmdSnapshot(KmdDocument(blocks), active, current.revision + 1)
        return current
    }
}
