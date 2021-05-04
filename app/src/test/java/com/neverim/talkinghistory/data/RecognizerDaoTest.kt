package com.neverim.talkinghistory.data

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class RecognizerDaoTest {

    @RelaxedMockK
    private lateinit var context: Context

    private lateinit var recoDao: RecognizerDao

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        MockKAnnotations.init(this)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        recoDao = RecognizerDao()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `check if it is not recognizing`() {
        assertFalse(recoDao.isRecognizing())
    }

    @Test
    fun `try to get the transcript`() {
        val spiedRepo = spyk<RecognizerDao>()
        spiedRepo.getTranscript()
        verify { spiedRepo.getTranscript() }
    }

    @Test
    fun `try to set up the audio`() {
        recoDao.audioSetup(context)
        verify { recoDao.audioSetup(context) }
    }

    @Test
    fun `try to start recognition`() {
        val spiedRepo = spyk<RecognizerDao>()
        spiedRepo.startRecognition(context)
        verify { spiedRepo.startRecognition(context) }
    }

    @Test
    fun `try to stop recognition`() {
        val spiedRepo = spyk<RecognizerDao>()
        spiedRepo.stopRecognition()
        verify { spiedRepo.stopRecognition() }
    }

}