package com.gobbledygook.theawless.speechy

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class FloatingService : Service() {
    companion object {
        private const val RECORD_FILENAME: String = "__test__"
    }

    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val floater by lazy {
        RoundProgress(this)
    }
    private val toast by lazy {
        Toast.makeText(this, "", Toast.LENGTH_LONG)
    }
    private val audioRecorder = WavRecorder()
    private val actionPerformer = ActionPerformer(this)

    init {
        System.loadLibrary("Speechy")
    }

    override fun onCreate() {
        super.onCreate()

        launch(UI) {
            floater.setState(false)
            withContext(CommonPool) {
                setup(Functions.getFolder(this@FloatingService))
            }
            floater.setState(true)
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
        launch(UI) {
            floater.setState(false)
            toastShow("")

            val filename = Functions.getFilename(this@FloatingService, RECORD_FILENAME)
            val sentence = mutableListOf<String>()
            do {
                floater.start(Constants.RECORD_DURATION)
                val word = withContext(CommonPool) {
                    audioRecorder.record(filename, Constants.RECORD_DURATION)
                    recognise(filename, sentence.size == 0)
                }
                if (word.isEmpty()) {
                    break
                }
                sentence.add(word)
                toastShow(sentence.joinToString(" "))
            } while (!actionPerformer.perform(sentence))

            floater.setState(true)
        }
    }

    private fun toastShow(text: String) {
        toast.setText(text)
        toast.show()
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