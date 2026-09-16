package com.thisux.kmd.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.thisux.kmd.CodeBlock

@Composable
internal fun RenderCodeBlock(
    block: CodeBlock,
    modifier: Modifier,
) {
    val style = LocalKmdStyle.current
    val highlighter = LocalKmdSyntaxHighlighter.current
    val clipboard = LocalClipboardManager.current
    val chrome = style.codeBlock
    val shape = RoundedCornerShape(chrome.cornerRadius)
    val highlighted =
        remember(block.code, block.language, highlighter) {
            highlighter?.highlight(block.language, block.code) ?: AnnotatedString(block.code)
        }

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(chrome.background)
            .border(1.dp, chrome.border, shape)
            .padding(chrome.contentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = block.language ?: "text",
                style = style.typography.inlineCode.copy(color = chrome.languageColor),
            )
            BasicText(
                text = "Copy",
                style = style.typography.inlineCode.copy(color = chrome.copyColor),
                modifier =
                    Modifier
                        .clickable { clipboard.setText(AnnotatedString(block.code)) }
                        .semantics { contentDescription = "Copy code" },
            )
        }
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = highlighted,
            style = style.typography.codeBlock.copy(color = style.colors.text),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        )
    }
}
