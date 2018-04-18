package com.gobbledygook.theawless.speechy

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

class WavRecorder {
    companion object {
        private const val N_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val HZ_SAMPLE = 16000
    }

    fun record(filename: String, duration: Double) {
        val bytesToRead = (HZ_SAMPLE * duration * 2).toInt()
        DataOutputStream(FileOutputStream(File("$filename.wav"))).use {
            // RIFF
            it.writeBytes("RIFF")
            it.writeInt(java.lang.Integer.reverseBytes((36 + bytesToRead)))
            it.writeBytes("WAVE")

            // fmt
            it.writeBytes("fmt ")
            it.writeInt(java.lang.Integer.reverseBytes(16))
            it.writeShort(java.lang.Short.reverseBytes(1).toInt())
            it.writeShort(java.lang.Short.reverseBytes(N_CHANNEL.toShort()).toInt())
            it.writeInt(java.lang.Integer.reverseBytes(HZ_SAMPLE))
            it.writeInt(java.lang.Integer.reverseBytes((HZ_SAMPLE * N_CHANNEL * 16 / 8)))
            it.writeShort(java.lang.Short.reverseBytes((N_CHANNEL * 16 / 8).toShort()).toInt())
            it.writeShort(java.lang.Short.reverseBytes(16).toInt())

            // data
            it.writeBytes("data")
            it.writeInt(java.lang.Integer.reverseBytes(bytesToRead))
            it.write(getAudio(bytesToRead))
        }
    }

    private fun getAudio(bytesToRead: Int): ByteArray {
        val audioData = ByteArray(bytesToRead)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT, HZ_SAMPLE, N_CHANNEL, AudioFormat.ENCODING_PCM_16BIT,
                                      AudioRecord.getMinBufferSize(HZ_SAMPLE, N_CHANNEL, AudioFormat.ENCODING_PCM_16BIT))
        audioRecord.startRecording()
        audioRecord.read(audioData, 0, bytesToRead)
        audioRecord.stop()
        audioRecord.release()
        return audioData
    }
}