package com.example.smart_fit

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smart_fit.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase


class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionsBinding
    private lateinit var progessDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progessDialog = ProgressDialog(this)
        progessDialog.setTitle("Please wait...")

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.closeBtn.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.loginEmailbtn.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.loginGoogleBtn.setOnClickListener(){
            beginGoogleLogin()

        }
    }
    private fun beginGoogleLogin(){
        Log.d(TAG, "beginGoogleLogin: ")

        val googleSignIntent = mGoogleSignInClient.signInIntent
        googleSignARL.launch(googleSignIntent)

    }
    private  val googleSignARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result->
        Log.d(TAG, "googleSignInARL: ")

        if (result.resultCode == RESULT_OK){

            val data = result.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account ID: ${account.id}")

                firebaseAuthWithGoogleAccount(account.idToken)
            }
            catch (e: Exception){
                Log.d(TAG, "googleSignInARL: ",e)
            }

        }
        else{

            Utils.toast(this, "Cancelled...!")
        }


    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?){
        Log.d(TAG, "firaebaseAuthWithGoogleAccount: idToken: $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                if (authResult.additionalUserInfo!!.isNewUser) {
                    Log.d(TAG, "firaebaseAuthWithGoogleAccount: New User, Account Created...")
                    updateUserinfoDb()
                }else{
                    Log.d(TAG, "firaebaseAuthWithGoogleAccount: Existing User, Logged In...")
                    checkUserTypeAndRedirect()
                    finishAffinity()
                }

            }
            .addOnFailureListener{e->
                Log.e(TAG, "firaebaseAuthWithGoogleAccount: ",e)
                Utils.toast(this, "${e.message}")
            }
    }

        private fun updateUserinfoDb(){
            Log.d(TAG, "updateUserinfoDb: ")

            progessDialog.setMessage("Saving user Info")
            progessDialog.show()

            val timestamp = Utils.getTimestamp()
            val registeredUserEmail = firebaseAuth.currentUser?.email
            val registeredUserUid = firebaseAuth.uid
            val name = firebaseAuth.currentUser?.displayName

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
            hashMap["userType"] = "Google"
            hashMap["timestamp"] = timestamp
            hashMap["onlineStatus"] = true
            hashMap["email"] = "$registeredUserEmail"
            hashMap["uid"] = "$registeredUserUid"

            val reference = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
            reference.child(registeredUserUid?.let { it } ?: "")  // Use safe access operator here
                .setValue(hashMap)
                .addOnSuccessListener {
                    Log.d(TAG, "updateUserinfoDb: User Info Saved ")
                    progessDialog.dismiss()

                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }

                .addOnFailureListener{e->
                    progessDialog.dismiss()
                    Log.e(TAG, "updateUserinfoDb: ",e )
                    Utils.toast(this, "Failed to save user info due to ${e.message}")
                }
        }
    private fun checkUserTypeAndRedirect() {
        val reference = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        val registeredUserUid = firebaseAuth.uid

        reference.child(registeredUserUid!!).get().addOnSuccessListener { snapshot ->
            val userType = snapshot.child("userType").getValue(String::class.java)
            if (userType == "admin") {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finishAffinity()
        }.addOnFailureListener { e ->
            Log.e(TAG, "checkUserTypeAndRedirect: ", e)
            Utils.toast(this, "${e.message}")
        }
    }
    companion object {
        private const val TAG = "LoginOptionsActivity"
    }
}
