package com.example.trelloclone.activities.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityBaseBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

open class BaseActivity : AppCompatActivity() {

  //  private lateinit var binding: ActivityBaseBinding
    private var doubleBackToExitPressedOne = false
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //    binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_base)


    }

    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text
        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }
    fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOne){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOne = true
        Toast.makeText(this, resources.getString(R.string.please_click_back_again_to_exit),Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            doubleBackToExitPressedOne = false
        },2000)
    }

    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG )
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackBar.show()
    }
}