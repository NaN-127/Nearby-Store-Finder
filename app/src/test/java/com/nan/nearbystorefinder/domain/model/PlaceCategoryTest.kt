package com.nan.nearbystorefinder.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceCategoryTest {

    @Test
    fun `ALL category has no included types so it applies no filter`() {
        assertTrue(PlaceCategory.ALL.includedTypes.isEmpty())
    }

    @Test
    fun `every non-ALL category maps to at least one valid Google place type`() {
        PlaceCategory.entries.filter { it != PlaceCategory.ALL }.forEach { category ->
            assertTrue("${category.name} must map to at least one Google type", category.includedTypes.isNotEmpty())
        }
    }

    @Test
    fun `restaurants category maps to the restaurant type`() {
        assertEquals(listOf("restaurant"), PlaceCategory.RESTAURANTS.includedTypes)
    }

    @Test
    fun `fromLabel resolves a known label back to its category`() {
        assertEquals(PlaceCategory.CAFES, PlaceCategory.fromLabel("Cafes"))
    }

    @Test
    fun `fromLabel falls back to ALL for an unknown label`() {
        assertEquals(PlaceCategory.ALL, PlaceCategory.fromLabel("Not A Real Category"))
    }

    @Test
    fun `fromLabel falls back to ALL for null`() {
        assertEquals(PlaceCategory.ALL, PlaceCategory.fromLabel(null))
    }
}
