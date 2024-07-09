package com.example.smart_fit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.smart_fit.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AccountFragment : Fragment() {

    private lateinit var binding:FragmentAccountBinding



    private companion object{
        private const val TAG = "ACCOUNT_TAG"
    }

    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(layoutInflater, container,false)
        return (binding.root)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseAuth = FirebaseAuth.getInstance()

        loadMyInfo()

        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(mContext, LoginOptionsActivity::class.java))
            activity?.finishAffinity()
        }

        binding.editCv.setOnClickListener{
            startActivity(Intent(mContext, MeEditActivity::class.java))
        }
    }
        private fun loadMyInfo(){
            val reference = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
            val uid = firebaseAuth.uid ?: return

            reference.child(uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val name = "${snapshot.child("name").value}"
//                        val dob = "${snapshot.child("dob").value}"
                            val email = "${snapshot.child("email").value}"
                            val phone = "${snapshot.child("phone").value}"
                            val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
//                        val userType = "${snapshot.child("userType").value}"
                            var timestamp = "${snapshot.child("timestamp").value}"

                            if (timestamp == "null") {
                                timestamp = "0"
                            }
                            val formattedDate = Utils.formatTimestampDate(timestamp.toLong())

                            binding.emailTv.text = email
                            binding.nameTv.text = name
                            binding.phoneTv.text = phone


//                        if(userType == "Email"){
//                            val isVerified = firebaseAuth.currentUser!!.isEmailVerified
//                            if(isVerified){
//                                binding.verificationTv.text= "Verified"
//                            }else{
//                                binding.verificationTv.text="NoT Verified"
//
//                        }
//
//                    }else{
//                        binding.verificationTv.text="Verified"
//                        }
                            try {
                                Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.round_person_24)
                                    .into(binding.profileTv)

                            } catch (e: Exception) {
                                Log.e(TAG, "onDataChange: ", e)
                            }
                        }else {
                                Log.d(TAG, "No data snapshot found for user with UID: $uid")}

                    } override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Database error: ", error.toException())
                    }
                })

        }

}