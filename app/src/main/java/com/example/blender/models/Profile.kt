package com.example.blender.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class Profile (
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var pseudo: String,
    var firstname: String,
    var birthdate: Calendar,
    var gender : Gender,
    var interestedIn : InterestGender,
    var mine : Boolean
)