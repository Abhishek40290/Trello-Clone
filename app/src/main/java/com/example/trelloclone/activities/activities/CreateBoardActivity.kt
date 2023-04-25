package com.example.trelloclone.activities.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.example.trelloclone.activities.model.Board
import com.example.trelloclone.activities.utils.Constants
import com.example.trelloclone.databinding.ActivityCreateBoardBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri:Uri? = null
    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var ivBoardImage: ImageView
    private lateinit var mUserName: String
    private var mBoardImageUrl: String =""
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivBoardImage = findViewById(R.id.iv_board_image)
        setUpActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        ivBoardImage.setOnClickListener{
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

        btnCreate = findViewById(R.id.btn_create)
        btnCreate.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadBoardImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun createBoard(){
        val etBoardName =findViewById<EditText>(R.id.et_board_name)
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())

        val board = Board(
            etBoardName.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignedUserArrayList
        )
        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef : StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE"+ System.currentTimeMillis()
                        + "."+ Constants.getFileExtension(this,mSelectedImageFileUri))

        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
            Log.i("Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                Log.i("Downloadable Image URl", uri.toString())
                mBoardImageUrl = uri.toString()

                createBoard()
            }
        }.addOnFailureListener{
                exception->
            Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }



    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding.toolbarCreateBoardActivity.setNavigationOnClickListener {
            onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
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
        if(resultCode== Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null)
            mSelectedImageFileUri = data.data

        try {
            Glide
                .with(this)
                .load(mSelectedImageFileUri)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(ivBoardImage)
        }catch (e: IOException){
            e.printStackTrace()
        }

    }
}