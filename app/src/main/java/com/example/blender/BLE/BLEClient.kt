package com.example.blender.BLE

import android.bluetooth.BluetoothDevice
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
                if (!peripherals.any { it.address == peripheral.address } && pendingOperation !is Connect && !operationQueue.any { it.peripheral.address == peripheral.address}) {
                    Log.d(TAG, "${central.connectedPeripherals.count()} ${peripheral.address}")
                    Log.d(TAG, "discovered : ${peripheral.address} ${scanResult.device.address}")
                    peripherals.add(peripheral)
                    //central.connectPeripheral(peripheral, peripheralCallback)
                    enqueueOperation(Connect(peripheral, peripheralCallback))
                }
            }

            override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
                super.onConnectedPeripheral(peripheral)
                if (pendingOperation is Connect) {
                    operationDone()
                }
                enqueueOperation(
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
                if (pendingOperation is Connect) {
                    operationDone()
                }
                Log.d(TAG, "connection fail : ${peripheral.address}")
            }

            override fun onDisconnectedPeripheral(
                peripheral: BluetoothPeripheral,
                status: HciStatus
            ) {
                super.onDisconnectedPeripheral(peripheral, status)
                if (pendingOperation is Connect) {
                    operationDone()
                }
                Log.d(TAG, "disconnected : ${peripheral.address}")
            }

            override fun onScanFailed(scanFailure: ScanFailure) {
                super.onScanFailed(scanFailure)
                if (pendingOperation is Connect) {
                    operationDone()
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
                if (pendingOperation is Connect) {
                    operationDone()
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
                    enqueueOperation(Connect(peripheral, this))
                    return
                }

                /*val result = peripheral.writeCharacteristic(
                    BlenderService.BLENDER_SERVICE_UUID,
                    BlenderService.FIND_MATCH_CHARACTERISTIC_UUID,
                    currentUser.toJsonPacket(),
                    WriteType.WITH_RESPONSE
                )
                Log.d(TAG, "Can read characteristic: $result")*/
                enqueueOperation(
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
                Log.d(TAG, "check op type read : ${pendingOperation is CharacteristicRead}")
                if (pendingOperation is CharacteristicRead) {
                    operationDone()
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
                Log.d(TAG, "check op type write : ${pendingOperation is CharacteristicWrite}")
                if (pendingOperation is CharacteristicWrite) {
                    operationDone()
                }

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
        enqueueOperation(
            CharacteristicRead(
                peripheral,
                BlenderService.BLENDER_SERVICE_UUID,
                BlenderService.PROFILE_CHARACTERISTIC_UUID
            )
        )
        //peripheral.readCharacteristic(BlenderService.BLENDER_SERVICE_UUID, BlenderService.PROFILE_CHARACTERISTIC_UUID)
    }

    fun sendMessage(remoteProfileUUID: String, message: Message) {
        enqueueOperation(
            CharacteristicWrite(
                peripherals[0],
                BlenderService.BLENDER_SERVICE_UUID,
                BlenderService.MESSAGES_CHARACTERISTIC_UUID,
                message.toJsonPacket(),
                WriteType.WITH_RESPONSE
            )
        )
        /*connectedUsers[remoteProfileUUID]?.writeCharacteristic(
            BlenderService.BLENDER_SERVICE_UUID,
            BlenderService.MESSAGES_CHARACTERISTIC_UUID,
            message.toJsonPacket(),
            WriteType.WITH_RESPONSE
        )*/
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

    @Synchronized
    private fun enqueueOperation(operation: BLEOperationType) {
        operationQueue.add(operation)
        Log.d(TAG, "new operation added")
        if (pendingOperation == null) {
            Log.d(TAG, "next operation scheduled")
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            return
        }

        val op = operationQueue.poll() ?: run {
            return
        }
        pendingOperation = op

        when (op) {
            is Connect -> Log.d(TAG, "current task : connect")
            is CharacteristicRead -> Log.d(TAG, "current task : read")
            is CharacteristicWrite -> Log.d(TAG, "current task : write")
        }

        when (op) {
            is Connect -> {
                setTimeout(1000)
                central.connectPeripheral(op.peripheral, op.peripheralCallback)
            }
            is CharacteristicRead -> op.peripheral.readCharacteristic(
                op.serviceUUID,
                op.characteristicUUID
            )
            is CharacteristicWrite -> op.peripheral.writeCharacteristic(
                op.serviceUUID,
                op.characteristicUUID,
                op.value,
                op.writeType
            )
            else -> Log.d(TAG, "Operation not found")
        }
    }

    @Synchronized
    private fun setTimeout(delay: Long) {
        timer.schedule(delay) {
            operationTimeout()
        }
    }

    @Synchronized
    private fun resetTimer() {
        timer.cancel()
        timer = Timer()
    }

    @Synchronized
    private fun operationDone() {
        when (pendingOperation) {
            is Connect -> Log.d(TAG, "done task : connect")
            is CharacteristicRead -> Log.d(TAG, "done task : read")
            is CharacteristicWrite -> Log.d(TAG, "done task : write")
        }

        resetTimer()

        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun operationTimeout() {
        enqueueOperation(pendingOperation!!)

        resetTimer()

        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    companion object {
        private var instance: BLEClient? = null
        val TAG = BLEClient::class.java.simpleName
        private val operationQueue = ConcurrentLinkedQueue<BLEOperationType>()
        private var pendingOperation: BLEOperationType? = null
        private var timer: Timer = Timer()
        private val peripherals : MutableList<BluetoothPeripheral> = mutableListOf()

        @Synchronized
        fun getInstance(context: Context?): BLEClient {
            if (BLEClient.instance == null && context != null) {
                BLEClient.instance = BLEClient(context.applicationContext)
            }
            return BLEClient.instance!!
        }
    }
}