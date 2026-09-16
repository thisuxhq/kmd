package com.thisux.kmd

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

class KmdKeywordHighlighter(
    private val keyword: Color = Color(0xFF0B57D0),
    private val string: Color = Color(0xFF188038),
    private val comment: Color = Color(0xFF5F6368),
    private val number: Color = Color(0xFFB06000),
) : KmdSyntaxHighlighter {
    override fun highlight(
        language: String?,
        code: String,
    ): AnnotatedString {
        val keywords = keywordsFor(language)
        return buildAnnotatedString {
            var index = 0
            while (index < code.length) {
                when {
                    code.startsWith("//", index) -> {
                        val end = code.indexOf('\n', index).let { if (it < 0) code.length else it }
                        withStyle(SpanStyle(color = comment, fontStyle = FontStyle.Italic)) {
                            append(code.substring(index, end))
                        }
                        index = end
                    }
                    code.startsWith("/*", index) -> {
                        val close = code.indexOf("*/", index + 2)
                        val end = if (close < 0) code.length else close + 2
                        withStyle(SpanStyle(color = comment, fontStyle = FontStyle.Italic)) {
                            append(code.substring(index, end))
                        }
                        index = end
                    }
                    code[index] == '"' || code[index] == '\'' || code[index] == '`' -> {
                        val quote = code[index]
                        val end = skipQuoted(code, index, quote)
                        withStyle(SpanStyle(color = string)) {
                            append(code.substring(index, end))
                        }
                        index = end
                    }
                    code[index].isDigit() -> {
                        val end = code.indexOfFirstFrom(index) { !it.isDigit() && it != '.' }
                        withStyle(SpanStyle(color = number)) {
                            append(code.substring(index, end))
                        }
                        index = end
                    }
                    code[index].isLetter() || code[index] == '_' -> {
                        val end = code.indexOfFirstFrom(index) { !it.isLetterOrDigit() && it != '_' }
                        val word = code.substring(index, end)
                        if (word in keywords) {
                            withStyle(SpanStyle(color = keyword, fontWeight = FontWeight.Medium)) {
                                append(word)
                            }
                        } else {
                            append(word)
                        }
                        index = end
                    }
                    else -> {
                        append(code[index])
                        index++
                    }
                }
            }
        }
    }

    private fun keywordsFor(language: String?): Set<String> {
        return when (language?.lowercase()) {
            "js", "javascript", "ts", "typescript" -> JsKeywords
            "python", "py" -> PythonKeywords
            "json" -> emptySet()
            else -> KotlinKeywords
        }
    }

    private companion object {
        val KotlinKeywords =
            setOf(
                "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
                "companion", "const", "constructor", "continue", "crossinline", "data", "do",
                "else", "enum", "expect", "external", "false", "final", "finally", "for",
                "fun", "get", "if", "import", "in", "infix", "init", "inline", "inner",
                "interface", "internal", "is", "lateinit", "noinline", "null", "object",
                "open", "operator", "out", "override", "package", "private", "protected",
                "public", "reified", "return", "sealed", "set", "super", "suspend", "this",
                "throw", "true", "try", "typealias", "typeof", "val", "var", "vararg",
                "when", "where", "while",
            )
        val JsKeywords =
            setOf(
                "async", "await", "break", "case", "catch", "class", "const", "continue",
                "debugger", "default", "delete", "do", "else", "export", "extends", "false",
                "finally", "for", "function", "if", "import", "in", "instanceof", "let",
                "new", "null", "return", "static", "super", "switch", "this", "throw",
                "true", "try", "typeof", "var", "void", "while", "with", "yield",
            )
        val PythonKeywords =
            setOf(
                "False", "None", "True", "and", "as", "assert", "async", "await", "break",
                "class", "continue", "def", "del", "elif", "else", "except", "finally",
                "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
                "not", "or", "pass", "raise", "return", "try", "while", "with", "yield",
            )
    }
}

private fun String.indexOfFirstFrom(
    start: Int,
    predicate: (Char) -> Boolean,
): Int {
    var index = start
    while (index < length && !predicate(this[index])) {
        index++
    }
    return index
}

private fun skipQuoted(
    code: String,
    start: Int,
    quote: Char,
): Int {
    var index = start + 1
    while (index < code.length) {
        when (code[index]) {
            '\\' -> index += 2
            quote -> return index + 1
            else -> index++
        }
    }
    return code.length
}
