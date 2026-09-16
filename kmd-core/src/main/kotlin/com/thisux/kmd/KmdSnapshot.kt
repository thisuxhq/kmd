package com.thisux.kmd

data class KmdSnapshot(
    val document: KmdDocument,
    val activeBlock: KmdBlock?,
    val revision: Long,
)
