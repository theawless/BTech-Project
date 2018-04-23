package com.gobbledygook.theawless.speechy

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlin.math.ceil

class RoundProgress(context: Context, attrs: AttributeSet?) : ProgressBar(context, attrs, android.R.attr.progressBarStyleHorizontal) {
    constructor(context: Context) : this(context, null)

    companion object {
        private const val FLICKER = 5.0
    }

    init {
        progressDrawable = context.getDrawable(R.drawable.progress)
    }

    fun start(duration: Double) {
        launch(UI) {
            var progressValue = 0.0
            progress = 0
            do {
                progressValue += FLICKER / (duration * 10)
                progress = ceil(progressValue).toInt()
                delay(FLICKER.toLong())
            } while (progress < 100)
        }
    }

    fun statefulActionGUI(action: () -> Unit) {
        launch(CommonPool) {
            setState(false)
            action()
            setState(true)
        }
    }

    private fun setState(enabled: Boolean) {
        launch(UI) {
            isClickable = enabled
            alpha = if (enabled) 0.5f else 1.0f
        }
    }
}