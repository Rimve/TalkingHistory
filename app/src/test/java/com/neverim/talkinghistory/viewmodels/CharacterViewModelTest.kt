package com.neverim.talkinghistory.viewmodels

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.neverim.talkinghistory.data.CharacterDao
import com.neverim.talkinghistory.data.CharacterRepository
import com.neverim.talkinghistory.data.IDatabaseCallback
import com.neverim.talkinghistory.data.Vertex
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CharacterViewModelTest {

    private lateinit var charDao: CharacterDao
    private lateinit var charRepo: CharacterRepository
    private lateinit var charViewModel: CharacterViewModel
    private lateinit var databaseCallBack: IDatabaseCallback

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        MockKAnnotations.init(this)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        charDao = CharacterDao()
        charRepo = spyk(CharacterRepository.getInstance(charDao))
        charViewModel = spyk(CharacterViewModel(charRepo))
        databaseCallBack = mockk()
        every { charViewModel.addDirectedEdge(any(), any()) } returns Unit
        every { databaseCallBack.onResponse(any()) } returns Unit
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `try adding directed edges`() {
        val srcVertex = Vertex(0, "Hello, do you hear me?")
        val dstVertex = Vertex(5, "Yes I do")

        charViewModel.addDirectedEdge(srcVertex, dstVertex)
        verify { charViewModel.addDirectedEdge(srcVertex, dstVertex) }
    }

    @Test
    fun `check if we can clear char viewModel`() {
        charViewModel.clear()
        verify { charViewModel.clear() }
    }

    @Test
    fun `check if we can get adjacencies from char viewModel`() {
        charViewModel.getAdjacencies()
        verify { charViewModel.getAdjacencies() }
    }

    @Test
    fun `check if we can get edges from char viewModel`() {
        charViewModel.getEdges()
        verify { charViewModel.getEdges() }
    }

}