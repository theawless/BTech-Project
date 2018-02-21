package com.gobbledygook.theawless.speechy.utils

import android.media.AudioFormat

object AudioConstants {
    const val SAMPLE_RATE = 16000
    const val OUT_CHANNEL = AudioFormat.CHANNEL_OUT_MONO
    const val IN_CHANNEL = AudioFormat.CHANNEL_IN_MONO
    const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    const val MAX_DURATION = 0.8 // In seconds.
    const val MAX_BYTES = AudioConstants.SAMPLE_RATE * MAX_DURATION * 2 // 16 bit encoding.
    const val SPHINX_BUFFER_SIZE = 4096
}

object SpeechConstants {
    const val N_UTTERANCES = 16
    val WORDS = arrayOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

    enum class RecognitionMode { HMM, SPHINX }
}

object UtilsConstants {
    const val RECORD_FILENAME = "test"
    const val RECORD_FOLDER = "recording"
    const val SPHINX_FOLDER = "sphinx"
}