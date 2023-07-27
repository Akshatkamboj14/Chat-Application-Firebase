package com.example.chatapplication.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivitySigninBinding
import com.example.chatapplication.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    lateinit var email: String
    lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    var googleName: String = ""
    var googleEmail: String? = ""
    private var googlePhotoUrl: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.apply {

            SignUpBtn.setOnClickListener {
                val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
            SignInBtn.setOnClickListener {
                val emailText = userEmail.text
                val passText = userPass.text
                email = emailText.toString()
                password = passText.toString()
                validateUserData()
            }
            googleSignInBtn.setOnClickListener {
                signInGoogle()
            }
        }

    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                googleName = account.displayName.toString()
                googleEmail = account.email.toString()
                googlePhotoUrl = account.photoUrl
                uploadData()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }

//    private fun uploadImage() {
//
//        val storageRef = FirebaseStorage.getInstance().getReference("profile")
//            .child(FirebaseAuth.getInstance().currentUser!!.uid)
//            .child("profile.jpg")
//
//        storageRef.putFile(googlePhotoUrl!!)
//            .addOnSuccessListener {
//                storageRef.downloadUrl.addOnSuccessListener {
//                    uploadData(it)
//                }.addOnFailureListener {
//                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener {
//                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun uploadData() {
        val user = User(
            googleName,
            googleEmail,
            FirebaseAuth.getInstance().currentUser!!.uid,
//            ImageUrl.toString()
        )
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Toast.makeText(this, "User saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateUserData() {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@SignInActivity, "Enter Blank Fields", Toast.LENGTH_SHORT).show()
        } else {
            signIn()
        }
    }

    private fun signIn() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                } else {

                    Toast.makeText(
                        baseContext,
                        "User Not Exist",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }
}