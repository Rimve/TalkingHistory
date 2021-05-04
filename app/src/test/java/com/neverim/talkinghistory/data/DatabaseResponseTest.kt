package com.neverim.talkinghistory.data

import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseResponseTest {

    @Test
    fun `try to create database response`() {
        val exception = mockk<Exception>()
        val result = DatabaseResponse("Test", exception)
        assertTrue(result.data == "Test")
        assertTrue(result.exception == exception)
    }
}