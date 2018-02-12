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
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


class MainFragment : Fragment(), AudioRecorder.Listener {
    companion object {
        init {
            System.loadLibrary("direct")
            System.loadLibrary("hmm")
        }
    }

    override var isRecording: Boolean = false
        set(value) {
            field = value
            if (value) {
                recordFab.setImageResource(R.drawable.mic_off)
                snackbar.setText("Recording now...")
            } else {
                recordFab.setImageResource(R.drawable.wait)
                snackbar.setText("Crunching the numbers...")
                async(UI) {
                    snackbar.setText("You have spoken: '" + getWord().await() + "'")
                    recordFab.setImageResource(R.drawable.mic_on)
                }
            }
            audioRecorder.isRecording = value
        }

    private val snackbar by lazy { Snackbar.make(mainContent, "Press record button", Snackbar.LENGTH_INDEFINITE) }
    private val audioRecorder by lazy {
        val audioRecorder = AudioRecorder(this)
        audioRecorder.recordPath = Utils.combinePaths((activity as MainActivity).speechDirPath,
                                                      UtilsConstants.RECORD_FOLDER, UtilsConstants.RECORD_FILENAME)
        audioRecorder
    }
    private val sphinxDecoder by lazy { PocketSphinx((activity as MainActivity).speechDirPath) }
    private var mode = SpeechConstants.RecognitionMode.DIRECT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioRecogniserGroup.check(R.id.radioDirect)
        radioRecogniserGroup.setOnCheckedChangeListener { _, id -> onRadioButtonClicked(id) }
        recordFab.setOnClickListener { isRecording = !isRecording }
        recordFab.setOnLongClickListener {
            isRecording = false
            true
        }
        snackbar.show()
    }

    private fun onRadioButtonClicked(id: Int) {
        mode = when (id) {
            R.id.radioDirect -> SpeechConstants.RecognitionMode.DIRECT
            R.id.radioHMM -> SpeechConstants.RecognitionMode.HMM
            R.id.radioSphinx -> SpeechConstants.RecognitionMode.SPHINX
            else -> throw IllegalStateException("Radio selection is out of range")
        }
    }

    private fun getWord() = async(CommonPool) {
        val wordIndex = when (mode) {
            SpeechConstants.RecognitionMode.DIRECT -> getWordIndexDirect((activity as MainActivity).speechDirPath)
            SpeechConstants.RecognitionMode.HMM -> getWordIndexHMM((activity as MainActivity).speechDirPath)
            SpeechConstants.RecognitionMode.SPHINX -> getWordIndexSphinx((activity as MainActivity).speechDirPath)
        }
        if (wordIndex == -1) "###"
        else SpeechConstants.WORDS[wordIndex]
    }

    private external fun getWordIndexDirect(path: String): Int

    private external fun getWordIndexHMM(path: String): Int

    private fun getWordIndexSphinx(path: String): Int = SpeechConstants.WORDS.indexOf(sphinxDecoder.getWord(path))
}