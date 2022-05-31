package com.example.blender

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.blender.models.Profile

@Database(entities = [Profile::class], version = 1, exportSchema = true)
@TypeConverters(GenderConverter::class, CalendarConverter::class, InterestGenderConverter::class)
abstract class BlenderDatabase : RoomDatabase() {
    // TODO Guillaume
    //abstract fun noteDao(): NoteDao

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