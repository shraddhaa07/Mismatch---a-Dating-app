package com.example.mismatch3.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mismatch3.R
import com.example.mismatch3.activity.EditProfileActivity
import com.example.mismatch3.auth.LoginActivity
import com.example.mismatch3.databinding.FragmentProfileBinding
import com.example.mismatch3.model.UserModel
import com.example.mismatch3.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)

        // Show loading dialog
        Config.showDialog(requireContext())

        // Get current user's phone number
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        if (currentUserPhoneNumber != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserPhoneNumber)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        // Retrieve user data from Firebase
                        val data = it.getValue(UserModel::class.java)
                        data?.let { userData ->
                            // Set user data to the views
                            binding.name.setText(userData.name)
                            binding.number.setText(userData.number)
                            binding.email.setText(userData.email)
                            binding.city.setText(userData.city)
                            binding.userAge.setText(userData.age)
                            binding.userInterest.setText(userData.interest)

                            // Load profile image using Glide
                            val img = userData.image
                            Glide.with(requireContext()).load(img)
                                .placeholder(R.drawable.girl)
                                .into(binding.userImage)

                            Config.hideDialog()  // Hide loading dialog after data is loaded
                        }
                    } else {
                        // User data not found
                        Config.hideDialog()
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Config.hideDialog()
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Config.hideDialog()
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            // Handle case where user is not logged in or phone number is not available
        }

        // Logout button click listener
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        // Edit Profile button click listener
        binding.editProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }


        return binding.root
    }
}
