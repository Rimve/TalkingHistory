package com.neverim.talkinghistory.utilities


import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class HelperUtilsTest {
    @Test
    fun `try to calculate Lev distance`() {
        runBlocking {
            val result = HelperUtils.levDistance("string one", "string two")
            assertTrue(result == 3)
        }
    }
}