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
            children.size == other.children.size &&
                children.zip(other.children).all { (left, right) -> left.sameContent(right) }
        this is BulletList && other is BulletList ->
            items.sameItems(other.items)
        this is OrderedList && other is OrderedList ->
            start == other.start && items.sameItems(other.items)
        else -> false
    }
}

private fun List<KmdListItem>.sameItems(other: List<KmdListItem>): Boolean {
    if (size != other.size) return false
    return zip(other).all { (left, right) ->
        left.children.size == right.children.size &&
            left.children.zip(right.children).all { (a, b) -> a.sameContent(b) }
    }
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
    }
}

private fun KmdListItem.rekey(ids: IdGenerator): KmdListItem {
    return KmdListItem(
        id = ids.next(),
        children = children.map { it.rekey(ids) },
    )
}
