package com.example.blender.BLE

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log

import com.welie.blessed.AdvertiseError;
import com.welie.blessed.BluetoothCentral;
import com.welie.blessed.BluetoothPeripheralManager;
import com.welie.blessed.BluetoothPeripheralManagerCallback;
import com.welie.blessed.GattStatus;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


class BLEServer {
    private lateinit var peripheralManager: BluetoothPeripheralManager
    private val serviceImplementations = HashMap<BluetoothGattService, Service>() // TODO plusieurs service ? Autrement à simplifier ?
    private lateinit var blenderService : BlenderService


    constructor(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(bluetoothAdapter == null || bluetoothManager == null) {
            Log.d(TAG, "bluetooth not supported")
            return
        }

        if(!bluetoothAdapter.isMultipleAdvertisementSupported) {
            Log.d(TAG, "not supporting advertising")
            return
        }

        this.peripheralManager = BluetoothPeripheralManager(context, bluetoothManager, peripheralManagerCallback)
        this.peripheralManager.removeAllServices()

        blenderService = BlenderService(peripheralManager)
        serviceImplementations[blenderService.service] = blenderService
        setupServices()
    }

    private val peripheralManagerCallback: BluetoothPeripheralManagerCallback =
        object : BluetoothPeripheralManagerCallback() {
            override fun onServiceAdded(status: GattStatus, service: BluetoothGattService) {}

            /**
             * Méthode appelé lorsqu'un "remote central" souhaite lire une caractéristique locale
             */
            override fun onCharacteristicRead(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                serviceImplementations[characteristic.service]?.onCharacteristicRead(
                    central,
                    characteristic
                )
            }
            /**
             * Méthode appelé lorsqu'un "remote central" souhaite écrire une valeur dans une caractéristique.
             */
            override fun onCharacteristicWrite(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ): GattStatus {
                val serviceImplementation = serviceImplementations[characteristic.service]
                return if (serviceImplementation != null) {
                    val res = serviceImplementation.onCharacteristicWrite(central, characteristic, value) as GattStatus
                    if(res == GattStatus.SUCCESS){
                        Matcher.getInstance().serverMatch(Pair(central.address, central))
                    }
                    Log.d("BLEServer", "SERVER MATCH")
                    res
                } else GattStatus.REQUEST_NOT_SUPPORTED
            }

            override fun onDescriptorRead(
                central: BluetoothCentral,
                descriptor: BluetoothGattDescriptor
            ) {
                val characteristic: BluetoothGattCharacteristic = Objects.requireNonNull(
                    descriptor.characteristic,
                    "Descriptor has no Characteristic"
                )
                val service: BluetoothGattService =
                    Objects.requireNonNull(characteristic.service, "Characteristic has no Service")
                val serviceImplementation = serviceImplementations[service]
                serviceImplementation?.onDescriptorRead(central, descriptor)
            }

            override fun onDescriptorWrite(
                central: BluetoothCentral,
                descriptor: BluetoothGattDescriptor,
                value: ByteArray
            ): GattStatus {
                val characteristic: BluetoothGattCharacteristic = Objects.requireNonNull(
                    descriptor.characteristic,
                    "Descriptor has no Characteristic"
                )
                val service: BluetoothGattService =
                    Objects.requireNonNull(characteristic.service, "Characteristic has no Service")
                val serviceImplementation = serviceImplementations[service]
                return if (serviceImplementation != null) {
                    serviceImplementation.onDescriptorWrite(central, descriptor, value) as GattStatus
                } else GattStatus.REQUEST_NOT_SUPPORTED
            }

            override fun onNotifyingEnabled(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onNotifyingEnabled(central, characteristic)
            }

            override fun onNotifyingDisabled(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onNotifyingDisabled(central, characteristic)
            }

            override fun onNotificationSent(
                central: BluetoothCentral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onNotificationSent(central, value, characteristic, status)
            }

            override fun onCentralConnected(central: BluetoothCentral) {
                for (serviceImplementation in serviceImplementations.values) {
                    serviceImplementation.onCentralConnected(central)
                }
            }

            override fun onCentralDisconnected(central: BluetoothCentral) {
                for (serviceImplementation in serviceImplementations.values) {
                    serviceImplementation.onCentralDisconnected(central)
                }
            }

            override fun onAdvertisingStarted(settingsInEffect: AdvertiseSettings) {}
            override fun onAdvertiseFailure(advertiseError: AdvertiseError) {}
            override fun onAdvertisingStopped() {}
        }

    fun startAdvertising(serviceUUID: UUID?) {
        Log.d(this.javaClass.simpleName, "startAdvertising 1" )
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val advertiseData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()
        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()
        peripheralManager.startAdvertising(advertiseSettings, scanResponse, advertiseData)
        Log.d(this.javaClass.simpleName, "startAdvertising 2" )
    }

    fun stopAdvertising() {
        peripheralManager.stopAdvertising()
    }

    private fun setupServices() {
        for (service in serviceImplementations.keys) {
            peripheralManager.add(service)
        }
    }

    companion object {
        private var instance : BLEServer? = null
        private const val TAG = "BLEServer"
        private const val ADAPTER_NAME = "BLENDER"

        @Synchronized
        fun getInstance(context : Context) : BLEServer {
            if (instance == null) {
                instance = BLEServer(context.applicationContext)
            }
            return instance!!
        }
    }
}