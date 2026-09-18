# Developer Experience

The developer experience should feel like using `Text()`.

```kotlin
Text("Hello")
```

```kotlin
Kmd("# Hello")
```

That level of simplicity is the goal.

---

## Installation

Eventually:

```kotlin
implementation("io.thisux:kmd:<version>")
```

Suggested package:

```text
com.thisux.kmd
```

Material 3 is a separate artifact. A user who does not want Material should not have to take it.

---

## The three calls

Almost every user should live here.

**Render a string**

```kotlin
Kmd(
    markdown = """
        # Hello

        Welcome to **KMD**.
    """.trimIndent()
)
```

Or:

```kotlin
Kmd("# Hello world")
```

**Style it**

```kotlin
Kmd(
    markdown = markdown,
    style = KmdStyle(...)
)
```

**Stream it**

```kotlin
val state = rememberKmdState()

Kmd(state = state)

llm.tokens.collect { token ->
    state.append(token)
}
```

If a use case needs more than this, it should still start here.

---

## What should not leak

A developer should not need to know:

```text
which parser we use
how the AST is shaped
how block identity is assigned
how the active block is detected
how Compose keys are chosen
```

Those are our problems.

The public types they *may* meet:

```text
Kmd
KmdStyle
KmdOptions
KmdState
KmdDocument          // snapshot / advanced
KmdRenderers
KmdImageRenderer
KmdSyntaxHighlighter
LazyKmd
```

Parser nodes never appear.

---

## Styling

Styling is strongly typed. No CSS, no HTML classes, no magic strings.

```kotlin
Kmd(
    markdown = text,
    style = KmdDefaults.style()
)
```

Custom:

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

Material 3 users get a ready-made mapping:

```kotlin
Kmd(
    markdown = markdown,
    style = KmdMaterial3.style()
)
```

---

## Links

The host app owns navigation.

```kotlin
Kmd(
    markdown = markdown,
    onLinkClick = { url ->
        open(url)
    }
)
```

KMD never auto-opens arbitrary protocols.

---

## Streaming operations

```kotlin
state.append(text)
state.reset()
state.replace(text)
state.snapshot()
```

`append` is the hot path. It should be cheap.

`replace` exists for corrections and full reloads. It does not need to be as cheap as append in V1.

---

## Replacement without forking

Users can replace the pieces that always differ by app:

```kotlin
Kmd(
    markdown = markdown,
    renderers = KmdRenderers {
        link { link, modifier -> MyLink(link, modifier) }
        image { image, modifier -> MyImage(image, modifier) }
        codeBlock { block, modifier -> MyCodeBlock(block.code, modifier) }
    }
)
```

Chat apps replace code blocks.

Doc apps replace images.

Design systems replace headings.

Nobody should copy the library to change a code block.

---

## Errors and incomplete Markdown

Streaming produces incomplete Markdown constantly:

```text
#
## Hel
## Hello

This is **som
This is **some text**
```

DX here means:

```text
do not crash
do not flash broken layout if we can avoid it
keep already-finished blocks on screen
treat the tail as an active block
```

The user of the library should not write a "partial Markdown" handler. That is KMD's job.

---

## What "feels like Text()" actually means

| `Text` | `Kmd` |
|---|---|
| One composable | One composable |
| String in | String in |
| Style object | Style object |
| Works in common code | Works in common code |
| No engine to configure | No parser to configure |
| Recomposition is cheap | Stable blocks do not recompose |

If we need a builder, a plugin host, or a documentation page before `Kmd("# Hello")` works, the DX is wrong.
