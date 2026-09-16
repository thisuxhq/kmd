package com.thisux.kmd

fun interface KmdParser {
    fun parse(markdown: String): KmdDocument
}
