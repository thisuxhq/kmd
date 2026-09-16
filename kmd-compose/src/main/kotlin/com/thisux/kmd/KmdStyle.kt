package com.thisux.kmd

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class KmdStyle(
    val typography: KmdTypography,
    val colors: KmdColors,
    val spacing: KmdSpacing,
    val codeBlock: KmdCodeBlockStyle,
    val quote: KmdQuoteStyle,
    val list: KmdListStyle,
)

data class KmdTypography(
    val paragraph: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val inlineCode: TextStyle,
    val codeBlock: TextStyle,
)

data class KmdColors(
    val text: Color,
    val link: Color,
    val codeBackground: Color,
    val divider: Color,
)

data class KmdSpacing(
    val block: Dp,
    val headingBottom: Dp,
    val listIndent: Dp,
)

data class KmdCodeBlockStyle(
    val background: Color,
    val border: Color,
    val cornerRadius: Dp,
    val contentPadding: Dp,
    val languageColor: Color,
    val copyColor: Color,
)

data class KmdQuoteStyle(
    val barColor: Color,
    val barWidth: Dp,
    val background: Color,
    val contentPadding: Dp,
)

data class KmdListStyle(
    val bullet: String,
    val markerWidth: Dp,
    val itemSpacing: Dp,
    val indent: Dp,
)
