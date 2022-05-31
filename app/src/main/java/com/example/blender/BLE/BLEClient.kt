package com.example.blender.BLE

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blender.BLE.Utils.Companion.toJsonPacket
import com.example.blender.MainActivity
import com.example.blender.MatchWanted
import com.example.blender.User
import com.google.gson.Gson
import com.welie.blessed.*
import java.nio.charset.Charset

class BLEClient {
    private lateinit var central: BluetoothCentralManager
    private lateinit var connectedUsers: Map<String, BluetoothPeripheral>

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                //Log.d(TAG, peripheral.name)
                central.connectPeripheral(peripheral, peripheralCallback)
            }
        }

    constructor(context : Context) {
        central = BluetoothCentralManager(
            context,
            bluetoothCentralManagerCallback,
            Handler(Looper.getMainLooper())
        )

        connectedUsers = mapOf()
    }

    private val peripheralCallback: BluetoothPeripheralCallback =
        object : BluetoothPeripheralCallback() {
            override fun onServicesDiscovered(
                peripheral: BluetoothPeripheral
            ) {
                super.onServicesDiscovered(peripheral)
                Log.d(TAG, peripheral.name)
                Log.d(
                    TAG,
                    peripheral.getService(BlenderService.BLENDER_SERVICE_UUID)?.characteristics?.get(0)?.uuid.toString()
                )
                if(connectedUsers.containsKey(peripheral.address)) {
                    return
                }

                connectedUsers = connectedUsers + Pair(peripheral.address, peripheral)
                val currentUser = User(
                    "Janne",
                    MatchWanted(
                        MatchWanted.Gender.MALE,
                        20,
                        30,
                    ),
                    MatchWanted.Gender.FEMALE,
                    18
                )
                val result = peripheral.writeCharacteristic(
                    BlenderService.BLENDER_SERVICE_UUID,
                    BlenderService.FIND_MATCH_CHARACTERISTIC_UUID,
                    currentUser.toJsonPacket(),
                    WriteType.WITH_RESPONSE
                )
                Log.d(TAG, "Can read characteristic: $result")
            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status)
                if (status === GattStatus.SUCCESS) {
                    Log.d(
                        TAG,
                        Utils.fromJsonPacket<User>(value).toString()
                    )
                }
                central.close();
            }

            override fun onCharacteristicWrite(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicWrite(peripheral, value, characteristic, status)
                if (characteristic.uuid == BlenderService.FIND_MATCH_CHARACTERISTIC_UUID) {
                    if (status == GattStatus.SUCCESS) {
                        Log.d(TAG, "A new match has been made!")
                        val profileCharacteristic = peripheral.getCharacteristic(BlenderService.BLENDER_SERVICE_UUID, BlenderService.PROFILE_CHARACTERISTIC_UUID)
                        if(profileCharacteristic != null) {
                            peripheral.readCharacteristic(profileCharacteristic)
                        }

                    } else if (status == GattStatus.VALUE_NOT_ALLOWED) {
                        Log.d(TAG, "Too bad! You just missed a match!")
                    }
                }
            }
        }

    fun startScan() {
        central.scanForPeripheralsWithServices(arrayOf(BlenderService.BLENDER_SERVICE_UUID))
    }

    fun stopScan() {
        central.stopScan()
        central.connectedPeripherals.find {
            it.address == ""
        }
    }

    companion object {
        val TAG = BLEClient::class.java.simpleName
    }
}