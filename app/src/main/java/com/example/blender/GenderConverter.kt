package com.example.blender

import androidx.room.TypeConverter
import com.example.blender.models.Gender

class GenderConverter {
    @TypeConverter
    fun fromGender(priority: Gender): String {
        return priority.name
    }

    @TypeConverter
    fun toGender(gender : String): Gender {
        return Gender.valueOf(gender)
    }
}