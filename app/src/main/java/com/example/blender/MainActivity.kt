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

class MainActivity : AppCompatActivity() {

    private var servicesRunning = false

    private val discussionViewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory((application as Blender).repository)
    }

    //Fonction afin d'initialiser les données dans la DB si elles n'existent pas encore
    private fun initData() {
        val repository = (application as Blender).repository

        repository.getMyProfile().observe(this) {
            if (it == null) {
                val p = Profile(
                    null,
                    "BlenderUser",
                    "John",
                    Calendar.getInstance(),
                    Gender.MAN,
                    InterestGender.ANY,
                    true,
                    UUID.randomUUID().toString()
                )
                repository.insertProfile(p)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        setContentView(R.layout.activity_main)

        Notification.createNotificationChannel(this)

        //Initialisation de la recyclerview pour les discussions
        val recycler = findViewById<RecyclerView>(R.id.discussions)
        val adapter = DiscussionRecyclerAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        //Passer les discussions triées de la plus récente à la plus ancienne
        discussionViewModel.allDiscussions.observe(this) { value ->
            adapter.items = value.sortedByDescending { it.conversation.updatedAt }
        }
        //Récupération du profil et démarrage des services BLE
        (application as Blender).repository.getMyProfile().observe(this) {
            if (it != null) {
                BLEServer.getInstance(application).setProfile(it)
                if (servicesRunning) {
                    startServices()
                }
            }
        }

        bleClient = BLEClient.getInstance(this)


    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            //Demande de l'activation du bluetooth
            if (!isBluetoothEnabled()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            (application as Blender).repository.getMyProfile().observe(this) {
                if (it != null) {
                    BLEServer.getInstance(application).setProfile(it)
                    if (!servicesRunning) {
                        startServices()
                    }
                }
            }
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        return bluetoothAdapter.isEnabled
    }

    private fun checkPermissions(): Boolean {
        //Vérification des permissions requises
        if (!EasyPermissions.hasPermissions(
                this,
                *getRequiredPermissions()
            )
        ) {

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
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private fun startServices() {
        if (checkPermissions() && checkLocationServices() && !servicesRunning) {
            BLEServer.getInstance(application)
                .startAdvertising(BlenderService.BLENDER_SERVICE_UUID)
            bleClient.startScan()
            servicesRunning = true
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
        //Création du menu
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Ajout du click pour modifier le profil
        return when (item.itemId) {
            R.id.menu_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        const val CHANNEL_ID = "main"
        const val TAG = "MainActivity"
        const val REQUEST_PERMISSIONS = 1
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_ENABLE_LOCATION = 2

        lateinit var bleClient: BLEClient
    }
}
