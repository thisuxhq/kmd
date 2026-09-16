# Extensions

Core Markdown stays small.

Anything beyond V1 blocks should arrive as an extension, not a core special case.

---

## Shape

```kotlin
interface KmdExtension
```

```kotlin
KmdEngine(
    extensions = listOf(
        GfmExtension,
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
| GFM | Phase 2 | strikethrough, task lists, tables, autolinks |
| Images | Phase 2 | Coil / Kamel / custom loaders |
| Highlight | Phase 2 | Tree-sitter, TextMate, Shiki, custom |
| Math | later | formulas as native UI or a supplied renderer |
| Mermaid | later | diagrams, host-supplied renderer |
| Directives | later | `:::warning` custom blocks |

---

## Custom blocks

Long term, this is one of the most powerful parts of KMD.

```md
:::warning
Do not expose your API key.
```

That should map to a user-supplied Compose block, not a paragraph with extra punctuation.

The AST grows a node. The registry supplies the UI. Core does not know what a warning is.

---

## Renderer registry

Phase 2 makes individual blocks replaceable without a full extension:

```kotlin
Kmd(
    markdown = markdown,
    renderers = KmdRenderers {
        codeBlock { block -> MyCodeBlock(block.code) }
        link { link -> MyLink(link) }
        image { image -> MyImage(image) }
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

GFM is the test of the extension API.

If tables, task lists, and strikethrough cannot land without patching core renderers by hand, the API is wrong.

They should:

```text
add nodes
add renderers
reuse style
keep streaming identities
```

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
