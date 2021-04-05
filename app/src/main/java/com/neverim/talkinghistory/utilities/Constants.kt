package com.neverim.talkinghistory.utilities

import android.media.AudioFormat

object Constants {
    val SAMPLE_RATE_CANDIDATES = intArrayOf(16000, 11025, 22050, 44100)
    const val RECORDER_SAMPLE_RATE: Int = 16000
    const val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    const val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    const val RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO
    const val LANGUAGE_CODEC = "lt-LT"
    const val BACK_TIME_INTERVAL = 2000
    const val MAXIMUM_LEV_DISTANCE = 5
    const val WORD_ANY = "..."
    const val WORD_OTHER = "Kiti"
}