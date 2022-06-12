package com.example.blender.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.blender.models.Message

@Dao
interface MessageDao {

    @Query("SELECT * FROM message")
    fun getAll(): LiveData<List<Message>>

    @Query("SELECT * FROM message WHERE id = :id")
    fun getById(id: Int): Message

    @Insert
    fun insertAll(messages: List<Message>)

    @Delete
    fun delete(message: Message)

    @Query("DELETE FROM Message")
    fun deleteAllMessages()

    @Insert
    fun insert(message: Message)

    @Query("SELECT COUNT(*) FROM message WHERE convId = :convId AND type= 'RECEIVED'")
    fun getByConvId(convId: Long) : LiveData<Int>

}