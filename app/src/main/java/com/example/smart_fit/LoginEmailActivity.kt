package com.example.smart_fit

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smart_fit.databinding.ActivityLoginEmailBinding

import com.google.firebase.auth.FirebaseAuth
import kotlin.math.log

class LoginEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginEmailBinding

    private companion object{
        private const val TAG =" LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth


    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.toolbarBackbtn.setOnClickListener{
            onBackPressed()
        }

        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        binding.loginBtn.setOnClickListener{
            validateData()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
    private var email=""
    private var password=""
    private fun validateData() {


        email= binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        Log.d(TAG, "ValidData: email: $email")
        Log.d(TAG, "validateData: password: $password")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error = "Invalid Email format"
            binding.emailEt.requestFocus()
            }
            else if(password.isEmpty()){
                binding.passwordEt.error = "Error Password"
            binding.passwordEt.requestFocus()
        }else{
            loginUser()
        }
    }
    private fun loginUser(){
        Log.d(TAG, "LoginUser: ")

        progressDialog.setMessage("Logging in")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener{

                Log.d(TAG, "loginUser: Logged in...")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
        }
            .addOnFailureListener{e->
                Log.e(TAG,"LoginUser: ", e)
                progressDialog.dismiss()

                Utils.toast(this, "Unable to login due to ${e.message}")
            }
    }
}

