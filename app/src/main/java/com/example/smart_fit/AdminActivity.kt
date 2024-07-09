package com.example.smart_fit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smart_fit.databinding.ActivityAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser == null) {
            startLoginOptions()
        } else {
            showDashboardFragment()
        }

        binding.bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_Dashboard -> {
                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    } else {
                        showDashboardFragment()
                        true
                    }
                }
                R.id.menu_Maintenance -> {
                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    } else {
                        showMaintenanceFragment()
                        true
                    }
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showDashboardFragment() {
        binding.toolbarTitleTv.text = "Admin"
        val fragment = Admin_DashboardFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "AdminFragment")
        fragmentTransaction.commit()
    }

    private fun showMaintenanceFragment() {
        binding.toolbarTitleTv.text = "Maintenance"
        val fragment = Admin_MaintenanceFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment, "MaintenanceFragment")
        fragmentTransaction.commit()
    }

    private fun startLoginOptions() {
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }
}
