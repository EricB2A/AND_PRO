package com.example.blender

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class DiscussionRecyclerAdapter (_items : List<Discussion> = listOf()) : RecyclerView.Adapter<DiscussionRecyclerAdapter.ViewHolder>()
{
    var items = listOf<Discussion>()
        set(value) {
            val diffCallback = DiscussionDiffCallback(items, value)
            val diffItems = DiffUtil.calculateDiff(diffCallback)
            field = value
            diffItems.dispatchUpdatesTo(this)

        }

    init {
        items = _items
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            RECEIVED -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.discussion_received, parent, false))
            else -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.discussion_sent, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        Log.d(this::class.simpleName, "HELLO")
        return if(items[position].received) RECEIVED
        else SENT
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val discussionSent = view.findViewById<TextView>(R.id.sent)
        private val discussionReceived = view.findViewById<TextView>(R.id.received)
        private val from = view.findViewById<TextView>(R.id.name)
        fun bind(discussion: Discussion) {
            if(discussion.received) {
                discussionReceived.text = discussion.content
            } else {
                discussionSent.text = discussion.content
            }
            from.text = discussion.from

        }
    }

    companion object {
        private const val RECEIVED = 1
        private const val SENT = 2
    }
}

