package com.example.trelloclone.activities.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.activities.firebase.FirestoreClass
import com.example.trelloclone.activities.model.User
import com.example.trelloclone.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun userRegisterSuccess(){
        Toast.makeText(this," You have registered", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        binding.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressed() }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val name:String =binding.etName.text.toString().trim(){ it <= ' '}
        val email:String =binding.etEmail.text.toString().trim(){ it <= ' '}
        val password:String =binding.etPassword.text.toString().trim(){ it <= ' '}

        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                   val user = User(firebaseUser.uid,name,registeredEmail)
                    FirestoreClass().registeredUser(this,user)
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateForm(name:String, email:String, password:String):Boolean{
        return when{
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter your password")
                false
            }
            else -> {true}
        }
    }
}