package com.example.mismatch3.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mismatch3.R
import com.example.mismatch3.model.MessageModel
import com.example.mismatch3.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessageAdapter(val context: Context, private val list: List<MessageModel>)
    : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val MSG_TYPE_RIGHT = 0
    private val MSG_TYPE_LEFT = 1
    private val userImageCache = mutableMapOf<String, String>()

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.messageText)
        val image: ImageView = itemView.findViewById(R.id.senderImage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == FirebaseAuth.getInstance().currentUser?.phoneNumber) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == MSG_TYPE_RIGHT) {
            R.layout.layout_sender_message
        } else {
            R.layout.layout_receiver_message
        }
        return MessageViewHolder(LayoutInflater.from(context).inflate(layoutId, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.text.text = list[position].message

        // Fetch sender's image from Firebase with caching
        val senderId = list[position].senderId
        if (!senderId.isNullOrEmpty()) {
            if (userImageCache.containsKey(senderId)) {
                Glide.with(context).load(userImageCache[senderId]).into(holder.image)
            } else {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(senderId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val data = snapshot.getValue(UserModel::class.java)
                                if (data != null && data.image != null) {
                                    userImageCache[senderId] = data.image // Cache the image URL
                                    Glide.with(context).load(data.image).placeholder(R.drawable.girl).into(holder.image)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }
}
