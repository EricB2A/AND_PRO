package com.example.blender.models

import androidx.room.Entity
import java.util.*
import androidx.room.PrimaryKey

@Entity
data class Message(
  @PrimaryKey(autoGenerate = true) var id : Long?,
  var content: String,
  var createdAt: Calendar,
  val type : MessageType
);