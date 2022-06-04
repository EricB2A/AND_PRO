package com.example.blender

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.blender.dao.ConversationDao
import com.example.blender.dao.MessageDao
import com.example.blender.dao.ProfileDao
import com.example.blender.models.Conversation
import com.example.blender.models.Message
import com.example.blender.models.Profile

@Database(entities = [Profile::class, Conversation::class, Message::class], version = 1, exportSchema = true)
@TypeConverters(GenderConverter::class, CalendarConverter::class, InterestGenderConverter::class)
abstract class BlenderDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun profileDao(): ProfileDao

    companion object {
        private val DATABASE_NAME_FILE = "blender.db"
        private var INSTANCE: BlenderDatabase? = null
        fun getDatabase(context: Context): BlenderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    BlenderDatabase::class.java, DATABASE_NAME_FILE
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE!!
            }
        }
    }
}