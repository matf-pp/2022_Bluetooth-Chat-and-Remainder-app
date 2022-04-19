package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    //ActionBar
    private lateinit var actionBar: ActionBar
    //Progres dialog
    private lateinit var progressDialog: ProgressDialog
    //FB auth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        actionBar=supportActionBar!!
        actionBar.title="Sign Up"
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Creating account...")
        progressDialog.setCanceledOnTouchOutside(false)

        //firebase
        firebaseAuth= FirebaseAuth.getInstance()

        binding.SignUpBtn.setOnClickListener {
            validateData()
        }
    }
    private fun validateData()
    {
        email=binding.emailEt.text.toString().trim()
        password=binding.passwordEt.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            binding.emailEt.error="Invalid format"

        }
        else if(TextUtils.isEmpty(password)){
            binding.passwordEt.error="You must enter password!"
        }
        else if(password.length<6)
        {
            binding.passwordEt.error="Password must contain at least 6 characters!"
        }
        else{
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        progressDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val firebaseUser=firebaseAuth.currentUser
                val email=firebaseUser!!.email
                Toast.makeText(this,"Account created successfully", Toast.LENGTH_SHORT).show()
                //open porfile
                startActivity(Intent(this,ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}