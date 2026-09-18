# Accessibility

KMD emits real Compose UI, so it also emits real semantics.

A Markdown heading that is only big text is a bug.

---

## What must be present

| Markdown | Semantics |
|---|---|
| `# Heading` | heading, with level |
| `[label](url)` | clickable link, with label |
| `![alt](src)` | content description from alt |
| `- [x] task` | checkbox, checked state |
| lists | list / list item where the platform supports it |
| code | not announced as a heading or a link |

Screen readers we care about:

```text
TalkBack
VoiceOver
keyboard navigation
other Compose-supported readers
```

---

## Headings

Heading level is part of the node (`H1`–`H6`) and must reach semantics.

Visual style and semantics are separate. A custom renderer that makes an `H2` look like body text should still expose heading level 2 unless the user replaces that too.

---

## Links

Links are clickable through `onLinkClick`.

They must remain focusable and announce as links even when styled as body color.

KMD does not open the URL. The host does. Semantics still say "link".

---

## Images

If `alt` is present, it is the content description.

If `alt` is missing, the image should not pretend to be decorative if it is the only content in the block. Prefer an empty description only when the Markdown is explicitly decorative.

The image renderer is pluggable. Any Coil / Kamel adapter must pass alt through.

---

## Task lists

```md
- [x] Build parser
- [ ] Build renderer
```

These are checkboxes, not text with a unicode box.

Checked state belongs in semantics. If the host does not handle toggle, they can be read-only checkboxes, but they still expose state.

---

## Keyboard

Users should be able to:

```text
focus links
activate links
scroll the document
copy from selection where the platform allows it
```

Custom renderers inherit this responsibility. A replaced code block that drops copy and focus is the app's bug, but defaults must get it right.

---

## Selection and copy

```kotlin
SelectionContainer {
    Kmd(markdown)
}
```

Copy is an accessibility feature, not only a power-user one.

Do not disable selection in defaults.

Cross-block selection is verified per platform before we promise it. See [Performance](performance.md).

---

## Motion and overflow

Code blocks scroll horizontally instead of shrinking text to unreadability.

We do not add decorative animation in core.

If a renderer animates, it should respect `Modifier` / platform reduced-motion conventions where Compose exposes them.

---

## Custom renderers

The registry is how apps break a11y.

Replacing a block means replacing its semantics too.

KMD can make the default path correct. It cannot save a renderer that draws a link as non-clickable text.
