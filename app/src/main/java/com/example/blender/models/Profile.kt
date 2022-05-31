package com.example.blender.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class Profile (
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var name: String,
    var birthdate: Calendar,
    var sex : Gender,
    var interestedIn : InterestGender,


)