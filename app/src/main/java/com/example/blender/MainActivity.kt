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
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blender.BLE.BLEClient
import com.example.blender.BLE.BLEServer
import com.example.blender.BLE.BlenderService
import com.example.blender.models.*
import com.example.blender.viewmodel.DiscussionViewModelFactory
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.blender.viewmodel.DiscussionViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private var servicesRunning = false


    private val discussionViewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory((application as Blender).repository)
    }

    private fun initTestData() {
        Log.d("Mainactivity", "WORKING1")
        val repository = (application as Blender).repository
        repository.reset()
        TimeUnit.SECONDS.sleep(1)
        Log.d("Mainactivity", "WORKING")

        repository.getMyProfile().observe(this) {
            if(it == null) {
                val p = Profile(null, "test", "test", Calendar.getInstance(), Gender.OTHER, InterestGender.ANY, true, UUID.randomUUID())
                repository.insertProfile(p)
                Log.d("test", "main p null")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTestData()
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.discussions)
        val adapter = DiscussionRecyclerAdapter()
        recycler.adapter= adapter
        recycler.layoutManager= LinearLayoutManager(this)
        discussionViewModel.allDiscussions.observe(this) { value ->
            adapter.items = value.sortedByDescending { it.conversation.updatedAt }
        }

        bleClient = BLEClient.getInstance(this)

        startServices()
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
                "We need theses permissions :)",
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
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private fun startServices() {
        if (checkPermissions() && checkLocationServices() && !servicesRunning) {
            Log.d(this.javaClass.simpleName, "1" )
            BLEServer.getInstance(application)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_PERMISSIONS = 1
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_ENABLE_LOCATION = 2

        lateinit var bleClient : BLEClient
    }
}
