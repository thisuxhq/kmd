package com.thisux.kmd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class KmdState internal constructor(
    private val engine: KmdEngine,
) {
    private var current by mutableStateOf(engine.snapshot())

    val document: KmdDocument
        get() = current.document

    fun append(value: String) {
        current = engine.append(value)
    }

    fun reset() {
        engine.reset()
        current = engine.snapshot()
    }

    fun replace(value: String) {
        current = engine.replace(value)
    }

    fun snapshot(): KmdSnapshot = current
}

@Composable
fun rememberKmdState(
    initialMarkdown: String = "",
    extensions: List<KmdExtension> = emptyList(),
): KmdState {
    return remember(extensions) {
        val engine = KmdEngine(extensions = extensions)
        if (initialMarkdown.isNotEmpty()) {
            engine.replace(initialMarkdown)
        }
        KmdState(engine)
    }
}
