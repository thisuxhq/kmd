package com.thisux.kmd

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

object KmdMaterial3 {
    @Composable
    fun style(): KmdStyle {
        val colorScheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        return remember(colorScheme, typography) {
            KmdStyle(
                typography =
                    KmdTypography(
                        paragraph = typography.bodyLarge,
                        h1 = typography.displaySmall,
                        h2 = typography.headlineLarge,
                        h3 = typography.headlineMedium,
                        h4 = typography.headlineSmall,
                        h5 = typography.titleLarge,
                        h6 = typography.titleMedium,
                        inlineCode = typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        codeBlock = typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    ),
                colors =
                    KmdColors(
                        text = colorScheme.onSurface,
                        link = colorScheme.primary,
                        codeBackground = colorScheme.surfaceVariant,
                        divider = colorScheme.outlineVariant,
                    ),
                spacing =
                    KmdSpacing(
                        block = 12.dp,
                        headingBottom = 8.dp,
                        listIndent = 8.dp,
                    ),
                codeBlock =
                    KmdCodeBlockStyle(
                        background = colorScheme.surfaceVariant,
                        border = colorScheme.outlineVariant,
                        cornerRadius = 8.dp,
                        contentPadding = 12.dp,
                        languageColor = colorScheme.onSurfaceVariant,
                        copyColor = colorScheme.primary,
                    ),
                quote =
                    KmdQuoteStyle(
                        barColor = colorScheme.primary,
                        barWidth = 3.dp,
                        background = colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentPadding = 12.dp,
                    ),
                list =
                    KmdListStyle(
                        bullet = "•",
                        markerWidth = 24.dp,
                        itemSpacing = 4.dp,
                        indent = 8.dp,
                    ),
            )
        }
    }
}
