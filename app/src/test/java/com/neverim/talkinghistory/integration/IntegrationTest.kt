package com.neverim.talkinghistory.integration

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.google.common.truth.Truth.assertThat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neverim.talkinghistory.data.*
import com.neverim.talkinghistory.utilities.InjectorUtils
import com.neverim.talkinghistory.viewmodels.CharacterViewModel
import com.neverim.talkinghistory.viewmodels.RecognizerViewModel
import com.neverim.talkinghistory.viewmodels.StorageViewModel
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class IntegrationTest {

    private val viewModelStore: ViewModelStore = mockk(relaxed = true)

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        mockkStatic(FirebaseStorage::class)
        mockkStatic(FirebaseDatabase::class)
        MockKAnnotations.init(this)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { FirebaseStorage.getInstance() } returns mockk(relaxed = true)
        every { FirebaseDatabase.getInstance() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `check creation of characterViewModel`() {
        val characterViewModelProvider = InjectorUtils.provideCharacterViewModelFactory()
        val characterViewModel = ViewModelProvider(viewModelStore, characterViewModelProvider).get(CharacterViewModel::class.java)
        assertThat(characterViewModel).isInstanceOf(CharacterViewModel::class.java)
    }

    @Test
    fun `check creation of recognitionViewModel`() {
        val recognitionModelProvider = InjectorUtils.provideRecognizerViewModelFactory()
        val recognizerViewModel = ViewModelProvider(viewModelStore, recognitionModelProvider).get(RecognizerViewModel::class.java)
        assertThat(recognizerViewModel).isInstanceOf(RecognizerViewModel::class.java)
    }

    @Test
    fun `check creation of storageViewModel`() {
        val storageModelProvider = InjectorUtils.provideStorageViewModelFactory()
        val storageViewModel = ViewModelProvider(viewModelStore, storageModelProvider).get(StorageViewModel::class.java)
        assertThat(storageViewModel).isInstanceOf(StorageViewModel::class.java)
    }

    @Test
    fun `check if recognition starts successfully`() {
        val context: Context = mockk(relaxed = true)
        val recognitionModelProvider = InjectorUtils.provideRecognizerViewModelFactory()
        val recognizerViewModel = ViewModelProvider(viewModelStore, recognitionModelProvider).get(RecognizerViewModel::class.java)
        recognizerViewModel.startRecognition(context)
        assertThat(recognizerViewModel.isRecognizing()).isTrue()
    }

    @Test
    fun `check if audio setup is working`() {
        val context: Context = mockk(relaxed = true)
        val recognitionModelProvider = InjectorUtils.provideRecognizerViewModelFactory()
        val recognizerViewModel = ViewModelProvider(viewModelStore, recognitionModelProvider).get(RecognizerViewModel::class.java)
        assertThat(recognizerViewModel.audioSetup(context)).isFalse()
    }

    @Test
    fun `check if character repo is being created`() {
        val charDao = CharacterDao()
        val charRepo: CharacterRepository = CharacterRepository.getInstance(charDao)
        assertThat(charRepo).isNotNull()
    }

    @Test
    fun `check if storage repo is being created`() {
        val storageDao = StorageDao()
        val storageRepo: StorageRepository = StorageRepository.getInstance(storageDao)
        assertThat(storageRepo).isNotNull()
    }

    @Test
    fun `check if recognizer repo is being created`() {
        val recoDao = RecognizerDao()
        val recoRepo = RecognizerRepository(recoDao)
        assertThat(recoRepo).isNotNull()
    }

    @Test
    fun `check vertex creation`() {
        val charDao = CharacterDao()
        val charRepo: CharacterRepository = CharacterRepository.getInstance(charDao)
        charRepo.createVertex(0, "Test")
        assertThat(charRepo.getQuestions()).isNotNull()
    }

    @Test
    fun `check vertex adding directed edge`() {
        val vertex = Vertex(0, "Test")
        val charDao = CharacterDao()
        val charRepo: CharacterRepository = CharacterRepository.getInstance(charDao)
        charRepo.addDirectedEdge(vertex, vertex)
        assertThat(charRepo.getAdjacencies()).isNotNull()
    }

    @Test
    fun `check if charRepo is cleanable`() {
        val charDao = CharacterDao()
        val charRepo: CharacterRepository = CharacterRepository.getInstance(charDao)
        charRepo.createVertex(0, "Test")
        charRepo.clear()
        verify { charRepo.clear() }
    }

}