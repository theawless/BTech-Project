package com.gobbledygook.theawless.srlib

import com.gobbledygook.theawless.srlib.utils.AudioConstants
import com.gobbledygook.theawless.srlib.utils.Relation

class AudioPreprocessor {
    fun process(signal: MutableList<Double>): MutableList<Double> {
        fixDcOffset(signal)
        normalise(signal)
        return trimNoise(signal)
    }

    private fun trimNoise(signal: MutableList<Double>): MutableList<Double> {
        val signalChunks = signal.chunked(AudioConstants.FRAME_SIZE)
        val energies = signalChunks.map { Relation.energy(it) }
        val maxEnergyIndex = energies.indexOf(energies.max())
        val trimStart = maxEnergyIndex - AudioConstants.FRAME_SELECT
        val trimEnd = maxEnergyIndex + AudioConstants.FRAME_SELECT
        return signalChunks.subList(trimStart, trimEnd).flatten().toMutableList()
    }

    private fun fixDcOffset(signal: MutableList<Double>) {
        val meanAmplitude = signal.sumByDouble { it } / signal.size
        signal.forEachIndexed { i, _ -> signal[i] -= meanAmplitude }
    }

    private fun normalise(signal: MutableList<Double>) {
        val maxAmplitude = signal.max()!!
        signal.forEachIndexed { i, _ -> signal[i] *= AudioConstants.NORMALISATION_AMPLITUDE / maxAmplitude }
    }
}