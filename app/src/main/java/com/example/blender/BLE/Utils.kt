package com.example.blender.BLE

import com.google.gson.Gson
import java.nio.charset.Charset

class Utils {

    companion object {
        /**
         * Function used to transform an object to be send as ByteArray
         */
        fun <T> T.toJsonPacket() : ByteArray {
            val gson = Gson()
            return gson.toJson(this).toByteArray(Charset.defaultCharset())
        }

        /**
         * Function used to read an object from a ByteArray
         */
        inline fun <reified T> fromJsonPacket(byteArray: ByteArray) : T? {
            val gson = Gson()
            return gson.fromJson(String(byteArray, Charset.defaultCharset()), T::class.java)
        }
    }
}