package com.example.blender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.models.Conversation
import com.example.blender.models.ConversationMessage
import com.example.blender.models.Message
import com.example.blender.models.MessageType
import com.example.blender.viewmodel.DiscussionViewModel
import com.example.blender.viewmodel.DiscussionViewModelFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    private val discussionViewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory((application as Blender).repository)
    }



    private fun initTestData() {
        Log.d("Mainactivity", "WORKING1")
        val repository = (application as Blender).repository
        Log.d("Mainactivity", "WORKING")
        repository.reset()
        val conversation = Conversation(1, "testPerson", Calendar.getInstance())
        val message1 = Message(null, 1,"content", Calendar.getInstance(), MessageType.RECEIVED)
        val message2 = Message(null, 1,"contentsent", Calendar.getInstance(), MessageType.SENT)
        val messages = listOf(message1, message2)
        repository.insertConversationMessages(conversation, messages)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTestData()
        setContentView(R.layout.activity_main)


        val recycler = findViewById<RecyclerView>(R.id.discussions)
        val adapter = DiscussionRecyclerAdapter()
        recycler.adapter= adapter
        recycler.layoutManager= LinearLayoutManager(this)
        discussionViewModel.allDiscussions.observe(this) { value ->
            adapter.items = value
        }
    }
}
