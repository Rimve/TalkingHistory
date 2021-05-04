package com.neverim.talkinghistory.data


import android.util.Log
import com.google.firebase.database.DataSnapshot
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Test

class CharacterRepositoryTest {

    @RelaxedMockK
    private lateinit var charDao: CharacterDao

    @RelaxedMockK
    private lateinit var vertex: Vertex
    private lateinit var charRepo: CharacterRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Log::class)
        MockKAnnotations.init(this)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        charRepo = spyk(CharacterRepository.getInstance(charDao))
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `verify vertex creation`() {
        charRepo.createVertex(0, "Test Vertex")
        verify { charRepo.createVertex(any(), any()) }
    }

    @Test
    fun `verify addition of directed edges`() {
        charRepo.addDirectedEdge(vertex, vertex)
        verify { charRepo.addDirectedEdge(vertex, vertex) }
    }

    @Test
    fun `verify addition of undirected edges`() {
        charRepo.addUndirectedEdge(vertex, vertex)
        verify { charRepo.addUndirectedEdge(vertex, vertex) }
    }

    @Test
    fun `check if adjacency list is returned`() {
        charRepo.getAdjacencies()
        verify { charRepo.getAdjacencies() }
    }

    @Test
    fun `check if edges are returned`() {
        charRepo.getEdges()
        verify { charRepo.getEdges() }
    }

    @Test
    fun `check if questions are returned`() {
        charRepo.getQuestions()
        verify { charRepo.getQuestions() }
    }

    @Test
    fun `check if first question is returned`() {
        charRepo.retrieveFirst()
        verify { charRepo.retrieveFirst() }
    }

    @Test
    fun `verify if lists are cleared`() {
        charRepo.clear()
        verify { charRepo.clear() }
    }

    @Test
    fun `check if edges are being returned`() {
        charRepo.edges(vertex)
        verify { charRepo.edges(vertex) }
    }

    @Test
    fun `check if edges without UI update are being returned`() {
        charRepo.edgesWithoutUi(vertex)
        verify { charRepo.edgesWithoutUi(vertex) }
    }

    @Test
    fun `check if adjacencies are returned`() {
        charRepo.getAdjacencies()
        verify { charRepo.getAdjacencies() }
    }

    @Test
    fun `check if audio file list data is being handled correctly when given array`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedArray: ArrayList<String> = ArrayList()

        mockedArray.add("test subj")
        mockedArray.add("test subj")
        mockedArray.add("test subj")

        every { dataSnapshot.value } returns mockedArray
        charRepo.getFileListFromDatabase(dataSnapshot, "Test")
        verify { charRepo.getFileListFromDatabase(dataSnapshot, "Test") }
    }

    @Test
    fun `check if audio file list data is being handled when given hashMap`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedHash: HashMap<String, String> = HashMap()

        mockedHash["0"] = "test"
        mockedHash["1"] = "test"
        mockedHash["2"] = "test"

        every { dataSnapshot.value } returns mockedHash
        charRepo.getFileListFromDatabase(dataSnapshot, "Test")
        verify { charRepo.getFileListFromDatabase(dataSnapshot, "Test") }
    }

    @Test
    fun `check if node list data is being handled when given array`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedArray: ArrayList<String> = ArrayList()

        mockedArray.add("test subj")
        mockedArray.add("test subj")
        mockedArray.add("test subj")

        every { dataSnapshot.value } returns mockedArray
        charRepo.getNodesFromDatabase(dataSnapshot)
        verify { charRepo.getNodesFromDatabase(dataSnapshot) }
    }

    @Test
    fun `check if node list data is being handled when given hashMap`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedHash: HashMap<String, String> = HashMap()

        mockedHash["0"] = "test"
        mockedHash["1"] = "test"
        mockedHash["2"] = "test"

        every { dataSnapshot.value } returns mockedHash
        charRepo.getNodesFromDatabase(dataSnapshot)
        verify { charRepo.getNodesFromDatabase(dataSnapshot) }
    }

    @Test(expected = NullPointerException::class)
    fun `check if adjacencies data is being handled when given hashMap`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedArray: ArrayList<String> = ArrayList()
        val mockedHash: HashMap<String, ArrayList<String>> = HashMap()

        mockedArray.add("test subj")
        mockedArray.add("test subj")
        mockedArray.add("test subj")

        mockedHash["0"] = mockedArray
        mockedHash["1"] = mockedArray
        mockedHash["2"] = mockedArray

        every { dataSnapshot.value } returns mockedHash
        charRepo.getAdjacenciesFromDatabase(dataSnapshot)
        verify { charRepo.getAdjacenciesFromDatabase(dataSnapshot) }
    }

    @Test(expected = NullPointerException::class)
    fun `check if adjacencies data is being handled when given arrayList of arrayLists`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedArray: ArrayList<String> = ArrayList()
        val mockedArrayOfArrays: ArrayList<ArrayList<String>> = ArrayList()

        mockedArray.add("test subj")
        mockedArray.add("test subj")
        mockedArray.add("test subj")

        mockedArrayOfArrays.add(mockedArray)
        mockedArrayOfArrays.add(mockedArray)
        mockedArrayOfArrays.add(mockedArray)

        every { dataSnapshot.value } returns mockedArrayOfArrays
        charRepo.getAdjacenciesFromDatabase(dataSnapshot)
        verify { charRepo.getAdjacenciesFromDatabase(dataSnapshot) }
    }

    @Test(expected = NullPointerException::class)
    fun `check if adjacencies data is being handled when given arrayList of hashMaps`() {
        val dataSnapshot: DataSnapshot = mockk(relaxed = true)
        val mockedHash: HashMap<String, String> = HashMap()
        val mockedArrayOfHashes: ArrayList<HashMap<String, String>> = ArrayList()

        mockedHash["0"] = "test"
        mockedHash["1"] = "test"
        mockedHash["2"] = "test"

        mockedArrayOfHashes.add(mockedHash)
        mockedArrayOfHashes.add(mockedHash)
        mockedArrayOfHashes.add(mockedHash)

        every { dataSnapshot.value } returns mockedArrayOfHashes
        charRepo.getAdjacenciesFromDatabase(dataSnapshot)
        verify { charRepo.getAdjacenciesFromDatabase(dataSnapshot) }
    }
}