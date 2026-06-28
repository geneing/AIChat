package com.github.geneing.aichat.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import com.github.geneing.aichat.core.util.UrlParser

/**
 * A small, dependency-free markdown renderer that supports the most
 * common chat reply shapes:
 *
 *   - # / ## / ### headings
 *   - **bold**, *italic*, ~~strike~~
 *   - `inline code`
 *   - fenced ``` code blocks (rendered monospaced, no syntax highlight)
 *   - bullet and numbered lists (- foo, 1. bar)
 *   - > blockquotes
 *   - plain URLs are converted to clickable link annotations via
 *     [UrlParser]
 *
 * The output is a single [AnnotatedString] styled for use inside a
 * [androidx.compose.foundation.text.BasicText] / Material 3 Text.
 */
object MarkdownRenderer {

    fun render(
        text: String,
        linkColor: Color,
        codeBackground: Color,
        codeText: Color,
        codeSize: TextUnit
    ): AnnotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        var i = 0
        var inCodeBlock = false
        var codeBlockBuffer = StringBuilder()

        while (i < lines.size) {
            val raw = lines[i]
            val line = raw.trimEnd()

            if (line.trimStart().startsWith("```")) {
                if (inCodeBlock) {
                    pushStyle(SpanStyle(background = codeBackground, color = codeText, fontSize = codeSize))
                    append(codeBlockBuffer.toString().trimEnd())
                    pop()
                    codeBlockBuffer = StringBuilder()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                i++
                continue
            }

            if (inCodeBlock) {
                if (codeBlockBuffer.isNotEmpty()) codeBlockBuffer.append('\n')
                codeBlockBuffer.append(raw)
                i++
                continue
            }

            val heading = headingLevel(line)
            if (heading != null) {
                val content = line.substring(heading).trim()
                pushStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = when (heading) {
                            1 -> codeSize * 1.6f
                            2 -> codeSize * 1.35f
                            else -> codeSize * 1.15f
                        }
                    )
                )
                appendInline(content, linkColor, codeBackground, codeText, codeSize)
                pop()
                append('\n')
                i++
                continue
            }

            if (line.startsWith("> ")) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = codeText))
                append("│ ")
                appendInline(line.removePrefix("> "), linkColor, codeBackground, codeText, codeSize)
                pop()
                append('\n')
                i++
                continue
            }

            if (line.startsWith("- ") || line.startsWith("* ")) {
                append("•  ")
                appendInline(line.removePrefix("- ").removePrefix("* "), linkColor, codeBackground, codeText, codeSize)
                append('\n')
                i++
                continue
            }

            val ordered = orderedPrefix(line)
            if (ordered != null) {
                append(ordered)
                append(' ')
                appendInline(line.removePrefix(ordered).trimStart(), linkColor, codeBackground, codeText, codeSize)
                append('\n')
                i++
                continue
            }

            appendInline(line, linkColor, codeBackground, codeText, codeSize)
            append('\n')
            i++
        }

        if (inCodeBlock && codeBlockBuffer.isNotEmpty()) {
            pushStyle(SpanStyle(background = codeBackground, color = codeText, fontSize = codeSize))
            append(codeBlockBuffer.toString().trimEnd())
            pop()
        }
    }

    private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInline(
        text: String,
        linkColor: Color,
        codeBackground: Color,
        codeText: Color,
        codeSize: TextUnit
    ) {
        val tokens = mutableListOf<Token>()
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '`') {
                if (sb.isNotEmpty()) {
                    tokens += Token.Text(sb.toString())
                    sb.clear()
                }
                val end = text.indexOf('`', i + 1)
                if (end < 0) {
                    sb.append(c)
                    i++
                } else {
                    tokens += Token.Code(text.substring(i + 1, end))
                    i = end + 1
                }
            } else {
                sb.append(c)
                i++
            }
        }
        if (sb.isNotEmpty()) tokens += Token.Text(sb.toString())

        for (token in tokens) {
            when (token) {
                is Token.Text -> appendRichText(token.value, linkColor)
                is Token.Code -> {
                    pushStyle(SpanStyle(background = codeBackground, color = codeText, fontSize = codeSize))
                    append(token.value)
                    pop()
                }
            }
        }
    }

    private fun androidx.compose.ui.text.AnnotatedString.Builder.appendRichText(
        text: String,
        linkColor: Color
    ) {
        val parts = UrlParser.splitOnUrls(text)
        for (part in parts) {
            when (part) {
                is UrlParser.Part.Text -> appendInlineStyles(part.value, linkColor)
                is UrlParser.Part.Url -> {
                    val tag = "url_${part.value.hashCode()}"
                    pushStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                    pushStringAnnotation(tag = tag, annotation = part.value)
                    append(part.value)
                    pop()
                    pop()
                }
            }
        }
    }

    private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlineStyles(
        text: String,
        linkColor: Color
    ) {
        var i = 0
        var bold = false
        var italic = false
        var strike = false
        val sb = StringBuilder()
        fun flush() {
            if (sb.isEmpty()) return
            val style = SpanStyle(
                fontWeight = if (bold) FontWeight.Bold else null,
                fontStyle = if (italic) FontStyle.Italic else null,
                textDecoration = if (strike) TextDecoration.LineThrough else null
            )
            pushStyle(style)
            append(sb.toString())
            pop()
            sb.clear()
        }
        while (i < text.length) {
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                flush(); bold = !bold; i += 2
            } else if (text[i] == '*') {
                flush(); italic = !italic; i++
            } else if (i + 1 < text.length && text[i] == '~' && text[i + 1] == '~') {
                flush(); strike = !strike; i += 2
            } else {
                sb.append(text[i]); i++
            }
        }
        flush()
    }

    private fun headingLevel(line: String): Int? {
        var hashes = 0
        while (hashes < line.length && line[hashes] == '#') hashes++
        if (hashes in 1..3 && line.length > hashes && line[hashes] == ' ') return hashes
        return null
    }

    private fun orderedPrefix(line: String): String? {
        val dot = line.indexOf('.')
        if (dot <= 0 || dot > 3) return null
        val head = line.substring(0, dot)
        if (head.any { !it.isDigit() }) return null
        return head + "."
    }

    private sealed class Token {
        data class Text(val value: String) : Token()
        data class Code(val value: String) : Token()
    }
}
