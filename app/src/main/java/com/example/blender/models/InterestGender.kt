package com.example.blender.models

import android.util.Log

enum class InterestGender(val value: Int) {
    MAN(1),
    WOMAN(-1),
    ANY(0);

    fun match(otherGender: Gender): Boolean {
        return (
            (value == MAN.value && otherGender == Gender.MAN)
            || (value == WOMAN.value && otherGender == Gender.WOMAN)
            || value == ANY.value
        )
    }
}