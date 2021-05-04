package com.neverim.talkinghistory.data

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

class StorageRepositoryTest {

    @SpyK
    private lateinit var storageRepo: StorageRepository

    @RelaxedMockK
    private lateinit var dbCallback: IDatabaseCallback

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        MockKAnnotations.init(this)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { storageRepo.getImageFile(any(), any(), any()) } answers {  }
        every { dbCallback.onResponse(any()) } returns Unit
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}