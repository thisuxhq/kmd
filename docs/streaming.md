# Streaming

Streaming is one of KMD's main differentiators.

LLM applications made the problem obvious. A model might emit:

```text
#
## Hel
## Hello

This is **som
This is **some text**
```

Traditional renderers reparse the complete document on every token.

KMD is designed for this from the start.

---

## The problem

Traditional:

```text
token
 ↓
reparse entire Markdown
 ↓
rerender entire document
```

That is fine for a static README. It is not fine for a chat bubble growing at 50–100 tokens per second.

---

## The model

KMD understands:

```text
finalized blocks
+
active block
```

Example:

```md
# Android Agent

Android agents can interact with apps.

- tap
- swipe
- typ
```

The heading, paragraph, and finished list items are stable.

Only:

```text
- typ
```

is changing.

---

## The path

Today KMD reparses the whole buffer on `append`, then rematches identities so finished blocks keep their keys.

The intended path:

```text
token
 ↓
identify active block
 ↓
parse the tail
 ↓
update block
 ↓
Compose recomposes changed UI
```

Document:

```text
Stable
Stable
Stable
Active
```

As tokens arrive:

```text
Stable
Stable
Stable
Active'
```

Stable blocks keep their identities. Compose does not recreate them.

---

## Identity

```kotlin
data class KmdBlockId(
    val value: Long
)
```

Before:

```text
A
B
C
D1
```

After:

```text
A
B
C
D2
```

Only `D` changes.

Not:

```text
A → recreate
B → recreate
C → recreate
D → recreate
```

If identities churn, streaming is broken even if the pixels look right for a moment.

---

## State

```kotlin
data class KmdSnapshot(
    val blocks: List<KmdBlock>,
    val activeBlock: KmdBlock?,
    val revision: Long
)
```

```kotlin
class KmdEngine {
    fun append(input: String): KmdSnapshot
    fun reset()
    fun replace(markdown: String): KmdSnapshot
}
```

Publicly, users hold `KmdState`:

```kotlin
val state = rememberKmdState()

Kmd(state = state)

state.append(token)
```

---

## Optimize append-only first

The common AI case is growth at the end:

```text
"Hel"
"Hello"
"Hello "
"Hello **"
"Hello **world"
"Hello **world**"
```

Editing in the middle of a huge document is harder. V1 does not need to make that fast.

Priority:

```text
append-only streaming > arbitrary editing
```

`replace()` exists for full reloads and corrections. It may reparse.

---

## Incomplete syntax

The active block is often invalid Markdown.

KMD should:

```text
not crash
keep finalized blocks rendered
best-effort render the tail
avoid layout flash on common incomplete patterns
```

Examples the engine will see constantly:

```text
unclosed **
unclosed ```
a heading that is still just #
a list item being typed
a half-finished link ](
```

The library user does not handle these. The engine does.

---

## Compose implications

- Key every block by `KmdBlockId`
- Do not use list index as identity
- Keep finalized block composables skippable
- Isolate the active block so inline changes do not invalidate the column
- Do not force `LazyColumn` in V1, but design identities so `LazyKmd` can later

---

## Performance targets

Streaming benchmarks:

```text
10 tokens/sec
50 tokens/sec
100 tokens/sec
```

Document sizes:

```text
1 KB
10 KB
50 KB
100 KB
```

Goals:

```text
minimal allocations during streaming
stable blocks should not recompose
smooth scrolling at 60 FPS
```

Initial render of a static document still matters. Streaming should not make the static path worse.

---

## What V1 does not do

```text
operational transform
collaborative editing
cursor-preserving mid-document edits
token-level inline diffs across the whole tree
```

V1 streaming is: append Markdown, update the tail, leave the rest alone.
