# Extensions

Core Markdown stays small.

Anything beyond V1 blocks should arrive as an extension, not a core special case.

---

## Shape

```kotlin
fun interface KmdExtension {
    fun process(document: KmdDocument): KmdDocument
}
```

```kotlin
Kmd(
    markdown = markdown,
    extensions = listOf(GfmAlerts)
)
```

```kotlin
KmdEngine(
    extensions = listOf(
        GfmAlerts,
        MathExtension
    )
)
```

An extension may:

```text
parse extra syntax (through the adapter)
add AST nodes
register renderers
contribute style defaults
```

An extension must not:

```text
force Material
force Coil
force a syntax engine
leak parser types
```

---

## Planned extensions

| Extension | When | What |
|---|---|---|
| GFM | shipped in core | strikethrough, task lists, tables, autolinks |
| Images | shipped (`kmd-images`) | Coil adapter; custom loaders via `KmdImageRenderer` |
| Highlight | shipped (`kmd-highlight`) | keyword highlighter; Tree-sitter / TextMate / Shiki later |
| Alerts | shipped (`GfmAlerts`) | GitHub `> [!NOTE]` as `CustomBlock` |
| Math | later | formulas as native UI or a supplied renderer |
| Mermaid | later | diagrams, host-supplied renderer |
| Directives | later | `:::warning` custom blocks |

GFM tables landed in core. Alerts use the extension API: `GfmAlerts` rewrites matching blockquotes into `CustomBlock(name = "alert")`. Enable it explicitly.

---

## Custom blocks

Long term, this is one of the most powerful parts of KMD.

```md
:::warning
Do not expose your API key.
```

That should map to a user-supplied Compose block, not a paragraph with extra punctuation.

Unknown syntax becomes `CustomBlock`. The registry looks it up by name:

```kotlin
Kmd(
    markdown = markdown,
    extensions = listOf(GfmAlerts),
    renderers = KmdRenderers {
        custom("alert") { block, modifier -> MyAlert(block, modifier) }
    }
)
```

Core does not know what a warning is. Defaults render GitHub alerts as a labeled quote.

---

## Renderer registry

Individual blocks are replaceable without a full extension:

```kotlin
Kmd(
    markdown = markdown,
    renderers = KmdRenderers {
        codeBlock { block, modifier -> MyCodeBlock(block.code, modifier) }
        link { link, modifier -> MyLink(link, modifier) }
        image { image, modifier -> MyImage(image, modifier) }
    }
)
```

Use a renderer override when the syntax already exists.

Use an extension when the syntax does not.

---

## Images

Core representation:

```kotlin
Image(
    source = "...",
    alt = "..."
)
```

Compose side:

```kotlin
interface KmdImageRenderer
```

Adapters live outside core:

```text
Coil
Kamel
custom loader
```

No image library in `kmd-core` or `kmd-compose`.

---

## Syntax highlighting

```kotlin
interface KmdSyntaxHighlighter {
    fun highlight(
        language: String?,
        code: String
    ): AnnotatedString
}
```

Default: plain text.

Possible later implementations:

```text
Tree-sitter
TextMate
custom highlighter
remote Shiki
```

Do not ship a large highlighter inside core.

---

## GFM as the first real extension

GFM was supposed to be the test of the extension API.

It did not wait. Tables, task lists, strikethrough, and autolinks are built into `kmd-core` and `kmd-compose`.

That is acceptable for syntax everyone needs. It is not the pattern for alerts, math, or directives. Those must add nodes and renderers without patching core by hand.

---

## Future syntax we will not special-case in core

```text
math
Mermaid
footnotes
alerts
mentions
hashtags
frontmatter
custom directives
```

If someone needs them, they build or enable an extension.
