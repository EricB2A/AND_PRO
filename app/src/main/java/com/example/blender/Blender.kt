package com.example.blender

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class Blender : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val repository by lazy {
        val database = BlenderDatabase.getDatabase(this)
        Repository(database.conversationDao(), database.messageDao(), database.profileDao(), applicationScope)
    }

}