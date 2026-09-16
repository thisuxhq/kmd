package com.thisux.kmd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KmdEngineTest {
    private lateinit var engine: KmdEngine

    @Before
    fun setUp() {
        engine = KmdEngine()
    }

    @Test
    fun parseHeadingAndParagraph() {
        val document =
            engine.parse(
                """
                # Hello

                Welcome to **KMD**.
                """.trimIndent(),
            )

        val heading = document.blocks[0] as Heading
        assertEquals(1, heading.level)
        assertEquals(listOf(Text("Hello")), heading.content)

        val paragraph = document.blocks[1] as Paragraph
        assertEquals(Text("Welcome to "), paragraph.content[0])
        assertEquals(Strong(listOf(Text("KMD"))), paragraph.content[1])
        assertEquals(Text("."), paragraph.content[2])
    }

    @Test
    fun parseEmphasisInlineCodeAndLink() {
        val document = engine.parse("This is *nice* with `code` and [KMD](https://example.com).")
        val paragraph = document.blocks.single() as Paragraph
        assertEquals(Text("This is "), paragraph.content[0])
        assertEquals(Emphasis(listOf(Text("nice"))), paragraph.content[1])
        assertEquals(Text(" with "), paragraph.content[2])
        assertEquals(InlineCode("code"), paragraph.content[3])
        assertEquals(Text(" and "), paragraph.content[4])
        assertEquals(Link("https://example.com", listOf(Text("KMD"))), paragraph.content[5])
        assertEquals(Text("."), paragraph.content[6])
    }

    @Test
    fun parseFencedCodeBlock() {
        val document =
            engine.parse(
                """
                ```kotlin
                fun main() {
                    println("Hello")
                }
                ```
                """.trimIndent(),
            )
        val code = document.blocks.single() as CodeBlock
        assertEquals("kotlin", code.language)
        assertEquals("fun main() {\n    println(\"Hello\")\n}", code.code)
    }

    @Test
    fun parseNestedLists() {
        val document =
            engine.parse(
                """
                1. Kotlin
                   - Android
                   - Desktop
                2. Swift
                """.trimIndent(),
            )
        val list = document.blocks.single() as OrderedList
        assertEquals(1, list.start)
        assertEquals(2, list.items.size)
        val first = list.items[0]
        assertTrue(first.children.any { it is Paragraph })
        assertTrue(first.children.any { it is BulletList })
        val nested = first.children.filterIsInstance<BulletList>().single()
        assertEquals(2, nested.items.size)
    }

    @Test
    fun parseBlockQuoteAndThematicBreak() {
        val document =
            engine.parse(
                """
                > Important message

                ---
                """.trimIndent(),
            )
        val quote = document.blocks[0] as BlockQuote
        val paragraph = quote.children.single() as Paragraph
        assertEquals(listOf(Text("Important message")), paragraph.content)
        assertTrue(document.blocks[1] is HorizontalRule)
    }

    @Test
    fun parseImage() {
        val document = engine.parse("![A cat](https://example.com/cat.png)")
        val paragraph = document.blocks.single() as Paragraph
        assertEquals(Image("https://example.com/cat.png", "A cat"), paragraph.content.single())
    }

    @Test
    fun appendReusesStableBlockIdentities() {
        engine.replace(
            """
            # Android Agent

            Android agents can interact with apps.
            """.trimIndent(),
        )
        val first = engine.snapshot()
        val headingId = first.document.blocks[0].id
        val paragraphId = first.document.blocks[1].id

        val second = engine.append("\n\n- tap")
        assertEquals(headingId, second.document.blocks[0].id)
        assertEquals(paragraphId, second.document.blocks[1].id)
        assertTrue(second.document.blocks[2] is BulletList)
        assertEquals(second.document.blocks.last(), second.activeBlock)
    }

    @Test
    fun growingParagraphKeepsTheSameIdentity() {
        engine.replace("Hello")
        val id = engine.snapshot().document.blocks.single().id
        val next = engine.append(" world")
        assertEquals(id, next.document.blocks.single().id)
        val paragraph = next.document.blocks.single() as Paragraph
        assertEquals(listOf(Text("Hello world")), paragraph.content)
        assertEquals(paragraph, next.activeBlock)
    }

    @Test
    fun finishedDocumentHasNoActiveBlock() {
        val snapshot = engine.replace("# Done\n\nAll set.")
        assertNull(snapshot.activeBlock)
        assertEquals(2, snapshot.document.blocks.size)
    }

    @Test
    fun resetClearsDocument() {
        engine.replace("# Hello")
        engine.reset()
        assertTrue(engine.snapshot().document.blocks.isEmpty())
        val next = engine.append("# Hello")
        assertEquals(1, next.document.blocks.size)
        assertNotEquals(0, next.document.blocks.single().id.value)
    }

    @Test
    fun incompleteEmphasisDoesNotCrash() {
        val snapshot = engine.replace("This is **som")
        assertTrue(snapshot.document.blocks.isNotEmpty())
    }

    @Test
    fun parseStrikethrough() {
        val document = engine.parse("This is ~~gone~~.")
        val paragraph = document.blocks.single() as Paragraph
        assertEquals(Text("This is "), paragraph.content[0])
        assertEquals(Strike(listOf(Text("gone"))), paragraph.content[1])
        assertEquals(Text("."), paragraph.content[2])
    }

    @Test
    fun parseTaskList() {
        val document =
            engine.parse(
                """
                - [x] Build parser
                - [ ] Build renderer
                """.trimIndent(),
            )
        val list = document.blocks.single() as BulletList
        assertEquals(true, list.items[0].checked)
        assertEquals(false, list.items[1].checked)
    }

    @Test
    fun parseTable() {
        val document =
            engine.parse(
                """
                | Name | Role |
                |------|------|
                | Sam  | Dev  |
                | Mia  | PM   |
                """.trimIndent(),
            )
        val table = document.blocks.single() as Table
        assertEquals(listOf(Text("Name")), table.header.cells[0].content)
        assertEquals(listOf(Text("Role")), table.header.cells[1].content)
        assertEquals(2, table.rows.size)
        assertEquals(listOf(Text("Sam")), table.rows[0].cells[0].content)
        assertEquals(listOf(Text("PM")), table.rows[1].cells[1].content)
    }

    @Test
    fun parseAutolink() {
        val document = engine.parse("See https://thisux.com for more.")
        val paragraph = document.blocks.single() as Paragraph
        assertTrue(paragraph.content.any { it is Link && it.destination.contains("thisux.com") })
    }
}
