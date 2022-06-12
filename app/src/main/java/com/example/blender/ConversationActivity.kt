package com.example.blender

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
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

/**
 * Activité contenant la conversation entre les 2 usagers de l'app. Elle contient la liste des messages
 * Des informations sur le profil de l'utilisateur distant et un forumlaire pour envoyer un nouveau message
 */
class ConversationActivity : AppCompatActivity() {
    private val conversationAdapter = ConversationRecyclerAdapter()
    private lateinit var btnSend: Button
    private lateinit var userInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var profile: LiveData<Profile>

    private lateinit var txtAge: TextView

    private lateinit var txtPseudoActionBar: TextView
    private lateinit var imgBtnActionBar: ImageButton

    private lateinit var previewProfilePicture: ImageView


    private val conversationViewModel: ConversationViewModel by viewModels {
        ConversationViewModelFactory((application as Blender).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        btnSend = findViewById(R.id.button_send)
        userInput = findViewById(R.id.editText_userInput)
        recyclerView = findViewById(R.id.recylerView_conversation)
        txtAge = findViewById(R.id.profil_age)
        previewProfilePicture = findViewById(R.id.preview_profile_picture)
        previewProfilePicture.setOnClickListener{
            previewProfilePicture.visibility = GONE
        }

        initRecycler()
        initActionBar()

        val repository = (application as Blender).repository
        val convId = intent.extras?.getLong(EXTRA_CONVERSATION_ID)!! // id conversation
        val uuid = intent.extras?.getString(EXTRA_REMOTE_USER_UUID)!! // uuid du profile remote

        profile = repository.getLiveProfileByUUID(uuid)

        var nbMessage = 0
        /**
         * On observe le changement de profile afin de pouvoir l'afficher en conséquence
         * (avec le nombre de message reçu)
         */
        profile.observe(this) {
            // Ici aurait dû être l'affichage de la photo de profile
            txtPseudoActionBar.text = it.pseudo
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
     * Initialisation de l'action bar
     */
    private fun initActionBar() {
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.title = ""
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = inflater.inflate(R.layout.actionbar_conversation, null)

        supportActionBar?.customView = v
        txtPseudoActionBar = v.findViewById(R.id.profile_other_name)
        imgBtnActionBar = v.findViewById(R.id.profile_picture)

        // Affichage de l'image en grand au clique
        imgBtnActionBar.setOnClickListener {
            previewProfilePicture.visibility = VISIBLE
        }
    }

    /**
     * Dévoile le profil de l'utilisateur en fonction du nombre de message reçu
     */
    @SuppressLint("SetTextI18n")
    private fun revealProfile(it: Int, profile: Profile) {

        if (it > LIMIT_REVEAL_PROFILE_AGE) {
            txtAge.text = getString(R.string.conversation_age, Calendar.getInstance().get(Calendar.YEAR) - profile.birthdate.get(
                Calendar.YEAR
            ))
        }
        if (it > LIMIT_REVEAL_PROFILE_FIRSTNAME) {
            txtPseudoActionBar.text = "${profile.pseudo} ( ${profile.firstname} )"

        }
    }

    private fun initRecycler() {
        findViewById<RecyclerView?>(R.id.recylerView_conversation).apply {
            adapter = conversationAdapter
            layoutManager =
                LinearLayoutManager(this@ConversationActivity).apply { stackFromEnd = true }

        }
    }

    companion object {
        const val LIMIT_REVEAL_PROFILE_AGE = 2
        const val LIMIT_REVEAL_PROFILE_FIRSTNAME = 3
        const val EXTRA_CONVERSATION_ID = "id"
        const val EXTRA_REMOTE_USER_UUID = "uuid"
    }
}