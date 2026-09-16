package com.thisux.kmd

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object KmdDefaults {
    fun style(): KmdStyle = DefaultStyle
}

private val DefaultStyle =
    KmdStyle(
        typography =
            KmdTypography(
                paragraph =
                    TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                h1 =
                    TextStyle(
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                h2 =
                    TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                h3 =
                    TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                h4 =
                    TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                h5 =
                    TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                h6 =
                    TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                inlineCode =
                    TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                codeBlock =
                    TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
            ),
        colors =
            KmdColors(
                text = Color(0xFF1A1A1A),
                link = Color(0xFF0B57D0),
                codeBackground = Color(0xFFF1F3F4),
                divider = Color(0xFFE0E0E0),
            ),
        spacing =
            KmdSpacing(
                block = 12.dp,
                headingBottom = 8.dp,
                listIndent = 8.dp,
            ),
        codeBlock =
            KmdCodeBlockStyle(
                background = Color(0xFFF6F8FA),
                border = Color(0xFFE0E0E0),
                cornerRadius = 8.dp,
                contentPadding = 12.dp,
                languageColor = Color(0xFF5F6368),
                copyColor = Color(0xFF0B57D0),
            ),
        quote =
            KmdQuoteStyle(
                barColor = Color(0xFFBDBDBD),
                barWidth = 3.dp,
                background = Color(0x00000000),
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
