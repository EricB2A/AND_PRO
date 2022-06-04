package com.example.blender

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.models.Message
import com.example.blender.models.MessageType
import java.util.*

class ConversationActivity : AppCompatActivity() {
    private val conversationAdapter = ConversationRecyclerAdapter()
    private lateinit var btnSend : Button
    private lateinit var userInput : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        btnSend = findViewById(R.id.button_send)
        userInput = findViewById(R.id.editText_userInput)
        initRecycler()
        btnSend.setOnClickListener{
            if(userInput.text.isEmpty()) return@setOnClickListener

            // TODO send message


            userInput.text.clear()
        }
    }
    private fun initRecycler() {
        findViewById<RecyclerView?>(R.id.recylerView_conversation).apply {
            adapter = conversationAdapter
            layoutManager =
                LinearLayoutManager(this@ConversationActivity).apply { stackFromEnd = true }

        }

        conversationAdapter.items = listOf(
            Message(1,  "Heelo ", Calendar.getInstance(), MessageType.RECEIVED),
            Message(2, "Heelo ", Calendar.getInstance(), MessageType.SENT),
            Message(
                2,
                "Golden brown texture like sun, lays me down, with my mind she runs",
                Calendar.getInstance(),
                MessageType.RECEIVED
            ),
            Message(
                2,
                "What is something interesting we would not know about you just by looking ?",
                Calendar.getInstance(),
                MessageType.SENT
            )
        )
         */
    }
}