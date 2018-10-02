package com.carlos.ticketsdesoporte

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login_google.*


class LoginGoogle : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val TAG = "JSAGoogleSignIn"
    private val REQUEST_CODE_SIGN_IN = 1234
    private val WEB_CLIENT_ID = "265111237066-nbvu1ohgviqoiv3elop4527fv72qoneo.apps.googleusercontent.com"

    private var mAuth: FirebaseAuth? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var dbreference: DatabaseReference

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_google)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth!!.currentUser != null) {
            this.startActivity(Intent(this, ActividadPrincipal::class.java))
        }


        title = "Iniciar Sessión"

        database = FirebaseDatabase.getInstance()
        dbreference = database.getReference("Usuarios")


        btn_sign_in.setOnClickListener { _ -> signIn() }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()



    }


    private fun signIn() {
        mGoogleApiClient!!.clearDefaultAccountAndReconnect()
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent();
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // successful -> authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                // failed -> update UI
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e(TAG, "firebaseAuthWithGoogle():" + acct.id!!)
        val progressbar = ProgressDialog(this)
        progressbar.setMessage("Registrando Nuevo Usuario")
        progressbar.show()

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.e(TAG, "signInWithCredential: Success!")
                        val user = mAuth!!.currentUser
                        val name = user!!.displayName.toString().trim()
                        val uid_user = user.uid

                        val userBD = dbreference.child(user.uid)
                        userBD.setValue(Usuarios(uid_user, name, user.email!!, "2"))
                        progressbar.dismiss()
                        this.startActivity(Intent(this, ActividadPrincipal::class.java))
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential: Failed!", task.exception)
                        Toast.makeText(applicationContext, "Authentication failed!",
                                Toast.LENGTH_SHORT).show()
                        progressbar.dismiss()
                    }
                }
    }

    override fun onBackPressed() {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "onConnectionFailed():" + connectionResult)
        Toast.makeText(applicationContext, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }
}
