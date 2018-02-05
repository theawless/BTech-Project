package com.gobbledygook.theawless.speechy.app

import com.gobbledygook.theawless.speechy.utils.AudioConstants
import com.gobbledygook.theawless.speechy.utils.Converter
import com.gobbledygook.theawless.speechy.utils.Utils
import com.gobbledygook.theawless.speechy.utils.UtilsConstants
import edu.cmu.pocketsphinx.Decoder
import java.io.File
import java.io.FileInputStream

class PocketSphinx(path: String) {
    companion object {
        init {
            System.loadLibrary("pocketsphinx_jni")
        }
    }

    private val decoder: Decoder

    init {
        val config = Decoder.defaultConfig()
        config.setString("-hmm", Utils.combinePaths(path, UtilsConstants.SPHINX_FOLDER, "en-us-ptm"))
        config.setString("-dict", Utils.combinePaths(path, UtilsConstants.SPHINX_FOLDER, "cmudict-en-us.dict"))
        decoder = Decoder(config)
        decoder.setJsgfFile("digits", Utils.combinePaths(path, UtilsConstants.SPHINX_FOLDER, "digits.gram"))
        decoder.search = "digits"
    }

    fun getWord(path: String): String {
        val recordPath = Utils.combinePaths(path, UtilsConstants.RECORD_FOLDER, UtilsConstants.RECORD_FILENAME)
        val inputStream = FileInputStream(File(recordPath))

        decoder.startUtt()
        val byteArray = ByteArray(AudioConstants.SPHINX_BUFFER_SIZE)
        while (inputStream.read(byteArray, 0, AudioConstants.SPHINX_BUFFER_SIZE) != -1) {
            val audioData = Converter.byteArrayToShortArray(byteArray)
            decoder.processRaw(audioData, audioData.size.toLong(), false, false)
        }
        decoder.endUtt()
        return decoder.hyp().hypstr
    }
}