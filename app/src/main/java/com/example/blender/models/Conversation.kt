package com.example.blender.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Conversation (
    @PrimaryKey(autoGenerate = true) var id: Long,
    var name: String,
    var updatedAt: Date,
)

