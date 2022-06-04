package com.example.blender.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import com.example.blender.Message

@Entity
data class ConversationMessage(@Embedded val conversation: Conversation,
                               @Relation( parentColumn = "id",
                                   entityColumn = "id")
val messages: List<Message>
)
