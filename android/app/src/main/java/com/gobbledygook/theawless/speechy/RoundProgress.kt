package com.gobbledygook.theawless.speechy

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class RoundProgress(context: Context, attrs: AttributeSet?) : ProgressBar(context, attrs, android.R.attr.progressBarStyleHorizontal) {
    constructor(context: Context) : this(context, null)

    companion object {
        private const val FLICKER = 20.0
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
                progress = Math.ceil(progressValue).toInt()
                delay(FLICKER.toLong())
            } while (progressValue < 100)
        }
    }

    fun setState(enabled: Boolean) {
        isClickable = enabled
        alpha = if (enabled) 0.5f else 1.0f
    }
}