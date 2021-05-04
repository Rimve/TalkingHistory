package com.neverim.talkinghistory.utilities

import android.os.Looper
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TestRule

class InjectorUtilsTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        mockkStatic(Looper::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `try to get characterViewModel`() {
        assertTrue(InjectorUtils.provideCharacterViewModelFactory() != null)
    }

    @Test
    fun `try to get recognizerViewModel`() {
        assertTrue(InjectorUtils.provideRecognizerViewModelFactory() != null)
    }

    @Test
    fun `try to get storageViewModel`() {
        assertTrue(InjectorUtils.provideStorageViewModelFactory() != null)
    }
}