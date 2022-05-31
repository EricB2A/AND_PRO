package com.example.blender

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.welie.blessed.*
import org.w3c.dom.Text
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var central: BluetoothCentralManager

    private lateinit var broadcastBtn: Button
    private lateinit var connectBtn: Button
    private lateinit var connectionStatusTxt: TextView
    private lateinit var matchWantedTxt: TextView

    private val REQUEST_ENABLE_BT = 1
    private val ACCESS_LOCATION_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        broadcastBtn.setOnClickListener {
            BLEServer.getInstance(this)
                .startAdvertising(BLEServer.getInstance(this).fms.service.uuid)
        }

        connectBtn.setOnClickListener {
            central = BluetoothCentralManager(
                applicationContext,
                bluetoothCentralManagerCallback,
                Handler(Looper.getMainLooper())
            )

            central.scanForPeripheralsWithServices(arrayOf(UUID.fromString("badb1111-cafe-f00d-d00d-8a41886b49fb")))

            val recycler = findViewById<RecyclerView>(R.id.discussions)
            val adapter = DiscussionRecyclerAdapter()
            recycler.adapter= adapter
            recycler.layoutManager= LinearLayoutManager(this)
            val list = mutableListOf(Discussion("Alec Berney", "123"), Discussion("Eric Broutba", "456"), Discussion("Manu", "voleur", true))
            for(i in 1..20) {
                list.add(Discussion("123", "123"))
            }
            adapter.items = list
        }
    }

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                Log.d("test", peripheral.name)
                central.stopScan()
                central.connectPeripheral(peripheral, peripheralCallback)
            }
        }

    private val peripheralCallback: BluetoothPeripheralCallback =
        object : BluetoothPeripheralCallback() {
            override fun onServicesDiscovered(
                peripheral: BluetoothPeripheral
            ) {
                super.onServicesDiscovered(peripheral)
                Log.d(TAG, peripheral.name)
                Log.d(
                    TAG,
                    peripheral.getService(FindMatchService.FMS_SERVICE_UUID)?.characteristics?.get(0)?.uuid.toString()
                )
                connectionStatusTxt.text = "Connected to: ${peripheral.name}\nCharacteristic: ${
                    peripheral.getService(FindMatchService.FMS_SERVICE_UUID)?.characteristics?.get(0)?.uuid.toString()
                }"
                connectionStatusTxt.visibility = TextView.VISIBLE
                val gson = Gson()
                val currentUser = User(
                    "Janne",
                    MatchWanted(MatchWanted.Gender.MALE,
                        20,
                        30,
                    ),
                    MatchWanted.Gender.FEMALE,
                    55
                )
                val result = peripheral.writeCharacteristic(
                    FindMatchService.FMS_SERVICE_UUID,
                    FindMatchService.FIND_MATCH_CHARACTERISTIC_UUID,
                    gson.toJson(currentUser).toByteArray(Charset.defaultCharset()),
                    WriteType.WITH_RESPONSE
                )
                Log.d(TAG, "Can read characteristic: ${result}")
            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status)
                if (status === GattStatus.SUCCESS) {
                    val s = String(value)
                    Log.d(
                        TAG,
                        "Wants : ${s.substring(0, s.indexOf(';'))}, aged: ${
                            s.substring(
                                s.indexOf(';') + 1, s.indexOf(';', s.indexOf(';') + 1)
                            )
                        }"
                    )
                    matchWantedTxt.text = "Wants : ${s.substring(0, s.indexOf(';'))}, aged: ${
                        s.substring(
                            s.indexOf(';') + 1, s.indexOf(';', s.indexOf(';') + 1)
                        )
                    }"
                    matchWantedTxt.visibility = TextView.VISIBLE
                }
                central.close();
            }

            override fun onCharacteristicWrite(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicWrite(peripheral, value, characteristic, status)
                if(characteristic.uuid == FindMatchService.FIND_MATCH_CHARACTERISTIC_UUID) {
                    if(status == GattStatus.SUCCESS) {
                        Log.d(TAG, "A new match has been made!")
                    } else if(status == GattStatus.VALUE_NOT_ALLOWED) {
                        Log.d(TAG, "Too bad! You just missed a match!")
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        if (!isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            checkPermissions()
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return false
        return bluetoothAdapter.isEnabled
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val missingPermissions = getMissingPermissions(getRequiredPermissions())
            if (missingPermissions.size > 0) {
                requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST)
            } else {
                permissionsGranted()
            }
        }
    }

    private fun getMissingPermissions(requiredPermissions: Array<String>): Array<String> {
        val missingPermissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (requiredPermission in requiredPermissions) {
                if (applicationContext.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission)
                }
            }
        }
        return missingPermissions.toTypedArray()
    }

    private fun getRequiredPermissions(): Array<String> {
        val targetSdkVersion = applicationInfo.targetSdkVersion
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work
        if (checkLocationServices()) {
            initBluetoothHandler()
        }
    }

    private fun areLocationServicesEnabled(): Boolean {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager == null) {
            Log.d(TAG, "could not get location manager")
            return false
        }
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }

    private fun checkLocationServices(): Boolean {
        return if (!areLocationServicesEnabled()) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Location services are not enabled")
                .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                .setPositiveButton("Enable",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.cancel()
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    })
                .setNegativeButton(
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which -> // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel()
                    })
                .create()
                .show()
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if all permission were granted
        var allGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }
        if (allGranted) {
            permissionsGranted()
        } else {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Location permission is required for scanning Bluetooth peripherals")
                .setMessage("Please grant permissions")
                .setPositiveButton("Retry",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.cancel()
                        checkPermissions()
                    })
                .create()
                .show()
        }
    }

    private fun initBluetoothHandler() {
        BLEServer.getInstance(applicationContext)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
