package com.example.mismatch3

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.mismatch3.auth.PrivacyPolicyActivity
import com.example.mismatch3.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber // Get the current user's ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the NavController for bottom navigation
        val navController = findNavController(R.id.nav_fragment)
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // Initialize ActionBarDrawerToggle
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()

        // Ensure the ActionBar is not null before using it
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up NavigationItemSelectedListener for the NavigationView
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.privacy -> {
                startActivity(Intent(this, PrivacyPolicyActivity::class.java))
            }
            R.id.favourite -> {
                showFeedbackDialog() // Show the feedback dialog
            }
            R.id.delete -> {
                showDeleteConfirmationDialog() // Show the confirmation dialog
            }
        }

        // Close the navigation drawer after selecting an item
        binding.drawerLayout.closeDrawers()
        return true
    }

    private fun showFeedbackDialog() {
        // Create an EditText for user feedback
        val feedbackEditText = EditText(this)
        feedbackEditText.hint = "Enter your feedback here"

        // Create and show the AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Feedback")
            .setMessage("Please provide your feedback")
            .setView(feedbackEditText)
            .setPositiveButton("Submit") { dialog: DialogInterface, _: Int ->
                val feedback = feedbackEditText.text.toString().trim()
                if (feedback.isNotEmpty()) {
                    submitFeedback(feedback) // Call function to submit feedback
                } else {
                    Toast.makeText(this, "Feedback cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss() // Close dialog if Cancel is clicked
            }
            .create()
            .show()
    }

    // Function to submit feedback to Firebase
    private fun submitFeedback(feedback: String) {
        userId?.let { uid ->
            // Create a feedback entry
            val feedbackData = mapOf("feedback" to feedback)

            // Store feedback in the database under the user's phone number
            FirebaseDatabase.getInstance().getReference("feedback")
                .child(uid) // Use user ID as the key
                .setValue(feedbackData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete your profile?")
            .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                deleteUserProfile() // Call function to delete user profile
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                dialog.dismiss() // Close dialog if No is clicked
            }
            .create()
            .show()
    }

    // Function to delete the user profile from Firebase
    private fun deleteUserProfile() {
        userId?.let { uid ->
            // Remove user data from the database
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            databaseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Delete the user from Firebase Auth
                    FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show()
                            // Optionally, log the user out or navigate to the login page
                            FirebaseAuth.getInstance().signOut()
                            finish() // Close the activity
                        } else {
                            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle the ActionBarDrawerToggle click events
        return if (actionBarDrawerToggle?.onOptionsItemSelected(item) == true) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
