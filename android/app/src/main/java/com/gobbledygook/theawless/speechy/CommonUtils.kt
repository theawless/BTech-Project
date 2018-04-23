package com.gobbledygook.theawless.speechy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

object Constants {
    const val RECORD_DURATION = 1.25
}

object Functions {
    fun getFolder(context: Context): String {
        return if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            "${context.getExternalFilesDir(null).path!!}/"
        } else {
            "${context.filesDir.path!!}/"
        }
    }

    fun getFilename(context: Context, word: String): String {
        return "${getFolder(context)}$word"
    }
}