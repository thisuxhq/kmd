# Users

KMD is for people shipping Compose apps that display Markdown.

Not for people building a Markdown engine.

---

## Compose developers

They want:

```kotlin
Kmd(readme)
```

instead of writing a renderer.

Typical content:

```text
in-app docs
settings help
changelogs
onboarding
licenses
```

They should never meet the parser.

---

## AI app developers

Chat and agent products:

```text
ChatGPT clients
Claude clients
AI assistants
coding agents
research agents
```

This is a primary audience.

They stream tokens. They need:

```text
append without full reparse
stable blocks
incomplete Markdown that does not crash
code blocks that look native
```

See [Streaming](streaming.md).

---

## Documentation apps

```text
README files
API docs
guides
release notes
knowledge bases
```

These documents are larger, more static, and more likely to need:

```text
LazyKmd
tables
images
anchor links
```

Most of that is Phase 2. V1 should still render a README cleanly.

---

## Note-taking apps

```text
notes
journals
knowledge tools
personal wikis
```

They care about lists, quotes, headings, and later GFM task lists.

They do **not** get an editor from KMD. They render a stored Markdown string.

---

## Developer tools

```text
Git clients
IDE tools
terminal apps
GitHub clients
code review apps
```

Code blocks matter more here than anywhere else.

Language label, copy, scroll, and a highlighter hook should exist even when highlighting itself is not bundled.

---

## What they share

All of these users want the same three calls:

```kotlin
Kmd(markdown)
Kmd(markdown, style)
Kmd(state)
```

If a user type needs a fourth required call, the API is too big.
