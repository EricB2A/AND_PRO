package com.example.blender.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.blender.models.Conversation
import com.example.blender.models.ConversationMessage
import java.util.*

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation")
    fun getAll(): LiveData<List<Conversation>>

    @Transaction
    @Query("SELECT * FROM conversation WHERE id = :id")
    fun getById(id: Long): LiveData<ConversationMessage>

    @Insert
    fun insert(conversation: Conversation): Long

    @Update
    fun update(conversation: Conversation)

    @Delete
    fun delete(conversation: Conversation)

    @Transaction
    @Query("SELECT * FROM Conversation")
    fun getConversationWithMessage(): LiveData<List<ConversationMessage>>

    @Query("DELETE FROM Conversation")
    fun deleteAllConversations()

    @Query("UPDATE Conversation SET updatedAt = :newTime WHERE id = :id" )
    fun updateTimeStamp(newTime : Calendar, id: Long)



}