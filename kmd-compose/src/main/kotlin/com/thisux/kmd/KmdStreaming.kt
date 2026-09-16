package com.thisux.kmd

data class KmdStreaming(
    val caret: Boolean = false,
    val stagger: Boolean = false,
) {
    companion object {
        val None: KmdStreaming = KmdStreaming()
        val Default: KmdStreaming = KmdStreaming(caret = true, stagger = true)
    }
}
