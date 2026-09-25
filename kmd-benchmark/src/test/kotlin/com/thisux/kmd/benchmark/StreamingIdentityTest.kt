package com.thisux.kmd.benchmark

import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

// The structural half of the benchmark: these must hold on every run.
class StreamingIdentityTest {
    @Test
    fun finishedBlocksAreNeverRebuiltWhileStreaming() {
        val engine = KmdEngine()
        val finished = mutableMapOf<Long, KmdBlock>()
        for (token in Corpus.tokens(Corpus.ofSize(10_000))) {
            val blocks = engine.append(token).document.blocks
            val byId = blocks.associateBy { it.id.value }
            for ((id, block) in finished) {
                assertSame("finished block $id was rebuilt", block, byId[id])
            }
            blocks.dropLast(1).forEach { finished.putIfAbsent(it.id.value, it) }
        }
    }

    @Test
    fun streamingMatchesAOneShotParse() {
        val markdown = Corpus.ofSize(10_000)
        val streamed = KmdEngine()
        Corpus.tokens(markdown).forEach(streamed::append)
        val oneShot = KmdEngine().parse(markdown)
        assertEquals(oneShot.blocks.size, streamed.snapshot().document.blocks.size)
        assertEquals(oneShot.blocks.map { it::class }, streamed.snapshot().document.blocks.map { it::class })
    }
}
