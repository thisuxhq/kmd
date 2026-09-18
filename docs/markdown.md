# Markdown support

V1 covers CommonMark-shaped documents that actually appear in apps.

GFM is on: strikethrough, task lists, tables, and autolinks.

---

## V1 blocks

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

---

## V1 inline

```text
bold
italic
bold + italic
inline code
links
images
line breaks
escaped characters
```

Images are data in V1. Loading pixels is pluggable. Without a loader, KMD shows alt text or a placeholder.

---

## Code blocks

Markdown:

````md
```kotlin
fun main() {
    println("Hello")
}
```
````

Default UI:

```text
┌─────────────────────────────┐
│ kotlin               copy   │
│                             │
│ fun main() {                │
│     println("Hello")        │
│ }                           │
└─────────────────────────────┘
```

Always:

```text
language label
copy action
horizontal scroll
```

Not in core:

```text
a bundled syntax engine
```

Highlighter hook:

```kotlin
interface KmdSyntaxHighlighter {
    fun highlight(
        language: String?,
        code: String
    ): AnnotatedString
}
```

Default: plain text.

---

## Lists

Support:

```text
unordered
ordered
nested
mixed
```

Example:

```md
1. Kotlin
   - Android
   - Desktop
2. Swift
```

The AST keeps hierarchy. Nested lists are children, not a flattened stream of bullets.

Task lists are GFM and are on.

---

## Links

```kotlin
Kmd(
    markdown = markdown,
    onLinkClick = { url ->
        open(url)
    }
)
```

The host owns navigation.

KMD never auto-executes protocols.

---

## GFM

Shipped in core. Not a separate module.

```text
strikethrough
task lists
tables
autolinks
```

Task lists:

```md
- [x] Build parser
- [ ] Build renderer
```

These become native Compose checkboxes, not emoji.

Tables:

```md
| Name | Role |
|------|------|
| Sam  | Dev  |
| Mia  | PM   |
```

```text
┌────────┬──────┐
│ Name   │ Role │
├────────┼──────┤
│ Sam    │ Dev  │
│ Mia    │ PM   │
└────────┬──────┘
```

Wide tables scroll horizontally. Cells can later be replaced through the renderer registry.

---

## Later

Not promised, but the extension API should make them possible:

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

---

## Incomplete input

Streaming will produce invalid Markdown. That is still supported input.

```text
unclosed emphasis
unclosed fences
a heading that is still #
a half-written link
```

Do not crash. Keep finalized blocks. Best-effort the tail.

Details in [Streaming](streaming.md).
