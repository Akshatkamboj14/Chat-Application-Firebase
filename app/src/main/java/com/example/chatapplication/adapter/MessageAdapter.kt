package com.example.chatapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.model.Message
import com.example.chatapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageAdapter(val context: Context, private val list: ArrayList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val MSG_TYPE_RIGHT = 0
    private val MSG_TYPE_LEFT = 1

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.messageText) as TextView
        val image = itemView.findViewById(R.id.senderImage) as ImageView
        val timeTv = itemView.findViewById(R.id.timeTv) as TextView
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderUid!! == FirebaseAuth.getInstance().currentUser!!.uid){
            MSG_TYPE_RIGHT
        }else{
            MSG_TYPE_LEFT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            MessageViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_reciever_message, parent, false)
            )
        } else {
            MessageViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_sender_message, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.text.text = list[position].message
        holder.timeTv.text = list[position].currentTime

        FirebaseDatabase.getInstance().getReference("users")
            .child(list[position].senderUid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val data = snapshot.getValue(User::class.java)
                            Glide.with(context).load(data!!.imageUrl)
                                .placeholder(R.drawable.placeholder)
                                .into(holder.image)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
    }

}