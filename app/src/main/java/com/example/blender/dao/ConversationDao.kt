package com.example.blender.dao

import androidx.room.*
import com.example.blender.models.Conversation

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation")
    fun getAll(): List<Conversation>

    @Query("SELECT * FROM conversation WHERE id = :id")
    fun getById(id: Int): Conversation

    @Insert
    fun insert(conversation: Conversation)

    @Update
    fun update(conversation: Conversation)

    @Delete
    fun delete(conversation: Conversation)
}