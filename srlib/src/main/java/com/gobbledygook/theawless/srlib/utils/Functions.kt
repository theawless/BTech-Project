package com.gobbledygook.theawless.srlib.utils

import java.io.File
import java.nio.ByteBuffer

object Converter {
    fun byteArrayToShortArray(byteArray: ByteArray): ShortArray {
        val shortArray = ShortArray(byteArray.size / 2)
        ByteBuffer.wrap(byteArray)
                .asShortBuffer()
                .get(shortArray)
        return shortArray
    }

    fun shortArrayToDoubleList(shortArray: ShortArray): MutableList<Double> =
            shortArray.map { it.toDouble() }.toMutableList()
}

object Utils {
    fun readFileToSignal(file: File): MutableList<Double> {
        val fileBytes = file.readBytes()
        val shortArray = Converter.byteArrayToShortArray(fileBytes)
        return Converter.shortArrayToDoubleList(shortArray)
    }
}