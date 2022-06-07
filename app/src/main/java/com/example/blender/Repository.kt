package com.example.blender

import androidx.lifecycle.LiveData
import com.example.blender.dao.ConversationDao
import com.example.blender.dao.MessageDao
import com.example.blender.dao.ProfileDao
import com.example.blender.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.IntStream.range

class Repository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val profileDao: ProfileDao,
    private val scope: CoroutineScope
) {
    val conversations = conversationDao.getConversationWithMessage()

    fun getMyProfile(): LiveData<Profile> {
        return profileDao.getMyProfile();
    }

    fun insertProfile(profile: Profile) {
        scope.launch(Dispatchers.IO) {
            profileDao.insert(profile)
        }
    }

    fun updateProfile(profile: Profile) {
        scope.launch(Dispatchers.IO) {
            profileDao.update(profile);
        }
    }

    fun insert(conversation: Conversation) {
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
        }
    }

    fun reset() {
        scope.launch(Dispatchers.IO) {
            conversationDao.deleteAllConversations()
            messageDao.deleteAllMessages()
        }

    }
}