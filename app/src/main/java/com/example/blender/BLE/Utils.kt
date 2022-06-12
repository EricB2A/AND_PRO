package com.example.blender.BLE

import com.google.gson.Gson
import java.nio.charset.Charset

class Utils {

    companion object {
        fun <T> T.toJsonPacket() : ByteArray {
            val gson = Gson()
            return gson.toJson(this).toByteArray(Charset.defaultCharset())
        }

        inline fun <reified T> fromJsonPacket(byteArray: ByteArray) : T? {
            val gson = Gson()
            return gson.fromJson(String(byteArray, Charset.defaultCharset()), T::class.java)
        }
    }
}