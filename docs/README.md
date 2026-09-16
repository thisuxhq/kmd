# KMD

**Native Markdown for Compose.**

KMD is a fast Markdown rendering engine for Compose Multiplatform. It turns Markdown into real Compose UI — no WebView, no HTML, no JavaScript.

```kotlin
Kmd("# Hello")
```

That should feel as simple as `Text("Hello")`.

---

## Docs

| Doc | What it covers |
|---|---|
| [Vision](vision.md) | What KMD should become |
| [Mission](mission.md) | What we optimize for |
| [Principles](principles.md) | How we make product decisions |
| [Phases](phases.md) | V1, V2, and later |
| [Non-goals](non-goals.md) | What KMD is not |
| [Users](users.md) | Who this is for |
| [Developer Experience](dx.md) | The API surface developers should feel |
| [Architecture](architecture.md) | Modules, AST, parser, renderer |
| [API](api.md) | Public composables, state, styling |
| [Markdown](markdown.md) | V1 syntax, code blocks, GFM |
| [Styling](styling.md) | Typed style and Material 3 |
| [Streaming](streaming.md) | Incremental Markdown for LLM output |
| [Extensions](extensions.md) | GFM, images, highlight, custom blocks |
| [Performance](performance.md) | Benchmarks, lazy rendering, identity |
| [Accessibility](accessibility.md) | Semantics, keyboard, screen readers |

---

## Targets

KMD should run everywhere Compose Multiplatform runs:

```text
Android
iOS
Desktop
Web / Wasm
```

---

## Not this

KMD is not an editor, not a Notion clone, and not a browser engine.

It is:

> Markdown → native Compose UI.
