package com.example.blender


import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattService
import android.os.Handler
import android.os.Looper
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import java.nio.charset.Charset
import java.util.*
import kotlin.random.Random


class FindMatchService(peripheralManager: BluetoothPeripheralManager) :
    BaseService(peripheralManager) {
    override val service =
        BluetoothGattService(FMS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val findMatch = BluetoothGattCharacteristic(
        FIND_MATCH_CHARACTERISTIC_UUID,
        PROPERTY_READ,
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
        findMatch.value = getMatchCriteria()
    }

    override fun onNotifyingEnabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.uuid == FIND_MATCH_CHARACTERISTIC_UUID) {
            //notifyCurrentTime()
        }
    }

    override fun onNotifyingDisabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.uuid == FIND_MATCH_CHARACTERISTIC_UUID) {
            //stopNotifying()
        }
    }

    private fun notifyCurrentTime() {
        //notifyCharacteristicChanged(getCurrentTime(), currentTime)
        handler.postDelayed(notifyRunnable, 1000)
    }

    private fun stopNotifying() {
        handler.removeCallbacks(notifyRunnable)
    }

    private fun getMatchCriteria(): ByteArray {
        val sb = StringBuilder()
        if (getRandomNumber(0, 1) == 0) {
            sb.append("Male")
        } else {
            sb.append("Female")
        }
        sb.append(";")
        sb.append("${getRandomNumber(16, 25)}-${getRandomNumber(26, 40)}")
        sb.append(";")
        return sb.toString().toByteArray(Charset.defaultCharset())
    }

    fun getRandomNumber(min: Int, max: Int): Int {
        return Random(System.currentTimeMillis()).nextInt(max - min + 1) + min
    }

    override val serviceName: String
        get() = SERVICE_NAME

    companion object {
        val FMS_SERVICE_UUID: UUID = UUID.fromString("badb1111-cafe-f00d-d00d-8a41886b49fb")
        val FIND_MATCH_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("badb1122-cafe-f00d-d00d-8a41886b49fb")
        private const val SERVICE_NAME = "Find Match Service"
    }

    init {
        service.addCharacteristic(findMatch)
        findMatch.addDescriptor(cccDescriptor)
        findMatch.value = getMatchCriteria()
    }
}