package com.example.blender

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.BLE.BLEClient
import com.example.blender.models.Message
import com.example.blender.models.MessageType
import com.example.blender.models.Profile
import com.example.blender.viewmodel.ConversationViewModel
import com.example.blender.viewmodel.ConversationViewModelFactory
import java.util.*

class ConversationActivity : AppCompatActivity() {
    private val conversationAdapter = ConversationRecyclerAdapter()
    private lateinit var btnSend: Button
    private lateinit var userInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var profile: LiveData<Profile>

    private lateinit var txtPseudo : TextView
    private lateinit var txtFirstname : TextView
    private lateinit var txtAge : TextView


    private val conversationViewModel: ConversationViewModel by viewModels {
        ConversationViewModelFactory((application as Blender).repository)
    }

    @SuppressLint("SetTextI18n")  // TODO traduire ? mettre dans String.xml ?
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        btnSend = findViewById(R.id.button_send)
        userInput = findViewById(R.id.editText_userInput)
        recyclerView = findViewById(R.id.recylerView_conversation)
        txtPseudo = findViewById(R.id.profil_pseudo)
        txtFirstname = findViewById(R.id.profil_firstname)
        txtAge = findViewById(R.id.profil_age)
        var nbMessage = 0
        initRecycler()


        val repository = (application as Blender).repository

        val convId = intent.extras?.getLong("id")!! // id conversation
        val uuid = intent.extras?.getString("uuid")!! // uuid du profile remote

        profile = repository.getLiveProfileByUUID(uuid)

        /**
         * On observe le changement de profile afin de pouvoir l'afficher en conséquence
         * (avec le nombre de message reçu)
         */
        profile.observe(this) {
            // TODO traduire ? mettre dans String.xml ?
            txtPseudo.text = "Pseudo : ${it.pseudo}"
            revealProfile(nbMessage, it)
        }

        /**
         * Lorsqu'on marque le nombre de message reçus et on met à jour le profile affiché
         */
        conversationViewModel.getNbReceivedMessage(convId).observe(this) {
            nbMessage = it
            if (profile.value != null) {
                revealProfile(it, profile.value!!)
            }
        }

        /**
         * Met à jour la liste des messages de conversation
         */
        conversationViewModel.getConversationMessage(convId)
            .observe(this) { value ->
                conversationAdapter.items = value.messages!!
                recyclerView.scrollToPosition(conversationAdapter.itemCount - 1)
            }


        /**
         * Lors du clique de l'envoie du message
         */
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

    /**
     * Dévoile le profil de l'utilisateur en fonction du nombre de message reçu
     */
    @SuppressLint("SetTextI18n")
    private fun revealProfile(it : Int, profile: Profile){

        if (it > LIMIT_REVEAL_PROFILE_AGE) {
            // TODO traduire ? mettre dans String.xml ?
            txtAge.text = "Age: ${
                Calendar.getInstance().get(Calendar.YEAR) - profile.birthdate.get(
                    Calendar.YEAR
                )
            }"
        }
        if (it > LIMIT_REVEAL_PROFILE_FIRSTNAME) {
            // TODO traduire ? mettre dans String.xml ?
            txtFirstname.text = "Prénom: ${profile.firstname}"

        }
    }
    private fun initRecycler() {
        findViewById<RecyclerView?>(R.id.recylerView_conversation).apply {
            adapter = conversationAdapter
            layoutManager =
                LinearLayoutManager(this@ConversationActivity).apply { stackFromEnd = true }

        }
    }
    companion object{
        const val LIMIT_REVEAL_PROFILE_AGE = 2
        const val LIMIT_REVEAL_PROFILE_FIRSTNAME = 3
    }
}