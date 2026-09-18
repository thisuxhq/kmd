package com.thisux.kmd.internal

import com.thisux.kmd.KmdDocument

internal data class ParsedMarkdown(
    val document: KmdDocument,
    val starts: IntArray,
)
