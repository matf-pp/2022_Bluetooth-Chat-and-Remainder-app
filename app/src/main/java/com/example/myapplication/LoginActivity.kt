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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    //ViewBinding
    private lateinit var binding:ActivityLoginBinding
    //ActionBar
    private lateinit var actionBar: ActionBar
    //Progres dialog
    private lateinit var progressDialog: ProgressDialog
    //FB auth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email=""
    private var password=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        actionBar=supportActionBar!!
        actionBar.title="Login"

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Logging in...")
        progressDialog.setCanceledOnTouchOutside(false)

        //firebase
        firebaseAuth= FirebaseAuth.getInstance()
        checkUser()

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        binding.loginBtn.setOnClickListener {
            validateData()
        }
    }
    private fun validateData()
    {
        //get data
        email=binding.emailEt.text.toString().trim()
        password=binding.passwordEt.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            //invalid format
            binding.emailEt.error="Invalid email format"
        }
        else if(TextUtils.isEmpty(password))
        {
            binding.passwordEt.error="Please enter password"
        }
        else
        {
            firebaseLogin()
        }
    }
    private fun firebaseLogin()
    {
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val firebaseUser=firebaseAuth.currentUser
                val email=firebaseUser!!.email
                Toast.makeText(this,"LoggedIn successfully",Toast.LENGTH_SHORT).show()
                //open porfile
                startActivity(Intent(this,ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
    private fun checkUser()
    {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser!=null)
        {
            startActivity(Intent(this,ProfileActivity::class.java))
            finish()
        }
    }
}