package com.example.blender

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Handler
import android.os.Looper

import com.welie.blessed.BluetoothBytesParser
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager

import java.nio.ByteOrder
import java.util.Calendar
import java.util.UUID


import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ


internal class FindMatchService(peripheralManager: BluetoothPeripheralManager) :
    BaseService(peripheralManager) {
    override val service =
        BluetoothGattService(CTS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val currentTime = BluetoothGattCharacteristic(
        CURRENT_TIME_CHARACTERISTIC_UUID,
        PROPERTY_READ or PROPERTY_INDICATE,
        PERMISSION_READ
    )
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val notifyRunnable = Runnable { notifyCurrentTime() }
    override fun onCentralDisconnected(central: BluetoothCentral) {
        if (noCentralsConnected()) {
            stopNotifying()
        }
    }

    override fun onCharacteristicRead(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        currentTime.value = getCurrentTime()
    }

    override fun onNotifyingEnabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.uuid == CURRENT_TIME_CHARACTERISTIC_UUID) {
            notifyCurrentTime()
        }
    }

    override fun onNotifyingDisabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.uuid == CURRENT_TIME_CHARACTERISTIC_UUID) {
            stopNotifying()
        }
    }

    private fun notifyCurrentTime() {
        notifyCharacteristicChanged(getCurrentTime(), currentTime)
        handler.postDelayed(notifyRunnable, 1000)
    }

    private fun stopNotifying() {
        handler.removeCallbacks(notifyRunnable)
    }

    private fun getCurrentTime(): ByteArray {
        val parser = BluetoothBytesParser(ByteOrder.LITTLE_ENDIAN)
        parser.setCurrentTime(Calendar.getInstance())
        return parser.value
    }

    override val serviceName: String
        get() = "Current Time Service"

    companion object {
        private val CTS_SERVICE_UUID: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
        private val CURRENT_TIME_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb")
    }

    init {
        service.addCharacteristic(currentTime)
        currentTime.addDescriptor(cccDescriptor)
        currentTime.value = getCurrentTime()
    }
}