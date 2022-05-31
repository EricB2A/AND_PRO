package com.example.blender.models

import java.util.*
import androidx.room.PrimaryKey
import java.sql.Timestamp

data class Message(
  @PrimaryKey(autoGenerate=true) var id : Long?,
  var content: String,
  var createdAt: Date,
  val type : MessageType
){
  override fun equals(other: Any?): Boolean {
    if(other !is Message) return false
    return (type == other.type) && (content == other.content) && (createdAt == other.createdAt)
  }
};