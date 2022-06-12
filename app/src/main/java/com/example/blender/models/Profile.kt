package com.example.blender.models

import androidx.room.ColumnInfo
import android.util.Log
import androidx.room.Entity
import androidx.room.Ignore
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
    var mine : Boolean,
    var uuid: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null
) {

    @Ignore
    fun isAMatch(other : Profile) : Boolean {
        return interestedIn.match(other.gender) && other.interestedIn.match(gender)
    }

    @Ignore
    override fun toString(): String {
        return "Name : $firstname, pseudo : $pseudo, uuid: $uuid"
    }

    @Ignore
    fun toRemoteProfile() : Profile {
        return Profile(
            null,
            this.pseudo,
            this.firstname,
            this.birthdate,
            this.gender,
            this.interestedIn,
            false,
            this.uuid
        )
    }
}