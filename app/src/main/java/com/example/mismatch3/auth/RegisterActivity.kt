package com.example.mismatch3.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mismatch3.MainActivity
import com.example.mismatch3.databinding.ActivityRegisterBinding
import com.example.mismatch3.model.UserModel
import com.example.mismatch3.utils.Config
import com.example.mismatch3.utils.Config.hideDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var imageUri: Uri? = null

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.userImage.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve phone number from the intent
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        binding.userImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.saveData.setOnClickListener {
            validateData(phoneNumber)  // Pass the phone number to validation and storing function
        }
    }

    private fun validateData(phoneNumber: String) {
        val userName = binding.userName.text.toString().trim()
        val userEmail = binding.userEmail.text.toString().trim()
        val userInterest = binding.userInterest.text.toString().trim()
        val userCity = binding.userCity.text.toString().trim()
        val userAge= binding.userAge.text.toString().trim()

        // Regular expression for email validation
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|org|net|edu|gov|mil|biz|info|io|me|co|in|us|uk|ca|au)"

        if (userName.isEmpty() ||
            userEmail.isEmpty() ||
            userInterest.isEmpty() ||
            userAge.isEmpty()||
            userCity.isEmpty() ||
            imageUri == null
        ) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
        } else if (!userEmail.matches(emailPattern.toRegex())) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show() // Email validation error message
        } else if (!binding.termsCondition.isChecked) {
            Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage(phoneNumber)
        }
    }


    private fun uploadImage(phoneNumber: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        Config.showDialog(this)
        val storageReference = FirebaseStorage.getInstance().getReference("profile")
            .child(currentUser.uid)
            .child("profile.jpg")

        storageReference.putFile(imageUri!!)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    storeData(uri, phoneNumber)  // Pass the phone number to storeData
                }.addOnFailureListener {
                    hideDialog()
                    Toast.makeText(this, it.message ?: "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                hideDialog()
                Toast.makeText(this, it.message ?: "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(imageUrl: Uri?, phoneNumber: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val data = UserModel(
            name = binding.userName.text.toString(),
            image = imageUrl.toString(),
            email = binding.userEmail.text.toString(),
            age = binding.userAge.text.toString(),
            interest = binding.userInterest.text.toString(),
            city = binding.userCity.text.toString(),
            number = phoneNumber  // Save the phone number
        )

        FirebaseDatabase.getInstance().getReference("users")
            .child(phoneNumber)  // Use the phone number as the key
            .setValue(data)
            .addOnCompleteListener {
                hideDialog()
                if (it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "User registration successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
