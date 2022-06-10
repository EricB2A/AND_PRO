package com.example.blender.models

import java.util.*

data class MessageWithProfileUUID (
    val uuid: String,
    val message: Message
        )