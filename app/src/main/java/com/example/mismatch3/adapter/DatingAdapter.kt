package com.example.mismatch3.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mismatch3.activity.MessageActivity
import com.example.mismatch3.databinding.ItemUserLayoutBinding
import com.example.mismatch3.model.UserModel

class DatingAdapter (
    val context : Context,
    val list : ArrayList<UserModel>): RecyclerView.Adapter<DatingAdapter.DatingViewHolder> (){
    inner class DatingViewHolder(val binding:ItemUserLayoutBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatingViewHolder {

        return DatingViewHolder(ItemUserLayoutBinding.inflate(LayoutInflater.from(context)
            ,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DatingViewHolder, position: Int) {

        holder.binding.textView3.text = list[position].name
        holder.binding.textView4.text = list[position].age
        holder.binding.textView5.text = list [position].interest

        Glide.with(context).load(list[position].image).into(holder.binding.userImage)

        holder.binding.chat.setOnClickListener{
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("userId", list[position].number)
            context.startActivity(intent)         //will start the message activity and assign unique userId
        }



    }
}