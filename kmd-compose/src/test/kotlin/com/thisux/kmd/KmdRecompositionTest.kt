package com.thisux.kmd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.createComposeRule
import com.thisux.kmd.internal.KmdBlockProbe
import com.thisux.kmd.internal.LocalKmdBlockProbe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KmdRecompositionTest {
    @get:Rule
    val rule = createComposeRule()

    private val counts = mutableMapOf<Long, Int>()
    private val carets = mutableMapOf<Long, Boolean>()
    private lateinit var state: KmdState

    @Test
    fun appendRecomposesOnlyTheActiveBlock() {
        render(caret = false) { Kmd(state, options = it) }
        assertOnlyTailRecomposes(caret = false)
    }

    @Test
    fun appendWithCaretRecomposesOnlyTheActiveBlock() {
        render(caret = true) { Kmd(state, options = it) }
        assertOnlyTailRecomposes(caret = true)
    }

    @Test
    fun lazyAppendRecomposesOnlyTheActiveBlock() {
        render(caret = true) { LazyKmd(state, options = it) }
        assertOnlyTailRecomposes(caret = true)
    }

    @Test
    fun newBlockLeavesFinishedBlocksAlone() {
        render(caret = true) { Kmd(state, options = it) }
        val finished = state.document.blocks.dropLast(1).map { it.id.value }
        val previousTail = state.document.blocks.last().id.value

        append("\n\nA new paragraph")

        val newTail = state.document.blocks.last().id.value
        for (id in finished) {
            assertEquals("finished block $id recomposed", 0, counts[id] ?: 0)
        }
        assertTrue("caret moved to the new block", carets[newTail] == true)
        assertEquals("caret left the old tail", false, carets[previousTail])
    }

    @Test
    fun streamingMixedDocumentKeepsFinishedBlocksStill() {
        render(caret = true, initial = "") { Kmd(state, options = it) }
        val finishedAt = mutableMapOf<Long, Int>()
        for (token in MixedDocument.chunked(7)) {
            val before = state.document.blocks.dropLast(1).map { it.id.value }.toSet()
            before.forEach { finishedAt.putIfAbsent(it, counts[it] ?: 0) }
            append(token)
            // A block that was finished before this token may only recompose to drop the caret.
            for (id in before) {
                val extra = (counts[id] ?: 0) - finishedAt.getValue(id)
                assertTrue("finished block $id recomposed $extra times", extra <= 1)
            }
        }
        val settled = state.document.blocks.dropLast(1).map { it.id.value }
        val snapshot = settled.associateWith { counts[it] ?: 0 }
        repeat(20) { append(" more") }
        for (id in settled) {
            assertEquals("settled block $id recomposed", snapshot.getValue(id), counts[id] ?: 0)
        }
    }

    private fun render(
        caret: Boolean,
        initial: String = Initial,
        content: @Composable (KmdOptions) -> Unit,
    ) {
        // The caret blinks forever; drive frames by hand.
        rule.mainClock.autoAdvance = false
        val options = KmdOptions(streaming = KmdStreaming(caret = caret))
        rule.setContent {
            state = rememberKmdState(initial)
            CompositionLocalProvider(
                LocalKmdBlockProbe provides
                    KmdBlockProbe { id, showCaret ->
                        counts[id.value] = (counts[id.value] ?: 0) + 1
                        carets[id.value] = showCaret
                    },
            ) {
                content(options)
            }
        }
        frame()
        counts.clear()
    }

    private fun assertOnlyTailRecomposes(caret: Boolean) {
        val tail = state.document.blocks.last().id.value
        repeat(10) { append(" token$it") }
        assertEquals(setOf(tail), counts.keys)
        // One per token, plus one when the caret first lands on the tail.
        assertEquals(if (caret) 11 else 10, counts[tail])
    }

    private fun append(value: String) {
        rule.runOnIdle {
            state.append(value)
            Snapshot.sendApplyNotifications()
        }
        frame()
    }

    private fun frame() {
        repeat(3) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
    }

    private companion object {
        const val Initial = "# Title\n\nFirst paragraph with a [link](https://example.com).\n\n- one\n- two\n\n```kotlin\nval x = 1\n```\n\nTail"

        val MixedDocument =
            """
            # Streaming

            KMD renders **bold**, *italic*, and `code` as it arrives.

            - first item
            - second item with [a link](https://example.com)

            > A quote that runs
            > across two lines.

            ```kotlin
            fun main() {
                println("hello")
            }
            ```

            | a | b |
            |---|---|
            | 1 | 2 |

            1. one
            2. two

            Final paragraph.
            """.trimIndent()
    }
}
