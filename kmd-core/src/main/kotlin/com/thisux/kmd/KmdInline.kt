package com.thisux.kmd

sealed interface KmdInline

data class Text(
    val value: String,
) : KmdInline

data class Strong(
    val children: List<KmdInline>,
) : KmdInline

data class Emphasis(
    val children: List<KmdInline>,
) : KmdInline

data class Strike(
    val children: List<KmdInline>,
) : KmdInline

data class InlineCode(
    val code: String,
) : KmdInline

data class Link(
    val destination: String,
    val children: List<KmdInline>,
) : KmdInline

data class Image(
    val source: String,
    val alt: String?,
) : KmdInline

data object SoftBreak : KmdInline

data object HardBreak : KmdInline
