package com.gobbledygook.theawless.speechy

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class TrainFragment : Fragment() {
    inner class TrainRecyclerViewAdapter(private val listItems: Array<String>) : RecyclerView.Adapter<TrainRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_train, parent, false))

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

    fun recording(viewHolder: TrainRecyclerViewAdapter.ViewHolder) {
        isRecording = if (lastViewHolder == viewHolder) !isRecording else true
        if (isRecording) {
            isPlaying = false
            lastViewHolder?.play?.setImageResource(R.drawable.play)
        }
        viewHolder.mic.setImageResource(if (isRecording) R.drawable.mic_off else R.drawable.mic_on)
        if (lastViewHolder != viewHolder) {
            lastViewHolder?.mic?.setImageResource(R.drawable.mic_on)
            lastViewHolder = viewHolder
        }
    }

    fun playing(viewHolder: TrainRecyclerViewAdapter.ViewHolder) {
        isPlaying = if (lastViewHolder == viewHolder) !isPlaying else true
        if (isPlaying) {
            isRecording = false
            lastViewHolder?.mic?.setImageResource(R.drawable.mic_on)
        }
        viewHolder.play.setImageResource(if (isPlaying) R.drawable.stop else R.drawable.play)
        if (lastViewHolder != viewHolder) {
            lastViewHolder?.play?.setImageResource(R.drawable.play)
            lastViewHolder = viewHolder
        }
    }

    private var isRecording = false
    private var isPlaying = false
    private var lastViewHolder: TrainRecyclerViewAdapter.ViewHolder? = null
    private val listItems = arrayOf("a", "e", "i", "o", "u")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_train_list, container, false) as RecyclerView
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = TrainRecyclerViewAdapter(listItems)
        return view
    }
}
