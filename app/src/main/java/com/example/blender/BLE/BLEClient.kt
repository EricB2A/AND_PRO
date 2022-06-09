package com.example.blender.BLE

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blender.BLE.Utils.Companion.toJsonPacket
import com.example.blender.Blender
import com.example.blender.Repository
import com.example.blender.models.Message
import com.example.blender.models.Profile
import com.welie.blessed.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class BLEClient {
    private lateinit var central: BluetoothCentralManager
    private lateinit var connectedUsers: Map<String, BluetoothPeripheral>
    private var currentUser : Profile? = null
    private lateinit var repository : Repository

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                if(!central.connectedPeripherals.contains(peripheral)) {
                    Log.d(TAG, peripheral.name)
                    central.connectPeripheral(peripheral, peripheralCallback)
                }
            }
        }

    constructor(context : Context) {
        central = BluetoothCentralManager(
            context,
            bluetoothCentralManagerCallback,
            Handler(Looper.getMainLooper())
        )

        repository = (context.applicationContext as Blender).repository

        repository.getMyProfile().observeForever {
            if (it != null) {
                currentUser = it
                Log.d("test", "success")
            }
        }

        connectedUsers = mapOf()
    }

    private val peripheralCallback: BluetoothPeripheralCallback =
        object : BluetoothPeripheralCallback() {
            override fun onServicesDiscovered(
                peripheral: BluetoothPeripheral
            ) {
                super.onServicesDiscovered(peripheral)
                Log.d(this.javaClass.simpleName, "onServicesDiscovered 1" )
                Log.d(TAG, peripheral.name)
                Log.d(
                    TAG,
                    peripheral.getService(BlenderService.BLENDER_SERVICE_UUID)?.characteristics?.get(0)?.uuid.toString()
                )

                if(currentUser == null) {
                    return
                }

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
                    if (characteristic.uuid == BlenderService.PROFILE_CHARACTERISTIC_UUID) {
                        val remoteProfile = Utils.fromJsonPacket<Profile>(value)
                        if (remoteProfile == null) {
                            Log.d("test", "remote profile null")
                            getRemoteProfile(peripheral)
                            return
                        }
                        Log.d("test", "remote profile not null")
                        Log.d(
                            TAG,
                            remoteProfile.toString()
                        )
                        GlobalScope.launch {
                            repository.addRemoteProfile(remoteProfile)
                        }
                        connectedUsers = connectedUsers + Pair(remoteProfile.pseudo, peripheral)
                        Log.d("test", "connected users : ${connectedUsers.count()}")
                    }
                }
                //central.close();
            }

            /**
             * Callback appelé comme résultat d'une écriture
             */
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
                        // TODO déplacer au besoin après la lecture du profil ?
                        Matcher.getInstance().clientMatch(Pair(peripheral.address, peripheral))
                        getRemoteProfile(peripheral)

                    } else if (status == GattStatus.VALUE_NOT_ALLOWED) {
                        Log.d(TAG, "Too bad! You just missed a match!")
                    }
                }
            }
        }

    private fun getRemoteProfile(peripheral: BluetoothPeripheral) {
        peripheral.readCharacteristic(BlenderService.BLENDER_SERVICE_UUID, BlenderService.PROFILE_CHARACTERISTIC_UUID)
    }

    fun sendMessage(remoteProfileUUID: String, message: Message) {
        connectedUsers[remoteProfileUUID]?.writeCharacteristic(
            BlenderService.BLENDER_SERVICE_UUID,
            BlenderService.MESSAGES_CHARACTERISTIC_UUID,
            message.toJsonPacket(),
            WriteType.WITH_RESPONSE
        )
    }

    fun startScan() {
        Log.d(TAG, "startScan()")
        central.scanForPeripheralsWithServices(arrayOf(BlenderService.BLENDER_SERVICE_UUID))
    }

    fun stopScan() {
        central.stopScan()
        central.connectedPeripherals.find {
            it.address == ""
        }
    }

    companion object {
        private var instance : BLEClient? = null
        val TAG = BLEClient::class.java.simpleName

        @Synchronized
        fun getInstance(context : Context?) : BLEClient {
            if (BLEClient.instance == null && context != null) {
                BLEClient.instance = BLEClient(context.applicationContext)
            }
            return BLEClient.instance!!
        }
    }
}