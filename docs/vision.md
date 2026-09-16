# Vision

KMD should become the default Markdown rendering engine for Compose applications.

A developer should be able to write:

```kotlin
Kmd(
    markdown = content
)
```

and get beautiful, native Markdown.

No WebView.

No HTML rendering layer.

No JavaScript.

No browser engine.

Just Kotlin + Compose.

---

## The bar

Using KMD should feel like using Compose itself.

```kotlin
Text("Hello")
```

should feel similar to:

```kotlin
Kmd("# Hello")
```

Same mental model. Same platform. Same composition.

---

## Where it lives

KMD should work across:

```text
Android
iOS
Desktop
Web / Wasm
```

using Compose Multiplatform.

The core of the product is not Android-shaped Markdown that happens to compile elsewhere. It is KMP-first Markdown that happens to look native on every target.

---

## Why this exists

Markdown is the language of:

```text
AI chat
documentation
notes
READMEs
release notes
developer tools
```

Those products now ship as Compose apps. The rendering layer they need does not exist in a form that is native, streaming-friendly, and small.

KMD should be that layer.

---

## Success

KMD is successful when a Compose developer reaches for it the same way they reach for `Text`, `LazyColumn`, or Coil — without thinking about parsers, HTML, or platform quirks.
