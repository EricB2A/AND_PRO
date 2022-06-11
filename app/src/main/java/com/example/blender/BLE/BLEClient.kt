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
import com.example.blender.models.MessageWithProfileUUID
import com.example.blender.models.Profile
import com.welie.blessed.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.schedule

class BLEClient {
    private lateinit var central: BluetoothCentralManager
    private lateinit var connectedUsers: Map<String, BluetoothPeripheral>
    private var currentUser: Profile? = null
    private lateinit var repository: Repository

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                if (!peripherals.any { it.value.address == peripheral.address }
                    && bleOperationManager.getPendingOperation() !is Connect
                    && !bleOperationManager.contains { it.peripheral.address == peripheral.address }
                ) {
                    Log.d(TAG, "${central.connectedPeripherals.count()} ${peripheral.address}")
                    Log.d(TAG, "discovered : ${peripheral.address} ${scanResult.device.address}")
                    bleOperationManager.enqueueOperation(Connect(peripheral, central, peripheralCallback))
                }
            }

            override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
                super.onConnectedPeripheral(peripheral)
                if (bleOperationManager.getPendingOperation() is Connect) {
                    bleOperationManager.operationDone()
                }
                bleOperationManager.enqueueOperation(
                    CharacteristicRead(
                        peripheral,
                        BlenderService.BLENDER_SERVICE_UUID,
                        BlenderService.PROFILE_CHARACTERISTIC_UUID
                    )
                )
                Log.d(TAG, "connected : ${peripheral.address}")

            }

            override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
                super.onConnectionFailed(peripheral, status)
                if (bleOperationManager.getPendingOperation() is Connect) {
                    bleOperationManager.operationDone()
                }
                Log.d(TAG, "connection fail : ${peripheral.address}")
            }

            override fun onDisconnectedPeripheral(
                peripheral: BluetoothPeripheral,
                status: HciStatus
            ) {
                super.onDisconnectedPeripheral(peripheral, status)
                if (bleOperationManager.getPendingOperation() is Connect) {
                    bleOperationManager.operationDone()
                }
                Log.d(TAG, "disconnected : ${peripheral.address}")
                peripherals.values.removeIf {
                    it.address == peripheral.address && it.state == ConnectionState.DISCONNECTED
                }
            }

            override fun onScanFailed(scanFailure: ScanFailure) {
                super.onScanFailed(scanFailure)
                if (bleOperationManager.getPendingOperation() is Connect) {
                    bleOperationManager.operationDone()
                }
            }
        }

    constructor(context: Context) {
        central = BluetoothCentralManager(
            context,
            bluetoothCentralManagerCallback,
            Handler(Looper.getMainLooper())
        )

        repository = (context.applicationContext as Blender).repository

        repository.getMyProfile().observeForever {
            if (it != null) {
                currentUser = it
                Log.d(TAG, "success")
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
                if (bleOperationManager.getPendingOperation() is Connect) {
                    bleOperationManager.operationDone()
                }
                Log.d(TAG, "onServicesDiscovered 1")
                Log.d(TAG, peripheral.name)
                Log.d(
                    TAG,
                    peripheral.getService(BlenderService.BLENDER_SERVICE_UUID)?.characteristics?.get(
                        0
                    )?.uuid.toString()
                )

                Log.d(TAG, "${currentUser == null}")
                if (currentUser == null) {
                    bleOperationManager.enqueueOperation(Connect(peripheral, central, this))
                    return
                }
                bleOperationManager.enqueueOperation(
                    CharacteristicWrite(
                        peripheral,
                        BlenderService.BLENDER_SERVICE_UUID,
                        BlenderService.FIND_MATCH_CHARACTERISTIC_UUID,
                        currentUser.toJsonPacket(),
                        WriteType.WITH_RESPONSE
                    )
                )
            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status)
                Log.d(TAG, "check op type read : ${bleOperationManager.getPendingOperation() is CharacteristicRead}")
                if (bleOperationManager.getPendingOperation() is CharacteristicRead) {
                    bleOperationManager.operationDone()
                }

                if (status === GattStatus.SUCCESS) {
                    if (characteristic.uuid == BlenderService.PROFILE_CHARACTERISTIC_UUID) {
                        val remoteProfile = Utils.fromJsonPacket<Profile>(value)
                        if (remoteProfile == null) {
                            Log.d(TAG, "remote profile null")
                            //getRemoteProfile(peripheral)
                            return
                        }
                        Log.d("test", "remote profile not null")
                        Log.d(
                            TAG,
                            remoteProfile.toString()
                        )
                        Log.d("***", remoteProfile.uuid)
                        peripherals[remoteProfile.uuid] = peripheral
                        GlobalScope.launch {
                            repository.addRemoteProfile(remoteProfile)
                        }
                        Log.d(TAG, "connected users : ${connectedUsers.count()}")
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
                Log.d(TAG, "check op type write : ${bleOperationManager.getPendingOperation() is CharacteristicWrite}")
                if (bleOperationManager.getPendingOperation() is CharacteristicWrite) {
                    bleOperationManager.operationDone()
                }

                if (characteristic.uuid == BlenderService.FIND_MATCH_CHARACTERISTIC_UUID) {
                    if (status == GattStatus.SUCCESS) {
                        Log.d(TAG, "A new match has been made!")
                        // TODO déplacer au besoin après la lecture du profil ?
                        getRemoteProfile(peripheral)

                    } else if (status == GattStatus.VALUE_NOT_ALLOWED) {
                        Log.d(TAG, "Too bad! You just missed a match!")
                    }
                } else if (characteristic.uuid == BlenderService.MESSAGES_CHARACTERISTIC_UUID) {
                    Log.d("###", peripheral.address)
                }
            }
        }

    private fun getRemoteProfile(peripheral: BluetoothPeripheral) {
        bleOperationManager.enqueueOperation(
            CharacteristicRead(
                peripheral,
                BlenderService.BLENDER_SERVICE_UUID,
                BlenderService.PROFILE_CHARACTERISTIC_UUID
            )
        )
        //peripheral.readCharacteristic(BlenderService.BLENDER_SERVICE_UUID, BlenderService.PROFILE_CHARACTERISTIC_UUID)
    }

    fun sendMessage(remoteProfileUUID: String, message: Message) {
        Log.d("###", remoteProfileUUID)
        if (peripherals[remoteProfileUUID] != null) {
            Log.d("###", peripherals[remoteProfileUUID]!!.state.name)
            bleOperationManager.enqueueOperation(
                Connect(peripherals[remoteProfileUUID]!!, central, peripheralCallback)
            )
            bleOperationManager.enqueueOperation(
                CharacteristicWrite(
                    peripherals[remoteProfileUUID]!!,
                    BlenderService.BLENDER_SERVICE_UUID,
                    BlenderService.MESSAGES_CHARACTERISTIC_UUID,
                    MessageWithProfileUUID(currentUser!!.uuid, message).toJsonPacket(),
                    WriteType.WITH_RESPONSE
                )
            )
        }else{
            Log.d("###", "nop")

        }
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
        private var instance: BLEClient? = null
        val TAG: String = BLEClient::class.java.simpleName
        val bleOperationManager : BLEOperationManager = BLEOperationManager()
        private val peripherals: MutableMap<String, BluetoothPeripheral> = mutableMapOf()

        @Synchronized
        fun getInstance(context: Context?): BLEClient {
            if (instance == null && context != null) {
                instance = BLEClient(context.applicationContext)
            }
            return instance!!
        }
    }
}