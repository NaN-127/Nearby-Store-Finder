package com.nan.nearbystorefinder.core.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleAuthClient(context: Context) {

    private val googleSignInClient: GoogleSignInClient

    init {
        val options = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken("380447933423-k90aknbq3957b3i4fs8vsrkvo8r33a24.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun signOut(){
        googleSignInClient.signOut()
    }

}
