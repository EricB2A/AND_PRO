package com.example.blender

import com.example.blender.dao.ConversationDao
import com.example.blender.dao.MessageDao
import com.example.blender.dao.ProfileDao
import com.example.blender.models.Conversation
import com.example.blender.models.ConversationMessage
import com.example.blender.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.IntStream.range

class Repository(private val conversationDao : ConversationDao,
                 private val messageDao: MessageDao,
                 private val profileDao: ProfileDao,
                 private val scope : CoroutineScope
                 ) {
    val conversations = conversationDao.getConversationWithMessage()

    fun insert(conversation: Conversation) {
        conversationDao.insert(conversation)
    }

    fun insertConversationMessages(conversation: Conversation, messages: List<Message>) {
        scope.launch(Dispatchers.IO) {
            val nid = conversationDao.insert(conversation)
            for(i in range(0, messages.size)) {
                messages[i].convId = nid
                messageDao.insert(messages[i])
            }
        }
    }

    fun reset() {
        scope.launch(Dispatchers.IO) {
            conversationDao.deleteAllConversations()
            messageDao.deleteAllMessages()
        }

    }
}