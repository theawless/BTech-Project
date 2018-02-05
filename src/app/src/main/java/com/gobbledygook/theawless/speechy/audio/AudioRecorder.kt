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
import java.io.PrintWriter

class AudioRecorder(private var listener: Listener) {
    interface Listener {
        var isRecording: Boolean
    }

    lateinit var recordPath: String

    companion object {
        private val TAG = AudioRecorder::class.java.simpleName
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(AudioConstants.SAMPLE_RATE,
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
        val outputStream = FileOutputStream(File(recordPath))
        val printWriter = PrintWriter(File(recordPath + ".txt"))
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                                      AudioConstants.SAMPLE_RATE,
                                      AudioConstants.IN_CHANNEL,
                                      AudioConstants.ENCODING,
                                      BUFFER_SIZE)
        asyncRecord(audioRecord, outputStream, printWriter).await()
        outputStream.close()
        printWriter.close()
        audioRecord.release()

        isRecording = false
        listener.isRecording = false
    }

    private fun asyncRecord(audioRecord: AudioRecord, outputStream: FileOutputStream, printWriter: PrintWriter) = async(CommonPool) {
        audioRecord.startRecording()
        Log.v(TAG, "Start recording. Buffer size: $BUFFER_SIZE")

        var bytesRead = 0L
        val audioData = ShortArray(BUFFER_SIZE / 2)
        do {
            audioRecord.read(audioData, 0, audioData.size)
            val byteArray = Converter.shortArrayToByteArray(audioData)
            outputStream.write(byteArray)
            audioData.forEach { printWriter.println(it.toDouble()) }
            bytesRead += byteArray.size.toLong()
        } while (isRecording && bytesRead < AudioConstants.MAX_BYTES)
        audioRecord.stop()
        Log.v(TAG, "Stop recording. Bytes read: $bytesRead")
    }
}