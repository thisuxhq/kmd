package com.thisux.kmd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GfmAlertsTest {
    private val engine = KmdEngine(extensions = listOf(GfmAlerts))

    @Test
    fun noteAlertStripsTheMarker() {
        val document =
            engine.parse(
                """
                > [!NOTE]
                > Append-only streaming is the first optimization.
                """.trimIndent(),
            )
        val alert = document.blocks.single() as CustomBlock
        assertEquals(GfmAlerts.AlertName, alert.name)
        assertEquals("NOTE", alert.data[GfmAlerts.KindKey])
        val paragraph = alert.children.single() as Paragraph
        assertEquals(
            listOf(Text("Append-only streaming is the first optimization.")),
            paragraph.content,
        )
    }

    @Test
    fun warningWithTitleOnTheSameLine() {
        val document = engine.parse("> [!warning] Do not expose your API key.")
        val alert = document.blocks.single() as CustomBlock
        assertEquals("WARNING", alert.data[GfmAlerts.KindKey])
        val paragraph = alert.children.single() as Paragraph
        assertEquals(listOf(Text("Do not expose your API key.")), paragraph.content)
    }

    @Test
    fun alertWithoutTheExtensionStaysAQuote() {
        val document =
            KmdEngine().parse(
                """
                > [!NOTE]
                > Append-only streaming is the first optimization.
                """.trimIndent(),
            )
        val quote = document.blocks.single() as BlockQuote
        assertTrue(quote.children.any { it is Paragraph })
    }

    @Test
    fun ordinaryQuoteIsUnchanged() {
        val document = engine.parse("> Important message")
        assertTrue(document.blocks.single() is BlockQuote)
    }

    @Test
    fun alertInsideAList() {
        val document =
            engine.parse(
                """
                - item
                  > [!TIP]
                  > Nested
                """.trimIndent(),
            )
        val list = document.blocks.single() as BulletList
        val alert = list.items.single().children.filterIsInstance<CustomBlock>().single()
        assertEquals("TIP", alert.data[GfmAlerts.KindKey])
    }

    @Test
    fun streamingAnAlertKeepsTheHeading() {
        engine.replace("# Title\n\n> [!NOTE]\n> Hel")
        val heading = engine.snapshot().document.blocks[0]
        val alertId = engine.snapshot().document.blocks[1].id
        engine.append("lo")
        val snapshot = engine.snapshot()
        assertSame(heading, snapshot.document.blocks[0])
        assertEquals(alertId, snapshot.document.blocks[1].id)
        val alert = snapshot.document.blocks[1] as CustomBlock
        val paragraph = alert.children.single() as Paragraph
        assertEquals(listOf(Text("Hello")), paragraph.content)
    }
}
