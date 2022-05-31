package com.example.blender.BLE

import com.google.gson.Gson
import java.nio.charset.Charset

class Utils {
    companion object {
        fun <T> toJsonPacket(obj : T) : ByteArray {
            val gson = Gson()
            return gson.toJson(obj).toByteArray(Charset.defaultCharset())
        }

        inline fun <reified T> fromJsonPacket(byteArray : ByteArray) : T {
            val gson = Gson()
            return gson.fromJson(String(byteArray), T::class.java)
        }
    }
}