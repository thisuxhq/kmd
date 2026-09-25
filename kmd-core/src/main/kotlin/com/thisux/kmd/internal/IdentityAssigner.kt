package com.thisux.kmd.internal

import com.thisux.kmd.BlockQuote
import com.thisux.kmd.BulletList
import com.thisux.kmd.CodeBlock
import com.thisux.kmd.Heading
import com.thisux.kmd.HorizontalRule
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdListItem
import com.thisux.kmd.OrderedList
import com.thisux.kmd.Paragraph
import com.thisux.kmd.CustomBlock
import com.thisux.kmd.Table
import com.thisux.kmd.TableRow

internal fun rematchBlocks(
    previous: List<KmdBlock>,
    incoming: List<KmdBlock>,
    ids: IdGenerator,
): Pair<List<KmdBlock>, KmdBlock?> {
    if (incoming.isEmpty()) return emptyList<KmdBlock>() to null

    val result = ArrayList<KmdBlock>(incoming.size)
    var index = 0
    while (index < incoming.size && index < previous.size && previous[index].sameContent(incoming[index])) {
        result += previous[index]
        index++
    }
    val firstChanged = index
    while (index < incoming.size) {
        val prev = previous.getOrNull(index)
        val next = incoming[index]
        result +=
            if (prev != null && prev::class == next::class) {
                rematchBlock(prev, next, ids)
            } else {
                next.rekey(ids)
            }
        index++
    }
    val active = if (firstChanged < result.size) result.last() else null
    return result to active
}

internal fun KmdBlock.sameContent(other: KmdBlock): Boolean {
    if (this === other) return true
    return when {
        this is Heading && other is Heading ->
            level == other.level && content == other.content
        this is Paragraph && other is Paragraph ->
            content == other.content
        this is CodeBlock && other is CodeBlock ->
            language == other.language && code == other.code
        this is HorizontalRule && other is HorizontalRule ->
            true
        this is BlockQuote && other is BlockQuote ->
            children.sameBlocks(other.children)
        this is BulletList && other is BulletList ->
            items.sameItems(other.items)
        this is OrderedList && other is OrderedList ->
            start == other.start && items.sameItems(other.items)
        this is Table && other is Table ->
            header.sameRow(other.header) && rows.allPaired(other.rows) { left, right -> left.sameRow(right) }
        this is CustomBlock && other is CustomBlock ->
            name == other.name &&
                data == other.data &&
                children.sameBlocks(other.children)
        else -> false
    }
}

private fun List<KmdListItem>.sameItems(other: List<KmdListItem>): Boolean =
    allPaired(other) { left, right ->
        left === right || (left.checked == right.checked && left.children.sameBlocks(right.children))
    }

private fun List<KmdBlock>.sameBlocks(other: List<KmdBlock>): Boolean =
    allPaired(other) { left, right -> left.sameContent(right) }

private fun TableRow.sameRow(other: TableRow): Boolean =
    this === other || cells.allPaired(other.cells) { left, right -> left.content == right.content }

// zip().all() without the Pair list: this runs on every append.
private inline fun <T> List<T>.allPaired(
    other: List<T>,
    predicate: (T, T) -> Boolean,
): Boolean {
    if (this === other) return true
    if (size != other.size) return false
    for (index in indices) {
        if (!predicate(this[index], other[index])) return false
    }
    return true
}

private fun rematchBlock(
    previous: KmdBlock,
    incoming: KmdBlock,
    ids: IdGenerator,
): KmdBlock {
    return when {
        previous is Heading && incoming is Heading ->
            incoming.copy(id = previous.id)
        previous is Paragraph && incoming is Paragraph ->
            incoming.copy(id = previous.id)
        previous is CodeBlock && incoming is CodeBlock ->
            incoming.copy(id = previous.id)
        previous is HorizontalRule && incoming is HorizontalRule ->
            previous
        previous is BlockQuote && incoming is BlockQuote ->
            incoming.copy(
                id = previous.id,
                children = rematchBlocks(previous.children, incoming.children, ids).first,
            )
        previous is BulletList && incoming is BulletList ->
            incoming.copy(
                id = previous.id,
                items = rematchItems(previous.items, incoming.items, ids),
            )
        previous is OrderedList && incoming is OrderedList ->
            incoming.copy(
                id = previous.id,
                items = rematchItems(previous.items, incoming.items, ids),
            )
        previous is Table && incoming is Table ->
            incoming.copy(
                id = previous.id,
                header = incoming.header.copy(id = previous.header.id),
                rows =
                    incoming.rows.mapIndexed { index, row ->
                        row.copy(id = previous.rows.getOrNull(index)?.id ?: ids.next())
                    },
            )
        previous is CustomBlock && incoming is CustomBlock ->
            incoming.copy(
                id = previous.id,
                children = rematchBlocks(previous.children, incoming.children, ids).first,
            )
        else -> incoming.rekey(ids)
    }
}

private fun rematchItems(
    previous: List<KmdListItem>,
    incoming: List<KmdListItem>,
    ids: IdGenerator,
): List<KmdListItem> {
    return incoming.mapIndexed { index, item ->
        val prev = previous.getOrNull(index)
        if (prev == null) {
            item.rekey(ids)
        } else {
            KmdListItem(
                id = prev.id,
                children = rematchBlocks(prev.children, item.children, ids).first,
                checked = item.checked,
            )
        }
    }
}

internal fun KmdBlock.rekey(ids: IdGenerator): KmdBlock {
    return when (this) {
        is Heading -> copy(id = ids.next())
        is Paragraph -> copy(id = ids.next())
        is CodeBlock -> copy(id = ids.next())
        is HorizontalRule -> copy(id = ids.next())
        is BlockQuote -> copy(id = ids.next(), children = children.map { it.rekey(ids) })
        is BulletList -> copy(id = ids.next(), items = items.map { it.rekey(ids) })
        is OrderedList -> copy(id = ids.next(), items = items.map { it.rekey(ids) })
        is Table ->
            copy(
                id = ids.next(),
                header = header.copy(id = ids.next()),
                rows = rows.map { it.copy(id = ids.next()) },
            )
        is CustomBlock -> copy(id = ids.next(), children = children.map { it.rekey(ids) })
    }
}

private fun KmdListItem.rekey(ids: IdGenerator): KmdListItem {
    return KmdListItem(
        id = ids.next(),
        children = children.map { it.rekey(ids) },
        checked = checked,
    )
}
