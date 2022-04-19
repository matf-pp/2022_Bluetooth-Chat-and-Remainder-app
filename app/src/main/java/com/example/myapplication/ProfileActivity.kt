package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import com.example.myapplication.databinding.ActivityProfileBinding
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth


private const val REQUEST_ENABLE_BT = 1

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    //ActionBar
    private lateinit var actionBar: ActionBar

    //FB auth
    private lateinit var firebaseAuth: FirebaseAuth
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show()
            finish() //automatic close app if Bluetooth service is not available!
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar=supportActionBar!!
        actionBar.title="Profile"

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
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
}