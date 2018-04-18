package com.gobbledygook.theawless.speechy

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.util.concurrent.TimeUnit


class FloatingService : Service() {
    companion object {
        private const val RECORD_FILENAME: String = "__test__"
        private const val RECORD_DURATION: Double = 1.25
    }

    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val folder by lazy {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getExternalFilesDir(null).path!!
        } else {
            filesDir.path!!
        }
    }
    private val floater by lazy {
        ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
    }
    private val toast by lazy {
        Toast.makeText(this, "", Toast.LENGTH_LONG)
    }
    private val audioRecorder = WavRecorder()
    private var sentence = mutableListOf<String>()
    private val actionPerformer = ActionPerformer(this)

    init {
        System.loadLibrary("Speechy")
    }

    override fun onCreate() {
        super.onCreate()

        statefulActionGUI {
            setup(folder + File.separator)
        }

        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                                                WindowManager.LayoutParams.WRAP_CONTENT,
                                                WindowManager.LayoutParams.TYPE_PHONE,
                                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = Resources.getSystem().displayMetrics.heightPixels / 2
        windowManager.addView(floater, params)

        floater.progressDrawable = getDrawable(R.drawable.progress)
        floater.setOnClickListener {
            sentenceTest()
        }
        floater.setOnTouchListener(object : View.OnTouchListener {
            private var lastX = 0
            private var lastY = 0
            private var lastTouchX = 0.0f
            private var lastTouchY = 0.0f

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = params.x
                        lastY = params.y
                        lastTouchX = event.rawX
                        lastTouchY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = lastX + (event.rawX - lastTouchX).toInt()
                        params.y = lastY + (event.rawY - lastTouchY).toInt()
                        windowManager.updateViewLayout(floater, params)
                    }
                }
                return false
            }
        })
    }

    private fun sentenceTest() {
        statefulActionGUI {
            toastShow("")
            do {
                record()
                val word = recognise(getTestFilename(), true)
                sentence.add(word)
                toastShow(sentence.joinToString(" "))
                val done = actionPerformer.perform(sentence)
            } while (!done && !word.isEmpty())

            sentence.clear()
        }
    }

    private fun record() {
        launch(UI) {
            val flicker = 5
            var progress = 0.0
            do {
                delay(flicker.toLong(), TimeUnit.MILLISECONDS)
                progress += flicker / (RECORD_DURATION * 10)
                floater.progress = progress.toInt()
            } while (progress < 100)
        }

        audioRecorder.record(getTestFilename(), RECORD_DURATION)
    }

    private fun getTestFilename(): String {
        return "$folder${File.separator}$RECORD_FILENAME"
    }

    private fun statefulActionGUI(action: () -> Unit) {
        launch(CommonPool) {
            setState(false)
            action()
            setState(true)
        }
    }

    private fun toastShow(text: String) {
        launch(UI) {
            toast.setText(text)
            toast.show()
        }
    }

    private fun setState(enabled: Boolean) {
        launch(UI) {
            floater.isEnabled = enabled
        }
    }

    private external fun setup(folder: String)

    private external fun recognise(filename: String, restart: Boolean): String

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floater)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}