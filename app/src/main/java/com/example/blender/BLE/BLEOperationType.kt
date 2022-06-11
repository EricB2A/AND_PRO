package com.example.blender.BLE

import com.welie.blessed.*
import java.util.*

sealed class BLEOperationType {
    abstract val peripheral: BluetoothPeripheral
}

data class Connect(
    override val peripheral: BluetoothPeripheral,
    val central: BluetoothCentralManager,
    val peripheralCallback: BluetoothPeripheralCallback
) : BLEOperationType()

data class CharacteristicRead(
    override val peripheral: BluetoothPeripheral,
    val serviceUUID: UUID,
    val characteristicUUID: UUID
) : BLEOperationType()

data class CharacteristicWrite(
    override val peripheral: BluetoothPeripheral,
    val serviceUUID: UUID,
    val characteristicUUID: UUID,
    val value: ByteArray,
    val writeType: WriteType
) : BLEOperationType() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharacteristicWrite

        if (peripheral != other.peripheral) return false
        if (serviceUUID != other.serviceUUID) return false
        if (characteristicUUID != other.characteristicUUID) return false
        if (writeType != other.writeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = peripheral.hashCode()
        result = 31 * result + serviceUUID.hashCode()
        result = 31 * result + characteristicUUID.hashCode()
        result = 31 * result + writeType.hashCode()
        return result
    }

}