package com.gobbledygook.theawless.speechy.app

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gobbledygook.theawless.speechy.R
import com.gobbledygook.theawless.speechy.audio.AudioRecorder
import com.gobbledygook.theawless.speechy.utils.AudioConstants
import com.gobbledygook.theawless.speechy.utils.SpeechConstants
import com.gobbledygook.theawless.speechy.utils.Utils
import com.gobbledygook.theawless.speechy.utils.UtilsConstants
import com.gobbledygook.theawless.srlib.SpeechRecogniser
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File

class MainFragment : Fragment(), AudioRecorder.Listener {
    override var isRecording: Boolean = false
        set(value) {
            field = value
            if (value) {
                recordFab.setImageResource(R.drawable.mic_off)
                snackbar.setText("Recording now...")
                snackbar.show()
            } else {
                recordFab.setImageResource(R.drawable.mic_on)
                snackbar.setText("You have spoken: " + getVowel())
            }
            audioRecorder.isRecording = value
        }

    private val snackbar by lazy { Snackbar.make(mainContent, "", (AudioConstants.MAX_DURATION + 3) * 1000) }
    private val audioRecorder by lazy {
        val audioRecorder = AudioRecorder(this)
        audioRecorder.file = File((activity as MainActivity).speechDir, UtilsConstants.FILENAME)
        audioRecorder
    }
    private val vowelFiles by lazy {
        SpeechConstants.VOWELS.associateBy({ it }, { File((activity as MainActivity).speechDir, Utils.getFilenameForVowel(it)) })
    }
    private val vowelRecogniser by lazy { SpeechRecogniser(vowelFiles) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordFab.setOnClickListener { onActionButtonClick() }
        vowelRecogniser
    }

    private fun onActionButtonClick() {
        isRecording = !isRecording
    }

    private fun getVowel(): String = vowelRecogniser.getVowel(File((activity as MainActivity).speechDir, UtilsConstants.FILENAME))
}