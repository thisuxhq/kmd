# Mission

Make Markdown rendering in Compose:

```text
simple
fast
native
customizable
streaming-friendly
multiplatform
```

---

## Simple

The default path is one composable and a string.

```kotlin
Kmd("# Hello")
```

A developer should not need to understand our parser, AST, or block engine to render a README.

---

## Fast

Markdown is often large, and LLM output arrives as a stream of tokens.

KMD should:

- parse incrementally where it matters
- keep stable blocks stable
- avoid full-document rerenders on every token
- stay smooth while scrolling

Performance is a product feature, not a later optimization.

---

## Native

Everything KMD renders should become real Compose UI.

A blockquote is a `Row` with a quote indicator and text.

A heading is a heading.

A code block is a Compose surface with language, copy, and scroll.

Not Markdown → HTML → WebView.

---

## Customizable

Defaults should look good.

Overrides should be typed, local, and replaceable:

```text
style
renderers
images
syntax highlighting
link clicks
```

Users should be able to match their design system without forking the library.

---

## Streaming-friendly

AI apps are a primary audience, not a special case.

KMD should understand:

```text
finalized blocks
+
active block
```

and only work on what is still changing.

---

## Multiplatform

Core logic lives in `commonMain`.

Platform-specific code stays small.

If a feature cannot exist on every Compose target, it does not belong in core.
