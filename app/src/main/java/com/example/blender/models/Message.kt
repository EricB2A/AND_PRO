package com.example.blender.models

import androidx.room.Entity
import java.util.*
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class Message(
  @PrimaryKey(autoGenerate = true) var id : Long?,
  var convId: Long,
  var content: String,
  var createdAt: Calendar,
  var type : MessageType
){
  override fun equals(other: Any?): Boolean {
    if(other !is Message) return false
    return (type == other.type) && (content == other.content) && (createdAt == other.createdAt)
  }
};