package com.example.mismatch3.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mismatch3.adapter.MessageAdapter
import com.example.mismatch3.databinding.ActivityMessageBinding
import com.example.mismatch3.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageAdapter: MessageAdapter
    private var messageList = arrayListOf<MessageModel>()
    private var senderId: String? = null
    private var receiverId: String? = null
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        messageAdapter = MessageAdapter(this, messageList)
        binding.recyclerView2.layoutManager = LinearLayoutManager(this)
        binding.recyclerView2.adapter = messageAdapter

        // Retrieve user information
        retrieveUserInfo()

        // Button click listener for sending a message
        binding.imageView10.setOnClickListener {
            val messageText = binding.yourMessage.text.toString()
            if (messageText.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter your message", Toast.LENGTH_SHORT).show()
            } else {
                checkIfUserExistsAndSendMessage(messageText) // Check if user exists before sending message
            }
        }
    }

    private fun retrieveUserInfo() {
        receiverId = intent.getStringExtra("userId")
        senderId = FirebaseAuth.getInstance().currentUser?.phoneNumber

        if (receiverId.isNullOrEmpty() || senderId.isNullOrEmpty()) {
            Toast.makeText(this, "Sender or Receiver ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        verifyChatId()
    }

    private fun verifyChatId() {
        chatId = createSortedChatId(senderId!!, receiverId!!)
        val reference = FirebaseDatabase.getInstance().getReference("chats")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(chatId!!)) {
                    Log.d("MessageActivity", "Chat exists with ID: $chatId")
                    getData(chatId)
                } else {
                    reference.child(chatId!!).setValue("").addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("MessageActivity", "New chat created with ID: $chatId")
                            getData(chatId)
                        } else {
                            Toast.makeText(this@MessageActivity, "Failed to create chat", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageActivity, "Something went wrong: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to sort senderId and receiverId and create a consistent chatId
    private fun createSortedChatId(senderId: String, receiverId: String): String {
        val ids = listOf(senderId, receiverId).sorted()
        return ids.joinToString("_")
    }

    private fun getData(chatId: String?) {
        FirebaseDatabase.getInstance().getReference("chats").child(chatId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (show in snapshot.children) {
                        val message = show.getValue(MessageModel::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.recyclerView2.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MessageActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkIfUserExistsAndSendMessage(msg: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User exists, send the message
                    storeData(msg)
                } else {
                    // User does not exist, send a deleted account message
                    val deletedAccountMessage = "This user has deleted their account."
                    storeData(deletedAccountMessage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageActivity, "Error checking user existence: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun storeData(msg: String) {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date())

        val map = hashMapOf<String, String>()
        map["message"] = msg
        map["senderId"] = senderId ?: ""
        map["currentTime"] = currentTime
        map["currentDate"] = currentDate

        val reference = FirebaseDatabase.getInstance().getReference("chats").child(chatId!!)
        reference.child(reference.push().key!!).setValue(map).addOnCompleteListener {
            if (it.isSuccessful) {
                binding.yourMessage.text = null
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
