package com.neverim.talkinghistory.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.database.DatabaseReference
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

class DatabaseSourceTest {

    private val databaseSource: DatabaseSource = mockk()
    private val databaseRef: DatabaseReference = mockk()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { DatabaseSource() } returns databaseSource
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}