package com.example.mismatch3.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mismatch3.R
import com.example.mismatch3.databinding.ActivityEditProfileBinding
import com.example.mismatch3.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var currentUserPhoneNumber: String
    private var existingImageUrl: String? = null // Variable to store the existing image URL
    private var selectedImageUri: Uri? = null // URI for the newly selected image

    private val IMAGE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get current user phone number from Firebase Auth
        currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

        // Load current user data from Firebase
        loadUserData()

        // Set up the 'Done' button click listener to save the changes
        binding.done.setOnClickListener {
            saveProfileChanges()
        }

        // Set up the 'Cancel' button click listener to discard changes
        binding.cancel.setOnClickListener {
            finish() // Close the activity without saving
        }

        // Set up the user image click listener to allow changing the image
        binding.userImage.setOnClickListener {
            selectImageFromGallery()
        }
    }

    private fun loadUserData() {
        FirebaseDatabase.getInstance().getReference("users")
            .child(currentUserPhoneNumber)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        binding.name.setText(user.name)
                        binding.number.setText(user.number)
                        binding.email.setText(user.email)
                        binding.userInterest.setText(user.interest)
                        binding.city.setText(user.city)
                        binding.userAge.setText(user.age)

                        // Store the existing image URL
                        existingImageUrl = user.image

                        // Load user profile image using Glide
                        Glide.with(this).load(existingImageUrl).placeholder(R.drawable.girl).into(binding.userImage)
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            selectedImageUri = data?.data
            binding.userImage.setImageURI(selectedImageUri) // Preview the selected image
        }
    }

    private fun saveProfileChanges() {
        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage()
        } else {
            // No new image selected, save profile with the existing image URL
            saveProfileData(existingImageUrl)
        }
    }

    private fun uploadImageToFirebaseStorage() {
        val storageRef = FirebaseStorage.getInstance().getReference("profile_images/${currentUserPhoneNumber}.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        saveProfileData(imageUrl.toString()) // Save profile with the new image URL
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileData(newImageUrl: String?) {
        val updatedUser = UserModel(
            name = binding.name.text.toString(),
            number = binding.number.text.toString(),
            email = binding.email.text.toString(),
            city = binding.city.text.toString(),
            age = binding.userAge.text.toString(),
            interest = binding.userInterest.text.toString(),
            image = newImageUrl // Save the new image URL or existing one if unchanged
        )

        // Update the Firebase database with new user data
        FirebaseDatabase.getInstance().getReference("users")
            .child(currentUserPhoneNumber)
            .setValue(updatedUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}
