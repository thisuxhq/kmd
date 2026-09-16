# Non-goals

KMD V1 is:

> Markdown → native Compose UI.

Everything else waits, even if it is interesting.

---

## Not in V1

```text
our own complete Markdown parser
a Markdown editor
rich text editing
WYSIWYG
a Notion clone
HTML execution
JavaScript execution
every Markdown extension
an IDE-grade Markdown surface
```

---

## Not ever, in core

These are host-app problems, not engine problems:

```text
opening arbitrary URL protocols
fetching images without a user-supplied loader
running fenced HTML
running scripts inside Markdown
network access
auth
file I/O
```

KMD renders. The app decides side effects.

---

## Not a competing category

KMD is not trying to be:

| Product type | Why not |
|---|---|
| WebView Markdown | that is the thing we are replacing |
| HTML renderer | Compose is the output |
| Rich text editor | editing is a different product |
| Note-taking app | we are a library |
| Syntax highlighter | optional plugin, default is plain text |
| Image loader | optional plugin, default is a placeholder |

---

## What this protects

A small public API.

A KMP-first core.

A streaming path that stays fast.

If a feature needs a browser, an editor canvas, or a parser written from scratch, it does not belong here.
