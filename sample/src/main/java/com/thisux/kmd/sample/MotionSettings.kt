package com.thisux.kmd.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thisux.kmd.KmdStreaming

class MotionSettings {
    var caret by mutableStateOf(true)
    var stagger by mutableStateOf(true)
    var enterMillis by mutableIntStateOf(180)
    var staggerMillis by mutableIntStateOf(100)
    var tokenDelayMs by mutableIntStateOf(28)

    fun toStreaming(): KmdStreaming {
        return KmdStreaming(
            caret = caret,
            stagger = stagger,
            enterMillis = enterMillis,
            staggerMillis = staggerMillis,
        )
    }
}
