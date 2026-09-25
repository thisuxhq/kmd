# Phases

KMD ships in three phases.

V1 is a complete product: Markdown → native Compose UI, with streaming.

V2 makes that product the one people customize.

Later makes it the one people extend.

---

## Phase 1 — Core

**Goal:** a developer can render Markdown as native Compose UI, including streaming LLM output, with a tiny API.

### Modules

```text
kmd-core
kmd-compose
kmd-compose-material3
sample
```

### Public API

```kotlin
Kmd(markdown)
Kmd(markdown, style)
Kmd(state)
```

Streaming:

```kotlin
val state = rememberKmdState()
state.append(token)
```

### Markdown support

**Blocks**

```text
paragraphs
H1–H6
blockquotes
horizontal rules
fenced code blocks
indented code blocks
ordered lists
unordered lists
nested lists
```

**Inline**

```text
bold
italic
bold + italic
inline code
links
images (as data, with a pluggable renderer)
line breaks
escaped characters
```

### Architecture that must exist in V1

```text
parser adapter
KMD AST
stable block identity
append-only streaming
typed style
Material 3 as an optional module
link click callbacks
```

### Parser

Do not write a parser.

Use an existing KMP parser (JetBrains Markdown is the leading candidate) behind `KmdParser`.

### Code blocks

Default rendering includes:

```text
language label
copy action
horizontal scroll
```

Syntax highlighting is *not* in core. Default is plain text, with a highlighter interface ready.

### Explicitly out of V1

```text
our own complete Markdown parser
rich text editing
WYSIWYG
HTML / JS execution
every Markdown extension
tables, task lists, strikethrough
forced LazyColumn
full cross-block selection promises
```

V1 is:

> Markdown → native Compose UI.

If it does not serve that sentence, it waits.

---

## Phase 2 — GFM and replacement points

**Shipped.** GitHub-flavored content looks native, and visually important blocks can be replaced.

### GFM

```text
strikethrough
task lists
tables
autolinks
```

Landed in `kmd-core` / `kmd-compose`, not a separate `kmd-gfm` artifact. Always on.

Task lists render as real Compose checkboxes, not emoji.

Tables render as native rows and cells, with horizontal scrolling for wide content.

### Renderer registry

```kotlin
Kmd(
    markdown = markdown,
    renderers = KmdRenderers {
        codeBlock { block, modifier -> MyCodeBlock(block.code, modifier) }
        image { image, modifier -> MyImage(image, modifier) }
    }
)
```

Overrides receive `Modifier` and cover:

```text
heading
paragraph
code block
image
table
quote
bullet list
ordered list
horizontal rule
link
inline code
checkbox
```

### Images

```kotlin
interface KmdImageRenderer
```

`kmd-images` ships a Coil adapter. Core still does not depend on Coil.

### Syntax highlighting

```kotlin
interface KmdSyntaxHighlighter {
    fun highlight(language: String?, code: String): AnnotatedString
}
```

`kmd-highlight` ships a small keyword highlighter. Default remains plain text.

Possible later implementations:

```text
Tree-sitter
TextMate
custom highlighter
remote Shiki
```

### Lazy rendering

`Kmd` is still a normal column. Large documents use:

```kotlin
LazyKmd(markdown = readme)
```

keyed by stable block identity.

### Selection

`SelectionContainer { Kmd(markdown) }` works on Android. Do not promise full cross-block selection on every target until it is verified.

### Modules that exist

```text
kmd-images
kmd-highlight
```

Streaming stays in core. Split `kmd-streaming` only if the engine deserves its own artifact.

---

## Phase 3 — Extensions and depth

**Goal:** KMD is the Markdown engine people build on, not just the one they drop in.

Incremental append is shipped: `append` reparses the tail, not the whole buffer.

`KmdExtension` and GitHub alerts are shipped. Math and Mermaid wait.

`kmd-benchmark` is shipped: finished blocks are not rebuilt or recomposed on append. See [Performance](performance.md).

### Extension API

```kotlin
interface KmdExtension

KmdEngine(
    extensions = listOf(
        GfmAlerts,
        MathExtension
    )
)
```

Possible extensions:

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

### Custom blocks

Markdown such as:

```md
:::warning
Do not expose your API key.
```

should be able to map to a user-supplied Compose block.

This is the long-term power feature.

### Performance program

Benchmarks against:

```text
1 KB
10 KB
50 KB
100 KB Markdown
```

Streaming at:

```text
10 tokens/sec
50 tokens/sec
100 tokens/sec
```

Example target document: 10,000 characters, 50–100 blocks, mixed code, lists, and links.

Goals:

```text
initial render < 16–30 ms where practical
smooth scrolling at 60 FPS
minimal allocations during streaming
stable blocks should not recompose
```

### Accessibility

Generated Compose should preserve semantics:

```text
headings
links
images (content description)
checkboxes (checked state)
```

Verified with TalkBack, VoiceOver, keyboard navigation, and screen readers.

### Tooling modules

```text
kmd-math
kmd-test
kmd-benchmark
```

### Parser freedom

Because V1 hid the parser, later we can add:

```text
KmdNativeParser
CommonMarkParser
ExperimentalStreamingParser
```

without touching the renderer.

---

## What does not move between phases

These stay true from day one:

```text
no WebView
no HTML execution
no parser types in the public API
no Material requirement in core
append-only streaming as the first optimization
small public API
```
