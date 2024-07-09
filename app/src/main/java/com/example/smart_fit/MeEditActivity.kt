package com.example.smart_fit

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.smart_fit.databinding.ActivityMeEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MeEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeEditBinding

    private companion object{
        private const val TAG = "ME_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var myUserType = ""
    private var imageUri: Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    binding = ActivityMeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadMyinfo()

        binding.toolbarBackbtn.setOnClickListener{
            onBackPressed()
        }
        binding.profileImagePickFab.setOnClickListener{
            imagePickupDialog()
        }
        binding.updateBtn.setOnClickListener{
            validateData()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private var name = ""
    private var weight = ""
    private var height = ""
    private var tweight = ""
    private fun validateData(){
        name = binding.nameEt.text.toString().trim()
        weight = binding.weightEt.text.toString().trim()
        height = binding.heightEt.text.toString().trim()
        tweight= binding.tweighttEt.text.toString().trim()


        if(imageUri == null){
            updateProfileDB(null)
        }else{
            uploadProfileImageStorage()
        }
    }
    private fun uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ")

        progressDialog.setMessage("Uploading image")
        progressDialog.show()

        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"

        val ref = FirebaseStorage.getInstance("gs://smartfitapp-c1cdb.appspot.com").reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadProfileImageStorage: Image Uploaded...")

                val uriTask = taskSnapshot.storage.downloadUrl
                uriTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val uploadedImageUrl = it.result.toString()
                        updateProfileDB(uploadedImageUrl)
                    } else {
                        Log.e(TAG, "uploadProfileImageStorage: Failed to get download URL", it.exception)
                        progressDialog.dismiss()
                        Utils.toast(this, "Failed to get download URL")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "uploadProfileImageStorage: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to upload due to ${e.message}")
            }
    }
    private fun updateProfileDB(uploadedImageUrl: String?){
        Log.d(TAG, "updateProfileDB: uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Updating user Info")
        progressDialog.show()




        val hashMap = HashMap<String, Any>()
        hashMap["name"] = "$name"
        hashMap["weight"] = "$weight"
        hashMap["height"] = "$height"
        hashMap["target_weight"] = "$tweight"


        if(uploadedImageUrl != null){
            hashMap["profileImageUrl"] ="$uploadedImageUrl"
        }
        val ref = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        val uid = firebaseAuth.uid ?: return
        ref.child(uid)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDB: Updated...")
                progressDialog.dismiss()
                Utils.toast(this, "Updated...")
                imageUri = null
                onBackPressed()
            }
            .addOnFailureListener() { e->
                Log.e(TAG, "updateProfileDB: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to update due to ${e.message}")
            }


    }

    private fun loadMyinfo(){
        Log.d(TAG, "loadMyinfo: ")

        val ref = FirebaseDatabase.getInstance("https://smartfitapp-c1cdb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        val uid = firebaseAuth.uid ?: return

        ref.child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val weight = "${snapshot.child("weight").value}"
                    val height = "${snapshot.child("height").value}"
                    val tweight = "${snapshot.child("target_weight").value}"
                    val profileImgUrl = "${snapshot.child("profileImgUrl").value}"
                    myUserType = "${snapshot.child("userType").value}"


//                    if(myUserType.equals("Email", true) || myUserType.equals("Google", true)){
//
//                        binding.nameTil.isEnabled = false
//                        binding.nameEt.isEnabled = false
//                    }
//                    else{
//
//                    }

                    binding.nameEt.setText(name)
                    binding.tweighttEt.setText(tweight)
                    binding.heightEt.setText(height)
                    binding.weightEt.setText(weight)

                    try {
                        Glide.with(this@MeEditActivity)
                            .load(profileImgUrl)
                            .placeholder(R.drawable.round_person_24)
                            .into(binding.profileTv)

                    }
                    catch (e: Exception){
                        Log.e(TAG, "onDataChange: ",e )
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickupDialog(){
        val popupMenu = PopupMenu(this,binding.profileImagePickFab)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId

            if (itemId == 1) {

                Log.d(TAG, "imagePickupDialog: Camera Clicked, Check if camera permission(s) granted or not")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                }
                else{
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            } else if (itemId == 2) {
                Log.d(TAG, "imagePickupDialog: Camera Clicked, Check if camera permission(s) granted or not")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            return@setOnMenuItemClickListener true
        }
    }
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ result->
            Log.d(TAG, "requestCameraPermissions: result:$result")

            var areAllGranted = true
            for(isGranted in result.values){
                areAllGranted = areAllGranted && isGranted
            }
            if(areAllGranted){
                Log.d(TAG, "requestCameraPermissions: All granted e.g Camera Storage")
                pickImageCamera()
            }
            else{
                Log.d(TAG, "requestCameraPermissions: All or either one is denied... ")
                Utils.toast(this, "Camera or Storage or both permissions denied")
            }
        }
    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->

            Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

            if(isGranted){
                pickImageGallery()
            }else{
                Utils.toast( this, "Storage permission denied...")
            }
        }
    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image_description")
        imageUri= contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLAuncher.launch(intent)


    }

    private val cameraActivityResultLAuncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->

            if(result.resultCode == Activity.RESULT_OK){
                Log.d(TAG, "cameraActivityResultLauncher: Image captured: imageUri: $imageUri")
                
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.round_person_24)
                        .into(binding.profileTv)
                }
                catch (e: Exception){
                    Log.e(TAG, "CameraActivityResultLauncher: ", e)
                }
            }else{
                Utils.toast(this, "Cancelled!")
            }

        }
    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"

        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.round_person_24)
                        .into(binding.profileTv)
                }
                catch (e: java.lang.Exception){
                    Log.e(TAG, "galleryActivityLauncher: ", e)
                }
            }
        }

}