# Styling

Styling is strongly typed Compose state, not CSS and not HTML classes.

---

## Shape

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

Usage:

```kotlin
Kmd(
    markdown = text,
    style = KmdDefaults.style()
)
```

Override:

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

---

## Defaults vs Material

`kmd-compose` ships defaults that work with no design system.

`kmd-compose-material3` maps Material tokens:

```kotlin
Kmd(
    markdown = markdown,
    style = KmdMaterial3.style()
)
```

Users can run KMD with:

```text
Material
Material 3
a custom design system
no Material
```

Core rendering must not import Material.

---

## What style covers

```text
body and heading type
text, link, code, quote colors
block spacing
list markers and indent
quote bar
code block chrome
```

What style does **not** cover:

```text
replacing a code block with a different composable
image loading
syntax highlighting
link navigation
```

Those are renderers, loaders, and callbacks. See [API](api.md).

---

## Replacement vs restyle

If changing colors is enough, use `KmdStyle`.

If the UI structure is wrong for the app, replace the renderer:

```kotlin
Kmd(
    markdown = markdown,
    renderers = KmdRenderers {
        codeBlock { block, modifier -> MyCodeBlock(block.code, modifier) }
        quote { quote, modifier -> MyCallout(quote, modifier) }
    }
)
```

Chat apps restyle less and replace more.

Doc apps restyle more and replace less.

---

## Code blocks

`KmdCodeBlockStyle` should cover the default chrome:

```text
background
radius
padding
language label
copy control
code typeface
```

Highlighting is a separate interface. Style colors the un-highlighted path and any `AnnotatedString` the highlighter returns.

---

## Quotes and lists

Quote:

```text
bar color
bar width
content indent
background (optional)
```

List:

```text
bullet
number style
indent per level
item spacing
```

Nested lists inherit indent. They do not flatten.

---

## Dark mode

Style is just data. The host passes a light or dark `KmdStyle`.

KMD does not own theme switching.

Material 3 users should derive `KmdMaterial3.style()` from the current `ColorScheme` and `Typography` so it follows the app automatically.
