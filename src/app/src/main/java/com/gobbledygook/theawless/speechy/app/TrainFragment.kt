package com.gobbledygook.theawless.speechy.app

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import com.gobbledygook.theawless.speechy.R
import com.gobbledygook.theawless.speechy.audio.AudioPlayer
import com.gobbledygook.theawless.speechy.audio.AudioRecorder
import com.gobbledygook.theawless.speechy.utils.SpeechConstants
import com.gobbledygook.theawless.speechy.utils.Utils
import com.gobbledygook.theawless.speechy.utils.UtilsConstants

class TrainFragment : Fragment(), AudioRecorder.Listener, AudioPlayer.Listener {
    inner class TrainRecyclerViewAdapter(private val listItems: Array<String>) : RecyclerView.Adapter<TrainRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val holder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_train, parent, false))
            holder.index.maxValue = SpeechConstants.N_UTTERANCES - 1
            holder.index.minValue = 0
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = listItems[position]
            holder.index.value = 0
            holder.mic.setOnClickListener({ _ -> recording(holder) })
            holder.play.setOnClickListener({ _ -> playing(holder) })
        }

        override fun getItemCount(): Int = listItems.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var title: TextView = view.findViewById(R.id.title)
            var index: NumberPicker = view.findViewById(R.id.index)
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
        val utteranceIndex = lastViewHolder.index.value
        val filename = SpeechConstants.WORDS[wordIndex] + "_" + utteranceIndex.toString()
        return Utils.combinePaths((activity as MainActivity).speechDirPath, UtilsConstants.RECORD_FOLDER, filename)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_train_list, container, false) as RecyclerView
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = TrainRecyclerViewAdapter(SpeechConstants.WORDS)
        return view
    }
}