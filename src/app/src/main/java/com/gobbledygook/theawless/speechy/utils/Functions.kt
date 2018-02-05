package com.gobbledygook.theawless.speechy.utils

import java.io.File
import java.nio.ByteBuffer


object Converter {
    fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(shortArray.size * 2)
        byteBuffer.asShortBuffer().put(shortArray)
        return byteBuffer.array()
    }

    fun byteArrayToShortArray(byteArray: ByteArray): ShortArray {
        val shortArray = ShortArray(byteArray.size / 2)
        ByteBuffer.wrap(byteArray).asShortBuffer().get(shortArray)
        return shortArray
    }
}

object Utils {
    fun combinePaths(vararg paths: String): String {
        if (paths.isEmpty())
            return ""

        var combined = File("")
        for (path in paths)
            combined = File(combined, path)
        return combined.path
    }
}