package com.gobbledygook.theawless.speechy.app

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.gobbledygook.theawless.speechy.R
import com.gobbledygook.theawless.speechy.audio.AudioPlayer
import com.gobbledygook.theawless.speechy.audio.AudioRecorder
import com.gobbledygook.theawless.speechy.utils.SpeechConstants
import com.gobbledygook.theawless.speechy.utils.Utils
import com.gobbledygook.theawless.speechy.utils.UtilsConstants
import kotlinx.android.synthetic.main.fragment_train.*
import java.io.File
import java.io.PrintWriter

class TrainFragment : Fragment(), AudioRecorder.Listener, AudioPlayer.Listener {
    inner class TrainRecyclerViewAdapter(private val listItems: Array<String>) : RecyclerView.Adapter<TrainRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.train_list_item, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = listItems[position]
            holder.mic.setOnClickListener({ _ -> recording(holder) })
            holder.play.setOnClickListener({ _ -> playing(holder) })
        }

        override fun getItemCount(): Int = listItems.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var title: TextView = view.findViewById(R.id.title)
            var mic: ImageButton = view.findViewById(R.id.mic)
            var play: ImageButton = view.findViewById(R.id.play)
        }
    }

    fun recording(viewHolder: TrainRecyclerViewAdapter.ViewHolder?) {
        val isRecording = if (lastViewHolder == viewHolder) !isRecording else true
        if (isRecording) lastViewHolder?.play?.setImageResource(R.drawable.play)
        if (lastViewHolder != viewHolder) {
            lastViewHolder?.mic?.setImageResource(R.drawable.mic_on)
            lastViewHolder = viewHolder
        }
        this.isRecording = isRecording
    }

    fun playing(viewHolder: TrainRecyclerViewAdapter.ViewHolder?) {
        val isPlaying = if (lastViewHolder == viewHolder) !isPlaying else true
        if (isPlaying) lastViewHolder?.mic?.setImageResource(R.drawable.mic_on)
        if (lastViewHolder != viewHolder) {
            lastViewHolder?.play?.setImageResource(R.drawable.play)
            lastViewHolder = viewHolder
        }
        this.isPlaying = isPlaying
    }

    override var isRecording = false
        set (value) {
            field = value
            if (value) {
                isPlaying = false
                audioRecorder.recordPath = getPathForListItem(lastViewHolder)
            }
            lastViewHolder?.mic?.setImageResource(if (isRecording) R.drawable.mic_off else R.drawable.mic_on)
            audioRecorder.isRecording = value
        }

    override var isPlaying = false
        set (value) {
            field = value
            if (value) {
                isRecording = false
                audioPlayer.playPath = getPathForListItem(lastViewHolder)
            }
            lastViewHolder?.play?.setImageResource(if (isPlaying) R.drawable.stop else R.drawable.play)
            audioPlayer.isPlaying = value
        }

    private var lastViewHolder: TrainRecyclerViewAdapter.ViewHolder? = null
    private val audioRecorder by lazy { AudioRecorder(this) }
    private val audioPlayer by lazy { AudioPlayer(this) }

    private fun getPathForListItem(lastViewHolder: TrainRecyclerViewAdapter.ViewHolder?): String {
        val wordIndex = lastViewHolder!!.adapterPosition
        val utteranceIndex = trainIndex.progress
        val filename = SpeechConstants.WORDS[wordIndex] + "_" + utteranceIndex.toString()
        return Utils.combinePaths((activity as MainActivity).speechDirPath, UtilsConstants.RECORD_FOLDER, filename)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_train, container, false)

    private fun createConfig() {
        val configFile = PrintWriter(File(Utils.combinePaths((activity as MainActivity).speechDirPath,
                                                             UtilsConstants.RECORD_FOLDER, "word.config")))
        for (word in SpeechConstants.WORDS) {
            configFile.println(word + ',' + SpeechConstants.N_UTTERANCES.toString())
        }
        configFile.close()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trainList.adapter = TrainRecyclerViewAdapter(SpeechConstants.WORDS)
        trainIndex.max = SpeechConstants.N_UTTERANCES
        trainIndex.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekbar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                trainIndexLabel.text = progress.toString()
            }
        })
        createConfig()
    }
}