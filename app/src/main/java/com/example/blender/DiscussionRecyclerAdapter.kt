package com.example.blender

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.models.ConversationMessage
import com.example.blender.models.MessageType


class DiscussionRecyclerAdapter (_items : List<ConversationMessage> = listOf()) : RecyclerView.Adapter<DiscussionRecyclerAdapter.ViewHolder>()
{
    var items = listOf<ConversationMessage>()
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
            SENT -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.discussion_sent, parent, false))
            else -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.discussion_none, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], holder.itemViewType)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val messages = items[position].messages!!
        if (messages.isEmpty()) {
            return NONE
        }
        val orderedMessages = messages.sortedBy { it.createdAt }
        val lastMessage = orderedMessages.last()
        return if(lastMessage.type == MessageType.RECEIVED) RECEIVED
        else SENT
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val discussionSent = view.findViewById<TextView>(R.id.sent)
        private val discussionReceived = view.findViewById<TextView>(R.id.received)
        private val from = view.findViewById<TextView>(R.id.name)
        fun bind(discussion: ConversationMessage, viewType: Int) {
            view.setOnClickListener{
                Log.d("TOUCH", "Touched")
                val intent = Intent(from.context, ConversationActivity::class.java)
                // TODO "id" refactor
                intent.putExtra("id", discussion.conversation.id)
                intent.putExtra("uuid", discussion.conversation.uuid)
                startActivity(from.context, intent,null)
            }
            from.text = discussion.conversation.name
            if (viewType != NONE) {
                val messages = discussion.messages!!
                val orderedMessages = messages.sortedBy { it.createdAt }
                val lastMessage = orderedMessages.last()
                if(viewType == RECEIVED) {
                    discussionReceived.text = lastMessage.content
                } else {
                    discussionSent.text = lastMessage.content
                }
            }
        }
    }

    companion object {
        private const val RECEIVED = 1
        private const val SENT = 2
        private const val NONE = 3
    }
}

