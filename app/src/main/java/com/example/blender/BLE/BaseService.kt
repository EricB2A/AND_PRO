package com.example.blender.BLE

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.GattStatus
import java.nio.charset.StandardCharsets
import java.util.*


open class BaseService(peripheralManager: BluetoothPeripheralManager) : Service {
    private val peripheralManager: BluetoothPeripheralManager = Objects.requireNonNull(peripheralManager)
    val cccDescriptor: BluetoothGattDescriptor
        get() {
            val cccDescriptor = BluetoothGattDescriptor(
                CCC_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
            )
            cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            return cccDescriptor
        }

    fun getCudDescriptor(defaultValue: String): BluetoothGattDescriptor {
        Objects.requireNonNull(defaultValue, "CUD value is null")
        val cudDescriptor = BluetoothGattDescriptor(
            CUD_DESCRIPTOR_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        cudDescriptor.value = defaultValue.toByteArray(StandardCharsets.UTF_8)
        return cudDescriptor
    }

    protected fun notifyCharacteristicChanged(
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic
    ) {
        peripheralManager.notifyCharacteristicChanged(value, characteristic)
    }

    fun noCentralsConnected(): Boolean {
        return peripheralManager.connectedCentrals.size == 0
    }

    override val service: BluetoothGattService?
        get() = null
    override val serviceName: String
        get() = ""

    override fun onCharacteristicRead(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
    }

    override fun onCharacteristicWrite(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): GattStatus? {
        return GattStatus.SUCCESS
    }

    override fun onDescriptorRead(central: BluetoothCentral, descriptor: BluetoothGattDescriptor) {}
    override fun onDescriptorWrite(
        central: BluetoothCentral,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ): GattStatus? {
        return GattStatus.SUCCESS
    }

    override fun onNotifyingEnabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
    }

    override fun onNotifyingDisabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
    }

    override fun onNotificationSent(
        central: BluetoothCentral,
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
    }

    override fun onCentralConnected(central: BluetoothCentral) {}
    override fun onCentralDisconnected(central: BluetoothCentral) {}

    companion object {
        val CUD_DESCRIPTOR_UUID: UUID = UUID.fromString("badb2901-cafe-f00d-d00d-8a41886b49fb")
        val CCC_DESCRIPTOR_UUID: UUID = UUID.fromString("badb2902-cafe-f00d-d00d-8a41886b49fb")
    }

}