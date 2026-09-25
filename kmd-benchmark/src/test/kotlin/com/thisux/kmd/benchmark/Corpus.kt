package com.thisux.kmd.benchmark

// Representative content from docs/performance.md: headings, prose with links,
// lists, code, quotes, and tables. Every block ends with a blank line so a
// finished block never changes meaning when later text arrives.
internal object Corpus {
    private val section =
        """
        ## Section {n}

        KMD renders **bold**, *italic*, `inline code`, and [links](https://example.com/{n}) as native text.
        A second line keeps the paragraph realistic for chat output.

        - streaming stays on the tail
        - finished blocks keep their identity
        - [ ] a task that is still open

        ```kotlin
        fun section{n}(input: String): Int {
            val words = input.split(" ")
            return words.count { it.isNotBlank() }
        }
        ```

        > A quote that explains why identity matters
        > for large documents.

        | block | cost |
        |---|---|
        | paragraph | low |
        | code | high |

        1. parse the tail
        2. rematch identity
        3. emit a snapshot


        """.trimIndent()

    fun ofSize(bytes: Int): String {
        val out = StringBuilder("# Benchmark\n\n")
        var n = 1
        while (out.length < bytes) {
            out.append(section.replace("{n}", n.toString()))
            n++
        }
        return out.substring(0, bytes).substringBeforeLast("\n\n") + "\n"
    }

    fun tokens(
        markdown: String,
        size: Int = 4,
    ): List<String> = markdown.chunked(size)
}
