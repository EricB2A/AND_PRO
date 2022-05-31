package com.example.blender


import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.models.Message
import com.example.blender.models.MessageType


class ConversationRecyclerAdapter(_items: List<Message> = listOf()) :
    RecyclerView.Adapter<ConversationRecyclerAdapter.ViewHolder>() {
    private lateinit var resources: Resources
    var items = listOf<Message>()
        set(value) {
            val diffCallback = ConversationDiffCallback(items, value)
            val diffItems = DiffUtil.calculateDiff(diffCallback)
            field = value
            diffItems.dispatchUpdatesTo(this)
        }

    init {
        items = _items

    }

    override fun getItemCount() = items.size
    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            MessageType.SENT.ordinal -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sent_message, parent, false)
            )
            else -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.received_message, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        resources = holder.itemView.context.resources
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // item with schedule
        private val receivedMessage = view.findViewById<TextView>(R.id.received_message_content)
        private val sentMessage = view.findViewById<TextView>(R.id.sent_message_content)


        fun bind(message: Message) {
            if (message.type == MessageType.RECEIVED) {
                receivedMessage?.text = message.content
            } else {
                sentMessage?.text = message.content
            }
        }
    }
}