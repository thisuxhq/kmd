# Architecture

KMD is split so the document model can live without Compose, and Compose can live without Material.

---

## High level

```text
Markdown Input
      │
      ▼
┌───────────────┐
│ Input Engine  │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Parser Adapter│
└───────┬───────┘
        │
        ▼
┌───────────────┐
│    KMD AST    │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Block Engine  │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│Renderer Registry│
└───────┬───────┘
        │
        ▼
      Compose
```

The parser is behind an adapter. The renderer never sees parser types.

---

## Modules

Start with:

```text
kmd-core
kmd-compose
kmd-compose-material3
```

Later:

```text
kmd-images
kmd-highlight
kmd-gfm
kmd-math
kmd-streaming
```

Eventually:

```text
kmd-test
kmd-benchmark
```

Repository:

```text
kmd/
├── kmd-core/
├── kmd-compose/
├── kmd-compose-material3/
├── sample/
├── benchmark/
├── docs/
└── gradle/
```

---

## kmd-core

No Compose dependency.

Responsibilities:

```text
Markdown parsing
KMD AST
incremental state
block identity
extensions
parser adapter
document snapshots
```

It must work from `commonMain`.

```kotlin
val engine = KmdEngine()
val document = engine.parse(markdown)
```

---

## KMD AST

KMD defines its own AST. This is the contract between parser and renderer.

```kotlin
data class KmdDocument(
    val blocks: List<KmdBlock>
)

sealed interface KmdBlock
```

Blocks:

```kotlin
data class Heading(
    val level: Int,
    val content: List<KmdInline>
) : KmdBlock

data class Paragraph(
    val content: List<KmdInline>
) : KmdBlock

data class CodeBlock(
    val language: String?,
    val code: String
) : KmdBlock

data class BlockQuote(
    val children: List<KmdBlock>
) : KmdBlock

data class BulletList(
    val items: List<KmdListItem>
) : KmdBlock

data class OrderedList(
    val start: Int,
    val items: List<KmdListItem>
) : KmdBlock

data class HorizontalRule(
    val id: String
) : KmdBlock
```

Inline:

```kotlin
sealed interface KmdInline

data class Text(val value: String) : KmdInline
data class Strong(val children: List<KmdInline>) : KmdInline
data class Emphasis(val children: List<KmdInline>) : KmdInline
data class Strike(val children: List<KmdInline>) : KmdInline
data class InlineCode(val code: String) : KmdInline
data class Link(val destination: String, val children: List<KmdInline>) : KmdInline
data class Image(val source: String, val alt: String?) : KmdInline
```

Lists retain hierarchy. They are not flattened.

Every block carries a stable `KmdBlockId`.

---

## Parser adapter

V1 does not write a parser.

```kotlin
interface KmdParser {
    fun parse(markdown: String): KmdDocument
}
```

Implementation:

```text
JetBrainsMarkdownParser
```

Later, without changing the renderer:

```text
KmdNativeParser
CommonMarkParser
ExperimentalStreamingParser
```

The parser dependency stays behind this interface.

---

## Block engine

The block engine:

```text
assigns stable identities
tracks the active (streaming) block
produces snapshots
decides what can be reused
```

Snapshot:

```kotlin
data class KmdSnapshot(
    val blocks: List<KmdBlock>,
    val activeBlock: KmdBlock?,
    val revision: Long
)
```

Engine:

```kotlin
class KmdEngine {
    fun append(input: String): KmdSnapshot
    fun reset()
    fun replace(markdown: String): KmdSnapshot
}
```

See [Streaming](streaming.md).

---

## Renderer

```kotlin
@Composable
internal fun RenderDocument(
    document: KmdDocument
)
```

Each block routes through a renderer:

```kotlin
when (block) {
    is Heading -> RenderHeading(block)
    is Paragraph -> RenderParagraph(block)
    is CodeBlock -> RenderCode(block)
    is BlockQuote -> RenderQuote(block)
    is BulletList -> RenderBulletList(block)
}
```

Hardcoding this is fine for V1. Phase 2 replaces the `when` with a registry.

KMD should not force `LazyColumn`. Default `Kmd` can be a normal column. `LazyKmd` is a later API for large documents.

---

## Styling vs rendering vs loading

Keep these separate:

| Concern | Lives in | Replaceable by |
|---|---|---|
| Document shape | `kmd-core` AST | no |
| Parse | `KmdParser` | yes, internally |
| Colors, type, space | `KmdStyle` | yes, immediately |
| Block UI | renderer registry | yes, phase 2 |
| Images | `KmdImageRenderer` | yes |
| Highlighting | `KmdSyntaxHighlighter` | yes |
| Material mapping | `kmd-compose-material3` | optional module |

---

## Extensions

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

Extensions may add AST nodes and renderers. They should not force core to know about math, Mermaid, or GFM tables.

---

## Dependency rules

```text
kmd-core            → Kotlin only
kmd-compose         → kmd-core + Compose
kmd-compose-material3 → kmd-compose + Material 3
kmd-images          → kmd-compose + optional Coil / Kamel
kmd-highlight       → kmd-compose + a highlighter
kmd-gfm             → kmd-core (+ compose renderers)
```

Nothing in core depends on Coil, Material, a syntax engine, or a browser.
