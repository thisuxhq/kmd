package com.thisux.kmd.internal

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.thisux.kmd.KmdBlockId
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

// Stable holder, so moving the caret only recomposes the blocks it leaves and enters.
internal class KmdCaret {
    var blockId: KmdBlockId? by mutableStateOf(null)
}

internal val LocalKmdCaret = staticCompositionLocalOf<KmdCaret?> { null }

internal val LocalBlockEnterTracker = staticCompositionLocalOf<BlockEnterTracker?> { null }

internal val LocalKmdReducedMotion = staticCompositionLocalOf { false }

// Test hook: called each time RenderBlock composes. Null in production.
internal fun interface KmdBlockProbe {
    fun onCompose(id: KmdBlockId, showCaret: Boolean)
}

internal val LocalKmdBlockProbe = staticCompositionLocalOf<KmdBlockProbe?> { null }
