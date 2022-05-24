package com.example.blender

import com.example.blender.models.MessageType

data class Message(val id : Int,val senderId : Int, val content : String, val type: MessageType){

}