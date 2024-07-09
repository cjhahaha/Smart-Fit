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
import com.example.smart_fit.databinding.ActivityRegisterEmailBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG= "REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackbtn.setOnClickListener{
            onBackPressed()
        }

        binding.haveAccountTv.setOnClickListener{
            onBackPressed()
        }

        binding.RegisterBtn.setOnClickListener{
            validateDATA()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private var email =""
    private var password =""
    private var retypepass = ""


    private fun validateDATA() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        retypepass = binding.retypepassEt.text.toString().trim()

        Log.d(TAG, "ValidateData: email: $email")
        Log.d(TAG, "ValidateData: password: $password")
        Log.d(TAG, "ValidateData: confirm password: $retypepass")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error = "Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            binding.passwordEt.error ="Enter Password"
            binding.passwordEt.requestFocus()
        }
        else if(retypepass.isEmpty()){
            binding.retypepassEt.error ="Please Confirm Password"
            binding.retypepassEt.requestFocus()
        }
        else if(password != retypepass){

            binding.retypepassEt.error = "Password Doesn't Match"
            binding.retypepassEt.requestFocus()

        }
        else{
            registerUser()
        }
    }

    private fun registerUser() {
        Log.d(TAG, "registerUser: ")
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()
            }
            .addOnFailureListener{e->
                Log.e(TAG, "registerUser: ",e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to create account due to ${e.message}")
            }
    }

    private fun updateUserInfo() {
        Log.d(TAG, "updateUserInfo: Starting...")
        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = System.currentTimeMillis() // or Utils.getTimestamp()
        val registeredUserEmail = firebaseAuth.currentUser?.email
        val registeredUserUid = firebaseAuth.currentUser?.uid

        if (registeredUserEmail == null || registeredUserUid == null) {
            Log.e(TAG, "updateUserInfo: Email or UID is null")
            progressDialog.dismiss()
            Utils.toast(this, "Failed to save user info: Email or UID is null")
            return
        }

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["gender"] = ""
        hashMap["dob"] = ""
        hashMap["height"] = ""
        hashMap["weight"] = ""
        hashMap["pushup"] = ""
        hashMap["gym"] = ""
        hashMap["weight"] = ""
        hashMap["hegiht"] = ""
        hashMap["target_weight"] = ""
        hashMap["BMI"] = ""
        hashMap["gym"] = ""
        hashMap["home"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["Equipment"] = ""
        hashMap["userType"] = "Email"
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = registeredUserEmail
        hashMap["uid"] = registeredUserUid

        val reference = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        reference.child(registeredUserUid?.let { it } ?: "")  // Use safe access operator here
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfo: User registered successfully")
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUserInfo: Error saving user info", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }
    }

}