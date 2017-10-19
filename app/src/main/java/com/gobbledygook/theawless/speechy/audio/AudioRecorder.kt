package com.gobbledygook.theawless.speechy.audio

import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.gobbledygook.theawless.speechy.utils.AudioConstants
import com.gobbledygook.theawless.speechy.utils.Converter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private var listener: Listener) {
    interface Listener {
        var isRecording: Boolean
    }

    lateinit var file: File

    companion object {
        private val TAG = AudioRecorder::class.java.simpleName
        private val bufferSize = AudioRecord.getMinBufferSize(AudioConstants.SAMPLE_RATE,
                AudioConstants.IN_CHANNEL,
                AudioConstants.ENCODING)
    }

    var isRecording = false
        set(value) {
            if (field == value) return
            field = value
            if (field) record()
        }

    private fun record() = async(UI) {
        val outputStream = FileOutputStream(file)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                AudioConstants.SAMPLE_RATE,
                AudioConstants.IN_CHANNEL,
                AudioConstants.ENCODING,
                bufferSize)
        asyncRecord(audioRecord, outputStream).await()
        outputStream.close()
        audioRecord.release()
        isRecording = false
        listener.isRecording = false
    }

    private fun asyncRecord(audioRecord: AudioRecord, outputStream: FileOutputStream) = async(CommonPool) {
        audioRecord.startRecording()
        Log.v(TAG, "Start recording. Buffer size: $bufferSize")

        var bytesRead = 0L
        val audioData = ShortArray(bufferSize / 2)
        while (isRecording && bytesRead < AudioConstants.MAX_BYTES) {
            audioRecord.read(audioData, 0, audioData.size)
            val byteArray = Converter.shortArrayToByteArray(audioData)
            outputStream.write(byteArray)
            bytesRead += byteArray.size.toLong()
        }
        audioRecord.stop()
        Log.v(TAG, "Stop recording. Bytes read: $bytesRead")
    }
}