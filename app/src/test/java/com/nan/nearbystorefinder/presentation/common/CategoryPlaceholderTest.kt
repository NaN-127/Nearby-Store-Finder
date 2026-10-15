package com.nan.nearbystorefinder.presentation.common

import com.nan.nearbystorefinder.R
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryPlaceholderTest {

    @Test
    fun `restaurant type resolves to restaurant placeholder`() {
        assertEquals(R.drawable.ic_placeholder_restaurant, categoryPlaceholderIcon(listOf("restaurant", "food")))
    }

    @Test
    fun `pharmacy type resolves to pharmacy placeholder`() {
        assertEquals(R.drawable.ic_placeholder_pharmacy, categoryPlaceholderIcon(listOf("pharmacy")))
    }

    @Test
    fun `unknown type falls back to generic place placeholder`() {
        assertEquals(R.drawable.ic_placeholder_place, categoryPlaceholderIcon(listOf("some_unmapped_type")))
    }

    @Test
    fun `empty type list falls back to generic place placeholder`() {
        assertEquals(R.drawable.ic_placeholder_place, categoryPlaceholderIcon(emptyList()))
    }
}
