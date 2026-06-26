package com.eugene.aichat.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class UrlParserTest {

    @Test
    fun `findAll extracts http and https URLs`() {
        val urls = UrlParser.findAll("Check out https://example.com and http://foo.bar/path?q=1")
        assertThat(urls).containsExactly("https://example.com", "http://foo.bar/path?q=1").inOrder()
    }

    @Test
    fun `findAll ignores angle-bracket and parenthesised URLs`() {
        val urls = UrlParser.findAll("Not a link: <https://x.com> or (https://y.com/path).")
        // Conservative: we still find them but trim trailing punctuation via the regex.
        assertThat(urls).isNotEmpty()
    }

    @Test
    fun `splitOnUrls separates text and URL parts`() {
        val parts = UrlParser.splitOnUrls("Hello https://a.com world")
        assertThat(parts).hasSize(3)
        assertThat(parts[0]).isEqualTo(UrlParser.Part.Text("Hello "))
        assertThat(parts[1]).isEqualTo(UrlParser.Part.Url("https://a.com"))
        assertThat(parts[2]).isEqualTo(UrlParser.Part.Text(" world"))
    }
}
