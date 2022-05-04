
package com.example.myapplication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import com.example.myapplication.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth


private const val REQUEST_ENABLE_BT = 1

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    //ActionBar
    private lateinit var actionBar: ActionBar

    //FB auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Bluetooth Management
    private lateinit var bluetoothAdapter: BluetoothAdapter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter


        actionBar=supportActionBar!!
        actionBar.title="Profile"

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }


        binding.notificationBtn.setOnClickListener {
            startActivity(Intent(this,NotificationActivity::class.java))
        }


        binding.chatBtn.setOnClickListener {
            discoverBluetooth(bluetoothAdapter)
        }

    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser!=null)
        {
            val email = firebaseUser.email
            binding.emailTv.text=email
        }
        else
        {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun discoverBluetooth(blAdapter : BluetoothAdapter?) {

        val bluetoothAdapter: BluetoothAdapter? = blAdapter

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
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
        if (bluetoothAdapter != null) {

            if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                val discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                startActivityForResult(discoveryIntent,0)

            }
            else{
                startActivity(Intent(this, ChatActivity::class.java))
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details
        }
        if ( bluetoothAdapter?.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
            startActivity(Intent(this, ChatActivity::class.java))

    }


}