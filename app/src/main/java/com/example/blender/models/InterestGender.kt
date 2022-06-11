package com.example.blender.models

enum class InterestGender(val value: Int) {
    MAN(1),
    WOMAN(-1),
    ANY(0);

    fun match(gender: Gender): Boolean {
        if (
            value == MAN.value && gender == Gender.MAN
            || value == WOMAN.value && gender == Gender.WOMAN
            || value == ANY.value
        ) {
            return true
        }

        return false
    }
}