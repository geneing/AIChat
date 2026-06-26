package com.eugene.aichat.core.util

/**
 * Locates plain http(s) URLs in free text. The matcher is intentionally
 * conservative — it doesn't try to handle parentheses, query strings with
 * embedded brackets, or IDN domains. The intent is to find obvious
 * "click me" links inside chat replies.
 */
object UrlParser {

    private val pattern = Regex(
        """(?<!\w)((?:https?://)[^\s<>"'`\)\]]+)""",
        RegexOption.IGNORE_CASE
    )

    fun findAll(text: String): List<String> =
        pattern.findAll(text).map { it.value }.toList()

    fun splitOnUrls(text: String): List<Part> {
        if (text.isEmpty()) return emptyList()
        val out = mutableListOf<Part>()
        var cursor = 0
        for (match in pattern.findAll(text)) {
            val start = match.range.first
            if (start > cursor) {
                out += Part.Text(text.substring(cursor, start))
            }
            out += Part.Url(match.value)
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            out += Part.Text(text.substring(cursor))
        }
        return out
    }

    sealed class Part {
        data class Text(val value: String) : Part()
        data class Url(val value: String) : Part()
    }
}
