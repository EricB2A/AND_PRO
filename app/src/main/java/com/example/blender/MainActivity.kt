package com.example.blender

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.BLE.BLEClient
import com.example.blender.BLE.BLEServer
import com.example.blender.BLE.BlenderService
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() {

    private var servicesRunning = false;

    private lateinit var bleClient: BLEClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bleClient = BLEClient(this)

        startServices()

        val recycler = findViewById<RecyclerView>(R.id.discussions)
        val adapter = DiscussionRecyclerAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        val list = mutableListOf(
            Discussion("Alec Berney", "123"),
            Discussion("Eric Broutba", "456"),
            Discussion("Manu", "voleur", true)
        )
        for (i in 1..20) {
            list.add(Discussion("123", "123"))
        }
        adapter.items = list
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if(checkPermissions()) {
            if (!isBluetoothEnabled()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            startServices()
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        return bluetoothAdapter.isEnabled
    }

    private fun checkPermissions() : Boolean {
        if(!EasyPermissions.hasPermissions(
                this,
                *getRequiredPermissions()
        )) {

            EasyPermissions.requestPermissions(
                this,
                "We need these permissions :)",
                REQUEST_PERMISSIONS,
                *getRequiredPermissions()
            )
            return false
        }
        return true
    }

    private fun getRequiredPermissions(): Array<String> {
        val targetSdkVersion = applicationInfo.targetSdkVersion

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

/*      TODO vérifier que cela fonctionne chez tout le monde avec le code ci-dessus.
        val targetSdkVersion = applicationInfo.targetSdkVersion
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN
        ) else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)*/


    }
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private fun startServices() {

        if (checkPermissions() && checkLocationServices() && !servicesRunning) {
            Log.d(this.javaClass.simpleName, "TAMERE" )
            BLEServer.getInstance(this)
                .startAdvertising(BlenderService.BLENDER_SERVICE_UUID)
            Log.d(this.javaClass.simpleName, "2" )
            bleClient.startScan()
            Log.d(this.javaClass.simpleName, "3" )
            servicesRunning = true;
        }
    }

    private fun areLocationServicesEnabled(): Boolean {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun checkLocationServices(): Boolean {
        if (!areLocationServicesEnabled()) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_PERMISSIONS = 1
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_ENABLE_LOCATION = 2
    }
}
