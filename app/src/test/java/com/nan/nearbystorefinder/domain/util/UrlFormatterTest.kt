package com.nan.nearbystorefinder.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlFormatterTest {

    @Test
    fun `formats long nested store URL to clean base URL with trailing slash`() {
        val input = "https://www.walgreens.com/locator/walgreens-4170+el+camino+real-palo+alto-ca-94306/id=3344"
        val expected = "https://www.walgreens.com/"
        assertEquals(expected, UrlFormatter.formatDisplayUrl(input))
    }

    @Test
    fun `formats complex query and hash URL to base domain`() {
        val input = "https://www.costco.com/w/-/ca/mountain-view/143?param=1&ref=app#details"
        val expected = "https://www.costco.com/"
        assertEquals(expected, UrlFormatter.formatDisplayUrl(input))
    }

    @Test
    fun `preserves http scheme if specified`() {
        val input = "http://mybakery.com/store/details"
        val expected = "http://mybakery.com/"
        assertEquals(expected, UrlFormatter.formatDisplayUrl(input))
    }

    @Test
    fun `adds https scheme when missing`() {
        val input = "www.target.com/sl/palo-alto/123"
        val expected = "https://www.target.com/"
        assertEquals(expected, UrlFormatter.formatDisplayUrl(input))
    }

    @Test
    fun `handles blank or null gracefully`() {
        assertEquals("", UrlFormatter.formatDisplayUrl(null))
        assertEquals("", UrlFormatter.formatDisplayUrl(""))
        assertEquals("", UrlFormatter.formatDisplayUrl("   "))
    }
}
