package com.example.blender

data class MatchWanted(var gender: Gender, var minAge: Int, var maxAge: Int) {

    enum class Gender(private val stringValue: String, private val intValue: Int) {
        MALE("Male", 0),
        FEMALE("Female", 1),
        OTHER("Other", 2);

        override fun toString(): String {
            return stringValue
        }
    }

}