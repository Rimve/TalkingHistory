package com.neverim.talkinghistory.data

import android.content.Context


class RecognizerRepository constructor(private val recognizerDao: RecognizerDao) {

    fun audioSetup(context: Context): Boolean = recognizerDao.audioSetup(context)
    fun startRecognition(context: Context) = recognizerDao.startRecognition(context)
    fun stopRecognition() = recognizerDao.stopRecognition()
    fun isRecognizing() = recognizerDao.isRecognizing()
    fun getTranscript() = recognizerDao.getTranscript()

}
