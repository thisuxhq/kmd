package com.thisux.kmd.internal

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.thisux.kmd.BlockQuote
import com.thisux.kmd.BulletList
import com.thisux.kmd.KmdBlock
import com.thisux.kmd.KmdBlockId
import com.thisux.kmd.OrderedList
import kotlin.math.roundToInt

internal val StreamEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

internal class BlockEnterTracker {
    private val seen = mutableSetOf<Long>()
    private var primed = false
    private var wave = 0

    fun onFrame(ids: List<Long>) {
        if (!primed) {
            seen.addAll(ids)
            primed = true
        }
        wave = 0
    }

    fun consume(id: Long): Int? {
        if (!primed) {
            seen.add(id)
            return null
        }
        if (!seen.add(id)) return null
        return wave++
    }
}

internal fun KmdBlock.caretLeafId(): KmdBlockId {
    return when (this) {
        is BlockQuote -> children.lastOrNull()?.caretLeafId() ?: id
        is BulletList -> items.lastOrNull()?.children?.lastOrNull()?.caretLeafId() ?: id
        is OrderedList -> items.lastOrNull()?.children?.lastOrNull()?.caretLeafId() ?: id
        else -> id
    }
}

@Composable
internal fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}

internal fun Modifier.streamingEnter(blockId: Long): Modifier =
    composed {
        val streaming = LocalKmdOptions.current.streaming
        val reduced = LocalKmdReducedMotion.current
        val tracker = LocalBlockEnterTracker.current
        if (!streaming.stagger || reduced || tracker == null) {
            return@composed this
        }
        val wave = remember(blockId) { tracker.consume(blockId) }
        if (wave == null) {
            return@composed this
        }
        val progress = remember { Animatable(0f) }
        val offsetPx = with(LocalDensity.current) { 4.dp.toPx() }
        LaunchedEffect(Unit) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = 180,
                        delayMillis = wave * 100,
                        easing = StreamEasing,
                    ),
            )
        }
        graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * offsetPx
        }
    }

@Composable
internal fun KmdText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    showCaret: Boolean = false,
) {
    if (!showCaret) {
        BasicText(text = text, style = style, modifier = modifier)
        return
    }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Box(modifier) {
        BasicText(
            text = text,
            style = style,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { layout = it },
        )
        val rect = layout?.getCursorRect(text.length)
        if (rect != null) {
            StreamingCaret(
                Modifier
                    .offset { IntOffset(rect.left.roundToInt(), rect.top.roundToInt()) }
                    .height(with(LocalDensity.current) { rect.height.toDp() }),
            )
        }
    }
}

@Composable
private fun StreamingCaret(modifier: Modifier) {
    val color = LocalKmdStyle.current.colors.text
    val reduced = LocalKmdReducedMotion.current
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(reduced) {
        if (reduced) {
            alpha.snapTo(1f)
            return@LaunchedEffect
        }
        alpha.animateTo(
            targetValue = 0.15f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(800, easing = StreamEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )
    }
    Box(
        modifier
            .width(2.dp)
            .graphicsLayer { this.alpha = alpha.value }
            .background(color),
    )
}
