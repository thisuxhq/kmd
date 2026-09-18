# AGENTS

KMD is a native Markdown engine for Compose. Markdown becomes Compose UI. No WebView, no HTML, no JavaScript.

Read `docs/` before changing product behavior. `docs/principles.md` and `docs/phases.md` win when code and docs disagree — update the code, or update the docs in the same change.

```kotlin
Kmd("# Hello")
```

---

## Layout

```text
kmd-core/                 AST, parser adapter, streaming engine
kmd-compose/              Compose renderer, Kmd(), KmdStyle, LazyKmd (no Material)
kmd-compose-material3/    KmdMaterial3.style()
kmd-images/               Coil image loader
kmd-highlight/            keyword syntax highlighter
sample/                   Android demo (Document + Stream + Settings)
docs/                     product source of truth
```

Package: `com.thisux.kmd`

GFM (strikethrough, task lists, tables, autolinks) lives in `kmd-core` / `kmd-compose`, not a separate artifact.

---

## Commands

```bash
./gradlew :kmd-core:test
./gradlew :kmd-compose:compileDebugKotlin
./gradlew :kmd-compose-material3:compileDebugKotlin
./gradlew :sample:assembleDebug
./gradlew :sample:installDebug
```

Android CLI:

```bash
android emulator list
android emulator start Medium_Phone_API_36.1
android run --device=emulator-5554 \
  --apks=sample/build/outputs/apk/debug/sample-debug.apk \
  --activity=com.thisux.kmd.sample.MainActivity
```

---

## Rules

1. **Compose native.** Real Compose nodes. Never WebView or HTML.
2. **KMP-shaped.** Core has no Compose. Compose has no Material. Material 3 is optional.
3. **Small public API.** Most apps only meet `Kmd`, `KmdStyle`, `KmdState`.
4. **Own the AST.** Parser types never leak. JetBrains Markdown stays behind `KmdParser`.
5. **Stable block identity.** Append-only streaming must not rekey finished blocks.
6. **Do not execute the document.** Links are callbacks. Images are pluggable. Code is text.

Do not write a Markdown parser. Do not add Coil, a highlighter, or Material to `kmd-core` / `kmd-compose`.

---

## Stacked PRs

Phase work is a stack, one concern per branch.

```bash
git config rerere.enabled true
git config remote.pushDefault origin

gh stack init <first-branch>
# commit with git add / git commit

gh stack add <next-branch>
# commit

gh stack submit --auto --open
```

Then **immediately** assign and label every new PR (see below).

```bash
gh stack merge --yes --squash
```

Merge from the bottom. Do not mix unrelated work into an existing stack.

---

## Labels (required)

Every PR must have:

1. **Type** — exactly one
2. **Phase** — exactly one
3. **Area** — one or more

Assign the author: `Spikeysanju`.

```bash
gh pr edit <n> --add-assignee Spikeysanju --add-label "<type>,<phase>,<area>"
```

After `gh stack submit`, label each PR in the stack. Do not leave unlabeled PRs.

### Type

| Label | When |
|---|---|
| `enhancement` | New behavior |
| `bug` | Broken existing behavior |
| `documentation` | Docs / AGENTS.md only |
| `build` | Gradle, CI, wrappers, module wiring |

### Phase

| Label | When |
|---|---|
| `phase-1` | Core Markdown → Compose, streaming |
| `phase-2` | GFM, renderers, images, highlight, LazyKmd |
| `phase-3` | Extensions, math, benchmarks, custom blocks |

### Area

| Label | When |
|---|---|
| `kmd-core` | AST, parser, engine, identity |
| `kmd-compose` | Renderer, `Kmd`, style, state |
| `material3` | `KmdMaterial3` |
| `sample` | Sample app |
| `gfm` | Tables, task lists, strikethrough, autolinks |
| `streaming` | Append path, active block, identity |
| `renderer` | Renderer registry / overrides |
| `images` | Image renderer / loaders |
| `highlight` | Syntax highlighting |
| `a11y` | Semantics, keyboard, screen readers |
| `performance` | Benchmarks, allocations, lazy |
| `api` | Public API shape |
| `tests` | Tests as the main change |

### Examples

| Change | Labels |
|---|---|
| JetBrains adapter + AST | `enhancement`, `phase-1`, `kmd-core` |
| Table renderer | `enhancement`, `phase-2`, `gfm`, `kmd-compose` |
| Identity bug on append | `bug`, `phase-1`, `kmd-core`, `streaming` |
| This file | `documentation`, `phase-1`, `build` |

If a label is missing, create it:

```bash
gh label create "<name>" --description "<one line>" --color "<hex>"
```

---

## Next

Phase 1 and Phase 2 have shipped.

Phase 3, in order:

1. Incremental append — parse the tail, not the whole buffer
2. Accessibility — heading level, lists, tasks, tables
3. Renderer registry — pass `Modifier`; override link, inline code, checkbox
4. Extension API — `KmdExtension`, custom blocks, GitHub alerts
5. `kmd-benchmark` — prove stable blocks do not recompose on append

Do not start math or Mermaid while those are open.
