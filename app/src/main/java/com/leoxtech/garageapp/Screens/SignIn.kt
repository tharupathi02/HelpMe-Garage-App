package com.leoxtech.garageapp.Screens

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.customerapp.Model.UserModel
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userRef: DatabaseReference
    private lateinit var dialog: AlertDialog

    private var user: FirebaseUser? = null

    companion object {
        private const val RC_SIGN_IN = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogBox()

        clickListeners()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        mAuth = FirebaseAuth.getInstance()

    }

    private fun signIn() {
        dialog.show()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        binding.cardGoogleSignIn.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account?.idToken!!)
                }catch (e: ApiException) {
                    Snackbar.make(binding.root, "Authentication Failed. ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }else{
                Snackbar.make(binding.root, "Authentication Failed. ${exception?.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    initial()
                } else {
                    Snackbar.make(binding.root, "Authentication Failed. ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun initial() {
        userRef = FirebaseDatabase.getInstance().getReference(Common.GARAGE_USER_REFERENCE)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        if (user != null){
            checkUserFromFirebase(user!!)
        }
    }

    private fun checkUserFromFirebase(user: FirebaseUser) {
        userRef!!.child(user!!.uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val userModel = snapshot.getValue(UserModel::class.java)
                    goToHomeActivity(userModel)
                }else{
                    showRegistration(user!!)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Snackbar.make(binding.root, "${p0.message}", Snackbar.LENGTH_SHORT).show()
            }

        })
    }

    private fun showRegistration(user: FirebaseUser) {
        dialog.dismiss()
        startActivity(Intent(this, SignUpPage2::class.java))
        finish()
        Snackbar.make(binding.root, "Please complete your registration.", Snackbar.LENGTH_SHORT).show()
    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser = userModel!!
        dialog.dismiss()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun clickListeners() {
        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpPage1::class.java))
        }

        binding.cardGoogleSignIn.setOnClickListener {
            signIn()
        }

        binding.btnSignIn.setOnClickListener {
            signInEmailPassword()
        }
    }

    private fun signInEmailPassword() {
        dialog.show()
        val email = binding.txtEmail.editText?.text.toString()
        val password = binding.txtPassword.editText?.text.toString()
        if (email.isEmpty() || password.isEmpty()){
            Snackbar.make(binding.root, "Please fill all the fields.", Snackbar.LENGTH_SHORT).show()
            dialog.dismiss()
        }else{
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        user = mAuth.currentUser
                        initial()
                    }else{
                        Snackbar.make(binding.root, "Authentication Failed. ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
        }
    }

    private fun dialogBox() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialog = it
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

}