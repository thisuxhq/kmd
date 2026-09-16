package com.thisux.kmd

data class KmdOptions(
    val softBreakAsNewline: Boolean = false,
    val streaming: KmdStreaming = KmdStreaming.None,
) {
    companion object {
        val Default: KmdOptions = KmdOptions()
    }
}
