package com.thisux.kmd

import org.junit.Assert.assertTrue
import org.junit.Test

class KmdKeywordHighlighterTest {
    private val highlighter = KmdKeywordHighlighter()

    @Test
    fun highlightsKotlinKeywords() {
        val result = highlighter.highlight("kotlin", "fun main() { val x = 1 }")
        assertTrue(result.text.contains("fun"))
        assertTrue(result.spanStyles.any { it.start == 0 && it.end == 3 })
    }
}
