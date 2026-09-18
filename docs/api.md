# API

The public API stays small on purpose.

Most apps should only meet `Kmd`, `KmdStyle`, and `KmdState`.

---

## Composable

```kotlin
@Composable
fun Kmd(
    markdown: String,
    modifier: Modifier = Modifier,
    style: KmdStyle = KmdDefaults.style(),
    options: KmdOptions = KmdOptions.Default
)
```

Simple:

```kotlin
Kmd("# Hello world")
```

Styled:

```kotlin
Kmd(
    markdown = markdown,
    style = KmdStyle(...)
)
```

Streaming:

```kotlin
Kmd(
    state = kmdState
)
```

---

## Streaming state

```kotlin
val state = rememberKmdState()
```

```kotlin
interface KmdState {
    val document: KmdDocument
    fun append(value: String)
    fun reset()
    fun replace(value: String)
}
```

Typical LLM loop:

```kotlin
llm.tokens.collect { token ->
    state.append(token)
}
```

Other operations:

```kotlin
state.append(text)
state.reset()
state.replace(text)
state.snapshot()
```

`append` is the path we optimize. `replace` reloads the document.

---

## Style

```kotlin
data class KmdStyle(
    val typography: KmdTypography,
    val colors: KmdColors,
    val spacing: KmdSpacing,
    val codeBlock: KmdCodeBlockStyle,
    val quote: KmdQuoteStyle,
    val list: KmdListStyle
)
```

Defaults:

```kotlin
Kmd(
    markdown = text,
    style = KmdDefaults.style()
)
```

Copy and override:

```kotlin
Kmd(
    markdown = text,
    style = KmdDefaults.style().copy(
        colors = KmdColors(
            text = Color.White,
            link = Color.Cyan
        )
    )
)
```

Material 3:

```kotlin
Kmd(
    markdown = markdown,
    style = KmdMaterial3.style()
)
```

Core Compose rendering does not require Material.

---

## Options and callbacks

Links:

```kotlin
Kmd(
    markdown = markdown,
    onLinkClick = { url ->
        open(url)
    }
)
```

Images, highlighting, and block overrides arrive as explicit types, not hidden globals.

```kotlin
interface KmdImageRenderer

interface KmdSyntaxHighlighter {
    fun highlight(
        language: String?,
        code: String
    ): AnnotatedString
}
```

Default highlighter: plain text.

Default image renderer: alt text / placeholder until the host supplies a loader.

---

## Renderer registry

```kotlin
Kmd(
    markdown = markdown,
    renderers = KmdRenderers {
        codeBlock { block ->
            MyCodeBlock(block.code)
        }
    }
)
```

Overridable blocks:

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
```

Link, inline code, and checkbox overrides are not shipped yet. Overrides do not yet receive the `Modifier` KMD would have applied.

---

## Lazy API

Not forced on the default composable:

```kotlin
LazyKmd(
    markdown = readme
)
```

Conceptually:

```kotlin
LazyColumn {
    items(
        document.blocks,
        key = { it.id }
    ) { block ->
        RenderBlock(block)
    }
}
```

---

## Document types

Advanced users and extensions may read the AST. They should not need to write it.

```kotlin
data class KmdDocument(
    val blocks: List<KmdBlock>
)

data class KmdBlockId(
    val value: Long
)
```

Parser types are never public.

---

## Selection

Users should be able to wrap KMD:

```kotlin
SelectionContainer {
    Kmd(markdown)
}
```

Do not promise full cross-block selection on every target until it is verified.

---

## What stays internal

```text
parser AST
block identity generation
active-block detection
input engine
default renderer implementations
```

If a type is only needed to implement KMD, it is not part of the API.
