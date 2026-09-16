package com.thisux.kmd

import androidx.compose.ui.text.AnnotatedString

fun interface KmdSyntaxHighlighter {
    fun highlight(
        language: String?,
        code: String,
    ): AnnotatedString
}
