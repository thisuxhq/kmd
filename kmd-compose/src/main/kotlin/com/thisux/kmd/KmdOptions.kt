package com.thisux.kmd

data class KmdOptions(
    val softBreakAsNewline: Boolean = false,
) {
    companion object {
        val Default: KmdOptions = KmdOptions()
    }
}
