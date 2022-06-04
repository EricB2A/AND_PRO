package com.example.blender

import com.example.blender.dao.ConversationDao
import com.example.blender.dao.MessageDao
import com.example.blender.dao.ProfileDao
import com.example.blender.models.Conversation
import kotlinx.coroutines.CoroutineScope

class Repository(private val conversationDao : ConversationDao,
                 private val messageDao: MessageDao,
                 private val profileDao: ProfileDao,
                 private val scope : CoroutineScope
                 ) {
    val conversations = conversationDao.getAll()

    fun insert(conversation: Conversation) {
        conversationDao.insert(conversation)
    }
}