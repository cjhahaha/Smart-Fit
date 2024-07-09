package com.example.smart_fit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment
import com.example.smart_fit.databinding.ActivityMainBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        if(firebaseAuth.currentUser == null){

            startLoginOptions()
            return

        }else{
            checkUserTypeAndRedirect()

        }

        binding.bottom.setOnItemSelectedListener {item->

            when(item.itemId){
                R.id.menu_workout ->{

                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{

                        showWorkoutFragment()
                        true

                    }
                }

//                R.id.menu_custom ->{
//                    if(firebaseAuth.currentUser == null){
//                        Utils.toast(this, "Login Required")
//                        startLoginOptions()
//
//                        false
//                    }
//                    else{
//
//                        showCustomFragment()
//                        true
//
//                    }
//                }
                R.id.menu_exercises ->{
                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{

                        showExerciseFragment()

                        true

                    }

                }
                R.id.menu_report ->{
                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{

                        showReportsFragment()
                        true

                    }


                }
                R.id.menu_account ->{
                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{

                        showAccountFragment()
                        true

                    }

                }
                else -> {
                    false
                }
            }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    private fun checkUserTypeAndRedirect() {
        val reference = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        val registeredUserUid = firebaseAuth.uid

        reference.child(registeredUserUid!!).get().addOnSuccessListener { snapshot ->
            val userType = snapshot.child("userType").getValue(String::class.java)
            if (userType == "admin") {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
            } else {
                showDefaultFragment()
            }

        }.addOnFailureListener { e ->
            Log.e(TAG, "checkUserTypeAndRedirect: ", e)
            Utils.toast(this, "${e.message}")
            // Handle the failure to fetch user data here
            // For simplicity, you can redirect to LoginOptionsActivity or handle it as per your app logic
            startLoginOptions()
        }
    }

    private fun showDefaultFragment() {
        // Show the default fragment or perform default actions for non-admin users
        // For example, showing the workout fragment
        binding.toolbarTitleTv.text = "Workout"
        val fragment = WorkoutFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "WorkoutFragment")
        fragmentTransaction.commit()
    }

    private fun showWorkoutFragment(){

//        showWorkoutFragment()
        binding.toolbarTitleTv.text = "Workout"
        val fragment = WorkoutFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "WorkoutFragment")
        fragmentTransaction.commit()

    }
    private fun showCustomFragment(){
//        showCustomFragment()
        binding.toolbarTitleTv.text = "Custom"
        val fragment = CustomFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "CustomFragment")
        fragmentTransaction.commit()

    }
    private fun showExerciseFragment(){
//        showExerciseFragment()
        binding.toolbarTitleTv.text = "Exercise"

        val fragment = ExerciseFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "ExerciseFragment")
        fragmentTransaction.commit()
    }
    private fun showReportsFragment(){
//        showReportFragment()
        binding.toolbarTitleTv.text = "Report"

        val fragment = ReportsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "ReportsFragment")
        fragmentTransaction.commit()
    }
    private fun showAccountFragment(){
//        showAccountFragment()
        binding.toolbarTitleTv.text = "Account"

        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "AccountFragment")
        fragmentTransaction.commit()

    }
    private fun startLoginOptions(){
        startActivity(Intent(this, LoginOptionsActivity::class.java))
        finish()
    }
}
