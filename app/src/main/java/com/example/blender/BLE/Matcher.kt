package com.example.blender.BLE

import android.util.Log
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheral

class Matcher {
    // Match fait par le BLEClient  (Peripheral  = advertiser( = BLEServer))
    lateinit var clientMatch: MutableMap<String, BluetoothPeripheral>

    // Match fait par le BLEServer (central = scanner( = BLEClient))
    lateinit var serverMatch: MutableMap<String, BluetoothCentral>

    private constructor() {
        init()
    }

    private fun init() {
        clientMatch = HashMap()
        serverMatch = HashMap()
    }


    fun clientMatch(pair: Pair<String, BluetoothPeripheral>) {
        match(true, pair)
    }

    fun serverMatch(pair: Pair<String, BluetoothCentral>) {
        match(false, pair)
    }

    @Synchronized
    private fun match(isClient: Boolean, pair: Pair<String, Any>) {
        val map: MutableMap<String, Any>
        val oppositeMap: MutableMap<String, Any>
        if (isClient) {
            map = clientMatch as MutableMap<String, Any>
            oppositeMap = serverMatch as MutableMap<String, Any>
        } else {
            map = serverMatch as MutableMap<String, Any>
            oppositeMap = clientMatch as MutableMap<String, Any>
        }
        /**
         * Ajout du nouveau match dans map si nouveau. S'il existe déjà dans l'opposite c'est que les 2 devices
         * ont matché et donc qu'ils peuvent commencer à converser.
         */
        Log.d(this.javaClass.simpleName, "Matching...")
        if (map[pair.first] == null) {
            Log.d(this.javaClass.simpleName, "Matching... $isClient")
            map[pair.first] = pair.second
            if (oppositeMap[pair.first] != null) {
                Log.d(this.javaClass.simpleName, "Match both side")
                // TODO utiliser viewmodel pour insérer une nouvelle conversation ?
                // TODO faire une notif ?
                // TODO ?
            }
        }
    }



    companion object {
        private var instance: Matcher? = null
        fun getInstance(): Matcher {
            if (instance == null)  // NOT thread safe!
                instance = Matcher()
            return instance!!
        }
    }


}