package com.thisux.kmd.internal

import com.thisux.kmd.KmdBlockId

internal class IdGenerator {
    private var next = 1L

    fun next(): KmdBlockId = KmdBlockId(next++)
}
