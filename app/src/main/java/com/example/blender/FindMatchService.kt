package com.example.blender


import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattService
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.GattStatus
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import kotlin.random.Random


class FindMatchService(peripheralManager: BluetoothPeripheralManager) :
    BaseService(peripheralManager) {
    override val service =
        BluetoothGattService(FMS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val findMatch = BluetoothGattCharacteristic(
        FIND_MATCH_CHARACTERISTIC_UUID,
        PROPERTY_WRITE,
        PERMISSION_WRITE
    )
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val notifyRunnable = Runnable { notifyCurrentTime() }
    override fun onCentralDisconnected(central: BluetoothCentral) {
        if (noCentralsConnected()) {
            stopNotifying()
        }
    }

    override fun onCharacteristicWrite(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): GattStatus? {
        if(checkMatch(value)) {
            return GattStatus.SUCCESS
        }
        return GattStatus.VALUE_NOT_ALLOWED
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

    private fun checkMatch(value: ByteArray): Boolean {
        val currentUser = User(
            "Jean",
            MatchWanted(MatchWanted.Gender.FEMALE,
                16,
                20,
            ),
            MatchWanted.Gender.MALE,
            25
        )
        val gson = Gson()
        val remoteUser = gson.fromJson(String(value), User::class.java)
        return currentUser.isAMatch(remoteUser)
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
        //findMatch.value = getMatchCriteria()
    }
}