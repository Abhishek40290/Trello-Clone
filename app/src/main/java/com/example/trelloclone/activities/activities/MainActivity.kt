package com.example.trelloclone.activities.activities

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.activities.adapters.BoardItemsAdapter
import com.example.trelloclone.activities.firebase.FirestoreClass
import com.example.trelloclone.activities.model.Board
import com.example.trelloclone.activities.model.User
import com.example.trelloclone.activities.utils.Constants
import com.example.trelloclone.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int =11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var binding:ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fabCreate:FloatingActionButton
    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        setUpActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.PROJECT_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences
            .getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }else{
            FirebaseMessaging.getInstance()
                .token.addOnSuccessListener(this@MainActivity){
                token ->
                updateFCMToken(token)           //changed
            }.addOnFailureListener{exception ->
                    Log.e(TAG, "Error getting FCM token: ", exception)
                }
        }

        FirestoreClass().loadUserData(this, true)

        fabCreate = findViewById(R.id.fab_create_board)
        fabCreate.setOnClickListener{
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun populateBoardListToUI(boardsList: ArrayList<Board>){
        val rvBoardsList1 = findViewById<RecyclerView>(R.id.rv_boards_list)
        val tvNoBoardAvailable = findViewById<TextView>(R.id.tv_no_boards_available)
        hideProgressDialog()
        if(boardsList.isNotEmpty()){
            rvBoardsList1.visibility = View.VISIBLE
            tvNoBoardAvailable.visibility = View.GONE

            rvBoardsList1.layoutManager = LinearLayoutManager(this)
            rvBoardsList1.setHasFixedSize(true)

            val adapter =BoardItemsAdapter(this, boardsList)
            rvBoardsList1.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            rvBoardsList1.visibility = View.GONE
            tvNoBoardAvailable.visibility = View.VISIBLE
        }
    }

    private fun setUpActionBar(){
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
         drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
             doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardsList:Boolean){
        hideProgressDialog()
        mUserName = user.name
        val navUserImage = findViewById<ImageView>(R.id.nav_user_image)
        val tvUsername = findViewById<TextView>(R.id.tv_username)
       Glide
           .with(this)
           .load(user.image)
           .centerCrop()
           .placeholder(R.drawable.ic_user_place_holder)
           .into(navUserImage)

        tvUsername.text = user.name
        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoarsList(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode==Activity.RESULT_OK
            && requestCode== CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoarsList(this)
        }else{
            Log.e("Cancelled", "Cancelled " )
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile ->{
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

}