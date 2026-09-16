package com.thisux.kmd.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun SettingsPane(
    settings: MotionSettings,
    onPlay: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Streaming motion", style = MaterialTheme.typography.titleMedium)
        Text(
            "Default is still. These only apply on the Stream tab.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingSwitch(
            title = "Caret",
            body = "Blink at the end of the active block",
            checked = settings.caret,
            onCheckedChange = { settings.caret = it },
        )
        SettingSwitch(
            title = "Stagger new blocks",
            body = "Fade and rise 4dp when a block is first committed",
            checked = settings.stagger,
            onCheckedChange = { settings.stagger = it },
        )

        SettingSlider(
            title = "Enter duration",
            valueLabel = "${settings.enterMillis} ms",
            value = settings.enterMillis.toFloat(),
            range = 80f..400f,
            onChange = { settings.enterMillis = it.roundToInt() },
            enabled = settings.stagger,
        )
        SettingSlider(
            title = "Stagger delay",
            valueLabel = "${settings.staggerMillis} ms",
            value = settings.staggerMillis.toFloat(),
            range = 0f..250f,
            onChange = { settings.staggerMillis = it.roundToInt() },
            enabled = settings.stagger,
        )
        SettingSlider(
            title = "Token delay",
            valueLabel = "${settings.tokenDelayMs} ms",
            value = settings.tokenDelayMs.toFloat(),
            range = 8f..80f,
            onChange = { settings.tokenDelayMs = it.roundToInt() },
        )

        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Play stream")
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingSlider(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    enabled: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            enabled = enabled,
        )
    }
}
