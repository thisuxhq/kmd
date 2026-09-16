package com.thisux.kmd

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface KmdImageRenderer {
    @Composable
    fun render(image: Image, modifier: Modifier)
}
