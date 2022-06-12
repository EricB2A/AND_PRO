package com.example.blender.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Conversation (
    @PrimaryKey(autoGenerate = true) var id: Long?,
    /*
        Permet de "customiser" par la suite le nom de la conversation affiché dans la liste de conversations.
        Dans notre cas, il s'agit du nom de l'utilisateur avec lequel on parle.
     */
    var name: String,
    var updatedAt: Calendar,
    var profileId: Long,
    var uuid: String
)

