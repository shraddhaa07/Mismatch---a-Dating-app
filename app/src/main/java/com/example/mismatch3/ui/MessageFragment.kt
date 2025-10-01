package com.example.mismatch3.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mismatch3.adapter.MessageUserAdapter
import com.example.mismatch3.databinding.FragmentMessageBinding
import com.example.mismatch3.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        getData()
        return binding.root
    }

    private fun getData() {
        Config.showDialog(requireContext())
        val currentId = FirebaseAuth.getInstance().currentUser?.phoneNumber

        if (currentId != null) {
            FirebaseDatabase.getInstance().getReference("chats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userList = arrayListOf<String>()
                        val chatKeyList = arrayListOf<String>()

                        for (data in snapshot.children) {
                            val chatKey = data.key ?: continue
                            val participants = chatKey.split("_")
                            if (participants.contains(currentId)) {
                                val otherUserId = participants.first { it != currentId }
                                userList.add(otherUserId)
                                chatKeyList.add(chatKey)
                            }
                        }

                        try {
                            binding.recyclerView.adapter = MessageUserAdapter(requireContext(), userList, chatKeyList)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            Config.hideDialog()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Config.hideDialog()
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Config.hideDialog()
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
