package com.gobbledygook.theawless.speechy

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class WavRecorder {
    companion object {
        private const val HZ_SAMPLE = 16000
    }

    fun getExtension(): String {
        return ".wav"
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
            it.writeShort(java.lang.Short.reverseBytes(1).toInt())
            it.writeInt(java.lang.Integer.reverseBytes(HZ_SAMPLE))
            it.writeInt(java.lang.Integer.reverseBytes((HZ_SAMPLE * 1 * 16 / 8)))
            it.writeShort(java.lang.Short.reverseBytes(1 * 16 / 8).toInt())
            it.writeShort(java.lang.Short.reverseBytes(16).toInt())

            // data
            it.writeBytes("data")
            it.writeInt(java.lang.Integer.reverseBytes(bytesToRead))
            it.write(getAudio(bytesToRead))
        }
    }

    private fun getAudio(bytesToRead: Int): ByteArray {
        val audioData = ByteBuffer.allocateDirect(bytesToRead)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT, HZ_SAMPLE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                                      AudioRecord.getMinBufferSize(HZ_SAMPLE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT))
        audioRecord.startRecording()
        audioRecord.read(audioData, bytesToRead, AudioRecord.READ_BLOCKING)
        audioRecord.stop()
        audioRecord.release()
        return audioData.array()
    }
}