package com.example.blender.models

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationMessage(
    @Embedded val conversation: Conversation,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val messages: List<Message>
)
