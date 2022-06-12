package com.example.blender

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.BLE.BLEClient
import com.example.blender.models.Message
import com.example.blender.models.MessageType
import com.example.blender.viewmodel.ConversationViewModel
import com.example.blender.viewmodel.ConversationViewModelFactory
import java.util.*

class ConversationActivity : AppCompatActivity() {
    private val conversationAdapter = ConversationRecyclerAdapter()
    private lateinit var btnSend: Button
    private lateinit var userInput: EditText
    private lateinit var recyclerView : RecyclerView

    private val conversationViewModel: ConversationViewModel by viewModels {
        ConversationViewModelFactory((application as Blender).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        btnSend = findViewById(R.id.button_send)
        userInput = findViewById(R.id.editText_userInput)
        recyclerView = findViewById(R.id.recylerView_conversation)
        initRecycler()


        val repository = (application as Blender).repository


        val convId = intent.extras?.getLong("id")!! // id conversation

        val uuid = intent.extras?.getString("uuid")!! // uuid du profile remote

        conversationViewModel.getConversationMessage(convId)
            .observe(this) { value ->
                conversationAdapter.items = value.messages!!
                recyclerView.scrollToPosition(conversationAdapter.itemCount - 1)
            }


        btnSend.setOnClickListener {
            if (userInput.text.isEmpty()) return@setOnClickListener

            val msg = Message(
                null,
                convId,
                userInput.text.toString(),
                Calendar.getInstance(),
                MessageType.SENT
            )



            BLEClient.getInstance(null).sendMessage(uuid, msg)

            repository.insertMessage(
                msg
            )

            userInput.text.clear()
        }
    }

    private fun initRecycler() {
        findViewById<RecyclerView?>(R.id.recylerView_conversation).apply {
            adapter = conversationAdapter
            layoutManager =
                LinearLayoutManager(this@ConversationActivity).apply { stackFromEnd = true }

        }
    }
}