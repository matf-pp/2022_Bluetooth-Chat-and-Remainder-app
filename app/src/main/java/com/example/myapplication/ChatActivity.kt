package com.example.myapplication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val REQUEST_ENABLE_BT = 1
open class ChatActivity : AppCompatActivity() {

    private lateinit var context: Context
    private val LOCATION_PERMISSION_REQUEST: Int = 101
    private val SELECT_DEVICE: Int = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        context = this

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        //Proverava se if za poziv iz drugog puta
        var address: String? = intent?.getStringExtra("Address")
        if ( address != null) {
            var out: String = "Address: $address"
            Toast.makeText(context, out, Toast.LENGTH_SHORT).show()
        }

        //Izvrsava se kada se prvi put startuje profile
        else {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                //Device doesn't support Bluetooth
                Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show()
                finish() //automatic close app if Bluetooth service is not available!
            }
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

       if (item.itemId == R.id.menu_search_devices) {
            checkPermissions()
            return true
        }
       else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, Array(10){android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST)
        }
        else{
            startActivityForResult(Intent(context, DeviceListActivity::class.java), SELECT_DEVICE)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivityForResult(Intent(context, DeviceListActivity::class.java), SELECT_DEVICE)
                finish()
            }
            else {
              AlertDialog.Builder(this)
                  .setCancelable(false)
                  .setMessage("Location permission is required.\n Please grant")
                  .setPositiveButton("Grant", DialogInterface.OnClickListener { dialogInterface, i -> checkPermissions() })
                  .setNegativeButton("Deny", DialogInterface.OnClickListener { dialogInterface, i -> this.finish() })
                  .show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         }
    }
}