package com.neverim.talkinghistory.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.File

class StorageDaoTest {

    private lateinit var storageDao: StorageDao

    @RelaxedMockK
    private lateinit var file: File

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        storageDao = spyk(StorageDao())
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `check if setting image file works`() {
        storageDao.setImageFile(file)
        verify { storageDao.setImageFile(file) }
    }

    @Test
    fun `check if setting audio file works`() {
        storageDao.setAudioFile(file)
        verify { storageDao.setAudioFile(file) }
    }

    @Test
    fun `check if image can be retrieved`() {
        storageDao.getAudio()
        verify { storageDao.getAudio() }
    }

    @Test
    fun `check if audio file can be retrieved`() {
        storageDao.getImage()
        verify { storageDao.getImage() }
    }
}