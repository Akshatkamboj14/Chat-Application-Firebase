package com.example.chatapplication.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.model.User
import com.example.chatapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    lateinit var auth: FirebaseAuth
    lateinit var email: String
    lateinit var password: String
    lateinit var name: String
    private var imageUri: Uri? = null

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.userImage.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.apply {
            SignInBtn.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            }
            binding.userImage.setOnClickListener {
                selectImage.launch("image/*")
            }
            SignUpBtn.setOnClickListener {
                val nameText = userName.text
                val emailText = userEmail.text
                val passText = userPass.text
                name = nameText.toString()
                email = emailText.toString()
                password = passText.toString()
                validateData()
            }
        }
    }

    private fun validateData() {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || imageUri == null) {
            Toast.makeText(this@SignUpActivity, "Enter Blank Fields", Toast.LENGTH_SHORT).show()
        } else {
            signUp()
        }
    }

    private fun signUp() {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    uploadImage()
                } else {
                    Toast.makeText(
                        baseContext,
                        task.exception.toString(),
                        Toast.LENGTH_LONG,
                    ).show()

                }
            }
    }

    private fun uploadImage() {

        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("profile.jpg")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener {
                    uploadData(it)
                }.addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadData(ImageUrl: Uri?) {
        val user = User(
            name,
            email,
            FirebaseAuth.getInstance().currentUser!!.uid,
            ImageUrl.toString()
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
}