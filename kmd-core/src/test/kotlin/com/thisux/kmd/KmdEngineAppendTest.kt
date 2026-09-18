package com.thisux.kmd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KmdEngineAppendTest {
    private lateinit var engine: KmdEngine

    @Before
    fun setUp() {
        engine = KmdEngine()
    }

    @Test
    fun growingParagraphReparsesOnlyTheLastBlock() {
        engine.replace("# Title\n\nHello")
        val heading = engine.snapshot().document.blocks[0]
        engine.append(" world")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        val paragraph = snapshot.document.blocks[1] as Paragraph
        assertEquals(listOf(Text("Hello world")), paragraph.content)
        assertEquals(paragraph, snapshot.activeBlock)
        assertEquals("Hello world".length, engine.lastParsedLength)
    }

    @Test
    fun newBlockAfterBlankLineDoesNotReparsePrefix() {
        engine.replace("# Title\n\nHello\n\n")
        val heading = engine.snapshot().document.blocks[0]
        val paragraph = engine.snapshot().document.blocks[1]
        engine.append("Next")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        assertSame(paragraph, snapshot.document.blocks[1])
        assertTrue(snapshot.document.blocks[2] is Paragraph)
        assertEquals("Next".length, engine.lastParsedLength)
    }

    @Test
    fun growingListKeepsTheHeadingIdentity() {
        engine.replace("# Title\n\n- tap")
        val heading = engine.snapshot().document.blocks[0]
        val listId = engine.snapshot().document.blocks[1].id
        engine.append("\n- swipe")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        assertEquals(listId, snapshot.document.blocks[1].id)
        val list = snapshot.document.blocks[1] as BulletList
        assertEquals(2, list.items.size)
    }

    @Test
    fun growingTableKeepsTheHeadingIdentity() {
        engine.replace(
            """
            # People

            | Name | Role |
            |------|------|
            | Sam  | Dev  |
            """.trimIndent(),
        )
        val heading = engine.snapshot().document.blocks[0]
        val tableId = engine.snapshot().document.blocks[1].id
        engine.append("\n| Mia  | PM   |")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        assertEquals(tableId, snapshot.document.blocks[1].id)
        val table = snapshot.document.blocks[1] as Table
        assertEquals(2, table.rows.size)
    }

    @Test
    fun unclosedFenceThenCloseKeepsThePrefix() {
        val prefix = "# Title\n\n```kotlin\nfun main() {"
        engine.replace(prefix)
        val heading = engine.snapshot().document.blocks[0]
        engine.append("\n}\n```")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        val code = snapshot.document.blocks[1] as CodeBlock
        assertEquals("kotlin", code.language)
        assertTrue(code.code.contains("fun main()"))
        assertTrue(engine.lastParsedLength < (prefix + "\n}\n```").length)
    }

    @Test
    fun nestedQuoteGrowthKeepsTheHeading() {
        engine.replace("# Title\n\n> Important")
        val heading = engine.snapshot().document.blocks[0]
        engine.append(" message")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        val quote = snapshot.document.blocks[1] as BlockQuote
        val paragraph = quote.children.single() as Paragraph
        assertEquals(listOf(Text("Important message")), paragraph.content)
    }

    @Test
    fun manyStableBlocksAreNotReparsedOnTailAppend() {
        val prefix = (1..40).joinToString("\n\n") { "Paragraph $it." }
        engine.replace(prefix)
        val first = engine.snapshot().document.blocks[0]
        val lastId = engine.snapshot().document.blocks.last().id
        engine.append(" More.")
        val snapshot = engine.snapshot()
        assertSame(first, snapshot.document.blocks[0])
        assertEquals(lastId, snapshot.document.blocks.last().id)
        assertTrue(engine.lastParsedLength < 40)
        assertTrue(engine.lastParsedLength < prefix.length)
    }

    @Test
    fun linkDefinitionFallsBackToAFullParse() {
        val start = "See [KMD][kmd].\n\nHello"
        engine.replace(start)
        val added = "\n\n[kmd]: https://thisux.com"
        engine.append(added)
        val snapshot = engine.snapshot()
        val paragraph = snapshot.document.blocks[0] as Paragraph
        val link = paragraph.content.filterIsInstance<Link>().single()
        assertEquals("https://thisux.com", link.destination)
        assertEquals((start + added).length, engine.lastParsedLength)
    }

    @Test
    fun replaceStillParsesTheWholeDocument() {
        engine.replace("# Title\n\nHello")
        val markdown = "# Other\n\nWorld"
        engine.replace(markdown)
        assertEquals(markdown.length, engine.lastParsedLength)
        val heading = engine.snapshot().document.blocks[0] as Heading
        assertEquals(listOf(Text("Other")), heading.content)
    }
}
