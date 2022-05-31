package com.example.blender

import com.example.blender.BLE.Utils

data class User(var name: String, var matchWanted: MatchWanted, var gender : MatchWanted.Gender, var age: Int) {
    fun isAMatch(user : User) : Boolean {
        if (
            this.matchWanted.gender.toString() == user.gender.toString()
            && this.gender.toString() == user.matchWanted.gender.toString()
            && user.age in this.matchWanted.minAge..this.matchWanted.maxAge
            && this.age in user.matchWanted.minAge..user.matchWanted.maxAge
        ) {
            return true
        }
        return false
    }

    override fun toString(): String {
        return "Name : $name, age : $age"
    }

    fun toPacket() : ByteArray {
        return Utils.toJsonPacket(this)
    }
}