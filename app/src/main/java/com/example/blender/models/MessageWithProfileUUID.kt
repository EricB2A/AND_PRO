package com.example.blender.models

import java.util.*

data class MessageWithProfileUUID (
    val uuid: UUID,
    val message: Message
        )