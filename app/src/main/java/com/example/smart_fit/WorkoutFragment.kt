package com.example.smart_fit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.example.smart_fit.databinding.FragmentWorkout2Binding

class WorkoutFragment : Fragment() {

    private lateinit var binding: FragmentWorkout2Binding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        binding = FragmentWorkout2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for the logout button
        binding.logoutbutton.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), LoginOptionsActivity::class.java))
            activity?.finishAffinity()
        }
    }
}
