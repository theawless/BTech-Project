package com.gobbledygook.theawless.speechy.app

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gobbledygook.theawless.speechy.R
import com.gobbledygook.theawless.speechy.audio.AudioRecorder
import com.gobbledygook.theawless.speechy.utils.SpeechConstants
import com.gobbledygook.theawless.speechy.utils.Utils
import com.gobbledygook.theawless.speechy.utils.UtilsConstants
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(), AudioRecorder.Listener {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    override var isRecording: Boolean = false
        set(value) {
            field = value
            if (value) {
                recordFab.setImageResource(R.drawable.mic_off)
                snackbar.setText("Recording now...")
            } else {
                recordFab.setImageResource(R.drawable.mic_on)
                snackbar.setText("You have spoken: " + getWord())
            }
            audioRecorder.isRecording = value
        }

    private val snackbar by lazy { Snackbar.make(mainContent, "Press the record button", Snackbar.LENGTH_INDEFINITE) }
    private val audioRecorder by lazy {
        val audioRecorder = AudioRecorder(this)
        audioRecorder.recordPath = Utils.combinePaths((activity as MainActivity).speechDirPath, UtilsConstants.FILENAME)
        audioRecorder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordFab.setOnClickListener { onActionButtonClick() }
        snackbar.show()
    }

    private fun onActionButtonClick() {
        isRecording = !isRecording
    }

    private fun getWord(): String {
        val wordIndex = getWordIndex((activity as MainActivity).speechDirPath)
        return SpeechConstants.WORDS[wordIndex]
    }

    private external fun getWordIndex(path: String): Int
}