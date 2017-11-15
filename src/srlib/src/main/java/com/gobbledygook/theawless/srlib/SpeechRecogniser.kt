package com.gobbledygook.theawless.srlib

import com.gobbledygook.theawless.srlib.utils.AudioConstants
import com.gobbledygook.theawless.srlib.utils.Distance
import com.gobbledygook.theawless.srlib.utils.Utils
import java.io.File

class SpeechRecogniser(files: Map<String, File>) {
    private val audioPreprocessor = AudioPreprocessor()
    private val frameRepresentor = FrameRepresentor()

    private val vowelRepresentations = files.mapValues {
        representSignal(audioPreprocessor.process(Utils.readFileToSignal(it.value)))
    }

    fun getVowel(file: File): String {
        val representation = representSignal(audioPreprocessor.process(Utils.readFileToSignal(file)))
        return vowelRepresentations.minBy {
            it.value.mapIndexed { i, _ -> Distance.tokhura(representation[i], it.value[i]) }.average()
        }!!.key
    }

    private fun representSignal(signal: MutableList<Double>): MutableList<MutableList<Double>> {
        val signalWindows = signal.windowed(AudioConstants.FRAME_SIZE, AudioConstants.FRAME_SLIDE)
        val representation = signalWindows.map {
            val frame = it.toMutableList()
            frameRepresentor.represent(frame)
        }
        return representation.toMutableList()
    }
}