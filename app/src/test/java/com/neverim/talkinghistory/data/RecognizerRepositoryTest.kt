package com.neverim.talkinghistory.data

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class RecognizerRepositoryTest {

    @RelaxedMockK
    private lateinit var recoDao: RecognizerDao

    @RelaxedMockK
    private lateinit var context: Context

    private lateinit var recoRepo: RecognizerRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        recoRepo = RecognizerRepository(recoDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `check if audio is being set up`() {
        recoRepo.audioSetup(context)
        verify { recoRepo.audioSetup(context) }
    }

    @Test
    fun `check if start recognition is being called`() {
        recoRepo.startRecognition(context)
        verify { recoRepo.startRecognition(context) }
    }

    @Test
    fun `check if stop recognition is being called`() {
        recoRepo.stopRecognition()
        verify { recoRepo.stopRecognition() }
    }

    @Test
    fun `check if is recognizing is being called`() {
        recoRepo.isRecognizing()
        assertFalse(recoRepo.isRecognizing())
        verify { recoRepo.isRecognizing() }
    }

    @Test
    fun `check if getTranscript returns a value`() {
        recoRepo.getTranscript()
        verify { recoRepo.getTranscript() }
    }
}