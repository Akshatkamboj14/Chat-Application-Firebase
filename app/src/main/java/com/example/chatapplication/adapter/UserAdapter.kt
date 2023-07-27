package com.example.chatapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.databinding.UseritemBinding
import com.example.chatapplication.model.User
import com.example.chatapplication.ui.ChatActivity

class UserAdapter(private val context: Context, private val list: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: UseritemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UseritemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
         holder.binding.apply {
             userName.text = user.name
             Glide.with(context).load(user.imageUrl).into(userImage)
         }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid", user.uid)
            intent.putExtra("name", user.name)
            intent.putExtra("imageUrl", user.imageUrl)
            context.startActivity(intent)
        }
    }


}