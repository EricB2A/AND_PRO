package com.example.blender

import android.Manifest
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager
import android.os.ParcelUuid;
import android.util.Log
import androidx.core.app.ActivityCompat

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
    private val serviceImplementations = HashMap<BluetoothGattService, Service>()

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

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter.name = ADAPTER_NAME

        this.peripheralManager = BluetoothPeripheralManager(context, bluetoothManager, peripheralManagerCallback)

    }

    private val peripheralManagerCallback: BluetoothPeripheralManagerCallback =
        object : BluetoothPeripheralManagerCallback() {
            override fun onServiceAdded(status: GattStatus, service: BluetoothGattService) {}
            override fun onCharacteristicRead(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                serviceImplementations[characteristic.service]?.onCharacteristicRead(
                    central,
                    characteristic
                )
            }

            override fun onCharacteristicWrite(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ): GattStatus {
                val serviceImplementation = serviceImplementations[characteristic.service]
                return if (serviceImplementation != null) ({
                    serviceImplementation.onCharacteristicWrite(central, characteristic, value)
                })!! else GattStatus.REQUEST_NOT_SUPPORTED
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
                return if (serviceImplementation != null) ({
                    serviceImplementation.onDescriptorWrite(central, descriptor, value)
                })!! else GattStatus.REQUEST_NOT_SUPPORTED
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
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()
        val advertiseData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()
        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()
        peripheralManager.startAdvertising(advertiseSettings, scanResponse, advertiseData)
    }

    private fun setupServices() {
        for (service in serviceImplementations.keys) {
            peripheralManager.add(service)
        }
    }

    companion object {
        private var instance : BLEServer? = null
        private var TAG = "BLEServer"
        private var ADAPTER_NAME = "BLENDER"

        @Synchronized
        fun getInstance(context : Context) : BLEServer {
            if (instance != null) {
                instance = BLEServer(context.applicationContext)
            }
            return instance!!
        }
    }
}