package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.Constants
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
//         Done: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          Done: If the user was authenticated, send him to RemindersActivity

//          Done: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )

        binding.lifecycleOwner = this

        binding.logInButton.setOnClickListener {
            launchSign()
        }
    }

    /* to use AuthUI  / IdpResponse  classes inside firbase package , Firebase  implemented by import this package
     'com.firebaseui:firebase-ui-auth:7.1.1'  in side gradle project

    */
    private fun launchSign() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()


        )


        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            Constants.SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(Constants.TAG_AUTH, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                Toast.makeText(this, "Successfully signed in", Toast.LENGTH_SHORT).show()
                startReminderActivity()

            } else {

                // response.getError().getErrorCode() and handle the error.
                Log.i(Constants.TAG_AUTH, "Sign in failed ${response?.error?.errorCode}")
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startReminderActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }





}
