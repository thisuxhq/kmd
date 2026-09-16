package com.thisux.kmd.internal

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.thisux.kmd.KmdImageRenderer
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdRenderers
import com.thisux.kmd.KmdStyle
import com.thisux.kmd.KmdSyntaxHighlighter

internal val LocalKmdStyle =
    compositionLocalOf<KmdStyle> {
        error("KmdStyle was not provided")
    }

internal val LocalKmdOptions = staticCompositionLocalOf { KmdOptions.Default }

internal val LocalKmdOnLinkClick = staticCompositionLocalOf<((String) -> Unit)?> { null }

internal val LocalKmdImageRenderer = staticCompositionLocalOf<KmdImageRenderer?> { null }

internal val LocalKmdSyntaxHighlighter = staticCompositionLocalOf<KmdSyntaxHighlighter?> { null }

internal val LocalKmdRenderers = staticCompositionLocalOf { KmdRenderers.Default }
