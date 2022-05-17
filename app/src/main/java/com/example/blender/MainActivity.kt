package com.example.blender

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var central : BluetoothCentralManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        central = BluetoothCentralManager(
            applicationContext,
            bluetoothCentralManagerCallback,
            Handler(Looper.getMainLooper())
        )

        central.scanForPeripherals()
    }

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                Log.d("test", peripheral.name)
                //central.stopScan()
                //central.connectPeripheral(peripheral, peripheralCallback)
            }
        }
}