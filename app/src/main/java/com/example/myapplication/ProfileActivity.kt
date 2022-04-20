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
import com.google.firebase.auth.FirebaseAuth


private const val REQUEST_ENABLE_BT = 1

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    //ActionBar
    private lateinit var actionBar: ActionBar

    //FB auth
    private lateinit var firebaseAuth: FirebaseAuth

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
        binding.notificationBtn.setOnClickListener {
            startActivity(Intent(this,NotificationActivity::class.java))
        }
        binding.chatBtn.setOnClickListener {
            startActivity(Intent(this,ChatActivity::class.java))
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