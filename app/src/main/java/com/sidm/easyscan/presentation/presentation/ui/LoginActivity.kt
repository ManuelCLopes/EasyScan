package com.sidm.easyscan.presentation.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.SignInButton
import com.sidm.easyscan.R

private const val REQUEST_SIGN_IN = 12345

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setup()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_SIGN_IN && resultCode == Activity.RESULT_OK){
            startActivity(Intent(this, MainActivity::class.java))

            finish()
        }
    }

    private fun setup() {

    findViewById<SignInButton>(R.id.sign_in).setOnClickListener{
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
                REQUEST_SIGN_IN
        )
    }

    }


}