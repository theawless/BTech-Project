package com.gobbledygook.theawless.speechy.utils

import java.io.File
import java.nio.ByteBuffer


object Converter {
    fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(shortArray.size * 2)
        byteBuffer.asShortBuffer()
                .put(shortArray)
        return byteBuffer.array()
    }

    fun byteArrayToShortArray(byteArray: ByteArray): ShortArray {
        val shortArray = ShortArray(byteArray.size / 2)
        ByteBuffer.wrap(byteArray)
                .asShortBuffer()
                .get(shortArray)
        return shortArray
    }
}

object Utils {
    fun combinePaths(path1: String, path2: String): String = File(File(path1), path2).path

    fun getFilenameForIndex(index: Int): String = UtilsConstants.FILENAME + "_" + index.toString()
}