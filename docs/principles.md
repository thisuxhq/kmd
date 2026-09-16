# Principles

These are the rules we use when a feature, API, or dependency is in doubt.

---

## 1. Compose native

Everything rendered by KMD should become real Compose UI.

```md
> Important message
```

should become something conceptually similar to:

```text
Row
├── quote indicator
└── Text
```

Not:

```text
Markdown
↓
HTML
↓
WebView
```

If a feature needs a browser engine, it is out of scope.

---

## 2. KMP first

Core functionality lives in `commonMain`.

Platform-specific code should be kept small.

```text
                    KMD
                     │
             ┌───────┴────────┐
             │                │
         kmd-core       kmd-compose
             │                │
             └───────┬────────┘
                     │
       ┌─────────────┼─────────────┐
       │             │             │
    Android         iOS         Desktop
                                     │
                                    Web
```

`kmd-core` has no Compose dependency.

`kmd-compose` has no Material dependency.

Material 3 is an optional skin.

---

## 3. Streaming first

LLM streaming is not an afterthought.

KMD should understand:

```text
finalized blocks
+
active block
```

When a model streams:

```md
# Android Agent

Android agents can interact with apps.

- tap
- swipe
- typ
```

the first blocks are stable. Only the last one is changing.

KMD should avoid reparsing everything above it.

Priority:

```text
append-only streaming > arbitrary editing
```

---

## 4. Small public API

A developer should not need to understand our parser.

Most users need three things:

```kotlin
Kmd(markdown = markdown)

Kmd(markdown = markdown, style = KmdStyle(...))

Kmd(state = kmdState)
```

Everything else is either a default or an extension.

If an API exposes parser internals, it is the wrong API.

---

## 5. Parser-independent architecture

KMD owns its document representation.

```text
Markdown
   ↓
Parser Adapter
   ↓
KMD AST
   ↓
KMD Renderer
   ↓
Compose
```

Do not expose parser-specific AST nodes.

This lets us replace the parser later without changing the renderer or the public API.

---

## 6. Own the AST, not the parser

V1 does not invent a Markdown parser.

Use an existing Kotlin Multiplatform parser behind:

```kotlin
interface KmdParser {
    fun parse(markdown: String): KmdDocument
}
```

The parser is a detail. The AST is the product.

---

## 7. Stable identity

Every rendered block has a stable identity.

When block 4 is still streaming, blocks 1–3 keep their identities.

Compose can then skip unnecessary recomposition.

If identities churn, streaming is broken even if parsing is correct.

---

## 8. Pluggable where it varies, fixed where it shouldn't

Hardcode the document model.

Make these replaceable:

```text
block renderers
image loading
syntax highlighting
link handling
style
extensions
```

Do not make these replaceable in V1:

```text
the public composable
the KMD AST
the streaming state model
```

---

## 9. Material is optional

Core Compose rendering must not require Material.

```text
kmd-compose
kmd-compose-material3
```

Users should be able to use KMD with Material, Material 3, a custom design system, or nothing.

---

## 10. Do not execute the document

Never automatically:

- run HTML
- run JavaScript
- follow arbitrary URL protocols
- fetch remote images without a user-supplied loader

Links expose callbacks. Images are pluggable. Code is text until a highlighter is provided.

KMD renders. The host app decides side effects.
