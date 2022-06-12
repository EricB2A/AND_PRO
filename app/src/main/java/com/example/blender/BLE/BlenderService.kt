package com.example.blender.BLE


import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService
import android.content.Context
import com.example.blender.*
import com.example.blender.BLE.Utils.Companion.toJsonPacket
import com.example.blender.models.MessageWithProfileUUID
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
        val msg = Utils.fromJsonPacket<MessageWithProfileUUID>(value)!!
        repository.insertReceivedMessage(msg)
        return true
    }

    /**
     * Réception d'un check match
     */
    private fun handleCheckMatch(value: ByteArray): GattStatus {
        return if (checkMatch(value)) GattStatus.SUCCESS else GattStatus.VALUE_NOT_ALLOWED
    }

    /**
     * Check si le profil reçu match avec notre profil
     */
    private fun checkMatch(value: ByteArray): Boolean {
        val remoteUser = Utils.fromJsonPacket<Profile>(value) ?: return false
        if (!currentUser!!.isAMatch(remoteUser)) {
            return false
        }
        runBlocking {
            coroutineScope {
                launch {
                    repository.addRemoteProfile(remoteUser)
                }
            }
        }
        return true
    }

    fun setProfile(current: Profile) {
        currentUser = current
        profile.value = currentUser.toJsonPacket()
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

        service.addCharacteristic(messages)
        messages.addDescriptor(cccDescriptor)
    }
}