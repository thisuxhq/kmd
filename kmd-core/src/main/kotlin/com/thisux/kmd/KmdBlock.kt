package com.thisux.kmd

sealed interface KmdBlock {
    val id: KmdBlockId
}

data class Heading(
    override val id: KmdBlockId,
    val level: Int,
    val content: List<KmdInline>,
) : KmdBlock

data class Paragraph(
    override val id: KmdBlockId,
    val content: List<KmdInline>,
) : KmdBlock

data class CodeBlock(
    override val id: KmdBlockId,
    val language: String?,
    val code: String,
) : KmdBlock

data class BlockQuote(
    override val id: KmdBlockId,
    val children: List<KmdBlock>,
) : KmdBlock

data class BulletList(
    override val id: KmdBlockId,
    val items: List<KmdListItem>,
) : KmdBlock

data class OrderedList(
    override val id: KmdBlockId,
    val start: Int,
    val items: List<KmdListItem>,
) : KmdBlock

data class HorizontalRule(
    override val id: KmdBlockId,
) : KmdBlock

data class Table(
    override val id: KmdBlockId,
    val header: TableRow,
    val rows: List<TableRow>,
) : KmdBlock

data class TableRow(
    val id: KmdBlockId,
    val cells: List<TableCell>,
)

data class TableCell(
    val content: List<KmdInline>,
)

data class KmdListItem(
    val id: KmdBlockId,
    val children: List<KmdBlock>,
    val checked: Boolean? = null,
)

data class CustomBlock(
    override val id: KmdBlockId,
    val name: String,
    val children: List<KmdBlock>,
    val data: Map<String, String> = emptyMap(),
) : KmdBlock
