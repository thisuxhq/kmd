package com.thisux.kmd.sample

internal val SampleMarkdown =
    """
# KMD

Native Markdown for Compose.

A developer should be able to write `Kmd("# Hello")` and get real Compose UI. No WebView. No HTML. No JavaScript.

## Why this exists

Markdown is the language of:

- AI chat
- documentation
- notes
- READMEs

Those products now ship as Compose apps.

## Streaming

LLM output arrives a few characters at a time:

1. Kotlin
   - Android
   - Desktop
2. Swift

KMD keeps finished blocks stable and only updates the tail.

> Important: append-only streaming is the first optimization.

---

## Code

```kotlin
fun main() {
    println("Hello")
}
```

Links stay under the host app's control: [thisux](https://thisux.com).

![Diagram](https://example.com/diagram.png)
    """.trimIndent()
