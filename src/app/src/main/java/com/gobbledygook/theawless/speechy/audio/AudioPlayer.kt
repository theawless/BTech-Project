package com.gobbledygook.theawless.speechy.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.gobbledygook.theawless.speechy.utils.AudioConstants
import com.gobbledygook.theawless.speechy.utils.Converter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File
import java.io.FileInputStream

class AudioPlayer(private var listener: Listener) {
    interface Listener {
        var isPlaying: Boolean
    }

    lateinit var playPath: String

    companion object {
        private val TAG = AudioPlayer::class.java.simpleName
        private val bufferSize = AudioTrack.getMinBufferSize(AudioConstants.SAMPLE_RATE,
                                                             AudioConstants.OUT_CHANNEL,
                                                             AudioConstants.ENCODING)
    }

    var isPlaying = false
        set(value) {
            if (field == value) return
            field = value
            if (field) play()
        }

    private fun play() = async(UI) {
        val inputStream = FileInputStream(File(playPath))
        val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                            .build())
                .setAudioFormat(AudioFormat.Builder()
                                        .setEncoding(AudioConstants.ENCODING)
                                        .setSampleRate(AudioConstants.SAMPLE_RATE)
                                        .setChannelMask(AudioConstants.OUT_CHANNEL)
                                        .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        asyncPlay(audioTrack, inputStream).await()
        inputStream.close()
        audioTrack.release()
        isPlaying = false
        listener.isPlaying = false
    }

    private fun asyncPlay(audioTrack: AudioTrack, inputStream: FileInputStream) = async(CommonPool) {
        audioTrack.play()
        Log.v(TAG, "Start playing. Buffer size: $bufferSize")

        var bytesWritten = 0L
        val byteArray = ByteArray(bufferSize)
        while (isPlaying && inputStream.read(byteArray, 0, bufferSize) != -1) {
            val audioData = Converter.byteArrayToShortArray(byteArray)
            audioTrack.write(audioData, 0, audioData.size)
            bytesWritten += byteArray.size.toLong()
        }
        audioTrack.stop()
        Log.v(TAG, "Stop playing. Bytes written: $bytesWritten")
    }
}