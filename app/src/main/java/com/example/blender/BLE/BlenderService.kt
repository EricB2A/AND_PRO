package com.example.blender.BLE


import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import com.example.blender.*
import com.example.blender.BLE.Utils.Companion.toJsonPacket
import com.example.blender.models.Message
import com.example.blender.models.MessageType
import com.example.blender.models.Profile
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.GattStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import java.util.*


class BlenderService(peripheralManager: BluetoothPeripheralManager, context: Context) :
    BaseService(peripheralManager) {
    override val service =
        BluetoothGattService(BLENDER_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val findMatch = BluetoothGattCharacteristic(
        FIND_MATCH_CHARACTERISTIC_UUID,
        PROPERTY_WRITE,
        PERMISSION_WRITE
    )
    private val profile = BluetoothGattCharacteristic(
        PROFILE_CHARACTERISTIC_UUID,
        PROPERTY_READ or PROPERTY_INDICATE,
        PERMISSION_READ
    )
    private val messages = BluetoothGattCharacteristic(
        MESSAGES_CHARACTERISTIC_UUID,
        PROPERTY_WRITE,
        PERMISSION_WRITE
    )

    private var currentUser: Profile? = null

    private var repository: Repository

    override fun onCharacteristicWrite(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): GattStatus? {

        Log.d(SERVICE_NAME, central.address)
        /**
         * Traitement des différentes types d'écriture: match ? réception de message ?
         */
        return when (characteristic.uuid) {
            FIND_MATCH_CHARACTERISTIC_UUID -> handleCheckMatch(value)
            MESSAGES_CHARACTERISTIC_UUID -> handleNewMessage(value)
            else -> GattStatus.VALUE_NOT_ALLOWED
        }
    }

    /**
     * Réception d'un nouveau message
     */
    private fun handleNewMessage(value: ByteArray): GattStatus {
        return if (receiveMessage(value)) GattStatus.SUCCESS else GattStatus.VALUE_NOT_ALLOWED
    }

    /**
     * Traitement d'un nouveau message
     */
    private fun receiveMessage(value: ByteArray): Boolean {
        val msg = Utils.fromJsonPacket<Message>(value)!!
        msg.type = MessageType.RECEIVED
        msg.id = null
        repository.insertMessage(msg)
        Log.d("test3", "message : ${msg.convId}, ${msg.content}")
        return true
    }

    /**
     * Réception d'un check match
     */
    private fun handleCheckMatch(value: ByteArray): GattStatus {
        return if (checkMatch(value)) GattStatus.SUCCESS else GattStatus.VALUE_NOT_ALLOWED
    }

    private fun getProfile(): ByteArray {
        Log.d("test2", currentUser.toString())
        return currentUser.toJsonPacket()
    }

    private fun checkMatch(value: ByteArray): Boolean {
        val remoteUser = Utils.fromJsonPacket<Profile>(value) ?: return false
        Log.d("test", "remote not null")
        if (!currentUser!!.isAMatch(remoteUser)) {
            return false
        }
        Log.d("test", "current not null")
        Log.d(SERVICE_NAME, "before blocking")
        runBlocking {
            coroutineScope {
                launch {
                    Log.d(SERVICE_NAME, "before add remote profile")
                    repository.addRemoteProfile(remoteUser)
                    Log.d(SERVICE_NAME, "after add remote profile")
                }
            }
        }
        Log.d(SERVICE_NAME, "after blocking")
        return true
    }

    override val serviceName: String
        get() = SERVICE_NAME

    companion object {
        val BLENDER_SERVICE_UUID: UUID =
            UUID.fromString("badb1111-cafe-f00d-d00d-8a41886b49fb")
        val FIND_MATCH_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("badb1112-cafe-f00d-d00d-8a41886b49fb")
        val PROFILE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("badb1113-cafe-f00d-d00d-8a41886b49fb")
        val MESSAGES_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("badb1114-cafe-f00d-d00d-8a41886b49fb")
        private const val SERVICE_NAME = "Blender service"
    }

    init {
        this.repository = (context.applicationContext as Blender).repository

        service.addCharacteristic(findMatch)
        findMatch.addDescriptor(cccDescriptor)

        service.addCharacteristic(profile)
        profile.addDescriptor(cccDescriptor)

        repository.getMyProfile().observeForever {
            if (it != null) {
                currentUser = it
            }
        }
        profile.value = getProfile()

        service.addCharacteristic(messages)
        messages.addDescriptor(cccDescriptor)
    }
}