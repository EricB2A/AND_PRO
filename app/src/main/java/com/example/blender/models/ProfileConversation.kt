package com.example.blender.models

import androidx.room.Embedded
import androidx.room.Relation

data class ProfileConversation(
    @Embedded val profile: Profile,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val conversation: Conversation?
)