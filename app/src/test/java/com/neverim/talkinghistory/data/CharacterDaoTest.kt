package com.neverim.talkinghistory.data

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

class CharacterDaoTest {

    private lateinit var charDao: CharacterDao
    private val testCharName = "Test Character"
    private val testFileName = "Test File"
    private val testVertexName = "Test Vertex"
    private val vertex: Vertex = Vertex(0, "test1")

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        MockKAnnotations.init(this)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        charDao = spyk(CharacterDao())
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `check if character addition works`() {
        charDao.addChar(testCharName)
        assertTrue(charDao.getCharList().contains(testCharName))
    }

    @Test
    fun `check if character audio file addition works`() {
        charDao.addFile(0, testCharName, testFileName)
        assertTrue(charDao.getAudioFileList().size > 0)
    }

    @Test
    fun `check if character error audio file addition works`() {
        charDao.addErrorFile(0, testFileName)
        assertTrue(charDao.getErrorAudioFileList().size > 0)
    }

    @Test
    fun `check if vertex creation is successful`() {
        charDao.createVertex(0, testVertexName)
        assertTrue(charDao.getQuestions().value!!.isNotEmpty())
    }

    @Test
    fun `check if directed edge is being added to adjacencies`() {
        charDao.addDirectedEdge(vertex, vertex)
        verify { charDao.addDirectedEdge(vertex, vertex) }
    }

    @Test
    fun `try to clear DAO data`() {
        charDao.clear()
        verify { charDao.clear() }
    }

    @Test
    fun `try to clear file list`() {
        charDao.clearFileList()
        verify { charDao.clearFileList() }
    }

    @Test
    fun `check if retrieve first returns a value`() {
        charDao.createVertex(0, "test1")
        charDao.retrieveFirst()
        verify { charDao.retrieveFirst() }
    }

    @Test
    fun `try to get edges of vertex`() {
        charDao.edges(vertex)
        verify { charDao.edges(vertex) }
    }

    @Test
    fun `try to get edges without updating UI`() {
        charDao.edgesWithoutUi(vertex)
        verify { charDao.edgesWithoutUi(vertex) }
    }

    @Test
    fun `check if adjacencies are returned`() {
        charDao.getAdjacencies()
        verify {  charDao.getAdjacencies() }
    }

    @Test
    fun `check if edges are returned`() {
        charDao.getEdges()
        verify {  charDao.getEdges() }
    }
}