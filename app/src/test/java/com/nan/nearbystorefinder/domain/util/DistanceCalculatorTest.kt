package com.nan.nearbystorefinder.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun `same point returns zero distance`() {
        val distance = DistanceCalculator.distanceKm(12.9716, 77.5946, 12.9716, 77.5946)
        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun `known distance between two cities is approximately correct`() {
        // Bengaluru (12.9716, 77.5946) to Chennai (13.0827, 80.2707) is ~290km.
        val distance = DistanceCalculator.distanceKm(12.9716, 77.5946, 13.0827, 80.2707)
        assertEquals(290.0, distance, 10.0)
    }

    @Test
    fun `distance is symmetric regardless of argument order`() {
        val d1 = DistanceCalculator.distanceKm(12.9716, 77.5946, 13.0827, 80.2707)
        val d2 = DistanceCalculator.distanceKm(13.0827, 80.2707, 12.9716, 77.5946)
        assertEquals(d1, d2, 0.0001)
    }

    @Test
    fun `one degree of latitude is approximately 111km`() {
        val distance = DistanceCalculator.distanceKm(0.0, 0.0, 1.0, 0.0)
        assertEquals(111.0, distance, 1.0)
    }
}
