package com.example.blender

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.models.MessageType
import com.example.blender.models.Message

class ConversationActivity : AppCompatActivity() {
    private val conversationAdapter = ConversationRecyclerAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        initRecycler()

    }

    private fun initRecycler() {
        findViewById<RecyclerView?>(R.id.recylerView_conversation).apply {
            adapter = conversationAdapter
            layoutManager =
                LinearLayoutManager(this@ConversationActivity).apply { stackFromEnd = true }

        }

        conversationAdapter.items = listOf<Message>(
            Message(1, 1, "Heelo ", MessageType.RECEIVED),
            Message(2, 1, "Hello", MessageType.SENT),
            Message(
                2,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                2,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                3,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                4,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                5,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                6,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                21,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                22,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                23,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                32,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                42,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                42,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                24,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                25,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                62,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                72,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                82,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
            Message(
                29,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.RECEIVED
            ),
            Message(
                20,
                1,
                "What is something interesting we would not know about you just by looking ?",
                MessageType.SENT
            ),
        )
    }
}