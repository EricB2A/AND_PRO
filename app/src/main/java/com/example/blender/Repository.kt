package com.example.blender

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.blender.dao.ConversationDao
import com.example.blender.dao.MessageDao
import com.example.blender.dao.ProfileDao
import com.example.blender.models.*
import kotlinx.coroutines.*
import java.util.*
import java.util.stream.IntStream.range

class Repository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val profileDao: ProfileDao,
    private val scope: CoroutineScope
) {
    fun getAllConversationWithMessage() : LiveData<List<ConversationMessage>> {
        return conversationDao.getConversationWithMessage()
    }

    fun getMyProfile(): LiveData<Profile?> {
        return profileDao.getMyProfile()
    }

    private suspend fun getProfileByUUID(uuid: String): Profile? {
        return profileDao.getByUUID(uuid)
    }

    suspend fun addRemoteProfile(remoteProfile: Profile) {
        val p = getProfileByUUID(remoteProfile.uuid)
        if (p == null) {
            Log.d("test", "null")
            val newP = remoteProfile.toRemoteProfile()
            scope.launch {
                val id = profileDao.insert(newP)
                insertConversation(Conversation(null, remoteProfile.pseudo, Calendar.getInstance(), id, remoteProfile.uuid))
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

    fun insertProfile(profile: Profile): Long {
        var id : Long = 0
        scope.launch(Dispatchers.IO) {
            id = profileDao.insert(profile)
        }
        return id
    }

    fun updateProfile(profile: Profile) {
        scope.launch(Dispatchers.IO) {
            profileDao.update(profile)
        }
    }

    fun insertConversation(conversation: Conversation) {
        conversationDao.insert(conversation)
    }

    fun getConversationMessage(id: Long): LiveData<ConversationMessage> {
        return conversationDao.getById(id)
    }

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

    fun insertMessage(message: Message) {
        scope.launch(Dispatchers.IO) {
            messageDao.insert(message)
            conversationDao.updateTimeStamp(Calendar.getInstance(), message.convId)
        }
    }

    fun insertReceivedMessage(messageWithUUID: MessageWithProfileUUID) {
        scope.launch(Dispatchers.IO) {
            val uuid = messageWithUUID.uuid
            val message = messageWithUUID.message
            Log.d("TEST MESSAGES", profileDao.getConvIdFromUUID(uuid).toString())
            Log.d("TEST MESSAGES", profileDao.getConvIdFromUUID(uuid)?.conversation?.id.toString())
            message.type = MessageType.RECEIVED
            message.id = null
            message.convId = profileDao.getConvIdFromUUID(uuid)?.conversation?.id!!
            messageDao.insert(message)
            conversationDao.updateTimeStamp(Calendar.getInstance(), message.convId)
        }
    }

    fun reset() {
        scope.launch(Dispatchers.IO) {
            conversationDao.deleteAllConversations()
            messageDao.deleteAllMessages()
            profileDao.deleteAll()
        }

    }
}