package com.example.blender.BLE


import com.example.blender.models.Message
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blender.MatchWanted
import com.example.blender.User
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.GattStatus
import java.util.*
import com.example.blender.BLE.Utils.Companion.toJsonPacket
import com.example.blender.models.MessageType


class BlenderService(peripheralManager: BluetoothPeripheralManager) :
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
        PROPERTY_READ or PROPERTY_INDICATE,
        PERMISSION_READ
    )
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val notifyRunnable = Runnable { notifyProfile() }
    override fun onCentralDisconnected(central: BluetoothCentral) {
        if (noCentralsConnected()) {
            stopNotifying()
        }
    }

    private val currentUser: User

    private var messagesUser = mapOf<String, List<Message>>()

    override fun onCharacteristicWrite(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): GattStatus? {
        Log.d(SERVICE_NAME, central.address)
        if (checkMatch(value)) {
            return GattStatus.SUCCESS
        }
        return GattStatus.VALUE_NOT_ALLOWED
    }

    override fun onNotifyingEnabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.uuid == PROFILE_CHARACTERISTIC_UUID) {
            notifyProfile()
        }
    }

    override fun onNotifyingDisabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.uuid == PROFILE_CHARACTERISTIC_UUID) {
            stopNotifying()
        }
    }

    override fun onCharacteristicRead(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicRead(central, characteristic)
        if (characteristic.uuid == MESSAGES_CHARACTERISTIC_UUID) {
            messages.value = messagesUser[central.address].toJsonPacket()
            Log.d(SERVICE_NAME, String(messages.value))
        }
    }

    private fun notifyProfile() {
        //notifyCharacteristicChanged(currentUser.toJsonPacket(), profile)
        //handler.postDelayed(notifyRunnable, 1000)
    }

    private fun notifyMessage() {

    }

    private fun stopNotifying() {
        handler.removeCallbacks(notifyRunnable)
    }

    private fun checkMatch(value: ByteArray): Boolean {
        val remoteUser = Utils.fromJsonPacket<User>(value)
        return currentUser.isAMatch(remoteUser)
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
        service.addCharacteristic(findMatch)
        findMatch.addDescriptor(cccDescriptor)

        service.addCharacteristic(profile)
        profile.addDescriptor(cccDescriptor)

        currentUser = User(
            "Jean",
            MatchWanted(
                MatchWanted.Gender.FEMALE,
                16,
                20,
            ),
            MatchWanted.Gender.MALE,
            25
        )
        profile.value = currentUser.toJsonPacket()

        val list = listOf(
            Message(0, "hello", Calendar.getInstance(), MessageType.SENT),
            Message(1, "hello", Calendar.getInstance(), MessageType.SENT),
            Message(2, "hello", Calendar.getInstance(), MessageType.SENT),
        )

        messagesUser = messagesUser + Pair("", list)

        service.addCharacteristic(messages)
        messages.value = list.toJsonPacket()
    }
}