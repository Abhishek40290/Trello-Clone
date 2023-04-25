package com.example.trelloclone.activities.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.activities.firebase.FirestoreClass
import com.example.trelloclone.activities.model.User
import com.example.trelloclone.activities.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var  ivProfileUserImage: ImageView
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL : String = ""
    private lateinit var btnUpdate: Button
    private lateinit var mUserDetails: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        FirestoreClass().loadUserData(this)
        btnUpdate = findViewById(R.id.btn_update)

        ivProfileUserImage = findViewById(R.id.iv_profile_user_image)

        ivProfileUserImage.setOnClickListener{
            if(ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
               Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        btnUpdate.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(this,
                "Oops you just denied the permission for storage. You can turn it on in the settings",
                Toast.LENGTH_LONG)
                .show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null)
            mSelectedImageFileUri = data.data

        try {
            Glide
                .with(this)
                .load(mSelectedImageFileUri)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(ivProfileUserImage)
        }catch (e: IOException){
            e.printStackTrace()
        }

    }

    private fun updateUserProfileData(){
        val etName = findViewById<EditText>(R.id.et_name)
        val etMobile = findViewById<EditText>(R.id.et_mobile)

        val userHashMap = HashMap<String, Any>()
        if(mProfileImageURL.isNotEmpty()&& mProfileImageURL!= mUserDetails.image){
            userHashMap["image"]
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if(etName.text.toString()!= mUserDetails.name){
            userHashMap[Constants.NAME] = etName.text.toString()
        }
        if(etMobile.text.toString()!= mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = etMobile.text.toString().toLong()
        }
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    private fun setUpActionBar(){
        val toolbar = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar)
       val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24)
            actionBar.title = resources.getString(R.string.my_profile)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User){
        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etMobile = findViewById<EditText>(R.id.et_mobile)
        mUserDetails = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(ivProfileUserImage)

        etName.setText(user.name)
        etEmail.setText(user.email)
        if(user.mobile!=0L){
            etMobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri!=null){
            val sRef :StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE"+ System.currentTimeMillis()
                        + "."+ Constants.getFileExtension(this,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URl", uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener{
                exception->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}