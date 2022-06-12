package com.example.blender

import androidx.lifecycle.LiveData
import com.example.blender.dao.ConversationDao
import com.example.blender.dao.MessageDao
import com.example.blender.dao.ProfileDao
import com.example.blender.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.IntStream.range

/**
 * Repository contenant les DAOs pour les accès à la db
 */
class Repository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val profileDao: ProfileDao,
    private val scope: CoroutineScope
) {
    /**
     * Retourne toutes les conversations et leurs messages
     */
    fun getAllConversationWithMessage(): LiveData<List<ConversationMessage>> {
        return conversationDao.getConversationWithMessage()
    }

    /**
     * Retourne le profile de l'utilsiateur de l'application
     */
    fun getMyProfile(): LiveData<Profile?> {
        return profileDao.getMyProfile()
    }

    /**
     * Retourne le profile de l'utilisateur  (version coroutine)
     */
    private suspend fun getProfileByUUID(uuid: String): Profile? {
        return profileDao.getByUUID(uuid)
    }

    /**
     * Ajoute un profile d'un utilisateur distant (version coroutine)
     */
    suspend fun addRemoteProfile(remoteProfile: Profile) {
        val p = getProfileByUUID(remoteProfile.uuid)
        if (p == null) {
            val newP = remoteProfile.toRemoteProfile()
            scope.launch {
                val id = profileDao.insert(newP)
                insertConversation(
                    Conversation(
                        null,
                        remoteProfile.pseudo,
                        Calendar.getInstance(),
                        id,
                        remoteProfile.uuid
                    )
                )
            }
        } else {
            scope.launch {
                profileDao.updateByUUID(
                    remoteProfile.pseudo,
                    remoteProfile.firstname,
                    remoteProfile.birthdate,
                    remoteProfile.gender,
                    remoteProfile.interestedIn,
                    remoteProfile.uuid
                )
            }
        }
    }

    /**
     * Insère un profile et retourne l'id de l'élément inséré
     */
    fun insertProfile(profile: Profile): Long {
        var id: Long = 0
        scope.launch(Dispatchers.IO) {
            id = profileDao.insert(profile)
        }
        return id
    }

    /**
     * Met à jour le profile dans la db
     */
    fun updateProfile(profile: Profile) {
        scope.launch(Dispatchers.IO) {
            profileDao.update(profile)
        }
    }

    /**
     * Insère la conversation dans la db
     */
    fun insertConversation(conversation: Conversation) {
        conversationDao.insert(conversation)
    }

    /**
     * Recupère la conversation et ses messages depuis la db en fonction de l'id donné
     */
    fun getConversationMessage(id: Long): LiveData<ConversationMessage> {
        return conversationDao.getById(id)
    }

    /**
     * Insère la conversation et ses messages dans la db
     */
    fun insertConversationMessages(conversation: Conversation, messages: List<Message>?) {
        scope.launch(Dispatchers.IO) {
            val nid = conversationDao.insert(conversation)
            if (messages != null) {
                for (i in range(0, messages.size)) {
                    messages[i].convId = nid
                    messageDao.insert(messages[i])
                }
            }
        }
    }

    /**
     * Insère un nouveau message dans la db
     */
    fun insertMessage(message: Message) {
        scope.launch(Dispatchers.IO) {
            messageDao.insert(message)
            conversationDao.updateTimeStamp(Calendar.getInstance(), message.convId)
        }
    }

    /**
     * Insère un message dans la db. Cette fonction est utilisé pour insérer les messages
     * depuis BLE, les messages insérés proviennet donc des autres utilisateurs
     */
    fun insertReceivedMessage(messageWithUUID: MessageWithProfileUUID) {
        scope.launch(Dispatchers.IO) {
            val uuid = messageWithUUID.uuid
            val message = messageWithUUID.message
            message.type = MessageType.RECEIVED
            message.id = null
            message.convId = profileDao.getConvIdFromUUID(uuid)?.conversation?.id!!
            messageDao.insert(message)
            conversationDao.updateTimeStamp(Calendar.getInstance(), message.convId)
        }
    }

    /**
     * Permet de reset la db
     */
    fun reset() {
        scope.launch(Dispatchers.IO) {
            conversationDao.deleteAllConversations()
            messageDao.deleteAllMessages()
            profileDao.deleteAll()
        }

    }

    /**
     * Retourne le nobmre de message reçu pour une conversation donnée
     * ( est utilisé pour afficher le profile de l'utilsiateur de manière progressive)
     */
    fun getNbReceivedMessage(convId: Long): LiveData<Int> {
        return messageDao.getByConvId(convId)
    }

    /**
     * Retourne le profile en fonction de l'UUID de l'utilisateur fourni en paramètre
     */
    fun getLiveProfileByUUID(uuid: String): LiveData<Profile> {
        return profileDao.getLiveByUUID(uuid)
    }
}