package com.example.blender

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
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
        val messages = items[position].messages!!
        if (messages.isEmpty()) {
            return SENT
        }
        val orderedMessages = messages.sortedBy { it.createdAt }
        val lastMessage = orderedMessages.first()
        return if(lastMessage.type == MessageType.RECEIVED) RECEIVED
        else SENT
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val discussionSent = view.findViewById<TextView>(R.id.sent)
        private val discussionReceived = view.findViewById<TextView>(R.id.received)
        private val from = view.findViewById<TextView>(R.id.name)
        fun bind(discussion: ConversationMessage) {
            val messages = discussion.messages!!
            val orderedMessages = messages.sortedBy { it.createdAt }
            if (orderedMessages.isEmpty()) {
                from.text = discussion.conversation.name
                return
            }
            val lastMessage = orderedMessages.first()
            if(lastMessage.type == MessageType.RECEIVED) {
                discussionReceived.text = lastMessage.content
            } else {
                discussionSent.text = lastMessage.content
            }
            from.setOnClickListener{

                val intent = Intent(from.context, ConversationActivity::class.java)
                // TODO "id" refactor
                intent.putExtra("id", discussion.conversation.id)
                startActivity(from.context, intent,null)
            }
            from.text = discussion.conversation.name

        }
    }

    companion object {
        private const val RECEIVED = 1
        private const val SENT = 2
    }
}

