# Performance

Performance is a product feature.

Streaming Markdown at 50–100 tokens per second is a default use case, not a benchmark we run later.

---

## Goals

For a representative document:

```text
10,000 Markdown characters
50–100 blocks
multiple code blocks
lists
links
```

Targets:

```text
initial render < 16–30 ms where practical
smooth scrolling at 60 FPS
minimal allocations during streaming
stable blocks should not recompose
```

---

## What we measure

Static sizes:

```text
1 KB
10 KB
50 KB
100 KB Markdown
```

Streaming rates:

```text
10 tokens/sec
50 tokens/sec
100 tokens/sec
```

Each run should report:

```text
parse time
snapshot time
compose apply
recomposition count of stable blocks (should be ~0)
allocations on append
```

---

## How we win

The architecture is the optimization.

```text
stable block identity
append-only hot path
active block isolated
parser hidden behind an adapter so it can be replaced
no HTML, no WebView
```

If stable blocks recompose on append, fix identity before micro-optimizing renderers.

Details in [Streaming](streaming.md).

---

## Lazy rendering

Large documents should eventually render blocks lazily.

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

KMD does not force `LazyColumn`.

```kotlin
Kmd(...)      // default, fine for chat bubbles and short docs
LazyKmd(...)  // large READMEs, knowledge bases
```

`LazyKmd` is Phase 2. Identities in V1 must already be lazy-safe.

---

## Allocation rules for streaming

On `append`:

```text
do not rebuild finalized AST nodes
do not copy the whole block list if we can avoid it
do not allocate style objects
do not create new Compose keys for stable blocks
```

The cheap path is: grow or replace the tail, bump revision, emit a snapshot.

`replace()` may allocate. That is acceptable.

---

## Code blocks

Code is the expensive visual.

V1:

```text
plain text
horizontal scroll
no highlighter work unless one is installed
```

A highlighter must be incremental-friendly. Re-highlighting a 400-line fence on every token is a bug, even if it is in a plugin.

---

## Selection

```kotlin
SelectionContainer {
    Kmd(markdown)
}
```

Selection is important and easy to get wrong across Android, iOS, Desktop, and Web.

Do not promise full cross-block selection until it is measured on each target.

Selection should not force extra recomposition of stable blocks.

---

## Accessibility cost

Semantics stay on.

```text
heading semantics
link clickable semantics
image content description
checkbox state
```

Performance work that strips semantics is not an optimization.

---

## Benchmarks module

Later:

```text
kmd-benchmark
kmd-test
```

Benchmarks live in the repo from the moment streaming exists, even if the module is thin.

If we cannot show that append does not recompose blocks 1–N, we do not have streaming yet.
