package com.example.blender.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class ProfileConversation(
    @Embedded val profile: Profile,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val conversation: Conversation?
)