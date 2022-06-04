package com.example.blender

import androidx.room.TypeConverter
import com.example.blender.models.InterestGender

class InterestGenderConverter {
    @TypeConverter
    fun fromInterestGender(interestGender: InterestGender): String {
        return interestGender.name
    }

    @TypeConverter
    fun toInterestGender(interestGender : String): InterestGender {
        return InterestGender.valueOf(interestGender)
    }
}