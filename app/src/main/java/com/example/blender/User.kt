package com.example.blender

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
}